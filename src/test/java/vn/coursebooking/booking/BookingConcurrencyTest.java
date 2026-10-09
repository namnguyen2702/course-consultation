package vn.coursebooking.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BookingConcurrencyTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("booking_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("database/schema.sql"),
                            "/docker-entrypoint-initdb.d/01-schema.sql"
                    );

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    BookingService bookingService;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void onlyOneCustomerCanBookTheSameSlot() throws Exception {
        createCustomer("first@example.com");
        createCustomer("second@example.com");

        Long courseId = jdbc.queryForObject("""
                INSERT INTO courses (name, duration_weeks)
                VALUES ('Java test', 8)
                RETURNING id
                """, Long.class);

        Long consultantId = jdbc.queryForObject("""
                INSERT INTO consultants (full_name, expertise)
                VALUES ('Consultant test', 'Java')
                RETURNING id
                """, Long.class);

        LocalDateTime startAt = LocalDateTime
                .now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .plusDays(7)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        Long slotId = jdbc.queryForObject("""
                INSERT INTO consultation_slots (consultant_id, start_at)
                VALUES (?, ?)
                RETURNING id
                """, Long.class, consultantId, Timestamp.valueOf(startAt));

        CreateBookingRequest request = new CreateBookingRequest(courseId, slotId);

        // Wait until both customers are ready, then release them together.
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Integer> first = executor.submit(() ->
                    book("first@example.com", request, ready, start));
            Future<Integer> second = executor.submit(() ->
                    book("second@example.com", request, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Both customers must be ready before starting");
            start.countDown();

            List<Integer> results = List.of(
                    first.get(30, TimeUnit.SECONDS),
                    second.get(30, TimeUnit.SECONDS)
            ).stream().sorted().toList();

            assertEquals(List.of(201, 409), results);

            Long bookingCount = jdbc.queryForObject("""
                    SELECT COUNT(*)
                    FROM bookings
                    WHERE slot_id = ? AND status = 'CONFIRMED'
                    """, Long.class, slotId);
            assertEquals(Long.valueOf(1), bookingCount);
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void createCustomer(String email) {
        jdbc.update("""
                INSERT INTO users (full_name, email, password_hash, role)
                VALUES (?, ?, ?, 'CUSTOMER')
                """, "Customer test", email, "unused-test-hash");
    }

    private int book(String email, CreateBookingRequest request,
                     CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        start.await();

        try {
            // Each call enters its own transaction through the Spring service.
            bookingService.createBooking(email, request);
            return 201;
        } catch (ResponseStatusException exception) {
            return exception.getStatusCode().value();
        }
    }
}
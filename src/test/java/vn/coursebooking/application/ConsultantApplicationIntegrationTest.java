package vn.coursebooking.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;
import vn.coursebooking.booking.*;
import vn.coursebooking.consultant.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ConsultantApplicationIntegrationTest {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("application_test").withUsername("test").withPassword("test")
            .withCopyFileToContainer(MountableFile.forHostPath("database/schema.sql"), "/docker-entrypoint-initdb.d/01-schema.sql");
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired ConsultantApplicationService applications;
    @Autowired ConsultantWorkspaceService workspace;
    @Autowired ConsultantService consultantService;
    @Autowired BookingService bookingService;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    String applicantEmail;
    String adminEmail;
    Long applicantId;
    Long courseId;
    LocalDateTime workingDate;

    @BeforeEach
    void fixture() {
        String unique = UUID.randomUUID().toString();
        applicantEmail = "applicant-" + unique + "@example.com";
        adminEmail = "admin-" + unique + "@example.com";
        applicantId = insertUser(applicantEmail, "CUSTOMER");
        insertUser(adminEmail, "ADMIN");
        courseId = jdbc.queryForObject("INSERT INTO courses(name,duration_weeks) VALUES ('Java application test',8) RETURNING id", Long.class);
        workingDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .plusDays(7).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
    Long insertUser(String email, String role) {
        return jdbc.queryForObject("INSERT INTO users(full_name,email,password_hash,role) VALUES ('Test user',?,'test-hash',?) RETURNING id",
                Long.class, email, role);
    }
    ConsultantApplicationRequest request() {
        return new ConsultantApplicationRequest("0901234567", "Java Backend", 2, "Experience teaching Java", "https://github.com/example",
                Set.of(workingDate.getDayOfWeek().getValue()), LocalTime.of(9,0), LocalTime.of(17,0), Set.of(courseId));
    }
    Long submit() {
        applications.submit(applicantEmail, request());
        return applications.latest(applicantEmail).id();
    }
    void approve() { applications.review(submit(), adminEmail, "APPROVED", "Reviewed profile and working hours"); }
    Long profileId() {
        return jdbc.queryForObject("SELECT id FROM consultants WHERE user_id = ?", Long.class, applicantId);
    }
    @Test
    void pendingApplicationDoesNotGrantAccessAndCannotBeSubmittedTwice() throws Exception {
        submit();
        assertEquals("CUSTOMER", jdbc.queryForObject("SELECT role FROM users WHERE id = ?", String.class, applicantId));
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM consultants WHERE user_id = ?", Long.class, applicantId));
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class, () -> submit()).getStatusCode());
        mvc.perform(get("/consultant/slots").with(user(applicantEmail).roles("CUSTOMER"))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/applications/1/review").with(user(applicantEmail).roles("CUSTOMER")).with(csrf())
                .param("decision","APPROVED")).andExpect(status().isForbidden());
        mvc.perform(get("/consultant-application").with(user(applicantEmail).roles("CUSTOMER")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Đang chờ admin duyệt")));
    }
    @Test
    void approvalCreatesLinkedProfileAndRendersBothWorkspaces() throws Exception {
        Long applicationId = submit();
        mvc.perform(get("/admin/applications").with(user(adminEmail).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Java Backend")));
        applications.review(applicationId, adminEmail, "APPROVED", "Verified");
        assertEquals("CONSULTANT", jdbc.queryForObject("SELECT role FROM users WHERE id = ?", String.class, applicantId));
        assertEquals("APPROVED", applications.latest(applicantEmail).status());
        assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM consultants WHERE user_id = ?", Long.class, applicantId));
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> applications.review(applicationId, adminEmail, "APPROVED", "Again")).getStatusCode());
        mvc.perform(get("/consultant/slots").with(user(applicantEmail).roles("CONSULTANT")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Lịch làm việc được duyệt")));
        mvc.perform(get("/consultant/bookings").with(user(applicantEmail).roles("CONSULTANT"))).andExpect(status().isOk());
        mvc.perform(get("/consultant-application").with(user(applicantEmail).roles("CONSULTANT"))).andExpect(status().isOk());
    }
    @Test
    void rejectionNeedsReasonAndAllowsNewApplication() {
        Long id = submit();
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> applications.review(id, adminEmail, "REJECTED", " ")).getStatusCode());
        assertEquals("PENDING", applications.latest(applicantEmail).status());
        applications.review(id, adminEmail, "REJECTED", "Need more experience");
        assertEquals("CUSTOMER", jdbc.queryForObject("SELECT role FROM users WHERE id = ?", String.class, applicantId));
        Long second = submit();
        assertNotEquals(id, second);
    }
    @Test
    void submittedHoursAndPortfolioAreValidated() {
        var valid = request();
        var invalid = new ConsultantApplicationRequest(valid.phone(), valid.expertise(), 2, valid.bio(), "javascript:alert(1)",
                valid.workDays(), valid.availableFrom(), valid.availableUntil(), valid.courseIds());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> applications.submit(applicantEmail, invalid)).getStatusCode());
        assertThrows(ResponseStatusException.class, () -> ConsultantApplicationService.validateHours(LocalTime.of(17,0), LocalTime.of(9,0)));
        assertThrows(ResponseStatusException.class, () -> ConsultantApplicationService.validateHours(LocalTime.of(9,15), LocalTime.of(17,0)));
    }
    @Test
    void invalidFormRendersErrorsAndKeepsInput() throws Exception {
        mvc.perform(post("/consultant-application").with(user(applicantEmail).roles("CUSTOMER")).with(csrf())
                .param("phone","0901234567").param("expertise","Java Backend").param("experienceYears","2")
                .param("bio","Experience teaching").param("availableFrom","09:00").param("availableUntil","17:00"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("applicationForm","workDays","courseIds"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java Backend")));
    }
    @Test
    void slotsMustFitApprovedDaysAndWholeThirtyMinuteWindow() throws Exception {
        approve();
        workspace.createSlot(applicantEmail, workingDate);
        assertEquals(1, workspace.mySlots(applicantEmail).size());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> workspace.createSlot(applicantEmail, workingDate.plusDays(1))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> workspace.createSlot(applicantEmail, workingDate.withHour(17))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> workspace.createSlot(applicantEmail, workingDate)).getStatusCode());
        mvc.perform(get("/consultant/slots").with(user(applicantEmail).roles("CONSULTANT"))).andExpect(status().isOk());
    }
    @Test
    void consultantCannotDeactivateOthersSlotOrBookedSlot() {
        approve();
        Long other = jdbc.queryForObject("INSERT INTO consultants(full_name,expertise) VALUES ('Other','SQL') RETURNING id",Long.class);
        Long foreignSlot = jdbc.queryForObject("INSERT INTO consultation_slots(consultant_id,start_at) VALUES (?,?) RETURNING id",
                Long.class,other,java.sql.Timestamp.valueOf(workingDate));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> workspace.deactivateSlot(applicantEmail, foreignSlot)).getStatusCode());
        workspace.createSlot(applicantEmail,workingDate);
        Long ownSlot = workspace.mySlots(applicantEmail).get(0).id();
        String customer = "customer-" + UUID.randomUUID() + "@example.com";
        insertUser(customer,"CUSTOMER");
        bookingService.createBooking(customer,new CreateBookingRequest(courseId,ownSlot));
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> workspace.deactivateSlot(applicantEmail,ownSlot)).getStatusCode());
        assertEquals(1, workspace.myBookings(applicantEmail).size());
    }
    @Test
    void consultantIsOnlyOfferedForApprovedCourses() {
        approve();
        Long otherCourse = jdbc.queryForObject("INSERT INTO courses(name,duration_weeks) VALUES ('SQL test',4) RETURNING id",Long.class);
        assertTrue(consultantService.getConsultantsForCourse(courseId).stream().anyMatch(c -> c.id().equals(profileId())));
        assertFalse(consultantService.getConsultantsForCourse(otherCourse).stream().anyMatch(c -> c.id().equals(profileId())));
        workspace.createSlot(applicantEmail,workingDate);
        String customer = "customer-" + UUID.randomUUID() + "@example.com";
        insertUser(customer,"CUSTOMER");
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> bookingService.createBooking(customer,new CreateBookingRequest(otherCourse,workspace.mySlots(applicantEmail).get(0).id()))).getStatusCode());
    }
    @Test
    void simultaneousReviewsOnlyCreateOneProfile() throws Exception {
        Long id = submit();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Integer> review = () -> {
            ready.countDown(); start.await();
            try { applications.review(id,adminEmail,"APPROVED",""); return 200; }
            catch (ResponseStatusException exception) { return exception.getStatusCode().value(); }
        };
        try {
            Future<Integer> first = executor.submit(review);
            Future<Integer> second = executor.submit(review);
            assertTrue(ready.await(10,TimeUnit.SECONDS)); start.countDown();
            assertEquals(List.of(200,409),List.of(first.get(30,TimeUnit.SECONDS),second.get(30,TimeUnit.SECONDS)).stream().sorted().toList());
            assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM consultants WHERE user_id=?",Long.class,applicantId));
        } finally { start.countDown(); executor.shutdownNow(); executor.awaitTermination(5,TimeUnit.SECONDS); }
    }
}

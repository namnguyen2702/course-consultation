package vn.coursebooking.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().strip()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã được đăng ký"
            );
        }

        // BCrypt giới hạn 72 byte; ký tự tiếng Việt có thể chiếm nhiều byte.
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu vượt quá giới hạn 72 byte UTF-8"
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        UserEntity entity = new UserEntity(
                request.fullName().strip(),
                email,
                passwordHash
        );

        try {
            UserEntity savedEntity = userRepository.saveAndFlush(entity);

            return new UserResponse(
                    savedEntity.getId(),
                    savedEntity.getFullName(),
                    savedEntity.getEmail(),
                    savedEntity.getRole()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể đăng ký do xung đột dữ liệu",
                    exception
            );
        }
    }
}
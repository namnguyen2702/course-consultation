# Course Consultation

Ứng dụng đặt lịch tư vấn lựa chọn khóa học lập trình.

[![Java CI](https://github.com/namnguyen2702/course-consultation/actions/workflows/ci.yml/badge.svg)](https://github.com/namnguyen2702/course-consultation/actions/workflows/ci.yml)
## Công nghệ

- Java 17, Spring Boot 4
- Spring MVC, Thymeleaf, HTML/CSS
- Spring Security: đăng nhập bằng session, phân quyền, CSRF
- Spring Data JPA, PostgreSQL 17
- Docker Compose
- JUnit, Mockito, MockMvc
- Maven

## Chức năng

### Khách hàng

- Đăng ký, đăng nhập, đăng xuất.
- Xem khóa học và tư vấn viên.
- Chọn khung giờ còn trống và đặt lịch.
- Xem lịch đã đặt.
- Hủy lịch trước giờ bắt đầu ít nhất 2 giờ.

### Admin

- Thêm, sửa và ngừng mở khóa học.
- Thêm, sửa hồ sơ tư vấn viên.
- Tạo và ngừng nhận khung giờ chưa được đặt.
- Xem lịch của tất cả khách hàng.
- Ghi nhận hoàn thành hoặc vắng mặt sau khi buổi tư vấn kết thúc.

## Quy định đặt lịch

- Mỗi buổi kéo dài 30 phút, bắt đầu ở phút 00 hoặc 30.
- Chỉ được đặt khung giờ đang hoạt động và nằm trong tương lai.
- Một khung giờ chỉ có một booking chưa hủy.
- Một khách hàng không được đặt nhiều lịch cùng giờ.
- Khách hàng chỉ được hủy lịch của mình.
- Ngừng mở khóa học không xóa lịch sử booking.

## Chạy trên máy local

Yêu cầu: JDK 17, Maven và Docker Desktop đang chạy.

### 1. Chuẩn bị cấu hình

Tại thư mục gốc dự án, chạy trong PowerShell:

```powershell
Copy-Item src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Chỉ sao chép khi chưa có file local để tránh ghi đè cấu hình riêng.

### 2. Khởi động PostgreSQL

```powershell
docker compose up -d
docker compose logs postgres
```

Chờ PostgreSQL sẵn sàng nhận kết nối.

Khi volume dữ liệu mới được khởi tạo, PostgreSQL chạy
`database/schema.sql` để tạo bảng và ràng buộc.

File này không tự chạy lại trên volume đã có dữ liệu.
Nó không chứa tài khoản hoặc dữ liệu mẫu.

### 3. Chạy ứng dụng

```powershell
mvn spring-boot:run
```

Mở:

- Trang khóa học: http://localhost:8082/courses
- Đăng ký: http://localhost:8082/register
- Đăng nhập: http://localhost:8082/login

Profile mặc định là `local`.

## Tạo tài khoản admin ở môi trường local mới

1. Đăng ký tài khoản tại `/register` bằng email do bạn chọn.
2. Mở PostgreSQL:

```powershell
docker compose exec postgres psql -U course_user -d course_consultation
```

3. Thay email trong câu lệnh bằng email vừa đăng ký:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin@example.com';

SELECT id, full_name, email, role
FROM users
WHERE email = 'admin@example.com';
```

Lệnh UPDATE phải cập nhật đúng một dòng.

4. Thoát psql bằng `\q`.
5. Đăng xuất rồi đăng nhập lại để phiên nhận quyền mới.

Chỉ người quản lý database thực hiện việc cấp quyền admin.
Form đăng ký thông thường luôn tạo tài khoản CUSTOMER.

Admin có thể tạo khóa học, tư vấn viên và khung giờ trên website.

## Chạy test

```powershell
mvn test
```

Các test hiện kiểm tra:

- Từ chối khung giờ đã được đặt.
- Từ chối khách hàng đặt nhiều lịch cùng giờ.
- Từ chối hủy lịch không thuộc khách hàng.
- Từ chối hủy khi còn dưới 2 giờ.
- Phân quyền HTTP và kiểm tra CSRF.

Test sử dụng service hoặc repository giả.
Chưa kiểm thử tích hợp việc khóa và ràng buộc trên PostgreSQL thật.

## Cấu hình deploy

Chọn profile `prod` và cung cấp biến môi trường:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`: JDBC URL của PostgreSQL
- `DB_USERNAME`
- `DB_PASSWORD`
- `PORT`: tùy chọn, mặc định 8082

Profile prod yêu cầu HTTPS để trình duyệt gửi cookie đăng nhập.

Database deploy phải được tạo cấu trúc trước khi chạy ứng dụng.
Hibernate dùng `ddl-auto=validate`, không tự tạo bảng.

Không dùng mật khẩu database demo cho môi trường deploy.

## Phạm vi hiện tại

- Tư vấn viên là hồ sơ do admin quản lý, chưa có tài khoản riêng.
- Tư vấn viên chưa được phân công theo từng khóa học.
- Danh sách giờ của admin hiện chỉ hiển thị giờ tương lai còn trống.
- Chưa có thanh toán, email thông báo hoặc AI.
- Dữ liệu và trung tâm sử dụng trong demo là giả lập.
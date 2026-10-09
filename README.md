# Đặt lịch tư vấn lựa chọn khóa học lập trình

Dự án học backend Java qua từng bước. Phiên bản hiện tại là bài 01:
API đọc danh sách khóa học, dùng dữ liệu demo trong bộ nhớ.
Chưa có database, tài khoản, booking hoặc AI.

## Công cụ và cách chạy

- JDK 17 hoặc 21; bước này dùng JDK 17 đã có trên máy.
- IntelliJ IDEA và Maven (máy hiện có Maven 3.9.9).
- Postman hoặc trình duyệt để thử GET API.

Trong IntelliJ: mở thư mục dự án, chọn mở như Maven project nếu được hỏi.
Đặt Project SDK và Maven Runner JRE thành JDK 17.
Đợi Maven tải thư viện, chạy `CourseConsultationApplication.main()`.

Hoặc từ PowerShell ở thư mục dự án:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
mvn spring-boot:run
```

JAVA_HOME ở trên chỉ thay đổi cho terminal hiện tại. Lần đầu Maven cần Internet.
Nếu cổng 8080 đang được dùng, chạy với cổng khác:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

## Bài 01: đọc hiểu luồng API

Mở http://localhost:8080/api/courses hoặc tạo request GET cùng URL trong Postman.
Kết quả mong đợi: HTTP 200, một mảng JSON gồm ba khóa học.
Trang gốc `/` chưa có giao diện nên có thể trả về 404.

Luồng xử lý:

```text
Trình duyệt/Postman
    -> GET /api/courses
    -> CourseController.getAllCourses()
    -> CourseService.getAllCourses()
    -> List<Course>
    -> Spring chuyển thành JSON và trả HTTP response
```

Đọc code theo thứ tự:

1. `CourseConsultationApplication`: điểm khởi động. `@SpringBootApplication`
   bật cấu hình tự động và tìm các thành phần Spring trong package này và package con.
2. `Course`: hình dạng dữ liệu của một khóa học. `record` là kiểu dữ liệu Java
   tự tạo constructor, các phương thức đọc như `name()`, cùng equals/hashCode/toString.
   Ta chưa cần viết nhiều getter/setter cho dữ liệu chỉ đọc ở bài này.
3. `CourseService`: nơi cung cấp dữ liệu và sau này xử lý nghiệp vụ.
   `@Service` giúp Spring tạo và quản lý đối tượng này.
4. `CourseController`: tiếp nhận HTTP request. `@RequestMapping` đặt đường dẫn,
   `@GetMapping` chọn phương thức GET. `@RestController` cho phép trả dữ liệu trong response.
   Spring truyền Service vào constructor; không cần tự gọi `new CourseService()`.
5. `pom.xml`: danh sách thư viện và cấu hình build. Maven tải thư viện;
   không cần tải từng file JAR. Starter Web MVC cung cấp các thành phần cho HTTP API.
6. `application.properties`: tên ứng dụng và cổng chạy.

Controller tập trung vào HTTP; Service tập trung vào dữ liệu/nghiệp vụ.
Ở bài này Service rất ngắn để thấy rõ hai trách nhiệm trước khi thêm database.
`List.of` tạo danh sách không sửa trực tiếp được: chưa thể thêm khóa bằng POST.

## Tự thực hành

1. Thêm khóa thứ tư vào `List.of`, dùng id mới, rồi khởi động lại ứng dụng.
2. Gọi GET và kiểm tra khóa vừa thêm.
3. Đổi `durationWeeks` của một khóa và kiểm tra JSON.
4. Thử GET `/api/course` (thiếu chữ s) và quan sát HTTP 404.

Bạn hiểu bài khi giải thích được vì sao URL gọi đúng Controller,
Service được tạo ở đâu, và tại sao kết quả Java trở thành JSON.

## Các bước tiếp theo

1. PostgreSQL + Repository: thay dữ liệu trong bộ nhớ bằng dữ liệu trong bảng.
2. Chi tiết khóa học và lỗi 404 khi id không tồn tại.
3. Tài khoản, mật khẩu được hash, phân quyền khách/tư vấn viên/admin.
4. Khung giờ và booking: đặt/hủy, transaction và chống trùng lịch.
5. Chat hỏi đáp dựa trên tài liệu, có nguồn; kiểm soát timeout và lượt sử dụng.
6. Giao diện demo, tài liệu API và triển khai.

Làm từng bước có thể chạy và giải thích được. Dùng một ứng dụng chia theo chức năng.
Viết test cho nghiệp vụ có nguy cơ sai như phân quyền và booking đồng thời.
Không commit API key hoặc mật khẩu thật. Dữ liệu hiện tại hoàn toàn là dữ liệu demo.

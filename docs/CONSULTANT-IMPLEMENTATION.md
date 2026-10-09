# Những thay đổi cho đăng ký tư vấn viên

Đã triển khai trực tiếp trong `E:\Codex\course-consultation`.

## Luồng hoạt động

Tài khoản CUSTOMER gửi hồ sơ → PENDING → admin xem chuyên môn và lịch làm việc → APPROVED hoặc REJECTED.
Duyệt tạo hồ sơ tư vấn viên gắn tài khoản, sao chép ngày/giờ và khóa học, đổi quyền sang CONSULTANT trong cùng transaction.
Người được duyệt đăng xuất/đăng nhập lại để cập nhật session; tự mở khung giờ 30 phút và xem booking thuộc mình.
Admin từ chối phải ghi lý do; ứng viên có thể gửi đơn mới, đơn cũ vẫn lưu.

## Java tạo mới

Các đường dẫn dưới đây nằm trong `src/main/java/vn/coursebooking/`.

| File | Công việc |
|---|---|
| `application/ConsultantApplicationRequest.java` | Record nhận input: phone, expertise, experienceYears, bio, portfolioUrl, workDays, availableFrom/Until, courseIds; validation và giá trị form ban đầu. |
| `application/ConsultantApplicationEntity.java` | Lưu đơn, trạng thái, người/ngày duyệt, ghi chú; ánh xạ các ngày làm và khóa học vào bảng phụ. |
| `application/ConsultantApplicationRepository.java` | Tìm đơn gần nhất, đơn pending, danh sách admin; khóa dòng khi xử lý đơn. |
| `application/ApplicationView.java` | DTO chỉ chứa dữ liệu hiển thị, gồm tên khóa học và tên ngày trong tuần; không đưa mật khẩu ra giao diện. |
| `application/ConsultantApplicationService.java` | `submit`: kiểm tra CUSTOMER, không trùng đơn chờ, ngày giờ và link hồ sơ; `review`: kiểm tra admin, quyết định, cấp quyền nguyên tử; `latest/all`: dữ liệu giao diện. |
| `consultant/ConsultantWorkspaceService.java` | Lấy hồ sơ từ email đăng nhập; mở/ngừng giờ của chính mình, xem booking của mình và ghi kết quả sau buổi tư vấn. Không tin consultantId từ form. |
| `web/ConsultantApplicationPageController.java` | GET/POST `/consultant-application`; GET `/admin/applications`; POST `/admin/applications/{id}/review`; lỗi giữ form và hiện thông báo. |
| `web/ConsultantWorkspacePageController.java` | GET/POST `/consultant/slots`, POST ngừng giờ; GET `/consultant/bookings`, POST cập nhật kết quả. |

## Java chỉnh sửa

| File | Phần sửa |
|---|---|
| `user/UserEntity.java` | Thêm `approveAsConsultant()`; không cho form đăng ký tự chọn quyền. |
| `consultant/ConsultantEntity.java` | Thêm userId, availableFrom/Until, workDays và courseIds; `linkApprovedAccount`, `acceptsStartAt`, `canConsultCourse`. userId null giữ hồ sơ demo cũ. |
| `consultant/ConsultantRepository.java` | Thêm tìm hồ sơ theo userId và truy vấn tư vấn viên nhận đúng khóa học. |
| `consultant/ConsultantService.java` | Thêm `getConsultantsForCourse(courseId)` để lọc chuyên môn đã duyệt. |
| `slot/ConsultationSlotRepository.java` | Thêm danh sách tất cả khung giờ tương lai của một tư vấn viên, gồm giờ đã đặt/ngừng nhận. |
| `slot/ConsultationSlotService.java` | `createSlot` có transaction; kiểm tra đủ 30 phút trong lịch làm việc đã duyệt, áp dụng cả khi admin tạo hộ. |
| `booking/BookingRepository.java` | Thêm truy vấn booking theo tư vấn viên sở hữu khung giờ. |
| `booking/BookingService.java` | `createBooking` kiểm tra tư vấn viên đang hoạt động và được tư vấn khóa học tương ứng. |
| `web/PageController.java` | Trang đặt lịch dùng danh sách tư vấn viên phù hợp khóa học. |
| `security/SecurityConfig.java` | `/consultant/**` yêu cầu CONSULTANT; JS là tài nguyên công khai; đăng nhập tư vấn viên về khu vực của mình; CSRF vẫn bật. Logout vẫn về trang khóa học. |

## Giao diện và database

| File | Phần mới/sửa |
|---|---|
| `src/main/resources/templates/consultant-application.html` (mới) | Form hồ sơ và trạng thái đơn; không cho gửi thêm khi đang chờ. |
| `src/main/resources/templates/admin-applications.html` (mới) | Danh sách hồ sơ, lịch đã đăng ký, quyết định và lý do. |
| `src/main/resources/templates/consultant-slots.html` (mới) | Lịch làm việc được duyệt; nút mở giờ; các giờ có trạng thái đã đặt/đang mở/đã ngừng. |
| `src/main/resources/templates/consultant-bookings.html` (mới) | Danh sách khách đặt với mình và form ghi kết quả. |
| `src/main/resources/templates/courses.html` | Link gửi hồ sơ, duyệt hồ sơ, khu vực tư vấn viên; hiển thị vai trò CONSULTANT. |
| `src/main/resources/templates/register.html` | Giải thích tạo tài khoản trước rồi đăng ký tư vấn viên. |
| `admin-bookings.html`, `admin-courses.html`, `admin-course-edit.html`, `admin-consultants.html`, `admin-consultant-edit.html`, `admin-slots.html` | Thêm mục Duyệt hồ sơ vào thanh điều hướng. |
| `src/main/resources/static/css/app.css` | Checkbox không bị kéo rộng như input; bố cục form hai cột và các thẻ duyệt hồ sơ. |
| `database/migrations/002_consultant_applications.sql` (mới) | Mở rộng role; thêm liên kết tài khoản, lịch làm việc, hồ sơ và bảng khóa học/ngày; unique một đơn PENDING cho mỗi người. |
| `database/schema.sql` | Thêm cấu trúc mới để clone dự án và tạo database mới vẫn chạy được. |
| `.gitignore` | Bỏ qua `.local-backups/`, tránh đưa backup tài khoản lên GitHub. |
| `README.md` | Thêm chức năng mới và link hướng dẫn. |

## Kiểm tra

Tạo `src/test/java/vn/coursebooking/application/ConsultantApplicationIntegrationTest.java`.
9 test PostgreSQL thực bằng Testcontainers kiểm tra:

- Pending không có quyền CONSULTANT; không được gửi trùng; CUSTOMER không vào workspace hoặc duyệt hồ sơ.
- Duyệt tạo đúng một hồ sơ, đổi quyền và các template mới render được.
- Từ chối bắt buộc lý do, không đổi quyền; được gửi lại.
- Chặn giờ không hợp lệ và link hồ sơ ngoài http/https.
- Form thiếu ngày/khóa học vẫn render lỗi và giữ input.
- Chặn ngày/giờ ngoài lịch, vượt giờ kết thúc và giờ bị trùng.
- Không ngừng giờ của người khác hoặc giờ đã có khách.
- Chỉ nhận khóa học đã được duyệt, kể cả gọi service đặt trực tiếp.
- Hai admin duyệt đồng thời chỉ một thành công.

`mvn -B -ntp verify`: 20 test, 0 failure, 0 error, BUILD SUCCESS.
Đã khởi động bản mới trên cổng riêng 18082 và xem trang duyệt admin. Test không tạo hồ sơ giả trong database local.

## Dữ liệu local và cách thử

Đã sao lưu database vào `.local-backups/before-consultant-applications-20261010-031825.dump` rồi áp dụng migration thành công.
Các khóa học, tài khoản và booking cũ vẫn được giữ. Backup này chứa dữ liệu tài khoản, không commit.
Không cần bạn chạy SQL lần nữa trên database local này.

1. Stop và Run lại ứng dụng trong IntelliJ; Ctrl+F5 trên trình duyệt.
2. Dùng tài khoản CUSTOMER, chọn Đăng ký tư vấn viên, gửi đầy đủ hồ sơ.
3. Dùng admin ở một trình duyệt/phiên khác, vào Duyệt hồ sơ và duyệt đơn.
4. Tài khoản được duyệt phải đăng xuất/đăng nhập lại; chọn một ngày tương lai đúng thứ và giờ đã khai báo để mở giờ.
5. Dùng CUSTOMER khác chọn khóa học tương ứng và đặt giờ; tư vấn viên xem Lịch tư vấn của tôi.

## Phạm vi bản đầu

Mỗi hồ sơ dùng một khoảng giờ chung cho các ngày đã chọn. Chưa sửa lịch làm việc sau duyệt, chưa sinh lịch lặp tự động, chưa xác minh chứng chỉ tự động.
Admin tự kiểm tra hồ sơ và cam kết thời gian. Phần mềm kiểm tra lịch hợp lệ, không bảo đảm người đó có mặt thực tế.
Không tự commit/push các thay đổi.

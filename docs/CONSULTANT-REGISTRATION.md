# Đăng ký và duyệt tư vấn viên

## Luồng sử dụng

1. Tạo tài khoản thông thường và đăng nhập.
2. Chọn **Đăng ký tư vấn viên** trên trang khóa học.
3. Nhập liên hệ, chuyên môn, kinh nghiệm, khóa học, ngày và giờ làm việc; gửi hồ sơ.
4. Admin vào **Duyệt hồ sơ**, kiểm tra và duyệt hoặc từ chối kèm lý do.
5. Người được duyệt **đăng xuất rồi đăng nhập lại** để cập nhật quyền trong session.
6. Tại **Khung giờ của tôi**, mở từng giờ 30 phút trong lịch được duyệt.
7. Khách chọn khóa học tương ứng và đặt giờ; tư vấn viên xem **Lịch tư vấn của tôi**.

## Database đang có dữ liệu

Stop ứng dụng trước khi cập nhật. Trong PowerShell tại thư mục dự án:

```powershell
Get-Content -Raw -Encoding UTF8 database/migrations/002_consultant_applications.sql | docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U course_user -d course_consultation
```

SQL bổ sung bảng/cột, không xóa dữ liệu hiện tại. Có thể chạy lại script.
Database mới được tạo từ `database/schema.sql` đã có cấu trúc mới; không cần migration riêng.

## Quy tắc

- Một tài khoản chỉ có một đơn chờ duyệt. Bị từ chối có thể gửi lại.
- Chỉ admin được duyệt; cấp quyền và tạo hồ sơ diễn ra trong cùng transaction.
- Các ngày dùng chung một khoảng giờ, theo giờ Việt Nam, không qua đêm.
- Chỉ nhận các khóa học đã đăng ký trong hồ sơ được duyệt.
- Khung giờ bắt đầu phút 00/30, đủ 30 phút trong lịch làm việc, không trùng, nằm trong tương lai.
- Tư vấn viên chỉ xem và thao tác lịch của chính mình; không gửi consultantId để chọn người khác.
- Không được ngừng khung giờ đã có booking chưa hủy.
- Duyệt hồ sơ không tự sinh khung giờ. Người được duyệt phải xác nhận từng giờ họ có thể nhận khách.
- Các hồ sơ demo admin tạo trước đây chưa gắn tài khoản vẫn do admin quản lý.
- Bản đầu chưa sửa lịch làm việc sau duyệt, chưa đính kèm tài liệu hay xác minh chứng chỉ tự động.

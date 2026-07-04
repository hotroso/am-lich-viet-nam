# Âm Lịch Việt Nam - hotroso (Hỗ trợ giải pháp số)

![App Mockup](screenshot.png)

Ứng dụng xem lịch Âm - Dương đơn giản, hiện đại và chính xác dành cho người Việt.

## 🌟 Tính năng nổi bật (Features)

- **Xem ngày Âm/Dương**: Hiển thị chính xác ngày lịch âm và lịch dương.
- **Thông tin Can Chi**: Tra cứu Can Chi của năm, tháng, ngày và giờ.
- **Giờ Hoàng Đạo**: Thông tin chi tiết về các khung giờ tốt trong ngày.
- **Thông tin Tiết Khí**: Theo dõi các giai đoạn tiết khí trong năm.
- **Quản lý sự kiện**: Lưu trữ và nhắc nhở các ngày kỷ niệm, ngày lễ quan trọng (Rằm, Mùng 1, Tết...).
- **Giao diện hiện đại**: Thiết kế tinh tế, dễ sử dụng với Bottom Sheet và hiệu ứng mượt mà.

## 🛠 Công nghệ sử dụng (Tech Stack)

- **Ngôn ngữ**: Kotlin
- **Kiến trúc**: MVVM (Model-View-ViewModel)
- **Database**: Room Database (để lưu trữ sự kiện)
- **Background Task**: WorkManager (cho thông báo nhắc nhở)
- **UI Components**: CoordinatorLayout, BottomSheetBehavior, RecyclerView, ViewBinding.

## 🚀 Cài đặt và Chạy thử (Setup & Run)

### Yêu cầu (Requirements)
- Android Studio Iguana hoặc mới hơn.
- JDK 17+.
- Android Device/Emulator chạy Android 5.0 (Lollipop) trở lên.

### Các bước thực hiện (Steps)
1. Clone project:
   ```bash
   git clone https://github.com/hotroso/am-lich-viet-nam.git
   ```
2. Mở project bằng Android Studio.
3. Chờ Gradle sync hoàn tất.
4. Nhấn **Run** để cài đặt lên thiết bị.

## 📂 Cấu trúc thư mục (Project Structure)

- `app/src/main/java/.../lunar`: Chứa logic tính toán lịch âm dương.
- `app/src/main/java/.../ui`: Các View tùy chỉnh và Adapter.
- `app/src/main/java/.../reminder`: Quản lý Room Database và WorkManager cho lời nhắc.
- `app/src/main/res/layout`: Các file XML định nghĩa giao diện.

## 📄 Bản quyền (License)

Dự án này được phát triển và duy trì bởi **[hotroso (Hỗ trợ giải pháp số)](https://github.com/hotroso)**. Được bảo lưu mọi quyền.

---

*Made with ❤️ by hotroso.*

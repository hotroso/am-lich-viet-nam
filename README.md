<p align="center">
  <img src="https://github.com/hotroso/am-lich-viet-nam/raw/main/screenshot/demo.gif" alt="Âm Lịch Việt Nam - Demo" width="280">
</p>

<h1 align="center">🌙 Âm Lịch Việt Nam</h1>

<p align="center">
  <b>Xem ngày Âm - Dương chính xác, tra Can Chi, Giờ Hoàng Đạo và quản lý sự kiện âm lịch — tất cả trong một app, không cần internet.</b>
</p>

<p align="center">
  <a href="https://github.com/hotroso/am-lich-viet-nam/releases/latest"><img src="https://img.shields.io/github/v/release/hotroso/am-lich-viet-nam?style=flat-square" alt="Latest Release"></a>
  <a href="https://github.com/hotroso/am-lich-viet-nam/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-Source%20Available%20(Non--Commercial)-blue?style=flat-square" alt="License"></a>
  <a href="https://github.com/hotroso/am-lich-viet-nam/actions"><img src="https://img.shields.io/github/actions/workflow/status/hotroso/am-lich-viet-nam/build-apk.yml?style=flat-square&label=build" alt="Build Status"></a>
</p>

<p align="center">
  <a href="https://github.com/hotroso/am-lich-viet-nam/releases/latest"><b>⬇️ Tải APK mới nhất</b></a>
</p>

---

## Vì sao chọn app này?

Có rất nhiều app lịch âm trên chợ ứng dụng, nhưng phần lớn đầy quảng cáo, chậm, hoặc tính sai tháng nhuận. **Âm Lịch Việt Nam** được viết lại từ đầu bằng thuật toán thiên văn chuẩn, giao diện gọn nhẹ, và hoạt động **hoàn toàn offline** — mở app lên là có kết quả ngay, không chờ load, không banner quảng cáo.

## ✨ Tính năng nổi bật

| | |
|---|---|
| 📅 **Âm/Dương chính xác** | Chuyển đổi hai chiều Dương lịch ⇄ Âm lịch, tự nhận diện tháng nhuận theo từng năm |
| 🐉 **Can Chi đầy đủ** | Tra cứu Can Chi của năm, tháng, ngày, giờ |
| ⏰ **Giờ Hoàng Đạo** | Biết ngay khung giờ tốt trong ngày để chọn việc quan trọng |
| 🌟 **Ngày tốt/xấu** | Đánh giá dựa trên Trực + Hoàng Đạo, gợi ý rõ ràng "Nên làm" / "Không nên làm" |
| 🌿 **24 Tiết Khí** | Theo dõi trọn năm, không bỏ lỡ thời điểm quan trọng trong nông lịch |
| 📖 **Nhị Thập Bát Tú** | Chi tiết 28 ngôi sao chiếu mệnh theo từng ngày |
| 🧭 **Hướng xuất hành** | Tài thần, Hỷ thần, Hạc thần cho mỗi ngày |
| 📌 **Sự kiện & nhắc nhở** | Lưu ngày giỗ, sinh nhật âm lịch — app tự nhắc đúng ngày mỗi năm |
| 🎂 **Kỷ niệm thông minh** | Tự tính và hiển thị "tròn xx tuổi", "kỷ niệm xx năm" |
| 📱 **3 loại Widget** | Xem lịch ngay trên màn hình chính, không cần mở app |
| 📤 **Chia sẻ nhanh** | Gửi thông tin ngày qua tin nhắn, mạng xã hội chỉ với 1 chạm |

## 📲 Tải về

| Kênh | Link |
|---|---|
| GitHub Release | [Download APK](https://github.com/hotroso/am-lich-viet-nam/releases/latest) |

> Quét mã QR trên trang Release để tải trực tiếp về điện thoại.

## ✅ Yêu cầu hệ thống

- Android 5.0 (Lollipop) trở lên
- Không cần kết nối internet

## 🛠️ Công nghệ sử dụng

| | |
|---|---|
| Ngôn ngữ | Kotlin |
| Kiến trúc | MVVM |
| Database | Room |
| Tác vụ nền | WorkManager + AlarmManager |
| UI | CoordinatorLayout, BottomSheet, RecyclerView |
| Build | Gradle + KSP |

## 📁 Cấu trúc dự án

```
app/src/main/java/io/github/hotroso/vietnameselunarcalendar/
├── lunar/          # Logic tính toán lịch âm dương, Can Chi, Tiết Khí
├── ui/             # Custom views (CalendarView2, CellView, DigitalClock)
├── widget/         # App widgets (3 loại) + config activity
├── reminder/       # Quản lý sự kiện âm lịch (Room DB + notifications)
├── MainActivity.kt
├── MainFragment.kt
└── ...
```

## 🚀 Build từ mã nguồn

```bash
# Debug
./gradlew assembleDebug

# Release (cần keystore)
./gradlew assembleRelease
```

## 🏷️ Phát hành phiên bản mới

```bash
git tag v2.3
git push origin v2.3
```

GitHub Actions sẽ tự động build APK đã ký, tạo release, và gắn kèm mã QR.

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=hotroso/am-lich-viet-nam&type=Date)](https://star-history.com/#hotroso/am-lich-viet-nam&Date)

## 🤝 Đóng góp

Mọi đóng góp đều được hoan nghênh! Nếu bạn thấy app hữu ích, hãy để lại ⭐ cho repo. Với các thay đổi lớn, vui lòng mở Issue trước khi tạo Pull Request.

## 📄 Giấy phép

**Source Available — Chỉ dùng cho mục đích phi thương mại.**

Bạn được tự do sử dụng, sửa đổi và phân phối cho mục đích phi thương mại. Xem chi tiết tại [LICENSE](https://github.com/hotroso/am-lich-viet-nam/blob/main/LICENSE).

Liên hệ cấp phép thương mại: [github.com/hotroso](https://github.com/hotroso)

---

<p align="center">Made with ❤️ by <a href="https://github.com/hotroso">Hỗ trợ giải pháp số</a></p>

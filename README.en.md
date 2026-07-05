<p align="center">
  <img src="https://github.com/hotroso/am-lich-viet-nam/raw/main/screenshot/demo.gif" alt="Vietnamese Lunar Calendar - Demo" width="280">
</p>

<h1 align="center">🌙 Vietnamese Lunar Calendar</h1>

<p align="center">
  <b>Accurate Lunar–Solar calendar, Zodiac (Can Chi) lookup, Auspicious Hours, and lunar event reminders — all in one app, no internet required.</b>
</p>

<p align="center">
  <a href="https://github.com/hotroso/am-lich-viet-nam/releases/latest"><img src="https://img.shields.io/github/v/release/hotroso/am-lich-viet-nam?style=flat-square" alt="Latest Release"></a>
  <a href="https://github.com/hotroso/am-lich-viet-nam/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-Source%20Available%20(Non--Commercial)-blue?style=flat-square" alt="License"></a>
  <a href="https://github.com/hotroso/am-lich-viet-nam/actions"><img src="https://img.shields.io/github/actions/workflow/status/hotroso/am-lich-viet-nam/build-apk.yml?style=flat-square&label=build" alt="Build Status"></a>
</p>

<p align="center">
  <a href="https://github.com/hotroso/am-lich-viet-nam/releases/latest"><b>⬇️ Download the latest APK</b></a>
</p>

---

## Why this app?

There are plenty of lunar calendar apps out there, but most are cluttered with ads, slow, or get leap months wrong. **Vietnamese Lunar Calendar** was built from scratch with accurate astronomical algorithms, a clean lightweight UI, and works **fully offline** — open it and get instant results, no loading screens, no ad banners.

## ✨ Key Features

| | |
|---|---|
| 📅 **Accurate Lunar/Solar dates** | Two-way conversion between Solar and Lunar calendars, with automatic leap-month detection for any given year |
| 🐉 **Full Can Chi (Zodiac) info** | Look up the Heavenly Stem / Earthly Branch for the year, month, day, and hour |
| ⏰ **Auspicious Hours** | Instantly see the best time slots in the day for important activities |
| 🌟 **Good/Bad day rating** | Evaluated from the day's "Trực" and zodiac hour, with clear "Do" / "Avoid" suggestions |
| 🌿 **24 Solar Terms (Tiết Khí)** | Track the full-year solar term cycle, useful for agricultural and traditional timing |
| 📖 **28 Lunar Mansions** | Detailed info on the 28 stars governing each day |
| 🧭 **Lucky directions** | Wealth God, Happiness God, and Crane God directions for each day |
| 📌 **Lunar events & reminders** | Save death anniversaries and lunar birthdays — the app reminds you automatically every year |
| 🎂 **Smart anniversary tracking** | Automatically calculates and displays milestone ages and anniversaries |
| 📱 **3 home screen widgets** | Check the calendar right from your home screen, no need to open the app |
| 📤 **Quick sharing** | Share the day's info via messaging apps or social media in one tap |

## 📲 Download

| Channel | Link |
|---|---|
| GitHub Release | [Download APK](https://github.com/hotroso/am-lich-viet-nam/releases/latest) |

> Scan the QR code on the Release page to download directly to your phone.

## ✅ Requirements

- Android 5.0 (Lollipop) or higher
- No internet connection required

## 🛠️ Tech Stack

| | |
|---|---|
| Language | Kotlin |
| Architecture | MVVM |
| Database | Room |
| Background tasks | WorkManager + AlarmManager |
| UI | CoordinatorLayout, BottomSheet, RecyclerView |
| Build | Gradle + KSP |

## 📁 Project Structure

```
app/src/main/java/io/github/hotroso/vietnameselunarcalendar/
├── lunar/          # Lunar/Solar calendar, Can Chi, and Solar Term calculation logic
├── ui/             # Custom views (CalendarView2, CellView, DigitalClock)
├── widget/         # App widgets (3 types) + config activity
├── reminder/       # Lunar event management (Room DB + notifications)
├── MainActivity.kt
├── MainFragment.kt
└── ...
```

## 🚀 Build from source

```bash
# Debug
./gradlew assembleDebug

# Release (requires a keystore)
./gradlew assembleRelease
```

## 🏷️ Releasing a new version

```bash
git tag v2.3
git push origin v2.3
```

GitHub Actions will automatically build a signed APK, create a release, and attach a QR code.

## ⭐ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=hotroso/am-lich-viet-nam&type=Date)](https://star-history.com/#hotroso/am-lich-viet-nam&Date)

## 🤝 Contributing

Contributions are welcome! If you find this app useful, please consider giving the repo a ⭐. For major changes, please open an issue first to discuss what you'd like to change before submitting a Pull Request.

## 📄 License

**Source Available — Non-Commercial Use Only.**

You are free to use, modify, and distribute this project for non-commercial purposes. See [LICENSE](https://github.com/hotroso/am-lich-viet-nam/blob/main/LICENSE) for details.

For commercial licensing inquiries, contact: [github.com/hotroso](https://github.com/hotroso)

---

<p align="center">Made with ❤️ by <a href="https://github.com/hotroso">Hỗ trợ giải pháp số</a></p>

# Hướng Dẫn Build Dự Án Android (Build Guide)

Dự án **Trợ Lý Liên Quân AI (Arena of Valor Tactical AI Coach)** là ứng dụng Android nguyên bản sử dụng Kotlin DSL, Jetpack Compose, Material Design 3, Room Database và KSP. Tài liệu này hướng dẫn chi tiết cách build ứng dụng trên môi trường cục bộ (Local / Android Studio) và môi trường tự động hóa (CI/CD GitHub Actions).

---

## 1. Yêu Cầu Môi Trường (System Requirements)

- **Hệ Điều Hành**: Windows 10/11, macOS (Intel/Apple Silicon), hoặc Ubuntu Linux (20.04/22.04/24.04).
- **Java Development Kit (JDK)**: **JDK 17** (Khuyến nghị Eclipse Temurin hoặc OpenJDK 17).
- **Android SDK Requirements**:
  - `compileSdk`: **36** (Android 15+)
  - `targetSdk`: **36**
  - `minSdk`: **26** (Android 8.0 Oreo trở lên)
  - `buildToolsVersion`: Tương thích AGP 9.1.x / Gradle 9.3.1
- **Gradle Version**: **9.3.1** (được tích hợp sẵn qua Gradle Wrapper).
- **Android Gradle Plugin (AGP)**: **9.1.1**
- **Kotlin Version**: **2.2.10** (KSP: **2.2.10-2.0.2**)

---

## 2. Hướng Dẫn Build Cục Bộ (Local Build)

### Bước 1: Clone Repository
```bash
git clone <URL_REPOSITORY>
cd <THU_MUC_DU_AN>
```

### Bước 2: Cấp Quyền Thực Thi Cho Gradle Wrapper (Linux / macOS)
```bash
chmod +x gradlew
```

### Bước 3: Build Debug APK
```bash
./gradlew assembleDebug
```
File APK kết quả sẽ được tạo tại:
`app/build/outputs/apk/debug/app-debug.apk`

### Bước 4: Build Release APK
```bash
./gradlew assembleRelease
```
File APK kết quả sẽ được tạo tại:
`app/build/outputs/apk/release/app-release.apk`

### Bước 5: Chạy Trên Android Studio
1. Mở Android Studio (Phiên bản Ladybug / Otter hoặc mới hơn hỗ trợ Gradle 9 & AGP 9.1).
2. Chọn **Open** và trỏ đến thư mục gốc của dự án.
3. Chờ Android Studio đồng bộ xong (Sync Project with Gradle Files).
4. Chọn cấu hình chạy `app` và nhấn **Run** (`Shift + F10`) để cài đặt lên thiết bị thật hoặc máy ảo.

---

## 3. Cấu Hình CI/CD Trên GitHub Actions

Quy trình tự động hóa CI/CD đã được cấu hình sẵn trong `.github/workflows/android-build.yml`.

### Các tính năng tự động:
1. **Checkout Source Code**: Tải toàn bộ mã nguồn.
2. **Setup JDK 17**: Cài đặt môi trường Temurin JDK 17.
3. **Gradle Dependency Cache**: Lưu cache tự động giúp tăng tốc độ build các lần sau.
4. **Auto-Generate Keystore**: Tự động tạo debug keystore nếu chạy trên CI chưa cấu hình Secrets.
5. **Build All Variants**: Tự động build cả `./gradlew assembleDebug` và `./gradlew assembleRelease`.
6. **Artifacts Upload**: Tự động lưu trữ file APK thành Artifact có thể tải về trực tiếp từ tab Actions trên GitHub (lưu trữ 14 ngày).

---

## 4. Quyền Hệ Thống & Cấu Hình Cần Thiết (Permissions & Secrets)

### Quyền Android (AndroidManifest.xml)
- `android.permission.INTERNET`: Gọi API và cập nhật dữ liệu.
- `android.permission.SYSTEM_ALERT_WINDOW`: Hiển thị cửa sổ nổi HUD trong lúc chơi game Liên Quân Mobile.
- `android.permission.FOREGROUND_SERVICE`: Duy trì dịch vụ trợ lý chiến thuật AI chạy nền mượt mà.
- `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`: Tuân thủ chính sách Foreground Service trên Android 14+.
- `android.permission.POST_NOTIFICATIONS`: Thông báo trạng thái trận đấu.
- `android.permission.VIBRATE`: Rung phản hồi khi có cảnh báo nguy cấp.

### Cấu Hình Firebase / Google Services
- Dự án đã tích hợp cờ:
  ```kotlin
  googleServices {
      missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
  }
  ```
  **Ý nghĩa**: Khi dự án chưa có file `google-services.json`, quá trình build Gradle vẫn diễn ra **HOÀN TOÀN THÀNH CÔNG** và không bị gián đoạn (chỉ hiển thị cảnh báo Warning). Để kích hoạt đầy đủ Firebase Cloud Sync, người dùng chỉ cần tải file `google-services.json` từ Firebase Console và đặt vào thư mục `app/`.

### Quản Lý Release Signing Key (Secrets Panel / CI Secrets)
Khi phát hành ứng dụng lên Google Play Store, bạn có thể truyền các biến môi trường sau vào GitHub Secrets hoặc môi trường build:
- `KEYSTORE_PATH`: Đường dẫn đến file `.jks` hoặc `.keystore`.
- `STORE_PASSWORD`: Mật khẩu kho khóa.
- `KEY_ALIAS`: Tên alias của khóa.
- `KEY_PASSWORD`: Mật khẩu của khóa.

*(Nếu không thiết lập các biến trên, hệ thống build sẽ tự động dự phòng sang debug signing config để đảm bảo lệnh `assembleRelease` luôn build thành công).*

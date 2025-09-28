# 📱 PandoraOS v0.1.0-chimera - Hướng dẫn sử dụng

## 🎯 Tổng quan

PandoraOS là một hệ thống bàn phím thần kinh thông minh với khả năng tự động hóa dựa trên AI. Ứng dụng sử dụng trí tuệ nhân tạo để hiểu ngữ cảnh, học hỏi từ người dùng và tự động thực hiện các hành động thông minh.

## 🚀 Cài đặt và thiết lập

### Yêu cầu hệ thống
- **Android:** 7.0 (API 24) trở lên
- **RAM:** Tối thiểu 2GB, khuyến nghị 4GB
- **Storage:** 100MB trống
- **Bluetooth:** Hỗ trợ BLE (tùy chọn)
- **NFC:** Hỗ trợ NFC (tùy chọn)

### Cài đặt ứng dụng

1. **Tải xuống APK**
   ```bash
   # Clone repository
   git clone https://github.com/Eilodon/Pandora-Beta1.git
   cd Pandora-Beta1
   
   # Build APK
   ./gradlew assembleDebug
   ```

2. **Cài đặt APK**
   - Bật "Unknown sources" trong Settings
   - Cài đặt file APK từ thư mục `app/build/outputs/apk/debug/`

3. **Cấp quyền cần thiết**
   - **System Alert Window:** Cho phép hiển thị overlay
   - **Bluetooth:** Cho kết nối thiết bị thông minh
   - **Location:** Cho tính năng dựa trên vị trí
   - **NFC:** Cho giao tiếp NFC (nếu có)

## ⌨️ Sử dụng Neural Keyboard

### Kích hoạt bàn phím

1. Mở **Settings** → **System** → **Languages & input**
2. Chọn **Virtual keyboard** → **Manage keyboards**
3. Bật **PandoraOS Keyboard**
4. Chọn **PandoraOS Keyboard** làm bàn phím mặc định

### Sử dụng Smart Context Bar

Khi gõ văn bản, PandoraOS sẽ tự động:
- **Phân tích ngữ cảnh** của văn bản
- **Hiển thị gợi ý thông minh** dựa trên nội dung
- **Đề xuất hành động** phù hợp

**Ví dụ:**
- Gõ "họp lúc 3pm" → Gợi ý mở Calendar
- Gõ "đi siêu thị" → Gợi ý mở Maps
- Gõ "chụp ảnh" → Gợi ý mở Camera

### Mini-Flows (Tự động hóa thông minh)

PandoraOS có 5 Mini-Flows chính:

#### 1. 📅 Calendar Flow
- **Trigger:** "họp", "meeting", "lịch", "calendar"
- **Hành động:** Tự động mở Google Calendar
- **Ví dụ:** "Họp team lúc 2pm ngày mai"

#### 2. 🎵 Spotify Flow
- **Trigger:** Kết nối tai nghe Bluetooth
- **Hành động:** Tự động mở Spotify
- **Cấu hình:** Có thể tùy chỉnh trong Settings

#### 3. 🗺️ Maps Flow
- **Trigger:** "đi", "đường", "maps", "navigate"
- **Hành động:** Tự động mở Google Maps
- **Ví dụ:** "Đi siêu thị gần nhất"

#### 4. 📷 Camera Flow
- **Trigger:** "chụp", "ảnh", "camera", "photo"
- **Hành động:** Tự động mở Camera
- **Ví dụ:** "Chụp ảnh tài liệu"

#### 5. 📝 Keep Flow
- **Trigger:** "note", "ghi chú", "keep", "memo"
- **Hành động:** Tự động mở Google Keep
- **Ví dụ:** "Ghi chú về dự án mới"

## 🔧 Cấu hình nâng cao

### Cài đặt AI

1. **Mở Settings** → **PandoraOS** → **AI Settings**
2. **Chế độ bảo mật:**
   - **On-Device:** Xử lý hoàn toàn trên thiết bị
   - **Hybrid:** Kết hợp local và cloud
   - **Cloud:** Xử lý trên cloud (nhanh hơn)

3. **Học hỏi cá nhân:**
   - Bật/tắt tính năng học hỏi
   - Xem lịch sử học hỏi
   - Reset dữ liệu học hỏi

### Cài đặt Performance

1. **Mở Settings** → **PandoraOS** → **Performance**
2. **Tối ưu bộ nhớ:**
   - Bật/tắt image optimization
   - Cấu hình cache size
   - Xem memory usage

3. **Tối ưu CPU:**
   - Cấu hình thread pool
   - Bật/tắt parallel processing
   - Xem CPU usage

### Cài đặt Integrations

1. **Bluetooth:**
   - Bật/tắt BLE scanning
   - Quản lý thiết bị đã kết nối
   - Cấu hình auto-launch apps

2. **NFC:**
   - Bật/tắt NFC support
   - Cấu hình NFC tags
   - Xem lịch sử NFC

3. **Google Services:**
   - Cấu hình Calendar integration
   - Cấu hình Maps integration
   - Cấu hình Keep integration

## 📊 Monitoring và Analytics

### Performance Dashboard

1. **Mở Settings** → **PandoraOS** → **Performance Dashboard**
2. **Xem metrics:**
   - Memory usage
   - CPU usage
   - Response times
   - Error rates

3. **Performance recommendations:**
   - Tối ưu hóa được đề xuất
   - Cảnh báo hiệu suất
   - Lịch sử performance

### AI Insights

1. **Mở Settings** → **PandoraOS** → **AI Insights**
2. **Xem thống kê:**
   - Số lần tương tác
   - Tỷ lệ thành công
   - Hành động phổ biến
   - Tiến độ học hỏi

## 🔍 Troubleshooting

### Vấn đề thường gặp

#### 1. Bàn phím không hiển thị
**Nguyên nhân:** Chưa kích hoạt bàn phím
**Giải pháp:**
1. Settings → System → Languages & input
2. Virtual keyboard → Manage keyboards
3. Bật PandoraOS Keyboard

#### 2. Mini-Flows không hoạt động
**Nguyên nhân:** Thiếu quyền hoặc app không có sẵn
**Giải pháp:**
1. Kiểm tra quyền trong Settings
2. Cài đặt các app cần thiết (Calendar, Maps, etc.)
3. Restart ứng dụng

#### 3. Hiệu suất chậm
**Nguyên nhân:** Thiếu tài nguyên hoặc cấu hình không tối ưu
**Giải pháp:**
1. Mở Performance Dashboard
2. Xem memory và CPU usage
3. Áp dụng recommendations
4. Restart ứng dụng

#### 4. BLE không kết nối
**Nguyên nhân:** Bluetooth tắt hoặc thiếu quyền
**Giải pháp:**
1. Bật Bluetooth
2. Cấp quyền Location
3. Restart BLE scanning

### Log và Debug

1. **Mở Settings** → **PandoraOS** → **Debug**
2. **Bật debug mode:**
   - Hiển thị log chi tiết
   - Lưu crash reports
   - Gửi feedback

3. **Xem logs:**
   - App logs
   - Performance logs
   - Error logs

## 📞 Hỗ trợ

### Liên hệ
- **GitHub Issues:** [Pandora-Beta1 Issues](https://github.com/Eilodon/Pandora-Beta1/issues)
- **Email:** support@pandoraos.dev
- **Discord:** [PandoraOS Community](https://discord.gg/pandoraos)

### Tài liệu
- **API Documentation:** [API Docs](docs/api/)
- **Developer Guide:** [Developer Guide](docs/developer/)
- **Performance Guide:** [Performance Guide](docs/performance/)

### Cập nhật
- **Changelog:** [CHANGELOG.md](CHANGELOG.md)
- **Releases:** [GitHub Releases](https://github.com/Eilodon/Pandora-Beta1/releases)
- **Roadmap:** [Roadmap](ROADMAP.md)

## 🎉 Kết luận

PandoraOS v0.1.0-chimera mang đến trải nghiệm bàn phím thông minh với AI mạnh mẽ. Với các tính năng tự động hóa, tối ưu hiệu suất và tích hợp sâu, PandoraOS sẽ giúp bạn làm việc hiệu quả hơn.

**Chúc bạn sử dụng PandoraOS vui vẻ! 🚀✨**

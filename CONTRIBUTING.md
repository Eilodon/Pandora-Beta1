# Contributing Guide

Cảm ơn bạn đã quan tâm đóng góp cho PandoraOS. Vui lòng làm theo hướng dẫn sau để đảm bảo chất lượng và tính nhất quán.

## Quy trình
1. Tạo nhánh tính năng: `git checkout -b feature/<ten-tinh-nang>`
2. Phát triển và viết test (ưu tiên JUnit5 + MockK/Flow test)
3. Chạy lệnh kiểm tra:
   - `./gradlew build`
   - `./gradlew :core-ai:testDebugUnitTest`
4. Đảm bảo release build pass:
   - `./gradlew :app:assembleRelease`
5. Tạo Pull Request kèm mô tả, ảnh/chụp log nếu cần

## Chuẩn code
- Kotlin (K1.9+), Jetpack Compose theo Material3
- KDoc cho public API (class, function, data class)
- Tôn trọng Clean Architecture, DI bằng Hilt
- Không để TODO treo dài hạn; mở issue nếu cần

## Test & Coverage
- Unit test cho logic cốt lõi
- Performance test tối thiểu cho đường tải model (network vs cache)
- Không merge nếu build/test fail

## Bảo mật & Build
- Bản release bật R8 minify + shrinkResources
- Không commit secret/token. Dùng biến môi trường hoặc local.properties

## Commit message
- Định dạng gợi ý: `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`, `test: ...`, `build: ...`

## Quy ước tài liệu
- API docs: đặt trong mô-đun liên quan (vd: `core-ai/API_OVERVIEW.md`)
- Changelog: cập nhật `CHANGELOG.md` theo mỗi thay đổi quan trọng

Cảm ơn đóng góp của bạn! 🙌

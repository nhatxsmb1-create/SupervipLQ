package com.example.model

enum class ScreenState(
    val titleVi: String,
    val descriptionVi: String
) {
    OUTSIDE_GAME("TRỢ LÝ SẴN SÀNG", "Chưa phát hiện trận đấu. Vào Liên Quân để bắt đầu phân tích"),
    GAME_MENU("ĐANG Ở MENU GAME", "Đang ở sảnh/menu Liên Quân"),
    LOADING("ĐANG TẢI TRẬN ĐẤU", "Đang chờ tải trận đấu vào game..."),
    IN_MATCH("ĐANG PHÂN TÍCH TRỰC TIẾP", "Đang phân tích chiến thuật thực chiến thời gian thực"),
    SCOREBOARD_OPEN("BẢNG ĐIỂM ĐANG MỞ", "Đang phân tích chỉ số vàng, KDA và trang bị toàn đội"),
    SHOP_OPEN("CỬA HÀNG ĐANG MỞ", "Đang tính toán các trang bị khắc chế tối ưu"),
    COMBAT("GIAO TRANH DỒN DẬP", "Chú ý giữ vị trí và sử dụng chiêu thức hợp lý"),
    MATCH_END("TRẬN ĐẤU KẾT THÚC", "Trận đấu đã hoàn thành"),
    UNKNOWN("ĐANG NHẬN DIỆN TRẬN", "Đang quét màn hình Liên Quân...")
}

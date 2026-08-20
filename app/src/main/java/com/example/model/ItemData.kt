package com.example.model

data class GameItem(
    val name: String,
    val category: String, // "Công", "Phép", "Thủ", "Trợ Thủ"
    val cost: Int,
    val stats: String,
    val passiveDesc: String,
    val counterPurpose: String
)

object ItemDatabase {
    val items: List<GameItem> = listOf(
        GameItem(
            name = "Đao Truy Hồn",
            category = "Công",
            cost = 2000,
            stats = "+100 Công vật lý, +10% Giảm hồi chiêu",
            passiveDesc = "Truy Hồn: Sát thương vật lý làm giảm 50% khả năng hồi máu của mục tiêu trong 1.5s.",
            counterPurpose = "Khắc chế cứng các tướng hồi phục mạnh như Biron, Helen, Florentino, Veres, Taara."
        ),
        GameItem(
            name = "Sách Truy Hồn",
            category = "Phép",
            cost = 2000,
            stats = "+200 Công phép, +10% Giảm hồi chiêu, +20 Hồi năng lượng/5s",
            passiveDesc = "Truy Hồn: Sát thương phép làm giảm 50% khả năng hồi máu của mục tiêu trong 1.5s.",
            counterPurpose = "Trang bị bắt buộc cho Pháp Sư (Mid) khi đội hình địch có Helen, Biron, Sephera hoặc tướng hút máu cao."
        ),
        GameItem(
            name = "Quả Cầu Băng Sương",
            category = "Phép",
            cost = 2000,
            stats = "+240 Công phép",
            passiveDesc = "Chủ Động - Phong Ấn: Trở nên bất tử và không thể bị chọn làm mục tiêu trong 1.5s, nhưng không thể di chuyển.",
            counterPurpose = "Cực kỳ quan trọng cho Pháp Sư / Sát Thủ né sốc sát thương hoặc né chiêu cuối dồn dame."
        ),
        GameItem(
            name = "Cung Tà Ma",
            category = "Công",
            cost = 2250,
            stats = "+100 Công vật lý, +15% Tỉ lệ chí mạng, +10% Hút máu",
            passiveDesc = "Chủ Động - Tà Ma: Tăng thêm 90% Hút máu trong 3s (dành riêng cho tướng đánh xa/Xạ Thủ).",
            counterPurpose = "Trang bị lật kèo sinh tử giúp Xạ Thủ (Capheny, Violet, Laville, Stuart) hồi đầy máu tức thì trong combat."
        ),
        GameItem(
            name = "Phụ Kiện Ma Nhãn",
            category = "Trợ Thủ",
            cost = 1400,
            stats = "+40 Giáp / Giáp Phép, +10% Giảm hồi chiêu, +500 Máu",
            passiveDesc = "Chủ Động - Ma Nhãn: Soi diện rộng xung quanh và phát hiện tàng hình / ẩn nấp bụi cỏ trong 5s.",
            counterPurpose = "Trợ Thủ bắt buộc phải lên khi đối phương có Kaine, Aoi, Elsu hoặc Ngộ Không tàng hình."
        ),
        GameItem(
            name = "Liềm Đoạt Mệnh",
            category = "Công",
            cost = 2000,
            stats = "+60 Công vật lý, +5% Giảm hồi chiêu",
            passiveDesc = "Bất Tử: Khi chịu sát thương chí tử sẽ miễn tử vong và tăng 20% tốc chạy trong 1s (cooldown 90s).",
            counterPurpose = "Trang bị sinh tồn số 1 cho Sát Thủ / Xạ Thủ tránh bị hạ gục bất ngờ."
        ),
        GameItem(
            name = "Huân Chương Troy",
            category = "Thủ",
            cost = 2220,
            stats = "+360 Giáp phép, +1000 Máu tối đa, +10% Giảm hồi chiêu",
            passiveDesc = "Hộ Thân: Nhận một lớp lá chắn hấp thụ từ 300-1050 sát thương phép mỗi 18s.",
            counterPurpose = "Kháng sốc sát thương phép từ Pháp Sư sốc điện / cấu rỉa như Yue, Liliana, Krixi, Tulen."
        ),
        GameItem(
            name = "Khiên Huyền Thoại",
            category = "Thủ",
            cost = 2180,
            stats = "+360 Giáp vật lý, +400 Năng lượng, +20% Giảm hồi chiêu",
            passiveDesc = "Lính Gác: Nếu chịu sát thương, giảm 30% tốc đánh và 15% tốc chạy của kẻ địch tấn công.",
            counterPurpose = "Giảm tốc độ xả đạn của Xạ Thủ (AD) bắn nhanh như Hayate, Capheny, Laville, Tel'Annas."
        ),
        GameItem(
            name = "Giáp Hộ Mệnh",
            category = "Thủ",
            cost = 2080,
            stats = "+120 Giáp vật lý",
            passiveDesc = "Phục Sinh: Hồi sinh tại chỗ sau 2s với 2000 máu (tối đa 2 lần mỗi trận).",
            counterPurpose = "Trang bị tình huống then chốt cho giao tranh tổng quyết định hoặc thủ nhà chính cuối trận."
        )
    )
}


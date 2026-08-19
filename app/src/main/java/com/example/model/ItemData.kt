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
            counterPurpose = "Khắc chế cứng các tướng hồi phục mạnh như Florentino, Veres, Lữ Bố, Taara."
        ),
        GameItem(
            name = "Sách Truy Hồn",
            category = "Phép",
            cost = 2000,
            stats = "+200 Công phép, +10% Giảm hồi chiêu, +20 Hồi năng lượng/5s",
            passiveDesc = "Truy Hồn: Sát thương phép làm giảm 50% khả năng hồi máu của mục tiêu trong 1.5s.",
            counterPurpose = "Trang bị bắt buộc cho Pháp Sư (Mid) khi đội hình địch có tướng hút máu cao hoặc Helen, Sephera."
        ),
        GameItem(
            name = "Huân Chương Troy",
            category = "Thủ",
            cost = 2220,
            stats = "+360 Giáp phép, +1000 Máu tối đa, +10% Giảm hồi chiêu",
            passiveDesc = "Hộ Thân: Nhận một lớp lá chắn hấp thụ từ 300-1050 sát thương phép mỗi 18s.",
            counterPurpose = "Kháng sốc sát thương phép từ Pháp Sư sốc điện như Tulen, Liliana, Veera, Natalya."
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
        ),
        GameItem(
            name = "Kiếm Muramasa",
            category = "Công",
            cost = 2020,
            stats = "+75 Công vật lý, +10% Giảm hồi chiêu",
            passiveDesc = "Phá Giáp: Nhận thêm +40% Xuyên giáp vật lý.",
            counterPurpose = "Xuyên thủng dàn Đỡ Đòn và Đấu Sĩ nhiều giáp (Thane, Mina, Toro, Baldum)."
        ),
        GameItem(
            name = "Nham Thuẫn",
            category = "Thủ",
            cost = 1980,
            stats = "+180 Giáp vật lý, +180 Giáp phép, +1200 Máu",
            passiveDesc = "Chủ Động: Kích hoạt nhận lá chắn tương đương 30% máu tối đa + 10% máu đã mất trong 3s.",
            counterPurpose = "Giúp Đỡ Đòn và Đấu Sĩ lật ngược thế cờ khi bị dồn sát thương hội đồng."
        ),
        GameItem(
            name = "Thánh Kiếm",
            category = "Công",
            cost = 2000,
            stats = "+100 Công vật lý, +25% Tỉ lệ chí mạng",
            passiveDesc = "Cuồng Bạo: Tăng thêm 50% Sát thương chí mạng.",
            counterPurpose = "Trang bị trấn phái giúp Xạ Thủ (Violet, Elsu, Yorn, Laville) bùng nổ sát thương."
        )
    )
}

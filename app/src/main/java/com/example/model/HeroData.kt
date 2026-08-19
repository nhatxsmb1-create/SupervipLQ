package com.example.model

data class HeroInfo(
    val name: String,
    val role: String,
    val powerSpike: String, // "Đầu Trận", "Giữa Trận", "Cuối Trận", "Toàn Trận"
    val counterHeroes: List<String>,
    val counteredBy: List<String>,
    val counterStrategy: String,
    val recommendedItems: List<String>
)

object HeroDatabase {
    val heroes: List<HeroInfo> = listOf(
        HeroInfo(
            name = "Florentino",
            role = "Đấu Sĩ / Đường Tà Thần",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Omen", "Arthur", "Taara", "Lữ Bố"),
            counteredBy = listOf("Aleister", "Arum", "Hayate"),
            counterStrategy = "Dùng khống chế cứng không thể hóa giải (Arum/Aleister) hoặc thả diều bằng Sát Thương Chuẩn (Hayate). Tuyệt đối không đứng trong bãi hoa.",
            recommendedItems = listOf("Đao Truy Hồn", "Áo Choàng Băng Giá", "Khiên Thất Truyền")
        ),
        HeroInfo(
            name = "Nakroth",
            role = "Sát Thủ / Đi Rừng",
            powerSpike = "Đầu & Giữa Trận",
            counterHeroes = listOf("Yorn", "Tel'Annas", "Krixi", "Veera"),
            counteredBy = listOf("Mina", "Gildur", "Rourke", "Baldum"),
            counterStrategy = "Tổ chức cướp Bùa Xanh đầu trận. Chọn Trợ Thủ khống chế cứng để ngắt 3 lần lướt gank chớp nhoáng.",
            recommendedItems = listOf("Khiên Huyền Thoại", "Huân Chương Troy", "Nham Thuẫn")
        ),
        HeroInfo(
            name = "Violet",
            role = "Xạ Thủ (AD) / Đường Rồng",
            powerSpike = "Cuối Trận",
            counterHeroes = listOf("Valhein", "Slimz", "Fennik"),
            counteredBy = listOf("Ngộ Không", "Keera", "Aoi", "Kaine"),
            counterStrategy = "Bắt lẻ ngay sau khi Violet vừa lướt Đạn Xuyên Thấu. Lên giáp vật lý sớm để giảm sát thương chí mạng.",
            recommendedItems = listOf("Thánh Kiếm", "Kiếm Muramasa", "Giáp Hộ Mệnh")
        ),
        HeroInfo(
            name = "Tulen",
            role = "Pháp Sư (Mid) / Đi Rừng",
            powerSpike = "Giữa Trận",
            counterHeroes = listOf("Raz", "Ignis", "Dirak"),
            counteredBy = listOf("Chaugnar", "Toro", "Qi"),
            counterStrategy = "Đỡ Đòn chủ động đứng chắn đạn Lôi Quang khi Tulen tung chiêu cuối. Toàn đội bắt buộc lên Huân Chương Troy.",
            recommendedItems = listOf("Huân Chương Troy", "Giáp Gaia", "Quyền Trượng Rhea")
        ),
        HeroInfo(
            name = "Mina",
            role = "Đỡ Đòn / Trợ Thủ (SP)",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Xạ Thủ Tốc Đánh", "Valhein", "Capheny"),
            counteredBy = listOf("Hayate (Sát Thương Chuẩn)", "Lauriel", "Thorne"),
            counterStrategy = "Không đánh thường liên tục vào Mina khi chưa có trang bị giảm hồi máu. Dùng tướng cấu rỉa sát thương chuẩn.",
            recommendedItems = listOf("Sách Truy Hồn", "Đao Truy Hồn", "Kiếm Muramasa")
        ),
        HeroInfo(
            name = "Veres",
            role = "Đấu Sĩ / Đi Rừng & Đường Tà Thần",
            powerSpike = "Giữa Trận",
            counterHeroes = listOf("Tướng cận chiến", "Skud", "Astrid"),
            counteredBy = listOf("Đao/Sách Truy Hồn", "Capheny", "Elsu"),
            counterStrategy = "Lên ngay Đao Truy Hồn hoặc Sách Truy Hồn từ trang bị thứ 2 để triệt tiêu hoàn toàn nội tại hồi máu khi xoay xích.",
            recommendedItems = listOf("Đao Truy Hồn", "Sách Truy Hồn", "Khiên Huyền Thoại")
        ),
        HeroInfo(
            name = "Elsu",
            role = "Xạ Thủ (AD) / Bắn Tỉa",
            powerSpike = "Toàn Trận",
            counterHeroes = listOf("Pháp Sư kém cơ động", "Arthur", "Maloch"),
            counteredBy = listOf("Aoi", "Zill", "Kaine", "Ngộ Không"),
            counterStrategy = "Dùng Sát Thủ tàng hình hoặc bay nhảy áp sát nhanh. Phá mắt Ưng Trạm của Elsu trong bụi cỏ.",
            recommendedItems = listOf("Kiếm Muramasa", "Nanh Fenrir", "Giáp Hộ Mệnh")
        ),
        HeroInfo(
            name = "Aoi",
            role = "Sát Thủ / Đi Rừng",
            powerSpike = "Đầu & Giữa Trận",
            counterHeroes = listOf("Chủ Lực Máu Giấy", "Yorn", "Natalya"),
            counteredBy = listOf("Aleister", "Baldum", "Mina", "Arum"),
            counterStrategy = "Ngắt chiêu đu dây Long Trảo bằng hất tung hoặc trói chân. Đứng tụ lại để chia sẻ sát thương chiêu cuối.",
            recommendedItems = listOf("Huân Chương Troy", "Liềm Đoạt Mệnh", "Nham Thuẫn")
        )
    )
}

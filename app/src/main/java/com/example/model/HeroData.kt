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
            name = "Biron",
            role = "Đấu Sĩ / Đường Tà Thần",
            powerSpike = "Đầu & Giữa Trận",
            counterHeroes = listOf("Florentino", "Arthur", "Skud", "Ryoma"),
            counteredBy = listOf("Hayate", "Aleister", "Arum", "Valhein"),
            counterStrategy = "Né trao đổi chiêu thức khi Biron tích đủ Thanh Năng Lượng (được tạo giáp & hồi máu lớn). Lên Đao/Sách Truy Hồn từ sớm và giữ khoảng cách cấu rỉa.",
            recommendedItems = listOf("Đao Truy Hồn", "Áo Choàng Băng Giá", "Huân Chương Troy")
        ),
        HeroInfo(
            name = "Stuart",
            role = "Xạ Thủ (AD) / Đường Rồng",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Violet", "Valhein", "Yorn", "Slimz"),
            counteredBy = listOf("Liliana", "Tulen", "Krixi", "Aoi"),
            counterStrategy = "Stuart có chiêu 2 Miễn Sát Thương Vật Lý. Tuyệt đối không dồn sát thương vật lý khi Stuart đang bật chiêu 2. Dùng Pháp Sư dồn sát thương phép để hạ gục nhanh.",
            recommendedItems = listOf("Huân Chương Troy", "Sách Truy Hồn", "Quả Cầu Băng Sương")
        ),
        HeroInfo(
            name = "Helen",
            role = "Trợ Thủ (SP) / Hồi Máu",
            powerSpike = "Toàn Trận",
            counterHeroes = listOf("Đội hình giao tranh dài hơi", "Thane", "Lumburr"),
            counteredBy = listOf("Sách Truy Hồn", "Đao Truy Hồn", "Yue", "Iggy"),
            counterStrategy = "BẮT BỘC toàn đội Pháp Sư / Sát Thủ / Xạ Thủ lên Sách Truy Hồn hoặc Đao Truy Hồn ngay trang bị thứ 1 hoặc thứ 2. Tập trung sát thương diện rộng dồn chết Helen trước.",
            recommendedItems = listOf("Sách Truy Hồn", "Đao Truy Hồn", "Mặt Nạ Berith")
        ),
        HeroInfo(
            name = "Florentino",
            role = "Đấu Sĩ / Đường Tà Thần",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Omen", "Arthur", "Taara", "Lữ Bố"),
            counteredBy = listOf("Aleister", "Arum", "Hayate", "Biron"),
            counterStrategy = "Dùng khống chế cứng không thể hóa giải (Arum/Aleister) hoặc thả diều bằng Sát Thương Chuẩn (Hayate). Tuyệt đối không đứng trong bãi hoa.",
            recommendedItems = listOf("Đao Truy Hồn", "Áo Choàng Băng Giá", "Khiên Thất Truyền")
        ),
        HeroInfo(
            name = "Aoi",
            role = "Sát Thủ / Đi Rừng",
            powerSpike = "Đầu & Giữa Trận",
            counterHeroes = listOf("Chủ Lực Máu Giấy", "Yorn", "Natalya", "Krixi"),
            counteredBy = listOf("Aleister", "Baldum", "Mina", "Arum"),
            counterStrategy = "Ngắt chiêu đu dây Long Trảo bằng hất tung hoặc trói chân. Đứng tụ lại để chia sẻ sát thương chiêu cuối.",
            recommendedItems = listOf("Huân Chương Troy", "Liềm Đoạt Mệnh", "Nham Thuẫn")
        ),
        HeroInfo(
            name = "Liliana",
            role = "Pháp Sư (Mid) / Toàn Năng",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Krixi", "Veera", "Natalya", "Tulen"),
            counteredBy = listOf("Chaugnar", "Aoi", "Kaine"),
            counterStrategy = "Liliana biến hình dạng Cáo sẽ tăng giáp/giáp phép và có ảo ảnh né chiêu. Chờ Liliana dùng xong dạng Cáo rồi mới bắt.",
            recommendedItems = listOf("Huân Chương Troy", "Giáp Gaia", "Quả Cầu Băng Sương")
        ),
        HeroInfo(
            name = "Yue",
            role = "Pháp Sư (Mid) / Cấu Rỉa Tầm Xa",
            powerSpike = "Giữa & Cuối Trận",
            counterHeroes = listOf("Thane", "Mina", "Tel'Annas", "Yorn"),
            counteredBy = listOf("Nakroth", "Aoi", "Kaine", "Zuka"),
            counterStrategy = "Né các giao điểm cắt nhau của quạt (nơi nhận nhân 4 sát thương). Dùng Sát Thủ cơ động bay vòng sau lưng bắt Yue.",
            recommendedItems = listOf("Huân Chương Troy", "Liềm Đoạt Mệnh", "Giáp Gaia")
        ),
        HeroInfo(
            name = "Kaine",
            role = "Sát Thủ / Đi Rừng & Trợ Thủ",
            powerSpike = "Đầu & Giữa Trận",
            counterHeroes = listOf("Yorn", "Tel'Annas", "Krixi", "Veera"),
            counteredBy = listOf("Phụ Kiện Trợ Thủ Ma Nhãn", "Max", "Lindis", "Elsu"),
            counterStrategy = "Trợ Thủ phải mua Phụ Kiện Trợ Thủ Ma Nhãn để soi tàng hình. Chủ lực không đi lẻ một mình ngoài đường khi mất tầm nhìn.",
            recommendedItems = listOf("Phụ Kiện Ma Nhãn", "Khiên Huyền Thoại", "Huân Chương Troy")
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
            name = "Elsu",
            role = "Xạ Thủ (AD) / Bắn Tỉa",
            powerSpike = "Toàn Trận",
            counterHeroes = listOf("Pháp Sư kém cơ động", "Arthur", "Maloch"),
            counteredBy = listOf("Aoi", "Zill", "Kaine", "Ngộ Không"),
            counterStrategy = "Dùng Sát Thủ tàng hình hoặc bay nhảy áp sát nhanh. Phá mắt Ưng Trạm của Elsu trong bụi cỏ.",
            recommendedItems = listOf("Kiếm Muramasa", "Nanh Fenrir", "Giáp Hộ Mệnh")
        )
    )
}


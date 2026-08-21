package com.example.tactical

import com.example.model.HeroScoreboardEntry
import com.example.model.ItemRecommendation
import com.example.model.ThreatPlayer
import java.util.Locale

enum class HeroTrait {
    HEALING_DRAIN,     // Hồi phục mạnh (Arum, Florentino, Taara, Lubu, Helen, Kil'Groth)
    BURST_MAGIC,       // Sốc sát thương phép (Raz, Natalya, Krixi, Veera, Liliana, Yue, Dirak)
    HARD_SUPPRESSION,  // Khống chế áp chế cứng không giải được (Arum, Aleister)
    HIGH_MOBILITY,     // Cơ động áp sát (Nakroth, Zuka, Aoi, Murad, Keera, Yan)
    TRUE_DAMAGE,       // Sát thương chuẩn (Florentino, Hayate, Maloch, Richter)
    PHYSICAL_BURST,    // Sát thương vật lý dồn dame (Zuka, Nakroth, Quillen, Wukong)
    TANK_FRONTLINE,    // Đỡ đòn trâu bò (Arthur, Thane, Baldum, Toro, Mina)
    CONTINUOUS_DPS     // Sát thương duy trì/Bắn rỉa (Valhein, Capheny, Hayate, Tel'Annas)
}

data class HeroInfo(
    val canonicalName: String,
    val aliases: List<String>,
    val role: String,
    val traits: List<HeroTrait>
)

data class RosterAnalysisResult(
    val allyHeroes: List<String>,
    val enemyHeroes: List<String>,
    val threats: List<ThreatPlayer>,
    val counterItems: List<ItemRecommendation>,
    val tacticalCallout: String,
    val summaryReport: String
)

class AoVRosterAnalyzer {

    private val heroDatabase = listOf(
        HeroInfo("Florentino", listOf("florentino", "flowborn", "flo"), "Đấu sĩ", listOf(HeroTrait.HEALING_DRAIN, HeroTrait.TRUE_DAMAGE, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Arum", listOf("arum", "su su"), "Đỡ đòn / Hỗ trợ", listOf(HeroTrait.HEALING_DRAIN, HeroTrait.HARD_SUPPRESSION, HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Raz", listOf("raz", "quyen vuong"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Zuka", listOf("zuka", "dai su gautruc", "gau"), "Đấu sĩ / Sát thủ", listOf(HeroTrait.PHYSICAL_BURST, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Arthur", listOf("arthur", "hiep si"), "Đỡ đòn / Đấu sĩ", listOf(HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Nakroth", listOf("nakroth", "nak", "luoi dao phan xet"), "Sát thủ / Rừng", listOf(HeroTrait.HIGH_MOBILITY, HeroTrait.PHYSICAL_BURST)),
        HeroInfo("Valhein", listOf("valhein", "val", "6 giay"), "Xạ thủ", listOf(HeroTrait.CONTINUOUS_DPS)),
        HeroInfo("Natalya", listOf("natalya", "nat"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC)),
        HeroInfo("Krixi", listOf("krixi", "buom"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC)),
        HeroInfo("Aleister", listOf("aleister", "tu tu"), "Pháp sư", listOf(HeroTrait.HARD_SUPPRESSION)),
        HeroInfo("Taara", listOf("taara"), "Đấu sĩ", listOf(HeroTrait.HEALING_DRAIN, HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Lubu", listOf("lubu", "lu bo"), "Đấu sĩ", listOf(HeroTrait.HEALING_DRAIN)),
        HeroInfo("Helen", listOf("helen", "payna"), "Trợ thủ", listOf(HeroTrait.HEALING_DRAIN)),
        HeroInfo("Aya", listOf("aya"), "Trợ thủ", listOf(HeroTrait.BURST_MAGIC)),
        HeroInfo("Tulen", listOf("tulen"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Hayate", listOf("hayate"), "Xạ thủ", listOf(HeroTrait.TRUE_DAMAGE, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Capheny", listOf("capheny"), "Xạ thủ", listOf(HeroTrait.CONTINUOUS_DPS)),
        HeroInfo("Violet", listOf("violet"), "Xạ thủ", listOf(HeroTrait.PHYSICAL_BURST)),
        HeroInfo("Elsu", listOf("elsu"), "Xạ thủ", listOf(HeroTrait.PHYSICAL_BURST)),
        HeroInfo("Aoi", listOf("aoi"), "Sát thủ", listOf(HeroTrait.HIGH_MOBILITY, HeroTrait.PHYSICAL_BURST)),
        HeroInfo("Murad", listOf("murad"), "Sát thủ", listOf(HeroTrait.HIGH_MOBILITY, HeroTrait.PHYSICAL_BURST)),
        HeroInfo("Veera", listOf("veera"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC)),
        HeroInfo("Liliana", listOf("liliana", "cao"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Maloch", listOf("maloch"), "Đấu sĩ", listOf(HeroTrait.TRUE_DAMAGE, HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Thane", listOf("thane"), "Đỡ đòn", listOf(HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Baldum", listOf("baldum"), "Đỡ đòn", listOf(HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Toro", listOf("toro"), "Đỡ đòn", listOf(HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Mina", listOf("mina"), "Đỡ đòn", listOf(HeroTrait.HEALING_DRAIN, HeroTrait.TANK_FRONTLINE)),
        HeroInfo("Omen", listOf("omen"), "Đấu sĩ", listOf(HeroTrait.TRUE_DAMAGE, HeroTrait.HARD_SUPPRESSION)),
        HeroInfo("Yan", listOf("yan"), "Đấu sĩ / Sát thủ", listOf(HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Keera", listOf("keera"), "Sát thủ phép", listOf(HeroTrait.BURST_MAGIC, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Yue", listOf("yue"), "Pháp sư", listOf(HeroTrait.BURST_MAGIC)),
        HeroInfo("Bijan", listOf("bijan"), "Đấu sĩ", listOf(HeroTrait.TANK_FRONTLINE, HeroTrait.HIGH_MOBILITY)),
        HeroInfo("Stuart", listOf("stuart", "joker"), "Xạ thủ", listOf(HeroTrait.PHYSICAL_BURST))
    )

    fun findHeroByName(rawText: String): HeroInfo? {
        val clean = rawText.lowercase(Locale.ROOT)
        for (hero in heroDatabase) {
            if (clean.contains(hero.canonicalName.lowercase(Locale.ROOT))) return hero
            for (alias in hero.aliases) {
                if (clean.contains(alias)) return hero
            }
        }
        return null
    }

    /**
     * Phân tích đội hình 2 team từ OCR raw text hoặc danh sách bóc tách
     */
    fun analyzeRosters(
        allyRoster: List<HeroScoreboardEntry>,
        enemyRoster: List<HeroScoreboardEntry>,
        rawText: String
    ): RosterAnalysisResult {
        val detectedAllies = mutableListOf<String>()
        val detectedEnemies = mutableListOf<String>()

        // 1. Phân loại tướng từ Roster hoặc Raw Text
        for (entry in allyRoster) {
            val hero = findHeroByName(entry.heroName)
            if (hero != null && !detectedAllies.contains(hero.canonicalName)) {
                detectedAllies.add(hero.canonicalName)
            }
        }

        for (entry in enemyRoster) {
            val hero = findHeroByName(entry.heroName)
            if (hero != null && !detectedEnemies.contains(hero.canonicalName)) {
                detectedEnemies.add(hero.canonicalName)
            }
        }

        // Nếu roster rỗng, quét từ raw text
        if (detectedAllies.isEmpty() && detectedEnemies.isEmpty()) {
            val lower = rawText.lowercase(Locale.ROOT)
            for (hero in heroDatabase) {
                if (hero.aliases.any { lower.contains(it) }) {
                    if (!detectedEnemies.contains(hero.canonicalName) && detectedEnemies.size < 5) {
                        detectedEnemies.add(hero.canonicalName)
                    }
                }
            }
        }

        // 2. Tìm đặc tính của team địch để xây dựng chiến thuật khắc chế
        val enemyTraits = mutableSetOf<HeroTrait>()
        for (heroName in detectedEnemies) {
            val hero = findHeroByName(heroName)
            hero?.traits?.let { enemyTraits.addAll(it) }
        }

        val counterItems = mutableListOf<ItemRecommendation>()
        val threatPlayers = mutableListOf<ThreatPlayer>()

        // Nếu địch có hồi phục mạnh (Arum, Florentino, Taara, Lubu) -> Đao/Sách Truy Hồn
        if (enemyTraits.contains(HeroTrait.HEALING_DRAIN)) {
            val healers = detectedEnemies.filter {
                findHeroByName(it)?.traits?.contains(HeroTrait.HEALING_DRAIN) == true
            }.joinToString(", ")

            counterItems.add(
                ItemRecommendation(
                    itemName = "Đao Truy Hồn / Sách Truy Hồn",
                    reason = "Khắc chế hồi phục của $healers (Giảm 50% hồi máu)",
                    category = "Bắt Buộc Lên Khắc Chế",
                    costGold = 2000,
                    isCounter = true
                )
            )
        }

        // Nếu địch có sát thương phép sốc (Raz, Natalya, Krixi, Veera) -> Huân Chương Troy / Giày Kiên Cường
        if (enemyTraits.contains(HeroTrait.BURST_MAGIC)) {
            val mages = detectedEnemies.filter {
                findHeroByName(it)?.traits?.contains(HeroTrait.BURST_MAGIC) == true
            }.joinToString(", ")

            counterItems.add(
                ItemRecommendation(
                    itemName = "Huân Chương Troy / Giày Kiên Cường",
                    reason = "Giảm sốc dame phép & kháng hiệu ứng từ $mages",
                    category = "Trang Bị Phòng Thủ",
                    costGold = 2220,
                    isCounter = true
                )
            )
        }

        // Nếu địch có khống chế áp chế cứng (Arum, Aleister)
        if (enemyTraits.contains(HeroTrait.HARD_SUPPRESSION)) {
            val suppressors = detectedEnemies.filter {
                findHeroByName(it)?.traits?.contains(HeroTrait.HARD_SUPPRESSION) == true
            }.joinToString(", ")

            counterItems.add(
                ItemRecommendation(
                    itemName = "Quả Cầu Băng Sương / Giáp Hộ Mệnh",
                    reason = "Tránh bị $suppressors trói áp chế dồn sát thương chết sốc",
                    category = "Trang Bị Giữ Mạng",
                    costGold = 2400,
                    isCounter = true
                )
            )
        }

        // 3. Phân tích đối thủ nguy hiểm nhất (Threats)
        for (entry in enemyRoster) {
            val hero = findHeroByName(entry.heroName) ?: HeroInfo(entry.heroName, emptyList(), "Địch", emptyList())
            val threatLevel = if (entry.gold >= 7000 || entry.kda.contains("9") || entry.kda.contains("10")) "CAO" else "TRUNG BÌNH"
            val tip = when {
                hero.traits.contains(HeroTrait.HARD_SUPPRESSION) -> "Tuyệt đối không lao vào trước khi chiêu cuối được tung ra!"
                hero.traits.contains(HeroTrait.HEALING_DRAIN) -> "Lên Đao/Sách Truy Hồn sớm để ngắt hồi máu."
                hero.traits.contains(HeroTrait.BURST_MAGIC) -> "Né cầu lửa/chiêu thức tầm xa, lên Huân Chương Troy."
                else -> "Giữ vị trí, đánh nhấp nhả cùng đồng đội."
            }
            threatPlayers.add(
                ThreatPlayer(
                    heroName = hero.canonicalName,
                    role = hero.role,
                    kda = if (entry.kda.isNotBlank()) entry.kda else "Xanh",
                    currentGold = entry.gold,
                    threatLevel = threatLevel,
                    counterTip = tip
                )
            )
        }

        // 4. Tạo Callout giọng nói HLV
        val voiceCallout = when {
            enemyTraits.contains(HeroTrait.HARD_SUPPRESSION) && enemyTraits.contains(HeroTrait.HEALING_DRAIN) ->
                "Cảnh báo: Địch có Arum và Florentino hồi phục mạnh và có trói cứng. Cần lên ngay Đao Truy Hồn hoặc Sách Truy Hồn và giữ khoảng cách an toàn!"
            enemyTraits.contains(HeroTrait.HEALING_DRAIN) ->
                "Đội hình địch có tướng hồi máu cao. Hãy hoàn thành Đao Truy Hồn hoặc Sách Truy Hồn để giảm nửa hồi máu của đối thủ!"
            enemyTraits.contains(HeroTrait.BURST_MAGIC) ->
                "Pháp sư bên địch đang có sát thương lớn. Hãy lên thêm Giày Kiên Cường và Huân Chương Troy!"
            else ->
                "Đã phân tích đội hình hai đội. Hãy giữ đúng cự ly giao tranh và tập trung mục tiêu lớn!"
        }

        val summary = StringBuilder()
        if (detectedAllies.isNotEmpty()) summary.append("Ta: ").append(detectedAllies.joinToString(", ")).append("\n")
        if (detectedEnemies.isNotEmpty()) summary.append("Địch: ").append(detectedEnemies.joinToString(", ")).append("\n")
        if (counterItems.isNotEmpty()) summary.append("Khắc chế: ").append(counterItems.joinToString { it.itemName })

        return RosterAnalysisResult(
            allyHeroes = detectedAllies,
            enemyHeroes = detectedEnemies,
            threats = threatPlayers,
            counterItems = counterItems,
            tacticalCallout = voiceCallout,
            summaryReport = summary.toString()
        )
    }
}

package com.example.model

enum class SupportedGame(
    val title: String,
    val defaultDragonName: String,
    val defaultSlayerName: String
) {
    ARENA_OF_VALOR("Liên Quân Mobile (Arena of Valor)", "Rồng Krayg / Rồng Ánh Sáng", "Tà Thần Caesar"),
    WILD_RIFT("Liên Minh: Tốc Chiến (Wild Rift)", "Rồng Nguyên Tố", "Baron Nashor"),
    MLBB("Mobile Legends: Bang Bang", "Rùa Thần Krayg", "Lord Bạo Chúa")
}

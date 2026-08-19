package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameItem
import com.example.model.HeroInfo
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlowGradient
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ManaPurple
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VictoryGreen
import com.example.viewmodel.StrategyViewModel

@Composable
fun StrategyScreen(viewModel: StrategyViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredHeroes by viewModel.filteredHeroes.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()

    val tabTitles = listOf("TƯỚNG KHẮC CHẾ", "TRANG BỊ KHẮC CHẾ", "DÒNG THỜI GIAN")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsDarkBg)
            .padding(16.dp)
            .testTag("strategy_screen_root")
    ) {
        // Tiêu đề
        Text(
            text = "CẨM NANG KHẮC CHẾ LIÊN QUÂN",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = TextGold
        )
        Text(
            text = "Khắc chế chất tướng, chọn trang bị chuẩn và kiểm soát mục tiêu lớn",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Thanh Chuyển Tab Esports
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = EsportsSurface,
            contentColor = ImperialGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ImperialGold
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.setTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (selectedTab == index) ImperialGoldLight else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Tab Tướng Khắc Chế
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Tìm tướng (Florentino, Nakroth, Violet...)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = ImperialGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EsportsSurface,
                        unfocusedContainerColor = EsportsSurface,
                        focusedBorderColor = ImperialGold,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHeroes, key = { it.name }) { hero ->
                        EsportsHeroCard(hero = hero)
                    }
                }
            }
            1 -> {
                // Tab Trang Bị Khắc Chế
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Tìm trang bị (Đao Truy Hồn, Huân Chương Troy...)", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = ArcaneCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EsportsSurface,
                        unfocusedContainerColor = EsportsSurface,
                        focusedBorderColor = ArcaneCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.name }) { item ->
                        EsportsItemCard(item = item)
                    }
                }
            }
            2 -> {
                // Tab Dòng Thời Gian Trận Đấu
                EsportsTimelineGuide()
            }
        }
    }
}

@Composable
fun EsportsHeroCard(hero: HeroInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GoldBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("hero_card_${hero.name}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = hero.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = hero.role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcaneCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1E38))
                        .border(1.dp, ImperialGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ngưỡng Sức Mạnh: ${hero.powerSpike}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = ImperialGoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bị khắc chế bởi (Countered by)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Bị Khắc Chế",
                    tint = DefeatRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bị Khắc Chế Bởi: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = DefeatRed
                )
                Text(
                    text = hero.counteredBy.joinToString(", "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Khắc Chế Cứng (Counters)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Khắc Chế Cứng",
                    tint = VictoryGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Khắc Chế Cứng: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = VictoryGreen
                )
                Text(
                    text = hero.counterHeroes.joinToString(", "),
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chiến thuật đối đầu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EsportsSurfaceElevated)
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Mẹo Đối Đầu: ${hero.counterStrategy}",
                    fontSize = 11.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Trang bị khuyến nghị
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = "Trang Bị",
                    tint = ImperialGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lên Đồ Đối Đầu: ${hero.recommendedItems.joinToString(" • ")}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGoldLight
                )
            }
        }
    }
}

@Composable
fun EsportsItemCard(item: GameItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("item_card_${item.name}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F1E38))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcaneCyan
                        )
                    }
                }

                Text(
                    text = "${item.cost} Vàng",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = ImperialGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.stats,
                fontSize = 11.sp,
                color = VictoryGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.passiveDesc,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF261836))
                    .border(1.dp, ImperialGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Mục Đích Khắc Chế: ${item.counterPurpose}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGoldLight
                )
            }
        }
    }
}

@Composable
fun EsportsTimelineGuide() {
    val timelineEvents = listOf(
        Triple("00:30", "Bắt Đầu Trận & Bùa Rừng Xuất Hiện", "Rừng ăn Bùa Xanh/Bùa Đỏ. Đường Giữa dọn đợt lính 1 rồi hỗ trợ lấy tầm nhìn bờ sông."),
        Triple("01:10", "Dơi Thủ Vệ Xuất Hiện", "Cung cấp hồi phục và bùa lợi tăng tốc. Đấu Sĩ đường Caesar chú ý tranh chấp để lấy ưu thế solo."),
        Triple("02:00", "Rồng Krayg & Rồng Ánh Sáng", "Mục tiêu lớn đầu tiên! Cung cấp lượng vàng và kinh nghiệm vượt trội cho toàn đội."),
        Triple("04:00", "Hết Khiên Bảo Hộ Trụ Ngoài", "Khiên giảm sát thương trụ biến mất. Bắt đầu đẩy mạnh phá trụ ngoài lấy thế kiểm soát rừng đối phương."),
        Triple("08:00", "Tà Thần Caesar Cấp 2", "Tà Thần Caesar xuất hiện. Giúp triệu hồi Caesar bóng đêm đẩy sập trụ trong đối phương."),
        Triple("15:00", "Caesar Bạo Chúa (Cấp 3)", "Mục tiêu tối thượng định đoạt thắng bại toàn trận! Cung cấp giáp Caesar và lính thần tiên công phá nhà chính.")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(timelineEvents) { (time, title, desc) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EsportsCardGradient)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1E38))
                            .border(1.dp, ImperialGold, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = ImperialGoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

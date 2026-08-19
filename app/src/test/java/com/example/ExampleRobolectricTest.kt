package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.tactical.TacticalEngine
import com.example.model.DetectedScreenMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Trợ Lý Liên Quân AI", appName)
    }

    @Test
    fun testTacticalEngineBehindGoldEvaluatesAvoidTeamfight() {
        val engine = TacticalEngine()
        val result = engine.evaluate(
            matchTimeSeconds = 400,
            goldDiff = -5200,
            allyKills = 3,
            enemyKills = 9,
            allyTowers = 1,
            enemyTowers = 3,
            detectedMode = DetectedScreenMode.IDLE
        )
        assertEquals("Tránh giao tranh - Đang thua tiền", result.voiceCallout)
        assertTrue(result.newState.winProbability < 50)
    }
}

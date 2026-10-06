package com.shilapi.xcertplay

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27, 33], manifest = Config.NONE)
class ReturnToCarLabelMigrationTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val prefs get() = context.getSharedPreferences("xcertplay_airplay", Context.MODE_PRIVATE)

    @Before fun clearPreferences() { prefs.edit().clear().commit() }

    @Test fun missingAndBlankLabelsUseReturnToCar() {
        assertEquals("返回车机", AirPlayPersistence.loadOemLabel(context))
        for (label in listOf("", "  ")) {
            AirPlayPersistence.saveOemLabel(context, label)
            assertEquals("返回车机", AirPlayPersistence.loadOemLabel(context))
        }
    }

    @Test fun previousBrandDefaultsMigrateAndPersistWithoutChangingAudioPreferences() {
        AirPlayPersistence.saveAudioFocusEnabled(context, true)
        AirPlayPersistence.saveNavigationAudioFocusEnabled(context, true)
        AirPlayPersistence.saveNavigationAudioChannel(context, 14)
        for (label in listOf("欧拉", " 欧拉 ", "歐拉", "BYD", "byd", "比亚迪", "比亞迪")) {
            AirPlayPersistence.saveOemLabel(context, label)
            val before = prefs.all
            assertEquals("返回车机", AirPlayPersistence.loadOemLabel(context))
            assertEquals(before + ("oem_label" to "返回车机"), prefs.all)
            assertEquals("返回车机", prefs.getString("oem_label", null))
            assertEquals("返回车机", AirPlayPersistence.loadOemLabel(context))
        }
    }

    @Test fun customLabelsArePreservedExactly() {
        for (label in listOf("我的车", "  My car  ", "欧拉好猫")) {
            AirPlayPersistence.saveOemLabel(context, label)
            val before = prefs.all
            assertEquals(label, AirPlayPersistence.loadOemLabel(context))
            assertEquals(before, prefs.all)
        }
    }

    @Test fun newDefaultIsStableAcrossRepeatedLoads() {
        AirPlayPersistence.saveOemLabel(context, "返回车机")
        val before = prefs.all
        repeat(3) { assertEquals("返回车机", AirPlayPersistence.loadOemLabel(context)) }
        assertEquals(before, prefs.all)
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ReminderEntity
import com.example.location.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GeoReminder", appName)
  }

  @Test
  fun `test distance calculation and geofence evaluation`() {
    val distance = LocationHelper.calculateDistanceMeters(
      37.7749, -122.4194,
      37.7749, -122.4194
    )
    assertEquals(0f, distance, 0.1f)

    // Test reminder weekday mask
    assertTrue(ReminderEntity.isDaySelected(ReminderEntity.MASK_WEEKDAYS, 0)) // Mon
    assertTrue(ReminderEntity.isDaySelected(ReminderEntity.MASK_WEEKDAYS, 4)) // Fri
    assertFalse(ReminderEntity.isDaySelected(ReminderEntity.MASK_WEEKDAYS, 5)) // Sat
    assertFalse(ReminderEntity.isDaySelected(ReminderEntity.MASK_WEEKDAYS, 6)) // Sun
  }
}


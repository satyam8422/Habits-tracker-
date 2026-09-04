package com.example

import com.example.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testBottomNavItems_notNull() {
    val items = Screen.bottomNavItems
    assertEquals(5, items.size)
    for (item in items) {
      assertNotNull("Item should not be null", item)
      assertNotNull("Route should not be null", item.route)
      assertNotNull("Title should not be null", item.title)
    }
  }
}

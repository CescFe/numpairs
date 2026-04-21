package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Test

class StripItemTest {
    @Test
    fun hidden_item_is_supported() {
        val stripItem = StripItem.Hidden

        assertEquals(StripItem.Hidden, stripItem)
    }

    @Test
    fun known_item_stores_a_positive_value() {
        val stripItem = StripItem.Known(7)

        assertEquals(7, stripItem.value)
    }

    @Test
    fun player_entered_item_stores_a_positive_value() {
        val stripItem = StripItem.PlayerEntered(9)

        assertEquals(9, stripItem.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun known_item_requires_a_positive_value() {
        StripItem.Known(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun player_entered_item_requires_a_positive_value() {
        StripItem.PlayerEntered(-1)
    }
}

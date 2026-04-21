package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

    @Test
    fun known_item_requires_a_positive_value() {
        assertThrows(IllegalArgumentException::class.java) {
            StripItem.Known(0)
        }
    }

    @Test
    fun player_entered_item_requires_a_positive_value() {
        assertThrows(IllegalArgumentException::class.java) {
            StripItem.PlayerEntered(-1)
        }
    }

    @Test
    fun hidden_item_can_be_completed_with_a_player_entered_value() {
        val completedItem = StripItem.Hidden.completeWith(8)

        assertEquals(StripItem.PlayerEntered(8), completedItem)
    }

    @Test
    fun completing_a_hidden_item_requires_a_positive_value() {
        assertThrows(IllegalArgumentException::class.java) {
            StripItem.Hidden.completeWith(0)
        }
    }

    @Test
    fun completing_a_known_item_is_not_allowed() {
        assertThrows(IllegalStateException::class.java) {
            StripItem.Known(5).completeWith(8)
        }
    }

    @Test
    fun completing_a_player_entered_item_can_replace_it_with_a_new_value() {
        val completedItem = StripItem.PlayerEntered(5).completeWith(8)

        assertEquals(StripItem.PlayerEntered(8), completedItem)
    }

    @Test
    fun completing_a_player_entered_item_can_keep_the_same_value() {
        val completedItem = StripItem.PlayerEntered(5).completeWith(5)

        assertEquals(StripItem.PlayerEntered(5), completedItem)
    }
}

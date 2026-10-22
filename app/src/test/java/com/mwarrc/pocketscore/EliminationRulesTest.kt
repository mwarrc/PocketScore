package com.mwarrc.pocketscore

import com.mwarrc.pocketscore.domain.logic.EliminationRules
import com.mwarrc.pocketscore.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the two rules that decide whether a player can still be scored:
 * whether they are eliminated at all, and whether their input is locked once
 * they are.
 *
 * Both used to be written out inline in several places and had already drifted
 * apart — the card honoured the "allow eliminated input" setting while the
 * numpad did not, so digits still landed in an eliminated player's field.
 * They now live in [EliminationRules] and are pinned here.
 */
class EliminationRulesTest {

    private fun player(name: String, score: Int) = Player(name = name, score = score)

    // --- isInputLocked ---

    @Test
    fun eliminatedPlayer_isLockedWhenInputIsNotAllowed() {
        assertTrue(
            EliminationRules.isInputLocked(
                isEliminated = true,
                allowEliminatedInput = false,
                strictTurnMode = false,
            )
        )
    }

    @Test
    fun eliminatedPlayer_isFullyAllowedWhenTheSettingIsOn() {
        // "Fully" is the point: the setting is not a softened lock, it removes
        // the lock altogether so the player scores like anyone else.
        assertFalse(
            EliminationRules.isInputLocked(
                isEliminated = true,
                allowEliminatedInput = true,
                strictTurnMode = false,
            )
        )
    }

    @Test
    fun strictMode_locksEliminatedPlayersEvenIfTheSettingWasEnabledFirst() {
        // The "allow eliminated input" toggle is hidden while strict mode is
        // on, so the setting can only be true there if it was turned on before.
        // Strict mode's promise is that the turn decides who scores, and an
        // out-of-contention player never gets the turn.
        assertTrue(
            EliminationRules.isInputLocked(
                isEliminated = true,
                allowEliminatedInput = true,
                strictTurnMode = true,
            )
        )
    }

    @Test
    fun activePlayer_isNeverLockedByElimination() {
        assertFalse(
            EliminationRules.isInputLocked(
                isEliminated = false,
                allowEliminatedInput = false,
                strictTurnMode = false,
            )
        )
        assertFalse(
            EliminationRules.isInputLocked(
                isEliminated = false,
                allowEliminatedInput = false,
                strictTurnMode = true,
            )
        )
    }

    // --- isEliminated ---

    @Test
    fun playerWhoCannotCatchTheLeader_isEliminated() {
        val leader = player("Leader", 30)
        val out = player("Out", 10)

        assertTrue(
            EliminationRules.isEliminated(
                player = out,
                activePlayers = listOf(leader, out),
                tableSum = 15,
                leaderScore = 30,
            )
        )
    }

    @Test
    fun tiedForTheLead_isNeverEliminated() {
        // A tie for the lead is not an elimination, however far behind the
        // rest of the table is.
        val first = player("First", 30)
        val alsoFirst = player("Also first", 30)
        val out = player("Out", 0)

        assertFalse(
            EliminationRules.isEliminated(
                player = alsoFirst,
                activePlayers = listOf(first, alsoFirst, out),
                tableSum = 5,
                leaderScore = 30,
            )
        )
    }

    @Test
    fun emptyTable_eliminatesNobody() {
        val leader = player("Leader", 30)
        val out = player("Out", 0)

        assertFalse(
            EliminationRules.isEliminated(
                player = out,
                activePlayers = listOf(leader, out),
                tableSum = 0,
                leaderScore = 30,
            )
        )
    }

    @Test
    fun nobodyHasScored_eliminatesNobody() {
        // The standings are meaningless at zero, so nothing can be out of
        // reach yet.
        val a = player("A", 0)
        val b = player("B", 0)

        assertFalse(
            EliminationRules.isEliminated(
                player = b,
                activePlayers = listOf(a, b),
                tableSum = 20,
                leaderScore = 0,
            )
        )
    }

    @Test
    fun eliminatedPlayerIds_matchesThePerPlayerRule() {
        val leader = player("Leader", 30)
        val middle = player("Middle", 25)
        val out = player("Out", 5)

        val players = listOf(leader, middle, out)

        assertEquals(
            setOf(out.id),
            EliminationRules.eliminatedPlayerIds(
                activePlayers = players,
                tableSum = 10,
                leaderScore = 30,
            )
        )
    }
}

package com.mwarrc.pocketscore.domain.logic

import com.mwarrc.pocketscore.domain.model.Player

/**
 * The single source of truth for "can this player still win?" — the elimination
 * calculation that decides whether a player is skipped in the turn rotation and
 * whether their card is labelled ELIMINATED.
 *
 * This exists because the same calculation had been written out **five** separate
 * times, and they had drifted:
 *
 *  - `GameViewModel.calculateNextTurnId` (the real one, used for skipping)
 *  - `ActivePlayerCard` and `PassivePlayerCard` (via `potentialMax < leaderScore`)
 *  - `PoolProbabilitySheet` (same, in the standings list)
 *  - `GameScreen.eliminatedIds` (same, driving the top-bar elimination pulse)
 *
 * The four non-ViewModel copies each dropped two guards that the ViewModel had:
 * the "nobody has scored yet" check and the "the table is empty" short-circuit.
 * The visible symptom was a card reading ELIMINATED (and the top bar pulsing)
 * while the turn rotation happily passed to that same player — the UI and the
 * rules disagreed about who was out.
 *
 * Keeping one implementation means the label and the rule can no longer diverge.
 */
object EliminationRules {

    /**
     * Whether [player] can no longer mathematically catch the leader.
     *
     * A player is eliminated only when the most they could still score (their
     * current score plus everything left on the table) falls short of the
     * leader's current score.
     *
     * Two guards keep this honest, and both are load-bearing:
     *
     *  - **Empty table** ([tableSum] <= 0): the game is over, nothing can be
     *    added, so nobody is "eliminated" — the endgame is handled elsewhere.
     *    Without this short-circuit every non-leader reads as eliminated the
     *    moment the last ball drops.
     *  - **Nobody has scored yet** (all active scores are 0): the standings are
     *    meaningless — the leader is just whoever the max happens to be, which
     *    is 0 for everyone. Negative scores in this state would otherwise be
     *    flagged as eliminated purely because they sit below a zero leader.
     *
     * A player who is currently tied for the lead is never eliminated, even if
     * [leaderScore] would suggest it — the tie check is done against the set of
     * everyone on the leading score rather than against a single player.
     *
     * @param player The player to test.
     * @param activePlayers Everyone still in the match (used for the leader set
     *   and the "nobody has scored" guard). Includes [player].
     * @param tableSum Total point value of the balls still on the table.
     * @param leaderScore Score of the current leader, as passed to the cards.
     */
    fun isEliminated(
        player: Player,
        activePlayers: List<Player>,
        tableSum: Int,
        leaderScore: Int,
    ): Boolean {
        // Nothing left on the table: the match is decided, not "elimination".
        if (tableSum <= 0) return false

        // Nobody has scored: there is no meaningful lead yet, so nothing can be
        // mathematically out of reach.
        if (activePlayers.none { it.score != 0 }) return false

        // Everyone sitting on the leading score is safe, including ties. A tie
        // for the lead is not an elimination.
        val isLeading = activePlayers.any { it.id == player.id && it.score == leaderScore }
        if (isLeading) return false

        val potentialMax = player.score + tableSum
        return potentialMax < leaderScore
    }

    /**
     * Whether an eliminated player's points must be locked out entirely.
     *
     * The single source of truth for the input gate, so the focused card, the
     * shared numpad buffer and the turn rotation can't drift apart. The rule
     * used to be written out three times and had already diverged: the card
     * honoured the "allow eliminated input" setting but the numpad did not, so
     * digits still landed in an eliminated player's field — the text appeared,
     * it just couldn't be applied, which read to the user as a broken app
     * rather than a deliberate lock.
     *
     * The rules:
     *
     *  - An eliminated player is locked unless [allowEliminatedInput] is on.
     *    When it is on, it is on *fully*: the player can be scored like anyone
     *    else, not just have the buttons enabled.
     *  - [strictTurnMode] always wins. The "allow eliminated input" toggle is
     *    hidden while strict mode is on, so the setting can only be true there
     *    if it was enabled first — and strict mode's promise is that the turn
     *    decides who scores, which an out-of-contention player never gets.
     *
     * @param isEliminated Whether the player is out of contention.
     * @param allowEliminatedInput Whether the user opted into scoring players
     *   who are out of contention.
     * @param strictTurnMode Whether strict turn mode is enabled.
     */
    fun isInputLocked(
        isEliminated: Boolean,
        allowEliminatedInput: Boolean,
        strictTurnMode: Boolean,
    ): Boolean = isEliminated && (strictTurnMode || !allowEliminatedInput)

    /**
     * The subset of [activePlayers] who are eliminated, by the same rule as
     * [isEliminated]. Provided so callers that need the whole set (the top-bar
     * pulse, for example) can't reimplement the loop with a different formula.
     */
    fun eliminatedPlayerIds(
        activePlayers: List<Player>,
        tableSum: Int,
        leaderScore: Int,
    ): Set<String> {
        if (activePlayers.size < 2) return emptySet()
        return activePlayers
            .filter { isEliminated(it, activePlayers, tableSum, leaderScore) }
            .map { it.id }
            .toSet()
    }
}

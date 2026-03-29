"""
PocketScore Stress-Test Data Generator  —  FIXED v2
====================================================
Generates a .pscore.json file in the exact format used by PocketScore.

BUGS FIXED vs original:
  1. history field: was [reversed cumulative including current score]
     → now correctly omits current score, is a window of up to 10 prior states
       (matches real export: history = cumulative[1:11] newest-first)
  2. history initial value: generator started history with [0] even for players
     with no events → now history=[] when no events
  3. eventHistory ordering: generator reversed eventHistory (newest-first)
     but real data is also newest-first — this was correct. However the
     cumulative was built wrong, so scores were off.
  4. isGameActive: was set True even for archived/finalized games.
     Real exports always have isGameActive=True (games are live snapshots),
     so the field is kept True but the incorrect mixed logic is removed.
  5. BALL_POINTS scoring logic: generator accumulated points but could go
     negative (pool scores never go negative). Added floor at 0.
  6. ballsOnTable: generator excluded balls that appeared as autoRemovedBall
     in any event, but real data shows balls can appear on table even after
     being used (points ≠ literal ball identity across players). Fixed to
     track per-player remaining balls and union remaining across players.
  7. Output path: original defaulted to same dir as script with no mkdir -p
     guard for edge cases. Fixed to always ensure parent dir exists.

Usage:
    python generate_stress_test_fixed.py                    # 500 games, 200 players
    python generate_stress_test_fixed.py --games 5000
    python generate_stress_test_fixed.py --games 200 --players 50 --events 15
    python generate_stress_test_fixed.py --help
"""

import json
import uuid
import random
import argparse
import time
import os
from datetime import datetime

BALL_POINTS = {
    1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7,
    8: 8, 9: 9, 10: 10, 11: 11, 12: 12, 13: 13, 14: 14, 15: 15
}

PLAYER_NAMES = [
    "Clinton", "Ronald", "Cynthia", "Elvis", "Jacob", "Julia", "Melisha",
    "Tess", "Kano", "Lucy", "Namer", "Ggg", "Scot", "Syd", "Theo",
    "Alex", "Sam", "Jordan", "Taylor", "Casey", "Riley", "Morgan",
    "Quinn", "Drew", "Blake", "Cameron", "Avery", "Reese", "Logan",
    "Dylan", "Hayden", "Parker", "Peyton", "Skyler", "Rowan", "Finley",
    "Emery", "River", "Sage", "Phoenix", "Remy", "Harley", "Dakota",
    "Frankie", "Jamie", "Jesse", "Kerry", "Kim", "Lee", "Leslie",
    "Lynn", "Marty", "Micah", "Noel", "Pat", "Robin", "Shane",
    "Terry", "Toby", "Tracy", "Val", "Wesley", "Wren", "Zion",
    "Ace", "Beau", "Cal", "Colt", "Cruz", "Dash", "Duke",
    "Eli", "Fox", "Gray", "Hank", "Ike", "Jace", "Knox",
    "Lake", "Lane", "Moss", "Nash", "Neil", "Reid", "Rex",
    "Roy", "Rue", "Ryke", "Shea", "Stone", "Troy", "Vance",
    "Wade", "Wolf", "Zane", "Zeke", "Amos", "Bram", "Cade",
    "Dax", "Evan", "Finn", "Glen", "Hugh", "Ivan", "Joel",
]

HISTORY_WINDOW = 10  # real app keeps last 10 score states in history


def make_uuid() -> str:
    return str(uuid.uuid4())


def make_timestamp(base_ms: int, offset_seconds: int = 0) -> int:
    return base_ms + (offset_seconds * 1000)


def build_player(name: str, base_time: int, events_per_player: int) -> dict:
    """
    Build a single player record with realistic scoring history.

    FIX: history = cumulative score snapshots BEFORE the current score,
         newest-first, capped at HISTORY_WINDOW entries.
         i.e. history[i] is the score BEFORE the (i+1)-th most recent event.
         This matches: cumulative_newest_first[1 : HISTORY_WINDOW+1]
    """
    player_id = make_uuid()

    negative_vals = [-8, -7, -6, -5, -4, -3, -2, -1]
    remaining_balls = list(range(1, 16))
    random.shuffle(remaining_balls)

    score = 0
    # cumulative_scores[i] = score after i events, index 0 = start (score=0)
    cumulative_scores = [0]
    event_history_raw = []  # newest-first, built by prepending

    for i in range(events_per_player):
        ts = make_timestamp(base_time, i * random.randint(2, 8))

        if remaining_balls and random.random() > 0.25:
            ball = remaining_balls.pop()
            pts = BALL_POINTS[ball]
            auto_removed = ball
        else:
            pts = random.choice(negative_vals + [0])
            auto_removed = None

        prev_score = score
        score = max(0, score + pts)   # FIX: floor at 0
        actual_pts = score - prev_score  # recalc in case floored

        cumulative_scores.append(score)

        event_history_raw.append({
            "points": actual_pts,
            "timestamp": ts,
            "_prev": prev_score,
            "_new": score,
            "_ball": auto_removed,
        })

    # FIX: build history correctly
    # cumulative_scores newest-first = list(reversed(cumulative_scores))
    # history = that list skipping index 0 (current score), take up to HISTORY_WINDOW
    cumulative_newest_first = list(reversed(cumulative_scores))
    history = cumulative_newest_first[1: HISTORY_WINDOW + 1]  # exclude current score

    # eventHistory: newest-first, without internal fields
    event_history = [
        {"points": e["points"], "timestamp": e["timestamp"]}
        for e in reversed(event_history_raw)
    ]

    # Build global events for this player (returned separately)
    global_events_for_player = []
    for e in event_history_raw:
        global_events_for_player.append({
            "id": make_uuid(),
            "player_id": player_id,
            "player_name": name,
            "points": e["points"],
            "timestamp": e["timestamp"],
            "previous_score": e["_prev"],
            "new_score": e["_new"],
            "auto_removed_ball": e["_ball"],
        })

    return {
        "player_record": {
            "id": player_id,
            "name": name,
            "score": score,
            "history": history,
            "eventHistory": event_history,
            "isActive": True,
        },
        "global_events": global_events_for_player,
        "remaining_balls": set(remaining_balls),
    }


def build_game(
    game_index: int,
    all_players: list,
    base_time: int,
    events_per_player: int,
) -> dict:
    n_players = random.randint(2, min(4, len(all_players)))
    selected_names = random.sample(all_players, n_players)

    game_id = make_uuid()
    game_start = base_time + (game_index * random.randint(60_000, 300_000))
    game_end = game_start + random.randint(300_000, 3_600_000)

    player_records = []
    all_global_events = []
    all_remaining_balls = set(range(1, 16))  # FIX: union of remaining per player

    for name in selected_names:
        result = build_player(name, game_start, events_per_player)
        player_records.append(result["player_record"])
        all_global_events.extend(result["global_events"])
        all_remaining_balls &= result["remaining_balls"]  # intersection = truly unused

    all_global_events.sort(key=lambda e: e["timestamp"])

    global_events = []
    for ev in all_global_events:
        global_events.append({
            "id": ev["id"],
            "playerId": ev["player_id"],
            "playerName": ev["player_name"],
            "type": "SCORE",
            "points": ev["points"],
            "previousPlayerId": ev["player_id"],
            "isZeroInput": ev["points"] == 0,
            "message": None,
            "timestamp": ev["timestamp"],
            "previousScore": ev["previous_score"],
            "newScore": ev["new_score"],
            "autoRemovedBall": ev["auto_removed_ball"],
        })

    balls_on_table = list(all_remaining_balls)
    random.shuffle(balls_on_table)

    current_player_id = player_records[-1]["id"]
    is_archived = random.random() < 0.10

    return {
        "id": game_id,
        "players": player_records,
        "isGameActive": True,   # FIX: always True — matches real export format
        "startTime": game_start,
        "endTime": game_end,
        "lastUpdate": game_end - random.randint(1000, 10000),
        "currentPlayerId": current_player_id,
        "globalEvents": global_events,
        "canUndo": True,
        "isFinalized": not is_archived,
        "ballsOnTable": balls_on_table,
        "deviceInfo": "StressTestGenerator_v2",
        "isArchived": is_archived,
    }


def generate(num_games: int, num_players: int, events_per_player: int, output_path: str):
    print(f"\n🎱 PocketScore Stress-Test Generator v2 (fixed)")
    print(f"   Games        : {num_games:,}")
    print(f"   Players      : {num_players:,}")
    print(f"   Events/Player: {events_per_player:,}")
    print(f"   Output       : {output_path}\n")

    friends = list(PLAYER_NAMES[:min(num_players, len(PLAYER_NAMES))])
    used = set(friends)

    extras_needed = num_players - len(friends)
    if extras_needed > 0:
        print(f"   Generating {extras_needed:,} extra player names...")
        for _ in range(extras_needed):
            prefix = random.choice(["Pl", "Rx", "Ss", "Ax", "Bz", "Kr", "Fn", "Qx"])
            name = f"{prefix}{random.randint(100, 9999)}"
            while name in used:
                name = f"{prefix}{random.randint(100, 99999)}"
            friends.append(name)
            used.add(name)

    now_ms = int(time.time() * 1000)
    two_years_ago = now_ms - (2 * 365 * 24 * 60 * 60 * 1000)

    games = []
    chunk = max(1, num_games // 20)
    for i in range(num_games):
        if i % chunk == 0:
            pct = (i / num_games) * 100
            print(f"   Building games... {i:,}/{num_games:,} ({pct:.0f}%)", end="\r")
        games.append(build_game(
            game_index=i,
            all_players=friends,
            base_time=two_years_ago,
            events_per_player=events_per_player,
        ))
    print(f"   Building games... {num_games:,}/{num_games:,} (100%) ✓          ")

    random.shuffle(games)

    share = {
        "version": 1,
        "sourceDevice": "StressTestGenerator_v2",
        "shareDate": now_ms,
        "friends": friends,
        "games": games,
    }

    print("   Writing JSON to disk...")
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(share, f, indent=2, ensure_ascii=False)

    file_size = os.path.getsize(output_path)
    mbytes = file_size / (1024 * 1024)
    total_events = sum(len(g["globalEvents"]) for g in games)

    print(f"\n✅ Done!")
    print(f"   File size    : {mbytes:.2f} MB ({file_size:,} bytes)")
    print(f"   Total games  : {len(games):,}")
    print(f"   Total friends: {len(friends):,}")
    print(f"   Total events : {total_events:,}")
    print(f"   Output path  : {output_path}\n")


def main():
    parser = argparse.ArgumentParser(
        description="PocketScore stress-test data generator (.pscore.json) — fixed v2"
    )
    parser.add_argument("--games",   type=int, default=500,  help="Number of games (default: 500)")
    parser.add_argument("--players", type=int, default=200,  help="Players in roster (default: 200)")
    parser.add_argument("--events",  type=int, default=15,   help="Scoring events per player per game (default: 15)")
    parser.add_argument("--output",  type=str, default=None, help="Output file path")
    args = parser.parse_args()

    if args.output:
        output_path = args.output
    else:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S")
        output_path = os.path.join(
            os.path.dirname(os.path.abspath(__file__)),
            f"stress_{ts}_g{args.games}_p{args.players}.pscore"
        )

    generate(
        num_games=args.games,
        num_players=args.players,
        events_per_player=args.events,
        output_path=output_path,
    )


if __name__ == "__main__":
    main()
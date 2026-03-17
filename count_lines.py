#!/usr/bin/env python3
"""
Kotlin Codebase Line Counter
Counts lines in app/src/main/java for a Kotlin/Jetpack Compose project.
"""

import os
import sys
from pathlib import Path


def count_lines(file_path: Path) -> dict:
    total = 0
    code = 0
    blank = 0
    comments = 0
    in_block_comment = False

    with open(file_path, encoding="utf-8", errors="ignore") as f:
        for line in f:
            total += 1
            stripped = line.strip()

            if not stripped:
                blank += 1
                continue

            if in_block_comment:
                comments += 1
                if "*/" in stripped:
                    in_block_comment = False
                continue

            if stripped.startswith("/*") or stripped.startswith("/**"):
                comments += 1
                if "*/" not in stripped[2:]:
                    in_block_comment = True
                continue

            if stripped.startswith("//"):
                comments += 1
                continue

            code += 1

    return {"total": total, "code": code, "blank": blank, "comments": comments}


def scan_codebase(root: Path) -> None:
    ext = ".kt"
    file_stats = []
    totals = {"total": 0, "code": 0, "blank": 0, "comments": 0}

    kt_files = sorted(root.rglob(f"*{ext}"))

    if not kt_files:
        print(f"No {ext} files found under: {root}")
        sys.exit(1)

    for f in kt_files:
        stats = count_lines(f)
        rel = f.relative_to(root)
        file_stats.append((rel, stats))
        for k in totals:
            totals[k] += stats[k]

    # --- per-file table ---
    col_w = max(len(str(r)) for r, _ in file_stats)
    header = f"{'File':<{col_w}}  {'Total':>7}  {'Code':>7}  {'Blank':>7}  {'Comments':>9}"
    sep = "-" * len(header)

    print(f"\n📁  Root: {root}\n")
    print(header)
    print(sep)
    for rel, s in file_stats:
        print(f"{str(rel):<{col_w}}  {s['total']:>7}  {s['code']:>7}  {s['blank']:>7}  {s['comments']:>9}")

    print(sep)
    print(f"{'TOTAL  (' + str(len(kt_files)) + ' files)':<{col_w}}  {totals['total']:>7}  {totals['code']:>7}  {totals['blank']:>7}  {totals['comments']:>9}")

    # --- summary block ---
    code_pct   = totals["code"]     / totals["total"] * 100 if totals["total"] else 0
    blank_pct  = totals["blank"]    / totals["total"] * 100 if totals["total"] else 0
    cmt_pct    = totals["comments"] / totals["total"] * 100 if totals["total"] else 0

    print(f"""
┌─────────────────────────────┐
│        SUMMARY              │
├─────────────────────────────┤
│  .kt files    : {len(kt_files):>6}       │
│  Total lines  : {totals['total']:>6}       │
│  Code lines   : {totals['code']:>6}  ({code_pct:4.1f}%) │
│  Blank lines  : {totals['blank']:>6}  ({blank_pct:4.1f}%) │
│  Comments     : {totals['comments']:>6}  ({cmt_pct:4.1f}%) │
└─────────────────────────────┘""")


def main():
    # Default path — adjust if your project root differs
    default_subpath = os.path.join("app", "src", "main", "java")

    if len(sys.argv) > 1:
        root = Path(sys.argv[1])
    else:
        # Try to find the project root automatically
        cwd = Path.cwd()
        candidate = cwd / default_subpath
        if candidate.exists():
            root = candidate
        else:
            # Walk up to find app/src/main/java
            for parent in cwd.parents:
                candidate = parent / default_subpath
                if candidate.exists():
                    root = candidate
                    break
            else:
                print(f"Could not auto-detect project root.")
                print(f"Usage: python count_lines.py [path/to/app/src/main/java]")
                sys.exit(1)

    if not root.exists():
        print(f"Path does not exist: {root}")
        sys.exit(1)

    scan_codebase(root)


if __name__ == "__main__":
    main()

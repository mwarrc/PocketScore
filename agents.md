# PocketScore Coding Guidelines

> **"Code cleanliness and modularity are paramount."**

## Core Principles
1. **Modularity**: Files should be focused and small. Avoid "God classes" or massive files.
   - Extract logic into testable helper functions or use classes.
   - Extract UI components into separate files if they are distinct or reusable.
   - Use dedicated packages for feature sub-components (e.g., `history/analysis/`).

2. **Clean Code**:
   - Variables and functions must be **well-labeled** and descriptive.
   - No magic numbers.
   - Comments should explain *why*, not *what* (unless complex).
   - formatting must be consistent (Kotlin guidelines).

3. **Open Source Standard**:
   - Code should be readable by anyone.
   - Architecture should be obvious from the folder structure.
   - Dependencies between modules should be clear.

## Session Rules
- **ALWAYS** check for opportunities to refactor when touching a file.
- **NEVER** leave a file significantly larger than it needs to be (>300-400 lines is a red flag).
- **VERIFY** imports after refactoring.

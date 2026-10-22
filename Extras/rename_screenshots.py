import os

"""
PocketScore Screenshot Renamer
------------------------------
This script renames all JPG/JPEG files in the 'screenshots' directory to 
a standardized format: screen_01.jpg, screen_02.jpg, etc.

This naming convention ensures compatibility with the PocketScore 
showcase website.
"""

def rename_screenshots():
    # Target directory relative to this script's location
    base_dir = os.path.dirname(os.path.abspath(__file__))
    target_dir = os.path.join(base_dir, 'screenshots')
    
    if not os.path.exists(target_dir):
        print(f"Error: Target directory '{target_dir}' does not exist.")
        return

    # supported extensions
    valid_exts = ('.jpg', '.jpeg', '.JPG', '.JPEG')
    
    # Get all matching files
    files = [f for f in os.listdir(target_dir) if f.endswith(valid_exts)]
    
    # Sort alphabetically to maintain existing sequence
    files.sort()

    if not files:
        print("No matching screenshots found in 'screenshots/' folder.")
        return

    print(f"Processing {len(files)} files in: {target_dir}")

    # Step 1: Rename everything to a temporary unique name to prevent collisions
    # This is necessary if files like 'screen_02.jpg' already exist but should be 'screen_03.jpg'
    temp_records = []
    for i, filename in enumerate(files, 1):
        old_path = os.path.join(target_dir, filename)
        temp_filename = f"re_naming_tmp_{i}.tmp"
        temp_path = os.path.join(target_dir, temp_filename)
        
        try:
            os.rename(old_path, temp_path)
            temp_records.append((temp_path, i))
        except OSError as e:
            print(f"Failed to process {filename}: {e}")

    # Step 2: Set final names: screen_01.jpg, screen_02.jpg ...
    for temp_path, index in temp_records:
        final_filename = f"screen_{index:02d}.jpg"
        final_path = os.path.join(target_dir, final_filename)
        
        try:
            os.rename(temp_path, final_path)
            print(f"Renamed -> {final_filename}")
        except OSError as e:
            print(f"Error finalized {final_filename}: {e}")

    print("\nBatch rename complete.")

if __name__ == "__main__":
    rename_screenshots()

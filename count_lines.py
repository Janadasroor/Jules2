import os

def count_lines(root_dir):
    # Extensions to include
    extensions = {'.kt', '.java', '.xml', '.gradle', '.kts', '.py', '.properties', '.pro'}
    # Directories to exclude
    exclude_dirs = {'.gradle', '.idea', 'build', 'gradle'}
    
    stats = {}
    total_lines = 0

    for root, dirs, files in os.walk(root_dir):
        # Filter out excluded directories
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            ext = os.path.splitext(file)[1]
            if ext in extensions:
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                        lines = sum(1 for _ in f)
                        stats[ext] = stats.get(ext, 0) + lines
                        total_lines += lines
                except Exception as e:
                    print(f"Error reading {file_path}: {e}")

    return stats, total_lines

if __name__ == "__main__":
    project_root = "."
    stats, total = count_lines(project_root)
    
    print(f"{'Extension':<12} | {'Lines':<10}")
    print("-" * 25)
    for ext, count in sorted(stats.items(), key=lambda x: x[1], reverse=True):
        print(f"{ext:<12} | {count:<10}")
    print("-" * 25)
    print(f"{'Total':<12} | {total:<10}")

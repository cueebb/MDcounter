import re

replacements = {
    r'"Switch Folders \(Hold to Edit\)"': r'TranslationManager.getString("switch_folders_hint", "Switch Folders (Hold to Edit)")',
    r'"Folder Info"': r'TranslationManager.getString("folder_info", "Folder Info")',
    r'"Log values, manage folders, and trace board game or task progress\."': r'TranslationManager.getString("folder_info_desc", "Log values, manage folders, and trace board game or task progress.")',
    r'"No counters found"': r'TranslationManager.getString("no_counters_found", "No counters found")',
    r'"Goal Progress: \$\{([^}]+)\}%"': r'TranslationManager.getString("goal_progress", "Goal Progress: %d%%").format(\1)',
    r'"Quick Adjust: \$\{counter\.name\}"': r'TranslationManager.getString("quick_adjust_title", "Quick Adjust: %s").format(counter.name)',
    r'"Please enter a valid positive number"': r'TranslationManager.getString("invalid_positive_number", "Please enter a valid positive number")',
    r'"Custom Color Picker"': r'TranslationManager.getString("custom_color_picker", "Custom Color Picker")',
    r'"Material 3 Palette Colors"': r'TranslationManager.getString("material_3_palette", "Material 3 Palette Colors")',
    r'"Folder name cannot be empty"': r'TranslationManager.getString("empty_folder_name", "Folder name cannot be empty")',
    r'"Select Folder Icon"': r'TranslationManager.getString("select_folder_icon", "Select Folder Icon")',
    r'"Smart Folder Settings"': r'TranslationManager.getString("smart_folder_settings", "Smart Folder Settings")',
    r'"Auto-fill counter values & bulk edit matching settings"': r'TranslationManager.getString("smart_folder_settings_desc", "Auto-fill counter values & bulk edit matching settings")',
    r'"Step size must be a positive number"': r'TranslationManager.getString("invalid_step_size", "Step size must be a positive number")',
    r'"Sort Counters"': r'TranslationManager.getString("sort_counters", "Sort Counters")',
    r'"Sort By"': r'TranslationManager.getString("sort_by", "Sort By")',
    r'"Reverse Sorting"': r'TranslationManager.getString("reverse_sorting", "Reverse Sorting")',
    r'"Auto-sort"': r'TranslationManager.getString("auto_sort", "Auto-sort")',
    r'"Keep counters sorted automatically over time"': r'TranslationManager.getString("auto_sort_desc", "Keep counters sorted automatically over time")',
    r'"Interval must be a positive number"': r'TranslationManager.getString("invalid_interval", "Interval must be a positive number")',
    r'"Title is required"': r'TranslationManager.getString("title_required", "Title is required")',
    r'"Select Counter Card Color"': r'TranslationManager.getString("select_counter_color", "Select Counter Card Color")',
    r'"Timeline & Activity Log"': r'TranslationManager.getString("timeline_activity_log", "Timeline & Activity Log")',
    r'"Current: \$\{counter\.currentValue\}"': r'TranslationManager.getString("current_value_label", "Current: %d").format(counter.currentValue)',
    r'"No log changes registered yet\."': r'TranslationManager.getString("no_log_changes", "No log changes registered yet.")',
    r'"Path: \$\{log\.previousValue\} ➔ \$\{log\.newValue\}"': r'TranslationManager.getString("log_path", "Path: %d ➔ %d").format(log.previousValue, log.newValue)',
    r'"Duplicate Counter"': r'TranslationManager.getString("duplicate_counter", "Duplicate Counter")'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


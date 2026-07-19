import re

replacements = {
    r'"All Counters"': r'TranslationManager.getString("all_counters", "All Counters")',
    r'"Counter Tracker"': r'TranslationManager.getString("app_name", "Counter Tracker")',
    r'"History"': r'TranslationManager.getString("history", "History")',
    r'"Folder History"': r'TranslationManager.getString("folder_history", "Folder History")',
    r'"All History Logs"': r'TranslationManager.getString("all_history_logs", "All History Logs")',
    r'"No history records found"': r'TranslationManager.getString("no_history_records_found", "No history records found")',
    r'"Manual Divider"': r'TranslationManager.getString("manual_divider", "Manual Divider")',
    r'"Global Manual Divider"': r'TranslationManager.getString("global_manual_divider", "Global Manual Divider")',
    r'"Inactivity"': r'TranslationManager.getString("inactivity", "Inactivity")',
    r'"Limit"': r'TranslationManager.getString("limit", "Limit")',
    r'"Create Folder"': r'TranslationManager.getString("create_folder", "Create Folder")',
    r'"Edit Folder"': r'TranslationManager.getString("edit_folder", "Edit Folder")',
    r'"Folder Name"': r'TranslationManager.getString("folder_name", "Folder Name")',
    r'"Color"': r'TranslationManager.getString("color", "Color")',
    r'"Icon"': r'TranslationManager.getString("icon", "Icon")',
    r'"Default Step Size"': r'TranslationManager.getString("default_step_size", "Default Step Size")',
    r'"Default Reset Value"': r'TranslationManager.getString("default_reset_value", "Default Reset Value")',
    r'"Default Target Value"': r'TranslationManager.getString("default_target_value", "Default Target Value")',
    r'"Quick Buttons"': r'TranslationManager.getString("quick_buttons", "Quick Buttons")',
    r'"History Divider Interval (seconds)"': r'TranslationManager.getString("history_divider_interval", "History Divider Interval (seconds)")',
    r'"Create Counter"': r'TranslationManager.getString("create_counter", "Create Counter")',
    r'"Edit Counter"': r'TranslationManager.getString("edit_counter", "Edit Counter")',
    r'"Counter Name"': r'TranslationManager.getString("counter_name", "Counter Name")',
    r'"Initial Value"': r'TranslationManager.getString("initial_value", "Initial Value")',
    r'"Current Value"': r'TranslationManager.getString("current_value", "Current Value")',
    r'"Value"': r'TranslationManager.getString("value", "Value")',
    r'"Target"': r'TranslationManager.getString("target", "Target")',
    r'"Reset"': r'TranslationManager.getString("reset", "Reset")',
    r'"Note"': r'TranslationManager.getString("note", "Note")',
    r'"Save"': r'TranslationManager.getString("save", "Save")',
    r'"Cancel"': r'TranslationManager.getString("cancel", "Cancel")',
    r'"Delete"': r'TranslationManager.getString("delete", "Delete")',
    r'"Select Accent Color"': r'TranslationManager.getString("select_accent_color", "Select Accent Color")',
    r'"Duplicate"': r'TranslationManager.getString("duplicate", "Duplicate")',
    r'"Add Local Divider"': r'TranslationManager.getString("add_local_divider", "Add Local Divider")',
    r'"Add Global Divider"': r'TranslationManager.getString("add_global_divider", "Add Global Divider")',
    r'"Search counters..."': r'TranslationManager.getString("search_counters", "Search counters...")',
    r'"General / Uncategorized"': r'TranslationManager.getString("uncategorized", "General / Uncategorized")',
    r'"Step Size"': r'TranslationManager.getString("step_size", "Step Size")',
    r'"Streak"': r'TranslationManager.getString("streak", "Streak")',
    r'"Target Reached!"': r'TranslationManager.getString("target_reached", "Target Reached!")',
    r'"Smart Folder"': r'TranslationManager.getString("smart_folder", "Smart Folder")',
    r'"Smart Folders inherit automatic divider settings"': r'TranslationManager.getString("smart_folder_desc", "Smart Folders inherit automatic divider settings")',
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    # Only replace if not already wrapped in TranslationManager (some might not be, but safe check)
    # Actually just replace globally since I haven't done them yet.
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


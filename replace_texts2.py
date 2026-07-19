import re
import json

replacements = {
    r'"Counters"': r'TranslationManager.getString("counters", "Counters")',
    r'"\$totalCountersCount active"': r'"$totalCountersCount " + TranslationManager.getString("active", "active")',
    r'"Accumulated"': r'TranslationManager.getString("accumulated", "Accumulated")',
    r'"Add Counter"': r'TranslationManager.getString("add_counter", "Add Counter")',
    r'"How many points\?"': r'TranslationManager.getString("how_many_points", "How many points?")',
    r'"Apply"': r'TranslationManager.getString("apply", "Apply")',
    r'"Red"': r'TranslationManager.getString("color_red", "Red")',
    r'"Green"': r'TranslationManager.getString("color_green", "Green")',
    r'"Blue"': r'TranslationManager.getString("color_blue", "Blue")',
    r'"Select"': r'TranslationManager.getString("select", "Select")',
    r'"Custom Picker"': r'TranslationManager.getString("custom_picker", "Custom Picker")',
    r'"Close"': r'TranslationManager.getString("close", "Close")',
    r'"Default Step"': r'TranslationManager.getString("default_step", "Default Step")',
    r'"Default Reset To"': r'TranslationManager.getString("default_reset_to", "Default Reset To")',
    r'"Default Goal Target \(Optional\)"': r'TranslationManager.getString("default_goal_target", "Default Goal Target (Optional)")',
    r'"Default Quick Buttons"': r'TranslationManager.getString("default_quick_buttons", "Default Quick Buttons")',
    r'"e\.g\. -10 -5 \+5 \+10"': r'TranslationManager.getString("quick_buttons_placeholder", "e.g. -10 -5 +5 +10")',
    r'"Only numbers and spaces are allowed\."': r'TranslationManager.getString("numbers_spaces_allowed", "Only numbers and spaces are allowed.")',
    r'"e\.g\. 1\.5 or 10"': r'TranslationManager.getString("divider_placeholder", "e.g. 1.5 or 10")',
    r'"Thin divider added to history if inactive for X seconds\. 0 to disable\."': r'TranslationManager.getString("divider_hint", "Thin divider added to history if inactive for X seconds. 0 to disable.")',
    r'"DELETE"': r'TranslationManager.getString("delete_upper", "DELETE")',
    r'"Auto-sort interval \(seconds\)"': r'TranslationManager.getString("auto_sort_interval", "Auto-sort interval (seconds)")',
    r'"Counter Title"': r'TranslationManager.getString("counter_title", "Counter Title")',
    r'"Folder Category"': r'TranslationManager.getString("folder_category", "Folder Category")',
    r'"Reset To"': r'TranslationManager.getString("reset_to", "Reset To")',
    r'"Current Count"': r'TranslationManager.getString("current_count", "Current Count")',
    r'"Goal Target \(Optional\)"': r'TranslationManager.getString("goal_target", "Goal Target (Optional)")',
    r'"Add Description / Note \(Optional\)"': r'TranslationManager.getString("add_description", "Add Description / Note (Optional)")',
    r'"Enter numbers separated by spaces\. Prefix with - to subtract\."': r'TranslationManager.getString("quick_buttons_hint", "Enter numbers separated by spaces. Prefix with - to subtract.")',
    r'"Thin divider added to history if inactive for X seconds\. Set 0 to disable\."': r'TranslationManager.getString("divider_hint2", "Thin divider added to history if inactive for X seconds. Set 0 to disable.")',
    r'"Delete Counter"': r'TranslationManager.getString("delete_counter", "Delete Counter")',
    r'"Are you sure you want to permanently delete \\"\$name\\"\?"': r'TranslationManager.getString("delete_confirm", "Are you sure you want to permanently delete \\"%s\\"?").format(name)',
    r'"Close Timeline"': r'TranslationManager.getString("close_timeline", "Close Timeline")',
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


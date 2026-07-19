import re

replacements = {
    r'"History Divider Interval \(seconds\)"': r'TranslationManager.getString("history_divider_interval", "History Divider Interval (seconds)")',
    r'"-10 -5 \+5 \+10"': r'TranslationManager.getString("quick_buttons_placeholder", "-10 -5 +5 +10")',
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

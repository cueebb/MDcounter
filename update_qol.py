import re

replacements = {
    r'"Decrease"': r'TranslationManager.getString("decrease", "Decrease")',
    r'"Add"': r'TranslationManager.getString("add", "Add")'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


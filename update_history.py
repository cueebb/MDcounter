import re

replacements = {
    r'"History: \$\{targetCounter\.name\}"': r'TranslationManager.getString("history_counter", "History: %s").format(targetCounter.name)',
    r'"Folder History: \$\{targetFolder\.name\}"': r'TranslationManager.getString("history_folder", "Folder History: %s").format(targetFolder.name)'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


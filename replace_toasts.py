import re

replacements = {
    r'"Local divider added for \$\{targetCounter\.name\}!"': r'TranslationManager.getString("local_divider_added", "Local divider added for %s!").format(targetCounter.name)',
    r'"Local divider added for counters in \$\{targetFolder\.name\}!"': r'TranslationManager.getString("folder_divider_added", "Local divider added for counters in %s!").format(targetFolder.name)',
    r'"No counters in this folder to add divider to!"': r'TranslationManager.getString("no_counters_divider", "No counters in this folder to add divider to!")',
    r'"Local divider added for \$\{firstCounter\.name\}!"': r'TranslationManager.getString("local_divider_added", "Local divider added for %s!").format(firstCounter.name)',
    r'"Global divider added to all counters & folder histories!"': r'TranslationManager.getString("global_divider_added", "Global divider added to all counters & folder histories!")'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

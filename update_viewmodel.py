import re

with open('app/src/main/java/com/example/CounterViewModel.kt', 'r') as f:
    content = f.read()

# Replace "${counter.name} (Copy)" with TranslationManager
content = content.replace('"${counter.name} (Copy)"', 'TranslationManager.getString("copy_postfix", "%s (Copy)").format(counter.name)')

with open('app/src/main/java/com/example/CounterViewModel.kt', 'w') as f:
    f.write(content)

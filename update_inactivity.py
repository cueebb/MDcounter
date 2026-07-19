import re

replacements = {
    r'" Inactivity: \$\{String\.format\("%.1f", timeDiffSec\)\}s \(Limit: \$\{threshold\}s\) "': r'TranslationManager.getString("inactivity_limit_format", " Inactivity: %ss (Limit: %ss) ").format(String.format("%.1f", timeDiffSec), threshold)'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


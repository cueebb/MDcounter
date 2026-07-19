import re

replacements = {
    r'"Quantity / Current Value"': r'TranslationManager.getString("sort_quantity", "Quantity / Current Value")',
    r'"Date of Creation"': r'TranslationManager.getString("sort_created_at", "Date of Creation")',
    r'"Date of Change"': r'TranslationManager.getString("sort_last_modified", "Date of Change")',
    r'"Name"': r'TranslationManager.getString("sort_name", "Name")',
    r'"From least to greatest"': r'TranslationManager.getString("sort_least_to_greatest", "From least to greatest")',
    r'"From greatest to least \(Default\)"': r'TranslationManager.getString("sort_greatest_to_least", "From greatest to least (Default)")'
}

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

for target, replacement in replacements.items():
    content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)


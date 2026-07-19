import re
import json
import os

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

matches = re.findall(r'TranslationManager\.getString\("([^"]+)", "([^"]+)"\)', content)

# Unique dictionary
strings = {}
for k, v in matches:
    strings[k] = v

lang_files = ['en.json', 'es.json', 'de.json', 'ru.json']

for lf in lang_files:
    p = os.path.join('translations', lf)
    if os.path.exists(p):
        with open(p, 'r') as f:
            data = json.load(f)
        
        for k, v in strings.items():
            if k not in data:
                # Add it with English fallback for now
                data[k] = v
                
        with open(p, 'w') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)


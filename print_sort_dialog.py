import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

match = re.search(r'fun SortDialog.*?^}', content, re.MULTILINE | re.DOTALL)
if match:
    print(match.group(0))
else:
    print("Not found")

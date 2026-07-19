import shutil
import os

source = 'translations'
destination = 'app/src/main/assets'

if not os.path.exists(destination):
    os.makedirs(destination)

for filename in os.listdir(source):
    if filename.endswith(".json"):
        shutil.copy(os.path.join(source, filename), os.path.join(destination, filename))
        print(f"Copied {filename}")

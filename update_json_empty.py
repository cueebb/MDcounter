
import json
import os

keys = {
    "folder_empty_hint": {
        "en": "Add a new counter to this folder to get started.",
        "ru": "Добавьте новый счетчик в эту папку, чтобы начать.",
        "es": "Añade un nuevo contador a esta carpeta para empezar.",
        "de": "Fügen Sie einen neuen Zähler zu diesem Ordner hinzu, um zu beginnen."
    },
    "global_empty_hint": {
        "en": "Create customizable counters with step goals, colors, and notes.",
        "ru": "Создавайте настраиваемые счетчики с целями шагов, цветами и заметками.",
        "es": "Crea contadores personalizables con objetivos de paso, colores y notas.",
        "de": "Erstellen Sie anpassbare Zähler mit Schrittzielen, Farben und Notizen."
    }
}

assets_dir = 'app/src/main/assets'
for lang in ['en', 'ru', 'es', 'de']:
    file_path = os.path.join(assets_dir, f'{lang}.json')
    if os.path.exists(file_path):
        with open(file_path, 'r') as f:
            data = json.load(f)
        
        for key, translations in keys.items():
            data[key] = translations[lang]
        
        with open(file_path, 'w') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"Updated {file_path}")

import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

pattern = r'(<activity\s+android:name="\.MainActivity")'
replacement = r'\1\n            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"'

content = re.sub(pattern, replacement, content)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)


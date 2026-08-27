import re
with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# find what comes after bottomBar = { ... }
start_idx = content.find("} paddingValues ->")
if start_idx == -1:
    start_idx = content.find("} {")
if start_idx == -1:
    print("Could not find scaffold content start")
else:
    print(content[start_idx-20:start_idx+500])

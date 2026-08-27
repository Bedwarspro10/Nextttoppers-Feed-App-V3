with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("            }\n    ) { innerPadding ->", "            }\n        }\n    ) { innerPadding ->")

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

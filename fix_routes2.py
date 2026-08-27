import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Fix Profile route
old_profile = r"appContainer\.authRepository\.signOut\(\)"
new_profile = "appContainer.firebaseAuth.signOut()"

content = re.sub(old_profile, new_profile, content)

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

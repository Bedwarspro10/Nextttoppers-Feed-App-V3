with open("app/src/main/java/com/example/core/designsystem/HyperOsMotion.kt", "r") as f:
    content = f.read()

content = content.replace("animationSpec = openSpringSpec", "animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow)")
content = content.replace("animationSpec = closeSpringSpec", "animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMedium)")

with open("app/src/main/java/com/example/core/designsystem/HyperOsMotion.kt", "w") as f:
    f.write(content)

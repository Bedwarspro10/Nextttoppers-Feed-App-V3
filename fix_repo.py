with open("app/src/main/java/com/example/data/repositories/UserRepository.kt", "r") as f:
    lines = f.readlines()
out = []
seen = set()
for line in lines:
    if line.startswith("import "):
        if line not in seen:
            seen.add(line)
            out.append(line)
    else:
        out.append(line)
with open("app/src/main/java/com/example/data/repositories/UserRepository.kt", "w") as f:
    f.writelines(out)

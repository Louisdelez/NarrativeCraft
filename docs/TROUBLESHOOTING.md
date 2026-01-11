# NarrativeCraft Troubleshooting Guide

**Version**: 1.1.0
**Last Updated**: 2026-01-09

## Table of Contents

- [Build Requirements](#build-requirements)
- [Common Build Errors](#common-build-errors)
- [Tag Errors](#tag-errors)
- [Runtime Issues](#runtime-issues)
- [State Recovery](#state-recovery)
- [Getting Help](#getting-help)

---

## Build Requirements

### Java 21 Required

NarrativeCraft requires **Java 21** or newer to build. This is a hard requirement from Fabric Loom 1.14+ and NeoForge ModDevGradle.

#### Verify Your Java Version

```bash
java -version
```

Expected output should show version 21 or higher:
```
openjdk version "21.0.x" ...
```

#### Installing Java 21

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

**Fedora:**
```bash
sudo dnf install java-21-openjdk-devel
```

**macOS (Homebrew):**
```bash
brew install openjdk@21
```

**Windows (Scoop):**
```powershell
scoop install temurin21-jdk
```

**SDKMAN (Recommended for developers):**
```bash
# Install SDKMAN if not present
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21.0.5-tem
sdk use java 21.0.5-tem
```

#### Setting JAVA_HOME

After installation, ensure `JAVA_HOME` points to Java 21:

```bash
export JAVA_HOME=/path/to/java-21
export PATH="$JAVA_HOME/bin:$PATH"
```

Add these lines to your `~/.bashrc` or `~/.zshrc` for persistence.

### Build Commands

Once Java 21 is configured, build with:

```bash
./gradlew :fabric:build :neoforge:build
```

Run tests:
```bash
./gradlew test
```

---

## Common Build Errors

### "Dependency requires at least JVM runtime version 21"

**Cause:** Gradle is running with Java 17 or older.

**Solution:**
1. Install Java 21 (see above)
2. Set `JAVA_HOME` to Java 21
3. Verify with `java -version`
4. Run the build again

### Gradle Daemon Issues

If you see unexpected behavior, try clearing the Gradle cache:

```bash
./gradlew --stop
rm -rf ~/.gradle/caches
./gradlew :fabric:build :neoforge:build --no-daemon
```

### OutOfMemoryError During Build

Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4G
```

---

## Tag Errors

### Understanding Error Messages

NarrativeCraft provides detailed error messages with four components:

```
[WHAT] Unknown tag
  Error: 'fde' tag is invalid
[WHERE] Story: intro / Scene: start / Line: 15
  Command: fde 1.0 2.0 1.0
[WHY] The tag 'fde' is not a recognized NarrativeCraft tag
[FIX] Did you mean 'fade'?
```

### "Unknown tag: xxx"

**Problem:** NarrativeCraft doesn't recognize your tag.

**Possible Causes:**
1. **Typo**: Check spelling (e.g., `fad` should be `fade`)
2. **Wrong format**: Tags use `#` prefix, not `//`
3. **Case**: Tags are case-insensitive, but be consistent

**Solution:**
```ink
// Wrong
# fad 1.0 2.0 1.0

// Correct
# fade 1.0 2.0 1.0
```

### "Missing required argument"

**Problem:** Tag is missing a required parameter.

**Solution:** Check [TAG_REFERENCE.md](TAG_REFERENCE.md) for required parameters:
```ink
// Wrong - missing stay and fadeOut
# fade 1.0

// Correct
# fade 1.0 2.0 1.0
```

### "Invalid argument type"

**Problem:** Argument has wrong type (e.g., text instead of number).

**Solution:**
```ink
// Wrong - "two" is not a number
# fade 1.0 two 1.0

// Correct
# fade 1.0 2.0 1.0
```

### "Invalid argument value"

**Problem:** Value is out of acceptable range.

**Solution:**
```ink
// Wrong - negative time
# fade -1.0 2.0 1.0

// Wrong - opacity > 1.0
# border 10 10 10 10 000000 1.5

// Correct
# fade 1.0 2.0 1.0
# border 10 10 10 10 000000 0.8
```

### "Command blocked for security"

**Problem:** Attempted to use a blocked Minecraft command.

**Solution:** Use only whitelisted commands. See [TAG_REFERENCE.md](TAG_REFERENCE.md#command).

```ink
// Blocked
# command "op @p"

// Allowed
# command "effect give @p speed 60 1"
```

---

## Runtime Issues

### Mod Not Loading

1. Check Minecraft version (1.21.11+)
2. Verify mod loader version:
   - Fabric Loader: 0.18.3+
   - NeoForge: 21.11.12+
3. Check logs: `.minecraft/logs/latest.log`

### Story Crashed During Execution

1. Check the crash screen for details
2. Look at the last executed tag
3. Verify all resources exist
4. Report if it's a mod bug

### Performance Issues

If experiencing lag:

1. Check `F3` debug for TPS
2. Reduce particle effects in commands
3. Limit concurrent sounds
4. Stop unused animations

---

## State Recovery

NarrativeCraft 1.1.0 guarantees state recovery in all scenarios:

### Automatic Cleanup

The following are automatically restored on scene exit:

| Component | Recovery |
|-----------|----------|
| HUD | `hideGui` reset to normal |
| Camera | Returned to player control |
| Input | All keys unblocked |
| Audio | All narrative sounds stopped |

### Recovery Scenarios

**Dialogue Exit:**
- Normal exit or abort
- All state guaranteed restored

**Cutscene Abort:**
- Player regains camera control
- HUD and input restored

**Player Disconnect:**
- Server-side cleanup
- Fresh state on reconnect

**Exception/Crash:**
- try/finally ensures cleanup
- Error details shown to player

### Manual Reset

If state seems stuck:

1. **Exit and re-enter world** - Full reset
2. **Use reset command** (if available):
   ```
   /narrativecraft reset
   ```
3. **Check F3 debug** - Verify game state

---

## Getting Help

- **GitHub Issues**: https://github.com/LOUDO56/NarrativeCraft/issues
- **Documentation**: See `docs/` folder

When reporting issues, include:
- NarrativeCraft version
- Minecraft version
- Mod loader (Fabric/NeoForge) and version
- Full error message
- Steps to reproduce
- Relevant Ink script excerpt

---

## See Also

- [INK_GUIDE.md](INK_GUIDE.md) - Scripting tutorial
- [TAG_REFERENCE.md](TAG_REFERENCE.md) - Complete tag documentation

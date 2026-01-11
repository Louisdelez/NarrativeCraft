# Quickstart: Multi-Version NarrativeCraft Development

**Feature**: 002-multi-version-support
**Date**: 2026-01-09

## Prerequisites

- **Java 21** (required for compilation; targets Java 17/21 at runtime)
- **IntelliJ IDEA** or **VS Code** with Java extensions
- **Git** for version control
- **~4GB RAM** for Gradle builds

## Quick Setup

### 1. Clone and Initialize

```bash
# Clone the repository
git clone https://github.com/your-username/NarrativeCraft.git
cd NarrativeCraft

# Checkout the multi-version branch
git checkout 002-multi-version-support

# Initialize Gradle wrapper
./gradlew --version
```

### 2. Build All Targets

```bash
# Build all 5 JARs at once
./gradlew build

# Output JARs will be in:
# - fabric-1.19.4/build/libs/narrativecraft-*-mc1.19.4-fabric.jar
# - fabric-1.20.6/build/libs/narrativecraft-*-mc1.20.6-fabric.jar
# - fabric-1.21.11/build/libs/narrativecraft-*-mc1.21.11-fabric.jar
# - neoforge-1.20.6/build/libs/narrativecraft-*-mc1.20.6-neoforge.jar
# - neoforge-1.21.11/build/libs/narrativecraft-*-mc1.21.11-neoforge.jar
```

### 3. Build Specific Targets

```bash
# Build only Fabric 1.21.11
./gradlew :fabric-1.21.11:build

# Build only NeoForge 1.20.6
./gradlew :neoforge-1.20.6:build

# Build only common module
./gradlew :common:build
```

## Project Structure

```
NarrativeCraft/
├── common/                    # Version-independent code
│   └── src/main/java/
│       └── fr/loudo/narrativecraft/
│           ├── api/           # InkAction system
│           ├── narrative/     # Core story system
│           ├── platform/      # ServiceLoader abstraction
│           └── compat/api/    # Compatibility interfaces
│
├── compat/                    # Version-specific implementations
│   ├── mc119x/               # Minecraft 1.19.x adaptations
│   ├── mc120x/               # Minecraft 1.20.x adaptations
│   └── mc121x/               # Minecraft 1.21.x adaptations
│
├── fabric-1.19.4/            # Fabric 1.19.4 loader module
├── fabric-1.20.6/            # Fabric 1.20.6 loader module
├── fabric-1.21.11/           # Fabric 1.21.11 loader module
├── neoforge-1.20.6/          # NeoForge 1.20.6 loader module
├── neoforge-1.21.11/         # NeoForge 1.21.11 loader module
│
├── buildSrc/                  # Gradle plugins
│   ├── multiloader-common.gradle
│   ├── multiloader-loader.gradle
│   └── version-matrix.gradle
│
└── gradle.properties          # Version matrix configuration
```

## Development Workflow

### Adding Common Code

Code that works on all Minecraft versions goes in `common/`:

```java
// common/src/main/java/fr/loudo/narrativecraft/narrative/MyFeature.java
package fr.loudo.narrativecraft.narrative;

public class MyFeature {
    // This code is shared across all versions
}
```

### Adding Version-Specific Code

Code that differs between versions goes in `compat/mc1XXx/`:

```java
// compat/mc121x/src/main/java/fr/loudo/narrativecraft/compat/mc121x/Mc121xGuiRenderCompat.java
package fr.loudo.narrativecraft.compat.mc121x;

import fr.loudo.narrativecraft.compat.api.IGuiRenderCompat;
import net.minecraft.client.gui.GuiGraphics;

public class Mc121xGuiRenderCompat implements IGuiRenderCompat {
    @Override
    public void fill(Object graphics, int x1, int y1, int x2, int y2, int color) {
        ((GuiGraphics) graphics).fill(x1, y1, x2, y2, color);
    }
    // ... other methods
}
```

### Using Compatibility Layer

```java
// In common code, use the compatibility interfaces
import fr.loudo.narrativecraft.compat.api.IVersionAdapter;
import fr.loudo.narrativecraft.platform.Services;

public class MyRenderer {
    private final IGuiRenderCompat guiCompat;

    public MyRenderer() {
        IVersionAdapter adapter = Services.load(IVersionAdapter.class);
        this.guiCompat = adapter.getGuiRenderCompat();
    }

    public void render(Object graphics, int x, int y) {
        guiCompat.fill(graphics, x, y, x + 100, y + 50, 0xFF000000);
    }
}
```

### Feature Gating

```java
import fr.loudo.narrativecraft.compat.api.ICapabilityChecker;
import fr.loudo.narrativecraft.platform.Services;

public class AdvancedHudRenderer {
    private final ICapabilityChecker capabilities;

    public AdvancedHudRenderer() {
        this.capabilities = Services.load(ICapabilityChecker.class);
    }

    public void render(Object graphics) {
        if (capabilities.isFeatureAvailable(ICapabilityChecker.FEATURE_ADVANCED_HUD)) {
            renderAdvancedHud(graphics);
        } else {
            capabilities.warnIfUnavailable(
                ICapabilityChecker.FEATURE_ADVANCED_HUD,
                "AdvancedHudRenderer"
            );
            renderBasicHud(graphics);
        }
    }
}
```

## Testing

### Run Unit Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :common:test
```

### Run Minecraft Client (for manual testing)

```bash
# Run Fabric 1.21.11 client
./gradlew :fabric-1.21.11:runClient

# Run NeoForge 1.20.6 client
./gradlew :neoforge-1.20.6:runClient

# Run Fabric 1.19.4 client
./gradlew :fabric-1.19.4:runClient
```

### Run Minecraft Server

```bash
# Run Fabric 1.21.11 server
./gradlew :fabric-1.21.11:runServer
```

## Adding a New Mixin

### Common Mixin (all versions)

1. Add to `common/src/main/java/fr/loudo/narrativecraft/mixin/`
2. Register in `common/src/main/resources/narrativecraft.mixins.json`

### Version-Specific Mixin

1. Add to appropriate module (e.g., `fabric-1.21.11/src/main/java/.../mixin/`)
2. Register in version-specific mixin config (e.g., `narrativecraft.mc121.mixins.json`)

## Common Tasks

### Check Code Formatting

```bash
./gradlew spotlessCheck
```

### Fix Code Formatting

```bash
./gradlew spotlessApply
```

### Clean Build

```bash
./gradlew clean build
```

### Generate IDE Files

```bash
# IntelliJ IDEA
./gradlew idea

# Eclipse
./gradlew eclipse
```

## Troubleshooting

### "Cannot resolve symbol" in IDE

Run `./gradlew genSources` then refresh Gradle in IDE.

### Mixin not applying

1. Check mixin is registered in correct `.mixins.json`
2. Verify target class exists in target Minecraft version
3. Check refmap is generated correctly

### Build fails with version mismatch

Ensure `gradle.properties` has correct versions for target.

### Java version error

```bash
# Check Java version
java -version

# Should show: openjdk version "21.x.x"
# If not, install Java 21 and set JAVA_HOME
```

## Version Matrix Reference

| Target | Minecraft | Loader | Java | Fabric API | NeoForge |
|--------|-----------|--------|------|------------|----------|
| fabric-1.19.4 | 1.19.4 | Fabric 0.14.24 | 17 | 0.87.2+1.19.4 | N/A |
| fabric-1.20.6 | 1.20.6 | Fabric 0.15.11 | 17 | 0.97.8+1.20.5 | N/A |
| neoforge-1.20.6 | 1.20.6 | NeoForge 20.6.119 | 17 | N/A | 20.6.119 |
| fabric-1.21.11 | 1.21.11 | Fabric 0.18.3 | 21 | 0.140.0+1.21.11 | N/A |
| neoforge-1.21.11 | 1.21.11 | NeoForge 21.11.12 | 21 | N/A | 21.11.12-beta |

## Resources

- [Architectury Documentation](https://docs.architectury.dev/)
- [Fabric Wiki](https://wiki.fabricmc.net/)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki)

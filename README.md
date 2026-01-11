# NarrativeCraft

[![CI](https://github.com/LOUDO56/NarrativeCraft/actions/workflows/ci.yml/badge.svg)](https://github.com/LOUDO56/NarrativeCraft/actions/workflows/ci.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.19.4%20%7C%201.20.6%20%7C%201.21.11-green)](https://github.com/LOUDO56/NarrativeCraft/releases)
[![Languages](https://img.shields.io/badge/Languages-7-blue)](docs/I18N_RULES.md)

<div align="center">
    Create your own stories, easily and freely, in Minecraft.
</div>
&nbsp;&nbsp;&nbsp;
<p align="center">
  <a href="https://discord.com/invite/E3zzNv79DN">
    <img width="250" height="150" alt="join_disord" src="https://github.com/user-attachments/assets/075953b6-de64-4f55-a9c8-fb407e50458b" />
  </a>
   &nbsp;&nbsp;&nbsp;
  <a href="https://loudo56.github.io/NarrativeCraft-docs/">
    <img width="250" height="150" alt="documentation" src="https://github.com/user-attachments/assets/7eaaca5f-52ef-4a5f-bb2d-9320ffc247b5" />
  </a>
</p>

## Tutorial video
<a href="https://youtu.be/QUZUdqEoVRU">
   <img width="1400" height="789" src="https://github.com/user-attachments/assets/33024ef3-eeef-4bee-aa94-a108a45c0866" />
</a>


## Concept
NarrativeCraft is a Minecraft mod that turns the game into a space for building interactive stories, with choices and branching narratives that react to the player’s decisions.

It's mostly for creating a game in a game and is highly inspired from games I love like Life Is Strange and Until Then.

It's all-in-one (recording, cutscene, rendering dialogs, story management) and is designed to be easy to use.

Create your own unique stories with endearing characters, let your imagination take form in a 3D sandbox, with shaders, music, and cutscenes.

This mod is mainly written to be singleplayer-only, to create adventure maps. Multiplayer is not supported yet.

## Features

### Core Features
- **Ink integration** - Powerful narrative scripting with [Ink](https://www.inklestudios.com/ink/)
- **All-in-one system** - Player recording, cutscenes, multiple recordings at the same time
- **Customizable** - Main screen, credits, dialogues, and more
- **Made to be accessible** - Designed for everyone, from beginners to advanced users

### Multi-Version Support
- **5 build targets** - Fabric 1.19.4, Fabric 1.20.6, Fabric 1.21.11, NeoForge 1.20.6, NeoForge 1.21.11
- **Single codebase** - Shared code with version-specific overrides
- **Automated CI** - All versions tested on every commit

### Internationalization (i18n)
- **7 languages** - English, French, German, Spanish, Chinese, Russian, Arabic
- **RTL support** - Full Arabic right-to-left text support
- **122 translation keys** - Complete UI localization
- **Automatic language detection** - Matches your Minecraft language setting

### Production Quality
- **Structured error messages** - Clear WHAT/WHERE/WHY/FIX format
- **Ink script validation** - Catch errors before runtime
- **Resource cleanup system** - Automatic cleanup of entities, blocks, and game state
- **CI validation** - Automated checks for translations and code quality

## Supported Languages

| Language | Code | Status |
|----------|------|--------|
| English (US) | en_us | Source |
| French | fr_fr | Complete |
| German | de_de | Complete |
| Spanish | es_es | Complete |
| Chinese (Simplified) | zh_cn | Complete |
| Russian | ru_ru | Complete |
| Arabic (RTL) | ar_sa | Complete |

Want to add a new language? See [ADDING_NEW_LANGUAGE.md](docs/ADDING_NEW_LANGUAGE.md).

## Mod Compatibility
- **EmoteCraft**: Emotes will be recorded, and you can play them from ink script.

## How does it work?

NarrativeCraft uses a narrative scripting language called [Ink](https://www.inklestudios.com/ink/), which is open-source and highly scalable for integration into other projects. The Java integration was made by [bladecoder](https://github.com/bladecoder/).

Ink is a great narrative scripting language because:
- It's easy to learn.
- Dialogues come first, logic later.
- It has enough advanced features to create complex stories.

NarrativeCraft is the middleman that interprets your dialogues and events as in-game actions.  
By events, I mean custom lines you can write to trigger in-game actions. For example, changing the time, weather, playing cutscenes...

## What does it look like in-game?

I'm glad you asked! I made a small showcase to demonstrate what this mod is capable of:  
<a href="https://youtu.be/4VunlM_XCms">
<img src="https://github.com/user-attachments/assets/a1c5a664-dbf4-4782-aa4e-d18ca1462579"/>
</a>

## Installation

### Supported Versions

| Minecraft | Loader | Status | Java |
|-----------|--------|--------|------|
| 1.21.11 | Fabric 0.18.3+ | ✅ Full support | 21+ |
| 1.21.11 | NeoForge 21.11+ | ✅ Full support | 21+ |
| 1.20.6 | Fabric 0.15.11+ | ✅ Full support | 17+ |
| 1.20.6 | NeoForge 20.6+ | ✅ Full support | 17+ |
| 1.19.4 | Fabric 0.14.21+ | ✅ Full support | 17+ |

### Requirements

- **Java**: 17+ (MC 1.19.4/1.20.6) or 21+ (MC 1.21.11)
- **Fabric API**: Required for Fabric versions

### For Players

1. Download the mod for your Minecraft version from [Releases](https://github.com/LOUDO56/NarrativeCraft/releases)
2. Place the `.jar` file in your `mods` folder
3. Install Fabric API (if using Fabric)
4. Launch Minecraft

### For Developers

```bash
# Clone the repository
git clone https://github.com/LOUDO56/NarrativeCraft.git
cd NarrativeCraft

# Build all versions (requires Java 21)
./gradlew :fabric-1.19.4:build :fabric-1.20.6:build :fabric-1.21.11:build :neoforge-1.20.6:build :neoforge-1.21.11:build

# Build specific version
./gradlew :fabric-1.19.4:build
./gradlew :fabric-1.21.11:build
./gradlew :neoforge-1.21.11:build

# Run tests
./gradlew :common:test
```

See [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for build issues.

## Documentation

### User Guides
- [INK_GUIDE.md](docs/INK_GUIDE.md) - Complete scripting tutorial
- [TAG_REFERENCE.md](docs/TAG_REFERENCE.md) - All tags and parameters
- [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) - Common issues and solutions
- [Online Documentation](https://loudo56.github.io/NarrativeCraft-docs/)

### Developer Guides
- [MULTI_VERSION_ARCHITECTURE.md](docs/MULTI_VERSION_ARCHITECTURE.md) - How multi-version support works
- [I18N_RULES.md](docs/I18N_RULES.md) - Translation guidelines and rules
- [ADDING_NEW_LANGUAGE.md](docs/ADDING_NEW_LANGUAGE.md) - How to add a new language

## Contribution

This mod is meant to be developed and used with the community. My goal is to make story creation easily accessible to as many people as possible.

If you're experienced in Minecraft modding or Java in general, any suggestions to refactor or improve the code are greatly appreciated.

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

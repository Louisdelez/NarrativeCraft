# NarrativeCraft Showcase Examples

Advanced examples demonstrating the full power of NarrativeCraft.

## complex-scene.ink

A complete, feature-rich story showcasing all major NarrativeCraft capabilities:

### Features Demonstrated

| Category | Features |
|----------|----------|
| **Dialog** | Multi-character conversations, variable interpolation |
| **Choices** | Branching paths, conditional responses |
| **State** | Trust system, inventory tracking, multiple endings |
| **Visual** | Fade effects, cinematic borders, screen shake, text overlays |
| **Audio** | Sound effects, music with fade in/out, ambient loops |
| **World** | Time of day, weather control |
| **UI** | Dialog customization (scale, color, width) |
| **Flow** | Save points, wait/timing, scene transitions |

### Error Examples

The file includes **commented error examples** that demonstrate NarrativeCraft's helpful error messages:

```ink
// Uncomment to see the error:
// # fde 1.0 2.0 1.0

// Error message:
// [WHAT] Unknown tag
// [WHERE] Story: showcase / Scene: complex-scene / Line: X
// [WHY] The tag 'fde' is not a recognized NarrativeCraft tag
// [FIX] Did you mean 'fade'?
```

### Prerequisites

- Complete the tutorial examples first (`../tutorial/`)
- For camera/cutscene features: Set up angles in the NarrativeCraft editor

### Story Synopsis

You arrive at the Castle of Shadows, encountering Marcus, a mysterious guardian. Your choices affect trust levels and unlock different paths:

- **High trust**: Full rewards, easy access to the artifact
- **Medium trust**: Standard rewards, some help
- **Low trust**: Minimal help, harder path
- **Confrontation**: Combat or capture

### How to Use

1. Create a new scene in NarrativeCraft
2. Copy `complex-scene.ink` content to your scene script
3. Build and play
4. Experiment with different choices!

## Documentation

- [INK_GUIDE.md](../../docs/INK_GUIDE.md) - Complete scripting guide
- [TAG_REFERENCE.md](../../docs/TAG_REFERENCE.md) - All tags reference
- [TROUBLESHOOTING.md](../../docs/TROUBLESHOOTING.md) - Error solutions

## Support

- [Discord](https://discord.com/invite/E3zzNv79DN)
- [GitHub Issues](https://github.com/LOUDO56/NarrativeCraft/issues)

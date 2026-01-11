# NarrativeCraft v1.2.0 Release Checklist

## Pre-Release Verification

### Build Verification
- [x] `./gradlew :fabric-1.19.4:build` - BUILD SUCCESSFUL
- [x] `./gradlew :fabric-1.20.6:build` - BUILD SUCCESSFUL
- [x] `./gradlew :fabric-1.21.11:build` - BUILD SUCCESSFUL
- [x] `./gradlew :neoforge-1.20.6:build` - BUILD SUCCESSFUL
- [x] `./gradlew :neoforge-1.21.11:build` - BUILD SUCCESSFUL

### Cross-Version Validation
- [x] `MULTI_VERSION_BUILD` constant present in all 5 JARs
- [x] `example-stories/cross-version-test.ink` created
- [x] Smoke test results documented in `docs/SMOKE_RESULTS_v1.2.0.md`

### CI/CD
- [x] `.github/workflows/ci.yml` includes all 5 targets
- [x] `.github/workflows/release.yml` includes all 5 targets
- [x] All builds REQUIRED for PR merge (fail-fast: false in CI)

### Documentation
- [x] README.md updated with 1.19.4 support
- [x] CHANGELOG.md updated for v1.2.0
- [x] CONTRIBUTING.md updated with multi-version guidelines
- [x] `docs/MULTI_VERSION_ARCHITECTURE.md` created
- [x] `docs/SMOKE_RESULTS_v1.2.0.md` created

## Release Artifacts

| File | Size | Status |
|------|------|--------|
| `narrativecraft-fabric-1.19.4-1.2.0.jar` | ~1.08 MB | Ready |
| `narrativecraft-fabric-1.20.6-1.2.0.jar` | ~1.07 MB | Ready |
| `narrativecraft-fabric-1.21.11-1.2.0.jar` | ~1.09 MB | Ready |
| `narrativecraft-neoforge-1.20.6-1.2.0.jar` | ~1.07 MB | Ready |
| `narrativecraft-neoforge-1.21.11-1.2.0.jar` | ~1.08 MB | Ready |

## Release Steps

1. **Final Build**
   ```bash
   ./gradlew clean build
   ```

2. **Verify All JARs**
   ```bash
   ls -la */build/libs/*.jar | grep -v sources | grep -v javadoc
   ```

3. **Create Git Tag**
   ```bash
   git add .
   git commit -m "Release v1.2.0 - Multi-version support (5 targets)"
   git tag v1.2.0
   git push origin main --tags
   ```

4. **GitHub Release** (Automated)
   - Triggered by `v1.2.0` tag push
   - All 5 JARs attached automatically
   - Changelog generated from commits

5. **Post-Release**
   - Verify GitHub Release page shows all 5 JARs
   - Update Discord announcement
   - Update mod hosting sites (CurseForge, Modrinth)

## Release Notes Template

```markdown
## NarrativeCraft v1.2.0

### Multi-Version Support

NarrativeCraft now supports **5 build targets**:

| Minecraft | Loader | Java |
|-----------|--------|------|
| 1.19.4 | Fabric | 17+ |
| 1.20.6 | Fabric | 17+ |
| 1.20.6 | NeoForge | 17+ |
| 1.21.11 | Fabric | 21+ |
| 1.21.11 | NeoForge | 21+ |

### Highlights
- Full feature parity across all versions
- Version-aware capability checking
- Comprehensive compatibility layer

### Installation
Download the JAR matching your Minecraft version and mod loader.
Place in your `mods` folder. Fabric versions require Fabric API.

### Documentation
- [MULTI_VERSION_ARCHITECTURE.md](docs/MULTI_VERSION_ARCHITECTURE.md)
- [SMOKE_RESULTS_v1.2.0.md](docs/SMOKE_RESULTS_v1.2.0.md)
```

## Rollback Plan

If critical issues are found post-release:

1. **Immediate**: Disable release on GitHub (mark as pre-release)
2. **Hotfix**: Create `hotfix/v1.2.1` branch
3. **Fix**: Address issue in affected version(s)
4. **Test**: Run cross-version test on fixed build
5. **Release**: Tag `v1.2.1` with fix

## Sign-Off

- [ ] Lead Developer: _________________ Date: _______
- [ ] QA Verification: _________________ Date: _______

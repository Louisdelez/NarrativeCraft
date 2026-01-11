# Requirements Checklist: Multi-Version Minecraft Support

**Feature**: 002-multi-version-support
**Created**: 2026-01-09
**Updated**: 2026-01-09 (post-clarification)

## Runtime Compatibility

- [ ] **FR-001**: Le mod charge sans erreur sur Minecraft Fabric 1.19.4
- [ ] **FR-002**: Le mod charge sans erreur sur Minecraft Fabric 1.20.6
- [ ] **FR-003**: Le mod charge sans erreur sur Minecraft Fabric 1.21.11
- [ ] **FR-002b**: Le mod charge sans erreur sur Minecraft NeoForge 1.20.6
- [ ] **FR-003b**: Le mod charge sans erreur sur Minecraft NeoForge 1.21.11
- [ ] **FR-004**: Fonctionnalités core identiques sur toutes versions
  - [ ] Dialogues Ink
  - [ ] Choix multiples
  - [ ] Variables Ink
  - [ ] Tags personnalisés
- [ ] **FR-005**: Fonctionnalités Minecraft adaptées par version (désactivées avec warning si non-portable)
  - [ ] Camera system
  - [ ] Recording
  - [ ] Playback
  - [ ] Triggers
- [ ] **FR-006**: Message d'erreur clair pour versions non-supportées
- [ ] **FR-007**: Aucun crash silencieux, erreurs loggées

## Maintenance Compatibility

- [ ] **FR-008**: Module `common` partagé entre versions
- [ ] **FR-009**: Modules version-spécifiques isolés
  - [ ] fabric-1.19
  - [ ] fabric-1.20
  - [ ] fabric-1.21
  - [ ] neoforge-1.20
  - [ ] neoforge-1.21
- [ ] **FR-010**: Mixins organisés par version avec refmaps
- [ ] **FR-011**: Architectury Loom configuré et fonctionnel

## Publication & Distribution

- [ ] **FR-012**: 5 JARs produits par le build
  - [ ] Fabric 1.19.4
  - [ ] Fabric 1.20.6
  - [ ] Fabric 1.21.11
  - [ ] NeoForge 1.20.6
  - [ ] NeoForge 1.21.11
- [ ] **FR-013**: Métadonnées correctes par JAR (fabric.mod.json / mods.toml)
- [ ] **FR-014**: Workflow release automatisé
  - [ ] Publication CurseForge
  - [ ] Publication Modrinth
- [ ] **FR-015**: CHANGELOG inclus dans releases

## QA & Non-Régression

- [ ] **FR-016**: Tests unitaires exécutés sur chaque version
- [ ] **FR-017**: Build vérifié sans erreur pour chaque version
- [ ] **FR-018**: Test de smoke pour chaque version
- [ ] **FR-019**: PRs bloquées si une version échoue

## Success Criteria

- [ ] **SC-001**: 5 combinaisons cibles fonctionnelles
- [ ] **SC-002**: 100% tests passent sur toutes versions
- [ ] **SC-003**: Stories compatibles cross-version
- [ ] **SC-004**: Ajout nouvelle sous-version < 4h
- [ ] **SC-005**: CI < 15 minutes
- [ ] **SC-006**: Zéro crash silencieux
- [ ] **SC-007**: Release one-click multi-version

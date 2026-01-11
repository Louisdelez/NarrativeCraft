# Feature Specification: Multi-Version Minecraft Support

**Feature Branch**: `002-multi-version-support`
**Created**: 2026-01-09
**Status**: DONE
**Completed**: 2026-01-11
**Input**: User description: "Je veux ajouter à NarrativeCraft une compatibilité multi-versions Minecraft, afin que le mod fonctionne proprement et soit publiable de Minecraft 1.19 jusqu'à 1.21.11 inclus."

## Clarifications

### Session 2026-01-09

- Q: Quelle granularité de JARs produire? → A: Un JAR par version majeure (3 JARs: 1.19.x, 1.20.x, 1.21.x)
- Q: Quel système de build multi-versions utiliser? → A: Architectury Loom
- Q: Stratégie si fonctionnalité non-portable sur 1.19.x? → A: Désactiver avec warning informatif
- Q: Quelles sous-versions officiellement supportées? → A: Dernière stable par majeure (1.19.4, 1.20.6, 1.21.11)
- Q: Support NeoForge en plus de Fabric? → A: Fabric + NeoForge pour toutes versions où disponible (1.20.6, 1.21.11)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Player on Minecraft 1.21.x (Priority: P1)

Un joueur utilisant Minecraft 1.21.x (1.21, 1.21.1, 1.21.11) veut installer et utiliser NarrativeCraft pour créer ou jouer des histoires interactives.

**Why this priority**: Version cible actuelle du mod, base de joueurs la plus récente, support déjà partiellement fonctionnel.

**Independent Test**: Installer le mod sur Minecraft 1.21.11 Fabric, lancer le jeu, vérifier que le menu du mod apparaît, créer une histoire simple et la jouer.

**Acceptance Scenarios**:

1. **Given** Minecraft 1.21.11 avec Fabric Loader 0.18.x installé, **When** le joueur place le JAR NarrativeCraft dans le dossier mods et lance le jeu, **Then** le mod se charge sans erreur et le bouton NarrativeCraft apparaît
2. **Given** NarrativeCraft chargé sur 1.21.x, **When** le joueur appuie sur la touche N, **Then** l'éditeur de story s'ouvre correctement
3. **Given** Une story créée sur 1.21.11, **When** le joueur lance la story, **Then** les dialogues, cutscenes et triggers fonctionnent identiquement à la version originale

---

### User Story 2 - Player on Minecraft 1.20.x (Priority: P1)

Un joueur utilisant Minecraft 1.20.x (1.20, 1.20.1, 1.20.2, 1.20.4, 1.20.6) veut installer et utiliser NarrativeCraft.

**Why this priority**: Version très populaire avec une large base de joueurs et de serveurs, critique pour l'adoption du mod.

**Independent Test**: Installer le mod sur Minecraft 1.20.4 Fabric, vérifier le chargement, créer et jouer une histoire.

**Acceptance Scenarios**:

1. **Given** Minecraft 1.20.4 avec Fabric Loader 0.15.x+ installé, **When** le joueur installe le JAR NarrativeCraft compatible 1.20.x, **Then** le mod se charge sans crash ni erreur
2. **Given** NarrativeCraft sur 1.20.x, **When** le joueur utilise toutes les fonctionnalités (dialogues, camera, recording, playback), **Then** chaque fonctionnalité se comporte de manière identique à la version 1.21.x
3. **Given** Une story créée sur 1.21.x, **When** elle est copiée et jouée sur 1.20.x, **Then** la story fonctionne sans modification (rétrocompatibilité des assets)

---

### User Story 3 - Player on Minecraft 1.19.x (Priority: P2)

Un joueur utilisant Minecraft 1.19.x (1.19, 1.19.2, 1.19.4) veut installer et utiliser NarrativeCraft.

**Why this priority**: Version encore utilisée mais moins prioritaire que 1.20.x et 1.21.x, nécessite potentiellement plus d'adaptations.

**Independent Test**: Installer le mod sur Minecraft 1.19.4 Fabric, vérifier le chargement et les fonctionnalités de base.

**Acceptance Scenarios**:

1. **Given** Minecraft 1.19.4 avec Fabric Loader 0.14.x+ installé, **When** le joueur installe le JAR NarrativeCraft compatible 1.19.x, **Then** le mod se charge correctement
2. **Given** NarrativeCraft sur 1.19.x, **When** le joueur joue une story, **Then** les dialogues et choix Ink fonctionnent correctement
3. **Given** Les différences d'API entre 1.19 et 1.21, **When** une fonctionnalité n'existe pas sur 1.19, **Then** le mod affiche un message clair indiquant la limitation

---

### User Story 4 - Story Creator Cross-Version (Priority: P2)

Un créateur de contenu veut créer des stories qui fonctionnent sur toutes les versions supportées de Minecraft.

**Why this priority**: Essentiel pour l'écosystème du mod, mais dépend des stories de base fonctionnelles.

**Independent Test**: Créer une story sur 1.21.11, la tester sur 1.20.4 et 1.19.4 sans modification.

**Acceptance Scenarios**:

1. **Given** Une story utilisant toutes les fonctionnalités communes, **When** elle est jouée sur n'importe quelle version 1.19-1.21, **Then** elle s'exécute identiquement
2. **Given** Une story utilisant une fonctionnalité spécifique à 1.21, **When** elle est jouée sur 1.19, **Then** un warning clair est affiché et la story continue sans crash
3. **Given** Le format de fichier des stories (.ink, .json), **When** copié entre versions, **Then** aucune modification n'est requise

---

### User Story 5 - Mod Maintainer (Priority: P2)

Le mainteneur du mod veut pouvoir maintenir facilement le code pour plusieurs versions Minecraft sans duplication excessive.

**Why this priority**: Critique pour la maintenabilité long-terme mais ne bloque pas la fonctionnalité utilisateur.

**Independent Test**: Modifier une fonctionnalité core et vérifier que le changement se propage à toutes les versions via la CI.

**Acceptance Scenarios**:

1. **Given** Une structure de projet multi-versions, **When** une modification est faite dans le code commun, **Then** elle s'applique à toutes les versions sans intervention manuelle
2. **Given** Un bug rapporté sur une version spécifique, **When** le fix est implémenté, **Then** il est facile d'identifier si le fix s'applique aux autres versions
3. **Given** La matrice de versions supportées, **When** une nouvelle sous-version Minecraft sort (ex: 1.21.12), **Then** l'adaptation requiert moins de 4h de travail

---

### User Story 6 - Release Manager (Priority: P3)

Le responsable des releases veut publier le mod sur CurseForge et Modrinth pour toutes les versions supportées.

**Why this priority**: Important pour la distribution mais vient après que les builds soient fonctionnels.

**Independent Test**: Exécuter le workflow de release et vérifier que tous les JARs sont générés avec les métadonnées correctes.

**Acceptance Scenarios**:

1. **Given** Un tag de version git (ex: v1.2.0), **When** le workflow GitHub Actions s'exécute, **Then** un JAR est produit pour chaque version Minecraft cible
2. **Given** Les JARs générés, **When** uploadés sur CurseForge/Modrinth, **Then** chaque JAR indique correctement sa version Minecraft compatible
3. **Given** Le fichier CHANGELOG, **When** une release est créée, **Then** les notes de version sont automatiquement attachées

---

### User Story 7 - QA/Reviewer (Priority: P3)

Un testeur veut vérifier que le mod fonctionne sur toutes les versions avant une release.

**Why this priority**: Qualité importante mais dépend des outils de build et test fonctionnels.

**Independent Test**: Exécuter la suite de tests sur chaque version et obtenir un rapport consolidé.

**Acceptance Scenarios**:

1. **Given** La CI configurée, **When** un PR est soumis, **Then** les tests s'exécutent sur toutes les versions cibles en parallèle
2. **Given** Un échec sur une version spécifique, **When** la CI termine, **Then** le rapport indique clairement quelle version a échoué et pourquoi
3. **Given** La matrice de tests, **When** une régression est détectée, **Then** elle est bloquante pour la PR

---

### Edge Cases

- Que se passe-t-il si un joueur utilise une version non-supportée (ex: 1.18.2 ou 1.22)?
  - Le mod ne doit pas charger et afficher un message clair indiquant les versions supportées
- Que se passe-t-il si Fabric Loader est trop ancien pour la version?
  - Le mod doit vérifier la version de Fabric et afficher une erreur claire
- Comment gérer les mixins qui ciblent des classes renommées/déplacées entre versions?
  - Utiliser des mixins conditionnels ou des modules spécifiques par version
- Que se passe-t-il si blade-ink n'est pas compatible avec une version Java plus ancienne?
  - Vérifier que blade-ink 1.2.1+nc fonctionne avec Java 17 (1.19/1.20) et Java 21 (1.21)

## Requirements *(mandatory)*

### Functional Requirements

#### Runtime Compatibility

- **FR-001**: Le mod DOIT charger sans erreur sur Minecraft 1.19.4 (version officielle supportée pour 1.19.x)
- **FR-002**: Le mod DOIT charger sans erreur sur Minecraft 1.20.6 (version officielle supportée pour 1.20.x)
- **FR-003**: Le mod DOIT charger sans erreur sur Minecraft 1.21.11 (version officielle supportée pour 1.21.x)
- **FR-004**: Toutes les fonctionnalités core (dialogues, choix, variables Ink) DOIVENT fonctionner identiquement sur toutes les versions
- **FR-005**: Les fonctionnalités dépendantes de l'API Minecraft (camera, recording, playback) DOIVENT être adaptées par version; si non-portable, la fonctionnalité est désactivée avec un warning clair informant l'utilisateur de la limitation
- **FR-006**: Le mod DOIT afficher un message d'erreur clair si lancé sur une version non-supportée
- **FR-007**: Le mod NE DOIT PAS crasher silencieusement - toute erreur doit être loggée et communiquée

#### Maintenance Compatibility

- **FR-008**: Le code commun DOIT être partagé entre toutes les versions via un module `common`
- **FR-009**: Les adaptations spécifiques par version DOIVENT être isolées dans des modules dédiés
- **FR-010**: Les mixins DOIVENT être organisés par version cible avec des refmaps appropriés
- **FR-011**: Le projet DOIT utiliser Architectury Loom pour la gestion multi-versions et le partage de code commun

#### Publication & Distribution

- **FR-012**: Le système de build DOIT produire 5 JARs: Fabric 1.19.4, Fabric 1.20.6, Fabric 1.21.11, NeoForge 1.20.6, NeoForge 1.21.11
- **FR-013**: Chaque JAR DOIT inclure les métadonnées correctes (fabric.mod.json ou mods.toml selon loader) indiquant les versions compatibles
- **FR-014**: Le workflow de release DOIT automatiser la publication sur CurseForge et Modrinth
- **FR-015**: Les releases DOIVENT inclure un CHANGELOG lisible par les utilisateurs

#### QA & Non-Régression

- **FR-016**: La CI DOIT exécuter les tests unitaires sur chaque version cible
- **FR-017**: La CI DOIT vérifier que chaque JAR se construit sans erreur
- **FR-018**: Un test de smoke (chargement du mod) DOIT exister pour chaque version
- **FR-019**: Les PRs NE DOIVENT PAS être mergées si une version échoue

### Key Entities

- **VersionAdapter**: Interface pour abstraire les différences d'API Minecraft entre versions
- **ModuleCommon**: Code partagé indépendant de la version Minecraft
- **ModuleFabric1.19**: Adaptations spécifiques pour Minecraft 1.19.x
- **ModuleFabric1.20**: Adaptations spécifiques pour Minecraft 1.20.x
- **ModuleFabric1.21**: Adaptations spécifiques pour Minecraft 1.21.x (existant actuellement)
- **BuildMatrix**: Configuration Gradle définissant les versions cibles et leurs dépendances

## Constraints

### In Scope

- Support de Minecraft 1.19.4, 1.20.6, 1.21.11 sur Fabric
- Support de Minecraft 1.20.6, 1.21.11 sur NeoForge
- Adaptation des mixins pour chaque version majeure
- Système de build multi-versions avec Architectury Loom
- CI/CD pour toutes les versions et loaders
- Documentation des différences entre versions

### Out of Scope

- Support de Minecraft < 1.19 ou > 1.21.11
- NeoForge pour 1.19.x (NeoForge n'existe qu'à partir de 1.20.4)
- Nouvelles fonctionnalités narratives (ce projet est uniquement le support multi-versions)
- Support Quilt
- Backport des optimisations 1.21 vers les anciennes versions si incompatibles

### Technical Constraints

- **Java**: 1.19.x et 1.20.x utilisent Java 17, 1.21.x utilise Java 21
- **Fabric API**: Versions différentes pour chaque Minecraft majeur
- **Mixins**: Certaines classes sont renommées ou déplacées entre versions
- **blade-ink**: Doit rester compatible avec Java 17 minimum
- **Architectury Loom**: Utilisé pour la gestion multi-versions et le partage de code commun entre modules

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Le mod charge et fonctionne sur les 5 combinaisons cibles: Fabric 1.19.4, Fabric 1.20.6, Fabric 1.21.11, NeoForge 1.20.6, NeoForge 1.21.11
- **SC-002**: 100% des tests unitaires passent sur toutes les versions
- **SC-003**: Une story créée sur une version peut être jouée sur une autre version sans modification (compatibilité 100%)
- **SC-004**: Le temps d'ajout d'une nouvelle sous-version Minecraft < 4 heures
- **SC-005**: La CI build et test toutes les versions en < 15 minutes
- **SC-006**: Zéro crash silencieux - toutes les erreurs sont loggées avec contexte
- **SC-007**: La release produit les JARs pour toutes les versions en une seule action

## Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Mixins incompatibles entre versions | High | High | Utiliser des refmaps séparés et des mixins conditionnels |
| API Minecraft changée significativement | High | Medium | Couche d'abstraction VersionAdapter |
| blade-ink incompatible Java 17 | Medium | Low | Vérifier la compatibilité, sinon utiliser version alternative |
| Temps de CI trop long | Low | Medium | Paralléliser les builds, utiliser le cache Gradle |
| Maintenance trop complexe | High | Medium | Documentation claire, structure modulaire stricte |

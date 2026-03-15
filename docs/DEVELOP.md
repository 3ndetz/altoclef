# Development Guide — altoclef + Tungsten + Baritone

## Prerequisites

| Requirement | Version |
|---|---|
| Java (JDK) | 21 |
| Gradle | bundled via `gradlew` (no separate install needed) |
| Git | any recent version |
| Internet | first build downloads ~1 GB (MC + Fabric + deps) |

---

## Folder structure

All three projects must sit side by side:

```
AUTOCLEF_UPD/
├── altoclef/                <- main mod (this project)
│   ├── build.gradle
│   ├── src/
│   └── versions/            <- multiversion (1.16.5 … 1.21.1)
├── Tungsten/                <- pathfinder mod
│   ├── build.gradle
│   └── src/
└── baritone_altoclef/       <- Baritone fork with altoclef patches
    ├── baritone/            <- git submodule (cabaletta/baritone)
    ├── patches/             <- altoclef patches
    ├── maven/               <- prebuilt Baritone JARs
    ├── apply_patches.bat
    └── apply_patches.sh
```

---

## Step 1 — Clone everything (one time)

```bash
git clone --recurse-submodules https://github.com/3ndetz/AUTOCLEF_UPD
cd AUTOCLEF_UPD
```

`baritone_altoclef/maven/` already contains a prebuilt
`baritone-unoptimized-fabric-1.21.jar`. No need to build Baritone
unless you change its source code.

---

## Step 2 — Build & publish Tungsten to local Maven

```bash
cd Tungsten
gradlew.bat publishToMavenLocal     # Windows
./gradlew publishToMavenLocal       # Linux/Mac
```

altoclef pulls Tungsten from `~/.m2/repository/` (local Maven) and bundles it
inside the final JAR via Jar-in-Jar. **One mod = everything.**

**After every Tungsten code change**, re-run `publishToMavenLocal` so altoclef
picks up the new version.

---

## Step 3 — Build & Run altoclef

```bash
cd altoclef
gradlew.bat :1.21:runClient       # Windows
./gradlew :1.21:runClient         # Linux/Mac
```

altoclef pulls Baritone from `baritone_altoclef/maven/` and Tungsten
from local Maven automatically (see `build.gradle`).

---

## Rebuilding after changes

| What you changed | What to rebuild |
|---|---|
| altoclef source | `cd altoclef && gradlew :1.21:build` |
| Tungsten source | `cd Tungsten && gradlew publishToMavenLocal`, then rebuild altoclef |
| Baritone source | See "Rebuilding Baritone" below, then rebuild altoclef |

---

## Rebuilding Baritone (only if you changed Baritone code)

```bash
cd baritone_altoclef/baritone
git checkout origin/1.21

# Apply altoclef patches
cd ..
apply_patches.bat           # Windows
bash apply_patches.sh       # Linux/Mac

# Build
cd baritone
gradlew.bat :fabric:build -x test    # Windows
./gradlew :fabric:build -x test      # Linux/Mac
```

Copy the built JAR into maven:

```bash
# Windows (PowerShell):
Copy-Item "baritone\fabric\build\libs\baritone-unoptimized-fabric-fabric-*.jar" `
          "maven\cabaletta\baritone-unoptimized-fabric\1.21\baritone-unoptimized-fabric-1.21.jar" -Force

# Linux/Mac:
cp baritone/fabric/build/libs/baritone-unoptimized-fabric-fabric-*.jar \
   maven/cabaletta/baritone-unoptimized-fabric/1.21/baritone-unoptimized-fabric-1.21.jar
```

---

## How dependencies flow

```
altoclef (final JAR bundles everything)
  ├── Baritone JAR        <- from baritone_altoclef/maven/ (local maven)
  ├── Tungsten JAR        <- from ~/.m2/ (publishToMavenLocal)
  └── Fabric API, MC      <- from internet (cached)

Tungsten
  ├── Baritone JAR        <- from baritone_altoclef/maven/ (same JAR)
  └── Fabric API, MC      <- from internet (cached)
```

Both altoclef and Tungsten depend on the same Baritone JAR.
This is a **diamond dependency** (not circular) — no conflicts.

> **Note:** Tungsten's Baritone fallback is **disabled by default**
> (`baritoneEnabled = false` in `tungsten.json`). When embedded in
> altoclef, Baritone is managed by altoclef — Tungsten only provides
> its own A* pathfinding.

---

## Build output

| Project | Output JAR | Location |
|---|---|---|
| altoclef | `altoclef-1.21-0.19.jar` (bundles Tungsten + Baritone) | `altoclef/build/libs/` |
| Tungsten | `tungsten-fabric-ALPHA-1.6.0-1.21compat.jar` | `Tungsten/build/libs/` |
| Baritone | `baritone-unoptimized-fabric-1.21.jar` | `baritone_altoclef/maven/…/` |

Drop **only the altoclef JAR** into `.minecraft/mods/` — Tungsten and Baritone are bundled inside.

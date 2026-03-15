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
git clone https://github.com/3ndetz/autoclef    altoclef
git clone https://github.com/3ndetz/Tungsten     Tungsten
git clone https://github.com/3ndetz/baritone_altoclef

cd baritone_altoclef
git submodule update --init
cd ..
```

`baritone_altoclef/maven/` already contains a prebuilt
`baritone-unoptimized-fabric-1.21.jar`. No need to build Baritone
unless you change its source code.

---

## Step 2 — Build Tungsten

```bash
cd Tungsten
gradlew.bat build           # Windows
./gradlew build             # Linux/Mac
```

Output JAR: `Tungsten/build/libs/tungsten-fabric-ALPHA-1.6.0-1.21compat.jar`

This JAR is what altoclef references as a dependency.

---

## Step 3 — Build & Run altoclef

```bash
cd altoclef
gradlew.bat runClient       # Windows
./gradlew runClient         # Linux/Mac
```

altoclef pulls Baritone from `baritone_altoclef/maven/` and Tungsten
from `Tungsten/build/libs/` automatically (see `build.gradle`).

---

## Rebuilding after changes

| What you changed | What to rebuild |
|---|---|
| altoclef source | `cd altoclef && gradlew build` |
| Tungsten source | `cd Tungsten && gradlew build`, then rebuild altoclef |
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
altoclef
  ├── Baritone JAR        <- from baritone_altoclef/maven/ (local maven)
  ├── Tungsten JAR        <- from Tungsten/build/libs/ (local file)
  └── Fabric API, MC      <- from internet (cached)

Tungsten
  ├── Baritone JAR        <- from baritone_altoclef/maven/ (same JAR)
  └── Fabric API, MC      <- from internet (cached)
```

Both altoclef and Tungsten depend on the same Baritone JAR.
This is a **diamond dependency** (not circular) — no conflicts.

When running as two separate mods: each bundles its own copy of Baritone.
Fabric Jar-in-Jar deduplicates automatically.

> **Note:** Tungsten's Baritone fallback is **disabled by default**
> (`baritoneEnabled = false` in `tungsten.json`). When embedded in
> altoclef, Baritone is managed by altoclef — Tungsten only provides
> its own A* pathfinding.

---

## Build output

| Project | Output JAR | Location |
|---|---|---|
| altoclef | `altoclef-1.21-0.19.jar` | `altoclef/build/libs/` |
| Tungsten | `tungsten-fabric-ALPHA-1.6.0-1.21compat.jar` | `Tungsten/build/libs/` |
| Baritone | `baritone-unoptimized-fabric-1.21.jar` | `baritone_altoclef/maven/…/` |

Drop both altoclef and Tungsten JARs into `.minecraft/mods/` to run.
Baritone is bundled inside both JARs automatically.

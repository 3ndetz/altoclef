# Baritone Source Modification Report

## Текущая архитектура (как всё устроено сейчас)

### Как baritone попадает в altoclef

```
baritone_altoclef/
  baritone/          ← клон cabaletta/baritone (исходник)
  patches/           ← 4 патча от MiranCZ
    0001-Add-altoclef-options.patch
    0002-AltoClef-changes.patch
    0003-BuilderProcess-changes.patch
    0004-Properties-changes.patch
  maven/             ← pre-built JAR (уже собранный)
  apply_patches.bat  ← скрипт: cd baritone && git apply ../patches/*.patch
```

**Шаг деплоя:**
1. `apply_patches.bat` применяет патчи к vanilla baritone
2. Собирается JAR (`gradle build`)
3. JAR выкладывается на `mirancz.github.io/maven/`
4. `altoclef/build.gradle` тянет его как обычную зависимость:

```groovy
// build.gradle altoclef (строки 141-148)
if (getProject().hasProperty("altoclef.development")) {
    modImplementation "baritone-api-fabric:baritone-unoptimized-fabric-${project.name}"
    include "baritone-api-fabric:baritone-unoptimized-fabric-${project.name}"
} else {
    modImplementation "cabaletta:baritone-unoptimized-fabric:${project.name}"
    include "cabaletta:baritone-unoptimized-fabric:${project.name}"
}
```

### Что делают патчи

| Патч | Файл(ы) | Суть |
|------|---------|------|
| 0001 | `AltoClefSettings.java` (новый) | Singleton с предикатами: какие блоки нельзя ломать/ставить, custom heuristics, protected items |
| 0002 | 13 файлов | `IBuilderProcess.popStack/isFromAltoclefFinished`, `RayTraceUtils.fluidHandling` поле, `InventoryBehavior` — не убирать инструменты из рук, `MineProcess` — учёт `AltoClefSettings`, `MovementHelper` — forceCanWalkOn/forceAvoidWalkThrough, и др. |
| 0003 | `BuilderProcess.java` | Реализация `popStack()`, `isFromAltoclefFinished()` |
| 0004 | `build.gradle`, `gradle.properties` | Убирает git-версионирование, ставит `version=1.19.4` (версию MC) |

---

## Вопрос 1: Как легко изменить исходник baritone

### Проблема

Сейчас цикл такой:
```
изменить исходник → пересобрать JAR → выложить на maven → altoclef подтянет
```
Для локальной разработки это неудобно.

### Вариант A — Development mode (уже встроен, ПРОСТОЙ)

MiranCZ уже предусмотрел это! В `build.gradle` есть флаг `altoclef.development`:

```groovy
// Если запустить gradle с -Paltoclef.development
modImplementation "baritone-api-fabric:baritone-unoptimized-fabric-${project.name}"
include "baritone-api-fabric:baritone-unoptimized-fabric-${project.name}"
```

**Шаги:**
1. `cd baritone_altoclef && apply_patches.bat`  ← применить патчи
2. `cd baritone && gradle build`                ← собрать baritone локально
3. JAR окажется в `baritone/build/libs/`
4. Зарегистрировать его как flatDir:
   ```groovy
   // altoclef/build.gradle добавить:
   flatDir { dirs '../baritone_altoclef/baritone/build/libs' }
   ```
5. Запустить сборку altoclef:
   ```bash
   gradle -Paltoclef.development build
   ```

**Плюсы:** Уже поддерживается кодом, минимум изменений
**Минусы:** Нужно каждый раз пересобирать baritone отдельно

### Вариант B — Gradle composite build (СРЕДНИЙ)

Gradle умеет включать один проект внутрь другого через `includeBuild`:

```groovy
// altoclef/settings.gradle
rootProject.name = 'altoclef-root'
includeBuild('../baritone_altoclef/baritone') {
    dependencySubstitution {
        substitute module('cabaletta:baritone-unoptimized-fabric') using project(':fabric')
    }
}
```

**Что происходит:** при сборке altoclef Gradle автоматически пересобирает baritone из исходников если что-то изменилось. Один `gradle build` — и всё.

**Плюсы:** Полностью прозрачно, автоматическая пересборка зависимостей, IDE видит исходники
**Минусы:** Нужно разобраться со структурой baritone (там multiproject: `fabric/`, `forge/`, `tweaker/`), может потребовать настройки

### Вариант C — Git patch workflow (ТЕКУЩИЙ, для изменений)

Если нужно добавить своё изменение к уже существующим патчам:

```bash
# 1. Применить существующие патчи
cd baritone_altoclef
apply_patches.bat

# 2. Внести изменения в baritone/src/...
# ... редактируем файлы ...

# 3. Создать новый патч
cd baritone
git add -A
git commit -m "My altoclef change"
git format-patch HEAD~1 -o ../patches/

# 4. Патч появится как 0005-My-altoclef-change.patch
# Закоммитить его в baritone_altoclef репо
```

**Плюсы:** Чисто, патчи читаемы, легко делиться
**Минусы:** Нужно поддерживать отдельный репозиторий, при обновлении vanilla baritone надо перебазировать патчи

### Вариант D — Fork baritone на GitHub + submodule (ПРАВИЛЬНЫЙ ДОЛГОСРОЧНЫЙ)

```
altoclef/ (mono-repo)
  baritone/   ← git submodule на fork cabaletta/baritone
              ← все патчи уже применены как коммиты
```

**Шаги:**
1. Форкнуть `cabaletta/baritone` на GitHub (например `3ndetz/baritone`)
2. Применить все 4 патча как коммиты в форке
3. Добавить как submodule в altoclef:
   ```bash
   git submodule add https://github.com/3ndetz/baritone baritone
   ```
4. Настроить `settings.gradle` на `includeBuild('./baritone')`

**Плюсы:** Полный контроль над исходниками, IDE видит всё, CI/CD прост, один репозиторий
**Минусы:** Нужно поддерживать fork актуальным (pull upstream), чуть сложнее настройка

---

## Как разворачивается текущая система (пошагово)

1. В `baritone_altoclef/baritone/` лежит vanilla baritone **без патчей** (чистый clone)
2. `apply_patches.bat` делает `git apply` для каждого `.patch` файла по порядку
3. Собранный JAR (`gradle build`) попадает в `maven/` папку
4. MiranCZ выкладывает его на `mirancz.github.io/maven/`
5. `altoclef/build.gradle` тянет через Maven: `cabaletta:baritone-unoptimized-fabric:1.21.1`
6. Gradle кэширует в `~/.gradle/caches/`

**Для нашего проекта локально:**
- В папке `baritone_altoclef/maven/` есть pre-built JAR — это запасной вариант
- `altoclef/build.gradle` уже имеет `flatDir { dirs '../baritone/dist' }` — но путь указывает на несуществующую папку `../baritone/dist`, а не на `../baritone_altoclef/baritone/build/libs`

---

## Рекомендация

**Для быстрых правок прямо сейчас:** Вариант A (dev mode)
**Для долгосрочной разработки в mono-repo:** Вариант D (fork + submodule + includeBuild)

Комбинация D+B (submodule + composite build) даст идеальный workflow: один `gradle build` из корня пересобирает весь стек.

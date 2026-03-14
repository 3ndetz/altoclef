# Tungsten Integration Report

## Что такое Tungsten

Tungsten — это самостоятельный патфайндер для Minecraft (Fabric), написанный kaptainwutax.
Он **не является форком Baritone** — это отдельная реализация с нуля.

**Ключевые характеристики:**
- MC 1.21.8 (значительно новее altoclef 1.21.1)
- 87 Java-файлов (небольшая кодовая база)
- Параллельный A* с несколькими коэффициентами эвристики одновременно
- Собственная физическая симуляция игрока (`Agent.java` ~1000 строк)
- Собственный `VoxelWorld` (кэш мира для потокобезопасного поиска)
- API: `PathFinder.find(world, target, player)` → `PathExecutor.setPath(path)`

---

## Архитектурные отличия от Baritone

| Аспект | Baritone | Tungsten |
|--------|---------|---------|
| Высокоуровневые процессы | MineProcess, BuilderProcess, GetToGoalProcess | Нет — только goto/stop |
| Интеграция с инвентарём | Да (InventoryBehavior) | Нет |
| AltoClef API | Глубокая интеграция (AltoClefSettings) | Нет |
| Физика игрока | Использует MC код напрямую | Своя симуляция (Agent.java) |
| Мир | Использует ClientWorld напрямую | VoxelWorld (кэш чанков) |
| Поиск | Один A* поток | Параллельный A*, несколько коэффициентов |
| Движения | Walk, Sprint, Fall, Swim, Climb, Place, Break | Walk, Sprint, Jump, CornerJump, LongJump, Swim, Ladder |
| Ломание/ставление блоков | Да | Нет |

---

## Варианты интеграции

### Вариант 1 — Запуск как отдельный мод рядом (ПРОСТОЙ)

Просто собрать Tungsten в JAR и положить его рядом с altoclef в папку `mods/`.

**Что нужно сделать:**
1. Обновить Tungsten с 1.21.8 → 1.21.1 (правка `gradle.properties` и возможно пара импортов из-за изменений API MC)
2. Собрать JAR
3. Оба мода работают одновременно

**Как использовать в altoclef:**
- Из кода altoclef вызывать `TungstenModDataContainer.EXECUTOR` через reflection или добавить общий интерфейс
- Или не вызывать вообще — просто иметь две системы навигации для разных команд

**Плюсы:**
- Минимум изменений кода
- Полная независимость, никаких конфликтов
- Можно сравнивать поведение двух патфайндеров

**Минусы:**
- Нет глубокой интеграции — altoclef не умеет вызывать Tungsten для своих задач
- Два отдельных JAR, отдельные настройки
- Версионный gap (1.21.8 vs 1.21.1) нужно устранить

---

### Вариант 2 — Замена Baritone на Tungsten (СЛОЖНЫЙ, РИСКОВАННЫЙ)

Заменить Baritone целиком: убрать зависимость на baritone, дописать в altoclef обёртку над Tungsten API.

**Что нужно сделать:**

1. **Убрать baritone** из `build.gradle`
2. **Добавить Tungsten** как зависимость (submodule или JAR)
3. **Создать адаптерный слой** — обёртка которая предоставляет baritone-совместимый API поверх Tungsten:
   ```java
   // Примерно такой фасад:
   public class TungstenBaritoneAdapter implements IBaritone {
       private PathFinder finder;
       private PathExecutor executor;

       public ICustomGoalProcess getCustomGoalProcess() { ... }
       public IGetToBlockProcess getGetToBlockProcess() { ... }
       // ... и так далее для каждого процесса
   }
   ```
4. **Воссоздать AltoClefSettings** — всё что altoclef пишет в `AltoClefSettings.getInstance()` нужно перенаправить в Tungsten
5. **Реализовать недостающее в Tungsten:**
   - Ломание блоков (Tungsten умеет только ходить)
   - Работа с инвентарём
   - MineProcess аналог
   - BuilderProcess аналог

**Объём работы:** ~2000-3000 строк нового кода адаптера

**Плюсы:**
- Потенциально более реалистичное движение (Tungsten имеет лучшую физику)
- Параллельный поиск пути быстрее

**Минусы:**
- Огромный объём работы
- Tungsten не умеет ломать/ставить блоки — половина altoclef сломается
- Версионный gap 1.21.1 → 1.21.8 большой (изменения в API блоков, реестров, движения)
- Нет гарантии что Tungsten вообще лучше для задач altoclef
- Высокий риск регрессий

---

### Вариант 3 — Гибридный: Tungsten для навигации, Baritone остаётся (СРЕДНИЙ)

Оставить Baritone, но добавить Tungsten как **альтернативный** патфайндер для определённых ситуаций.

**Идея:** altoclef решает когда какой патфайндер использовать:
- Baritone: когда нужно ломать блоки, строить, копать
- Tungsten: когда нужно быстро добраться до точки, PvP-манёвры, паркур

**Что нужно сделать:**

1. Добавить Tungsten в mono-repo как submodule или скопировать исходники
2. Обновить MC версию Tungsten до 1.21.1
3. Создать `TungstenNavigator` класс в altoclef:
   ```java
   public class TungstenNavigator {
       private PathFinder finder = new PathFinder();
       private PathExecutor executor = new PathExecutor(true);

       public void goTo(AltoClef mod, BlockPos target) {
           finder.find(mod.getWorld(), Vec3d.ofCenter(target), mod.getPlayer());
       }

       public boolean isActive() { ... }
       public void stop() { executor.stop = true; }
   }
   ```
4. Добавить в `BotBehaviour` поле `useTungsten` / `useBaritone`
5. В задачах где нужна быстрая навигация — переключаться на Tungsten

**Плюсы:**
- Получаем лучшее из обоих миров
- Baritone продолжает работать для всего что умеет
- Tungsten добавляет качество движения для PvP сценариев
- Умеренный объём работы (~500-800 строк)

**Минусы:**
- Два патфайндера могут конфликтовать (оба пытаются нажимать клавиши)
- Нужен чёткий mutex/переключатель между ними
- Версионный gap всё равно нужно устранить

---

### Вариант 4 — Mono-repo с shared settings (ИДЕАЛЬНЫЙ ДОЛГОСРОЧНЫЙ)

Объединить всё в одно Gradle multi-project:

```
AUTOCLEF_UPD/
  settings.gradle           ← includeBuild для всех
  altoclef/                 ← основной мод
  baritone_altoclef/        ← baritone с патчами
    baritone/               ← submodule cabaletta/baritone
  Tungsten/                 ← tungsten (обновлён до 1.21.1)
```

**`settings.gradle` в корне:**
```groovy
rootProject.name = 'autoclef-monorepo'
include ':altoclef'
include ':Tungsten'
includeBuild('baritone_altoclef/baritone') {
    dependencySubstitution {
        substitute module('cabaletta:baritone-unoptimized-fabric') using project(':fabric')
    }
}
```

**`altoclef/build.gradle` добавить:**
```groovy
dependencies {
    // Tungsten как проектная зависимость
    implementation project(':Tungsten')
    // Baritone через composite build
}
```

**Плюсы:**
- Один `gradle build` собирает всё
- IDE видит все исходники всех проектов
- Легко делать изменения в любом из компонентов
- Чистая структура

**Минусы:**
- Нужно унифицировать MC версии (Tungsten 1.21.8 → 1.21.1)
- Нужно настроить Gradle multi-project правильно (может быть нетривиально из-за loom)
- Tungsten не имеет публичного API — нужно будет делать интеграционный слой

---

## Версионный gap: 1.21.8 vs 1.21.1

Это реальная проблема. Tungsten написан на 1.21.8, altoclef на 1.21.1.

**Основные изменения MC API между 1.21.1 и 1.21.8:**
- `PlayerInput` изменился (Tungsten использует его в `Agent.java` и `PathExecutor.java`)
- Некоторые блочные API изменились
- Система регистрации (registry) слегка изменилась

**Трудоёмкость портирования Tungsten 1.21.8 → 1.21.1:** ~1-3 дня, зависит от глубины изменений MC API.

**Альтернатива:** Обновить altoclef до 1.21.8. Но это огромный объём работы из-за preprocess мультиверсии.

---

## Что реально уникального в Tungsten

После анализа кода:

1. **Параллельный A*** — `PathFinder.java` запускает поиск с коэффициентами `{1.5, 2, 2.5, 3, 4, 5, 10}` одновременно в thread pool, берёт лучший результат. Это быстрее для сложных маршрутов.

2. **Физическая симуляция** (`Agent.java`) — Tungsten симулирует физику игрока без доступа к серверу. Это позволяет искать пути которые используют инерцию, прыжки с разгона, etc.

3. **VoxelWorld** — кэш мира в собственной структуре данных, потокобезопасный. Поиск пути не блокирует main thread.

4. **CornerJump, LongJump** — специальные движения которых нет в стандартном Baritone.

5. **NeoJump** (`path/specialMoves/neo/`) — продвинутый прыжок с разворотом.

---

## Рекомендация

**Краткосрочно (сейчас):** Вариант 1 (отдельный мод). Можно быстро проверить насколько Tungsten вообще работает в сочетании с altoclef без каких-либо изменений кода. Нужно только решить версионный gap.

**Среднесрочно:** Вариант 3 (гибридный). Tungsten как альтернативный navigator для PvP-сценариев где важно реалистичное движение. Сохранить Baritone для всего остального.

**Долгосрочно:** Вариант 4 (mono-repo) — но только если активно разрабатываем Tungsten под нужды проекта.

**Вариант 2 (полная замена) — не рекомендую:** Tungsten слишком мало умеет (нет break/place), и объём работы несоразмерен выгоде.

---

## Оценка сложности

| Вариант | Сложность | Время |
|---------|-----------|-------|
| 1 — Отдельный мод | Низкая | 1-2 дня (версионный gap) |
| 2 — Замена Baritone | Очень высокая | 3-6 недель |
| 3 — Гибрид | Средняя | 1-2 недели |
| 4 — Mono-repo | Средняя | 3-5 дней (только инфраструктура) |

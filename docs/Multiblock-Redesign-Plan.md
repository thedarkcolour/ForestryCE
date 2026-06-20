# Multiblock System Redesign — Implementation Plan (rev 2)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Forestry's "Erogenous Beef" multiblock engine with a corruption-proof, declarative-pattern engine while keeping the Alveary and Farm player-experience identical.

**Architecture:** A declarative `MultiblockPattern` is validated statelessly on block-change/load events (no per-tick flood-fill, no merge). Each machine's shared state lives in one **holder** block's own NBT (the lowest member; base-class hosting), ticked by that block's `BlockEntityTicker`. Chunk events only flip an `assembled` flag. See `docs/Multiblock-Redesign-Spec.md` (the source of truth) and `docs/Multiblock-Debugging.md` (corruption catalogue; finding IDs A1/A2/B1/C1/C2/C3/D1/E1 are referenced as regression tests).

**Tech Stack:** Java 17, Minecraft Forge 1.20.1 (`net.neoforged.moddev.legacyforge` v2.0.80), JUnit 5 (new), Forge GameTest (new). Mod id `forestry`, package root `forestry`.

---

## How to read this plan

- **Spec is the source of truth.** Every task cites the spec section it implements (e.g. *(spec §6.4)*).
- **Two design constraints that shaped the phase order (read these — they explain rev 2):**
  1. **The cutover is semi-atomic.** `AlvearyController`/`FarmController` extend
     `RectangularMultiblockControllerBase → MultiblockControllerForestry → MultiblockControllerBase`. You
     **cannot** delete the old base before the two controllers are re-parented onto the new
     `MultiblockController`. So Phase 2 is a single *decomposed cutover* (re-parent → migrate call sites
     → delete), each sub-step ending on a **compiling** tree. There is no period where two engines both
     drive the same block (Task 2.5 stops feeding the old registry before the new triggers go live; the
     old auto-registered handlers become harmless no-ops until deleted in Task 2.7).
  2. **Minecraft splits testing into two layers.** The pure pattern logic (Phase 1) is written with **no
     `net.minecraft` imports** (its own `StructurePos`), so JUnit needs no Minecraft classpath — this
     sidesteps the moddev test-classpath and `Bootstrap` problems entirely. Everything touching the
     world (assembly, ticking, inventories, capabilities, drops, `ItemStack`) is verified with **Forge
     GameTest** (`/test runall`) plus **documented manual verification** for chunk-border unload/reload
     timing GameTest can't reproduce.
- **TDD per layer:** Phase 1 is strict JUnit TDD (red→green→commit). In-world layers: write the GameTest,
  implement, run `/test run forestry:<name>` → green, commit. Never commit red.
- **Commit after every green step.** **Branch:** all work on `feature/multiblock-redesign` off `1.20.1`.

## Build & test commands

> **JDK (REQUIRED for every Gradle call):** the system default is Java 26, which breaks the Gradle 9.2.1
> + moddev toolchain ("Unsupported class file major version 70"). Use **JBR 17** for all Gradle:
> `export JAVA_HOME=/home/thedarkcolour/.jdks/jbr-17.0.14` (deps are cached → run Gradle with `--offline`).

- Compile: `JAVA_HOME=/home/thedarkcolour/.jdks/jbr-17.0.14 ./gradlew compileJava --offline`
- Unit tests: `… ./gradlew test --offline` · Full build: `… ./gradlew build --offline`
- Dev client (GameTest + manual): `… ./gradlew runClient` → in a creative world: `/test runall` or `/test run forestry:<name>`.

---

## File structure

**New — pattern engine, MC-free (`src/main/java/forestry/core/multiblock/pattern/`)**
- `StructurePos.java` — `record StructurePos(int x,int y,int z)` (no `net.minecraft`); `Comparable` (x,y,z order).
- `StructureView.java` — interface: `CellSample sample(StructurePos)`, `boolean isLoaded(StructurePos)`; `record CellSample(boolean isComponent, Object component, boolean isWoodenSlab, boolean isSolidRender, String componentTypeId)`.
- `CellPredicate.java` — `@Nullable String test(CellSample)` (null = ok, else failure key).
- `MultiblockPattern.java` — cells, size ranges, predicates, post-checks, **maximality + loaded-shell** rule, `validate`, `candidateOrigins`.
- `PatternResult.java` — sealed `Match(members, min, max, holder, components)` | `Failure(cells)`.
- `Patterns.java` (apiculture/farming) — `ALVEARY_PATTERN`, `FARM_PATTERN` defined over the predicates.

**New — world adapter + host (`src/main/java/forestry/core/multiblock/`)**
- `LevelStructureView.java` — `StructureView` over a Forge `Level` (chunk-safe; `BlockPos↔StructurePos`).
- `MultiblockController.java` — the **new superclass** of `AlvearyController`/`FarmController`; absorbs
  `MultiblockControllerForestry` (owner, error logic, inventory plumbing) + hosting/ticker/buckets; **no**
  merge/assimilate/PAUSED.
- `MultiblockIndex.java` — per-`Level` main-thread map of active machines by holder pos (replaces the registry; no ticking/merge).

**Modified:** `forestry/api/multiblock/MultiblockTileEntityBase` (the generic base — note the **`api`** package; its `load`/`saveAdditional`/`setRemoved`/`onChunkUnloaded`/`onLoad` **and** the network methods `getUpdateTag`/`onDataPacket`/`handleUpdateTag` all currently delegate to `multiblockLogic` and must be reworked in Task 2.4), `forestry/core/multiblock/MultiblockTileEntityForestry`, `AlvearyController`, `FarmController`, `MultiblockLogicAlveary`/`Farm`, `FakeAlvearyController`/`FakeFarmController`, the trimmed `IMultiblockControllerInternal`/`IAlvearyControllerInternal`/`IFarmControllerInternal`/`IMultiblockLogic`, `BlockAlveary`, `FarmBlock`, `BlockStructure`, `PacketAlvearyChange`, `InventoryUtil` (add variant), `FarmFertilizerManager`/`FarmHydrationManager`, shared inventories, per-component tiles.

**Deleted (Task 2.7):** `MultiblockRegistry`, `MultiblockWorldRegistry`, `MultiblockServerTickHandler`, `MultiblockEventHandler`, `MultiblockControllerBase`, `MultiblockControllerForestry`, `RectangularMultiblockControllerBase`, `MultiblockLogic`, `MultiblockUtil.getNeighboringParts`.

**New tests:** `src/test/java/forestry/multiblock/…` (JUnit, MC-free); `src/main/java/forestry/multiblock/gametest/…` (`@GameTestHolder("forestry")`).

---

## Phase 0 — Branch + test harness (gating spikes)

### Task 0.1: Feature branch + commit docs
- [ ] **Step 1:** `git checkout -b feature/multiblock-redesign`
- [ ] **Step 2:** `./gradlew compileJava` → `BUILD SUCCESSFUL` (clean baseline).
- [ ] **Step 3:** `git add docs/Multiblock-Redesign-Spec.md docs/Multiblock-Redesign-Plan.md docs/Multiblock-Debugging.md && git commit -m "docs: multiblock redesign spec + plan + debugging analysis"`

### Task 0.2: JUnit source set (gated on an MC-free test)
**Files:** Modify `build.gradle`; Create `src/test/java/forestry/multiblock/SanityTest.java`
> The pure-logic layer imports **no** `net.minecraft` types, so the test source set needs **only** JUnit — not the Minecraft classpath. This is deliberate (avoids the moddev test-classpath problem).
- [ ] **Step 1:** Add to `dependencies { }`:
  ```groovy
  testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
  testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
  ```
  and top-level: `test { useJUnitPlatform() }`
- [ ] **Step 2:** `SanityTest` asserting `1+1==2` (no MC imports).
- [ ] **Step 3:** Run `./gradlew test` → SUCCESS, 1 passed.
- [ ] **Step 4 (gate):** Confirm the test source set does **not** pull MC: a test importing only `forestry.core.multiblock.pattern.*` + JUnit must compile. (If a future step needs a `net.minecraft` type in a unit test, **stop** — move that check to a GameTest instead; do not try to add the MC classpath to `sourceSets.test`.)
- [ ] **Step 5:** `git add build.gradle src/test/... && git commit -m "test: add JUnit 5 source set (MC-free unit layer)"`

### Task 0.3: GameTest harness + discovery spike (gating)
**Files:** Create `forestry/multiblock/gametest/MultiblockGameTests.java`, a registration hook; Modify `build.gradle` runs.
> Project has **zero** existing GameTests, so prove discovery works before building on it.
- [ ] **Step 1:** Holder class:
  ```java
  package forestry.multiblock.gametest;
  import net.minecraft.gametest.framework.*;
  import net.minecraftforge.gametest.*;
  @GameTestHolder("forestry") @PrefixGameTestTemplate(false)
  public class MultiblockGameTests {
      @GameTest(template = "forestry:empty3")
      public static void smoke(GameTestHelper h) { h.succeed(); }
  }
  ```
- [ ] **Step 2:** Register explicitly (belt-and-suspenders — `Forestry.java` has **no** `FMLCommonSetupEvent`
  hook, so create a **dedicated** MOD-bus subscriber class and ensure it is classloaded):
  ```java
  package forestry.multiblock.gametest;
  import forestry.api.ForestryConstants;
  import net.minecraftforge.event.RegisterGameTestsEvent;   // NOTE: net.minecraftforge.EVENT, not .gametest
  import net.minecraftforge.eventbus.api.SubscribeEvent;
  import net.minecraftforge.fml.common.Mod;
  @Mod.EventBusSubscriber(modid = ForestryConstants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
  public class GameTestRegistration {
      @SubscribeEvent static void register(RegisterGameTestsEvent e) { e.register(MultiblockGameTests.class); }
  }
  ```
  > Footprint note: with no separate test source set (legacyforge keeps `sourceSets.test` MC-free), these
  > GameTest classes + `.snbt` templates + this subscriber live in `src/main` and **ship in the release jar**
  > (inert unless `forge.enabledGameTestNamespaces` is set). Accept this, or add a build-time exclude in Phase 9.3.
- [ ] **Step 3:** Enable the namespace for dev runs — add to `runs { configureEach { } }`:
  `systemProperty 'forge.enabledGameTestNamespaces', 'forestry'`
- [ ] **Step 4:** Add a 3×3×3 air template `src/main/resources/data/forestry/structures/empty3.snbt` (size `[3,3,3]`, all `minecraft:air`).
- [ ] **Step 5 (gating spike, manual):** `./gradlew runClient`; in a creative world run `/test run forestry:smoke`. **It must report the test was found and passed.** If "no such test": the annotation scan didn't pick it up — confirm the `RegisterGameTestsEvent` hook runs (it must be on the MOD bus, in a class that's loaded). Do not proceed until green.
- [ ] **Step 6:** `git add ... && git commit -m "test: Forge GameTest harness + verified discovery"`

---

## Phase 1 — Pattern engine (MC-free, strict JUnit TDD)

*(spec §5)* No `net.minecraft` imports anywhere in this phase.

### Task 1.1: `StructurePos` + `StructureView` + `CellPredicate`
**Files:** Create the three; Test helper `FakeStructureView` (test sources).
- [ ] **Step 1:** Implement `record StructurePos(int x,int y,int z) implements Comparable<StructurePos>` (compare x then y then z); `offset(int,int,int)`. `StructureView` + `CellPredicate` as in File structure.
- [ ] **Step 2:** `FakeStructureView`: builder over a `Map<StructurePos,CellSample>`; `isLoaded` true unless a pos is explicitly marked unloaded.
- [ ] **Step 3:** `./gradlew compileTestJava` → SUCCESS. **Step 4:** commit `feat(multiblock): MC-free StructurePos/StructureView/CellPredicate`.

### Task 1.2: Predicates (TDD)
**Files:** Create `Predicates.java`; Test `PredicatesTest.java`
- [ ] **Step 1: Failing tests** for each predicate ↔ exact parity key. **Keys (verified against code/lang):** `for.multiblock.alveary.error.needSlabs`, `…needSpace`, `…needPlainOnTop`, `…needPlainInterior`, `for.multiblock.farm.error.needGearbox`, **`for.multiblock.farm.error.needPlainBand`** (farm level-2 band — `FarmController.isGoodForExteriorLevel`), `for.multiblock.farm.error.needPlainInterior`, `for.multiblock.error.small[.x/.y/.z]`, `…large[.x/.y/.z]`.
  **Two distinct "wrong block" keys — do not confuse them:** a cell inside the prism that is **not a Forestry component** → **`for.multiblock.error.invalid.interior`** (arg: the block; thrown by the base `isBlockGoodForInterior`/`isBlockGoodForExteriorLevel`, `MultiblockControllerBase:482,494`). A cell that **is** a component but of the **wrong controller type** → `for.multiblock.error.invalid.part` (arg: the localized machine type; `RectangularMultiblockControllerBase:77`).
- [ ] **Step 2:** `./gradlew test --tests '*PredicatesTest'` → FAIL.
- [ ] **Step 3:** Implement `Predicates` incl. a `componentOfType(String typeId, String failKey)` factory (matches on `CellSample.componentTypeId`, e.g. `"alveary_plain"`/`"farm_plain"`/`"farm_gearbox"`). **Step 4:** PASS. **Step 5:** commit.

### Task 1.3: `MultiblockPattern` + `PatternResult` + `validate` (TDD, incl. D1)
**Files:** Create both; Test `MultiblockPatternTest.java` *(spec §5.1, §5.2, §6.1)*
- [ ] **Step 1: Failing tests** over `FakeStructureView`:
  - Complete 3×3×3 component grid + plain interior/top + slab cap + air ring → `Match`; `holder == lowest member`; `members.size()==27`; bbox correct.
  - Missing one slab → `Failure`, first key `needSlabs`. Non-component interior cell → `Failure` (cube must be solid components, spec §5.1).
  - Farm 3×4×3 and 5×4×5 → `Match`; 2-wide → `Failure` (size); no gearbox → `Failure` `needGearbox`.
  - **D1 maximality:** a 3×4×3 candidate whose exterior shell contains a same-type farm component → **non-match** (spec §5.2). **Loaded-shell:** a valid grid with one cell (or a shell cell) marked **unloaded** → **non-match**.
- [ ] **Step 2:** FAIL.
- [ ] **Step 3:** Implement: cells `(Vec3i-like StructurePos offset, CellPredicate)`; optional per-axis size range; a post-match whole-structure hook (gearbox-present / slab / air-ring); `validate(view, origin)` runs predicates, then the **maximality + loaded-shell** check (spec §5.2): every pattern + consulted-shell cell must be `isLoaded`, and the growable-face shell must contain no same-type component. On success → `Match(members, min, max, holder=members.min, components)`.
- [ ] **Step 4:** PASS. **Step 5:** commit `feat(multiblock): MultiblockPattern.validate with maximality+loaded-shell (D1)`.

### Task 1.4: `candidateOrigins` (TDD)
- [ ] **Steps:** Failing test → `candidateOrigins(P) = { P − cellOffset }` over all size variants, deduped → implement → PASS → commit. *(spec §5.3)*

### Task 1.5: `ALVEARY_PATTERN` + `FARM_PATTERN` (TDD)
**Files:** Create `forestry/apiculture/multiblock/AlvearyPattern.java`, `forestry/farming/multiblock/FarmPattern.java` (still MC-free — they reference `Predicates` + type-id strings); Tests for each.
- [ ] **Step 1:** Failing JUnit tests modeling `AlvearyController.isMachineWhole` / `FarmController.isMachineWhole` parity (each error path → its key; valid → Match; farm size range + needGearbox + needPlainBand).
- [ ] **Step 2–4:** FAIL → implement the two patterns → PASS. **Step 5:** commit `feat(multiblock): declarative Alveary/Farm patterns with parity keys`.

---

## Phase 2 — The cutover (decomposed; each sub-task ends compiling)

*(spec §6, §7, §10)* This replaces the old engine. Re-parent the controllers **before** deleting the old base.

### Task 2.1: `MultiblockController` (the new superclass) + `LevelStructureView` + `MultiblockIndex`
**Files:** Create the three (MC-coupled now).
- [ ] **Step 1:** `MultiblockController` (abstract): absorbs `MultiblockControllerForestry`'s surface (`getOwnerHandler`/`getErrorLogic`/inventory plumbing/`getWorldObj`) **and** adds: `members`, `min`/`max`, `assembled`, component buckets, machine `owner`; `getReferenceCoord()` = lowest member (never null/ZERO, spec §6.1), `getCenterCoord`/`getTopCenterCoord` from bbox; abstract `serverTick`/`clientTick`/`writePayload`/`readPayload`/`onAssembled`/`onBroken`/`getUnlocalizedType`. **No** `assimilate`/`onAssimilate`/`checkForDisconnections`/PAUSED.
- [ ] **Step 2:** `LevelStructureView` (chunk-safe reads; sets `componentTypeId` from the tile). `MultiblockIndex` (per-`Level` map, main thread).
- [ ] **Step 3:** `./gradlew compileJava` → SUCCESS (new classes are inert; nothing extends/uses them yet).
- [ ] **Step 4:** commit `feat(multiblock): MultiblockController superclass + LevelStructureView + index`.

### Task 2.2: Re-parent `AlvearyController` (incl. E1 owner-vote-once)
**Files:** Modify `AlvearyController.java`, `MultiblockLogicAlveary.java`, `FakeAlvearyController.java`.
*(spec §3.1 E1, §8.2)*
- [ ] **Step 1: Reshape the interfaces first (else the controller won't compile after method deletion).**
  `AlvearyController implements IAlvearyControllerInternal`, which (with `IMultiblockControllerInternal`)
  **mandates** the methods this cutover deletes: `assimilate`, `_onAssimilated`, `onAssimilated`,
  `checkForDisconnections`, `detachAllBlocks`, `updateMultiblockEntity`, `shouldConsume`, `attachBlock`,
  `detachBlock`, `checkIfMachineIsWhole`, `auditParts`, `recalculateMinMaxCoords`, `getDestroyedCoord`.
  In this same commit, **strip those from `IMultiblockControllerInternal` and `IAlvearyControllerInternal`**
  (keep only what the new engine needs: owner/error/inventory/climate accessors, `getReferenceCoord`,
  `write/read` → rename to payload, `getUnlocalizedType`). Fix the analogous Farm interface in Task 2.3.
- [ ] **Step 2:** Change `extends RectangularMultiblockControllerBase` → `extends MultiblockController`. Move the `serverTick`/`clientTick` bodies into the controller's tick methods (logic **unchanged**: keep climatiser-before-`canWork` ordering + temp/humidity reset, spec §8.2). `write`/`read` → `writePayload`/`readPayload`. Delete `isMachineWhole`/`assimilate`/`onAssimilate`. Bucket components from the member set (port `onBlockAdded` logic; re-add the constructor-seeded `AlvearyBeeModifier` on re-bucket, spec §8.2). **Owner (E1):** compute the majority-vote owner **once at first formation**, store it in the payload, and **do not** re-vote on reassembly/reload (spec §3.1).
- [ ] **Step 3: Define the logic-class contract** (it's deleted in Task 2.7, so pin its replacement now).
  `MultiblockLogicAlveary` currently `extends MultiblockLogic<…>`. After `MultiblockLogic` is removed, it
  becomes a thin object that implements the trimmed `IMultiblockLogic` directly; **`getController()`
  resolves the controller via the owning BE's `anchorPos` through `MultiblockIndex`** (returning
  `FakeAlvearyController.INSTANCE` when unassembled/anchor-missing). Update `FakeAlvearyController` to the
  trimmed interface. (You may keep `getMultiblockLogic()` as the public accessor — only its internals change.)
- [ ] **Step 4:** `./gradlew compileJava` → SUCCESS (old base still exists, now used only by `FarmController`).
- [ ] **Step 5:** commit `refactor(alveary): re-parent onto MultiblockController; reshape interfaces; E1 owner-once`.

### Task 2.3: Re-parent `FarmController`
**Files:** Modify `FarmController.java`, `MultiblockLogicFarm.java`, `FakeFarmController.java`.
- [ ] **Step 1:** As Task 2.2 for the Farm (keep the `serverTick` `//FIXME return true`; socket/circuit logic intact; `writePayload`/`readPayload`; valve routes to the shared tank; E1 owner-once). Delete `isMachineWhole`/`assimilate`/`onAssimilate`.
- [ ] **Step 2:** `./gradlew compileJava` → SUCCESS (now **neither** controller extends the old base).
- [ ] **Step 3:** commit `refactor(farm): re-parent onto MultiblockController; relocate tick/persistence; E1 owner-once`.

### Task 2.4: Base BE rework — holder, ticker, anchorPos, callbacks, stash
**Files:** Modify `MultiblockTileEntityBase`, `MultiblockTileEntityForestry`; add `getTicker` to `BlockAlveary`/`FarmBlock`.
*(spec §6.1, §6.4, §7.1, §7.3)*
- [ ] **Step 1:** Store `@Nullable BlockPos anchorPos`; `getController()` resolves the holder BE; `saveAdditional`/`load` call `getInternalInventory().read/write` **only when `getBlockPos().equals(anchorPos)`** (holder-gating). Add the static `BlockEntityTicker` guarded by `pos.equals(anchorPos) && controller.assembled`. Add the legacy/payload **stash** field round-tripped in `saveAdditional`/`load` (spec §6.4). Add the `unloading` flag (set in `onChunkUnloaded`, checked in `setRemoved`). Retain per-part `onMachineAssembled`/`onMachineBroken` calls (now invoked by the controller on transitions, spec §7.3). **Remove** the old `multiblockLogic.validate/invalidate/onChunkUnload/read/write` save-delegate calls (this stops feeding the old registry — the old auto-registered handlers now no-op).
- [ ] **Step 2:** `getTicker()` on `BlockAlveary`/`FarmBlock`. **The holder may be ANY member type** (it's
  the lowest member — could be a heater, sieve, gearbox…), and each member has its own `BlockEntityType`
  (`alveary`, `alveary_sieve`, `alveary_swarmer`, `alveary_hygroregulator`, `alveary_stabiliser`,
  `alveary_fan`, `alveary_heater`; farm `plain/gearbox/hatch/valve/control`). Return the ticker for
  **every** member type (cast the BE to the shared base `MultiblockTileEntityForestry`/`TileAlveary`/
  `TileFarm`) — do **not** key it to one concrete type, or a machine whose holder is an upgrade block
  never ticks. (The `once_per_tick` test in Task 3.1 uses all-plain blocks and would NOT catch this.)
- [ ] **Step 3:** `./gradlew compileJava` → SUCCESS.
- [ ] **Step 4:** commit `refactor(multiblock): holder-hosted BEs, anchor-only ticker, stash, break/unload flag`.

### Task 2.4b: Re-anchor hand-off, canonicalization, and drop gating (the §6.4 algorithm — explicit)
**Files:** Modify `MultiblockTileEntityForestry` (setRemoved hand-off), `MultiblockController` (canonicalize), `forestry/core/blocks/BlockStructure.java` (drop gating).
*(spec §6.1 single-holder + canonicalization, §6.4 re-anchor, §6.3 drops — do NOT defer this to "implement any gap")*
- [ ] **Step 1 — re-anchor hand-off (genuine holder break, `unloading==false`):** (1) resolve the lowest-
  `(x,y,z)` **currently-loaded** surviving member; (2) hand the payload to it synchronously, re-point the
  shared-inventory back-reference (§6.2), and **force-mark the old holder's chunk dirty** so its stale copy
  is dropped; (3) if no survivor is loaded, **force-load the nearest survivor's chunk** (`getChunk(cx,cz,
  ChunkStatus.FULL, true)`) and hand off; only if no survivor exists on disk → full-dismantle drop. Make it
  **idempotent across multiple removals in one tick** (re-resolve on each `setRemoved`).
- [ ] **Step 2 — canonicalization (spec §6.1):** on every full (all-members-loaded) (re)assembly, if the
  current holder ≠ the lowest member, move the payload to the lowest member, re-point the back-ref, and
  force-mark the old holder's chunk dirty. (Keeps holder == reference coord in steady state.)
- [ ] **Step 3 — drop gating (spec §6.3, fixes a severe parity bug):** rewrite `BlockStructure.onRemove`
  so the **shared**-inventory drop happens **only on full dismantle** (last member) at the final position —
  because after the cutover `getInternalInventory()` on a plain/holder block resolves to the **shared**
  controller inventory, the current unconditional `Containers.dropContents(getInternalInventory())` would
  dump the whole machine's inventory on **any** single-block break. **Keep** the per-block own-inventory
  drop for sieve/swarmer/hygro (they override `getInternalInventory()` to their own). (Note: spec §13's
  "keep `onRemove`" refers to the per-block branch; the shared branch changes.)
- [ ] **Step 4:** `./gradlew compileJava` → SUCCESS (GameTests for these land in Phase 3).
- [ ] **Step 5:** commit `feat(multiblock): re-anchor hand-off, holder canonicalization, shared-drop gating`.

### Task 2.5: Wire validation triggers
**Files:** Modify `MultiblockTileEntityForestry` (onLoad/setRemoved → validate via `MultiblockIndex`), `BlockAlveary.neighborChanged`, `PacketAlvearyChange`.
*(spec §5.3)*
- [ ] **Step 1:** `onLoad`/place → `pattern.candidateOrigins(pos)` → `validate(LevelStructureView)` → assemble/update the index; `setRemoved`/break → deactivate + re-validate neighbors; `onChunkUnloaded` → deactivate (no re-anchor; `unloading` set). Rewrite `BlockAlveary.neighborChanged` to call `validate` for the affected origin (delete the `controller.reassemble()` call). Re-point `PacketAlvearyChange` at a client-side `validate`. *(Pattern lookup per block: alveary blocks → `ALVEARY_PATTERN`; farm blocks → `FARM_PATTERN`.)*
- [ ] **Step 2:** `./gradlew compileJava` → SUCCESS. The new engine is now live.
- [ ] **Step 3:** commit `feat(multiblock): event-driven validation triggers (load/break/neighbor)`.

### Task 2.6: Migrate remaining `getController()` call sites
**Files:** Audit + fix every `getMultiblockLogic().getController()` consumer.
- [ ] **Step 1:** Grep `getMultiblockLogic().getController()` and fix per subsystem so each resolves through the new controller: `getInternalInventory()` (TileAlveary/TileFarm trees), `getCapability` (the per-block caps), `isHighlighted`, `BlockStructure.use`, the GUIs (`ContainerAlveary`/`ContainerFarm` + the sieve/swarmer/hygro containers), `PacketAlvearyChange`. Keep behavior identical for now (parity fixes land in Phase 4/5/6).
- [ ] **Step 2:** `./gradlew compileJava` → SUCCESS.
- [ ] **Step 3:** commit `refactor(multiblock): migrate controller call sites to the new engine`.

### Task 2.7: Delete the dead old engine
**Files:** Delete `MultiblockRegistry`, `MultiblockWorldRegistry`, `MultiblockServerTickHandler`, `MultiblockEventHandler`, `MultiblockControllerBase`, `MultiblockControllerForestry`, `RectangularMultiblockControllerBase`, `MultiblockLogic`, and `MultiblockUtil.getNeighboringParts`.
- [ ] **Step 1:** Delete; fix any last references.
- [ ] **Step 2:** `./gradlew compileJava` → SUCCESS (full tree compiles on the new engine).
- [ ] **Step 3:** `./gradlew test` (Phase 1 units still green). **Step 4:** commit `refactor(multiblock): remove flood-fill/merge/registry engine`.

---

## Phase 3 — In-world behavior GameTests (engine now assembles)

*(spec §5.2, §6.4, §7)* Each: add the `@GameTest`, implement any gap, `/test run forestry:<name>` → green, commit.

- [ ] **Task 3.1 `once_per_tick`** — 3×3×3 of plain blocks; assert the machine logic runs exactly once/tick (anchor guard, spec §7.1).
- [ ] **Task 3.2 `assemble_fires_visuals`** — alveary entrance blockstate flips on assemble/break; farm BAND/redstone via `updateNeighborsAt` (spec §7.3).
- [ ] **Task 3.3 `reload_reactivates`** — assemble, simulate member unload+reload (BE hooks), assert reassembly with state intact (spec §5.3; guards the reload blocker).
- [ ] **Task 3.4 `two_machines_independent`** — bridge two complete alvearies; assert no merge, no inventory loss (spec §7; audit B1).
- [ ] **Task 3.5 Re-anchor & drops (spec §6.3/§6.4 — several GameTests):**
  - `reanchor_on_break` — break the holder with survivors loaded; assert single-holder + payload preserved.
  - `reanchor_holder_is_upgrade` — build a machine whose lowest member is an upgrade block (e.g. a bottom-
    corner heater); assert the upgrade-block holder round-trips the shared payload through save/reload.
  - `reanchor_multi_break` — remove the holder + other low blocks in one operation (e.g. `helper`-driven
    rapid breaks / a simulated explosion); assert exactly one loaded member holds the payload after **each**
    removal (idempotency).
  - `drop_on_full_dismantle` — break to the last block; assert shared inv **+ farm sockets** drop at the
    final position, and that breaking a *non-last* plain block does **not** dump the shared inventory
    (verifies the Task 2.4b onRemove gating).
  - `unload_is_not_break` — call `onChunkUnloaded` then `setRemoved` on a member; assert **no** drop/re-anchor.
  - **Negative parity** — after breaking a swarmer/hygroregulator, assert `pendingSpawns`/tank fluid are
    **not** in world drops (spec §3.1).
  - `reanchor_survivor_unloaded` — **if GameTest cannot reproduce the unloaded-survivor force-load timing,
    route this to documented manual verification (Task 9.2)** rather than dropping it.
- [ ] **Task 3.6 `d1_no_subprism`** — build a 5×4×3 farm; assert no 3×4×3 sub-machine forms (maximality). Simulate one chunk-column unloaded (mark cells unloaded via a test `StructureView`, or break+withhold), assert no partial assembly (loaded-shell). *(spec §5.2)*

Commit after each green test.

---

## Phase 4 — Persistence hardening (corruption fixes)

*(spec §6.2, §7.2, §8.3)*

- [ ] **Task 4.1 Clearing read (GameTest — needs `ItemStack`, so not JUnit).** Add `InventoryUtil.readFromNBTClearing(Container,String,CompoundTag)` that clears the destination **before** the tag-absent check (spec §6.2). Leave the global `readFromNBT` untouched. GameTest: seed a shared inventory, save a tag with that slot absent, read → slot empty (no ghost). Commit `feat(multiblock): clearing read for shared inventories (C2/F1)`.
- [ ] **Task 4.2 A2 holder dirtying (GameTest).** Shared inventories use the clearing read + hold a re-pointable `holderBE` back-ref whose `setChanged()` dirties the holder (re-pointed on canonicalize/re-anchor); member `setChanged()` resolves+dirties the holder. GameTest `gui_edit_persists_via_anchor`: edit a slot from a **non-holder** block (different chunk), save+reload, assert persisted (audit A2). Commit.
- [ ] **Task 4.3 Key-guarded managers (could be JUnit if MC-free; else GameTest).** `FarmFertilizerManager`/`FarmHydrationManager.read` guard with `data.contains(key)` (spec §6.2; C3). Test: read a tag missing `StoredFertilizer` → value unchanged. Commit.
- [ ] **Task 4.4 Per-component `setChanged` (A1) + cached shared caps (C2) (GameTest).** Each climatiser/hygro/swarmer/gearbox calls `setChanged()` on **its own** BE when mutating energy/fluid/heatTicks/pendingSpawns (spec §8.3). Convert shared caps (`TileAlveary` ITEM_HANDLER, `TileFarmValve` FLUID, `TileFarmHatch` ITEM_HANDLER) to **cached** `LazyOptional`s invalidated on the assembled→deactivated flip + `setRemoved`; keep own caps unconditional (spec §7.2). GameTests `idle_alveary_persists_resources` (A1) + `shared_cap_invalidated_on_deactivate`. Commit each.

---

## Phase 5 — Alveary parity GameTests

*(controller already relocated in Task 2.2; pattern in 1.5)*
- [ ] **Task 5.1 `alveary_full_function`** — assemble 3×3×3 + heater/fan/hygro/sieve/swarmer/stabiliser + queen; assert breeding, climate steps, products; sieve/swarmer/hygro open **own** GUIs, plain/heater/stabiliser the **shared** GUI; ITEM_HANDLER exposes the shared bee inv on plain/heater/fan/stabiliser and own inv on sieve/swarmer/hygro (spec §8.1). Commit.
- [ ] **Task 5.2 `alveary_climate_origin`** — assert climate sampling uses the bbox center / reference coord (member-set-derived), unchanged after reload (spec §6.1). **Use a geometry where the lowest member is deliberately NOT the geometric center** (so the test actually distinguishes member-set-derived from anchor-derived coords — otherwise it can pass even if center is wrongly computed from the holder). Commit.

## Phase 6 — Farm parity GameTests
- [ ] **Task 6.1 `farm_full_function`** — assemble 5×4×5 + gearbox/hatch/valve/control; charge gearbox, fill valve (shared tank), socket a circuit; assert farming work, NO_POWER error logic, hydration; per-`Active` tick offsets random-not-zero (spec §8.2); `getCoords()` member-set-derived (spec §6.1). Commit.

---

## Phase 7 — Migration (no data loss)

*(spec §10)*
- [ ] **Task 7.1 Legacy carrier tie-break (JUnit, MC-free).** `LegacyMigration.chooseCarrier(Map<StructurePos,Boolean nonEmpty>)` returns the lowest non-empty carrier + the rest to discard (abstract over positions/non-emptiness, no `CompoundTag`). TDD. Commit.
- [ ] **Task 7.2 Adoption wiring (GameTest + manual).** Stash legacy `multiblockData` on load; adopt at **load-time validation** (Task 2.5) via `chooseCarrier` + `readPayload` (clearing read); **owner adopted verbatim** from the legacy `ownerHandler` (spec §10); discard other carriers; write new format thereafter. GameTest `migrate_single` (construct a legacy tag in-test). **Manual:** load a real pre-update world with a filled cross-border alveary/farm; relog; assert contents/owner intact. Commit.
- [ ] **Task 7.3 E1 non-migration owner test (GameTest).** Build a fresh structure, partially unload/reload, assert owner unchanged (vote-once-at-formation held; spec §3.1 — distinct from 7.2's verbatim path). Commit.

---

## Phase 8 — Feedback & Patchouli

*(spec §11)*
- [ ] **Task 8.1 (manual).** `BlockStructure.use` calls `PatternValidator` directly for the failure key (works for a lone block); `isHighlighted` resolves the reference coord from the member set (Fake/unassembled → no highlight). Manual-verify the chat message + spectacle highlight. Commit.
- [ ] **Task 8.2 (manual).** Patchouli `IMultiblock` export from the pattern (alveary fixed; farm canonical min example), behind a Patchouli-present guard (no runtime dependency in validation). Verify previews render. Commit.

---

## Phase 9 — Regression collection, chunk-border verification, cleanup

- [ ] **Task 9.1** Cross-check spec §14 line-by-line; ensure each audit finding (A1,A2,B1,C1,C2,C3,**D1**,E1) has a green GameTest/JUnit; `/test runall` all green. Commit.
- [ ] **Task 9.2 Manual chunk-border verification** → `docs/Multiblock-Redesign-Manual-Verification.md`: build both machines straddling a chunk border (F3+G); fill with identifiable contents; `/forceload` one half, walk away so the other unloads, return; relog; repeat in varying order. Assert no loss/dup/rollback, no fertilizer/hydration zeroing, correct owner, and that the deleted classes' error log lines never appear. Record results. Commit.
- [ ] **Task 9.3 Final build + sweep.** `./gradlew build` → SUCCESS; remove dead imports/refs; full manual play-test (build/break/upgrade/automation/GUIs/climate/redstone/drops parity). Commit. Then use superpowers:finishing-a-development-branch.

---

## Definition of done

- All JUnit + GameTests pass (`./gradlew test`; `/test runall`); `./gradlew build` SUCCESS.
- Manual chunk-border verification (9.2) shows no corruption across unload/reload cycles.
- Every spec §14 regression test exists and passes; every audit finding (A1/A2/B1/C1/C2/C3/D1/E1) has a green test or documented manual check.
- Player-facing parity confirmed (9.3): blocks, upgrades, GUIs, caps, drops, climate, redstone, visuals, error messages identical.
- Legacy worlds migrate with no data loss (7.2).

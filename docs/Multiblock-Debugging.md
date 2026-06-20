# Forestry CE — Multiblock System (Debugging Reference)

> Scope: `forestry.core.multiblock` (engine), `forestry.apiculture.multiblock` (Alveary),
> `forestry.farming.multiblock` (Farm), and `forestry.api.multiblock` (public contracts).
> Target: Minecraft **Forge 1.20.1** (Forge 47.3.10).
>
> Purpose: explain how the system works in enough detail to debug it, then catalogue the
> failure modes that corrupt persistent data — with emphasis on structures that straddle a
> **chunk border** — and contrast the design with GregTech's hatch/bus approach.

### Start here — symptom → section

| Symptom | Most likely | Section |
|---------|-------------|---------|
| Inventory/progress **rolled back** after relog | one migrating save-delegate; dirty-marking missed | §6, §7 A1/A2/C1 |
| Items **duplicated / reappear** after reload | additive non-clearing `read()` re-seeding a snapshot | §6.2, §7 C2 |
| Whole machine's contents **wiped/empty** after joining another machine | empty `onAssimilate` on merge | §7 B1 |
| Farm **fertilizer/hydration reset to 0** | empty/partial second snapshot + unconditional `getInt` | §7 C3 |
| Machine **won't reassemble / acts smaller** near a chunk edge | re-assembly on shrunken footprint | §5.4, §7 D1 |
| `…same reference coord…both have valid parts` in log | split-brain controllers | §9.1, §7 C |
| **Ownership** changed after reload | owner re-vote over loaded parts | §7 E1 |
| Client GUI shows **phantom items** (server is fine) | non-clearing client packet decode | §7 F1 |

A 60-second mental model: **all shared state lives in one block's NBT (the "save delegate"),
whose identity can move between chunks, and controllers merge by throwing one side's data
away.** Almost every bug below is a consequence of those two facts. Start at §6 (persistence)
and §7.0 (the PAUSE safeguard and its three holes).

---

## 1. Lineage & one-paragraph mental model

This is a fork of the **"Erogenous Beef" multiblock library** (the engine behind Big
Reactors / Extreme Reactors). The author tag is still on `MultiblockWorldRegistry` and
`MultiblockRegistry`. The core idea: a group of adjacent tile entities ("parts" /
"components") is governed by a single **controller** object — a *meta-tile-entity* that is
**not** itself a block in the world. The controller is built up at runtime by a flood-fill
over neighbouring parts, owns all the shared game state (inventory, progress, climate…),
and **persists that shared state into the NBT of exactly one of its parts** — the *save
delegate*. Parts merge their controllers when they touch (`assimilate`) and split them when
they're cut (`checkForDisconnections`). Almost every chunk-border bug in this system traces
back to two facts: (a) **all shared state lives in one block's NBT**, and that block's
identity can move; and (b) **controllers merge into a single logical entity**, and the merge
path throws data away.

---

## 2. Glossary

| Term | Meaning | Where |
|------|---------|-------|
| **Part / Component** (`IMultiblockComponent`) | A `BlockEntity` that participates in a multiblock (e.g. `TileAlveary`, `TileFarmPlain`). | `api/multiblock/IMultiblockComponent.java` |
| **Logic** (`IMultiblockLogic` / `MultiblockLogic`) | Per-part state machine that wires the part's BE callbacks into the engine and holds the part's cached NBT + flags. | `core/multiblock/MultiblockLogic.java` |
| **Controller** (`IMultiblockControllerInternal` / `MultiblockControllerBase`) | The runtime meta-entity that owns the assembled machine's shared state and game logic. **Not a block.** One per assembled structure. | `core/multiblock/MultiblockControllerBase.java` |
| **Reference coord** | The connected part with the lowest `(x, then y, then z)`. Deterministically identifies the controller and designates the save delegate. | `MultiblockControllerBase` field `referenceCoord` |
| **Save delegate** | The single part whose NBT carries the serialized controller state, under the `"multiblockData"` tag. Normally the part at the reference coord. | `MultiblockLogic.saveMultiblockData` |
| **Assembly state** | `DISASSEMBLED` / `ASSEMBLED` / `PAUSED`. Only `ASSEMBLED` runs game logic. | `MultiblockControllerBase.AssemblyState` |
| **World registry** | Per-`Level` bookkeeping: the orphan queue, merge detection, dirty/dead controller sets, the awaiting-chunk-load map. | `core/multiblock/MultiblockWorldRegistry.java` |
| **Registry** | Static singleton mapping each `Level` → its `MultiblockWorldRegistry`. | `core/multiblock/MultiblockRegistry.java` |
| **Orphan** | A part with no controller that wants one this tick. | `MultiblockWorldRegistry.orphanedParts` |

---

## 3. File map (responsibility per class)

**Engine (`forestry/core/multiblock`)**

| File | Responsibility |
|------|----------------|
| `MultiblockControllerBase` | The heart. `attachBlock` / `detachBlock` / `assimilate` / `checkForDisconnections` / `checkIfMachineIsWhole` / `updateMultiblockEntity`; owns `referenceCoord`, `minimumCoord`/`maximumCoord`, `assemblyState`, `connectedParts`. |
| `MultiblockControllerForestry` | Adds owner handling + error logic + a fake inventory. Base `write`/`read` persist **only the owner**. |
| `RectangularMultiblockControllerBase` | `isMachineWhole`: the bounding-box size + per-block validation walk for "big cube" machines. |
| `MultiblockWorldRegistry` | Per-world tick bookkeeping: `processMultiblockChanges` (orphan attach → controller create/merge → split/assembly checks → dead cleanup), the chunk-load queues, the mutex-guarded orphan/awaiting sets. |
| `MultiblockRegistry` | Static dispatcher keyed by `Level`; entry points `tickStart`, `onChunkLoaded`, `onPartAdded`, `onPartRemovedFromWorld`, `onWorldUnloaded`, `addDirty/DeadController`. |
| `MultiblockLogic` | Per-part: caches `"multiblockData"` on load, writes it back **iff** save delegate; holds `saveMultiblockData`/`visited` flags; routes `validate`/`invalidate`/`onChunkUnload`. |
| `MultiblockServerTickHandler` | `@SubscribeEvent` on `LevelTickEvent.START` → `MultiblockRegistry.tickStart`. |
| `MultiblockEventHandler` | `ChunkEvent.Load` → `onChunkLoaded`; `LevelEvent.Unload` → `onWorldUnloaded`. |
| `MultiblockUtil` | `getNeighboringParts` (chunk-safe on server only); component/logic/controller lookups. |
| `MultiblockValidationException` | Thrown by `isMachineWhole` with a translation key + optional pos. |
| `MultiblockTileEntityForestry` | BE base for Forestry parts: inventory plumbing + owner NBT. |
| `IMultiblockSizeLimits` | Min/max X/Y/Z and min block count per machine type. |

**Public API (`forestry/api/multiblock`)**

`MultiblockTileEntityBase` is the BE-side glue: it maps `onLoad → validate`,
`setRemoved → invalidate`, `onChunkUnloaded → onChunkUnload`, `load/saveAdditional →
logic.readFromNBT/write`, and the description-packet methods. **This is the seam between
Minecraft's BlockEntity lifecycle and the multiblock engine.**

**Alveary (`forestry/apiculture/multiblock`)** — `AlvearyController` (state: bee inventory,
beekeeping logic, climate steps), `TileAlveary*` parts, `MultiblockLogicAlveary`,
`AlvearyMultiblockSizeLimits` (fixed 3×3×3, ≥27 blocks), `FakeAlvearyController`.

**Farm (`forestry/farming/multiblock`)** — `FarmController` (state: sockets/circuits,
hydration + fertilizer + tank managers, farm inventory), `MultiblockLogicFarm`,
`FarmMultiblockSizeLimits` (3×4×3 … 5×4×5, ≥36 blocks), `FakeFarmController`.

---

## 4. The two concrete machines

### Alveary — `AlvearyController`, `AlvearyMultiblockSizeLimits`
- Footprint: **exactly 3×3×3** alveary blocks (≥27), plus extra non-cube checks:
  - The whole 3×3 layer at `maxY + 1` must be **wooden slabs** (`BlockTags.WOODEN_SLABS`).
    (`AlvearyController.isMachineWhole`, slab loop.)
  - The ring around the top layer (`minX-1 … maxX+1`, `minZ-1 … maxZ+1`, at `y = maxY`)
    must not be solid-render (entrances need air). **This ring reaches one block outside the
    footprint — into potentially different chunks.**
- Per-block rules (`isGoodForExteriorLevel` / `isGoodForInterior`): the top exterior level
  (`level == 2`) and the entire interior must be `TileAlvearyPlain`; the lower two exterior
  levels may be any alveary block (plain or upgrade: heater/fan/hygro/sieve/swarmer/…).

### Farm — `FarmController`, `FarmMultiblockSizeLimits`
- Footprint: rectangular prism, **X∈[3,5], Y=4, Z∈[3,5]**, ≥36 blocks; requires ≥1
  `TileFarmGearbox`.
- Per-block rules: the band at exterior `level == 2` and the entire interior must be
  `TileFarmPlain`; other positions may be farm blocks or upgrades (gearbox/hatch/valve/control).

Both validators run through `RectangularMultiblockControllerBase.isMachineWhole`, which:
1. Rejects on block count / bounding-box dimensions.
2. Walks **every coordinate** in `[min..max]`, reads the tile via `TileUtil.getTile` and (for
   non-component slots) `level.getBlockState`, and applies the per-block validators.
   **The cube must be solid Forestry components.** The `part == null` branch carries a code
   comment claiming non-multiblock interior blocks are "permitted", but that comment is **stale**:
   it sets `part = null` and then calls `isBlockGoodForInterior(Level,BlockPos)` /
   `isBlockGoodForExteriorLevel(int,Level,BlockPos)`, whose base implementations in
   `MultiblockControllerBase` **throw** and which neither `AlvearyController` nor `FarmController`
   overrides. So any non-component block inside `[min..max]` fails validation. The only
   legitimately non-Forestry cells are the Alveary's wooden-slab cap and entrance air ring, which
   are checked **outside** the prism walk (in `AlvearyController.isMachineWhole`).

---

## 5. Lifecycle & control flow

### 5.1 The per-tick pipeline (server)
`LevelTickEvent.START` → `MultiblockRegistry.tickStart(level)` →
`MultiblockWorldRegistry.processMultiblockChanges()` **then** `tickStart()`:

`processMultiblockChanges()` runs, in order:
1. **Orphan resolution** — drains `orphanedParts` (under mutex). For each orphan in a loaded
   chunk that still exists: `attachToNeighbors(orphan)`.
   - 0 compatible controllers → **create a new controller** and attach (the *only* place new
     controllers are created).
   - ≥2 compatible controllers → record a **merge pool** (the *only* place merges are
     detected).
2. **Merge** — each merge pool elects the master with the lowest reference coord
   (`shouldConsume`), then `master.assimilate(other)` for every other controller; consumed
   controllers are marked dead.
3. **Split / assembly** — for every **dirty** controller: `checkForDisconnections()`
   (flood-fill from the reference part; prune anything not reachable or in an unloaded chunk),
   then `recalculateMinMaxCoords()` + `checkIfMachineIsWhole()`.
4. **Dead cleanup** — unregister empty controllers (the *only* place controllers are
   unregistered); fire `onDestroyed(destroyedCoord)` so a torn-down machine can drop its
   inventory.
5. **Detached → orphan** — parts shed this tick are asserted-detached and moved to the orphan
   queue for next tick.

`tickStart()` then calls `updateMultiblockEntity()` on every live controller (game logic).

> Key timing invariant: **structure changes are processed at the _start_ of the world tick,
> before block entities tick.** Orphans always wait at least one tick before attaching.

### 5.2 Adding a part (placement OR chunk load)
`BlockEntity.onLoad` → `MultiblockTileEntityBase.onLoad` → `logic.validate` →
`MultiblockRegistry.onPartAdded` → `MultiblockWorldRegistry.onPartAdded`:
- If the part's **own chunk** isn't loaded yet → parked in `partsAwaitingChunkLoad[chunkHash]`
  (mutex-guarded; can be written from off-thread chunk loads).
- Else → pushed onto `orphanedParts` for next tick.

`MultiblockEventHandler.onChunkLoad` (`ChunkEvent.Load`) → `onChunkLoaded(cx,cz)` moves any
parked parts for that chunk into the orphan queue.

### 5.3 Removing a part (block broken / replaced)
`BlockEntity.setRemoved` → `MultiblockTileEntityBase.setRemoved` → `logic.invalidate` →
`detachSelf(chunkUnloading = false)` → `controller.detachBlock(part, false)`:
- Not a chunk unload, so the machine goes from `ASSEMBLED` toward disassembly via the normal
  dirty → `checkIfMachineIsWhole` path; if the last part is removed, the controller is marked
  dead and remembers `destroyedCoord` so `onDestroyed` can drop the inventory.

### 5.4 Chunk unload (the important one)
`LevelChunk.clearAllBlockEntities()` (MC) runs **two passes over every BE in the chunk**:
```
blockEntities.values().forEach(BlockEntity::onChunkUnloaded);  // pass 1
blockEntities.values().forEach(BlockEntity::setRemoved);       // pass 2
```
So for our parts: **`onChunkUnloaded` fires first** →
`MultiblockTileEntityBase.onChunkUnloaded` → `logic.onChunkUnload` →
`detachSelf(chunkUnloading = true)` → `controller.detachBlock(part, true)`:
- `if (chunkUnloading && state == ASSEMBLED)` → **state = `PAUSED`**, `onMachinePaused()`.
- The part is detached; if it was the reference coord, `selectNewReferenceCoord()` picks a new
  delegate **among still-loaded parts**.
- `detachSelf` then nulls `logic.controller`.

When pass 2 (`setRemoved` → `invalidate` → `detachSelf(false)`) runs, `logic.controller` is
already `null`, so the removal path is a no-op for the controller. **Net effect of a chunk
unload: the machine pauses (does not disassemble) and the parts in that chunk leave the
controller.** This pause-not-break distinction is the engine's central chunk-safety mechanism.

### 5.5 Assembly validation
`checkIfMachineIsWhole()` → `isMachineWhole()` (throws on failure):
- whole now → `assembleMachine` (`PAUSED→ASSEMBLED` fires `onMachineRestored`; otherwise
  `onMachineAssembled`), broadcasts `onMachineAssembled` to every part.
- not whole & was `ASSEMBLED` → `disassembleMachine` (fires `onMachineBroken` on every part).
- not whole & `PAUSED` → **do nothing** (stay paused). A partially-unloaded machine therefore
  does not false-disassemble.

### 5.6 Merge (`assimilate`) and split (`checkForDisconnections`)
- **Merge**: `master.assimilate(other)` — `other._onAssimilated(master)` releases its parts
  and forfeits its delegate; the master adds each acquired part via `connectedParts.add` +
  `logic.setController` + `onBlockAdded`, then calls `onAssimilate(other)`.
  ⚠️ `assimilate` does **not** call `attachBlock`, so it neither reads an acquired part's
  `cachedMultiblockData` nor runs reference-coord bookkeeping for it; and `onAssimilate` is the
  *only* hook to transfer the consumed controller's game state. **See the flaw catalogue.**
- **Split**: `checkForDisconnections` resets `referenceCoord`, prunes parts whose chunk is
  unloaded / whose tile no longer matches / that are removed, re-picks the lowest-coord
  reference, flood-fills neighbours, and removes anything unreachable (returns the orphaned
  set for re-processing next tick).

### 5.7 Assembly-state diagram
```
                 place blocks / chunk reload completes structure
   DISASSEMBLED ───────────────────────────────────────────────► ASSEMBLED
        ▲                                                          │   ▲
        │ break a block (isMachineWhole fails while ASSEMBLED)     │   │
        └──────────────────────────────────────────────────────────   │
                                                                   │   │ onMachineRestored
                       any part's chunk unloads (chunkUnloading)   │   │ (structure whole again
                                                       ┌───────────┘   │  after reload)
                                                       ▼               │
                                                    PAUSED ────────────┘
        (game logic frozen; not re-validated to DISASSEMBLED while paused)
```

---

## 6. Persistence model — read this twice

This is where the corruption lives, so it gets its own section.

### 6.1 Where state is stored
- **Per-part NBT** (always): owner, the part's own inventory (sieve/swarmer), block-state data.
- **Shared controller state** (inventory, beekeeping/farm progress, climate steps, sockets,
  tanks…): serialized into **one part's** NBT under the compound tag `"multiblockData"`.
  That part is the **save delegate**.

`MultiblockLogic.write` (`MultiblockTileEntityBase.saveAdditional` → here):
```java
if (isMultiblockSaveDelegate() && controller != null) {
    CompoundTag d = new CompoundTag();
    controller.write(d);          // Alveary/FarmController.write → full shared payload
    data.put("multiblockData", d);
}
```
`MultiblockLogic.readFromNBT` (`load` → here): if `"multiblockData"` present, stash it in
`cachedMultiblockData` (the controller doesn't exist yet at BE-load time).

`MultiblockControllerForestry.write/read` persist **only the owner**; the concrete
controllers override `write/read`, call `super`, and append their real payload
(`AlvearyController.write` → temperature/humidity steps + beekeeping + bee inventory;
`FarmController.write` → sockets + manager (hydration/fertilizer/tank) + farm inventory).

### 6.2 How cached data re-enters a controller
Only via `attachBlock` (placement/orphan attach):
```java
if (logic.hasMultiblockSaveData()) {
    onAttachedPartWithMultiblockData(part, savedData);  // controller.read(savedData)
    logic.onMultiblockDataAssimilated();                // clears the cache
}
```
**Any** part that attaches carrying cached data will `read` it into the controller — it does
not have to be the delegate or the lowest coord. If two parts carry cached data, both `read`
fire and **last-writer-wins** (order = orphan/chunk-load order = nondeterministic).

Two properties of `read` make this dangerous (see §7 Group C):
- **`read` is additive / non-clearing.** It funnels through `InventoryUtil.readFromNBT`
  (`InventoryUtil.java:433-444`), which returns early if the tag is missing and otherwise only
  `setItem`s the slots present in the snapshot — it **never clears the container first**. So
  re-`read`ing a snapshot onto an already-populated controller *merges* (resurrects removed
  items) rather than *replaces*. Manager reads (`FarmFertilizerManager.read:54`,
  `FarmHydrationManager.read:110-112`) use **unconditional `getInt`**, so a missing key reads
  back as `0` (zeroes the value).
- **The live inventory is gated on assembly.** `AlvearyController.getInternalInventory()` /
  `FarmController.getInternalInventory()` return the real inventory only while `isAssembled()`,
  otherwise a `FakeInventoryAdapter`. A capability resolved *while assembled* still wraps the
  real inventory afterward (caps aren't invalidated on PAUSE), which is how a paused
  controller's inventory can still be mutated by automation.

### 6.3 Two facts that make this fragile
1. **The `saveMultiblockData` flag is _not_ persisted.** Only the presence of the
   `"multiblockData"` tag survives a save/load. On reload, *whichever* parts have that tag
   become data carriers, regardless of who "should" be the delegate.
2. **The delegate's identity can move between blocks** — and therefore between **chunks** —
   via `attachBlock` (a lower corner appears), `selectNewReferenceCoord` /
   `checkForDisconnections` (the lower part's chunk unloaded), or `_onAssimilated`.

### 6.4 Marking chunks dirty
`updateMultiblockEntity` (when `serverTick` returns `true`) marks **every chunk in the
`[min..max]` bounding box** `setUnsaved(true)` — but only if `level.hasChunksAt(min,max)` (all
bounding-box chunks loaded). `FarmController.serverTick` returns `true` unconditionally
(there's a `//FIXME` about it); `AlvearyController.serverTick` returns `canWork` (so an idle
alveary does **not** re-mark its chunks every tick).

### 6.5 The MC 1.20.1 save-vs-unload ordering (authoritative)
`ChunkMap.scheduleUnload` (main thread, one chunk at a time, **order across a structure's
chunks not guaranteed**):
```
1. ((LevelChunk)chunk).setLoaded(false);
2. post ChunkEvent.Unload(chunk);
3. this.save(chunk);            // serialize → saveAdditional → MultiblockLogic.write()
4. this.level.unload(chunk);    // → clearAllBlockEntities() → onChunkUnloaded() then setRemoved()
```
**A chunk is serialized (step 3) _before_ its parts get `onChunkUnloaded` (step 4).** So at
serialization time the delegate is still the delegate and writes its data — the unload itself
doesn't lose the *current* chunk's data. The danger is what happens to the *other* chunks and
to the in-memory delegate identity afterward (see catalogue).

### 6.6 Client/server sync (separate from disk)
`getUpdateTag`/`onDataPacket`/`handleUpdateTag` → `encodeDescriptionPacket` /
`decodeDescriptionPacket`. On the **client**, `decodeDescriptionPacket` stashes incoming
`"multiblockData"` into `cachedMultiblockData` when the part has no controller yet — the same
field used for disk load. Client and server share the **static** `MultiblockRegistry.registries`
map keyed by `Level`, relying on client vs server `Level` instances being distinct keys.

---

## 7. Failure catalogue — chunk-border data corruption

This catalogue is the reconciled output of an adversarial audit (independent finders →
refute-by-default verification → completeness critic) cross-checked against the manual trace
and the decompiled MC/Forge sources. Each entry is tagged:

- **CONFIRMED** — mechanism and a realistic trigger both verified.
- **LATENT** — the defective code path is real and verified, but a safeguard currently keeps
  it from biting in normal play; it's a loaded gun (one refactor away, or hit by an addon).
- **CONTINGENT** — real, but needs a specific precondition (usually "two on-disk snapshots
  exist" or "automation was already wired before the pause").
- **EXOTIC** — real but needs a contrived geometry/timing that won't occur by accident.

### 7.0 The one safeguard everything hinges on — and its three holes

The engine's defence against chunk churn is **PAUSE** (§5.4): the instant *any* part's chunk
unloads, the controller stops ticking and freezes. If the controller's state could *only*
change via its own `serverTick`, pausing would make every on-disk snapshot self-consistent and
none of the corruption below would matter. It bites because state changes leak around the
pause through **three holes**:

1. **Dirty-marking is decoupled from the data.** The *only* per-tick "please persist me" signal
   is `updateMultiblockEntity`'s `setUnsaved` loop, gated on `serverTick()==true` **and**
   `hasChunksAt(min,max)`. Mutations that happen when that gate is false are never flagged →
   silently lost on reload. (Group A.)
2. **Reads are additive, and the delegate can duplicate across a border.** `read()` ⇒
   `InventoryUtil.readFromNBT` never clears the destination, and `FarmFertilizerManager/
   HydrationManager.read` use unconditional `getInt`. Combined with the delegate's identity
   migrating between chunks (so two — or zero — `multiblockData` tags can exist on disk),
   reload merges/zeroes state instead of restoring it. (Groups B, C.)
3. **`assimilate` throws data away.** Merging two controllers discards the consumed one's
   entire game state because `onAssimilate` is empty. (Group B.)

### Severity summary

| ID | Status | Sev | Scope | One-line |
|----|--------|-----|-------|----------|
| A1 | CONFIRMED | High | any chunk | Idle Alveary mutates climatiser/queen state but `serverTick` returns `canWork==false` → never marked dirty → rolls back. |
| A2 | CONFIRMED | High | border | GUI edit marks the *clicked* part's chunk, not the save-delegate's chunk → edit lost when they're in different chunks. |
| A3 | LATENT | Med | border | `hasChunksAt(min,max)` is all-or-nothing → one unloaded bbox chunk suppresses *all* dirty-marking. |
| A4 | LATENT | Med | border | Farm socket insert / climatiser block-flip mark the wrong chunk or none. |
| B1 | CONFIRMED | High | any chunk | Bridging two assembled machines → consumed machine's inventory/tanks/progress discarded (`onAssimilate` empty). |
| B2 | CONTINGENT | High | border | Reload split-then-merge can discard the data-bearing half (only if two delegates exist; see C). |
| C1 | CONTINGENT | Critical | border | Delegate migrates on partial unload → stale/duplicate `multiblockData` on disk; reload last-writer-wins. |
| C2 | CONFIRMED(prop) | High | border | Additive non-clearing `read()` resurrects removed items (ghost duplication) whenever any snapshot is re-seeded. |
| C3 | CONTINGENT | Med | border | Half-structure loads alone → empty `multiblockData` written; unconditional `getInt` zeroes fertilizer/hydration on unified reload. |
| C4 | LATENT | Med | border | New delegate promoted without consuming its pending cache; `assimilate` doesn't enforce one-delegate invariant. |
| D1 | CONFIRMED | High | border | Border-spanning Farm re-assembles on a shrunken valid sub-prism while half is unloaded (transient; drives C). |
| D2 | EXOTIC | High | border | `checkForDisconnections` BFS orphans live parts only on a checkerboard diagonal-chunk unload of a max farm. |
| E1 | LATENT | Med | border | Owner re-derived by majority vote over *loaded* parts on assembly → ownership flips on partial reload. |
| F1 | LOW | Low | border | Client description-packet decode (additive read) → phantom client-side items across pause/assemble. |

---

### Group A — Persistence dirty-marking is decoupled from the data

**A1 · CONFIRMED · High · (not even chunk-border-specific — start here)**
`AlvearyController.serverTick` returns `canWork` (`AlvearyController.java:243`). But every
assembled tick, *before* that return, it unconditionally runs each `climatiser.changeClimate`
(`:234-236`) and the `beekeepingLogic.canWork()` probe (`:225`). Those mutate **persistent**
state regardless of `canWork`: the hygroregulator drains its fluid tank + `heatTicks`
(`TileAlvearyHygroregulator.java:64-91`), the heater drains energy (`TileAlvearyClimatiser.java:43`),
and `canWork()` itself can kill a dead queen and queue spawn items yet still return `false`
(`BeekeepingLogic.java:178`). None of these call `setChanged()` during ticking, so the **only**
dirty-mark for them is the `setUnsaved` loop in `updateMultiblockEntity` (`MultiblockControllerBase.java:422-441`),
which is skipped because `serverTick` returned `false`.
*Repro:* assembled Alveary, full hygroregulator + heater, **no queen** (so `canWork()==false`
every tick). Let it drain fluid/energy. Crash or unload after the last clean save → on reload the
tank/energy are full again (rolled back). *Fix:* mark dirty when any climatiser consumed
resources or `canWork()` mutated queen/spawn state, decoupled from the `canWork` return.

**A2 · CONFIRMED · High · border**
A GUI item move marks dirty **the chunk of the tile the player right-clicked**, via vanilla
`Slot.setChanged → BlockEntity.setChanged → chunk.setUnsaved(true)`. But the persistent
inventory lives only in the **save-delegate** part's `multiblockData`. When the machine
straddles a border and you open the GUI on a *non-delegate* part, the delegate's chunk is
left clean; for an idle Alveary the `serverTick` loop (A1) never re-marks it either.
(`MultiblockTileEntityBase.java:51`, `MultiblockLogic.java:112`, delegate ≠ clicked tile.)
*Repro:* working Alveary across a border, delegate in chunk B, remove the queen; right-click a
plain block **in chunk A** and insert/extract items; relog → the edit is gone (or removed items
reappear). *Fix:* route controller-inventory `setChanged` to mark the **delegate's** chunk
(`referenceCoord`) dirty, not the interacted tile's.

**A3 · LATENT · Med · border**
`updateMultiblockEntity`'s dirty-mark is wrapped in `if (… level.hasChunksAt(min,max))`
(`MultiblockControllerBase.java:425`). `hasChunksAt` is all-or-nothing — one unloaded chunk in
the bounding-box rectangle makes it `false`, so even the **loaded** delegate chunk is not
marked. *Bounded by:* normally the unloaded chunk's parts have already PAUSED the controller
(so `serverTick` doesn't run anyway); the open window is a same-tick race before the
pause-detach runnable drains. *Fix:* mark each loaded bbox chunk independently (drop the
all-or-nothing gate), or `setChanged()` the delegate BE directly.

**A4 · LATENT · Med · border**
Same family: `FarmController.setSocket` mutates live circuit logic but the socket `ItemStack`
is only persisted via the delegate's `multiblockData` and is never independently dirty-marked
(`FarmController.java:438-459`, gap). And a climatiser's `setActive` block-flip marks **only its
own** chunk (`TileAlvearyClimatiser.java:88-93`), so the climatiser half and the delegate half
can be saved out of sync → internally inconsistent reload (gap).

---

### Group B — Merge (`assimilate`) discards the consumed controller's state

**B1 · CONFIRMED · High**
`assimilate` (`MultiblockControllerBase.java:347-373`) moves the consumed controller's *parts*
(`connectedParts.add` + `setController` + `onBlockAdded`) and then calls `onAssimilate(other)`
— which is **empty** in both `AlvearyController.java:211-213` and `FarmController.java:175-178`.
The consumed controller's own fields (bee/farm inventory, tanks, fertilizer, sockets, beekeeping
progress, climate steps) are never transferred and are garbage-collected with it
(`_onAssimilated` + dead-controller cleanup, `MultiblockWorldRegistry.java:197-198`).
*Repro (single chunk):* build two complete same-type machines side by side, let the
higher-`referenceCoord` one accumulate state (fluid/fertilizer/items or bee progress), then
place one part bridging them → a merge pool forms, the lower-coord controller consumes the
other, **and the other's data vanishes.** *Fix:* implement `onAssimilate` to merge the
consumed controller's inventory/tanks/progress into the master (or refuse the merge and drop
its inventory to the world).

**B2 · CONTINGENT · High · border**
On reload of a border-spanning structure the halves can momentarily form **two** controllers
which then merge. If the half holding the authoritative `multiblockData` is the one
*consumed*, B1 destroys it. *Bounded by an invariant:* the save delegate is the **global-lowest
`(x,y,z)`** part, and the controller that reads it inherits that minimum → it is always the
merge **master**, never the consumed party. So a *clean, single-delegate* reload is safe. B2
only bites once a **second** delegate exists on disk — which is exactly what Group C produces.

---

### Group C — One migrating save-delegate ⇒ stale / duplicate / empty snapshots

This is the core "across chunk borders" failure surface.

**C1 · CONTINGENT · Critical · border**
All shared state is serialized through the single delegate part (`MultiblockLogic.write:111-118`).
When the delegate's chunk unloads, MC saves it **while it is still the delegate** (`ChunkMap`
step 3 before the detach in step 4), so chunk A keeps a `multiblockData` snapshot; then the
detach promotes a still-loaded part in chunk B to delegate (`selectNewReferenceCoord:828-852`),
and on B's next save a **second** `multiblockData` tag is written. Nothing clears A's stale tag
(`forfeitMultiblockSaveDelegate` only flips an in-memory boolean and does **not** mark the old
chunk to be re-saved). On reload both parts carry `cachedMultiblockData`; both feed
`controller.read` via `attachBlock` (`MultiblockControllerBase.java:123-127`) in nondeterministic
order → **last-writer-wins**.
*Bounded by:* PAUSE freezes the controller while any chunk is gone, so in pure single-player
walk-away the two snapshots are value-identical and the race is harmless. It becomes real
corruption when the two snapshots **diverge** (C2) or one is **empty** (C3).

**C2 · CONFIRMED (code property) · High · border**
`read()` is **additive and non-clearing**: `InventoryUtil.readFromNBT`
(`InventoryUtil.java:433-444`) returns early if the tag is absent and otherwise only
`setItem`s the slots present in the snapshot — it **never clears the container first**. So
re-seeding any snapshot onto a live controller **resurrects** items that were removed since
that snapshot (ghost duplication), rather than replacing the inventory. This is what turns
C1's "harmless re-read" into duplication, and it amplifies every read path (re-attach, merge,
client packets). Two ways the live state diverges from a stale snapshot during a partial
unload, defeating the PAUSE safeguard:
- **Stale capability:** `getInternalInventory()` returns the real inventory only while
  `isAssembled()` (else a `FakeInventoryAdapter`) — but a hopper/pipe that resolved the
  `ITEM_HANDLER` capability **while assembled** holds a cached `InvWrapper` over the real
  inventory; caps aren't invalidated on PAUSE, so it can keep inserting/extracting while the
  other chunk is unloaded.
- **D1 re-assembly:** the loaded half re-assembles and keeps ticking (below).
*Fix:* clear the destination before an additive read, and/or only seed `cachedMultiblockData`
into a *freshly created* controller (skip the read when re-attaching to a live one).

**C3 · CONTINGENT · Med · border**
If only half a structure loads (the other chunk still out), that half can form its own
controller and write a **second, empty/partial** `multiblockData`. On a later unified load the
empty tag is read alongside the real one, and because `FarmFertilizerManager.read`
(`:54 storedFertilizer = data.getInt("StoredFertilizer")`) and `FarmHydrationManager.read`
(`:110-112`) use **unconditional `getInt`**, the absent keys read back as **0** → stored
fertilizer / hydration zeroed. *Fix:* guard reads with `data.contains(key)`, and don't let a
partial/empty half persist a `multiblockData` tag.

**C4 · LATENT · Med · border**
`selectNewReferenceCoord` / `checkForDisconnections` promote a new delegate by calling
`becomeMultiblockSaveDelegate()` **without** consuming that part's pending `cachedMultiblockData`
(the only consumer is `attachBlock`); and `assimilate` never enforces the single-delegate
invariant, so a consumed delegate sitting in an *unloaded* chunk keeps its flag (its
`_onAssimilated` forfeit is skipped because `hasChunk` is false). Both push toward "two parts
believe they're the delegate." (gaps 3, 6.)

---

### Group D — Structural re-shaping of a partially-loaded machine

**D1 · CONFIRMED (transient) · High · border**
When one chunk unloads, `onDetachBlock` nulls min/max and marks the controller dirty; next tick
`checkForDisconnections` permanently drops the unloaded parts, `recalculateMinMaxCoords`
shrinks the bounding box to the loaded sub-region, and `checkIfMachineIsWhole` re-validates
**that shrunken box**. If the loaded half is itself a valid prism with a gearbox (e.g. a 5×4×3
Farm split into a complete 3×4×3), it **re-assembles** (PAUSED→ASSEMBLED) and keeps ticking as
a smaller machine while half is unloaded. By itself this is *transient* (it re-expands on
reload), but it's the engine that keeps the controller live and mutating — feeding the delegate
migration and dirty-churn that make C1/C2 bite. *Fix:* don't let a PAUSED machine re-assemble
onto a footprint smaller than its pre-pause footprint while parts are merely chunk-unloaded.

**D2 · EXOTIC · High · border**
`checkForDisconnections`' BFS uses chunk-safe neighbour lookup, so if the only physical link
between two loaded islands runs through an unloaded chunk, one island is pruned and orphaned.
The size caps make this reachable **only** for a max Farm (5×5×4) straddling a *4-chunk corner*
where the two **diagonally opposite** chunks unload (checkerboard), leaving two corner-touching
sub-rectangles. Won't happen without deliberate force-loading. *Fix:* skip the BFS prune
entirely while any part is in an unloaded chunk (the machine is already PAUSED).

---

### Group E / F — Identity and client

**E1 · LATENT · Med · border** — `MultiblockControllerForestry.onMachineAssembled` (`:50-79`)
recomputes the owner by **majority vote over currently-loaded parts** and overwrites the
just-read on-disk owner. On a cross-border reload where only some parts are loaded at assembly
time, the vote sample is partial → persisted ownership can flip. (gap 2.)

**F1 · LOW · border** — The client decodes description packets through the same non-clearing
`read()` (`AlvearyController.java:300-309`, `FarmController.java:248-259`), so a slot emptied
server-side isn't cleared client-side → phantom items in the GUI across pause/assemble
transitions. Cosmetic (client-only). (gap 5.)

---

### 7.x Ruled out (checked, **not** the cause — don't chase these)

- **Off-thread / concurrency corruption** (unsynchronised `registries` map; `detachedParts` /
  `dirtyControllers` races; the `orphanedParts`/`partsAwaitingChunkLoad` mutexes). Chunk
  *generation, disk I/O, deserialization, and lighting* genuinely run on **worker threads** in
  1.20.1 — but the part the multiblock code hooks does not. The promotion to a FULL chunk,
  `ChunkMap.protoChunkToFullChunk` (`ChunkMap.java:711-748`), runs `registerAllBlockEntities…`
  → `BlockEntity.onLoad()` and posts `ChunkEvent.Load` (lines 731-738) inside
  `thenApplyAsync(body, executor)` whose executor is `mainThreadMailbox` (line 747) →
  `ServerChunkCache.MainThreadExecutor`, whose `getRunningThread()` is the **server main
  thread** (`ServerChunkCache.java:548-549`); it's drained via `pollTask()` during the server
  tick. The unload side is symmetric: `scheduleUnload` enqueues to `unloadQueue`, drained by
  `processUnloads` from `ServerChunkCache.tick` on the main thread. So `onLoad`→`validate`,
  `onChunkUnloaded`, `setRemoved`, and the `ChunkEvent.Load` subscriber **all execute on the
  main server thread**, single-file. The mutexes / "can be added asynchronously via chunk
  loads" comments are a holdover from the 1.6/1.7 MCPC+/Cauldron era (and the original
  threaded-chunk experiments) and are effectively no-ops here. No data race is reachable
  *through this path* — though that also means the mutexes are not a defence you can rely on if
  an addon ever touches the registry from another thread.
- **Validation reading stale air across a border.** On the **server**, `Level.getBlockState`
  calls `getChunk(…, FULL, true)` which **force-loads/generates** the chunk synchronously, so
  `isMachineWhole` never reads "air" for an unloaded neighbour. (It *can* trigger synchronous
  neighbour-chunk loads — a perf cost — but not a false pass/fail.)
- **Parts stranded forever in `partsAwaitingChunkLoad`.** The own-chunk `ChunkEvent.Load`
  reliably drains them to the orphan queue.
- **Pure "delegate chunk unloads first → rollback"** with *no* external mutation: prevented by
  the save-before-detach ordering + PAUSE freeze (the snapshots stay identical). It only
  becomes corruption via C2/C3/D1.

---

## 8. Why GregTech doesn't have these problems

The user's paraphrase of the GregTech design is accurate, and each GregTech choice maps to a
specific Forestry failure surface it removes:

| GregTech choice | What it avoids in Forestry's model |
|-----------------|-------------------------------------|
| **Structures never merge into a single "entity."** Controller + hatches/buses stay distinct blocks; the controller just keeps a list of the hatch positions it found. | Forestry's `assimilate` / merge pool / `onAssimilate` machinery — and the data-loss when one controller consumes another. No merge ⇒ no assimilation data loss. |
| **All I/O lives in single-block hatches/buses.** Each hatch owns its inventory/tank/energy in its **own** BE NBT. | Forestry concentrates *all* shared state in **one** delegate block's NBT. A single block never spans a chunk border, so a hatch's data can't be split, duplicated, or orphaned by chunk boundaries. There is no "save delegate" whose identity migrates between chunks. |
| **The controller adds listeners to hatches when it reads a complete structure**, and hatches assume the controller's texture/CTM. | No floodfill-maintained `connectedParts` graph, no reference-coord, no per-tick split detection. Linking is by stored position references, re-derived on demand. |
| **Structure checks run only when blocks change, not on chunk load/unload.** | Forestry re-runs orphan-attach / merge / split / `checkIfMachineIsWhole` driven by chunk-load and chunk-unload churn (via `validate`/`onChunkUnload`). Chunk events are exactly when partial loads, nondeterministic ordering, and delegate migration happen. Event-on-block-change means a half-loaded structure simply fails its next check harmlessly — no data is moved. |

**The crux:** Forestry makes the multiblock a *single logical entity that owns shared state in
one migrating block and merges/splits dynamically*. GregTech keeps every functional block
independent and self-persisting, and treats "is the structure complete?" as a cheap,
event-driven, **stateless** query. Persistence is per-block, so chunk borders — which are a
per-block concept — never threaten it.

A pragmatic middle path for Forestry (short of a rewrite): persist the shared controller state
**redundantly in every part** (or in a `SavedData`/level-attached store keyed by reference
coord) instead of in a single migrating delegate, and make `onAssimilate` actually transfer
state. That removes the "one block owns everything and its identity moves across chunks"
hazard without abandoning the controller abstraction.

---

## 9. Debugging playbook

### 9.1 Log signatures to grep (`Forestry.LOGGER`)
| Message (substring) | Emitted by | Meaning |
|---|---|---|
| `is double-adding part` | `attachBlock` | A part was added to a controller twice — list desync; often precedes weirdness. |
| `Double-removing part` | `detachBlock` | Part removed twice. |
| `Encountered two controllers with the same reference coordinate` | `shouldConsume` | Merge tie-break hit the audit-and-retry path — two controllers think they own the same corner. |
| `Two controllers with the same reference coord that somehow both have valid parts` | `shouldConsume` | **Hard invariant violation** (throws). Smoking gun for split-brain controllers. |
| `found N dead parts during an audit` | `auditParts` | Stale parts were pruned. |
| `Found a non-empty controller. Forcing it to shed its blocks` | `processMultiblockChanges` dead cleanup | A controller was marked dead while still holding parts. |
| `marked as dead, but that world is not tracked` | `addDeadController` | Controller outliving its world registry. |
| `should be detached already, but detected that it was not` | `assertDetached` | A part kept a stale controller reference. |

### 9.2 What to inspect at a breakpoint
- On a misbehaving machine, find its controller via
  `MultiblockUtil.getController(level, pos, TileAlveary.class / TileFarmPlain.class)` and check:
  - `assemblyState` (stuck `PAUSED`? wrongly `DISASSEMBLED`?),
  - `referenceCoord` (does it match the lowest loaded part? is its chunk loaded?),
  - `connectedParts.size()` vs the physical block count,
  - which part has `logic.isMultiblockSaveDelegate() == true` (should be exactly one, and it
    should equal the reference part),
  - whether more than one part has a non-null `cachedMultiblockData` after load.
- For lost/rolled-back data: dump the on-disk NBT of every part in the structure and count how
  many carry a `"multiblockData"` tag. **Expected: exactly one. Zero = total loss risk; ≥2 =
  duplication / last-writer-wins risk.**

### 9.3 Reproduction harness (manual)
1. Build an Alveary or Farm **straddling a chunk border** (use F3+G to show chunk lines;
   place so the lowest-`(x,y,z)` corner and the rest fall in different chunks).
2. Fill it with identifiable contents (named items, partial breeding progress).
3. Force partial unload: stand so only part of the structure stays within ticking/loaded
   range, or use `/forceload` on one half's chunk and let the other unload, then walk far away
   and back. Repeat the load/unload cycle several times.
4. Reload the world (or `/reload`-adjacent: relog) and compare contents/progress.
5. Watch the log for §9.1 signatures during the load/unload churn.

### 9.4 Useful instrumentation points to add temporarily
- Log `(referenceCoord, isAssembled, connectedParts.size())` each time `attachBlock`,
  `detachBlock`, `assimilate`, and `selectNewReferenceCoord` run.
- In `MultiblockLogic.write`, log when a part writes `"multiblockData"` (who, where, chunk).
- In `attachBlock`'s `hasMultiblockSaveData()` branch, log every `read` so you can see
  double-reads and their order.
- In `assimilate`, log master/other reference coords and whether `onAssimilate` moved any data
  (it currently doesn't).

---

## 10. Quick reference — the hot code paths

- New controller / merge / split / dead cleanup: `MultiblockWorldRegistry.processMultiblockChanges` (the comments literally flag *"THIS IS THE ONLY PLACE WHERE …"*).
- Delegate selection: `MultiblockControllerBase.attachBlock` (lines ~129-142),
  `selectNewReferenceCoord` (~828-852), `checkForDisconnections` (~684-801).
- Merge data path: `MultiblockControllerBase.assimilate` (~347-373) + `onAssimilate`
  (Alveary/Farm — **empty**).
- Persistence: `MultiblockLogic.write`/`readFromNBT` (~101-118), concrete
  `AlvearyController.write/read` / `FarmController.write/read`.
- Chunk lifecycle seam: `MultiblockTileEntityBase` (`onLoad`/`setRemoved`/`onChunkUnloaded`).

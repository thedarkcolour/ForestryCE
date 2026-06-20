# Forestry CE — Multiblock System Redesign (Design Spec)

> Status: **Draft for review (rev 4)** · Date: 2026-06-18 · Target: Minecraft Forge 1.20.1
> Companion: `docs/Multiblock-Debugging.md` (analysis + corruption catalogue; finding IDs
> A1/B1/C1… below refer to it).
>
> Review history: 3 adversarial review rounds (spec-quality + code-grounded behavior-parity +
> corruption-feasibility). Reviewers confirmed the core approach sound; rev 4 resolves the final
> blocker (reload-time re-validation) and the remaining scoping/wiring refinements. The re-anchor
> edge cases (§6.4) are the area of inherent complexity — flagged for human attention.

## 1. Problem & motivation

Forestry's two multiblocks (Alveary, Farm) run on a fork of the "Erogenous Beef" library
(`forestry.core.multiblock`). A virtual controller is assembled by a per-tick flood-fill, owns
**all** shared state, and persists it into the NBT of one member block (the *save delegate*) whose
identity migrates across chunks; controllers **merge** by discarding one side's data. The audit
confirmed corruption rooted in four causes:

- **RC1** — all shared state lives in one migrating save-delegate block.
- **RC2** — controllers merge (`assimilate`) and discard the consumed controller's state.
- **RC3** — persistence dirty-marking is decoupled from the data; reads are additive/non-clearing.
- **RC4** — flood-fill connectivity + pause/resume + merge/split run off chunk load/unload events.

## 2. Goals, non-goals, constraints

**Goals**
1. Eliminate all four root causes, especially across chunk borders.
2. Preserve the **player experience exactly** — blocks, build steps, every upgrade block
   (alveary: plain/heater/fan/hygro/sieve/swarmer/stabiliser; farm: plain/gearbox/hatch/valve/control),
   size rules, all GUIs, capabilities, drops, validation/error feedback, climate, visuals, redstone.
3. **Flexibility**: adding multiblocks is declarative; the design does not hard-block non-rectangular
   shapes.

**Non-goals**
- Backward-compatible *public API* (only Alveary/Farm consume it; free to redesign).
- A general I/O hatch/bus framework.
- Errored-cell spectacle rendering (structure teaching → Patchouli previews, §11).

**Constraints**
- **No save-data loss.** Detect the legacy `multiblockData` NBT on load, convert it, persist the new
  format thereafter (one-time, self-healing). §10.
- Chunk load/unload and the BE lifecycle run on the **main server thread** in 1.20.1 (verified:
  `ChunkMap.protoChunkToFullChunk` dispatches `BlockEntity.onLoad`/`ChunkEvent.Load` via the
  main-thread mailbox; `LevelChunk.clearAllBlockEntities` runs `onChunkUnloaded` then `setRemoved`).
  No cross-thread locking needed.
- **Faithful re-implementation, not a balance change** (§3.1 records every kept-vs-fixed decision).

## 3. Approach (decision record)

Three approaches were weighed:
- **A — patch the existing engine.** Fixes symptoms but leaves RC1 + RC4; corruption can regress;
  no flexibility gain. *Rejected.*
- **B — full GregTech model** (player-placed controller block + I/O hatches/buses). Robust + great for
  shapes but **changes how players build/interact**. *Rejected (breaks parity).*
- **C — hybrid: GregTech principles, Forestry UX.** *Chosen.* Keep symmetric blocks and identical
  player behavior; rebuild the three broken internal layers.

| Root cause | Eliminated by |
|---|---|
| RC1 migrating save-delegate | Shared state in one **anchor block's own NBT**; the anchor changes only on a synchronous in-memory hand-off when a block is actually broken (§6.4) — never on chunk events. |
| RC2 merge discards data | **No merge / no `assimilate`** (§7). |
| RC3 dirty-marking + additive reads | Anchor + per-component state use normal `setChanged()` (shared edits routed to the anchor, §6.2); reads made clearing/key-guarded in the shared reader (§6.2). |
| RC4 chunk-reactive flood-fill/pause | Event-driven stateless validation + a self-ticking anchor; chunk events flip an `assembled` flag (§5, §7). |

### 3.1 Explicit behavior decisions (kept vs fixed)
- **Owner resolution (audit E1).** *Keep result, fix latent bug.* Per-part `owner` NBT stays (set in
  `BlockStructure.setPlacedBy`; used by `getOwner()`). The **machine owner** is resolved by the same
  **majority vote over members** as today (`MultiblockControllerForestry.onMachineAssembled`) but
  **only at first formation**, then stored on the anchor and **not re-voted on reload** (removes E1's
  partial-reload flip; preserves the initial outcome).
- **Additive reads (audit C2/C3/F1).** *Fix:* clearing/key-guarded reads in the shared reader (§6.2).
- **A1 idle-resource rollback.** *Fix:* per-component `setChanged()` (§8.3).
- **A2 GUI-edit lost on wrong chunk.** *Fix:* route shared-inventory dirtying to the anchor (§6.2).
- **D1 shrunken-footprint re-assembly.** *Fix:* the validator's maximality + loaded-shell rule (§5.2)
  — a sub-prism of a larger Farm, or a Farm with half its cells unloaded, never assembles.
- **Swarmer `pendingSpawns` & hygroregulator `liquidTank` not dropped on break.** *Keep as-is
  (current behavior).* Today `BlockStructure.onRemove` drops only `getInternalInventory()`; the
  swarmer's queued `pendingSpawns` and the hygroregulator's fluid are discarded with the BE NBT, not
  dropped. This redesign **does not change that** (faithful parity; not a balance change).
- All other player-facing behaviors preserved verbatim.

## 4. Architecture overview

```
            ┌─────────────────────────── per machine ───────────────────────────┐
 block /    │  MultiblockPattern ──validate(world, origin)──► PatternResult       │
 neighbor   │  (declarative, Patchouli-compatible)            (match | failure)   │
 change     │                                                       │             │
            │   members store anchorPos; per-part onMachineAssembled/Broken fire  │
            │              ┌───────────── Anchor BlockEntity ────────────────┐    │
            │              │ lowest-(x,y,z) member; the base member class     │    │
            │              │ hosts Controller (game logic + shared state) in  │    │
            │              │ THIS block's NBT; ticker runs ONLY on the anchor │    │
            │              │ & when assembled; drives component buckets       │    │
            │              └──────────────────────────────────────────────────┘    │
            └────────────────────────────────────────────────────────────────────┘
   No global registry. No flood-fill. No merge/split. No pause state machine.
```

## 5. Structure definition & validation (kills RC4; enables flexibility)

### 5.1 The pattern — the cube is solid Forestry components
A `MultiblockPattern` declares per machine: a set of **cells** (relative offsets) each with a **cell
predicate**, and optional **size ranges** (Farm). Predicates reuse current validation verbatim (same
translation keys): `anyAlvearyBlock`, `mustBePlain`, `woodenSlab` (`BlockTags.WOODEN_SLABS`),
`nonSolidRender`, `isGearbox`. No rectangular assumption — a pattern is offsets+predicates.

**Every cell of the `[min..max]` prism must be a Forestry component** (this is current behavior — the
`part==null` branch in `RectangularMultiblockControllerBase` calls the base `isBlockGood*` which
**throws**, and neither controller overrides it; the "permitted" code comment is stale). New
predicates therefore **reject** non-component blocks inside the prism. The *only* legitimately
non-Forestry cells are the Alveary's slab cap and entrance air ring, validated **outside** the prism.

Machine mappings (behavior-identical):
- **Alveary** — 3×3×3 of `anyAlvearyBlock`; top exterior layer + interior `mustBePlain`; 3×3 layer at
  `maxY+1` is `woodenSlab`; ring at `minX−1..maxX+1`/`minZ−1..maxZ+1` at `y=maxY` is `nonSolidRender`.
- **Farm** — size range X∈[3,5], Z∈[3,5], Y=4; level-2 band + interior `mustBePlain`; ≥1 `isGearbox`;
  all-component solid prism.

### 5.2 Validation is a stateless query
`PatternValidator.validate(world, candidateOrigin) → PatternResult`:
- **Match** → the member set, the **bounding box** (min/max recomputed over members — see §6.1), the
  resolved **anchor** = lowest-`(x,y,z)` member (§6.1), and components bucketed by interface (§8).
- **Failure** → structured `{ pos, translationKey }` failing cells (first reason drives chat).

Nothing is retained between calls; nothing runs per tick or on chunk load/unload.

**Maximality + loaded-shell rule (fixes audit D1; replaces the old flood-fill's implicit behavior).**
The old engine flood-filled *all* connected components into one controller, so it always formed the
**maximal** structure and never a sub-region. A candidate-origin pattern check has no such guarantee on
its own: for the **variable-size Farm**, a 3×4×3 sub-region of a 5×4×3 farm satisfies every 3×4×3 cell
predicate and would wrongly assemble as a smaller machine (D1) — especially when the extra layers sit in
an **unloaded** chunk. Two conditions make `validate` return **Match** only for the true, fully-loaded
structure:
1. **Maximality:** the cells in the **exterior shell** immediately beyond the matched prism's growable
   faces must **not** be same-type multiblock components. If they are, the real structure is larger and
   this candidate is a non-maximal sub-region → **non-match** (a larger candidate will match instead).
   (Fixed-size patterns like the Alveary cannot be sub-regions of themselves; their existing exterior
   air-ring/slab checks already pin the boundary, so maximality applies chiefly to size-range patterns.)
2. **Loaded shell:** every pattern cell **and** every shell cell consulted above must be in a **loaded**
   chunk (`StructureView.isLoaded`). If any required cell is unloaded, maximality cannot be confirmed →
   **non-match** (defer). So a Farm straddling a chunk border never assembles as a shrunken sub-prism
   while half its blocks are unloaded; it assembles only once all cells (and the confirming shell) are
   loaded — exactly when the old engine would have, and never on a partial footprint.

### 5.3 Validation triggers (stated precisely; validation is cheap and stateless, never per-tick)
Validation (the bounded stateless query, §5.2) runs **on events only** — never on a tick loop:
- **A Forestry multiblock block is placed/broken/replaced** (its BE `onLoad`/`setRemoved`, block
  place/break) → discovery / re-validate.
- **A member's chunk loads** — its BE `onLoad` runs validation, re-establishing `anchor`/`assembled`/
  buckets and re-firing per-part `onMachineAssembled`. **This is what reactivates an assembled machine
  after a reload, and what triggers legacy migration adoption (§10) on a pure first-load with no block
  change.** (It is *not* the old flood-fill — it is one cheap stateless pattern check. Resolves the
  rev-3 "reload never reassembles" contradiction.) Chunk **unload** of a member deactivates the machine
  via `onChunkUnloaded` (§6.4, §7.4).
- **Alveary only:** `BlockAlveary.neighborChanged` catches the **non-Forestry** cells (slab cap above;
  entrance air ring beside the top edge). Its body is **rewritten** for the event-driven model: instead
  of the deleted `controller.reassemble()` (which marked the old registry dirty), it invokes
  `PatternValidator` for the affected machine and applies the assemble/deactivate transition. The
  `PacketAlvearyChange` client handler is likewise re-pointed at the client-side validator. Face-
  adjacency covers every non-Forestry Alveary cell; the diagonal ring-corner blind spot is identical
  to today. The Farm has **no** non-Forestry cells, so `FarmBlock` needs no `neighborChanged`.

**Discovery candidate set (concrete).** For a Forestry block changed/loaded at `P`, the candidate
origins are `{ P − cellOffset : cell ∈ pattern }`, taken over all size variants (the Farm's
`[3..5]×[3..5]`) and deduplicated. Each candidate is checked by the stateless query (§5.2). This is
bounded by the pattern's cell count (tens for the Alveary; a few hundred across the Farm's size range)
and runs only on block-change/load events, so it cannot churn per tick.

A global `BlockEvent`+spatial-index backstop (non-adjacent dependencies in exotic future shapes) is
**out of scope** (§11).

## 6. State ownership & persistence (kills RC1, RC3)

### 6.1 Three distinct positions (don't conflate them)
The redesign uses three deterministic positions; keep them separate:

1. **Reference coord** = lowest-`(x,y,z)` member, **recomputed from the validated member set**. Drives
   the biome sample (`AlvearyController.getBiome`, `FarmController.getCoordinates`/`getBiome`) and the
   spectacle highlight (§11). It must **always** read the validated member set and **never** fall back
   to a null/`ZERO` cached field (the old `getReferenceCoord()` returns `ZERO` when unset — that path
   must not survive, or a pre-resolution tick samples biome at `(0,0,0)`).
2. **Center / top-center** = bounding-box-derived (`getCenterCoord`/`getTopCenterCoord`; alveary climate
   origin, FX/light/sky; `FarmController.getCoords()` targeting origin). `min`/`max` are **recomputed
   from the member set on every (re)assembly including deactivate→reactivate**; never anchor-derived or
   cached across reloads.
3. **Payload holder (the "anchor")** = the single member that **serializes the shared controller
   payload** and **hosts/ticks the controller**. Hosting lives on the common base class
   `MultiblockTileEntityForestry`, so **any** member type can hold it.

The shared state (alveary: `InventoryBeeHousing`, beekeeping logic, `temperatureSteps`/`humiditySteps`,
machine owner; farm: `InventoryFarm`, `sockets`, `FarmManager` tanks/fertilizer/hydration, owner) is
serialized **only by the holder**.

**Single-holder invariant:** *exactly one loaded member serializes the payload at any save point.* It
is maintained as follows:
- **At first formation** the holder is the lowest member.
- **On every full (re)assembly (all members loaded)** the holder is canonicalized to the lowest member
  — if the current holder is not the lowest, the payload moves to the lowest member and the old
  holder's chunk is **force-marked dirty** so its stale copy is dropped on next save. (All members are
  loaded at assembly, so this is always safe.) In the steady assembled state the holder == reference
  coord == lowest member, so they coincide.
- The existing base-class `MultiblockTileEntityForestry.load/saveAdditional` calls to
  `getInternalInventory().read/write` (which resolve to the shared controller inventory) must be
  **holder-gated**: only the holder serializes the shared inventory; a non-holder member must not (else
  ≥2 members write the shared inventory — an RC1-adjacent multi-writer hazard).

### 6.2 Why this fixes the corruption
- A single block **never spans a chunk border** → no split, no delegate hopping, no dual/zero/stale
  snapshots (RC1). The anchor changes only on a real block break (§6.4), never on chunk events.
- **Dirty-marking is wired to the data:** every persisted mutation marks the owning BE via
  `setChanged()`. Critically, **shared-inventory edits must dirty the HOLDER BE, not the clicked
  tile.** Today a GUI slot edit calls `Container.setChanged()` on the clicked (non-holder) tile,
  marking the wrong chunk (audit A2). *Fix:* the shared controller inventory holds a **re-pointable
  back-reference to the holder BE** and calls `holderBE.setChanged()` on mutation; the member's
  `setChanged()` also resolves the holder and dirties it. The back-reference is **re-pointed on every
  re-anchor / canonicalization** (§6.1, §6.4). So GUI edits **and** automation always mark the chunk
  that persists the shared state.
- **Reads are clearing / key-guarded — scoped to the multiblock readers, not the global helper:**
  - The global `InventoryUtil.readFromNBT` is **left unchanged** (it backs the worktable's
    `MemorizedRecipe`, `InventoryPlain`, and many non-multiblock machine inventories — a blanket change
    is out of scope and risky). Instead, the **multiblock shared-inventory read path**
    (`InventoryBeeHousing`/`InventoryFarm`/`sockets`) uses a **clearing read** that clears the
    destination **before** the tag-absent check, so a missing inventory tag yields an empty container
    rather than preserving ghosts (fixes C2 + the F1 client packet-decode path). Add a clearing variant
    (or clear at these call sites) — do not mutate the shared utility.
  - `FarmFertilizerManager`/`FarmHydrationManager.read` are **`contains(key)`-guarded** so a missing
    scalar key keeps the current value rather than zeroing it (fixes C3).
  - **NBT key names are unchanged** (migration depends on it).

### 6.3 Two drop paths (both preserved exactly)
1. **Per-block `HasInventory` drops — immediate, on ANY break of that block** — via
   `BlockStructure.onRemove`, which drops `component.getInternalInventory()`: the **sieve**
   (`InventoryAlvearySieve`), **swarmer** (`InventorySwarmer` item slots), and **hygroregulator**
   (its item inventory) drop their own inventory. **Kept verbatim.** *Note (parity):* the swarmer's
   `pendingSpawns` and the hygroregulator's `liquidTank` fluid are **not** in `getInternalInventory()`
   and are **not** dropped today — they are discarded with the BE NBT, and this is unchanged (§3.1).
2. **Shared-inventory drop — deferred, on FULL dismantle** — when the last member is removed, the
   anchor (or its hand-off holder, §6.4) drops the shared inventory **and**, for the Farm, the circuit
   **`sockets`** (`FarmController.onDestroyed` drops both today) at the final position.

### 6.4 Re-anchor & break-vs-unload disambiguation

**Chunk-unload vs break (load-bearing).** MC fires `onChunkUnloaded` **then** `setRemoved` on the same
BE during a chunk unload. A member sets an `unloading` flag in `onChunkUnloaded`; in `setRemoved`, if
`unloading` is set it is a **temporary unload** → deactivate only (no drops, no re-anchor); otherwise a
**genuine break** → the rules below. Mirrors today's `chunkUnloading` distinction.

**Break of a non-holder block** → machine deactivates; the payload stays on its current holder; only
that block's own drops fire (§6.3.1).

**Break of the holder** must preserve the single-holder invariant (§6.1) even when several blocks are
removed in one operation (explosion/piston/`setBlock` cascades process `setRemoved` one BE at a time on
the main thread). Algorithm, applied on **each** holder removal:
1. Resolve the lowest-`(x,y,z)` **currently-loaded** surviving member.
2. If one exists → **hand the payload to it synchronously** (it becomes the holder; re-point the
   inventory back-reference §6.2; **force-mark the old holder's chunk dirty** so its stale copy is
   dropped). Hosting lives on the **base member class**, so any survivor (incl. an upgrade block)
   round-trips the payload in its `saveAdditional`/`load` **alongside its own per-block NBT in the same
   tag, under distinct keys** (no collision). The hand-off is **idempotent** — if a subsequent break in
   the same tick removes the new holder, step 1 re-resolves the next survivor. Invariant: after each
   `setRemoved`, exactly one loaded member holds the payload.
3. If **no surviving member is currently loaded** (holder broken while the rest of the structure is in
   unloaded chunks): **force-load the nearest survivor's chunk to perform the hand-off** (synchronous
   `getChunk(…, FULL, true)`), preserving "no drop on partial break". Only if that is impossible (no
   survivor on disk either — i.e. truly the last block) does the **full-dismantle drop** fire (shared
   inventory + farm `sockets` at the holder position). The single-holder invariant guarantees no other
   member carries a payload copy, so this can never duplicate.

**Full dismantle** (last member removed) → drop the shared inventory **and** farm `sockets` at the
final position (today's `onDestroyed`).

> **Risk note (for human review):** this re-anchor logic is the most intricate part of the design — it
> exists because all shared state is concentrated in one block (the persistence model you chose). The
> force-load-on-unloaded-survivor branch is rare (you must break the structure's holder while its other
> blocks are unloaded) but must be tested (explosion/piston/world-edit). An alternative that removes
> this complexity entirely — a per-`Level` `SavedData` store keyed by structure id — was considered and
> set aside in favor of anchor-block NBT; it remains a fallback if the re-anchor edge cases prove
> troublesome in practice.

## 7. Lifecycle (kills RC2, RC4)

### 7.1 The anchor hosts and ticks the controller — with an anchor-only guard
The runtime controller (game logic + shared state) is hosted by the **holder/anchor** BlockEntity.
Ticking moves from the deleted global `MultiblockServerTickHandler` to a `BlockEntityTicker`: each
member block (`BlockAlveary`, `FarmBlock` — already `EntityBlock`, but with **no `getTicker()` today**)
must add a `getTicker()` returning a server ticker (and, for alveary client FX, a client ticker).
Because a structure contains **many** member BEs of the same type, the ticker body **must early-return
unless `getBlockPos().equals(anchorPos) && assembled`** (and must resolve the controller via `anchorPos`
without NPE before the anchor is assembled) — only the holder instance runs the machine logic (today's
`AlvearyController.serverTick`/`clientTick`, `FarmController.serverTick` bodies, unchanged). Without
this guard the logic would run once per member (N× climate/breeding/fertilizer) — test §14.

**Deleted:** `MultiblockRegistry`, `MultiblockWorldRegistry`, `MultiblockServerTickHandler` (global
tick loop), `MultiblockEventHandler` (chunk events), orphan/dirty/dead sets, merge pools, split
detection, `assimilate`/`onAssimilate`, the save-delegate machinery, the PAUSED state machine.

### 7.2 GUIs and capabilities are PER-BLOCK (verified against code — do not blanket-route)
On assembly each member stores the anchor's `BlockPos`; `getController()` resolves the anchor BE's
hosted controller. **GUI routing (all gated on `assembled` via `BlockStructure.use`):**
- Plain alveary blocks, all farm blocks, alveary heater/fan/stabiliser → open the **shared** machine
  GUI (their `createMenu` → the shared controller container).
- **Sieve / swarmer / hygroregulator** override `createMenu` and open their **own** GUI — keep this;
  do not route them to the shared GUI. (Still `assembled`-gated by `BlockStructure.use`.)

**Capabilities (every alveary tile inherits `TileAlveary.getCapability` → `ITEM_HANDLER` over
`getInternalInventory()`; farm tiles override individually):**

| Cap | Blocks | Backing | Lifecycle |
|---|---|---|---|
| `ITEM_HANDLER` → **shared** bee inv | alveary plain / heater / fan / stabiliser | controller bee inv (`Fake` when not assembled) | assembled-gated; **invalidate on deactivate** |
| `ITEM_HANDLER` → **own** inv | alveary sieve / swarmer / hygro | that block's own inventory | always; invalidate on `setRemoved` only |
| `ITEM_HANDLER` → **shared** farm inv | farm **hatch** only (base `TileFarm` exposes none) | controller farm inv (`Fake` when not assembled) | assembled-gated; invalidate on deactivate |
| `ENERGY` → **own** | alveary heater/fan, farm gearbox | that block's own `energyStorage` | **always** (chargeable before assembly); do **not** invalidate on deactivate |
| `FLUID` → **own** | alveary hygroregulator | that block's own `liquidTank` | always |
| `FLUID` → **shared** tank | farm **valve** | `controller.getTankManager()` (`FakeTankManager` no-op when not assembled) | assembled-gated; **not** an own tank, **not** fillable pre-assembly |

Capability invalidation must be **selective**, and requires a code change to be meaningful:
- The shared-backed caps today return a **fresh `LazyOptional` per `getCapability` call**
  (`TileAlveary` → `LazyOptional.of(() -> new InvWrapper(getInternalInventory()))`; `TileFarmValve` →
  `LazyOptional.of(this::getTankManager)`) — nothing is cached, so a pipe re-resolves and keeps writing
  to the `Fake` backing after deactivation (the C2 hole). **Convert the shared caps (alveary shared
  `ITEM_HANDLER`; farm hatch `ITEM_HANDLER`; farm valve `FLUID`) to a cached `LazyOptional`**,
  invalidated on the `assembled→deactivated` flip **and** on `setRemoved`.
- The *own*-backed caps (gearbox/heater/fan `ENERGY`; hygro own `FLUID`; sieve/swarmer/hygro own
  `ITEM_HANDLER`) must **stay exposed across deactivation** — the `assembled` flag must **not** gate
  their exposure (you can charge a gearbox/heater before completing the structure, as today).
- **Reconcile with Forge:** `invalidateCaps()` fires on **both** `setRemoved` and `onChunkUnloaded`
  (e.g. `TileFarmGearbox` already overrides it). That's fine — own caps are simply **re-created lazily**
  after any Forge invalidation when next requested. The `assembled→deactivated` invalidation is an
  **additional** trigger that targets **only** the cached shared-cap `LazyOptional`, independent of
  Forge's lifecycle invalidation.

### 7.3 `assembled` is a cached flag flipped by events; per-part callbacks still fire
- The pattern check sets `assembled` + member set + buckets on a match; a structural change clears it
  (and runs §6.3/§6.4 only on a genuine break).
- **Per-member `onMachineAssembled`/`onMachineBroken` still fire on every transition** (assemble,
  disassemble, **and** deactivate↔reactivate), as today's `assembleMachine`/`disassembleMachine`. They
  are **not** no-ops: `TileAlveary.onMachineAssembled/Broken` re-set entrance-slot visuals via
  `BlockAlveary.getNewState`; `TileFarm.onMachineAssembled/Broken` call `updateNeighborsAt` driving the
  BAND blockstate + redstone. Reload (deactivate→reactivate) must re-fire them.

### 7.4 Chunk-border behavior
- **A member's chunk unloads** → it resolves `anchorPos` and (if the anchor is loaded) flips the
  anchor's `assembled=false` (stops ticking). State untouched in the anchor's NBT. Reload →
  re-validate → reactivate. No state moves; no split/merge.
- **The anchor's chunk unloads** → its BE stops ticking; its state saves atomically with its chunk;
  the machine is simply not assembled until it reloads.
- **Two valid structures touch** → independent; no merge, so no consumed data to discard.

## 8. Component capability system (preserved verbatim)

The `IAlvearyComponent`/`IFarmComponent` sub-interfaces and the `Tile*` classes are kept unchanged;
only *where buckets are filled* (at assembly, from the member set) and *what drives the tick* (the
anchor) change.

### 8.1 Per-block parity matrix (state / GUI / caps / drops) — verified against code

| Block | Own per-block NBT | GUI (assembled-gated) | Capabilities | Drops on break |
|---|---|---|---|---|
| Alveary **plain** | — (may be anchor → hosts shared payload) | **shared** | shared `ITEM_HANDLER` | shared inv on full dismantle |
| Alveary **heater/fan** | energy + workingTime/heating | **shared** | own `ENERGY` (always) + shared `ITEM_HANDLER` | own item |
| Alveary **stabiliser** | — (contributes `BeeModifier`) | **shared** | shared `ITEM_HANDLER` | own item |
| Alveary **sieve** | `InventoryAlvearySieve` | **own** | own `ITEM_HANDLER` | own inv (immediate) |
| Alveary **swarmer** | inventory + `PendingSpawns` | **own** | own `ITEM_HANDLER` | own inv (immediate); **pendingSpawns NOT dropped** |
| Alveary **hygroregulator** | inventory + `liquidTank` + `TransferTime`/`CurrentLiquid` | **own** | own `ITEM_HANDLER` + own `FLUID` | own inv (immediate); **tank fluid NOT dropped** |
| Farm **plain/control** | — (may be anchor) | **shared** | — (base `TileFarm` exposes none) | shared inv+sockets on full dismantle |
| Farm **gearbox** | energy + `ActivationDelay`/`PrevDelays` | **shared** | own `ENERGY` (always) | own item |
| Farm **valve** | — | **shared** | **shared** `FLUID` (controller tank, assembled-gated) | own item |
| Farm **hatch** | per-block as today | **shared** | **shared** `ITEM_HANDLER` (controller inv) | own item |

### 8.2 Bucketing & tick semantics (preserve exactly)
- Buckets (`beeModifiers`/`beeListeners`/`climatisers`/`activeComponents`; farm active+listeners) are
  filled at assembly from the member set (today's `onBlockAdded` logic), cleared on disassembly.
- **The constructor-seeded `new AlvearyBeeModifier()` must survive re-bucketing** — re-add it whenever
  `beeModifiers` is rebuilt (feeds production + the HELLISH `temperature()` path).
- **Climatiser ordering is load-bearing:** every assembled tick, `temperatureSteps`/`humiditySteps`
  reset to 0, then all `climatisers.changeClimate(...)` run **unconditionally, before** the `canWork`
  gate.
- **Farm per-`Active` tick offsets** are `level.random.nextInt(256)`, assigned at attach, **not
  persisted**, re-randomized on re-bucket — preserve (do not zero/persist them).
- Components do **not** self-tick; the anchor drives them (so they tick only when assembled+all-loaded).

### 8.3 Per-component persistence fix (the real A1 fix)
The heater's drained energy, the hygroregulator's fluid/`heatTicks`, the gearbox's energy, and the
swarmer's `PendingSpawns` live in **each component's own BE NBT** — *not* the holder's. These **continue
to round-trip through the component's own `saveAdditional`/`load`** (they are per-block state, §8.1, and
survive normal save/reload) — the persistence-relocation refactor must **not** drop those reads/writes;
the only thing intentionally *absent* is the on-break world-drop of `PendingSpawns`/the hygro tank
(§3.1). Today the only thing that *dirty-marked* this state each tick was the deleted per-tick
`setUnsaved` loop, so **each component must call `setChanged()` on its own BE whenever
`changeClimate`/`updateServer` mutates that state** (the real A1 fix). The A1 regression test asserts
against the **component's** chunk.

## 9. Networking & client sync

- The holder's description packet (`getUpdateTag`/`onDataPacket`) carries the controller payload to the
  client so the GUI shows live shared state, as today. The client decode path uses the **clearing**
  multiblock read (§6.2) so a slot emptied server-side does not leave a client-side ghost (audit F1).
- The client runs its **own** stateless validation. `PacketAlvearyChange` (sent by
  `BlockAlveary.neighborChanged`) is re-pointed at the client validator so slab-cap / air-ring changes
  refresh the client's assembled state and the alveary entrance textures (§5.3, §7.3). There is a
  separate client registry/`Level` key, but with no global tick loop the client simply validates and
  ticks the holder via the client ticker (alveary FX) — no cross-thread concerns (all main/client
  thread).
- `getController()` on the client resolves the holder via `anchorPos` exactly as on the server; an
  unassembled member resolves to the `Fake` controller (no GUI/highlight), matching today.

## 10. Migration (no data loss)

Legacy format: the save-delegate part's NBT holds `multiblockData` = controller payload (incl. owner
via `ownerHandler`); **plus** every block stores its own top-level `owner` tag and its own per-block
tags. The new anchor (lowest member) **coincides with the old save delegate** (also the lowest
member/reference coord) in the common case, so the legacy carrier *is* the new anchor — migration is
usually a no-op rename.

**Legacy keys read unchanged (no renames):** per-part `owner`; controller-payload owner;
`temperatureSteps`/`humiditySteps`; the beekeeping payload; `InventoryBeeHousing`/`InventoryFarm` slot
lists; `sockets`; `StoredFertilizer`; hydration keys; hygroregulator `TransferTime`/`CurrentLiquid`;
swarmer `PendingSpawns`; gearbox `ActivationDelay`/`PrevDelays`.

**Procedure (order-independent; handles already-corrupted multi-tag worlds):**
1. On BE load, if NBT contains the legacy `multiblockData` tag, stash it on that BE
   (`LegacyMachineState`). Per-part `owner` is read as today.
2. **Adoption is attempted whenever any member carrying an unconsumed legacy tag finishes loading —
   driven by the load-time validation of §5.3 (not only a block-change-triggered formation)** — so a
   legacy world's first load (no block change) still migrates, and intra-chunk BE load order cannot
   skip it. The holder adopts with a deterministic tie-break: among members carrying a legacy tag,
   **adopt the lowest-`(x,y,z)` non-empty one and discard the others without reading them; never
   overwrite populated state with an empty tag** (C1/C3 left ≥2 or empty tags on some worlds; lowest-
   coord mirrors the old delegate so the authoritative snapshot wins deterministically).
3. Adoption uses the clearing/key-guarded readers (§6.2) and unchanged keys, then writes the new
   format; legacy tags drop on the next save. **The machine owner is adopted verbatim from the legacy
   `multiblockData` `ownerHandler`** (it is authoritative on a migrated world) — the §3.1 majority vote
   runs **only** for brand-new, never-formed structures, so migration cannot re-trigger E1's ownership
   flip on the upgrade path.

## 11. Player feedback & structure teaching

- **Error messages (parity):** the right-click-empty-hand → chat path (`BlockStructure.use`, keys
  `needSlabs`/`needSpace`/`needGearbox`/`error.small`/…) is preserved, computed **on demand**. To
  deliver the "works for a lone/not-yet-formed block" improvement, `BlockStructure.use` must call
  `PatternValidator` **directly** for the failure reason rather than routing through `getController()`
  (which returns the `Fake` controller — and a null error — for an unformed block).
- **Spectacle highlight (parity):** keep highlighting the **reference coord** (lowest-`(x,y,z)` member,
  recomputed from the member set) in creative (`ISpectacleBlock.isHighlighted`). When the structure is
  unassembled, `getController()` is the `Fake` controller — highlight is absent then, matching today;
  the implementation must resolve the reference coord from the validated member set (not the old
  `getReferenceCoord()` field) and handle the unassembled branch.
- **Structure teaching (forward):** lean on **Patchouli multiblock previews**. The declarative pattern
  is the single source of truth and is Patchouli-compatible (cell grid + `IStateMatcher` + center), so
  one definition drives validation, error messages, and the preview. Errored-cell spectacle *rendering*
  is **not** built now (the matcher exposes failing-cell data for a future feature). The variable-size
  Farm shows a **canonical example** (e.g. min 3×4×3) in Patchouli; the pattern validates the full
  range. Validation must **not** runtime-depend on Patchouli being present.

## 12. Scope boundaries

**In scope:** the three-layer rewrite of `forestry.core.multiblock`; relocating Alveary/Farm shared
persistence to the anchor (base-class hosting); the A2 anchor-dirtying and per-component `setChanged()`
fixes; pattern definitions; clearing/key-guarded shared readers; selective capability invalidation;
migration; preserving all components, GUIs (shared + per-block), both drop paths, owner semantics,
per-part assembled/broken visuals, the lowest-member highlight; Patchouli-compatible pattern export.

**Out of scope (deferred):** global `BlockEvent`+spatial-index detection; errored-cell spectacle
rendering; a hatch/bus I/O framework; any gameplay/balance change (including the swarmer
`pendingSpawns` / hygro tank break-loss, kept as-is per §3.1).

## 13. Class plan (orientation for the implementation plan)

*New (`forestry.core.multiblock`):* `MultiblockPattern`, `IStructurePredicate` (+ predicates),
`PatternValidator`, `PatternResult`; `MultiblockController` (rewritten host: no flood-fill/merge; holds
buckets; ticked only on the anchor).

*Reworked:* `MultiblockTileEntityBase`/`MultiblockTileEntityForestry` (base-class hosting of the shared
payload + anchor-only ticker guard §7.1; members store `anchorPos`; per-part `onMachineAssembled/Broken`
retained; `unloading`-flag break disambiguation; legacy stash; member `setChanged()` routes to anchor);
a **scoped clearing read** for the multiblock shared inventories (the global `InventoryUtil.readFromNBT`
is **left unchanged** — see §6.2); `FarmFertilizerManager`/`FarmHydrationManager.read` (key-guarded);
component `changeClimate`/`updateServer` (own `setChanged()`); shared inventory dirties the anchor;
selective cap invalidation.

*Removed:* `MultiblockRegistry`, `MultiblockWorldRegistry`, `MultiblockServerTickHandler`,
`MultiblockEventHandler`, `RectangularMultiblockControllerBase`, `MultiblockLogic` (save-delegate),
`assimilate`/merge/split/PAUSED machinery, `MultiblockUtil.getNeighboringParts` flood-fill.

*Reused, persistence relocated:* `AlvearyController`/`FarmController` (game logic intact; serverTick
bodies become the anchor tick body), all `Tile*` components, `IAlvearyComponent`/`IFarmComponent`,
`FarmManager` & sub-managers, `InventoryBeeHousing`/`InventoryFarm`, `BlockStructure`/`BlockAlveary`
(keep `neighborChanged`/`use`; keep `onRemove`'s **per-block** own-inventory drop for sieve/swarmer/hygro,
but **gate the shared-inventory drop to full dismantle** — after the redesign `getInternalInventory()` on a
plain/holder block resolves to the shared inventory, so an unconditional `onRemove` drop would dump the
whole machine on any single-block break; see §6.3).

## 14. Testing strategy

Audit findings → regression tests (each must now be safe):
- **A1** — idle (queenless) Alveary draining hygro fluid / heater energy: assert the **component BE's**
  state persists across save/reload.
- **A2** — open the **shared GUI from a non-anchor block in a different chunk than the anchor**, edit a
  slot, reload: assert the edit persists.
- **B1** — adjacency of two complete machines: assert independence; no inventory lost.
- **C1/C2/C3** — build across a chunk border; unload/reload halves in varying order: assert no item
  loss, duplication, rollback, or fertilizer/hydration zeroing (incl. the missing-inventory-tag clears,
  missing-scalar-key keeps cases).
- **Once-per-tick anchor** — many member blocks: assert machine logic runs **exactly once/tick**.
- **D1 maximality / loaded-shell** — a 3×4×3 sub-region of a 5×4×3 Farm does **not** assemble as a
  smaller machine (maximality); a Farm with cells in an unloaded chunk does **not** assemble on a
  partial footprint (loaded-shell); both assemble correctly once fully loaded (§5.2).
- **Drops** — non-anchor / anchor-with-survivors (survivor may be an **upgrade block** → assert it
  round-trips the payload through save/unload before reassembly) / last block / anchor-with-no-survivor
  (fallback) — assert §6.3/§6.4, incl. farm **sockets** drop; assert swarmer **pendingSpawns** and
  hygro **tank** are **not** dropped (parity).
- **Chunk-unload ≠ break** — unload a member's chunk: assert deactivate only (no drops/re-anchor).
- **Per-block GUIs/caps** — sieve/swarmer/hygro own GUIs reachable when assembled; gearbox/heater/fan
  ENERGY caps work pre-assembly; valve `FLUID` is a no-op pre-assembly and feeds the shared tank when
  assembled; shared item caps invalidated on deactivate, own caps not.
- **Visuals** — assemble/disassemble/reload re-fires per-part callbacks (alveary entrance textures,
  farm BAND/redstone).
- **Bounding box** — reload a structure whose lowest member is not the geometric center: assert
  `getCenterCoord`/`getTopCenterCoord`/`getCoords` unchanged (member-set-derived, not anchor-derived).
- **Reload reactivation** — assemble, unload, reload a structure with **no block change**: assert
  load-time validation re-establishes `assembled`, re-fires per-part `onMachineAssembled` (visuals),
  and resumes ticking (guards against the rev-3 "reload never reassembles" bug).
- **Per-component persistence** — assert a heater's energy, hygro fluid, and swarmer `PendingSpawns`
  survive normal save/reload (own `saveAdditional`/`load` intact), independent of the on-break drop
  parity.
- **Cap invalidation** — a pipe pulling from a shared cap: after deactivate, assert the cached shared
  `LazyOptional` is invalidated (no writes to the `Fake` backing); assert own energy caps still work
  pre-assembly and after a chunk-unload/reload.
- **Re-anchor under multi-break** — explosion/piston removing the holder + other low blocks in one
  operation: assert exactly one loaded member holds the payload after each removal, no duplication/loss.
- **Re-anchor, survivor in unloaded chunk** — break the holder while the rest is unloaded: assert the
  survivor chunk is force-loaded for the hand-off (no drop, no dup); only true last-block drops.
- **Migration** — load legacy worlds, incl. a deliberately **multi-`multiblockData`** / empty-tag one,
  and a **first load with no block change**: assert contents/progress preserved, owner adopted
  **verbatim** from the legacy payload (no E1 flip), lowest-coord non-empty snapshot wins, new format
  written on save.

## 15. Risks & open questions

- **Re-anchor under non-player removal** (§6.4 fallback): confirm via tests (explosion/piston/
  world-edit) and that the `unloading`-flag disambiguation is robust.
- **Discovery cost:** bounded candidate-origin checks per Forestry-block change; confirm no
  pathological churn on rapid place/break.
- **Patchouli coupling:** pattern↔Patchouli export must be optional (no runtime dependency).

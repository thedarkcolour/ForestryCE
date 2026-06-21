package forestry.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import forestry.api.ForestryConstants;
import forestry.api.multiblock.IMultiblockController;
import forestry.api.multiblock.IMultiblockInventoryProbe;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Inventory-conservation regression suite for Forestry multiblocks (alveary + farm).
 *
 * <p>Each test builds a real machine in an empty arena, loads a known countable item into its shared inventory, drives a
 * real game operation (chunk-style reload, block break, or a controller merge induced by block placement), and asserts
 * <b>conservation</b>: the items the machine started with end up either still in some machine or dropped on the floor —
 * never silently lost, never duplicated.
 *
 * <p><b>Fully engine-agnostic.</b> The only coupling to a specific multiblock engine is reading the shared inventory
 * through {@link IMultiblockInventoryProbe}; everything else is vanilla blocks/entities and Forestry-public block
 * registries. Nothing calls an internal merge/assimilate method. So the SAME tests are the oracle for the current
 * ("Erogenous Beef") engine and the upcoming overhaul — re-run {@code ./gradlew runGameTestServer} against the new
 * engine and every test should go green.
 *
 * <p><b>Baseline on the CURRENT engine</b> (validated by a real GameTest run): the reload tests FAIL with a DUPE (the
 * machine restores its inventory from the save-delegate NBT while a duplicate copy is spilled); the bridge-merge tests
 * FAIL with a silent LOSS; the break / partial-break tests PASS. On the overhaul branch all should pass.
 *
 * <p><b>The silent loss is deterministically reproducible without any engine-internal call.</b> Merge mastership is
 * decided purely by reference-coord order (the lowest-coord controller consumes the rest), so an EMPTY machine at lower
 * coords that merges with a DATA machine at higher coords discards the data inventory — the empty {@code onAssimilate}
 * transfers nothing and the consumed controller is retired without a {@code destroyedCoord}, so nothing drops.
 * {@link #alvearyBridgeMergeConservesInventory} / {@link #farmBridgeMergeConservesInventory} reproduce exactly this by
 * placing a single bridge block between two separately-assembled machines, then asserting the union of inventories
 * survives across whatever controllers remain.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MultiblockGameTests {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int TIMEOUT = 240;

	/** A countable, vanilla item used as the tracked inventory contents (slot acceptance is bypassed on insert). */
	private static ItemStack trackedStack() {
		return new ItemStack(Items.HONEYCOMB, 7);
	}

	private static BlockState alvearyPart() {
		return ApicultureBlocks.ALVEARY.get(BlockAlvearyType.PLAIN).defaultState();
	}

	private static BlockState farmPart() {
		return FarmingBlocks.FARM.get(EnumFarmBlockType.PLAIN, EnumFarmMaterial.STONE_BRICK).defaultState();
	}

	/* ===================== Alveary ===================== */

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyRoundTripConservesInventory(GameTestHelper helper) {
		roundTrip(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyStagedReloadConservesInventory(GameTestHelper helper) {
		stagedReload(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyBreakConservesInventory(GameTestHelper helper) {
		breakAll(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyPartialBreakConservesInventory(GameTestHelper helper) {
		partialBreak(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyBridgeMergeConservesInventory(GameTestHelper helper) {
		bridgeMerge(helper, MultiblockTestSupport::buildAlveary, alvearyPart(), MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
	}

	/* ===================== Farm ===================== */

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmRoundTripConservesInventory(GameTestHelper helper) {
		roundTrip(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmStagedReloadConservesInventory(GameTestHelper helper) {
		stagedReload(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmBreakConservesInventory(GameTestHelper helper) {
		breakAll(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmBridgeMergeConservesInventory(GameTestHelper helper) {
		bridgeMerge(helper, MultiblockTestSupport::buildFarm, farmPart(), MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	/* ===================== shared scenarios ===================== */

	/** Build fn: places the machine at {@code base} (relative) and returns its absolute member positions. */
	@FunctionalInterface
	private interface Builder {
		List<BlockPos> build(GameTestHelper helper, BlockPos base);
	}

	/** Mutable state threaded across the sequence steps of a single test. */
	private static final class Run {
		List<BlockPos> members;
		List<BlockPos> membersLow;
		List<BlockPos> membersHigh;
		Map<Item, Integer> before;
		AABB box;
		Set<UUID> dropsBefore;
		Map<BlockPos, BlockEntity> fresh;
		BlockPos brokenPos;
		BlockState brokenState;
	}

	/**
	 * Single-tick full reload: tear down every member block entity and recreate them all at once, then let the engine
	 * reform on its own tick. Asserts the machine keeps its inventory AND nothing leaks to the floor (a leak here is the
	 * drop-on-teardown dupe).
	 */
	private static void roundTrip(GameTestHelper helper, Builder builder, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		Run run = new Run();
		helper.startSequence()
				.thenExecute(() -> {
					placeFloor(helper);
					run.members = builder.build(helper, BASE);
				})
				.thenExecuteAfter(5, () -> {
					assertAssembledAndLoad(helper, run, invSize, slot);
					MultiblockTestSupport.reloadInPlace(level, run.members);
				})
				.thenExecuteAfter(8, () -> assertConservedNoLeak(helper, run, "reload"))
				.thenSucceed();
	}

	/**
	 * Staged reload in the adversarial "save-delegate arrives last" order: recreate every member except the anchor, let
	 * a partial controller form (asserted, so this is a genuinely different code path from {@link #roundTrip}), THEN
	 * recreate the anchor and let the structure reform. Same conservation assertion.
	 */
	private static void stagedReload(GameTestHelper helper, Builder builder, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		Run run = new Run();
		helper.startSequence()
				.thenExecute(() -> {
					placeFloor(helper);
					run.members = builder.build(helper, BASE);
				})
				.thenExecuteAfter(5, () -> {
					assertAssembledAndLoad(helper, run, invSize, slot);
					run.fresh = MultiblockTestSupport.teardown(level, run.members);
				})
				.thenExecute(() -> {
					List<BlockPos> withoutAnchor = new ArrayList<>(run.members);
					withoutAnchor.remove(0); // the anchor (lowest-(x,y,z) member, the save-delegate)
					MultiblockTestSupport.placeAndRegister(level, run.fresh, withoutAnchor);
				})
				.thenExecuteAfter(4, () -> {
					helper.assertTrue(MultiblockTestSupport.controllerAt(level, run.members.get(1)) != null,
							"staged path not exercised: no partial controller formed from the non-anchor members");
					MultiblockTestSupport.placeAndRegister(level, run.fresh, List.of(run.members.get(0)));
				})
				.thenExecuteAfter(8, () -> assertConservedNoLeak(helper, run, "staged reload (anchor last)"))
				.thenSucceed();
	}

	/** Break every member: the inventory must be fully dropped as items, never silently wiped and never duplicated. */
	private static void breakAll(GameTestHelper helper, Builder builder, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		Run run = new Run();
		helper.startSequence()
				.thenExecute(() -> {
					placeFloor(helper);
					run.members = builder.build(helper, BASE);
				})
				.thenExecuteAfter(5, () -> {
					assertAssembledAndLoad(helper, run, invSize, slot);
					for (BlockPos member : run.members) {
						level.destroyBlock(member, false); // remove block, no block-item drop; inventory still spills
					}
				})
				.thenExecuteAfter(8, () -> {
					int expected = MultiblockTestSupport.total(run.before);
					int kept = keptInMachine(level, run);
					int dropped = MultiblockTestSupport.newDropCount(level, run.box, run.dropsBefore, trackedStack().getItem());
					// exact conservation: every tracked item dropped exactly once, none left behind, none duplicated.
					helper.assertTrue(kept == 0, "break left " + kept + " item(s) in the machine instead of dropping them");
					helper.assertTrue(dropped == expected,
							"break did not conserve: dropped " + dropped + " but expected exactly " + expected + " (wipe or dupe)");
				})
				.thenSucceed();
	}

	/**
	 * Break a SINGLE non-anchor member, then RESTORE it, and assert the inventory is recovered. This tests the
	 * engine-agnostic <b>recoverability</b> invariant: a dormant/disassembled structure may hold its inventory in a
	 * live controller (old engine) OR in a stash the controller-level probe cannot see (the redesign hands the payload
	 * to a member's stash and deregisters the controller). Reading mid-break would falsely report a wipe; restoring the
	 * structure forces either engine to surface the inventory again — it must come back (or have dropped), never vanish.
	 */
	private static void partialBreak(GameTestHelper helper, Builder builder, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		Run run = new Run();
		helper.startSequence()
				.thenExecute(() -> {
					placeFloor(helper);
					run.members = builder.build(helper, BASE);
				})
				.thenExecuteAfter(5, () -> {
					assertAssembledAndLoad(helper, run, invSize, slot);
					run.brokenPos = run.members.get(run.members.size() - 1); // one corner, not the anchor
					run.brokenState = level.getBlockState(run.brokenPos);
					level.destroyBlock(run.brokenPos, false);
				})
				.thenExecuteAfter(4, () ->
						// restore the broken part: a stash-based engine re-seeds its dormant payload on re-validation
						level.setBlock(run.brokenPos, run.brokenState, Block.UPDATE_ALL))
				.thenExecuteAfter(6, () -> {
					int expected = MultiblockTestSupport.total(run.before);
					int kept = keptInMachine(level, run);
					int dropped = MultiblockTestSupport.newDropCount(level, run.box, run.dropsBefore, trackedStack().getItem());
					helper.assertTrue(kept + dropped >= expected,
							"SILENT WIPE on break+restore: " + expected + " before; only " + kept + " recovered + " + dropped + " dropped");
				})
				.thenSucceed();
	}

	/**
	 * The silent-LOSS oracle — fully engine-agnostic (block placement only, no engine-internal merge call). Builds two
	 * separately-assembled machines, an EMPTY one at lower coords (x=3..5) and a DATA one at higher coords (x=7..9),
	 * places a single bridge part in the 1-block gap (x=6, bottom row) so the two controllers collide, then UN-bridges
	 * to restore two valid structures and measures recovery. On the old engine the lower (empty) controller assimilates
	 * the higher (data) one and the inventory vanishes silently — un-bridging cannot bring it back, so it FAILS. An
	 * engine that conserves on merge (keeps it live, stashes and re-seeds on un-bridge, or drops it) passes. The
	 * conservation assertion sums inventory across ALL surviving controllers plus drops.
	 */
	private static void bridgeMerge(GameTestHelper helper, Builder builder, BlockState bridgePart, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		BlockPos lowBase = new BlockPos(3, 1, 3);   // empty machine, x=3..5
		BlockPos highBase = new BlockPos(7, 1, 3);  // data machine,  x=7..9 (1-block gap at x=6)
		BlockPos bridgeRel = new BlockPos(6, 1, 3); // bottom-row bridge, face-touches x=5 (low) and x=7 (high)
		Run run = new Run();
		helper.startSequence()
				.thenExecute(() -> {
					placeFloor(helper);
					run.membersLow = builder.build(helper, lowBase);
					run.membersHigh = builder.build(helper, highBase);
				})
				.thenExecuteAfter(6, () -> {
					helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.membersLow.get(0)), "low machine failed to assemble");
					helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.membersHigh.get(0)), "high machine failed to assemble");
					helper.assertTrue(MultiblockTestSupport.controllerAt(level, run.membersLow.get(0))
									!= MultiblockTestSupport.controllerAt(level, run.membersHigh.get(0)),
							"the two machines unexpectedly share one controller before bridging");
					MultiblockTestSupport.insertItem(level, run.membersHigh, invSize, slot, trackedStack());
					run.before = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.membersHigh.get(0)));
					helper.assertTrue(MultiblockTestSupport.total(run.before) > 0, "inventory insertion failed (nothing to track)");
					run.box = MultiblockTestSupport.dropBox(union(run.membersLow, run.membersHigh));
					run.dropsBefore = MultiblockTestSupport.itemEntityIds(level, run.box);
					helper.setBlock(bridgeRel, bridgePart); // the bridge — induces the merge, no engine-internal call
				})
				.thenExecuteAfter(8, () ->
						// un-bridge: restore two valid structures so a stash-based engine re-seeds the dormant payload
						level.setBlock(helper.absolutePos(bridgeRel), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL))
				.thenExecuteAfter(8, () -> {
					int expected = MultiblockTestSupport.total(run.before);
					int kept = MultiblockTestSupport.keptAcross(level, union(run.membersLow, run.membersHigh), trackedStack().getItem());
					int dropped = MultiblockTestSupport.newDropCount(level, run.box, run.dropsBefore, trackedStack().getItem());
					helper.assertTrue(kept + dropped == expected,
							"non-conservation on bridge merge: expected " + expected + " kept+dropped across all controllers, got kept="
									+ kept + " dropped=" + dropped + (kept + dropped < expected ? " (SILENT LOSS)" : " (DUPE)"));
				})
				.thenSucceed();
	}

	/* ===================== shared steps ===================== */

	private static void assertAssembledAndLoad(GameTestHelper helper, Run run, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.members.get(0)), "structure failed to assemble");
		MultiblockTestSupport.insertItem(level, run.members, invSize, slot, trackedStack());
		run.before = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.get(0)));
		helper.assertTrue(MultiblockTestSupport.total(run.before) > 0, "inventory insertion failed (nothing to track)");
		run.box = MultiblockTestSupport.dropBox(run.members);
		run.dropsBefore = MultiblockTestSupport.itemEntityIds(level, run.box);
	}

	private static void assertConservedNoLeak(GameTestHelper helper, Run run, String op) {
		ServerLevel level = helper.getLevel();
		// distinguish a genuine verdict from an inconclusive / infrastructure failure
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.members.get(0)),
				op + ": structure did not reform within the tick budget (inconclusive, not a verdict)");
		helper.assertTrue(MultiblockTestSupport.controllerAt(level, run.members.get(0)) instanceof IMultiblockInventoryProbe,
				op + ": inventory probe seam missing after the operation (cannot measure conservation)");
		IMultiblockController owner = MultiblockTestSupport.controllerAt(level, run.members.get(0));
		for (BlockPos member : run.members) {
			helper.assertTrue(MultiblockTestSupport.controllerAt(level, member) == owner,
					op + ": members split across multiple controllers");
		}
		Map<Item, Integer> after = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.get(0)));
		int leaked = MultiblockTestSupport.newDropCount(level, run.box, run.dropsBefore, trackedStack().getItem());
		helper.assertTrue(after.equals(run.before),
				"LOSS across " + op + ": inventory changed before=" + run.before + " after=" + after);
		helper.assertTrue(leaked == 0,
				"DUPE across " + op + ": machine kept its inventory but " + leaked + " duplicate item(s) were spilled");
	}

	private static int keptInMachine(ServerLevel level, Run run) {
		return MultiblockTestSupport.total(MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.get(0))));
	}

	private static List<BlockPos> union(List<BlockPos> a, List<BlockPos> b) {
		List<BlockPos> all = new ArrayList<>(a);
		all.addAll(b);
		return all;
	}

	/** A stone floor across the arena bottom so dropped items land within the measured box instead of falling away. */
	private static void placeFloor(GameTestHelper helper) {
		BlockState stone = Blocks.STONE.defaultBlockState();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), stone);
			}
		}
	}
}

package forestry.gametest;

import forestry.api.ForestryConstants;
import forestry.api.core.multiblock.IMultiblockController;
import forestry.api.core.multiblock.IMultiblockInventoryProbe;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.features.MultifarmBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.*;

/**
 * Data-corruption regression suite for Forestry multiblocks (alveary + farm).
 *
 * <p>Each test builds the multiblock in an empty area, adds items into its shared inventory, triggers an operation
 * (chunk-style reload, block break, or a controller merge induced by block placement), and checks for data corruption.
 * A passing test means the items added at the start are either spilled out of the machine or kept intact, without any
 * duplicates or missing items.
 *
 * <p>This suite is implementation-agnostic. To ensure corruption paths are fixed, the only coupling is through
 * {@link IMultiblockInventoryProbe}, which can be tested against both implementations with minimal changes.
 *
 * <p>With the BigReactors implementation, the reload tests FAIL with a DUPE: the machine restores its inventory from the
 * save-delegate NBT while a duplicate copy dumps its items into the world and the bridge-merge tests FAIL with a silent
 * LOSS of all items and data. Only the break / partial-break tests PASS. With the new implementation, all tests pass.
 *
 * <p>The silent loss is reproducible without any internal API calls. In the BigReactors implementation, selection of a
 * new save delegate is decided by reference-coord order (the lowest-coord controller consumes the rest), so an EMPTY
 * machine at lower coords can wipe the data of a machine at higher coords, as the empty {@code onAssimilate}
 * transfers nothing and the consumed controller is discarded without a {@code destroyedCoord}, meaning nothing drops.
 * {@link #alvearyBridgeMergeConservesInventory} / {@link #farmBridgeMergeConservesInventory} reproduce this scenario by
 * placing a single bridge block between two separately-assembled machines, then checking that the inventories survive
 * across the controller merge.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MultiblockGameTests {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int TIMEOUT = 240;

	/**
	 * Item used as the sample inventory contents (slot acceptance is bypassed on insert).
	 */
	private static ItemStack trackedStack() {
		return new ItemStack(Items.HONEYCOMB, 7);
	}

	private static BlockState alvearyPart() {
		return ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.PLAIN).defaultState();
	}

	private static BlockState farmPart() {
		return MultifarmBlocks.FARM.get(MultifarmBlockType.PLAIN, MultifarmMaterialType.STONE_BRICK).defaultState();
	}

	// ===================== Alveary =====================

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyRoundTripConservesInventory(GameTestHelper helper) {
		roundTrip(helper, MultiblockTestSupport::buildAlveary, MultiblockTestSupport.ALVEARY_INV_SIZE, 2);
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

	// ===================== Farm =====================

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmRoundTripConservesInventory(GameTestHelper helper) {
		roundTrip(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmBreakConservesInventory(GameTestHelper helper) {
		breakAll(helper, MultiblockTestSupport::buildFarm, MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmBridgeMergeConservesInventory(GameTestHelper helper) {
		bridgeMerge(helper, MultiblockTestSupport::buildFarm, farmPart(), MultiblockTestSupport.FARM_INV_SIZE, 0);
	}

	// ===================== shared scenarios =====================

	/**
	 * Function to place the machine at {@code base} (relative) and return its absolute member positions.
	 */
	@FunctionalInterface
	private interface Builder {
		List<BlockPos> build(GameTestHelper helper, BlockPos base);
	}

	/**
	 * State used throughout the steps of a single test.
	 */
	private static final class Run {
		List<BlockPos> members;
		List<BlockPos> membersLow;
		List<BlockPos> membersHigh;
		Map<Item, Integer> before;
		AABB box;
		Set<UUID> dropsBefore;
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
	 * Break every member: the inventory must be fully dropped as items, never silently wiped and never duplicated.
	 */
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
				run.brokenPos = run.members.getLast(); // one corner, not the anchor
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
	 * The silent-LOSS oracle — implementation agnostic, block placement only, no internal API merge call.
	 * Builds two separate multiblocks, an EMPTY one at lower coords (x=3..5) and a DATA one at higher coords (x=7..9),
	 * places a single bridge part in the 1-block gap (x=6, bottom row) so the two controllers merge, then UN-bridges
	 * to restore two valid structures and measures recovery.
	 *
	 * <p>On the old implementation, the lower EMPTY controller assimilates the higher DATA one and its inventory is wiped (FAIL).
	 * The new implementation conserves on merge (keeps it live, stashes and re-seeds on un-bridge, or drops it) (PASS).
	 * The conservation assertion sums inventory across ALL surviving controllers plus drops.
	 */
	private static void bridgeMerge(GameTestHelper helper, Builder builder, BlockState bridgePart, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		// EMPTY machine, x=3..5
		BlockPos lowBase = new BlockPos(3, 1, 3);
		// DATA machine,  x=7..9 (1-block gap at x=6)
		BlockPos highBase = new BlockPos(7, 1, 3);
		// bottom-row bridge block, face-touches x=5 (EMPTY) and x=7 (DATA)
		BlockPos bridgeRel = new BlockPos(6, 1, 3);
		Run run = new Run();
		helper.startSequence()
			.thenExecute(() -> {
				placeFloor(helper);
				run.membersLow = builder.build(helper, lowBase);
				run.membersHigh = builder.build(helper, highBase);
			})
			.thenExecuteAfter(6, () -> {
				helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.membersLow.getFirst()), "low machine failed to assemble");
				helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.membersHigh.getFirst()), "high machine failed to assemble");
				helper.assertTrue(MultiblockTestSupport.controllerAt(level, run.membersLow.getFirst())
						!= MultiblockTestSupport.controllerAt(level, run.membersHigh.getFirst()),
					"the two machines unexpectedly share one controller before bridging");
				MultiblockTestSupport.insertItem(level, run.membersHigh, invSize, slot, trackedStack());
				run.before = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.membersHigh.getFirst()));
				helper.assertTrue(MultiblockTestSupport.total(run.before) > 0, "inventory insertion failed (nothing to track)");
				run.box = MultiblockTestSupport.dropBox(union(run.membersLow, run.membersHigh));
				run.dropsBefore = MultiblockTestSupport.itemEntityIds(level, run.box);
				// bridge: triggers the merge, no internal API call
				helper.setBlock(bridgeRel, bridgePart);
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

	// ===================== shared steps =====================

	private static void assertAssembledAndLoad(GameTestHelper helper, Run run, int invSize, int slot) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.members.getFirst()), "structure failed to assemble");
		MultiblockTestSupport.insertItem(level, run.members, invSize, slot, trackedStack());
		run.before = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.getFirst()));
		helper.assertTrue(MultiblockTestSupport.total(run.before) > 0, "inventory insertion failed (nothing to track)");
		run.box = MultiblockTestSupport.dropBox(run.members);
		run.dropsBefore = MultiblockTestSupport.itemEntityIds(level, run.box);
	}

	private static void assertConservedNoLeak(GameTestHelper helper, Run run, String op) {
		ServerLevel level = helper.getLevel();
		// distinguish a genuine verdict from an inconclusive / infrastructure failure
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, run.members.getFirst()),
			op + ": structure did not reform within the tick budget (inconclusive, not a verdict)");
		helper.assertTrue(MultiblockTestSupport.controllerAt(level, run.members.getFirst()) instanceof IMultiblockInventoryProbe,
			op + ": inventory probe seam missing after the operation (cannot measure conservation)");
		IMultiblockController owner = MultiblockTestSupport.controllerAt(level, run.members.getFirst());
		for (BlockPos member : run.members) {
			helper.assertTrue(MultiblockTestSupport.controllerAt(level, member) == owner,
				op + ": members split across multiple controllers");
		}
		Map<Item, Integer> after = MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.getFirst()));
		int leaked = MultiblockTestSupport.newDropCount(level, run.box, run.dropsBefore, trackedStack().getItem());
		helper.assertTrue(after.equals(run.before),
			"LOSS across " + op + ": inventory changed before=" + run.before + " after=" + after);
		helper.assertTrue(leaked == 0,
			"DUPE across " + op + ": machine kept its inventory but " + leaked + " duplicate item(s) were spilled");
	}

	private static int keptInMachine(ServerLevel level, Run run) {
		return MultiblockTestSupport.total(MultiblockTestSupport.tally(MultiblockTestSupport.snapshot(level, run.members.getFirst())));
	}

	private static List<BlockPos> union(List<BlockPos> a, List<BlockPos> b) {
		List<BlockPos> all = new ArrayList<>(a);
		all.addAll(b);
		return all;
	}

	/**
	 * A stone floor to catch spilled items instead of letting them fall into the void.
	 */
	private static void placeFloor(GameTestHelper helper) {
		BlockState stone = Blocks.STONE.defaultBlockState();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), stone);
			}
		}
	}
}

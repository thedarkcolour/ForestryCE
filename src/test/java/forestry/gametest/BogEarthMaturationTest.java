package forestry.gametest;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.core.content.soil.BogEarthBlock;
import forestry.core.features.CoreBlocks;

/**
 * Port-verification suite for bog earth maturing into peat.
 * <p>
 * Bog earth advances one maturity step per random tick while water is within 2 blocks, and turns into peat on the step
 * after the last maturity value. The vanilla surface this rides on (random ticks, {@code IntegerProperty} bounds,
 * {@code BlockPos.betweenClosed}) is exactly the kind of thing a major version port breaks silently, so these tests
 * pin the whole path rather than any one method.
 * <p>
 * The tests drive {@link BlockState#randomTick} directly instead of waiting on the level to roll random ticks, because
 * the in-world rate would make the suite take minutes. {@code randomTick} then gates itself on a further 1 in 13 roll
 * off {@code level.random}, which the test cannot seed, so the loop counts state CHANGES and not calls. The number of
 * calls is random but the number of changes is not, which is what {@link #EXPECTED_ADVANCES} asserts.
 * <p>
 * A whole test method body runs inside a single server tick. No fluid tick fires while it runs, so a placed water
 * source stays exactly where the test put it and cannot spread into or out of the scanned box.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class BogEarthMaturationTest {
	// maturity 0 -> 1, 1 -> 2, then 2 -> peat
	private static final int EXPECTED_ADVANCES = 3;
	// randomTick acts on 1 call in 13, so 39 calls are needed on average. This cap makes a spurious failure impossible
	// in practice while still bounding a broken block that never converts.
	private static final int MAX_RANDOM_TICKS = 2000;

	private static final BlockPos BOG_POS = new BlockPos(8, 2, 8);
	// inside the 5x5x5 box isMoistened scans
	private static final BlockPos NEAR_WATER = BOG_POS.offset(2, 0, 0);
	// one block outside that box
	private static final BlockPos FAR_WATER = BOG_POS.offset(3, 0, 0);

	@GameTest(template = "empty")
	public static void bogEarthBecomesPeatAfterThreeMaturityAdvances(GameTestHelper helper) {
		helper.setBlock(NEAR_WATER, Blocks.WATER);
		helper.setBlock(BOG_POS, CoreBlocks.BOG_EARTH.defaultState());

		int advances = advanceUntilPeat(helper);

		helper.assertBlockPresent(CoreBlocks.PEAT.block(), BOG_POS);
		helper.assertTrue(advances == EXPECTED_ADVANCES,
			"expected bog earth to reach peat in " + EXPECTED_ADVANCES + " maturity advances, took " + advances);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void bogEarthDoesNotMatureWithoutWater(GameTestHelper helper) {
		helper.setBlock(BOG_POS, CoreBlocks.BOG_EARTH.defaultState());

		assertStaysImmature(helper, "bog earth matured with no water in the level at all");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void bogEarthDoesNotMatureWithWaterOutOfRange(GameTestHelper helper) {
		helper.setBlock(FAR_WATER, Blocks.WATER);
		helper.setBlock(BOG_POS, CoreBlocks.BOG_EARTH.defaultState());

		assertStaysImmature(helper, "bog earth matured with its nearest water 3 blocks away, outside the scanned box");
		helper.succeed();
	}

	/**
	 * Covers the maturity values the growth path never produces on its own. Closes #157, where a maturity set with a
	 * debug stick fell into the increment branch and threw on an out-of-range property value.
	 */
	@GameTest(template = "empty")
	public static void everyMaturityValueReachesPeat(GameTestHelper helper) {
		helper.setBlock(NEAR_WATER, Blocks.WATER);

		Collection<Integer> maturities = BogEarthBlock.MATURITY.getPossibleValues();
		helper.assertTrue(maturities.size() == EXPECTED_ADVANCES,
			"expected " + EXPECTED_ADVANCES + " maturity values, one per advance, found " + maturities.size());

		for (int maturity : maturities) {
			helper.setBlock(BOG_POS, CoreBlocks.BOG_EARTH.defaultState().setValue(BogEarthBlock.MATURITY, maturity));

			int advances = advanceUntilPeat(helper);

			helper.assertBlockPresent(CoreBlocks.PEAT.block(), BOG_POS);
			helper.assertTrue(advances == EXPECTED_ADVANCES - maturity,
				"expected maturity " + maturity + " to reach peat in " + (EXPECTED_ADVANCES - maturity)
					+ " advances, took " + advances);
		}
		helper.succeed();
	}

	/**
	 * Guards the rest of this class. Every other test calls {@code randomTick} by hand, so all of them would still pass
	 * if the block stopped opting into random ticks and could never mature in a real world.
	 */
	@GameTest(template = "empty")
	public static void bogEarthOptsIntoRandomTicks(GameTestHelper helper) {
		helper.setBlock(BOG_POS, CoreBlocks.BOG_EARTH.defaultState());

		helper.assertTrue(helper.getBlockState(BOG_POS).isRandomlyTicking(),
			"bog earth must opt into random ticks, it can never mature in a real world otherwise");
		helper.succeed();
	}

	/**
	 * Random-ticks the block at {@link #BOG_POS} until it turns into peat, and returns the number of ticks that changed
	 * the block. Fails the test if a tick skips a maturity step.
	 */
	private static int advanceUntilPeat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(BOG_POS);
		int advances = 0;

		for (int i = 0; i < MAX_RANDOM_TICKS; i++) {
			BlockState before = helper.getBlockState(BOG_POS);
			if (before.is(CoreBlocks.PEAT.block())) {
				break;
			}

			before.randomTick(level, pos, level.random);

			// block states are singletons, so identity separates a tick that acted from one the 1 in 13 roll rejected
			BlockState after = helper.getBlockState(BOG_POS);
			if (after == before) {
				continue;
			}
			advances++;

			if (!after.is(CoreBlocks.PEAT.block())) {
				int from = before.getValue(BogEarthBlock.MATURITY);
				int to = after.getValue(BogEarthBlock.MATURITY);
				helper.assertTrue(to == from + 1, "maturity jumped from " + from + " to " + to + " in one tick");
			}
		}

		return advances;
	}

	private static void assertStaysImmature(GameTestHelper helper, String message) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(BOG_POS);

		for (int i = 0; i < MAX_RANDOM_TICKS; i++) {
			helper.getBlockState(BOG_POS).randomTick(level, pos, level.random);
		}

		helper.assertBlockPresent(CoreBlocks.BOG_EARTH.block(), BOG_POS);
		helper.assertTrue(helper.getBlockState(BOG_POS).getValue(BogEarthBlock.MATURITY) == 0, message);
	}
}

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
import forestry.core.content.soil.HumusBlock;
import forestry.core.features.CoreBlocks;

/**
 * Port-verification suite for humus degrading into sand.
 * <p>
 * Humus advances one degrade step per random tick while a log or a bonemealable block sits in the ring directly above
 * it, and turns into sand on the step after the last degrade value. The block directly above the centre is deliberately
 * excluded so that planting a sapling on humus does not degrade its own soil.
 * <p>
 * Built in the same shape as {@link BogEarthMaturationTest}, and for the same reason: the tests drive
 * {@link BlockState#randomTick} directly rather than waiting on the level to roll random ticks, and count state CHANGES
 * rather than calls, because {@code randomTick} gates itself on a further 1 in 140 roll off {@code level.random} that
 * the test cannot seed.
 * <p>
 * A solid block sits under the humus in every test. Sand is a falling block, and the support keeps a converted block
 * where the assertions expect it.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class HumusDegradationTest {
	// degrade 0 -> 1, 1 -> 2, then 2 -> sand
	private static final int EXPECTED_ADVANCES = 3;
	// randomTick acts on 1 call in 140, so 420 calls are needed on average. This cap makes a spurious failure
	// impossible in practice while still bounding a broken block that never converts.
	private static final int MAX_RANDOM_TICKS = 20000;

	private static final BlockPos HUMUS_POS = new BlockPos(8, 2, 8);
	private static final BlockPos SUPPORT_POS = HUMUS_POS.below();
	// in the ring isEnrooted scans, one above and one across
	private static final BlockPos ROOT_POS = HUMUS_POS.offset(1, 1, 0);
	// the one position in that ring isEnrooted skips
	private static final BlockPos CENTRE_ABOVE_POS = HUMUS_POS.above();

	@GameTest(template = "empty")
	public static void humusBecomesSandAfterThreeDegradeAdvances(GameTestHelper helper) {
		placeHumus(helper);
		helper.setBlock(ROOT_POS, Blocks.OAK_LOG);

		int advances = advanceUntilSand(helper);

		helper.assertBlockPresent(Blocks.SAND, HUMUS_POS);
		helper.assertTrue(advances == EXPECTED_ADVANCES,
			"expected humus to reach sand in " + EXPECTED_ADVANCES + " degrade advances, took " + advances);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void humusDoesNotDegradeWithoutRoots(GameTestHelper helper) {
		placeHumus(helper);

		assertStaysIntact(helper, "humus degraded with nothing above it");
		helper.succeed();
	}

	/**
	 * Covers the carve-out that keeps a sapling from degrading the soil it stands on. A log directly above the centre,
	 * and nowhere else, must not count as roots.
	 */
	@GameTest(template = "empty")
	public static void humusDoesNotDegradeFromTheBlockDirectlyAbove(GameTestHelper helper) {
		placeHumus(helper);
		helper.setBlock(CENTRE_ABOVE_POS, Blocks.OAK_LOG);

		assertStaysIntact(helper, "humus degraded from the block directly above it, which isEnrooted must skip");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void everyDegradeValueReachesSand(GameTestHelper helper) {
		helper.setBlock(ROOT_POS, Blocks.OAK_LOG);

		Collection<Integer> degrades = HumusBlock.DEGRADE.getPossibleValues();
		helper.assertTrue(degrades.size() == EXPECTED_ADVANCES,
			"expected " + EXPECTED_ADVANCES + " degrade values, one per advance, found " + degrades.size());

		for (int degrade : degrades) {
			helper.setBlock(SUPPORT_POS, Blocks.STONE);
			helper.setBlock(HUMUS_POS, CoreBlocks.HUMUS.defaultState().setValue(HumusBlock.DEGRADE, degrade));

			int advances = advanceUntilSand(helper);

			helper.assertBlockPresent(Blocks.SAND, HUMUS_POS);
			helper.assertTrue(advances == EXPECTED_ADVANCES - degrade,
				"expected degrade " + degrade + " to reach sand in " + (EXPECTED_ADVANCES - degrade)
					+ " advances, took " + advances);
		}
		helper.succeed();
	}

	/**
	 * Guards the rest of this class. Every other test calls {@code randomTick} by hand, so all of them would still pass
	 * if the block stopped opting into random ticks and could never degrade in a real world.
	 */
	@GameTest(template = "empty")
	public static void humusOptsIntoRandomTicks(GameTestHelper helper) {
		placeHumus(helper);

		helper.assertTrue(helper.getBlockState(HUMUS_POS).isRandomlyTicking(),
			"humus must opt into random ticks, it can never degrade in a real world otherwise");
		helper.succeed();
	}

	private static void placeHumus(GameTestHelper helper) {
		helper.setBlock(SUPPORT_POS, Blocks.STONE);
		helper.setBlock(HUMUS_POS, CoreBlocks.HUMUS.defaultState());
	}

	/**
	 * Random-ticks the block at {@link #HUMUS_POS} until it turns into sand, and returns the number of ticks that
	 * changed the block. Fails the test if a tick skips a degrade step.
	 */
	private static int advanceUntilSand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(HUMUS_POS);
		int advances = 0;

		for (int i = 0; i < MAX_RANDOM_TICKS; i++) {
			BlockState before = helper.getBlockState(HUMUS_POS);
			if (before.is(Blocks.SAND)) {
				break;
			}

			before.randomTick(level, pos, level.random);

			// block states are singletons, so identity separates a tick that acted from one the 1 in 140 roll rejected
			BlockState after = helper.getBlockState(HUMUS_POS);
			if (after == before) {
				continue;
			}
			advances++;

			if (!after.is(Blocks.SAND)) {
				int from = before.getValue(HumusBlock.DEGRADE);
				int to = after.getValue(HumusBlock.DEGRADE);
				helper.assertTrue(to == from + 1, "degrade jumped from " + from + " to " + to + " in one tick");
			}
		}

		return advances;
	}

	private static void assertStaysIntact(GameTestHelper helper, String message) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(HUMUS_POS);

		for (int i = 0; i < MAX_RANDOM_TICKS; i++) {
			helper.getBlockState(HUMUS_POS).randomTick(level, pos, level.random);
		}

		helper.assertBlockPresent(CoreBlocks.HUMUS.block(), HUMUS_POS);
		helper.assertTrue(helper.getBlockState(HUMUS_POS).getValue(HumusBlock.DEGRADE) == 0, message);
	}
}

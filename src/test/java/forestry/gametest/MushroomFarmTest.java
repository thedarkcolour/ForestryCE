package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.agriculture.features.FarmingBlocks;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.agriculture.IFarmHousing;
import forestry.api.agriculture.IFarmLogic;

@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MushroomFarmTest {
	private static final BlockPos FARM_POS = new BlockPos(1, 2, 1);
	private static final BlockPos SOIL_POS = new BlockPos(8, 1, 8);
	private static final BlockPos MUSHROOM_POS = SOIL_POS.above();

	// growth is random, so this only has to be far above the expected number of passes
	private static final int CULTIVATE_PASSES = 2000;
	// a huge mushroom is at most 13 blocks tall with a cap radius of 3
	private static final int CLEANUP_RADIUS = 3;
	private static final int CLEANUP_HEIGHT = 14;

	@GameTest(template = "empty")
	public static void farmGrowsPlantedBrownMushroom(GameTestHelper helper) {
		assertFarmGrowsMushroom(helper, Blocks.BROWN_MUSHROOM);
	}

	@GameTest(template = "empty")
	public static void farmGrowsPlantedRedMushroom(GameTestHelper helper) {
		assertFarmGrowsMushroom(helper, Blocks.RED_MUSHROOM);
	}

	private static void assertFarmGrowsMushroom(GameTestHelper helper, Block mushroom) {
		helper.setBlock(FARM_POS, FarmingBlocks.MANAGED_MINIFARM.get(MinifarmBlockType.FARM_MUSHROOM).defaultState());
		helper.setBlock(SOIL_POS, Blocks.MYCELIUM);
		helper.setBlock(MUSHROOM_POS, mushroom);

		BlockEntity blockEntity = helper.getBlockEntity(FARM_POS);
		if (!(blockEntity instanceof IFarmHousing housing)) {
			helper.fail("Expected an IFarmHousing at " + FARM_POS);
			return;
		}

		ServerLevel level = helper.getLevel();
		BlockPos soilPos = helper.absolutePos(SOIL_POS);
		BlockPos mushroomPos = helper.absolutePos(MUSHROOM_POS);
		IFarmLogic logic = IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.SHROOM).getLogic(false);

		boolean grown = false;
		for (int i = 0; i < CULTIVATE_PASSES && !grown; i++) {
			logic.cultivate(level, housing, soilPos, Direction.EAST, 1);
			grown = level.getBlockState(mushroomPos).is(Blocks.MUSHROOM_STEM);
		}

		BlockPos min = mushroomPos.offset(-CLEANUP_RADIUS, 0, -CLEANUP_RADIUS);
		BlockPos max = mushroomPos.offset(CLEANUP_RADIUS, CLEANUP_HEIGHT, CLEANUP_RADIUS);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}

		helper.assertTrue(grown, "expected the mushroom farm to grow the planted " + mushroom + " into a huge mushroom, it was still a small mushroom after " + CULTIVATE_PASSES + " cultivate passes");
		helper.succeed();
	}
}

package forestry.gametest;

import forestry.agriculture.multifarm.multiblock.MultifarmController;
import forestry.api.ForestryConstants;
import forestry.api.IForestryApi;
import forestry.api.agriculture.ForestryFarmTypes;
import forestry.api.agriculture.HorizontalDirection;
import forestry.api.agriculture.IFarmLogic;
import forestry.api.core.ForestryError;
import forestry.core.features.CoreItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class FarmMissingSoilTest {
	private static final BlockPos FARM_BASE = new BlockPos(6, 1, 6);
	private static final int WORK_CYCLES = 40;
	// four stacks cover every soil position on the 16x16 platform
	private static final int MYCELIUM_STACKS = 4;
	private static final int FERTILIZER_SLOT = 20;

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void shroomFarmReportsMissingSoil(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE_BRICKS);
			}
		}
		List<BlockPos> members = MultiblockTestSupport.buildFarm(helper, FARM_BASE);

		// wait for the multiblock to assemble
		helper.runAfterDelay(40, () -> {
			if (!(MultiblockTestSupport.controllerAt(level, members.get(0)) instanceof MultifarmController farm)) {
				helper.fail("Expected an assembled multifarm at " + FARM_BASE);
				return;
			}

			IFarmLogic shroom = IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.SHROOM).getLogic(false);
			for (Direction side : HorizontalDirection.VALUES) {
				farm.setFarmLogic(side, shroom);
			}
			MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.FARM_INV_SIZE, FERTILIZER_SLOT, CoreItems.FERTILIZER_COMPOUND.stack(64));

			work(farm);
			helper.assertTrue(farm.getErrorLogic().contains(ForestryError.NO_RESOURCE), "expected NO_RESOURCE while the farm has no mycelium or podzol");

			for (int slot = 0; slot < MYCELIUM_STACKS; slot++) {
				MultiblockTestSupport.insertItem(level, members, MultiblockTestSupport.FARM_INV_SIZE, slot, new ItemStack(Blocks.MYCELIUM, 64));
			}
			work(farm);
			helper.assertFalse(farm.getErrorLogic().contains(ForestryError.NO_RESOURCE), "expected NO_RESOURCE to clear once mycelium was added");
			helper.succeed();
		});
	}

	private static void work(MultifarmController farm) {
		for (int i = 0; i < WORK_CYCLES; i++) {
			farm.getTankManager().fill(new FluidStack(Fluids.WATER, 10000), IFluidHandler.FluidAction.EXECUTE);
			farm.doWork();
		}
	}
}

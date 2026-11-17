package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.core.content.energy.blocks.EngineBlockType;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.energy.tiles.BiogasEngineBlockEntity;
import forestry.core.content.energy.tiles.CombustionEngineBlockEntity;
import forestry.core.features.CoreItems;
import forestry.core.platform.fluids.StandardTank;
import forestry.core.platform.gui.ContainerLiquidTanksHelper;
import forestry.core.platform.tile.ILiquidTankTile;

@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class PipetteTankDrainTest {
	private static final BlockPos ENGINE_POS = new BlockPos(8, 2, 8);
	// both engines keep their drain-locked tank at index 1
	private static final int LOCKED_TANK = 1;
	private static final int INITIAL_AMOUNT = 3 * FluidType.BUCKET_VOLUME;

	@GameTest(template = "empty")
	public static void pipetteDrainsCombustionCoolantTank(GameTestHelper helper) {
		helper.setBlock(ENGINE_POS, EnergyBlocks.ENGINES.get(EngineBlockType.COMBUSTION).defaultState());

		if (!(helper.getBlockEntity(ENGINE_POS) instanceof CombustionEngineBlockEntity engine)) {
			helper.fail("Expected a CombustionEngineBlockEntity at " + ENGINE_POS);
			return;
		}

		assertPipetteDrains(helper, engine, new ContainerLiquidTanksHelper<>(engine), new FluidStack(Fluids.WATER, INITIAL_AMOUNT));
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void pipetteDrainsBiogasHeatingTank(GameTestHelper helper) {
		helper.setBlock(ENGINE_POS, EnergyBlocks.ENGINES.get(EngineBlockType.BIOGAS).defaultState());

		if (!(helper.getBlockEntity(ENGINE_POS) instanceof BiogasEngineBlockEntity engine)) {
			helper.fail("Expected a BiogasEngineBlockEntity at " + ENGINE_POS);
			return;
		}

		assertPipetteDrains(helper, engine, new ContainerLiquidTanksHelper<>(engine), new FluidStack(Fluids.LAVA, INITIAL_AMOUNT));
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void pipetteClickIgnoresInvalidTankIndex(GameTestHelper helper) {
		helper.setBlock(ENGINE_POS, EnergyBlocks.ENGINES.get(EngineBlockType.COMBUSTION).defaultState());

		if (!(helper.getBlockEntity(ENGINE_POS) instanceof CombustionEngineBlockEntity engine)) {
			helper.fail("Expected a CombustionEngineBlockEntity at " + ENGINE_POS);
			return;
		}

		ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
		player.containerMenu.setCarried(CoreItems.PIPETTE.stack());

		ContainerLiquidTanksHelper<CombustionEngineBlockEntity> tanks = new ContainerLiquidTanksHelper<>(engine);
		// the index comes straight from the client packet
		tanks.handlePipetteClick(-1, player);
		tanks.handlePipetteClick(engine.getTankManager().getTanks(), player);
		helper.succeed();
	}

	private static void assertPipetteDrains(GameTestHelper helper, ILiquidTankTile engine, ContainerLiquidTanksHelper<?> tanks, FluidStack contents) {
		StandardTank tank = (StandardTank) engine.getTankManager().getTank(LOCKED_TANK);
		helper.assertFalse(tank.canDrain(), "tank " + LOCKED_TANK + " is no longer drain-locked, so this test covers nothing");
		tank.fillInternal(contents, IFluidHandler.FluidAction.EXECUTE);

		ServerPlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
		player.containerMenu.setCarried(CoreItems.PIPETTE.stack());

		tanks.handlePipetteClick(LOCKED_TANK, player);

		ItemStack pipette = player.containerMenu.getCarried();
		int inPipette = FluidUtil.getFluidContained(pipette).orElse(FluidStack.EMPTY).getAmount();
		helper.assertTrue(inPipette == FluidType.BUCKET_VOLUME, "expected a full pipette, found " + inPipette + " mB");
		helper.assertTrue(tank.getFluidAmount() == INITIAL_AMOUNT - inPipette,
			"pipette took " + inPipette + " mB but the tank went from " + INITIAL_AMOUNT + " to " + tank.getFluidAmount() + " mB");
	}
}

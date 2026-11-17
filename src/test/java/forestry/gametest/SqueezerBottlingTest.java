package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.inventory.SqueezerInventory;
import forestry.core.content.machines.tiles.SqueezerBlockEntity;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.fluids.StandardTank;

/**
 * Covers the glass bottle branch of {@link SqueezerInventory#fillContainers}.
 * <p>
 * The squeezer normally fills containers through {@code FluidHelper.fillContainers}, which delegates to the item's
 * fluid handler capability. A vanilla glass bottle has no such capability, so
 * {@code FluidHelper.isFillableEmptyContainer} rejects it and the method falls through to a hardcoded
 * fluid-to-bottle table. That table is what these tests pin down: it is the only place the mapping exists, there is
 * no recipe backing it, and a wrong entry is invisible until someone puts a bottle in a squeezer.
 * <p>
 * On 1.20.1 water mapped to {@link Items#HONEY_BOTTLE}, so a tank of water produced honey. The water test below is
 * the regression for that; the honey test guards the entry next to it, which was correct and must stay correct while
 * the two sit in the same if/else chain.
 * <p>
 * The tests call {@code fillContainers} directly rather than ticking the squeezer. {@code TileSqueezer.serverTick}
 * only reaches it once every 20 ticks and only after the machine has satisfied its energy and recipe requirements,
 * none of which this table depends on.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class SqueezerBottlingTest {
	private static final BlockPos SQUEEZER_POS = new BlockPos(8, 1, 8);

	/** Comfortably more than any single bottling costs, so the "not enough fluid" guard never interferes. */
	private static final int TANK_CONTENTS = 1000;
	private static final int BOTTLES_IN = 2;

	// The amounts InventorySqueezer charges per bottle. Honey is 200 rather than 250 because two honey drops of
	// 100mB each is what the Bottled Honey recipe costs.
	private static final int HONEY_PER_BOTTLE = 200;
	private static final int WATER_PER_BOTTLE = 250;

	/**
	 * The 1.20.1 regression: a tank of water must bottle into a water bottle, not a honey bottle.
	 */
	@GameTest(template = "empty")
	public static void waterBottlesIntoAWaterBottle(GameTestHelper helper) {
		SqueezerBlockEntity squeezer = bottlingSqueezer(helper, Fluids.WATER);

		bottleOnce(squeezer);

		ItemStack output = output(squeezer);
		// Deviation from 1.20.1: PotionUtils is gone, so the expected stack is built from a PotionContents component
		// and compared with isSameItemSameComponents — every potion is Items.POTION in 1.21.
		ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		helper.assertTrue(ItemStack.isSameItemSameComponents(output, waterBottle),
			"squeezing water into a glass bottle produced " + output + " instead of a water bottle");
		assertConsumed(helper, squeezer, WATER_PER_BOTTLE);
		helper.succeed();
	}

	/**
	 * The entry next to it, which was already correct. Kept so a future edit to the chain cannot quietly break honey
	 * while fixing something else.
	 */
	@GameTest(template = "empty")
	public static void honeyBottlesIntoAHoneyBottle(GameTestHelper helper) {
		SqueezerBlockEntity squeezer = bottlingSqueezer(helper, ForestryFluids.HONEY.getFluid());

		bottleOnce(squeezer);

		ItemStack output = output(squeezer);
		helper.assertTrue(output.is(Items.HONEY_BOTTLE),
			"squeezing honey into a glass bottle produced " + output.getItem() + " instead of a honey bottle");
		assertConsumed(helper, squeezer, HONEY_PER_BOTTLE);
		helper.succeed();
	}

	/**
	 * A fluid with no entry in the table must leave the bottle and the tank alone. Lava is the case the original
	 * author called out: an unusable container silently does nothing rather than raising an error.
	 */
	@GameTest(template = "empty")
	public static void anUnmappedFluidBottlesIntoNothing(GameTestHelper helper) {
		SqueezerBlockEntity squeezer = bottlingSqueezer(helper, Fluids.LAVA);

		bottleOnce(squeezer);

		helper.assertTrue(output(squeezer).isEmpty(),
			"squeezing lava into a glass bottle produced " + output(squeezer) + ", but lava has no bottled form");
		assertConsumed(helper, squeezer, 0);
		helper.succeed();
	}

	/**
	 * A tank holding less than one bottle's worth must not hand out a partially paid-for bottle.
	 */
	@GameTest(template = "empty")
	public static void aPartialTankBottlesNothing(GameTestHelper helper) {
		SqueezerBlockEntity squeezer = squeezerWith(helper, Fluids.WATER, WATER_PER_BOTTLE - 1);

		bottleOnce(squeezer);

		helper.assertTrue(output(squeezer).isEmpty(),
			"a tank holding less than one bottle's worth of water still produced " + output(squeezer));
		int left = tank(squeezer).getFluidAmount();
		helper.assertTrue(left == WATER_PER_BOTTLE - 1,
			"expected the tank to be untouched at " + (WATER_PER_BOTTLE - 1) + "mB, found " + left);
		helper.succeed();
	}

	private static SqueezerBlockEntity bottlingSqueezer(GameTestHelper helper, Fluid fluid) {
		return squeezerWith(helper, fluid, TANK_CONTENTS);
	}

	private static SqueezerBlockEntity squeezerWith(GameTestHelper helper, Fluid fluid, int amount) {
		helper.setBlock(SQUEEZER_POS, FactoryBlocks.PLAIN.get(FactoryBlockType.SQUEEZER).block());
		SqueezerBlockEntity squeezer = (SqueezerBlockEntity) helper.getBlockEntity(SQUEEZER_POS);

		// The product tank refuses external fills by design, so seed it the way workCycle() does.
		tank(squeezer).fillInternal(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
		inventory(squeezer).setItem(SqueezerInventory.SLOT_CAN_INPUT, new ItemStack(Items.GLASS_BOTTLE, BOTTLES_IN));
		return squeezer;
	}

	private static void bottleOnce(SqueezerBlockEntity squeezer) {
		inventory(squeezer).fillContainers(tank(squeezer).getFluid(), squeezer.getTankManager());
	}

	private static void assertConsumed(GameTestHelper helper, SqueezerBlockEntity squeezer, int fluidCost) {
		int expectedBottles = BOTTLES_IN - (fluidCost > 0 ? 1 : 0);
		int bottlesLeft = inventory(squeezer).getItem(SqueezerInventory.SLOT_CAN_INPUT).getCount();
		helper.assertTrue(bottlesLeft == expectedBottles,
			"expected " + expectedBottles + " glass bottle(s) left in the input slot, found " + bottlesLeft);

		int expectedFluid = TANK_CONTENTS - fluidCost;
		int fluidLeft = tank(squeezer).getFluidAmount();
		helper.assertTrue(fluidLeft == expectedFluid,
			"expected " + expectedFluid + "mB left in the tank, found " + fluidLeft);
	}

	private static SqueezerInventory inventory(SqueezerBlockEntity squeezer) {
		return (SqueezerInventory) squeezer.getInternalInventory();
	}

	private static StandardTank tank(SqueezerBlockEntity squeezer) {
		return (StandardTank) squeezer.getTankManager().getTank(0);
	}

	private static ItemStack output(SqueezerBlockEntity squeezer) {
		return inventory(squeezer).getItem(SqueezerInventory.SLOT_CAN_OUTPUT);
	}
}

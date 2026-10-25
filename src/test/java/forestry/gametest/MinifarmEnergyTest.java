package forestry.gametest;

import forestry.core.platform.tile.PoweredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.agriculture.features.MinifarmBlocks;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import forestry.core.content.energy.ForestryEnergyStorage;

/**
 * Regression suite for the minifarm doing its work for free.
 * <p>
 * {@link PoweredBlockEntity} charges for work in steps. It draws one step's worth of energy while
 * the work counter is below {@code stepsPerWorkCycle}, calls {@code workCycle} once the counter reaches it, and resets
 * the counter only when {@code workCycle} reports that a cycle finished. {@code AbstractMinifarmBlockEntity.workCycle}
 * threw away the result of {@code FarmManager.doWork} and always returned false, so the counter stayed pinned at the
 * top after the first cycle. That stopped every further energy draw while still running the farm every step.
 * <p>
 * Both tests drive the pending produce branch of {@code FarmManager.doWork}, which reports work done as soon as one
 * stack moves into the product inventory. That keeps the farm off soil, water and fertilizer setup, and leaves the
 * work cycle itself as the only thing under test.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class MinifarmEnergyTest {
	private static final BlockPos FARM_POS = new BlockPos(8, 2, 8);

	private static final Item PRODUCT = Items.APPLE;
	// one pending stack is stowed per work cycle, so keep the queue longer than any test runs
	private static final int PENDING_PRODUCTS = 16;

	// TilePowered runs one work step every 5 game ticks, and the minifarm takes 2 steps per cycle
	private static final int TICKS_PER_CYCLE = 10;
	private static final int CYCLES = 6;
	// a full step of slack, so the phase of the step interval cannot cost a whole cycle
	private static final int PAID_CYCLES_IDLE = CYCLES * TICKS_PER_CYCLE + 5;
	// the last cycle may not have been paid for yet when the assertion runs
	private static final int EXPECTED_PAID_CYCLES = CYCLES - 1;

	// long enough for many steps past the point where the broken tile kept working without power
	private static final int OUT_OF_ENERGY_IDLE = 100;

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void minifarmPaysEnergyForEveryWorkCycle(GameTestHelper helper) {
		AbstractMinifarmBlockEntity farm = placeFarm(helper);
		ForestryEnergyStorage energy = farm.getEnergyManager();

		energy.setEnergyStored(energy.getMaxEnergyStored());
		queueProducts(farm, PENDING_PRODUCTS);

		int stored = energy.getEnergyStored();
		int expected = EXPECTED_PAID_CYCLES * energyPerCycle(farm);

		helper.startSequence()
			.thenIdle(PAID_CYCLES_IDLE)
			.thenExecute(() -> {
				int spent = stored - energy.getEnergyStored();
				helper.assertTrue(spent >= expected,
					"expected the minifarm to spend at least " + expected + " energy over " + CYCLES
						+ " work cycles, it spent " + spent + ": it stopped paying after the first cycle");
			})
			.thenSucceed();
	}

	/**
	 * The other half of the same bug. A pinned work counter never falls below {@code stepsPerWorkCycle}, so the farm
	 * kept harvesting on an empty energy buffer.
	 */
	@GameTest(template = "empty", timeoutTicks = 200)
	public static void minifarmStopsWorkingWhenOutOfEnergy(GameTestHelper helper) {
		AbstractMinifarmBlockEntity farm = placeFarm(helper);
		ForestryEnergyStorage energy = farm.getEnergyManager();

		// exactly one cycle's worth, so the second cycle has nothing to spend
		energy.setEnergyStored(energyPerCycle(farm));
		queueProducts(farm, PENDING_PRODUCTS);

		helper.startSequence()
			.thenIdle(OUT_OF_ENERGY_IDLE)
			.thenExecute(() -> {
				int stowed = countProducts(farm);
				helper.assertTrue(stowed == 1,
					"expected the minifarm to stow 1 product on its one cycle of energy, it stowed " + stowed
						+ ": it kept working with an empty energy buffer");
			})
			.thenSucceed();
	}

	private static AbstractMinifarmBlockEntity placeFarm(GameTestHelper helper) {
		helper.setBlock(FARM_POS, MinifarmBlocks.MANAGED_PLANTER.get(MinifarmBlockType.ARBORETUM).defaultState());

		if (helper.getBlockEntity(FARM_POS) instanceof AbstractMinifarmBlockEntity farm) {
			return farm;
		}

		helper.fail("Expected an AbstractMinifarmBlockEntity at " + FARM_POS);
		throw new AssertionError("unreachable");
	}

	private static void queueProducts(AbstractMinifarmBlockEntity farm, int count) {
		for (int i = 0; i < count; i++) {
			farm.addPendingProduct(new ItemStack(PRODUCT));
		}
	}

	// TilePowered draws one step at a time and rounds each step up, so a cycle can cost more than energyPerWorkCycle
	private static int energyPerCycle(AbstractMinifarmBlockEntity farm) {
		int steps = farm.getStepsPerWorkCycle();
		return steps * (int) Math.ceil(farm.getEnergyPerWorkCycle() / (float) steps);
	}

	private static int countProducts(AbstractMinifarmBlockEntity farm) {
		int found = 0;
		for (int i = 0; i < farm.getContainerSize(); i++) {
			ItemStack stack = farm.getItem(i);
			if (stack.is(PRODUCT)) {
				found += stack.getCount();
			}
		}
		return found;
	}
}

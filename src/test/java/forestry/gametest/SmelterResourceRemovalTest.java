package forestry.gametest;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.content.machines.inventory.SmelterInventory;
import forestry.core.content.machines.tiles.SmelterBlockEntity;

/**
 * Covers {@link SmelterInventory#removeResources}, which is the only place the smelter decides whether it can pay
 * for a recipe and then deducts the cost.
 * <p>
 * The method was ported from 1.20.1 with two corrections, and these tests are what pin them down. The original
 * loop ran to {@code SLOT_INPUT_1 + SLOT_INPUT_COUNT - 1} exclusive, so with nine input slots it never read the
 * ninth. The partial-slot branch then recorded {@code amountToRemove - slotCount}, the shortfall, rather than what
 * the slot holds, so a cost spread over several slots was deducted wrongly and never fully satisfied.
 * <p>
 * Neither shows up on the common case of one slot holding the whole cost, which is why they survived. The tests
 * below all use costs that must be paid from more than one slot, or from the ninth slot alone.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class SmelterResourceRemovalTest {
	private static final BlockPos SMELTER_POS = new BlockPos(8, 1, 8);

	/** The last input slot, the one the original loop bound excluded. */
	private static final int LAST_INPUT_SLOT = SmelterInventory.SLOT_INPUT_COUNT - 1;

	/**
	 * A cost spread over three slots must be payable, and each slot must be emptied by what it actually held.
	 */
	@GameTest(template = "empty")
	public static void aCostSpreadOverSlotsIsPaidFromAllOfThem(GameTestHelper helper) {
		SmelterInventory inventory = smelter(helper);
		inventory.setItem(0, new ItemStack(Items.QUARTZ, 5));
		inventory.setItem(1, new ItemStack(Items.QUARTZ, 5));
		inventory.setItem(2, new ItemStack(Items.QUARTZ, 5));

		helper.assertTrue(inventory.removeResources(costOf(Items.QUARTZ, 12)),
			"12 quartz spread over three slots holding 15 in total could not be paid");

		int left = countIn(inventory, Items.QUARTZ);
		helper.assertTrue(left == 3, "expected 3 quartz left after paying 12 of 15, found " + left);
		helper.succeed();
	}

	/**
	 * The ninth input slot is real inventory. The original loop stopped before it.
	 */
	@GameTest(template = "empty")
	public static void theLastInputSlotIsUsable(GameTestHelper helper) {
		SmelterInventory inventory = smelter(helper);
		inventory.setItem(LAST_INPUT_SLOT, new ItemStack(Items.QUARTZ, 4));

		helper.assertTrue(inventory.removeResources(costOf(Items.QUARTZ, 4)),
			"the only quartz sat in input slot " + LAST_INPUT_SLOT + ", which was not counted");

		int left = countIn(inventory, Items.QUARTZ);
		helper.assertTrue(left == 0, "expected the last input slot to be emptied, found " + left + " quartz");
		helper.succeed();
	}

	/**
	 * Not enough across every slot must fail, and must leave the inventory untouched.
	 */
	@GameTest(template = "empty")
	public static void anUnpayableCostChangesNothing(GameTestHelper helper) {
		SmelterInventory inventory = smelter(helper);
		inventory.setItem(0, new ItemStack(Items.QUARTZ, 2));
		inventory.setItem(1, new ItemStack(Items.QUARTZ, 2));

		helper.assertTrue(!inventory.removeResources(costOf(Items.QUARTZ, 9)),
			"paying 9 quartz out of 4 should have failed");

		int left = countIn(inventory, Items.QUARTZ);
		helper.assertTrue(left == 4, "a failed payment still took items: expected 4 quartz, found " + left);
		helper.succeed();
	}

	/**
	 * Two ingredients that both match the same slot must not double spend it. The original overwrote the recorded
	 * amount per slot rather than accumulating, so the second ingredient erased the first one's claim.
	 */
	@GameTest(template = "empty")
	public static void twoIngredientsDoNotDoubleSpendOneSlot(GameTestHelper helper) {
		SmelterInventory inventory = smelter(helper);
		inventory.setItem(0, new ItemStack(Items.QUARTZ, 3));

		List<SizedIngredient> twoClaimsOnTheSameSlot = List.of(
			new SizedIngredient(Ingredient.of(Items.QUARTZ), 2),
			new SizedIngredient(Ingredient.of(Items.QUARTZ), 2));

		helper.assertTrue(!inventory.removeResources(twoClaimsOnTheSameSlot),
			"two costs of 2 were both paid from a single slot holding 3");

		int left = countIn(inventory, Items.QUARTZ);
		helper.assertTrue(left == 3, "a failed payment still took items: expected 3 quartz, found " + left);
		helper.succeed();
	}

	private static SmelterInventory smelter(GameTestHelper helper) {
		helper.setBlock(SMELTER_POS, FactoryBlocks.PLAIN.get(FactoryBlockType.SMELTER).block());
		SmelterBlockEntity smelter = (SmelterBlockEntity) helper.getBlockEntity(SMELTER_POS);
		return (SmelterInventory) smelter.getInternalInventory();
	}

	private static List<SizedIngredient> costOf(net.minecraft.world.item.Item item, int count) {
		return List.of(new SizedIngredient(Ingredient.of(item), count));
	}

	private static int countIn(SmelterInventory inventory, net.minecraft.world.item.Item item) {
		int total = 0;
		for (int slot = 0; slot < SmelterInventory.SLOT_INPUT_COUNT; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}
}

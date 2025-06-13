package forestry.core.inventory;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.tiles.EscritoireGame;
import forestry.core.tiles.TileEscritoire;
import forestry.core.utils.GeneticsUtil;
import forestry.core.utils.SlotUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InventoryEscritoire extends InventoryAdapterTile<TileEscritoire> {
	public static final short SLOT_ANALYZE = 0;
	public static final short SLOT_RESULTS_1 = 1;
	public static final short SLOTS_RESULTS_COUNT = 6;
	public static final short SLOT_INPUT_1 = 7;
	public static final short SLOTS_INPUT_COUNT = 5;

	public InventoryEscritoire(TileEscritoire escritoire) {
		super(escritoire, 12, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (slotIndex >= SLOT_INPUT_1 && slotIndex < SLOT_INPUT_1 + this.tile.getGame().getSampleSize(SLOTS_INPUT_COUNT)) {
			ItemStack specimen = getItem(SLOT_ANALYZE);
			if (specimen.isEmpty()) {
				return false;
			}
			IIndividual individual = IIndividualHandlerItem.getIndividual(specimen);
			if (individual != null) {
				return individual.getType().getResearchSuitability(individual.getSpecies().cast(), stack) > 0f;
			}
			return false;
		}

		return slotIndex == SLOT_ANALYZE && IIndividualHandlerItem.isIndividual(stack);
	}

	@Override
	public boolean isLocked(int slotIndex) {
		if (slotIndex == SLOT_ANALYZE) {
			return false;
		}

		if (getItem(SLOT_ANALYZE).isEmpty()) {
			return true;
		}

		if (SlotUtil.isSlotInRange(slotIndex, SLOT_INPUT_1, SLOTS_INPUT_COUNT)) {
			return slotIndex >= SLOT_INPUT_1 + this.tile.getGame().getSampleSize(SLOTS_INPUT_COUNT);
		}

		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack itemstack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, SLOT_RESULTS_1, SLOTS_RESULTS_COUNT);
	}

	@Override
	public void setItem(int slotIndex, ItemStack itemstack) {
		super.setItem(slotIndex, itemstack);
		if (slotIndex == SLOT_ANALYZE) {
			ItemStack specimen = getItem(SLOT_ANALYZE);

			if (!IIndividualHandlerItem.isIndividual(specimen) && !specimen.isEmpty()) {
				specimen = GeneticsUtil.convertToGeneticEquivalent(specimen);
				if (IIndividualHandlerItem.isIndividual(specimen)) {
					super.setItem(SLOT_ANALYZE, specimen);
				}
			}
			Level level = this.tile.getLevel();
			if (level != null && !level.isClientSide) {
				EscritoireGame game = this.tile.getGame();
				game.initialize(specimen);
			}
		}
	}
}

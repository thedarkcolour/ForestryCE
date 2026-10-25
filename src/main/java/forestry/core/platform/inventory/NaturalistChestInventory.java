package forestry.core.platform.inventory;

import forestry.api.core.genetics.ISpeciesType;
import forestry.core.platform.tile.NaturalistChestBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class NaturalistChestInventory extends BlockEntityInventoryAdapter<NaturalistChestBlockEntity> {
	public NaturalistChestInventory(NaturalistChestBlockEntity tile) {
		super(tile, 128, "Items");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		// Null when the jar owning this chest's species type is not installed, so the chest takes nothing
		ISpeciesType<?, ?> speciesType = this.tile.getSpeciesTypeSafe();
		return speciesType != null && speciesType.isMember(stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return true;
	}
}

package forestry.core.inventory;

import forestry.api.genetics.ISpeciesType;
import forestry.core.tiles.TileNaturalistChest;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class InventoryNaturalistChest extends InventoryAdapterTile<TileNaturalistChest> {
	private final ISpeciesType speciesRoot;

	public InventoryNaturalistChest(TileNaturalistChest tile, ISpeciesType speciesRoot) {
		super(tile, 125, "Items");
		this.speciesRoot = speciesRoot;
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return this.speciesRoot.isMember(stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return true;
	}
}

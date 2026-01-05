package forestry.apiculture.inventory;

import forestry.api.IForestryApi;
import forestry.apiculture.multiblock.TileAlvearySwarmer;
import forestry.core.inventory.InventoryAdapterTile;
import net.minecraft.world.item.ItemStack;

public class InventorySwarmer extends InventoryAdapterTile<TileAlvearySwarmer> {
	public InventorySwarmer(TileAlvearySwarmer alvearySwarmer) {
		super(alvearySwarmer, 4, "SwarmInv");
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return IForestryApi.INSTANCE.getHiveManager().getSwarmingMaterialChance(stack.getItem()) != 0f;
	}
}

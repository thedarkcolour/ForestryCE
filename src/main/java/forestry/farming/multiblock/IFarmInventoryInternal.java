package forestry.farming.multiblock;

import java.util.ArrayDeque;

import net.minecraft.world.item.ItemStack;

import forestry.api.farming.IFarmInventory;

public interface IFarmInventoryInternal extends IFarmInventory {
	int getFertilizerValue();

	boolean useFertilizer();

	void stowProducts(Iterable<ItemStack> harvested, ArrayDeque<ItemStack> pendingProduce);

	boolean tryAddPendingProduce(ArrayDeque<ItemStack> pendingProduce);
}

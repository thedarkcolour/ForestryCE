package forestry.farming.compat;

import net.minecraft.world.item.ItemStack;

import forestry.api.circuits.ICircuit;
import forestry.api.farming.IFarmType;

public record FarmingInfoRecipe(ItemStack tube, IFarmType properties, ICircuit circuit) {
}

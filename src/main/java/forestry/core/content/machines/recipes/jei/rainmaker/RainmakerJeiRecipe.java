package forestry.core.content.machines.recipes.jei.rainmaker;

import forestry.api.core.machines.fuels.RainmakerSubstrate;
import net.minecraft.world.item.ItemStack;

public record RainmakerJeiRecipe(ItemStack substrate, RainmakerSubstrate fuel) {
}

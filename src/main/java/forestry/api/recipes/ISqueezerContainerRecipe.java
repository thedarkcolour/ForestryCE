package forestry.api.recipes;

import net.minecraft.world.item.ItemStack;

public interface ISqueezerContainerRecipe extends IForestryRecipe, ISqueezerRecipe {
	ItemStack getEmptyContainer();

	int getProcessingTime();

	ItemStack getRemnants();

	float getRemnantsChance();
}

package forestry.lepidopterology.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import forestry.Forestry;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.lepidopterology.features.LepidopterologyRecipes;

public class ButterflyMatingRecipe extends CustomRecipe {
	public ButterflyMatingRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer grid, Level level) {
		int containerSize = grid.getContainerSize();
		boolean hasButterfly = false;
		boolean hasSerum = false;

		for (int i = 0; i < containerSize; ++i) {
			ItemStack stack = grid.getItem(i);

			if (!stack.isEmpty()) {
				IIndividualHandlerItem handler = IIndividualHandlerItem.get(stack);

				if (handler == null) {
					return false;
				} else {
					if (handler.getStage() == ButterflyLifeStage.BUTTERFLY) {
						if (hasButterfly) {
							return false;
						} else {
							hasButterfly = true;
						}
					} else if (handler.getStage() == ButterflyLifeStage.SERUM) {
						if (hasSerum) {
							return false;
						} else {
							hasSerum = true;
						}
					} else {
						return false;
					}
				}
			}
		}

		return hasButterfly && hasSerum;
	}

	@Override
	public ItemStack assemble(CraftingContainer grid, RegistryAccess lookup) {
		IButterfly butterfly = null;
		IIndividual serum = null;
		int containerSize = grid.getContainerSize();

		for (int i = 0; i < containerSize; i++) {
			IIndividualHandlerItem handler = IIndividualHandlerItem.get(grid.getItem(i));

			if (handler != null) {
				if (handler.getStage() == ButterflyLifeStage.BUTTERFLY) {
					butterfly = (IButterfly) handler.getIndividual();
				} else if (handler.getStage() == ButterflyLifeStage.SERUM) {
					serum = handler.getIndividual();
				}
			}
		}

		if (butterfly != null && serum != null) {
			IButterfly copy = butterfly.copy();
			copy.setMate(serum.getGenome());
			return copy.createStack(ButterflyLifeStage.BUTTERFLY);
		}

		Forestry.LOGGER.warn("Failed to craft butterfly");
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return LepidopterologyRecipes.MATING_SERIALIZER.get();
	}
}

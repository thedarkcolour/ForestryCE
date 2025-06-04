package forestry.lepidopterology.recipe;

import forestry.Forestry;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.lepidopterology.features.LepidopterologyRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ButterflyMatingRecipe extends CustomRecipe {
	public ButterflyMatingRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		int containerSize = input.size();
		boolean hasButterfly = false;
		boolean hasSerum = false;

		for (int i = 0; i < containerSize; ++i) {
			ItemStack stack = input.getItem(i);

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
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		IButterfly butterfly = null;
		IIndividual serum = null;
		int containerSize = input.size();

		for (int i = 0; i < containerSize; i++) {
			IIndividualHandlerItem handler = IIndividualHandlerItem.get(input.getItem(i));

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
		return LepidopterologyRecipes.MATING_SERIALIZER.value();
	}
}

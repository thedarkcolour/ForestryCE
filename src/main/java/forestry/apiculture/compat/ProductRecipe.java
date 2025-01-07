package forestry.apiculture.compat;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.item.ItemStack;

import forestry.api.core.IProduct;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.core.utils.ItemStackUtil;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

class ProductRecipe {
	final ISpecies<?> species;

	// Used for recipe lookup
	final List<ItemStack> inputs;
	// Null when species is not IProductProducer
	@Nullable
	final Object2FloatOpenCustomHashMap<ItemStack> products;
	// Null when species is not ISpecialtyProducer
	@Nullable
	final Object2FloatOpenCustomHashMap<ItemStack> specialties;

	// Displayed in GUI
	final ItemStack displayInput;

	ProductRecipe(ISpecies<?> species) {
		this.species = species;

		// gather species stacks as inputs
		ItemStack displayInput = null;
		List<ItemStack> inputs = new ArrayList<>();
		ILifeStage displayStage = species.getType().getTypeForMutation(2);

		for (ILifeStage iLifeStage : species.getType().getLifeStages()) {
			ItemStack stack = species.createStack(iLifeStage);
			inputs.add(stack);

			// save the queen or equivalent as the display stack
			if (iLifeStage == displayStage) {
				displayInput = stack;
			}
		}

		this.inputs = inputs;
		this.displayInput = Objects.requireNonNull(displayInput);

		this.products = species instanceof IProductProducer producer ? createProductsList(producer.getProducts()) : null;
		this.specialties = species instanceof ISpecialtyProducer producer ? createProductsList(producer.getSpecialties()) : null;
	}

	private Object2FloatOpenCustomHashMap<ItemStack> createProductsList(List<IProduct> productGetter) {
		Object2FloatOpenCustomHashMap<ItemStack> list = new Object2FloatOpenCustomHashMap<>(productGetter.size(), ItemStackUtil.ITEM_STACK_STRATEGY);

		for (IProduct product : productGetter) {
			ItemStack stack = product.createStack();

			list.put(stack, product.chance());
		}

		return list;
	}
}

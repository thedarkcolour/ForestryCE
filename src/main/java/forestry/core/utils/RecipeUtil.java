package forestry.core.utils;

import forestry.api.recipes.*;
import forestry.core.ClientsideCode;
import forestry.core.fluids.FluidHelper;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.factory.recipes.FabricatorSmeltingRecipe;
import forestry.modules.features.FeatureRecipeType;
import forestry.worktable.inventory.WorktableCraftingContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeUtil {
	/**
	 * @return The global registry manager. {@code null} on server when there is no server, or when there is no world (on client).
	 */
	@Nullable
	public static RecipeManager getRecipeManager() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? (FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.getRecipeManager() : null) : server.getRecipeManager();
	}

	public static RegistryAccess getRegistryAccess() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? (FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.getRegistryAccess() : null) : server.registryAccess();
	}

	public static Registry<Fluid> getFluidRegistry() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? (FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.getFluidRegistry() : null) : server.registryAccess().registryOrThrow(Registries.FLUID);
	}

	@Nullable
	public static <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(@Nullable ResourceLocation name) {
		if (name == null) {
			return null;
		}
		RecipeManager manager = getRecipeManager();
		if (manager == null) {
			return null;
		}
		Optional<RecipeHolder<?>> holder = manager.byKey(name);
		return (RecipeHolder<T>) holder.orElse(null);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> getRecipes(RecipeType<T> recipeType, I inventory, @Nullable Level world) {
		RecipeManager manager = getRecipeManager();
		if (manager == null || world == null) {
			return Collections.emptyList();
		}
		return manager.getRecipesFor(recipeType, inventory, world);
	}

	public static List<RecipeHolder<CraftingRecipe>> findMatchingRecipes(CraftingInput inventory, Level level) {
		return level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, inventory, level);
	}

	// Returns a crafting matrix for a certain recipe using available items
	@Nullable
	public static WorktableCraftingContainer getUsedMatrix(WorktableCraftingContainer originalMatrix, NonNullList<ItemStack> availableItems, Level level, CraftingRecipe recipe) {
		CraftingInput input = originalMatrix.asCraftInput();

		if (!recipe.matches(input, level)) {
			return null;
		}

		ItemStack expectedOutput = recipe.assemble(input, level.registryAccess());
		if (expectedOutput.isEmpty()) {
			return null;
		}

		WorktableCraftingContainer usedMatrix = new WorktableCraftingContainer();
		List<ItemStack> stockCopy = ItemStackUtil.condenseStacks(availableItems);

		for (int slot = 0; slot < originalMatrix.getContainerSize(); slot++) {
			ItemStack stack = originalMatrix.getItem(slot);

			if (!stack.isEmpty()) {
				ItemStack equivalent = getCraftingEquivalent(stockCopy, originalMatrix, slot, level, recipe, expectedOutput);
				if (equivalent.isEmpty()) {
					return null;
				} else {
					usedMatrix.setItem(slot, equivalent);
				}
			}
		}

		CraftingInput usedInput = usedMatrix.asCraftInput();

		if (recipe.matches(usedInput, level)) {
			ItemStack output = recipe.assemble(usedInput, level.registryAccess());
			if (ItemStack.matches(output, expectedOutput)) {
				return usedMatrix;
			}
		}

		return null;
	}

	private static ItemStack getCraftingEquivalent(List<ItemStack> stockCopy, WorktableCraftingContainer originalMatrix, int slot, Level level, CraftingRecipe recipe, ItemStack expectedOutput) {
		ItemStack originalStack = originalMatrix.getItem(slot);
		CraftingInput input = originalMatrix.asCraftInput();

		for (ItemStack stockStack : stockCopy) {
			if (!stockStack.isEmpty()) {
				ItemStack singleStockStack = stockStack.copy();
				singleStockStack.setCount(1);
				originalMatrix.setItem(slot, singleStockStack);

				if (recipe.matches(input, level)) {
					ItemStack output = recipe.assemble(input, level.registryAccess());
					if (ItemStack.matches(output, expectedOutput)) {
						originalMatrix.setItem(slot, originalStack);
						return stockStack.split(1);
					}
				}
			}
		}
		originalMatrix.setItem(slot, originalStack);
		return ItemStack.EMPTY;
	}

	@Nullable
	public static RecipeHolder<IHygroregulatorRecipe> getHygroRegulatorRecipe(RecipeManager manager, FluidStack input) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.HYGROREGULATOR, recipe -> recipe.getInputFluid().test(input));
	}

	@Nullable
	public static RecipeHolder<IFermenterRecipe> getFermenterRecipe(RecipeManager manager, ItemStack inputItem, FluidStack inputFluid) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FERMENTER, recipe -> recipe.matches(inputItem, inputFluid));
	}

	public static boolean isFermenterInput(RecipeManager manager, ItemStack stack) {
		return getRecipes(manager, FactoryRecipeTypes.FERMENTER)
			.anyMatch(recipe -> recipe.value().getInputItem().test(stack));
	}

	@Nullable
	public static RecipeHolder<ICarpenterRecipe> getCarpenterRecipe(RecipeManager manager, FluidStack fluid, ItemStack boxStack, Container craftingInventory, Level level) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CARPENTER, recipe -> recipe.matches(fluid, boxStack, craftingInventory, level));
	}

	public static boolean isCarpenterBox(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CARPENTER, recipe -> recipe.getBox().test(stack)) != null;
	}

	// Returns true if the item is part of any squeezer recipe.
	public static boolean isSqueezerIngredient(RecipeManager manager, ItemStack stack) {
		return getRecipes(manager, FactoryRecipeTypes.SQUEEZER).anyMatch(recipe -> {
			for (Ingredient ingredient : recipe.value().getInputs()) {
				if (ingredient.test(stack)) {
					return true;
				}
			}
			return false;
		});
	}

	@Nullable
	public static RecipeHolder<ISqueezerContainerRecipe> getSqueezerContainerRecipe(RecipeManager manager, ItemStack stack) {
		if (!FluidHelper.isDrainableFilledContainer(stack)) {
			return null;
		}
		return getMatchingRecipe(manager, FactoryRecipeTypes.SQUEEZER_CONTAINER, recipe -> ItemStack.isSameItem(recipe.getEmptyContainer(), stack));
	}

	@Nullable
	public static RecipeHolder<ICentrifugeRecipe> getCentrifugeRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CENTRIFUGE, recipe -> recipe.getInput().test(stack));
	}

	@Nullable
	public static RecipeHolder<FabricatorSmeltingRecipe> getFabricatorMeltingRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR_SMELTING, recipe -> recipe.input().test(stack));
	}

	@Nullable
	public static RecipeHolder<IFabricatorRecipe> getFabricatorRecipe(RecipeManager manager, Level level, FluidStack liquid, ItemStack stack, Container inventory) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR, recipe -> recipe.matches(level, liquid, stack, inventory));
	}

	public static boolean isFabricatorPlan(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR, recipe -> recipe.getPlan().test(stack)) != null;
	}

	@Nullable
	public static RecipeHolder<IMoistenerRecipe> getMoistenerRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.MOISTENER, recipe -> recipe.getInput().test(stack));
	}

	@Nullable
	public static RecipeHolder<ISqueezerRecipe> getSqueezerRecipe(RecipeManager manager, List<ItemStack> inputs) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.SQUEEZER, recipe -> ItemStackUtil.createConsume(recipe.getInputs(), inputs.size(), inputs::get, false).length > 0);
	}

	@Nullable
	public static RecipeHolder<IStillRecipe> getStillRecipe(RecipeManager manager, FluidStack input) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.STILL, recipe -> recipe.matches(input));
	}

	@Nullable
	private static <R extends Recipe<I>, I extends RecipeInput> RecipeHolder<R> getMatchingRecipe(RecipeManager manager, FeatureRecipeType<R> type, Predicate<R> matcher) {
		return getRecipes(manager, type)
			.filter(holder -> matcher.test(holder.value()))
			.findFirst()
			.orElse(null);
	}

	public static <R extends Recipe<I>, I extends RecipeInput> Stream<RecipeHolder<R>> getRecipes(RecipeManager manager, FeatureRecipeType<R> type) {
		return manager.byType(type.type()).stream();
	}

	public static <R extends Recipe<C>, C extends RecipeInput> Set<ResourceLocation> getTargetFluidsFromIngredients(RecipeManager manager, RecipeType<R> type, Function<R, FluidIngredient> targetFluid) {
		return getTargetFluids(manager, type, recipe -> Arrays.stream(targetFluid.apply(recipe).getStacks()).map(FluidStack::getFluid));
	}

	public static <R extends Recipe<I>, I extends RecipeInput> Set<ResourceLocation> getTargetFluids(RecipeManager manager, RecipeType<R> type, Function<R, Stream<Fluid>> targetFluid) {
		return manager.byType(type).stream()
			.flatMap(holder -> targetFluid.apply(holder.value()).map(ModUtil::getRegistryName))
			.collect(Collectors.toSet());
	}

	public static <R extends Recipe<I>, I extends RecipeInput> RecipeHolder<R> getRecipeByOutput(FeatureRecipeType<R> recipeType, RegistryAccess registryAccess, ItemStack output) {
		return getRecipes(getRecipeManager(), recipeType)
			.filter(recipe -> ItemStack.isSameItem(recipe.value().getResultItem(registryAccess), output))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Couldn't find a recipe with output: " + output));
	}

	@Nullable
	public static <T extends Recipe<?>> T unwrap(@Nullable RecipeHolder<T> holder) {
		return holder == null ? null : holder.value();
	}
}

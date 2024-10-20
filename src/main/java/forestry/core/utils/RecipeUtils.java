package forestry.core.utils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.server.ServerLifecycleHooks;

import net.minecraftforge.fml.loading.FMLEnvironment;

import forestry.api.recipes.ICarpenterRecipe;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.IFabricatorRecipe;
import forestry.api.recipes.IFabricatorSmeltingRecipe;
import forestry.api.recipes.IFermenterRecipe;
import forestry.api.recipes.IHygroregulatorRecipe;
import forestry.api.recipes.IMoistenerRecipe;
import forestry.api.recipes.ISqueezerContainerRecipe;
import forestry.api.recipes.ISqueezerRecipe;
import forestry.api.recipes.IStillRecipe;
import forestry.core.ClientsideCode;
import forestry.core.fluids.FluidHelper;
import forestry.factory.features.FactoryRecipeTypes;
import forestry.modules.features.FeatureRecipeType;
import forestry.worktable.inventory.WorktableCraftingContainer;

public class RecipeUtils {
	/**
	 * @return The global registry manager. {@code null} on server when there is no server, or when there is no world (on client).
	 */
	@Nullable
	public static RecipeManager getRecipeManager() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? (FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.getRecipeManager() : null) : server.getRecipeManager();
	}

	public static Registry<Fluid> getFluidRegistry() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? (FMLEnvironment.dist == Dist.CLIENT ? ClientsideCode.getFluidRegistry() : null) : server.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
	}

	@Nullable
	public static <C extends Container, T extends Recipe<C>> Recipe<C> getRecipe(RecipeType<T> recipeType, ResourceLocation name, @Nullable Level world) {
		RecipeManager manager = getRecipeManager();
		if (manager == null) {
			return null;
		}
		return manager.byType(recipeType).get(name);
	}

	public static <C extends Container, T extends Recipe<C>> List<T> getRecipes(RecipeType<T> recipeType, C inventory, @Nullable Level world) {
		RecipeManager manager = getRecipeManager();
		if (manager == null || world == null) {
			return Collections.emptyList();
		}
		return manager.getRecipesFor(recipeType, inventory, world);
	}

	public static List<CraftingRecipe> findMatchingRecipes(CraftingContainer inventory, Level level) {
		return level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, inventory, level);
	}

	// Returns a crafting matrix for a certain recipe using available items
	@Nullable
	public static WorktableCraftingContainer getUsedMatrix(WorktableCraftingContainer originalMatrix, NonNullList<ItemStack> availableItems, Level level, CraftingRecipe recipe) {
		if (!recipe.matches(originalMatrix, level)) {
			return null;
		}

		ItemStack expectedOutput = recipe.assemble(originalMatrix);
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

		if (recipe.matches(usedMatrix, level)) {
			ItemStack output = recipe.assemble(usedMatrix);
			if (ItemStack.matches(output, expectedOutput)) {
				return usedMatrix;
			}
		}

		return null;
	}

	private static ItemStack getCraftingEquivalent(List<ItemStack> stockCopy, WorktableCraftingContainer originalMatrix, int slot, Level level, CraftingRecipe recipe, ItemStack expectedOutput) {
		ItemStack originalStack = originalMatrix.getItem(slot);
		for (ItemStack stockStack : stockCopy) {
			if (!stockStack.isEmpty()) {
				ItemStack singleStockStack = stockStack.copy();
				singleStockStack.setCount(1);
				originalMatrix.setItem(slot, singleStockStack);

				if (recipe.matches(originalMatrix, level)) {
					ItemStack output = recipe.assemble(originalMatrix);
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
	public static IHygroregulatorRecipe getHygroRegulatorRecipe(RecipeManager manager, FluidStack input) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.HYGROREGULATOR, recipe -> recipe.getInputFluid().isFluidEqual(input));
	}

	@Nullable
	public static IFermenterRecipe getFermenterRecipe(RecipeManager manager, ItemStack inputItem, FluidStack inputFluid) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FERMENTER, recipe -> recipe.matches(inputItem, inputFluid));
	}

	public static boolean isFermenterInput(RecipeManager manager, ItemStack stack) {
		return getRecipes(manager, FactoryRecipeTypes.FERMENTER)
				.anyMatch(recipe -> recipe.getInputItem().test(stack));
	}

	@Nullable
	public static ICarpenterRecipe getCarpenterRecipe(RecipeManager manager, FluidStack fluid, ItemStack boxStack, Container craftingInventory, Level level) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CARPENTER, recipe -> recipe.matches(fluid, boxStack, craftingInventory, level));
	}

	public static boolean isCarpenterBox(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CARPENTER, recipe -> recipe.getBox().test(stack)) != null;
	}

	// Returns true if the item is part of any squeezer recipe.
	public static boolean isSqueezerIngredient(RecipeManager manager, ItemStack stack) {
		return getRecipes(manager, FactoryRecipeTypes.SQUEEZER).anyMatch(recipe -> {
			for (Ingredient ingredient : recipe.getInputs()) {
				if (ingredient.test(stack)) {
					return true;
				}
			}
			return false;
		});
	}

	@Nullable
	public static ISqueezerContainerRecipe getSqueezerContainerRecipe(RecipeManager manager, ItemStack stack) {
		if (!FluidHelper.isDrainableFilledContainer(stack)) {
			return null;
		}
		return getMatchingRecipe(manager, FactoryRecipeTypes.SQUEEZER_CONTAINER, recipe -> recipe.getEmptyContainer().sameItem(stack));
	}

	@Nullable
	public static ICentrifugeRecipe getCentrifugeRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.CENTRIFUGE, recipe -> recipe.getInput().test(stack));
	}

	@Nullable
	public static IFabricatorSmeltingRecipe getFabricatorMeltingRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR_SMELTING, recipe -> recipe.getInput().test(stack));
	}

	@Nullable
	public static IFabricatorRecipe getFabricatorRecipe(RecipeManager manager, Level level, FluidStack liquid, ItemStack stack, Container inventory) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR, recipe -> recipe.matches(level, liquid, stack, inventory));
	}

	public static boolean isFabricatorPlan(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.FABRICATOR, recipe -> recipe.getPlan().test(stack)) != null;
	}

	@Nullable
	public static IMoistenerRecipe getMoistenerRecipe(RecipeManager manager, ItemStack stack) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.MOISTENER, recipe -> recipe.getInput().test(stack));
	}

	@Nullable
	public static ISqueezerRecipe getSqueezerRecipe(RecipeManager manager, List<ItemStack> inputs) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.SQUEEZER, recipe -> ItemStackUtil.createConsume(recipe.getInputs(), inputs.size(), inputs::get, false).length > 0);
	}

	@Nullable
	public static IStillRecipe getStillRecipe(RecipeManager manager, FluidStack input) {
		return getMatchingRecipe(manager, FactoryRecipeTypes.STILL, recipe -> recipe.matches(input));
	}

	@Nullable
	private static <R extends Recipe<C>, C extends Container> R getMatchingRecipe(RecipeManager manager, FeatureRecipeType<R> type, Predicate<R> matcher) {
		return getRecipes(manager, type)
				.filter(matcher)
				.findFirst()
				.orElse(null);
	}

	public static <R extends Recipe<C>, C extends Container> Stream<R> getRecipes(RecipeManager manager, FeatureRecipeType<R> type) {
		return manager.byType(type.type()).values().stream();
	}

	public static <R extends Recipe<C>, C extends Container> Set<ResourceLocation> getTargetFluidsFromStacks(RecipeManager manager, RecipeType<R> type, Function<R, FluidStack> targetFluid) {
		return getTargetFluids(manager, type, recipe -> targetFluid.apply(recipe).getFluid());
	}

	public static <R extends Recipe<C>, C extends Container> Set<ResourceLocation> getTargetFluids(RecipeManager manager, RecipeType<R> type, Function<R, Fluid> targetFluid) {
		return manager.byType(type).values().stream()
				.map(value -> ModUtil.getRegistryName(targetFluid.apply(value)))
				.collect(Collectors.toSet());
	}

	public static <R extends Recipe<C>, C extends Container> R getRecipeByOutput(FeatureRecipeType<R> recipeType, ItemStack output) {
		return getRecipes(getRecipeManager(), recipeType)
				.filter(recipe -> recipe.getResultItem().sameItem(output))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Couldn't find a recipe with output: " + output));
	}
}

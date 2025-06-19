package forestry.worktable.recipes;

import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipeMemory implements INbtWritable, IStreamable {
	private static final int CAPACITY = 9;

	private final List<MemorizedRecipe> memorizedRecipes = new ArrayList<>(CAPACITY);
	private long lastUpdate;

	public RecipeMemory(RegistryFriendlyByteBuf buffer) {
		readData(buffer);
	}

	public RecipeMemory() {
	}

	public RecipeMemory(CompoundTag nbt, HolderLookup.Provider registries) {
		if (!nbt.contains("RecipeMemory")) {
			return;
		}

		ListTag memoryNbt = nbt.getList("RecipeMemory", Tag.TAG_COMPOUND);

		for (int j = 0; j < memoryNbt.size(); ++j) {
			CompoundTag recipeNbt = memoryNbt.getCompound(j);
			MemorizedRecipe recipe = new MemorizedRecipe(recipeNbt, registries);

			if (recipe.hasSelectedRecipe()) {
				this.memorizedRecipes.add(recipe);
			}
		}
	}

	public long getLastUpdate() {
		return this.lastUpdate;
	}

	public void memorizeRecipe(long worldTime, MemorizedRecipe recipe, Level world) {
		RecipeHolder<CraftingRecipe> selectedRecipe = recipe.getSelectedRecipe();
		if (selectedRecipe == null) {
			return;
		}

		this.lastUpdate = worldTime;
		recipe.updateLastUse(this.lastUpdate);

		if (recipe.hasRecipeConflict()) {
			recipe.removeRecipeConflicts();
		}

		// update existing matching recipes
		MemorizedRecipe memory = getExistingMemorizedRecipe(selectedRecipe);
		if (memory != null) {
			updateExistingRecipe(memory, recipe);
			return;
		}

		// add a new recipe
		if (this.memorizedRecipes.size() < CAPACITY) {
			this.memorizedRecipes.add(recipe);
		} else {
			MemorizedRecipe oldest = getOldestUnlockedRecipe();
			if (oldest != null) {
				this.memorizedRecipes.remove(oldest);
				this.memorizedRecipes.add(recipe);
			}
		}
	}

	private void updateExistingRecipe(MemorizedRecipe existingRecipe, MemorizedRecipe updatedRecipe) {
		if (existingRecipe.isLocked() != updatedRecipe.isLocked()) {
			updatedRecipe.toggleLock();
		}
		int index = this.memorizedRecipes.indexOf(existingRecipe);
		this.memorizedRecipes.set(index, updatedRecipe);
	}

	@Nullable
	private MemorizedRecipe getOldestUnlockedRecipe() {
		MemorizedRecipe oldest = null;
		for (MemorizedRecipe existing : this.memorizedRecipes) {
			if (oldest != null && oldest.getLastUsed() < existing.getLastUsed()) {
				continue;
			}

			if (!existing.isLocked()) {
				oldest = existing;
			}
		}
		return oldest;
	}

	@Nullable
	public MemorizedRecipe getRecipe(int recipeIndex) {
		if (recipeIndex < 0 || recipeIndex >= this.memorizedRecipes.size()) {
			return null;
		}
		return this.memorizedRecipes.get(recipeIndex);
	}

	//Client Only
	public ItemStack getRecipeDisplayOutput(Level level, int recipeIndex) {
		MemorizedRecipe recipe = getRecipe(recipeIndex);
		if (recipe == null) {
			return ItemStack.EMPTY;
		}
		return recipe.getOutputIcon(level);
	}

	public boolean isLocked(int recipeIndex) {
		MemorizedRecipe recipe = getRecipe(recipeIndex);
		return recipe != null && recipe.isLocked();
	}

	public void toggleLock(long worldTime, int recipeIndex) {
		this.lastUpdate = worldTime;
		if (this.memorizedRecipes.size() > recipeIndex) {
			this.memorizedRecipes.get(recipeIndex).toggleLock();
		}
	}

	@Nullable
	private MemorizedRecipe getExistingMemorizedRecipe(@Nullable RecipeHolder<CraftingRecipe> recipe) {
		if (recipe != null) {
			for (MemorizedRecipe memorizedRecipe : this.memorizedRecipes) {
				if (memorizedRecipe.hasRecipe(recipe)) {
					return memorizedRecipe;
				}
			}
		}

		return null;
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		ListTag listNBT = new ListTag();
		for (MemorizedRecipe recipe : this.memorizedRecipes) {
			if (recipe != null && recipe.hasSelectedRecipe()) {
				CompoundTag recipeNbt = new CompoundTag();
				recipe.write(recipeNbt, registries);
				listNBT.add(recipeNbt);
			}
		}
		compoundNBT.put("RecipeMemory", listNBT);
		return compoundNBT;
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		NetworkUtil.writeStreamables(buffer, this.memorizedRecipes);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		NetworkUtil.readStreamables(buffer, this.memorizedRecipes, MemorizedRecipe::new);
	}

	public void copy(RecipeMemory memory) {
		this.memorizedRecipes.clear();
		this.memorizedRecipes.addAll(memory.memorizedRecipes);
	}
}

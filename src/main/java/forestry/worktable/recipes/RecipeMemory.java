package forestry.worktable.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.core.utils.NetworkUtil;

public class RecipeMemory implements INbtWritable, IStreamable {
	private static final int CAPACITY = 9;

	private final List<MemorizedRecipe> memorizedRecipes = new ArrayList<>(CAPACITY);
	private long lastUpdate;

	public RecipeMemory(FriendlyByteBuf buffer) {
		readData(buffer);
	}

	public RecipeMemory() {
	}

	public RecipeMemory(CompoundTag nbt) {
		if (!nbt.contains("RecipeMemory")) {
			return;
		}

		ListTag memoryNbt = nbt.getList("RecipeMemory", Tag.TAG_COMPOUND);

		for (int j = 0; j < memoryNbt.size(); ++j) {
			CompoundTag recipeNbt = memoryNbt.getCompound(j);
			MemorizedRecipe recipe = new MemorizedRecipe(recipeNbt);

			if (recipe.hasSelectedRecipe()) {
				memorizedRecipes.add(recipe);
			}
		}
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void memorizeRecipe(long worldTime, MemorizedRecipe recipe, Level world) {
		CraftingRecipe selectedRecipe = recipe.getSelectedRecipe();
		if (selectedRecipe == null) {
			return;
		}

		lastUpdate = worldTime;
		recipe.updateLastUse(lastUpdate);

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
		if (memorizedRecipes.size() < CAPACITY) {
			memorizedRecipes.add(recipe);
		} else {
			MemorizedRecipe oldest = getOldestUnlockedRecipe();
			if (oldest != null) {
				memorizedRecipes.remove(oldest);
				memorizedRecipes.add(recipe);
			}
		}
	}

	private void updateExistingRecipe(MemorizedRecipe existingRecipe, MemorizedRecipe updatedRecipe) {
		if (existingRecipe.isLocked() != updatedRecipe.isLocked()) {
			updatedRecipe.toggleLock();
		}
		int index = memorizedRecipes.indexOf(existingRecipe);
		memorizedRecipes.set(index, updatedRecipe);
	}

	@Nullable
	private MemorizedRecipe getOldestUnlockedRecipe() {
		MemorizedRecipe oldest = null;
		for (MemorizedRecipe existing : memorizedRecipes) {
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
		if (recipeIndex < 0 || recipeIndex >= memorizedRecipes.size()) {
			return null;
		}
		return memorizedRecipes.get(recipeIndex);
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
		lastUpdate = worldTime;
		if (memorizedRecipes.size() > recipeIndex) {
			memorizedRecipes.get(recipeIndex).toggleLock();
		}
	}

	@Nullable
	private MemorizedRecipe getExistingMemorizedRecipe(@Nullable CraftingRecipe recipe) {
		if (recipe != null) {
			for (MemorizedRecipe memorizedRecipe : memorizedRecipes) {
				if (memorizedRecipe.hasRecipe(recipe)) {
					return memorizedRecipe;
				}
			}
		}

		return null;
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		ListTag listNBT = new ListTag();
		for (MemorizedRecipe recipe : memorizedRecipes) {
			if (recipe != null && recipe.hasSelectedRecipe()) {
				CompoundTag recipeNbt = new CompoundTag();
				recipe.write(recipeNbt);
				listNBT.add(recipeNbt);
			}
		}
		compoundNBT.put("RecipeMemory", listNBT);
		return compoundNBT;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		NetworkUtil.writeStreamables(data, memorizedRecipes);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		NetworkUtil.readStreamables(data, memorizedRecipes, MemorizedRecipe::new);
	}

	public void copy(RecipeMemory memory) {
		this.memorizedRecipes.clear();
		this.memorizedRecipes.addAll(memory.memorizedRecipes);
	}
}

package forestry.worktable.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.RecipeUtils;
import forestry.worktable.inventory.WorktableCraftingContainer;

public class MemorizedRecipe implements INbtWritable, INbtReadable, IStreamable {
	private WorktableCraftingContainer craftMatrix = new WorktableCraftingContainer();
	private List<CraftingRecipe> recipes = new ArrayList<>();
	private final List<ResourceLocation> recipeIds = new ArrayList<>();
	private int selectedRecipe;
	private long lastUsed;
	private boolean locked;

	public MemorizedRecipe(FriendlyByteBuf buffer) {
		readData(buffer);
	}

	public MemorizedRecipe(CompoundTag nbt) {
		read(nbt);
	}

	public MemorizedRecipe(CraftingContainer craftMatrix, List<CraftingRecipe> recipes) {
		InventoryUtil.deepCopyInventoryContents(craftMatrix, this.craftMatrix);
		this.recipes = recipes;
		for (CraftingRecipe recipe : recipes) {
			recipeIds.add(recipe.getId());
		}
	}

	public WorktableCraftingContainer getCraftMatrix() {
		return craftMatrix;
	}

	public void setCraftMatrix(WorktableCraftingContainer usedMatrix) {
		this.craftMatrix = usedMatrix;
	}

	public void incrementRecipe() {
		selectedRecipe++;
		if (selectedRecipe >= recipes.size()) {
			selectedRecipe = 0;
		}
	}

	public void decrementRecipe() {
		selectedRecipe--;
		if (selectedRecipe < 0) {
			selectedRecipe = recipes.size() - 1;
		}
	}

	public boolean hasRecipeConflict() {
		return recipes.size() > 1;
	}

	public void removeRecipeConflicts(Level level) {
		CraftingRecipe recipe = getSelectedRecipe(level);
		recipes.clear();
		recipes.add(recipe);
		selectedRecipe = 0;
	}

	public ItemStack getOutputIcon(Level level) {
		CraftingRecipe selectedRecipe = getSelectedRecipe(level);
		if (selectedRecipe != null) {
			ItemStack recipeOutput = selectedRecipe.assemble(craftMatrix, level.registryAccess());
			if (!recipeOutput.isEmpty()) {
				return recipeOutput;
			}
		}
		return ItemStack.EMPTY;
	}

	public ItemStack getCraftingResult(CraftingContainer inventory, Level level) {
		CraftingRecipe selectedRecipe = getSelectedRecipe(level);
		if (selectedRecipe != null && selectedRecipe.matches(inventory, level)) {
			ItemStack recipeOutput = selectedRecipe.assemble(inventory, level.registryAccess());
			if (!recipeOutput.isEmpty()) {
				return recipeOutput;
			}
		}
		return ItemStack.EMPTY;
	}

	public boolean hasRecipes() {
		return (!recipes.isEmpty() || !recipeIds.isEmpty());
	}

	public boolean hasSelectedRecipe() {
		return hasRecipes() && selectedRecipe >= 0 && recipeIds.size() > selectedRecipe && recipeIds.get(selectedRecipe) != null;
	}

	public List<CraftingRecipe> getRecipes(@Nullable Level level) {
		if (recipes.isEmpty() && !recipeIds.isEmpty()) {
			for (ResourceLocation key : recipeIds) {
				Recipe<CraftingContainer> recipe = RecipeUtils.getRecipe(RecipeType.CRAFTING, key, level);
				if (recipe instanceof CraftingRecipe) {
					recipes.add((CraftingRecipe) recipe);
				}
			}
			if (selectedRecipe > recipes.size()) {
				selectedRecipe = 0;
			}
		}
		return recipes;
	}

	@Nullable
	public CraftingRecipe getSelectedRecipe(@Nullable Level level) {
		List<CraftingRecipe> recipes = getRecipes(level);
		if (recipes.isEmpty()) {
			return null;
		} else {
			return recipes.get(selectedRecipe);
		}
	}

	public boolean hasRecipe(@Nullable CraftingRecipe recipe, @Nullable Level level) {
		return getRecipes(level).contains(recipe);
	}

	public void updateLastUse(long lastUsed) {
		this.lastUsed = lastUsed;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void toggleLock() {
		locked = !locked;
	}

	public boolean isLocked() {
		return locked;
	}

	@Override
	public final void read(CompoundTag compoundNBT) {
		InventoryUtil.readFromNBT(craftMatrix, "inventory", compoundNBT);
		lastUsed = compoundNBT.getLong("LastUsed");
		locked = compoundNBT.getBoolean("Locked");

		if (compoundNBT.contains("SelectedRecipe")) {
			selectedRecipe = compoundNBT.getInt("SelectedRecipe");
		}

		recipes.clear();
		recipeIds.clear();
		ListTag recipesNbt = compoundNBT.getList("Recipes", Tag.TAG_STRING);
		for (int i = 0; i < recipesNbt.size(); i++) {
			String recipeKey = recipesNbt.getString(i);
			recipeIds.add(new ResourceLocation(recipeKey));
		}

		if (selectedRecipe > recipeIds.size()) {
			selectedRecipe = 0;
		}
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		InventoryUtil.writeToNBT(craftMatrix, "inventory", compoundNBT);
		compoundNBT.putLong("LastUsed", lastUsed);
		compoundNBT.putBoolean("Locked", locked);
		compoundNBT.putInt("SelectedRecipe", selectedRecipe);

		ListTag recipesNbt = new ListTag();
		for (ResourceLocation recipeName : recipeIds) {
			recipesNbt.add(StringTag.valueOf(recipeName.toString()));
		}
		compoundNBT.put("Recipes", recipesNbt);

		return compoundNBT;
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		NetworkUtil.writeInventory(data, craftMatrix);
		data.writeBoolean(locked);
		data.writeVarInt(selectedRecipe);

		data.writeVarInt(recipeIds.size());
		for (ResourceLocation recipeName : recipeIds) {
			data.writeResourceLocation(recipeName);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		NetworkUtil.readInventory(data, craftMatrix);
		locked = data.readBoolean();
		selectedRecipe = data.readVarInt();

		recipes.clear();
		recipeIds.clear();
		int recipeCount = data.readVarInt();
		for (int i = 0; i < recipeCount; i++) {
			ResourceLocation recipeId = data.readResourceLocation();
			recipeIds.add(recipeId);
		}
	}
}

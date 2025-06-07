package forestry.worktable.recipes;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IStreamable;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.RecipeUtil;
import forestry.worktable.inventory.WorktableCraftingContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MemorizedRecipe implements INbtWritable, INbtReadable, IStreamable {
	private WorktableCraftingContainer craftMatrix = new WorktableCraftingContainer();
	private List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
	private final List<ResourceLocation> recipeIds = new ArrayList<>();
	private int selectedRecipe;
	private long lastUsed;
	private boolean locked;

	public MemorizedRecipe(RegistryFriendlyByteBuf buffer) {
		readData(buffer);
	}

	public MemorizedRecipe(CompoundTag nbt) {
		read(nbt);
	}

	public MemorizedRecipe(CraftingContainer craftMatrix, List<RecipeHolder<CraftingRecipe>> recipes) {
		InventoryUtil.deepCopyInventoryContents(craftMatrix, this.craftMatrix);
		this.recipes = recipes;
		for (RecipeHolder<CraftingRecipe> recipe : recipes) {
			this.recipeIds.add(recipe.id());
		}
	}

	public WorktableCraftingContainer getCraftMatrix() {
		return this.craftMatrix;
	}

	public void setCraftMatrix(WorktableCraftingContainer usedMatrix) {
		this.craftMatrix = usedMatrix;
	}

	public void incrementRecipe() {
		this.selectedRecipe++;
		if (this.selectedRecipe >= this.recipes.size()) {
			this.selectedRecipe = 0;
		}
	}

	public void decrementRecipe() {
		this.selectedRecipe--;
		if (this.selectedRecipe < 0) {
			this.selectedRecipe = this.recipes.size() - 1;
		}
	}

	public boolean hasRecipeConflict() {
		return this.recipes.size() > 1;
	}

	public void removeRecipeConflicts() {
		RecipeHolder<CraftingRecipe> recipe = getSelectedRecipe();
		this.recipes.clear();
		this.recipes.add(recipe);
		this.selectedRecipe = 0;
	}

	public ItemStack getOutputIcon(Level level) {
		RecipeHolder<CraftingRecipe> selectedRecipe = getSelectedRecipe();
		if (selectedRecipe != null) {
			ItemStack recipeOutput = selectedRecipe.value().assemble(this.craftMatrix.asCraftInput(), level.registryAccess());
			if (!recipeOutput.isEmpty()) {
				return recipeOutput;
			}
		}
		return ItemStack.EMPTY;
	}

	public ItemStack getCraftingResult(CraftingInput input, Level level) {
		RecipeHolder<CraftingRecipe> selectedRecipe = getSelectedRecipe();

		if (selectedRecipe != null) {
			CraftingRecipe recipe = selectedRecipe.value();

			if (recipe.matches(input, level)) {
				ItemStack recipeOutput = recipe.assemble(input, level.registryAccess());
				if (!recipeOutput.isEmpty()) {
					return recipeOutput;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public boolean hasRecipes() {
		return (!this.recipes.isEmpty() || !this.recipeIds.isEmpty());
	}

	public boolean hasSelectedRecipe() {
		return hasRecipes() && this.selectedRecipe >= 0 && this.recipeIds.size() > this.selectedRecipe && this.recipeIds.get(this.selectedRecipe) != null;
	}

	public List<RecipeHolder<CraftingRecipe>> getRecipes() {
		if (this.recipes.isEmpty() && !this.recipeIds.isEmpty()) {
			for (ResourceLocation key : this.recipeIds) {
				RecipeHolder<CraftingRecipe> recipe = RecipeUtil.getRecipe(key);
				if (recipe != null) {
					this.recipes.add(recipe);
				}
			}
			if (this.selectedRecipe > this.recipes.size()) {
				this.selectedRecipe = 0;
			}
		}
		return this.recipes;
	}

	@Nullable
	public RecipeHolder<CraftingRecipe> getSelectedRecipe() {
		List<RecipeHolder<CraftingRecipe>> recipes = getRecipes();
		if (recipes.isEmpty()) {
			return null;
		} else {
			return recipes.get(this.selectedRecipe);
		}
	}

	public boolean hasRecipe(@Nullable RecipeHolder<CraftingRecipe> recipe) {
		return getRecipes().contains(recipe);
	}

	public void updateLastUse(long lastUsed) {
		this.lastUsed = lastUsed;
	}

	public long getLastUsed() {
		return this.lastUsed;
	}

	public void toggleLock() {
		this.locked = !this.locked;
	}

	public boolean isLocked() {
		return this.locked;
	}

	@Override
	public void read(CompoundTag compoundNBT) {
		InventoryUtil.readFromNBT(this.craftMatrix, "inventory", compoundNBT);
		this.lastUsed = compoundNBT.getLong("LastUsed");
		this.locked = compoundNBT.getBoolean("Locked");

		if (compoundNBT.contains("SelectedRecipe")) {
			this.selectedRecipe = compoundNBT.getInt("SelectedRecipe");
		}

		this.recipes.clear();
		this.recipeIds.clear();
		ListTag recipesNbt = compoundNBT.getList("Recipes", Tag.TAG_STRING);
		for (int i = 0; i < recipesNbt.size(); i++) {
			String recipeKey = recipesNbt.getString(i);
			ResourceLocation recipeId = ResourceLocation.tryParse(recipeKey);

			if (recipeId != null) {
				this.recipeIds.add(recipeId);
			}
		}

		if (this.selectedRecipe > this.recipeIds.size()) {
			this.selectedRecipe = 0;
		}
	}

	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		InventoryUtil.writeToNBT(this.craftMatrix, "inventory", compoundNBT);
		compoundNBT.putLong("LastUsed", this.lastUsed);
		compoundNBT.putBoolean("Locked", this.locked);
		compoundNBT.putInt("SelectedRecipe", this.selectedRecipe);

		ListTag recipesNbt = new ListTag();
		for (ResourceLocation recipeName : this.recipeIds) {
			recipesNbt.add(StringTag.valueOf(recipeName.toString()));
		}
		compoundNBT.put("Recipes", recipesNbt);

		return compoundNBT;
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		NetworkUtil.writeInventory(buffer, this.craftMatrix);
		buffer.writeBoolean(this.locked);
		buffer.writeVarInt(this.selectedRecipe);

		buffer.writeVarInt(this.recipeIds.size());
		for (ResourceLocation recipeName : this.recipeIds) {
			buffer.writeResourceLocation(recipeName);
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		NetworkUtil.readInventory(buffer, this.craftMatrix);
		this.locked = buffer.readBoolean();
		this.selectedRecipe = buffer.readVarInt();

		this.recipes.clear();
		this.recipeIds.clear();
		int recipeCount = buffer.readVarInt();
		for (int i = 0; i < recipeCount; i++) {
			ResourceLocation recipeId = buffer.readResourceLocation();
			this.recipeIds.add(recipeId);
		}
	}
}

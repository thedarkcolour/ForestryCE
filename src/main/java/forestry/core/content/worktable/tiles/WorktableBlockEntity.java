package forestry.core.content.worktable.tiles;

import forestry.core.platform.inventory.GhostCraftingInventory;
import forestry.core.platform.inventory.wrappers.InventoryMapper;
import forestry.core.platform.tile.BaseBlockEntity;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.ItemStackUtil;
import forestry.core.platform.util.RecipeUtils;
import forestry.core.content.worktable.features.WorktableBlockEntities;
import forestry.core.content.worktable.inventory.WorktableCraftingContainer;
import forestry.core.content.worktable.inventory.WorktableInventory;
import forestry.core.content.worktable.recipes.MemorizedRecipe;
import forestry.core.content.worktable.recipes.RecipeMemory;
import forestry.core.content.worktable.screens.WorktableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;

import javax.annotation.Nullable;
import java.util.List;

public class WorktableBlockEntity extends BaseBlockEntity implements ICrafterWorktable {
	private RecipeMemory memory;
	private final GhostCraftingInventory<WorktableBlockEntity> craftingDisplay;
	@Nullable
	private MemorizedRecipe currentRecipe;

	public WorktableBlockEntity(BlockPos pos, BlockState state) {
		super(WorktableBlockEntities.WORKTABLE.tileType(), pos, state);
		setInternalInventory(new WorktableInventory(this));

		this.craftingDisplay = new GhostCraftingInventory<>(this, 10);
		this.memory = new RecipeMemory();
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		this.craftingDisplay.write(nbt, registries);
		this.memory.write(nbt, registries);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.craftingDisplay.read(nbt, registries);
		this.memory = new RecipeMemory(nbt, registries);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		this.craftingDisplay.writeData(data);
		this.memory.writeData(data);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		this.craftingDisplay.readData(data);
		this.memory.readData(data);
	}

	public boolean hasRecipeConflict() {
		return this.currentRecipe != null && this.currentRecipe.hasRecipeConflict();
	}

	public void chooseNextConflictRecipe() {
		if (this.currentRecipe != null) {
			this.currentRecipe.incrementRecipe();
		}
	}

	public void choosePreviousConflictRecipe() {
		if (this.currentRecipe != null) {
			this.currentRecipe.decrementRecipe();
		}
	}

	@Override
	public ItemStack getResult(CraftingContainer inventory, Level level) {
		if (this.currentRecipe != null) {
			return this.currentRecipe.getCraftingResult(inventory.asCraftInput(), level);
		}
		return ItemStack.EMPTY;
	}

	/* ICrafterWorktable */
	@Override
	public boolean mayPickup(int craftingSlotIndex) {
		return craftingSlotIndex != GhostCraftingInventory.SLOT_CRAFTING_RESULT || canCraftCurrentRecipe();
	}

	private boolean canCraftCurrentRecipe() {
		return craftRecipe(true);
	}

	@Override
	public boolean onCraftingStart(Player player) {
		return craftRecipe(false);
	}

	private boolean craftRecipe(boolean simulate) {
		if (this.currentRecipe == null) {
			return false;
		}

		RecipeHolder<CraftingRecipe> selectedRecipe = this.currentRecipe.getSelectedRecipe();
		if (selectedRecipe == null) {
			return false;
		}

		NonNullList<ItemStack> inventoryStacks = InventoryUtil.getStacks(this);
		WorktableCraftingContainer usedMatrix = RecipeUtils.getUsedMatrix(this.currentRecipe.getCraftMatrix(), inventoryStacks, this.level, selectedRecipe.value());
		if (usedMatrix == null) {
			return false;
		}

		NonNullList<ItemStack> recipeItems = InventoryUtil.getStacks(usedMatrix);

		Container inventory;
		if (simulate) {
			inventory = new SimpleContainer(getContainerSize());
			InventoryUtil.deepCopyInventoryContents(this, inventory);
		} else {
			inventory = this;
		}

		if (!InventoryUtil.deleteExactSet(inventory, recipeItems)) {
			return false;
		}

		if (!simulate) {

			// update crafting display to match the ingredients that were actually used
			this.currentRecipe.setCraftMatrix(usedMatrix);
			setCurrentRecipe(this.currentRecipe);
		}

		return true;
	}

	@Override
	public void onCraftingComplete(Player player) {
		RecipeHolder<CraftingRecipe> selectedRecipe = this.currentRecipe.getSelectedRecipe();

		CommonHooks.setCraftingPlayer(player);
		WorktableCraftingContainer craftMatrix = this.currentRecipe.getCraftMatrix();
		NonNullList<ItemStack> remainingItems = selectedRecipe.value().getRemainingItems(craftMatrix.asCraftInput());
		CommonHooks.setCraftingPlayer(null);

		for (ItemStack remainingItem : remainingItems) {
			if (remainingItem != null && !remainingItem.isEmpty()) {
				if (!InventoryUtil.tryAddStack(this, remainingItem, true)) {
					player.drop(remainingItem, false);
				}
			}
		}

		if (!this.level.isClientSide) {
			this.memory.memorizeRecipe(this.level.getGameTime(), this.currentRecipe, this.level);
		}
	}

	/* Crafting Container methods */
	public RecipeMemory getMemory() {
		return this.memory;
	}

	public void chooseRecipeMemory(int recipeIndex) {
		MemorizedRecipe recipe = this.memory.getRecipe(recipeIndex);
		setCurrentRecipe(recipe);
	}

	private void setCraftingDisplay(Container craftMatrix) {
		for (int slot = 0; slot < craftMatrix.getContainerSize(); slot++) {
			ItemStack stack = craftMatrix.getItem(slot);
			this.craftingDisplay.setItem(slot, stack.copy());
		}
	}

	public Container getCraftingDisplay() {
		return new InventoryMapper(this.craftingDisplay, GhostCraftingInventory.SLOT_CRAFTING_1, GhostCraftingInventory.SLOT_CRAFTING_COUNT);
	}

	public void clearCraftMatrix() {
		for (int slot = 0; slot < this.craftingDisplay.getContainerSize(); slot++) {
			this.craftingDisplay.setItem(slot, ItemStack.EMPTY);
		}
	}

	public void setCurrentRecipe(CraftingContainer crafting) {
		List<RecipeHolder<CraftingRecipe>> recipes = RecipeUtils.getRecipes(RecipeType.CRAFTING, crafting.asCraftInput(), this.level);
		MemorizedRecipe recipe = recipes.isEmpty() ? null : new MemorizedRecipe(crafting, recipes);

		if (this.currentRecipe != null && recipe != null) {
			if (recipe.hasRecipe(this.currentRecipe.getSelectedRecipe())) {
				NonNullList<ItemStack> stacks = InventoryUtil.getStacks(crafting);
				NonNullList<ItemStack> currentStacks = InventoryUtil.getStacks(this.currentRecipe.getCraftMatrix());

				if (ItemStackUtil.equalSets(stacks, currentStacks)) {
					return;
				}
			}
		}

		setCurrentRecipe(recipe);
	}

	/* Network Sync with PacketWorktableRecipeUpdate */
	@Nullable
	public MemorizedRecipe getCurrentRecipe() {
		return this.currentRecipe;
	}

	public void setCurrentRecipe(@Nullable MemorizedRecipe recipe) {
		this.currentRecipe = recipe;
		if (this.currentRecipe != null) {
			setCraftingDisplay(this.currentRecipe.getCraftMatrix());
		}
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new WorktableMenu(windowId, inv, this);
	}
}

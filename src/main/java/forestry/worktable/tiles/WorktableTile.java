package forestry.worktable.tiles;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.ForgeHooks;

import forestry.core.inventory.InventoryGhostCrafting;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.tiles.TileBase;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.RecipeUtils;
import forestry.worktable.features.WorktableTiles;
import forestry.worktable.inventory.WorktableCraftingContainer;
import forestry.worktable.inventory.WorktableInventory;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.recipes.RecipeMemory;
import forestry.worktable.screens.WorktableMenu;

public class WorktableTile extends TileBase implements ICrafterWorktable {
	private RecipeMemory memory;
	private final InventoryGhostCrafting<WorktableTile> craftingDisplay;
	@Nullable
	private MemorizedRecipe currentRecipe;

	public WorktableTile(BlockPos pos, BlockState state) {
		super(WorktableTiles.WORKTABLE.tileType(), pos, state);
		setInternalInventory(new WorktableInventory(this));

		this.craftingDisplay = new InventoryGhostCrafting<>(this, 10);
		this.memory = new RecipeMemory();
	}

	@Override
	public void saveAdditional(CompoundTag data) {
		super.saveAdditional(data);

		craftingDisplay.write(data);
		memory.write(data);
	}

	@Override
	public void load(CompoundTag data) {
		super.load(data);

		craftingDisplay.read(data);
		memory = new RecipeMemory(data);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		craftingDisplay.writeData(data);
		memory.writeData(data);
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		craftingDisplay.readData(data);
		memory.readData(data);
	}

	public boolean hasRecipeConflict() {
		return currentRecipe != null && currentRecipe.hasRecipeConflict();
	}

	public void chooseNextConflictRecipe() {
		if (currentRecipe != null) {
			currentRecipe.incrementRecipe();
		}
	}

	public void choosePreviousConflictRecipe() {
		if (currentRecipe != null) {
			currentRecipe.decrementRecipe();
		}
	}

	@Override
	public ItemStack getResult(CraftingContainer inventory, Level level) {
		if (currentRecipe != null) {
			return currentRecipe.getCraftingResult(inventory, level);
		}
		return ItemStack.EMPTY;
	}

	/* ICrafterWorktable */
	@Override
	public boolean mayPickup(int craftingSlotIndex) {
		return craftingSlotIndex != InventoryGhostCrafting.SLOT_CRAFTING_RESULT || canCraftCurrentRecipe();
	}

	private boolean canCraftCurrentRecipe() {
		return craftRecipe(true);
	}

	@Override
	public boolean onCraftingStart(Player player) {
		return craftRecipe(false);
	}

	private boolean craftRecipe(boolean simulate) {
		if (currentRecipe == null) {
			return false;
		}

		CraftingRecipe selectedRecipe = currentRecipe.getSelectedRecipe();
		if (selectedRecipe == null) {
			return false;
		}

		NonNullList<ItemStack> inventoryStacks = InventoryUtil.getStacks(this);
		WorktableCraftingContainer usedMatrix = RecipeUtils.getUsedMatrix(currentRecipe.getCraftMatrix(), inventoryStacks, level, selectedRecipe);
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
			currentRecipe.setCraftMatrix(usedMatrix);
			setCurrentRecipe(currentRecipe);
		}

		return true;
	}

	@Override
	public void onCraftingComplete(Player player) {
		CraftingRecipe selectedRecipe = currentRecipe.getSelectedRecipe();

		ForgeHooks.setCraftingPlayer(player);
		WorktableCraftingContainer craftMatrix = currentRecipe.getCraftMatrix();
		NonNullList<ItemStack> remainingItems = selectedRecipe.getRemainingItems(craftMatrix.copy());
		ForgeHooks.setCraftingPlayer(null);

		for (ItemStack remainingItem : remainingItems) {
			if (remainingItem != null && !remainingItem.isEmpty()) {
				if (!InventoryUtil.tryAddStack(this, remainingItem, true)) {
					player.drop(remainingItem, false);
				}
			}
		}

		if (!level.isClientSide) {
			memory.memorizeRecipe(level.getGameTime(), currentRecipe, level);
		}
	}

	/* Crafting Container methods */
	public RecipeMemory getMemory() {
		return memory;
	}

	public void chooseRecipeMemory(int recipeIndex) {
		MemorizedRecipe recipe = memory.getRecipe(recipeIndex);
		setCurrentRecipe(recipe);
	}

	private void setCraftingDisplay(Container craftMatrix) {
		for (int slot = 0; slot < craftMatrix.getContainerSize(); slot++) {
			ItemStack stack = craftMatrix.getItem(slot);
			craftingDisplay.setItem(slot, stack.copy());
		}
	}

	public Container getCraftingDisplay() {
		return new InventoryMapper(craftingDisplay, InventoryGhostCrafting.SLOT_CRAFTING_1, InventoryGhostCrafting.SLOT_CRAFTING_COUNT);
	}

	public void clearCraftMatrix() {
		for (int slot = 0; slot < craftingDisplay.getContainerSize(); slot++) {
			craftingDisplay.setItem(slot, ItemStack.EMPTY);
		}
	}

	public void setCurrentRecipe(CraftingContainer crafting) {
		List<CraftingRecipe> recipes = RecipeUtils.getRecipes(RecipeType.CRAFTING, crafting, level);
		MemorizedRecipe recipe = recipes.isEmpty() ? null : new MemorizedRecipe(crafting, recipes);

		if (currentRecipe != null && recipe != null) {
			if (recipe.hasRecipe(currentRecipe.getSelectedRecipe())) {
				NonNullList<ItemStack> stacks = InventoryUtil.getStacks(crafting);
				NonNullList<ItemStack> currentStacks = InventoryUtil.getStacks(currentRecipe.getCraftMatrix());

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
		return currentRecipe;
	}

	public void setCurrentRecipe(@Nullable MemorizedRecipe recipe) {
		this.currentRecipe = recipe;
		if (currentRecipe != null) {
			setCraftingDisplay(currentRecipe.getCraftMatrix());
		}
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new WorktableMenu(windowId, inv, this);
	}
}

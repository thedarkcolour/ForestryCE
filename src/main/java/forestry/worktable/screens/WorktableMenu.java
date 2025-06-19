package forestry.worktable.screens;

import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.TileMenu;
import forestry.core.gui.IContainerCrafting;
import forestry.core.gui.IGuiSelectable;
import forestry.core.gui.slots.SlotCraftMatrix;
import forestry.core.gui.slots.WorktableSlot;
import forestry.core.inventory.InventoryGhostCrafting;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.NetworkUtil;
import forestry.worktable.features.WorktableMenus;
import forestry.worktable.inventory.WorktableCraftingContainer;
import forestry.worktable.inventory.WorktableInventory;
import forestry.worktable.network.packets.PacketWorktableMemoryUpdate;
import forestry.worktable.network.packets.PacketWorktableRecipeRequest;
import forestry.worktable.network.packets.PacketWorktableRecipeUpdate;
import forestry.worktable.recipes.MemorizedRecipe;
import forestry.worktable.recipes.RecipeMemory;
import forestry.worktable.tiles.WorktableTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorktableMenu extends TileMenu<WorktableTile> implements IContainerCrafting, IGuiSelectable {
	private final WorktableCraftingContainer craftMatrix = new WorktableCraftingContainer(this);
	private long lastMemoryUpdate;
	private boolean craftMatrixChanged = false;

	public static WorktableMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
		WorktableTile worktable = TileUtil.getTile(playerInv.player.level(), extraData.readBlockPos(), WorktableTile.class);
		return new WorktableMenu(windowId, playerInv, worktable);
	}

	public WorktableMenu(int windowId, Inventory inv, WorktableTile tile) {
		super(windowId, WorktableMenus.WORKTABLE.menuType(), inv, tile, 8, 136);

		Container craftingDisplay = tile.getCraftingDisplay();
		Container internalInventory = tile.getInternalInventory();

		// Internal inventory
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 9; k++) {
				addSlot(new Slot(internalInventory, WorktableInventory.SLOT_INVENTORY_1 + k + i * 9, 8 + k * 18, 90 + i * 18));
			}
		}

		// Crafting matrix
		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 3; k1++) {
				addSlot(new SlotCraftMatrix(this, craftingDisplay, k1 + l * 3, 11 + k1 * 18, 20 + l * 18));
			}
		}

		// CraftResult display
		addSlot(new WorktableSlot(inv.player, this.craftMatrix, craftingDisplay, tile, InventoryGhostCrafting.SLOT_CRAFTING_RESULT, 77, 38));

		for (int i = 0; i < this.craftMatrix.getContainerSize(); i++) {
			onCraftMatrixChanged(tile.getCraftingDisplay(), i);
		}
	}

	@Override
	public void broadcastChanges() {
		if (this.craftMatrixChanged) {
			this.craftMatrixChanged = false;
			this.tile.setCurrentRecipe(this.craftMatrix);
			sendPacketToListeners(new PacketWorktableRecipeUpdate(this.tile));
		}

		super.broadcastChanges();

		if (this.lastMemoryUpdate != this.tile.getMemory().getLastUpdate()) {
			this.lastMemoryUpdate = this.tile.getMemory().getLastUpdate();
			sendPacketToListeners(new PacketWorktableMemoryUpdate(this.tile));
		}
	}

	public void updateCraftMatrix() {
		Container crafting = this.tile.getCraftingDisplay();
		for (int i = 0; i < crafting.getContainerSize(); i++) {
			onCraftMatrixChanged(crafting, i);
		}
	}

	@Override
	public void onCraftMatrixChanged(Container iinventory, int slot) {
		if (slot >= this.craftMatrix.getContainerSize()) {
			return;
		}

		ItemStack stack = iinventory.getItem(slot);
		ItemStack currentStack = this.craftMatrix.getItem(slot);

		if (!ItemStackUtil.isIdenticalItem(stack, currentStack)) {
			this.craftMatrix.setItem(slot, stack.copy());
		}
	}

	// Fired when SlotCraftMatrix detects a change.
	// Direct changes to the underlying inventory are not detected, only slot changes.
	@Override
	public void slotsChanged(Container container) {
		this.craftMatrixChanged = true;
	}

	/* Gui Selection Handling */
	public static void clearRecipe() {
		NetworkUtil.sendRecipeClick(-1, 0);
	}

	@Override
	public void handleSelectionRequest(Player player, int primary, int secondary) {
		switch (primary) {
			case -1: { // clicked clear button
				this.tile.clearCraftMatrix();
				updateCraftMatrix();
				sendPacketToListeners(new PacketWorktableRecipeUpdate(this.tile));
				break;
			}
			case 0: { // clicked a memorized recipe
				this.tile.chooseRecipeMemory(secondary);
				updateCraftMatrix();
				sendPacketToListeners(new PacketWorktableRecipeUpdate(this.tile));
				break;
			}
			case 1: { // right clicked a memorized recipe
				long time = player.level().getGameTime();
				RecipeMemory memory = this.tile.getMemory();
				memory.toggleLock(time, secondary);
				break;
			}
			case 100: { // clicked previous recipe conflict button
				this.tile.choosePreviousConflictRecipe();
				sendPacketToListeners(new PacketWorktableRecipeUpdate(this.tile));
				break;
			}
			case 101: { // clicked next recipe conflict button
				this.tile.chooseNextConflictRecipe();
				sendPacketToListeners(new PacketWorktableRecipeUpdate(this.tile));
				break;
			}
		}
	}

	public void sendWorktableRecipeRequest(MemorizedRecipe recipe) {
        IForestryPacketServer packet = new PacketWorktableRecipeRequest(this.tile.getBlockPos(), recipe);
        PacketDistributor.sendToServer(packet);
    }
}

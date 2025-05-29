package forestry.core.gui.slots;

import forestry.worktable.tiles.ICrafterWorktable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WorktableSlot extends Slot {
	private final CraftingContainer craftMatrix;
	private final ICrafterWorktable crafter;
	private final Player player;
	private int amountCrafted;

	public WorktableSlot(Player player, CraftingContainer craftMatrix, Container craftingDisplay, ICrafterWorktable crafter, int slot, int xPos, int yPos) {
		super(craftingDisplay, slot, xPos, yPos);
		this.craftMatrix = craftMatrix;
		this.crafter = crafter;
		this.player = player;
	}

	// Identical to ResultSlot
	@Override
	public boolean mayPlace(ItemStack pStack) {
		return false;
	}

	// Identical to ResultSlot
	@Override
	protected void onQuickCraft(ItemStack pStack, int pAmount) {
        this.amountCrafted += pAmount;
		checkTakeAchievements(pStack);
	}

	// Identical to ResultSlot
	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		if (this.amountCrafted > 0) {
			stack.onCraftedBy(this.player.level(), this.player, this.amountCrafted);
			net.neoforged.neoforge.event.EventHooks.firePlayerCraftingEvent(this.player, stack, this.craftMatrix);
		}

		if (this.container instanceof RecipeCraftingHolder holder) {
			holder.awardUsedRecipes(this.player, this.craftMatrix.getItems());
		}

        this.amountCrafted = 0;
	}

	// DIFFERENT
	@Override
	public ItemStack remove(int amount) {
		if (!hasItem()) {
			return ItemStack.EMPTY;
		}

		return getItem();
	}

	@Override
	public boolean mayPickup(Player player) {
		return this.crafter.mayPickup(getSlotIndex());
	}

	@Override
	public ItemStack getItem() {
		return this.crafter.getResult(this.craftMatrix, this.player.level());
	}

	@Override
	public boolean hasItem() {
		return !getItem().isEmpty() && this.crafter.mayPickup(getSlotIndex());
	}

	// DIFFERENT
	@Override
	public void onTake(Player pPlayer, ItemStack stack) {
		if (this.crafter.onCraftingStart(this.player)) {
			checkTakeAchievements(stack);

            this.crafter.onCraftingComplete(this.player);
		}
	}
}

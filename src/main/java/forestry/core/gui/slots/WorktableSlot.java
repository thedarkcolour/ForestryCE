package forestry.core.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import forestry.worktable.tiles.ICrafterWorktable;

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
		amountCrafted += pAmount;
		checkTakeAchievements(pStack);
	}

	// Identical to ResultSlot
	@Override
	protected void checkTakeAchievements(ItemStack stack) {
		if (amountCrafted > 0) {
			stack.onCraftedBy(player.level(), player, amountCrafted);
			net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftMatrix);
		}

		if (container instanceof RecipeHolder holder) {
			holder.awardUsedRecipes(this.player, this.craftMatrix.getItems());
		}

		amountCrafted = 0;
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
		return crafter.mayPickup(getSlotIndex());
	}

	@Override
	public ItemStack getItem() {
		return crafter.getResult(craftMatrix, player.level());
	}

	@Override
	public boolean hasItem() {
		return !getItem().isEmpty() && crafter.mayPickup(getSlotIndex());
	}

	// DIFFERENT
	@Override
	public void onTake(Player pPlayer, ItemStack stack) {
		if (crafter.onCraftingStart(player)) {
			checkTakeAchievements(stack);

			crafter.onCraftingComplete(player);
		}
	}
}

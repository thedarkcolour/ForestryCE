package forestry.storage.items;

import forestry.api.event.BackpackEvent;
import forestry.api.storage.EnumBackpackType;
import forestry.api.storage.IBackpackDefinition;
import forestry.core.config.ForestryConfig;
import forestry.core.inventory.ItemHandlerInventoryManipulator;
import forestry.core.inventory.ItemInventory;
import forestry.core.items.ItemWithGui;
import forestry.core.items.definitions.IColoredItem;
import forestry.core.tiles.TileUtil;
import forestry.storage.BackpackMode;
import forestry.storage.gui.BackpackMenu;
import forestry.storage.inventory.ItemInventoryBackpack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBackpack extends ItemWithGui implements IColoredItem {
	public static final int SLOTS_BACKPACK_DEFAULT = 15;
	public static final int SLOTS_BACKPACK_WOVEN = 45;
	public static final int SLOTS_BACKPACK_APIARIST = 125;
	private final IBackpackDefinition definition;
	private final EnumBackpackType type;

	public ItemBackpack(IBackpackDefinition definition, EnumBackpackType type) {
		super(new Item.Properties().stacksTo(1));

		this.definition = definition;
		this.type = type;
	}

	public IBackpackDefinition getDefinition() {
		return this.definition;
	}

	@Override
	protected void writeContainerData(RegistryFriendlyByteBuf buffer, Player player, ItemStack stack, int slotIndex) {
		// isWoven
		buffer.writeByte(slotIndex);
		buffer.writeBoolean(this.type == EnumBackpackType.WOVEN);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand hand) {
		if (!playerIn.isShiftKeyDown()) {
			return super.use(level, playerIn, hand);
		} else {
			ItemStack heldItem = playerIn.getItemInHand(hand);
			switchMode(heldItem);
			return InteractionResultHolder.success(heldItem);
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (getInventoryHit(level, ctx.getClickedPos(), ctx.getClickedFace()) != null) {
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Player player = context.getPlayer();
		// We only do this when shift is clicked
		if (player != null && player.isShiftKeyDown()) {
			ItemStack heldItem = player.getItemInHand(context.getHand());
			return evaluateTileHit(heldItem, context.getLevel(), context.getClickedPos(), context.getClickedFace()) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		return super.onItemUseFirst(stack, context);
	}

	public static void tryStowing(Player player, ItemStack backpackStack, ItemStack stack) {
		if (getMode(backpackStack) == BackpackMode.LOCKED) {
			return;
		}

		ItemBackpack backpack = (ItemBackpack) backpackStack.getItem();
		ItemInventory inventory = new ItemInventoryBackpack(backpack.getBackpackSize(), backpackStack);

		if (NeoForge.EVENT_BUS.post(new BackpackEvent.Stow(player, backpack.getDefinition(), inventory, stack)).isCanceled()) {
			return;
		}
		if (stack.isEmpty()) {
			return;
		}

		IItemHandler itemHandler = inventory.getItemHandler();
		ItemHandlerInventoryManipulator manipulator = new ItemHandlerInventoryManipulator(itemHandler);
		ItemStack remainder = manipulator.addStack(stack);

		stack.setCount(remainder == null ? 0 : remainder.getCount());
	}

	private static void switchMode(ItemStack itemstack) {
		BackpackMode mode = getMode(itemstack);
		int nextMode = mode.ordinal() + 1;
		if (!ForestryConfig.SERVER.enableBackpackResupply.get() && nextMode == BackpackMode.RESUPPLY.ordinal()) {
			nextMode++;
		}
		nextMode %= BackpackMode.VALUES.length;
		itemstack.setDamageValue(nextMode);
	}

	@Nullable
	private static IItemHandler getInventoryHit(Level world, BlockPos pos, Direction side) {
		BlockEntity targeted = TileUtil.getTile(world, pos);
		return TileUtil.getInventoryFromTile(targeted, side);
	}

	private boolean evaluateTileHit(ItemStack stack, Level world, BlockPos pos, Direction side) {
		// Shift right-clicking on an inventory tile will attempt to transfer
		// items contained in the backpack
		IItemHandler inventory = getInventoryHit(world, pos, side);
		// Process only inventories
		if (inventory != null) {

			// Must have inventory slots
			if (inventory.getSlots() <= 0) {
				return true;
			}

			if (!world.isClientSide) {
				// Create our own backpack inventory
				ItemInventoryBackpack backpackInventory = new ItemInventoryBackpack(getBackpackSize(), stack);

				BackpackMode mode = getMode(stack);
				if (mode == BackpackMode.RECEIVE) {
					receiveFromChest(backpackInventory, inventory);
				} else {
					transferToChest(backpackInventory, inventory);
				}
			}

			return true;
		}

		return false;
	}

	private static void transferToChest(ItemInventoryBackpack backpackInventory, IItemHandler target) {
		ItemHandlerInventoryManipulator manipulator = new ItemHandlerInventoryManipulator(backpackInventory.getItemHandler());
		manipulator.transferStacks(target, stack -> true);
	}

	private void receiveFromChest(ItemInventoryBackpack backpackInventory, IItemHandler target) {
		ItemHandlerInventoryManipulator manipulator = new ItemHandlerInventoryManipulator(target);
		manipulator.transferStacks(backpackInventory.getItemHandler(), this.definition.getFilter());
	}

	public int getBackpackSize() {
		return getSlotsForType(this.type);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, ctx, list, flag);

		int occupied = ItemInventory.getOccupiedSlotCount(stack);

		BackpackMode mode = getMode(stack);
		String infoKey = mode.getTranslationKey();
		if (infoKey != null) {
			list.add(Component.translatable(infoKey).withStyle(ChatFormatting.GRAY));
		}
		list.add(Component.translatable("for.gui.slots", String.valueOf(occupied), String.valueOf(getBackpackSize())).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public int getColorFromItemStack(ItemStack itemstack, int layer) {
		if (layer == 0) {
			return this.definition.getPrimaryColour();
		} else if (layer == 1) {
			return this.definition.getSecondaryColour();
		} else {
			return 0xffffff;
		}
	}

	private static int getSlotsForType(EnumBackpackType type) {
		return switch (type) {
			case NATURALIST -> SLOTS_BACKPACK_APIARIST;
			case WOVEN -> SLOTS_BACKPACK_WOVEN;
			case NORMAL -> SLOTS_BACKPACK_DEFAULT;
		};
	}

	public static BackpackMode getMode(ItemStack backpack) {
		if (!(backpack.getItem() instanceof ItemBackpack)) {
			return BackpackMode.NEUTRAL;
		}

		int meta = Mth.clamp(backpack.getDamageValue(), 0, 3);

		return BackpackMode.VALUES[meta];
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		Item oldItem = oldStack.getItem();
		Item newItem = newStack.getItem();
		return oldItem != newItem || getMode(oldStack) != getMode(newStack);
	}

	@Override
	@Nullable
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, int slotIndex) {
		return switch (this.type) {
			case NORMAL -> new BackpackMenu(windowId, playerInv, BackpackMenu.Size.DEFAULT, slotIndex);
			case WOVEN -> new BackpackMenu(windowId, playerInv, BackpackMenu.Size.T2, slotIndex);
			default -> null;
		};
	}
}

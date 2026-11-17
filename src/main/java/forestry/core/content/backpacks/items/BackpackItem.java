package forestry.core.content.backpacks.items;

import com.google.common.base.Preconditions;
import forestry.api.core.backpacks.BackpackStowEvent;
import forestry.api.core.backpacks.EnumBackpackType;
import forestry.api.core.backpacks.IBackpackDefinition;
import forestry.core.platform.config.ForestryConfig;
import forestry.core.platform.inventory.ItemHandlerInventoryManipulator;
import forestry.core.platform.inventory.ItemInventory;
import forestry.core.platform.inventory.StandardStackFilters;
import forestry.core.platform.item.WithScreenItem;
import forestry.core.platform.item.ITintedItem;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.NetworkUtil;
import forestry.core.content.backpacks.BackpackMode;
import forestry.core.content.backpacks.gui.BackpackMenu;
import forestry.core.content.backpacks.inventory.BackpackInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class BackpackItem extends WithScreenItem implements ITintedItem {
	public static final int SLOTS_BACKPACK_DEFAULT = 15;
	public static final int SLOTS_BACKPACK_WOVEN = 45;
	public static final int SLOTS_BACKPACK_APIARIST = 128;
	private final IBackpackDefinition definition;
	private final EnumBackpackType type;

	public BackpackItem(IBackpackDefinition definition, EnumBackpackType type, Item.Properties properties) {
		super(properties);

		this.definition = definition;
		this.type = type;
	}

	public IBackpackDefinition getDefinition() {
		return this.definition;
	}

	@Override
	protected void writeContainerData(ServerPlayer player, ItemStack stack, RegistryFriendlyByteBuf buffer) {
		NetworkUtil.writeEnum(buffer, this.type == EnumBackpackType.WOVEN ? BackpackMenu.Size.T2 : BackpackMenu.Size.DEFAULT);
		ItemStack.STREAM_CODEC.encode(buffer, stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!playerIn.isShiftKeyDown()) {
			return super.use(worldIn, playerIn, handIn);
		} else {
			ItemStack heldItem = playerIn.getItemInHand(handIn);
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
			return evaluateTileHit(heldItem, player, context.getLevel(), context.getClickedPos(), context.getClickedFace()) ? InteractionResult.PASS : InteractionResult.FAIL;
		}
		return super.onItemUseFirst(stack, context);
	}

	public static void tryStowing(Player player, ItemStack backpackStack, ItemStack stack) {
		if (getMode(backpackStack) == BackpackMode.LOCKED) {
			return;
		}

		BackpackItem backpack = (BackpackItem) backpackStack.getItem();
		ItemInventory inventory = new BackpackInventory(player, backpack.getBackpackSize(), backpackStack);

		if (NeoForge.EVENT_BUS.post(new BackpackStowEvent(player, backpack.getDefinition(), inventory, stack)).isCanceled()) {
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

	private boolean evaluateTileHit(ItemStack stack, Player player, Level world, BlockPos pos, Direction side) {

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
				BackpackInventory backpackInventory = new BackpackInventory(player, getBackpackSize(), stack);

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

	private static void transferToChest(BackpackInventory backpackInventory, IItemHandler target) {
		ItemHandlerInventoryManipulator manipulator = new ItemHandlerInventoryManipulator(backpackInventory.getItemHandler());
		manipulator.transferStacks(target, StandardStackFilters.ALL);
	}

	private void receiveFromChest(BackpackInventory backpackInventory, IItemHandler target) {
		ItemHandlerInventoryManipulator manipulator = new ItemHandlerInventoryManipulator(target);
		manipulator.transferStacks(backpackInventory.getItemHandler(), this.definition.getFilter());
	}

	public int getBackpackSize() {
		return getSlotsForType(this.type);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);

		int occupied = ItemInventory.getOccupiedSlotCount(itemstack);

		BackpackMode mode = getMode(itemstack);
		String infoKey = mode.getTranslationKey();
		if (infoKey != null) {
			list.add(Component.translatable(infoKey).withStyle(ChatFormatting.GRAY));
		}
		list.add(Component.translatable("for.gui.slots", String.valueOf(occupied), String.valueOf(getBackpackSize())).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public Component getName(ItemStack itemstack) {
		return this.definition.getName(itemstack);
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
		if (!(backpack.getItem() instanceof BackpackItem)) {
			return BackpackMode.NEUTRAL;
		}

		int meta = Mth.clamp(backpack.getDamageValue(), 0, 3);

		return BackpackMode.VALUES[meta];
	}

	public static EnumBackpackType getType(ItemStack backpack) {
		Preconditions.checkArgument(backpack.getItem() instanceof BackpackItem, "Item must be a backpack");
		return ((BackpackItem) backpack.getItem()).type;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		Item oldItem = oldStack.getItem();
		Item newItem = newStack.getItem();
		return oldItem != newItem || getMode(oldStack) != getMode(newStack);
	}

	@Override
	@Nullable
	public AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem) {
		Item item = heldItem.getItem();
		if (!(item instanceof BackpackItem backpack)) {
			return null;
		}
		return switch (backpack.type) {
			case NORMAL -> new BackpackMenu(containerId, player, BackpackMenu.Size.DEFAULT, heldItem);
			case WOVEN -> new BackpackMenu(containerId, player, BackpackMenu.Size.T2, heldItem);
			default -> null;
		};
	}
}

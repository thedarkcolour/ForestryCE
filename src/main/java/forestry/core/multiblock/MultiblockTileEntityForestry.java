package forestry.core.multiblock;

import forestry.api.core.ILocationProvider;
import forestry.api.core.ISpectacleBlock;
import forestry.api.multiblock.IMultiblockLogic;
import forestry.api.multiblock.MultiblockTileEntityBase;
import forestry.core.config.Constants;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.IFilterSlotDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class MultiblockTileEntityForestry<T extends IMultiblockLogic> extends MultiblockTileEntityBase<T> implements WorldlyContainer, IFilterSlotDelegate, ILocationProvider, MenuProvider, ISpectacleBlock {
	@Nullable
	private ResolvableProfile owner;

	public MultiblockTileEntityForestry(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, T multiblockLogic) {
		super(tileEntityType, pos, state, multiblockLogic);
	}

	/**
	 * Called by a structure block when it is right clicked by a player.
	 */
	public void openGui(ServerPlayer player, BlockPos pos) {
		player.openMenu(this, pos);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		if (nbt.contains("owner")) {
			ResolvableProfile.CODEC
				.parse(NbtOps.INSTANCE, nbt.get("profile"))
				.result()
				.ifPresent(this::setOwner);
		}

		getInternalInventory().read(nbt, registries);
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		if (this.owner != null) {
			nbt.put("owner", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
		}

		getInternalInventory().write(nbt, registries);
	}

	/* INVENTORY */
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	public boolean allowsAutomation() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getInternalInventory().isEmpty();
	}

	@Override
	public final int getContainerSize() {
		return getInternalInventory().getContainerSize();
	}

	@Override
	public final ItemStack getItem(int slotIndex) {
		return getInternalInventory().getItem(slotIndex);
	}

	@Override
	public final ItemStack removeItem(int slotIndex, int amount) {
		return getInternalInventory().removeItem(slotIndex, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slotIndex) {
		return getInternalInventory().removeItemNoUpdate(slotIndex);
	}

	@Override
	public final void setItem(int slotIndex, ItemStack itemstack) {
		getInternalInventory().setItem(slotIndex, itemstack);
	}

	@Override
	public final int getMaxStackSize() {
		return getInternalInventory().getMaxStackSize();
	}

	@Override
	public final void startOpen(Player player) {
		getInternalInventory().startOpen(player);
	}

	@Override
	public final void stopOpen(Player player) {
		getInternalInventory().stopOpen(player);
	}

	@Override
	public final boolean stillValid(Player player) {
		return getInternalInventory().stillValid(player);
	}

	@Override
	public final boolean canPlaceItem(int slotIndex, ItemStack itemStack) {
		return getInternalInventory().canPlaceItem(slotIndex, itemStack);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		if (allowsAutomation()) {
			return getInternalInventory().getSlotsForFace(side);
		} else {
			return Constants.SLOTS_NONE;
		}
	}

	@Override
	public final boolean canPlaceItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return allowsAutomation() && getInternalInventory().canPlaceItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canTakeItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return allowsAutomation() && getInternalInventory().canTakeItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return getInternalInventory().canSlotAccept(slotIndex, stack);
	}

	@Override
	public final boolean isLocked(int slotIndex) {
		return getInternalInventory().isLocked(slotIndex);
	}

	@Override
	@Nullable
	public final @Nullable ResolvableProfile getOwner() {
		return this.owner;
	}

	public final void setOwner(@Nullable ResolvableProfile owner) {
		this.owner = owner;
	}

	@Override
	public void clearContent() {
		getInternalInventory().clearContent();
	}

	@Override
	public boolean isHighlighted(Player player) {
		return player.isCreative() && getMultiblockLogic().getController() instanceof IMultiblockControllerInternal internal && this.worldPosition.equals(internal.getReferenceCoord());
	}
}

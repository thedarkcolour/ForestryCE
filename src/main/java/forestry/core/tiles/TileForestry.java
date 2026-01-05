package forestry.core.tiles;

import com.google.common.base.Preconditions;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorLogicSource;
import forestry.api.core.ILocationProvider;
import forestry.api.util.TickHelper;
import forestry.core.blocks.TileStreamUpdateTracker;
import forestry.core.errors.ErrorLogic;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.network.IStreamable;
import forestry.core.utils.NBTUtilForestry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

public abstract class TileForestry extends BlockEntity implements IStreamable, IErrorLogicSource, WorldlyContainer, IFilterSlotDelegate, ITitled, ILocationProvider, MenuProvider {
	private final ErrorLogic errorHandler = new ErrorLogic();
	private final AdjacentTileCache tileCache = new AdjacentTileCache(this);

	private IInventoryAdapter inventory = FakeInventoryAdapter.INSTANCE;

	// package private for ForestryTicker
	final TickHelper tickHelper;

	public TileForestry(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);

		this.tickHelper = new TickHelper(pos.hashCode());
	}

	protected AdjacentTileCache getTileCache() {
		return this.tileCache;
	}

	public void onNeighborTileChange(Level world, BlockPos pos, BlockPos neighbor) {
        this.tileCache.onNeighborChange();
	}

	@Override
	public void setRemoved() {
        this.tileCache.purge();
		super.setRemoved();
	}

	@Override
	public void clearRemoved() {
        this.tileCache.purge();
		super.clearRemoved();
	}

	// these are not called automatically, they must be specified in the MachineProperties
	protected void clientTick(Level level, BlockPos pos, BlockState state) {
	}

	protected void serverTick(Level level, BlockPos pos, BlockState state) {
	}

	protected final boolean updateOnInterval(int tickInterval) {
		return this.tickHelper.updateOnInterval(tickInterval);
	}

	// / SAVING & LOADING
	@Override
	public void load(CompoundTag data) {
		super.load(data);
        this.inventory.read(data);
	}

	@Override
	public void saveAdditional(CompoundTag data) {
		super.saveAdditional(data);
        this.inventory.write(data);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		return NBTUtilForestry.writeStreamableToNbt(this, tag);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		NBTUtilForestry.readStreamableFromNbt(this, tag);
	}

	/* INetworkedEntity */
	protected final void sendNetworkUpdate() {
		TileStreamUpdateTracker.sendVisualUpdate(this);
	}

	/* IStreamable */
	@Override
	public void writeData(FriendlyByteBuf data) {
	}

	@Override
	public void readData(FriendlyByteBuf data) {
	}

	// serverside only, called when the block is destroyed and its inventory is spilled into the world
	public void onDropContents(ServerLevel level) {
	}

	@Nullable
	@Override
	public Level getWorldObj() {
		return this.level;
	}

	// / REDSTONE INFO
	protected boolean isRedstoneActivated() {
		return this.level.getBestNeighborSignal(getBlockPos()) > 0;
	}

	@Override
	public final IErrorLogic getErrorLogic() {
		return this.errorHandler;
	}

	/* NAME */

	/**
	 * Gets the tile's unlocalized name, based on the block at the location of this entity (client-only).
	 */
	@Override
	public Component getTitle() {
		return Component.translatable(getBlockState().getBlock().getDescriptionId());
	}

	/* INVENTORY BASICS */
	public IInventoryAdapter getInternalInventory() {
		return this.inventory;
	}

	protected final void setInternalInventory(IInventoryAdapter inv) {
		Preconditions.checkNotNull(inv);
		this.inventory = inv;
	}

	/* ISidedInventory */

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
	public ItemStack removeItem(int slotIndex, int amount) {
		return getInternalInventory().removeItem(slotIndex, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slotIndex) {
		return getInternalInventory().removeItemNoUpdate(slotIndex);
	}

	@Override
	public void setItem(int slotIndex, ItemStack itemstack) {
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
	public final boolean canSlotAccept(int slotIndex, ItemStack stack) {
		return getInternalInventory().canSlotAccept(slotIndex, stack);
	}

	@Override
	public boolean isLocked(int slotIndex) {
		return getInternalInventory().isLocked(slotIndex);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return getInternalInventory().getSlotsForFace(side);
	}

	@Override
	public final boolean canPlaceItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return getInternalInventory().canPlaceItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final boolean canTakeItemThroughFace(int slotIndex, ItemStack itemStack, Direction side) {
		return getInternalInventory().canTakeItemThroughFace(slotIndex, itemStack, side);
	}

	@Override
	public final BlockPos getCoordinates() {
		return getBlockPos();
	}

	@Override
	public void clearContent() {
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER) {
			if (facing != null) {
				return LazyOptional.of(() -> new SidedInvWrapper(getInternalInventory(), facing)).cast();
			} else {
				return LazyOptional.of(() -> new InvWrapper(getInternalInventory())).cast();
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Component getDisplayName() {
		return getTitle();
	}
}

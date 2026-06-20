package forestry.api.multiblock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Base logic class for multiblock-connected tile entities.
 *
 * <p>After the engine rewrite (plan Phase 2; spec §6, §7) this base no longer delegates to a heavyweight
 * "multiblock logic" object. Instead each member stores its {@code anchorPos} (the holder of the shared
 * payload, spec §6.1) and resolves its controller through the {@code MultiblockIndex}. The shared-payload
 * persistence, the anchor-only ticker, the legacy/payload stash, the break-vs-unload disambiguation, and
 * the network sync are implemented by the concrete {@code MultiblockTileEntityForestry} subclass — this
 * class only holds the common state and the thin {@link IMultiblockLogic} accessor.
 */
public abstract class MultiblockTileEntityBase<T extends IMultiblockLogic> extends BlockEntity implements IMultiblockComponent {
	private final T multiblockLogic;

	/**
	 * The holder/anchor that serializes this machine's shared payload and hosts its controller (spec §6.1).
	 * {@code null} when this block is not part of an assembled structure.
	 */
	@Nullable
	private BlockPos anchorPos;

	/**
	 * The shared payload as last seen on disk / packet, round-tripped in {@code saveAdditional}/{@code load}
	 * until the load-time validation adopts it into a live controller (spec §6.4 stash; §10 legacy).
	 */
	@Nullable
	private CompoundTag stash;

	/**
	 * Set in {@code onChunkUnloaded}, checked in {@code setRemoved}, to distinguish a temporary chunk unload
	 * (deactivate only) from a genuine break (re-anchor / drops) — spec §6.4.
	 */
	private boolean unloading;

	public MultiblockTileEntityBase(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, T multiblockLogic) {
		super(tileEntityType, pos, state);
		this.multiblockLogic = multiblockLogic;
	}

	@Override
	public BlockPos getCoordinates() {
		return getBlockPos();
	}

	@Override
	public T getMultiblockLogic() {
		return this.multiblockLogic;
	}

	/* ===== Anchor / stash / unloading state (spec §6.1, §6.4) ===== */

	@Nullable
	public BlockPos getAnchorPos() {
		return this.anchorPos;
	}

	public void setAnchorPos(@Nullable BlockPos anchorPos) {
		this.anchorPos = anchorPos == null ? null : anchorPos.immutable();
	}

	@Nullable
	protected CompoundTag getStash() {
		return this.stash;
	}

	protected void setStash(@Nullable CompoundTag stash) {
		this.stash = stash;
	}

	public void clearStash() {
		this.stash = null;
	}

	protected boolean isUnloading() {
		return this.unloading;
	}

	protected void setUnloading(boolean unloading) {
		this.unloading = unloading;
	}

	@Override
	public abstract void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord);

	@Override
	public abstract void onMachineBroken();

	/* ===== Network plumbing (re-implemented for the new engine; spec §9) ===== */

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag updateTag = super.getUpdateTag();
		this.encodeDescriptionPacket(updateTag);
		return updateTag;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public final void onDataPacket(Connection network, ClientboundBlockEntityDataPacket packet) {
		super.onDataPacket(network, packet);
		CompoundTag nbtData = packet.getTag();
		if (nbtData != null) {
			this.decodeDescriptionPacket(nbtData);
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		this.decodeDescriptionPacket(tag);
	}

	/**
	 * Writes tile/payload data to the description packet (overridden by the holder to add the controller
	 * payload, spec §9).
	 */
	protected void encodeDescriptionPacket(CompoundTag packetData) {

	}

	/** Reads tile/payload data from the description packet. */
	protected void decodeDescriptionPacket(CompoundTag packetData) {

	}
}

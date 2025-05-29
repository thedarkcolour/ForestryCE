package forestry.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base logic class for Multiblock-connected tile entities.
 * Most multiblock components should derive from this.
 */
public abstract class MultiblockTileEntityBase<T extends IMultiblockLogic> extends BlockEntity implements IMultiblockComponent {
	private final T multiblockLogic;

	public MultiblockTileEntityBase(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, T multiblockLogic) {
		super(tileEntityType, pos, state);
		this.multiblockLogic = multiblockLogic;
	}

	@Override
	public T getMultiblockLogic() {
		return this.multiblockLogic;
	}

	@Override
	public abstract void onMachineAssembled(IMultiblockController multiblockController, BlockPos minCoord, BlockPos maxCoord);

	@Override
	public abstract void onMachineBroken();

	@Override
	public void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.loadAdditional(data, registries);
		this.multiblockLogic.readFromNBT(data);
	}

	@Override
	public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
		super.saveAdditional(data, registries);
		this.multiblockLogic.write(data);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		this.multiblockLogic.invalidate(this.level, this);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		this.multiblockLogic.onChunkUnload(this.level, this);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.multiblockLogic.validate(this.level, this);
	}

	/* Network Communication */

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag updateTag = super.getUpdateTag(registries);
		this.multiblockLogic.encodeDescriptionPacket(updateTag);
		this.encodeDescriptionPacket(updateTag);
		return updateTag;
	}

	@Override
	public final void onDataPacket(Connection network, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
		super.onDataPacket(network, packet, registries);
		CompoundTag nbtData = packet.getTag();
		if (nbtData != null) {
			this.multiblockLogic.decodeDescriptionPacket(nbtData);
			this.decodeDescriptionPacket(nbtData);
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		this.multiblockLogic.decodeDescriptionPacket(tag);
		this.decodeDescriptionPacket(tag);
	}

	/**
	 * Used to write tileEntity-specific data to the descriptionPacket
	 */
	protected void encodeDescriptionPacket(CompoundTag packetData) {
	}

	/**
	 * Used to read tileEntity-specific data from the descriptionPacket (onDataPacket)
	 */
	protected void decodeDescriptionPacket(CompoundTag packetData) {
	}
}

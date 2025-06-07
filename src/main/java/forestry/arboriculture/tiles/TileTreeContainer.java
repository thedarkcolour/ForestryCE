package forestry.arboriculture.tiles;

import forestry.api.arboriculture.genetics.ITree;
import forestry.core.ClientsideCode;
import forestry.core.network.IStreamable;
import forestry.core.utils.NBTUtilForestry;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * This is the base TE class for any block that needs to contain tree genome information.
 */
public abstract class TileTreeContainer extends BlockEntity implements IStreamable {
	@Nullable
	private ITree containedTree;

	public TileTreeContainer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/* SAVING & LOADING */
	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		if (this.containedTree != null) {
			Tag serialized = SpeciesUtil.serializeIndividual(this.containedTree);
			if (serialized != null) {
				nbt.put("ContainedTree", serialized);
			}
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		Tag treeNbt = nbt.get("ContainedTree");

		if (treeNbt != null) {
			this.containedTree = SpeciesUtil.deserializeIndividual(SpeciesUtil.TREE_TYPE.get(), treeNbt);
		}
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf buffer) {
		ITree tree = getTree();
		if (tree != null) {
			buffer.writeBoolean(true);
			ResourceLocation speciesId = tree.getSpecies().id();
			buffer.writeResourceLocation(speciesId);
		} else {
			buffer.writeBoolean(false);
		}
	}

	@Override
	public void readData(RegistryFriendlyByteBuf buffer) {
		if (buffer.readBoolean()) {
			ResourceLocation speciesId = buffer.readResourceLocation();
			ITree tree = SpeciesUtil.getTreeSpecies(speciesId).createIndividual();
			setTree(tree);
		}
	}

	/* CONTAINED TREE */
	public void setTree(ITree tree) {
		this.containedTree = tree;

		if (this.level != null && this.level.isClientSide) {
			ClientsideCode.markForUpdate(this.worldPosition);
		}
	}

	@Nullable
	public ITree getTree() {
		return this.containedTree;
	}

	/**
	 * Leaves and saplings will implement their logic here.
	 */
	public abstract void onBlockTick(Level worldIn, BlockPos pos, BlockState state, RandomSource rand);

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		CompoundTag nbt = pkt.getTag();
		handleUpdateTag(nbt, registries);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		return NBTUtilForestry.writeStreamableToNbt(this, tag);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		NBTUtilForestry.readStreamableFromNbt(this, tag);
	}
}

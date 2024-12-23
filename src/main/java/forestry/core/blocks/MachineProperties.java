package forestry.core.blocks;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import forestry.core.tiles.IForestryTicker;
import forestry.core.tiles.TileForestry;
import forestry.modules.features.FeatureTileType;

public class MachineProperties<T extends TileForestry> implements IMachineProperties<T> {
	private static final ISimpleShapeProvider FULL_CUBE = Shapes::block;

	private final String name;
	private final FeatureTileType<? extends T> teType;
	private final IShapeProvider shape;
	@Nullable
	private final IForestryTicker<? extends T> clientTicker;
	@Nullable
	private final IForestryTicker<? extends T> serverTicker;
	@Nullable
	private Block block;

	public MachineProperties(FeatureTileType<? extends T> teType, String name, IShapeProvider shape, @Nullable IForestryTicker<? extends T> clientTicker, @Nullable IForestryTicker<? extends T> serverTicker) {
		this.teType = teType;
		this.name = name;
		this.shape = shape;
		this.clientTicker = clientTicker;
		this.serverTicker = serverTicker;
	}

	@Override
	public void setBlock(Block block) {
		this.block = block;
	}

	@Nullable
	@Override
	public Block getBlock() {
		return block;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape.getShape(state, level, pos, context);
	}

	@Override
	public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
		return this.teType.tileType().create(pos, state);
	}

	@Nullable
	@Override
	public IForestryTicker<? extends T> getClientTicker() {
		return clientTicker;
	}

	@Nullable
	@Override
	public IForestryTicker<? extends T> getServerTicker() {
		return serverTicker;
	}

	@Override
	public BlockEntityType<? extends T> getTeType() {
		return this.teType.tileType();
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public static class Builder<T extends TileForestry, B extends Builder<T, ?>> {
		protected final FeatureTileType<? extends T> type;
		protected final String name;
		protected IShapeProvider shape = FULL_CUBE;
		@Nullable
		protected IForestryTicker<? extends T> clientTicker = null;
		@Nullable
		protected IForestryTicker<? extends T> serverTicker = null;

		public Builder(FeatureTileType<? extends T> type, String name) {
			this.type = type;
			this.name = name;
		}

		public B setShape(VoxelShape shape) {
			return setShape(() -> shape);
		}

		public B setShape(ISimpleShapeProvider shape) {
			this.shape = shape;
			//noinspection unchecked
			return (B) this;
		}

		public B setShape(IShapeProvider shape) {
			this.shape = shape;
			//noinspection unchecked
			return (B) this;
		}

		public B setClientTicker(@Nullable IForestryTicker<? extends T> clientTicker) {
			this.clientTicker = clientTicker;
			//noinspection unchecked
			return (B) this;
		}

		public B setServerTicker(@Nullable IForestryTicker<? extends T> serverTicker) {
			this.serverTicker = serverTicker;
			//noinspection unchecked
			return (B) this;
		}

		public MachineProperties<T> create() {
			Preconditions.checkNotNull(shape);
			return new MachineProperties<>(type, name, shape, clientTicker, serverTicker);
		}
	}
}

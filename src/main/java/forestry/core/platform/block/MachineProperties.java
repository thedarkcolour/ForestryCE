package forestry.core.platform.block;

import com.google.common.base.Preconditions;
import forestry.core.platform.tile.IForestryTicker;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.registration.FeatureTileType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class MachineProperties<T extends ForestryBlockEntity> implements IMachineProperties<T> {
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

	private final TankLayout tankLayout;

	public MachineProperties(FeatureTileType<? extends T> teType, String name, IShapeProvider shape, TankLayout tankLayout, @Nullable IForestryTicker<? extends T> clientTicker, @Nullable IForestryTicker<? extends T> serverTicker) {
		this.teType = teType;
		this.name = name;
		this.shape = shape;
		this.tankLayout = tankLayout;
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
		return this.block;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shape.getShape(state, level, pos, context);
	}

	@Override
	public TankLayout getTankLayout() {
		return this.tankLayout;
	}

	@Override
	public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
		return this.teType.tileType().create(pos, state);
	}

	@Nullable
	@Override
	public IForestryTicker<? extends T> getClientTicker() {
		return this.clientTicker;
	}

	@Nullable
	@Override
	public IForestryTicker<? extends T> getServerTicker() {
		return this.serverTicker;
	}

	@Override
	public BlockEntityType<? extends T> getTeType() {
		return this.teType.tileType();
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static class Builder<T extends ForestryBlockEntity, B extends Builder<T, ?>> {
		protected final FeatureTileType<? extends T> type;
		protected final String name;
		protected IShapeProvider shape = FULL_CUBE;
		protected TankLayout tankLayout = IMachineProperties.TankLayout.NONE;
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

		public B setTankLayout(TankLayout tankLayout) {
			this.tankLayout = tankLayout;
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
			Preconditions.checkNotNull(this.shape);
			return new MachineProperties<>(this.type, this.name, this.shape, this.tankLayout, this.clientTicker, this.serverTicker);
		}
	}
}

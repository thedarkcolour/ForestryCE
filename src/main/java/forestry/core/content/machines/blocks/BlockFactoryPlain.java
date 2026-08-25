package forestry.core.content.machines.blocks;

import forestry.core.platform.block.BlockBase;
import forestry.core.platform.block.IMachineProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockFactoryPlain extends BlockBase<BlockTypeFactoryPlain> {
	public static final IntegerProperty TANK_PRODUCT_LEVEL = IntegerProperty.create("tank_product_fill_level", 0, 4);
	public static final IntegerProperty TANK_RESOURCE_LEVEL = IntegerProperty.create("tank_resource_fill_level", 0, 4);

	// createBlockStateDefinition runs from within the Block constructor, before BlockBase has assigned
	// this.blockType, so the type is smuggled in through this thread-local instead
	private static final ThreadLocal<BlockTypeFactoryPlain> CACHE_TYPE = new ThreadLocal<>();

	private BlockFactoryPlain(BlockTypeFactoryPlain type) {
		super(Properties.of(), type);
	}

	public static void setMachineType(BlockTypeFactoryPlain type) {
		CACHE_TYPE.set(type);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);

		BlockTypeFactoryPlain type = CACHE_TYPE.get();

		if (type != null) {
			IMachineProperties.TankLayout layout = type.getMachineProperties().getTankLayout();

			switch (layout) {
				case BOTH -> {
					builder.add(TANK_PRODUCT_LEVEL);
					builder.add(TANK_RESOURCE_LEVEL);
				}
				case RESOURCE -> builder.add(TANK_RESOURCE_LEVEL);
				case PRODUCT -> builder.add(TANK_PRODUCT_LEVEL);
			}
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		switch (this.blockType) {
			case FABRICATOR -> {
				return Shapes.block();
			}
			default -> {
				if (state.getValue(BlockBase.FACING) == Direction.SOUTH || state.getValue(BlockBase.FACING) == Direction.NORTH) {
					return Shapes.or(
						Block.box(0, 0, 0, 16, 16, 4),
						Block.box(2, 2, 4, 14, 14, 12),
						Block.box(0, 0, 12, 16, 16, 16)
					);
				} else {
					return Shapes.or(
						Block.box(0, 0, 0, 4, 16, 16),
						Block.box(4, 2, 2, 12, 14, 14),
						Block.box(12, 0, 0, 16, 16, 16)
					);
				}
			}
		}
	}

	/**
	 * The registry needs to use this instead of calling BlockFactoryPlain::new, because otherwise
	 * createBlockStateDefinition can't see the type and blockstates don't generate.
	 *
	 * @param type The type of factory block to make
	 * @return A new instance of this factory block
	 */
	public static BlockFactoryPlain create(BlockTypeFactoryPlain type) {
		setMachineType(type);
		return new BlockFactoryPlain(type);
	}
}

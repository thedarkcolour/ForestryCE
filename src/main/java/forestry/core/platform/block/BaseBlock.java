package forestry.core.platform.block;

import forestry.api.agriculture.HorizontalDirection;
import forestry.core.engine.circuits.ISocketable;
import forestry.core.platform.tile.BaseBlockEntity;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidUtil;

import javax.annotation.Nullable;

public class BaseBlock<P extends IBlockType> extends ForestryBlock implements EntityBlock {
	/**
	 * use this instead of {@link net.minecraft.world.level.block.HorizontalDirectionalBlock#FACING} so the blocks rotate in a circle instead of NSWE order.
	 */
	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, HorizontalDirection.VALUES);

	public final P blockType;

	private static Block.Properties createProperties(Block.Properties properties) {
		return properties.strength(2.0f);
	}

	public BaseBlock(Block.Properties properties, P blockType) {
		super(createProperties(properties));

		if (getStateDefinition().any().hasProperty(FACING)) {
			registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
		}

		this.blockType = blockType;

		blockType.getMachineProperties().setBlock(this);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState p_220080_1_, BlockGetter p_220080_2_, BlockPos p_220080_3_) {
		return 0.2F;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return getDefinition().createTileEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> actualType) {
		if (actualType == this.blockType.getMachineProperties().getTeType()) {
			//noinspection unchecked
			return (BlockEntityTicker<T>) (level.isClientSide ? this.blockType.getMachineProperties().getClientTicker() : this.blockType.getMachineProperties().getServerTicker());
		} else {
			return null;
		}
	}

	private IMachineProperties<?> getDefinition() {
		return this.blockType.getMachineProperties();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		IMachineProperties<?> definition = getDefinition();
		return definition.getShape(state, reader, pos, context);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit) {
		BaseBlockEntity tile = TileUtil.getTile(worldIn, pos, BaseBlockEntity.class);
		if (tile == null) {
			return InteractionResult.PASS;
		}
		if (TileUtil.isUsableByPlayer(playerIn, tile)) {
			if (!worldIn.isClientSide) {
				ServerPlayer sPlayer = (ServerPlayer) playerIn;
				tile.openGui(sPlayer, InteractionHand.MAIN_HAND, pos);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!playerIn.isShiftKeyDown() && FluidUtil.interactWithFluidHandler(playerIn, hand, worldIn, pos, hit.getDirection())) {
			return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
		}
		return useWithoutItem(state, worldIn, pos, playerIn, hit).consumesAction()
			? ItemInteractionResult.sidedSuccess(worldIn.isClientSide)
			: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity tile = TileUtil.getTile(level, pos);
			if (tile instanceof Container inventory) {
				Containers.dropContents(level, pos, inventory);
			}
			if (tile instanceof ForestryBlockEntity forestry) {
				forestry.onDropContents((ServerLevel) level);
			}
			if (tile instanceof ISocketable socketable) {
				InventoryUtil.dropSockets(socketable, level, pos);
			}
		}

		// Remove tile entity after emptying out its contents
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Direction facing = state.getValue(FACING);
		return state.setValue(FACING, rot.rotate(facing));
	}
}

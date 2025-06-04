package forestry.core.blocks;

import forestry.core.circuits.ISocketable;
import forestry.core.tiles.TileBase;
import forestry.core.tiles.TileForestry;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidUtil;

import javax.annotation.Nullable;

public class BlockBase<P extends IBlockType> extends BlockForestry implements EntityBlock {
	public final P blockType;

	public BlockBase(P blockType, Block.Properties properties) {
		super(properties.strength(2.0f));

		if (getStateDefinition().any().hasProperty(HorizontalDirectionalBlock.FACING)) {
			registerDefaultState(getStateDefinition().any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
		}

		this.blockType = blockType;

		blockType.getMachineProperties().setBlock(this);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HorizontalDirectionalBlock.FACING);
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
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		TileBase tile = TileUtil.getTile(level, pos, TileBase.class);
		if (tile != null) {
			if (TileUtil.isUsableByPlayer(player, tile)) {
				// todo do we need this SHIFT check
				if ((!player.isShiftKeyDown() && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection()))
					|| (tile.interactWithItem(level, pos, player, hand, stack))) {
					return ItemInteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		TileBase tile = TileUtil.getTile(level, pos, TileBase.class);

		if (tile != null && tile.interactNoItem(level, player, pos)) {
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity tile = TileUtil.getTile(level, pos);
			if (tile instanceof Container inventory) {
				Containers.dropContents(level, pos, inventory);
			}
			if (tile instanceof TileForestry forestry) {
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
		Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
		return state.setValue(HorizontalDirectionalBlock.FACING, rot.rotate(facing));
	}
}

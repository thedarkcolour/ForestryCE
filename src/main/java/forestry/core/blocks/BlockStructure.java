package forestry.core.blocks;

import com.mojang.authlib.GameProfile;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.api.multiblock.IMultiblockController;
import forestry.core.multiblock.MultiblockTileEntityForestry;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class BlockStructure extends BlockForestry {
	protected BlockStructure(Block.Properties properties) {
		super(properties.strength(1f));
	}

	protected long previousMessageTick = 0;

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (stack.isEmpty()) {
			MultiblockTileEntityForestry<?> part = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
			if (part == null) {
				return ItemInteractionResult.FAIL;
			}

			IMultiblockController controller = part.getMultiblockLogic().getController();

			if (!controller.isAssembled()) {
				String validationError = controller.getLastValidationError();

				if (validationError != null) {
					long tick = level.getGameTime();

					if (tick > this.previousMessageTick + 20) {
						player.sendSystemMessage(Component.literal(validationError));
						this.previousMessageTick = tick;
					}

					return ItemInteractionResult.sidedSuccess(level.isClientSide);
				}
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		MultiblockTileEntityForestry<?> part = TileUtil.getTile(level, pos, MultiblockTileEntityForestry.class);
		if (part == null) {
			return InteractionResult.FAIL;
		}

		IMultiblockController controller = part.getMultiblockLogic().getController();

		if (controller.isAssembled()) {
			if (!level.isClientSide) {
				part.openGui((ServerPlayer) player, pos);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return InteractionResult.PASS;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (world.isClientSide) {
			return;
		}

		if (placer instanceof Player) {
			TileUtil.actOnTile(world, pos, MultiblockTileEntityForestry.class, tile -> {
				Player player = (Player) placer;
				GameProfile gameProfile = player.getGameProfile();
				tile.setOwner(gameProfile);
			});
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (level.getBlockEntity(pos) instanceof IMultiblockComponent.HasInventory component) {
			Containers.dropContents(level, pos, component.getInternalInventory());
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}
}

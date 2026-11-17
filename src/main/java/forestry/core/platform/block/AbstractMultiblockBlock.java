package forestry.core.platform.block;

import com.mojang.authlib.GameProfile;
import forestry.api.core.multiblock.IMultiblockComponent;
import forestry.api.core.multiblock.IMultiblockController;
import forestry.core.platform.multiblock.MultiblockTileEntityForestry;
import forestry.core.platform.multiblock.MultiblockValidation;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractMultiblockBlock extends ForestryBlock {
	protected AbstractMultiblockBlock(Block.Properties properties) {
		super(properties.strength(1f));
	}

	protected long previousMessageTick = 0;

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit) {
		if (playerIn.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		MultiblockTileEntityForestry part = TileUtil.getTile(worldIn, pos, MultiblockTileEntityForestry.class);
		if (part == null) {
			return InteractionResult.FAIL;
		}
		IMultiblockController controller = part.getMultiblockLogic().getController();
		if (!controller.isAssembled()) {
			// Prefer a stored error from a controller that has tried (and failed) to (re)assemble; for a
			// never-formed block the controller resolves to the Fake stand-in (no stored error), so compute
			// the hint on demand by running the pattern validator directly for this block (spec §11).
			String validationError = controller.getLastValidationError();
			if (validationError == null) {
				Component hint = MultiblockValidation.findValidationHint(worldIn, pos, part);
				if (hint != null) {
					validationError = hint.getString();
				}
			}
			if (validationError != null) {
				long tick = worldIn.getGameTime();
				if (tick > this.previousMessageTick + 20) {
					playerIn.sendSystemMessage(Component.literal(validationError));
					this.previousMessageTick = tick;
				}
				return InteractionResult.SUCCESS;
			}
		}

		// Don't open the GUI if the multiblock isn't assembled
		if (!controller.isAssembled()) {
			return InteractionResult.PASS;
		}

		if (!worldIn.isClientSide) {
			part.openGui((ServerPlayer) playerIn, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		return useWithoutItem(state, worldIn, pos, playerIn, hit).consumesAction()
			? ItemInteractionResult.sidedSuccess(worldIn.isClientSide)
			: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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

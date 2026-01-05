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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BlockStructure extends BlockForestry {
	protected BlockStructure(Block.Properties properties) {
		super(properties.strength(1f));
	}

	protected long previousMessageTick = 0;

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (playerIn.isShiftKeyDown()) { //isSneaking
			return InteractionResult.PASS;
		}

		MultiblockTileEntityForestry part = TileUtil.getTile(worldIn, pos, MultiblockTileEntityForestry.class);
		if (part == null) {
			return InteractionResult.FAIL;
		}
		IMultiblockController controller = part.getMultiblockLogic().getController();

		ItemStack heldItem = playerIn.getItemInHand(hand);
		// If the player's hands are empty and they right-click on a multiblock, they get a
		// multiblock-debugging message if the machine is not assembled.
		if (heldItem.isEmpty()) {
			if (!controller.isAssembled()) {
				String validationError = controller.getLastValidationError();
				if (validationError != null) {
					long tick = worldIn.getGameTime();
					if (tick > this.previousMessageTick + 20) {
						playerIn.sendSystemMessage(Component.literal(validationError));
                        this.previousMessageTick = tick;
					}
					return InteractionResult.SUCCESS;
				}
			}
		}

		// Don't open the GUI if the multiblock isn't assembled
		if (controller == null || !controller.isAssembled()) {
			return InteractionResult.PASS;
		}

		if (!worldIn.isClientSide) {
			part.openGui((ServerPlayer) playerIn, pos);
		}
		return InteractionResult.SUCCESS;
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

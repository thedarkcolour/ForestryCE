package forestry.core.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * The anchor-only multiblock ticker (plan Task 2.4; spec §7.1). Replaces the deleted global
 * {@code MultiblockServerTickHandler} tick loop with a per-block {@link BlockEntityTicker}.
 *
 * <p>Because a structure contains many member BEs of the same type, this ticker body <b>early-returns
 * unless the block is the holder/anchor and the controller is assembled</b> — so the machine logic runs
 * exactly once per tick (on the holder), not once per member. The holder may be any member type, so
 * {@code BlockAlveary}/{@code FarmBlock} return this ticker for <em>every</em> member
 * {@code BlockEntityType}.
 */
public final class MultiblockTicker {
	private static final ServerTicker SERVER = new ServerTicker();
	private static final ClientTicker CLIENT = new ClientTicker();

	private MultiblockTicker() {
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level) {
		return (BlockEntityTicker<T>) (level.isClientSide ? CLIENT : SERVER);
	}

	@Nullable
	private static MultiblockController resolveHolderController(Level level, BlockPos pos, BlockEntity be) {
		if (!(be instanceof MultiblockTileEntityForestry<?> member)) {
			return null;
		}
		BlockPos anchor = member.getAnchorPos();
		// Anchor-only guard: only the holder instance runs the machine logic (spec §7.1).
		if (anchor == null || !anchor.equals(pos)) {
			return null;
		}
		MultiblockController controller = MultiblockIndex.get(level, anchor);
		if (controller == null || !controller.isAssembled()) {
			return null;
		}
		return controller;
	}

	private static final class ServerTicker implements BlockEntityTicker<BlockEntity> {
		@Override
		public void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
			MultiblockController controller = resolveHolderController(level, pos, be);
			if (controller == null) {
				return;
			}
			// Stagger machines by a per-controller phase so they don't all hit interval boundaries on the same
			// game tick (spec §7.1; MINOR 7 restores the old engine's per-machine random start offset).
			int tickCount = (int) level.getGameTime() + controller.getTickPhase();
			if (controller.serverTick(tickCount)) {
				// State changed: mark the holder's chunk dirty so it persists (spec §6.1; the holder owns the
				// payload). Per-chunk dirtying across the whole bbox is a Phase-4 refinement.
				MultiblockController.markChunkDirty(level, pos);
			}
		}
	}

	private static final class ClientTicker implements BlockEntityTicker<BlockEntity> {
		@Override
		public void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
			MultiblockController controller = resolveHolderController(level, pos, be);
			if (controller == null) {
				return;
			}
			controller.clientTick((int) level.getGameTime() + controller.getTickPhase());
		}
	}
}

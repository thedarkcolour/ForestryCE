package forestry.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

// real chunk loading and unloading for GameTests. The framework force-loads every test's chunks and a ticket keeps
// chunks in memory out to ChunkLevel.MAX_LEVEL (about 11 chunks), so nothing near the test grid can unload
//
// A ticket at distance d gives its chunk level 33 - d and each chunk further out one more. 33 is fully loaded and 32
// also ticks block entities, so hold(pos, 0) leaves the neighbors just short of fully loaded, like the edge of a
// player's loaded area. A chunk past 33 keeps its block entities until no ticket reaches it at all
public final class ChunkLifecycle {
	private static final TicketType<ChunkPos> TICKET = TicketType.create("forestry_gametest", Comparator.comparingLong(ChunkPos::toLong));
	// far enough past the grid and apart from each other that no two sites' tickets overlap
	private static final int SITE_DISTANCE = 700;
	private static final int SITE_SPACING = 64;
	private static final AtomicInteger NEXT_SITE = new AtomicInteger();
	private static final long WAIT_NANOS = TimeUnit.SECONDS.toNanos(30);

	private ChunkLifecycle() {
	}

	public static ChunkPos isolatedSite(GameTestHelper helper) {
		ChunkPos grid = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
		return new ChunkPos(grid.x + SITE_DISTANCE + NEXT_SITE.getAndIncrement() * SITE_SPACING, grid.z);
	}

	public static void hold(ServerLevel level, ChunkPos pos, int distance) {
		level.getChunkSource().addRegionTicket(TICKET, pos, distance, pos);
	}

	public static void release(ServerLevel level, ChunkPos pos, int distance) {
		level.getChunkSource().removeRegionTicket(TICKET, pos, distance, pos);
	}

	// never loads the chunk, unlike Level.getBlockEntity
	public static boolean isFull(ServerLevel level, ChunkPos pos) {
		return level.getChunkSource().getChunkNow(pos.x, pos.z) != null;
	}

	// the condition LevelChunk.isTicking checks before it ticks a block entity, which a FULL chunk can lag behind
	public static boolean isTicking(ServerLevel level, ChunkPos pos) {
		return level.getChunkSource().getChunkNow(pos.x, pos.z) instanceof LevelChunk chunk
			&& chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)
			&& level.areEntitiesLoaded(pos.toLong());
	}

	@Nullable
	public static BlockEntity blockEntityIfLoaded(ServerLevel level, BlockPos pos) {
		return isFull(level, new ChunkPos(pos)) ? level.getBlockEntity(pos) : null;
	}

	public static List<BlockEntity> blockEntities(ServerLevel level, List<BlockPos> positions) {
		List<BlockEntity> result = new ArrayList<>(positions.size());
		for (BlockPos pos : positions) {
			BlockEntity be = blockEntityIfLoaded(level, pos);
			if (be != null) {
				result.add(be);
			}
		}
		return result;
	}

	// true once the chunk has been saved and its block entities got onChunkUnloaded and setRemoved
	public static boolean isUnloaded(List<BlockEntity> blockEntities) {
		for (BlockEntity be : blockEntities) {
			if (!be.isRemoved()) {
				return false;
			}
		}
		return true;
	}

	public static BlockPos relative(GameTestHelper helper, BlockPos absolute) {
		return absolute.subtract(helper.absolutePos(BlockPos.ZERO));
	}

	// a position on the site at the grid's floor height, relative to the test
	public static BlockPos siteBlock(GameTestHelper helper, ChunkPos site, int x, int z) {
		int y = helper.absolutePos(new BlockPos(0, 1, 0)).getY();
		return relative(helper, new BlockPos(site.getMinBlockX() + x, y, site.getMinBlockZ() + z));
	}

	public static Steps steps(GameTestHelper helper) {
		return new Steps(helper);
	}

	// a GameTestSequence that stops at the first failure, since a plain one runs the later steps in the same tick and
	// reports whichever failed last
	public static final class Steps {
		private final GameTestHelper helper;
		private final GameTestSequence sequence;
		private boolean failed;

		private Steps(GameTestHelper helper) {
			this.helper = helper;
			this.sequence = helper.startSequence();
		}

		public Steps run(Runnable task) {
			this.sequence.thenExecute(() -> {
				if (this.failed) {
					return;
				}
				try {
					task.run();
				} catch (GameTestAssertException e) {
					this.failed = true;
					throw e;
				}
			});
			return this;
		}

		// the test server never sleeps between ticks, so a tick timeout is about a second of real time while chunk
		// work runs at real speed on other threads
		public Steps await(BooleanSupplier condition, String message) {
			long[] deadline = new long[1];
			run(() -> deadline[0] = System.nanoTime() + WAIT_NANOS);
			// an assertion thrown while waiting only means "not yet", so the wait ends at the deadline and fails below
			this.sequence.thenWaitUntil(() -> {
				if (!this.failed && !condition.getAsBoolean() && System.nanoTime() < deadline[0]) {
					throw new GameTestAssertException(message);
				}
			});
			return run(() -> this.helper.assertTrue(condition.getAsBoolean(), message));
		}

		// runs the task once in this tick and once after each of the next ticks level ticks
		public Steps everyTick(int ticks, Runnable task) {
			int[] left = new int[1];
			run(() -> left[0] = ticks);
			this.sequence.thenWaitUntil(() -> {
				if (this.failed) {
					return;
				}
				task.run();
				if (left[0]-- > 0) {
					throw new GameTestAssertException("watching");
				}
			});
			return this;
		}

		public Steps idle(int ticks) {
			this.sequence.thenIdle(ticks);
			return this;
		}

		public void succeed() {
			run(this.helper::succeed);
		}
	}
}

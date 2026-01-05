package forestry.core.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

// todo does this actually improve performance?

/**
 * A helper class that caches adjacent tiles for a given tile entity.
 * <p>
 * Listeners can be added to listen for adjacent tile changes.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class AdjacentTileCache {
	private static final int DELAY_MIN = 20;
	private static final int DELAY_MAX = 2400;
	private static final int DELAY_STEP = 2;

	private final Timer[] timer = new Timer[6];
	private final BlockEntity[] cache = new BlockEntity[6];
	private final int[] delay = new int[6];
	private final BlockEntity source;
	private final Set<ICacheListener> listeners = new LinkedHashSet<>();

	/**
	 * Listener that listens for adjacent tile changes.
	 */
	public interface ICacheListener {
		/**
		 * Called at the moment the tile registers an adjacent tile change.
		 */
		void changed();

		/**
		 * Calle if the tile entity gets removed to clean the cached data.
		 */
		void purge();
	}

	public AdjacentTileCache(BlockEntity tile) {
		this.source = tile;
		Arrays.fill(this.delay, DELAY_MIN);
		for (int i = 0; i < this.timer.length; i++) {
            this.timer[i] = new Timer();
		}
	}

	public void addListener(ICacheListener listener) {
        this.listeners.add(listener);
	}

	@Nullable
	private BlockEntity searchSide(Direction side) {
		Level world = this.source.getLevel();
		BlockPos pos = this.source.getBlockPos().relative(side);
		if (world.hasChunkAt(pos) && !world.isEmptyBlock(pos)) {
			return TileUtil.getTile(world, pos);
		}
		return null;
	}

	public void refresh() {
		for (Direction side : Direction.values()) {
			getTileOnSide(side);
		}
	}

	public void purge() {
		Arrays.fill(this.cache, null);
		Arrays.fill(this.delay, DELAY_MIN);
		for (Timer t : this.timer) {
			t.reset();
		}
		changed();
		for (ICacheListener listener : this.listeners) {
			listener.purge();
		}
	}

	public void onNeighborChange() {
		Arrays.fill(this.delay, DELAY_MIN);
	}

	private void setTile(int side, @Nullable BlockEntity tile) {
		if (this.cache[side] != tile) {
            this.cache[side] = tile;
			changed();
		}
	}

	private void changed() {
		for (ICacheListener listener : this.listeners) {
			listener.changed();
		}
	}

	private boolean areCoordinatesOnSide(Direction side, BlockEntity target) {
		return this.source.getBlockPos().getX() + side.getStepX() == target.getBlockPos().getX() && this.source.getBlockPos().getY() + side.getStepY() == target.getBlockPos().getY() && this.source.getBlockPos().getZ() + side.getStepZ() == target.getBlockPos().getZ();
	}

	@Nullable
	public BlockEntity getTileOnSide(Direction side) {
		int s = side.ordinal();
		if (this.cache[s] != null) {
			if (this.cache[s].isRemoved() || !areCoordinatesOnSide(side, this.cache[s])) {
				setTile(s, null);
			} else {
				return this.cache[s];
			}
		}

		if (this.timer[s].hasTriggered(this.source.getLevel(), this.delay[s])) {
			setTile(s, searchSide(side));
			if (this.cache[s] == null) {
				incrementDelay(s);
			} else {
                this.delay[s] = DELAY_MIN;
			}
		}

		return this.cache[s];
	}

	private void incrementDelay(int side) {
        this.delay[side] += DELAY_STEP;
		if (this.delay[side] > DELAY_MAX) {
            this.delay[side] = DELAY_MAX;
		}
	}

	public BlockEntity getSource() {
		return this.source;
	}

	private static class Timer {

		private long startTime = Long.MIN_VALUE;

		public boolean hasTriggered(Level world, int ticks) {
			long currentTime = world.getGameTime();
			if (currentTime >= ticks + this.startTime || this.startTime > currentTime) {
                this.startTime = currentTime;
				return true;
			}
			return false;
		}

		public void reset() {
            this.startTime = Long.MIN_VALUE;
		}

	}
}

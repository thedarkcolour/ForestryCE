package forestry.farming;

import forestry.api.farming.IFarmHousing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FarmTarget {
	private final BlockPos start;
	private final Direction direction;
	private final int limit;

	private int yOffset;
	private int extent;

	public FarmTarget(BlockPos start, Direction direction, int limit) {
		this.start = start;
		this.direction = direction;
		this.limit = limit;
	}

	public BlockPos getStart() {
		return this.start;
	}

	public int getYOffset() {
		return this.yOffset;
	}

	public int getExtent() {
		return this.extent;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setExtentAndYOffset(Level level, @Nullable BlockPos platformPosition, IFarmHousing housing) {
		if (platformPosition == null) {
            this.extent = 0;
			return;
		}

		BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
		position.set(platformPosition);
		for (this.extent = 0; this.extent < this.limit; this.extent++) {
			if (!level.hasChunkAt(position)) {
				break;
			}
			if (!housing.isValidPlatform(level, position)) {
				break;
			}
			position.move(this.direction);
		}

        this.yOffset = platformPosition.getY() + 1 - getStart().getY();
	}
}

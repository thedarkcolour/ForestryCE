package forestry.core.multiblock;

import com.google.common.base.Preconditions;

/**
 * @param minimumNumberOfBlocksForAssembledMachine The minimum number of blocks connected to the machine for it to be
 *                                                 assembled. Helper method so we don't check for a whole machine until
 *                                                 we have enough blocks to actually assemble it. This isn't as simple
 *                                                 as xmax*ymax*zmax for non-cubic machines or for machines with
 *                                                 hollow/complex interiors.
 * @param minimumXSize                             The minimum X dimension size of the machine
 * @param minimumYSize                             The minimum Y dimension size of the machine
 * @param minimumZSize                             The minimum Z dimension size of the machine
 * @param maximumXSize                             The maximum X dimension size of the machine, or {@link #DIMENSION_UNBOUNDED}
 * @param maximumYSize                             The maximum Y dimension size of the machine, or {@link #DIMENSION_UNBOUNDED}
 * @param maximumZSize                             The maximum Z dimension size of the machine, or {@link #DIMENSION_UNBOUNDED}
 */
public record MultiblockSizeLimits(
	int minimumNumberOfBlocksForAssembledMachine,
	int minimumXSize,
	int minimumYSize,
	int minimumZSize,
	int maximumXSize,
	int maximumYSize,
	int maximumZSize
) {
	/**
	 * Used to specify that this multiblock has no maximum size in a given dimension.
	 */
	public static final int DIMENSION_UNBOUNDED = -1;

	public MultiblockSizeLimits {
		Preconditions.checkArgument(minimumXSize >= 1);
		Preconditions.checkArgument(minimumYSize >= 1);
		Preconditions.checkArgument(minimumZSize >= 1);
		Preconditions.checkArgument(maximumXSize >= minimumXSize || maximumXSize == DIMENSION_UNBOUNDED);
		Preconditions.checkArgument(maximumYSize >= minimumYSize || maximumYSize == DIMENSION_UNBOUNDED);
		Preconditions.checkArgument(maximumZSize >= minimumZSize || maximumZSize == DIMENSION_UNBOUNDED);
	}
}

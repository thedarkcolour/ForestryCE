package forestry.farming.multiblock;

import forestry.core.multiblock.IMultiblockSizeLimits;

enum FarmMultiblockSizeLimits implements IMultiblockSizeLimits {
	INSTANCE;

	@Override
	public int getMinimumNumberOfBlocksForAssembledMachine() {
		return 3 * 3 * 4;
	}

	@Override
	public int getMaximumXSize() {
		return 5;
	}

	@Override
	public int getMaximumZSize() {
		return 5;
	}

	@Override
	public int getMaximumYSize() {
		return 4;
	}

	@Override
	public int getMinimumXSize() {
		return 3;
	}

	@Override
	public int getMinimumZSize() {
		return 3;
	}

	@Override
	public int getMinimumYSize() {
		return 4;
	}
}

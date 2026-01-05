package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public class TreeBlockTypeLeaf implements ITreeBlockType {
	private final ITreeGenData tree;
	private final IGenome genome;

	public TreeBlockTypeLeaf(ITreeGenData tree, IGenome genome) {
		this.tree = tree;
		this.genome = genome;
	}

	@Override
	public void setDirection(Direction facing) {
	}

	@Override
	public boolean setBlock(LevelAccessor level, BlockPos pos) {
		return this.tree.setLeaves(this.genome, level, pos, level.getRandom(), false);
	}
}

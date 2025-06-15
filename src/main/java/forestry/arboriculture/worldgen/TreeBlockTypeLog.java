package forestry.arboriculture.worldgen;

import forestry.api.arboriculture.genetics.ITreeSpecies;
import forestry.api.genetics.IGenome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public class TreeBlockTypeLog implements ITreeBlockType {
	private final ITreeSpecies tree;
	private final IGenome genome;
	private Direction facing = Direction.UP;

	public TreeBlockTypeLog(ITreeSpecies tree, IGenome genome) {
		this.tree = tree;
		this.genome = genome;
	}

	@Override
	public void setDirection(Direction facing) {
		this.facing = facing;
	}

	@Override
	public boolean setBlock(LevelAccessor level, BlockPos pos) {
		return this.tree.setLogBlock(this.genome, level, pos, this.facing);
	}
}

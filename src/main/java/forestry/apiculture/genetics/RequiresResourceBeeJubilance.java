package forestry.apiculture.genetics;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.IGenome;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;

// todo expose in IForestryApi
public class RequiresResourceBeeJubilance implements IBeeJubilance {
	private final HashSet<BlockState> acceptedBlockStates = new HashSet<>();

	public RequiresResourceBeeJubilance(BlockState... acceptedBlockStates) {
		Collections.addAll(this.acceptedBlockStates, acceptedBlockStates);
	}

	@Override
	public boolean isJubilant(IBeeSpecies species, IGenome genome, IBeeHousing housing) {
		Level level = housing.getLevel();
		BlockPos pos = housing.getBlockPos();

		BlockEntity tile;
		do {
			pos = pos.below();
			tile = TileUtil.getTile(level, pos);
		} while (tile instanceof IBeeHousing && pos.getY() > level.dimensionType().minY());

		BlockState blockState = level.getBlockState(pos);
		return this.acceptedBlockStates.contains(blockState);
	}
}

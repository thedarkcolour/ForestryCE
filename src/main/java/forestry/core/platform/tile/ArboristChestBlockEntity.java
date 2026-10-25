package forestry.core.platform.tile;

import forestry.api.core.genetics.ForestrySpeciesTypes;
import forestry.core.features.CoreTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ArboristChestBlockEntity extends NaturalistChestBlockEntity {
	public ArboristChestBlockEntity(BlockPos pos, BlockState state) {
		super(CoreTiles.ARBORIST_CHEST.tileType(), pos, state, ForestrySpeciesTypes.TREE);
	}
}

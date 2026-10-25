package forestry.core.platform.tile;

import forestry.api.core.genetics.ForestrySpeciesTypes;
import forestry.core.features.CoreTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class LepidopteristChestBlockEntity extends NaturalistChestBlockEntity {
	public LepidopteristChestBlockEntity(BlockPos pos, BlockState state) {
		super(CoreTiles.LEPIDOPTERIST_CHEST.tileType(), pos, state, ForestrySpeciesTypes.BUTTERFLY);
	}
}

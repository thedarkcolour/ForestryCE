package forestry.arboriculture.tiles;

import forestry.arboriculture.features.ArboricultureTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileForestryHangingSign extends HangingSignBlockEntity {
	public TileForestryHangingSign(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public BlockEntityType<?> getType() {
		return ArboricultureTiles.HANGING_SIGN.tileType();
	}
}

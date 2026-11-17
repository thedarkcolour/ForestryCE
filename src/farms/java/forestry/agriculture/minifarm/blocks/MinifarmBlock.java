package forestry.agriculture.minifarm.blocks;

import forestry.core.platform.block.BaseBlock;
import forestry.core.platform.render.ParticleRender;
import forestry.agriculture.minifarm.tiles.AbstractMinifarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinifarmBlock extends BaseBlock<MinifarmBlockType> {
	private final boolean manual;

	public MinifarmBlock(MinifarmBlockType type, boolean manual) {
		super(Properties.of().noOcclusion(), type);
		this.manual = manual;
	}

	public boolean isManual() {
		return this.manual;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (this.blockType == MinifarmBlockType.FARM_ENDER) {
			for (int i = 0; i < 3; ++i) {
				ParticleRender.addPortalFx(level, pos, rand);
			}
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		BlockEntity tile = super.newBlockEntity(pos, state);

		if (tile instanceof AbstractMinifarmBlockEntity planter) {
			planter.setManual(this.manual);
		}

		return tile;
	}
}

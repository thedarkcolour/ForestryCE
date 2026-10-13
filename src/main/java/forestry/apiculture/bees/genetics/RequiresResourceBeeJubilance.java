package forestry.apiculture.bees.genetics;

import com.mojang.serialization.MapCodec;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.core.platform.tile.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RequiresResourceBeeJubilance implements IBeeJubilance {
	public static final MapCodec<RequiresResourceBeeJubilance> MAP_CODEC = BlockMatcher.CODEC.fieldOf("blocks")
		.xmap(RequiresResourceBeeJubilance::new, jubilance -> jubilance.blocks);

	private final BlockMatcher blocks;

	public RequiresResourceBeeJubilance(BlockMatcher blocks) {
		this.blocks = blocks;
	}

	@Override
	public MapCodec<? extends IBeeJubilance> codec() {
		return MAP_CODEC;
	}

	@Override
	public boolean isJubilant(IBeeSpecies species, IGenome genome, IBeeHousing housing) {
		Level level = housing.getLevel();
		BlockPos pos = housing.getBlockPos();

		BlockEntity tile;
		do {
			pos = pos.below();
			tile = TileUtil.getTile(level, pos);
		} while (tile instanceof IBeeHousing && pos.getY() > 0);

		return this.blocks.matches(level.getBlockState(pos));
	}

}

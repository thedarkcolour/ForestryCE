package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GlowBerryGrowEffect extends ThrottledBeeEffect{
    public GlowBerryGrowEffect() {
        super(false, 200, true, true);
    }

    @Override
    IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
        Level level = housing.getWorldObj();
        Vec3i area = Bee.getParticleArea(genome, housing);
        Vec3i offset = VecUtil.scale(area, -1 / 2.0f);

        BlockPos randomPos = VecUtil.getRandomPositionInArea(level.random, area);

        BlockPos posBlock = randomPos.offset(housing.getCoordinates()).offset(offset);

        if (level.hasChunkAt(posBlock)) {
            BlockState state = level.getBlockState(posBlock);
            if (state.getBlock() instanceof CaveVines && !state.getValue(BlockStateProperties.BERRIES)) {
                level.setBlockAndUpdate(posBlock, state.setValue(BlockStateProperties.BERRIES,true));
            }
        }

        return storedData;
    }
}

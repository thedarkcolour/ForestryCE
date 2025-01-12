package forestry.apiculture;

import forestry.api.apiculture.IFlowerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PhotosynthesisFlowerType implements IFlowerType {
    @Override
    public boolean isAcceptableFlower(Level level, BlockPos pos) {
        return level.isDay() && level.getBrightness(LightLayer.SKY, pos)>=15;
    }

    @Override
    public boolean plantRandomFlower(Level level, BlockPos pos, List<BlockState> nearbyFlowers) {
        return false;
    }

    @Override
    public boolean isDominant() {
        return false;
    }
}

package forestry.apiculture;

import forestry.api.ForestryTags;
import forestry.api.apiculture.IFlowerType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

public class WaterFlowerType implements IFlowerType {
    private final TagKey<Block> acceptableFlowers;
    private final boolean dominant;

    public WaterFlowerType(TagKey<Block> acceptableFlowers, boolean dominant) {
        this.acceptableFlowers = acceptableFlowers;
        this.dominant = dominant;
    }

    @Override
    public boolean isAcceptableFlower(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(this.acceptableFlowers);
    }

    @Override
    public boolean plantRandomFlower(Level level, BlockPos pos, List<BlockState> nearbyFlowers) {
        if (level.hasChunkAt(pos) && level.getBlockState(pos).getBlock()== Blocks.WATER) {
            for (BlockState state : nearbyFlowers) {
                if (state.is(ForestryTags.Blocks.PLANTABLE_FLOWERS)) {
                    if (state.canSurvive(level, pos)) {
                        if (state.hasProperty(DoublePlantBlock.HALF)) {
                            BlockPos topPos = pos.above();

                            if (level.getBlockState(topPos).getBlock()==Blocks.WATER) {
                                return level.setBlockAndUpdate(pos, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
                                        && level.setBlockAndUpdate(topPos, state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                            }
                        } else {
                            return level.setBlockAndUpdate(pos, state);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDominant() {
        return this.dominant;
    }
}

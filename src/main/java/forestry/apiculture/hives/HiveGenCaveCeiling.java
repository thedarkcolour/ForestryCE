package forestry.apiculture.hives;

import forestry.api.apiculture.hives.IHiveGen;
import forestry.core.utils.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class HiveGenCaveCeiling implements IHiveGen {
    private final TagKey<Block> blocks;
    private final TagKey<Block> extra_replaceable;

    public HiveGenCaveCeiling(TagKey<Block> blocks, TagKey<Block> extra_replaceable){
        this.blocks=blocks;
        this.extra_replaceable=extra_replaceable;
    }

    @Override
    public @Nullable BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ) {
        return null;
    }

    @Override
    public @Nullable BlockPos getPosForHive(WorldGenLevel level, RandomSource rand, int posX, int posZ) {
        // get to the ground
        int groundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, posX, posZ);
        int minBuildHeight = level.getMinBuildHeight();
        if (groundY == minBuildHeight) {
            return null;
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, groundY, posZ);
        ArrayList<BlockPos> validPos=new ArrayList<>();

        BlockState blockState = level.getBlockState(pos);
        while (pos.getY() > minBuildHeight) {
            if(blockState.is(blocks)){
                BlockPos bellow=pos.below();
                if(canReplace(level.getBlockState(bellow),level,bellow)){
                    validPos.add(bellow);
                }
            }
            pos.move(Direction.DOWN);
            blockState = level.getBlockState(pos);
        }

        return !validPos.isEmpty()?validPos.get(validPos.size()>1?rand.nextInt(validPos.size()):0):null;
    }

    @Override
    public boolean isValidLocation(WorldGenLevel world, BlockPos pos) {
        return world.getBlockState(pos.above()).is(blocks);
    }

    @Override
    public boolean canReplace(BlockState blockState, WorldGenLevel world, BlockPos pos) {
        return BlockUtil.canReplace(blockState, world, pos)||blockState.is(extra_replaceable);
    }
}

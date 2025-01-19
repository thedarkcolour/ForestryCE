package forestry.apiculture.hives;

import forestry.api.apiculture.hives.IHiveGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class HiveGenOcean extends HiveGenGround {
    public HiveGenOcean(TagKey<Block> blocks) {
        super(blocks);
    }

    @Override
    public boolean canReplace(BlockState blockState, WorldGenLevel world, BlockPos pos) {
        return blockState.getBlock()== Blocks.WATER;
    }

    @Override
    public BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ) {
        // get to the ground
        int groundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, posX, posZ);
        int minBuildHeight = level.getMinBuildHeight();
        if (groundY == minBuildHeight) {
            return null;
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posX, groundY, posZ);

        BlockState blockState = level.getBlockState(pos);
        while (canReplace(blockState, level, pos)) {
            pos.move(Direction.DOWN);
            if (pos.getY() <= minBuildHeight) {
                return null;
            }
            blockState = level.getBlockState(pos);
        }

        return pos.above();
    }
}

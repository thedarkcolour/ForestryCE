package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.apiculture.genetics.Bee;
import forestry.core.utils.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class SculkSpreadEffect extends ThrottledBeeEffect{
    Random random=new Random();

    public SculkSpreadEffect() {
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
            if(state.is(BlockTags.SCULK_REPLACEABLE)){
                boolean hasAir=false;
                for(Direction direction:Direction.VALUES){
                    BlockState testState=level.getBlockState(posBlock.relative(direction));
                    if(testState.canBeReplaced()){
                        hasAir=true;
                        break;
                    }
                }
                if(hasAir){
                    //TODO:Sculk vein generation
                    level.setBlockAndUpdate(posBlock, Blocks.SCULK.defaultBlockState());
                }
            }else{
                if(state.getBlock()==Blocks.SCULK && level.getBlockState(posBlock.above()).canBeReplaced()){
                    if(random.nextInt(100)==0){
                        int nearbySculk=0;
                        BlockPos.MutableBlockPos mutableBlockPos=new BlockPos.MutableBlockPos();
                        for(int x= posBlock.getX()-4;x<= posBlock.getX()+4;x++){
                            for(int z= posBlock.getZ()-4;z<= posBlock.getZ()+4;z++){
                                for (int y= posBlock.getY();y<= posBlock.getY()+2;y++){
                                    mutableBlockPos.set(x,y,z);
                                    BlockState state1=level.getBlockState(mutableBlockPos);
                                    if(state1.getBlock()==Blocks.SCULK_SENSOR||state1.getBlock()==Blocks.SCULK_SHRIEKER){
                                        nearbySculk++;
                                        if(nearbySculk>2)
                                            return storedData;
                                    }
                                }
                            }
                        }
                        if(random.nextInt(10)==0){
                            level.setBlockAndUpdate(posBlock.above(), Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, true));
                        }else{
                            level.setBlockAndUpdate(posBlock.above(), Blocks.SCULK_SENSOR.defaultBlockState());
                        }
                    }
                }
            }
        }

        return storedData;
    }
}

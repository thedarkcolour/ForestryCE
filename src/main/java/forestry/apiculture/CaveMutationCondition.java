package forestry.apiculture;

import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.IMutationCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class CaveMutationCondition implements IMutationCondition {
    @Override
    public float modifyChance(Level level, BlockPos pos, IMutation<?> mutation, IGenome firstGenome, IGenome secondGenome, IClimateProvider climate, float currentChance) {
        for(Direction direction:Direction.VALUES){
            if(level.getBrightness(LightLayer.SKY, pos.relative(direction))>0)
                return 0;
        }
        return currentChance;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("for.mutation.condition.underground");
    }
}

package forestry.apiculture.genetics.effects;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class PhasingBeeEffect extends ThrottledBeeEffect{
    Random random=new Random();
    public PhasingBeeEffect() {
        super(true, 40, true, true);
    }

    @Override
    IEffectData doEffectThrottled(IGenome genome, IEffectData storedData, IBeeHousing housing) {
        List<LivingEntity> list=getEntitiesInRange(genome, housing, LivingEntity.class);
        for(LivingEntity entity:list){
            double d0 = entity.getX();
            double d1 = entity.getY();
            double d2 = entity.getZ();
            for(int i=0;i<16;i++){
                double d3=entity.getX()+(random.nextDouble()-0.5D)*16D;
                double d4= Mth.clamp(entity.getY() + (double)(random.nextInt(16) - 8), (double)housing.getWorldObj().getMinBuildHeight(), (double)(housing.getWorldObj().getMinBuildHeight() + ((ServerLevel)housing.getWorldObj()).getLogicalHeight() - 1));
                double d5=entity.getZ()+(random.nextDouble()-0.5D)*16D;
                if(entity.isPassenger())
                    entity.stopRiding();
                Vec3 vec3 = entity.position();
                housing.getWorldObj().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(entity, d3, d4, d5);
                if (event.isCanceled()) break;
                if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = entity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    housing.getWorldObj().playSound(null, d0, d1, d2, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    entity.playSound(soundevent, 1.0F, 1.0F);
                    break;
                }
            }
        }
        return storedData;
    }

    @Override
    public IEffectData doFX(IGenome genome, IEffectData storedData, IBeeHousing housing) {
        housing.getWorldObj().addParticle(ParticleTypes.PORTAL, housing.getCoordinates().getX(), housing.getCoordinates().getY() + random.nextDouble() * 2.0D, housing.getCoordinates().getZ(), random.nextGaussian(), 0.0D, random.nextGaussian());
        return storedData;
    }
}

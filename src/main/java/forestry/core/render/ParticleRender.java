package forestry.core.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.IForestryApi;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.hives.IHiveTile;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.effects.ThrottledBeeEffect;
import forestry.apiculture.particles.ApicultureParticles;
import forestry.apiculture.particles.BeeParticleData;
import forestry.apiculture.particles.BeeTargetParticleData;
import forestry.apiculture.particles.ParticleSnow;
import forestry.core.config.ForestryConfig;
import forestry.core.entities.ParticleIgnition;
import forestry.core.entities.ParticleSmoke;
import forestry.core.utils.VecUtil;

import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ParticleRender {
	private static final DustParticleOptions HONEY_DUST = new DustParticleOptions(new Vector3f(0.9F, 0.75F, 0.0F), 1.0F);

	public static boolean shouldSpawnParticle(Level world) {
		if (!ForestryConfig.CLIENT.showParticles.get()) {
			return false;
		}

		Minecraft mc = Minecraft.getInstance();
		ParticleStatus particleSetting = mc.options.particles().get();

		if (particleSetting == ParticleStatus.MINIMAL) { // minimal
			return world.random.nextInt(10) == 0;
		} else if (particleSetting == ParticleStatus.DECREASED) { // decreased
			return world.random.nextInt(3) != 0;
		} else { // all
			return true;
		}
	}

	public static void addBeeHiveFX(IBeeHousing housing, IGenome genome, List<BlockPos> flowerPositions) {
		LevelAccessor world1 = housing.getWorldObj();
		ClientLevel world = (ClientLevel) world1;
		if (!shouldSpawnParticle(world)) {
			return;
		}

		Vec3 particleStart = housing.getBeeFXCoordinates();

		// Avoid rendering bee particles that are too far away, they're very small.
		// At 32+ distance, have no bee particles. Make more particles up close.
		BlockPos playerPosition = Minecraft.getInstance().player.blockPosition();
		//TODO - correct?
		double playerDistanceSq = playerPosition.distSqr(BlockPos.containing(particleStart.x, particleStart.y, particleStart.z));
		if (world.random.nextInt(1024) < playerDistanceSq) {
			return;
		}

		int color = genome.getActiveValue(BeeChromosomes.SPECIES).getOutline();

		int randomInt = world.random.nextInt(100);

		if (housing instanceof IHiveTile) {
			if (((IHiveTile) housing).isAngry() || randomInt >= 85) {
				List<LivingEntity> entitiesInRange = ThrottledBeeEffect.getEntitiesInRange(genome, housing, LivingEntity.class);
				if (!entitiesInRange.isEmpty()) {
					LivingEntity entity = entitiesInRange.get(world.random.nextInt(entitiesInRange.size()));
					//Particle particle = new ParticleBeeTargetEntity(world, particleStart, entity, color);
					//effectRenderer.add(particle);
					world.addParticle(new BeeTargetParticleData(entity, color), particleStart.x, particleStart.y, particleStart.z, 0, 0, 0);
					return;
				}
			}
		}

		if (randomInt < 75 && !flowerPositions.isEmpty()) {
			BlockPos destination = flowerPositions.get(world.random.nextInt(flowerPositions.size()));
			//Particle particle = new ParticleBeeRoundTrip(world, particleStart, destination, color);
			//effectRenderer.add(particle);
			world.addParticle(new BeeParticleData(ApicultureParticles.BEE_ROUND_TRIP_PARTICLE.get(), destination, color), particleStart.x, particleStart.y, particleStart.z, 0, 0, 0);
		} else {
			Vec3i area = getModifiedArea(genome, housing);
			Vec3i offset = housing.getCoordinates().offset(-area.getX() / 2, -area.getY() / 4, -area.getZ() / 2);
			BlockPos destination = VecUtil.getRandomPositionInArea(world.random, area).offset(offset);
			world.addParticle(new BeeParticleData(ApicultureParticles.BEE_EXPLORER_PARTICLE.get(), destination, color), particleStart.x, particleStart.y, particleStart.z, 0, 0, 0);
			//Particle particle = new ParticleBeeExplore(world, particleStart, destination, color);
			//effectRenderer.add(particle);
		}
	}

	public static void addEntityHoneyDustFX(Level world, double x, double y, double z) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		world.addParticle(HONEY_DUST, x, y, z, 0, 0, 0);
		//		effectRenderer.addEffect(new ParticleHoneydust(world, x, y, z, 0, 0, 0));
	}

	public static void addEntityExplodeFX(Level world, double x, double y, double z) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		//TODO particle data
		Particle Particle = effectRenderer.createParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
		effectRenderer.add(Particle);
	}

	public static void addEntitySnowFX(Level world, double x, double y, double z) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		effectRenderer.add(new ParticleSnow((ClientLevel) world, x + world.random.nextGaussian(), y, z + world.random.nextGaussian()));
	}

	public static void addEntityIgnitionFX(ClientLevel world, double x, double y, double z) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		effectRenderer.add(new ParticleIgnition(world, x, y, z));
	}

	public static void addEntitySmokeFX(Level world, double x, double y, double z) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		effectRenderer.add(new ParticleSmoke((ClientLevel) world, x, y, z));
	}

	public static void addEntityPotionFX(Level world, double x, double y, double z, int color) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;

		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		//TODO - maybe EFFECT?
		//TODO particle data
		Particle particle = effectRenderer.createParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
		if (particle != null) {
			particle.setColor(red, green, blue);
			effectRenderer.add(particle);
		}
	}

	public static void addPortalFx(Level world, BlockPos pos, RandomSource rand) {
		if (!shouldSpawnParticle(world)) {
			return;
		}

		int j = rand.nextInt(2) * 2 - 1;
		int k = rand.nextInt(2) * 2 - 1;
		double xPos = (double) pos.getX() + 0.5D + 0.25D * (double) j;
		double yPos = (float) pos.getY() + rand.nextFloat();
		double zPos = (double) pos.getZ() + 0.5D + 0.25D * (double) k;
		double xSpeed = rand.nextFloat() * (float) j;
		double ySpeed = ((double) rand.nextFloat() - 0.5D) * 0.125D;
		double zSpeed = rand.nextFloat() * (float) k;
		ParticleEngine effectRenderer = Minecraft.getInstance().particleEngine;
		//TODO particle data
		Particle particle = effectRenderer.createParticle(DustParticleOptions.REDSTONE, xPos, yPos, zPos, xSpeed, ySpeed, zSpeed);
		if (particle != null) {
			effectRenderer.add(particle);
		}
	}

	public static Vec3i getModifiedArea(IGenome genome, IBeeHousing housing) {
		Vec3i area = Bee.getAdjustedTerritory(genome, IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing));
		int x = area.getX();
		int y = area.getY();
		int z = area.getZ();

		if (x < 1) {
			x = 1;
		}
		if (y < 1) {
			y = 1;
		}
		if (z < 1) {
			z = 1;
		}

		return new Vec3i(x, y, z);
	}
}

package forestry.compat.kubejs.apiculture;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveGen;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;

// I was told that KubeJS cannot make classes that implement an interface with more than one method. So...
public record KubeHiveDefinition(IHiveGen placement, BlockState hiveState, Predicate<Holder<Biome>> isGoodBiome,
								 Predicate<HumidityType> isGoodHumidity, Predicate<TemperatureType> isGoodTemperature,
								 float genChance, PostGenFunction postGen) implements IHiveDefinition {
	@Override
	public IHiveGen getHiveGen() {
		return this.placement;
	}

	@Override
	public BlockState getBlockState() {
		return this.hiveState;
	}

	@Override
	public boolean isGoodBiome(Holder<Biome> biome) {
		return this.isGoodBiome.test(biome);
	}

	@Override
	public boolean isGoodHumidity(HumidityType humidity) {
		return this.isGoodHumidity.test(humidity);
	}

	@Override
	public boolean isGoodTemperature(TemperatureType temperature) {
		return this.isGoodTemperature.test(temperature);
	}

	@Override
	public float getGenChance() {
		return this.genChance;
	}

	@Override
	public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
		this.postGen.postGen(level, rand, pos);
	}

	public interface PostGenFunction {
		void postGen(WorldGenLevel world, RandomSource rand, BlockPos pos);
	}
}

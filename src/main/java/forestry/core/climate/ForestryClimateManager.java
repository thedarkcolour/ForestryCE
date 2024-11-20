package forestry.core.climate;

import java.util.IdentityHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

import forestry.api.climate.ClimateState;
import forestry.api.climate.IClimateManager;
import forestry.api.climate.IClimateProvider;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;

import org.jetbrains.annotations.ApiStatus;

public class ForestryClimateManager implements IClimateManager {
	private final IdentityHashMap<ResourceKey<Biome>, TemperatureType> temperatures = new IdentityHashMap<>();
	private final IdentityHashMap<ResourceKey<Biome>, HumidityType> humidities = new IdentityHashMap<>();

	@Override
	public TemperatureType getTemperature(Holder<Biome> biome) {
		// avoid Optional creation if possible
		return getTemperature(biome instanceof Holder.Reference<Biome> reference ? reference.key() : biome.unwrapKey().get());
	}

	@Override
	public TemperatureType getTemperature(ResourceKey<Biome> biome) {
		return this.temperatures.get(biome);
	}

	@Override
	public HumidityType getHumidity(Holder<Biome> biome) {
		// avoid Optional creation if possible
		return getHumidity(biome instanceof Holder.Reference<Biome> reference ? reference.key() : biome.unwrapKey().get());
	}

	@Override
	public HumidityType getHumidity(ResourceKey<Biome> biome) {
		return this.humidities.get(biome);
	}

	@Override
	public IClimateProvider createClimateProvider(LevelReader world, BlockPos pos) {
		return new ClimateProvider(world, pos);
	}

	@Override
	public IClimateProvider createDummyClimateProvider() {
		return FakeClimateProvider.INSTANCE;
	}

	@Override
	public ClimateState getState(ServerLevel level, BlockPos pos) {
		// todo implement climate modifiers
		return getBiomeState(level, pos);
	}

	@Override
	public ClimateState getBiomeState(LevelReader level, BlockPos coordinates) {
		Holder<Biome> biome = level.getBiome(coordinates);
		return new ClimateState(getTemperature(biome), getHumidity(biome));
	}

	@ApiStatus.Internal
	public void onBiomesReloaded(Registry<Biome> registry) {
		this.temperatures.clear();
		this.humidities.clear();

		// check biome tags. if no temperature/humidity tags are found, then calculate based on temperature/downfall
		registry.holders().forEach(holder -> {
			boolean hasTemperatureTag = false;
			boolean hasHumidityTag = false;

			for (TemperatureType temperature : TemperatureType.VALUES) {
				if (holder.is(temperature.tag)) {
					this.temperatures.put(holder.key(), temperature);
					hasTemperatureTag = true;
					break;
				}
			}
			for (HumidityType humidity : HumidityType.VALUES) {
				if (holder.is(humidity.tag)) {
					this.humidities.put(holder.key(), humidity);
					hasHumidityTag = true;
					break;
				}
			}
			if (!hasTemperatureTag) {
				this.temperatures.put(holder.key(), TemperatureType.getFromValue(holder.value().getBaseTemperature()));
			}
			if (!hasHumidityTag) {
				this.humidities.put(holder.key(), HumidityType.getFromValue(holder.value().getModifiedClimateSettings().downfall()));
			}
		});
	}
}

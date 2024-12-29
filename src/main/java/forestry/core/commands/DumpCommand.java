package forestry.core.commands;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import forestry.Forestry;
import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.climate.IClimateManager;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.IGeneticManager;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.IChromosome;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.features.FeatureItem;
import forestry.storage.features.BackpackItems;
import forestry.storage.items.ItemBackpack;

// Used to dump currently registered Forestry data
public class DumpCommand {
	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("dump")
				.then(Commands.literal("mutations").executes(DumpCommand::mutations))
				.then(Commands.literal("climates").executes(DumpCommand::climates))
				.then(Commands.literal("backpacks").executes(DumpCommand::backpacks))
				.then(Commands.literal("bee_species").executes(DumpCommand::beeSpecies))
				.then(Commands.literal("chromosomes").executes(DumpCommand::chromosomes));
	}

	// Dumps all registered mutations
	private static int mutations(CommandContext<CommandSourceStack> ctx) {
		IGeneticManager genetics = IForestryApi.INSTANCE.getGeneticManager();

		for (ISpeciesType<?, ?> type : genetics.getSpeciesTypes()) {
			List<? extends IMutation<?>> mutations = type.getMutations().getAllMutations();
			Forestry.LOGGER.debug("There are {} registered mutations for species type {}", mutations.size(), type.id());

			for (IMutation<?> mutation : mutations) {
				Forestry.LOGGER.debug("> {} + {} = {}", mutation.getFirstParent().id(), mutation.getSecondParent().id(), mutation.getResult().id());
			}
			Forestry.LOGGER.debug("=======");
		}
		return 1;
	}

	private static int climates(CommandContext<CommandSourceStack> ctx) {
		List<Holder.Reference<Biome>> biomes = ctx.getSource().getLevel().registryAccess().registryOrThrow(Registries.BIOME).holders()
				.toList();
		Forestry.LOGGER.debug("Listing Forestry climates for {} biomes", biomes.size());

		Table<HumidityType, TemperatureType, List<ResourceLocation>> climates = createClimatesTable(ctx);

		for (TemperatureType temperature : TemperatureType.VALUES) {
			for (HumidityType humidity : HumidityType.VALUES) {
				List<ResourceLocation> climateList = climates.get(humidity, temperature);

				if (climateList == null || climateList.isEmpty()) {
					Forestry.LOGGER.error("No biomes found with climate ({} / {})", temperature, humidity);
				} else {
					Forestry.LOGGER.debug("Found {} biomes with climate ({} / {}): ", climateList.size(), temperature, humidity);

					for (ResourceLocation biomeId : climateList) {
						Forestry.LOGGER.debug("  > {}", biomeId);
					}
				}
			}
		}
		Forestry.LOGGER.debug("Finished listing Forestry climates.");

		return 1;
	}

	private static Table<HumidityType, TemperatureType, List<ResourceLocation>> createClimatesTable(CommandContext<CommandSourceStack> ctx) {
		List<Holder.Reference<Biome>> biomes = ctx.getSource().getLevel().registryAccess().registryOrThrow(Registries.BIOME).holders().toList();
		Table<HumidityType, TemperatureType, List<ResourceLocation>> climates = HashBasedTable.create(HumidityType.VALUES.size(), TemperatureType.VALUES.size());
		IClimateManager manager = IForestryApi.INSTANCE.getClimateManager();

		for (Holder.Reference<Biome> biome : biomes) {
			HumidityType humidity = manager.getHumidity(biome);
			TemperatureType temperature = manager.getTemperature(biome);

			List<ResourceLocation> climateList = climates.get(humidity, temperature);
			if (climateList == null) {
				climates.put(humidity, temperature, climateList = new ArrayList<>());
			}
			climateList.add(biome.key().location());
		}
		return climates;
	}

	@SuppressWarnings("deprecation")
	private static int backpacks(CommandContext<CommandSourceStack> ctx) {
		// is there a reason not to hardcode this?
		@SuppressWarnings({"unchecked", "rawtypes"})
		List<FeatureItem<ItemBackpack>> definitions = (List) List.of(
				BackpackItems.MINER_BACKPACK,
				BackpackItems.DIGGER_BACKPACK,
				BackpackItems.FORESTER_BACKPACK,
				BackpackItems.HUNTER_BACKPACK,
				BackpackItems.ADVENTURER_BACKPACK,
				BackpackItems.BUILDER_BACKPACK
		);

		Forestry.LOGGER.debug("Listing Forestry backpack filters for {} backpack types (excluding Naturalist bags)", definitions.size());

		for (FeatureItem<ItemBackpack> backpack : definitions) {
			Predicate<ItemStack> filter = backpack.get().getDefinition().getFilter();
			ArrayList<Item> allowedItems = new ArrayList<>();

			for (Item item : ctx.getSource().registryAccess().registryOrThrow(Registries.ITEM)) {
				if (filter.test(new ItemStack(item))) {
					allowedItems.add(item);
				}
			}

			Forestry.LOGGER.debug("The backpack definition {} allows {} items:", backpack.item().builtInRegistryHolder().key().location(), allowedItems.size());

			for (Item allowedItem : allowedItems) {
				Forestry.LOGGER.debug("  > {}", allowedItem.builtInRegistryHolder().key().location());
			}
		}

		return 1;
	}

	private static int beeSpecies(CommandContext<CommandSourceStack> ctx) {
		IBeeSpeciesType beeType = SpeciesUtil.BEE_TYPE.get();
		List<IBeeSpecies> allSpecies = beeType.getAllSpecies();
		Table<HumidityType, TemperatureType, List<ResourceLocation>> climatesTable = createClimatesTable(ctx);

		try {
			Forestry.LOGGER.debug("Listing properties of all {} bee species", allSpecies.size());

			for (IBeeSpecies species : allSpecies) {
				IGenome defaultGenome = species.getDefaultGenome();

				TemperatureType temperature = species.getTemperature();
				HumidityType humidity = species.getHumidity();

				Forestry.LOGGER.debug("PROPERTIES OF BEE SPECIES {}:", species.id());
				Forestry.LOGGER.debug("  Primary Stats:");
				Forestry.LOGGER.debug("    Lifespan: {}", defaultGenome.getActiveName(BeeChromosomes.LIFESPAN).getString());
				Forestry.LOGGER.debug("    Production: {}", defaultGenome.getActiveName(BeeChromosomes.SPEED).getString());
				Forestry.LOGGER.debug("    Pollination: {}", defaultGenome.getActiveName(BeeChromosomes.POLLINATION).getString());
				Forestry.LOGGER.debug("    Fertility: {}", defaultGenome.getActiveName(BeeChromosomes.FERTILITY).getString());
				Forestry.LOGGER.debug("    Territory: {}", defaultGenome.getActiveName(BeeChromosomes.TERRITORY).getString());
				Forestry.LOGGER.debug("    Effect: {}", defaultGenome.getActiveName(BeeChromosomes.EFFECT).getString());
				Forestry.LOGGER.debug("  Secondary Stats:");
				Forestry.LOGGER.debug("    Temperature: {}, {}", temperature, defaultGenome.getActiveName(BeeChromosomes.TEMPERATURE_TOLERANCE).getString());
				Forestry.LOGGER.debug("    Humidity: {}, {}", humidity, defaultGenome.getActiveName(BeeChromosomes.HUMIDITY_TOLERANCE).getString());
				Forestry.LOGGER.debug("    Activity: {}", defaultGenome.getActiveName(BeeChromosomes.ACTIVITY));
				Forestry.LOGGER.debug("    Tolerant flyer: {}", defaultGenome.getActiveName(BeeChromosomes.TOLERATES_RAIN).getString());
				Forestry.LOGGER.debug("    Cave-dwelling: {}", defaultGenome.getActiveName(BeeChromosomes.CAVE_DWELLING).getString());
				Forestry.LOGGER.debug("    Flower Type: {}", defaultGenome.getActiveName(BeeChromosomes.FLOWER_TYPE).getString());

				List<ResourceLocation> suitableBiomes = new ArrayList<>();
				ToleranceType temperatureTolerance = defaultGenome.getActiveValue(BeeChromosomes.TEMPERATURE_TOLERANCE);
				ToleranceType humidityTolerance = defaultGenome.getActiveValue(BeeChromosomes.HUMIDITY_TOLERANCE);

				int maxTemperature = Math.min(temperature.ordinal() + temperatureTolerance.up, TemperatureType.VALUES.size() - 1);
				int maxHumidity = Math.min(humidity.ordinal() + humidityTolerance.up, HumidityType.VALUES.size() - 1);

				for (int t = temperature.down(temperatureTolerance.down).ordinal(); t <= maxTemperature; t++) {
					for (int h = humidity.down(humidityTolerance.down).ordinal(); h <= maxHumidity; h++) {
						List<ResourceLocation> validBiomes = climatesTable.get(HumidityType.VALUES.get(h), TemperatureType.VALUES.get(t));

						if (validBiomes != null) {
							suitableBiomes.addAll(validBiomes);
						}
					}
				}

				Forestry.LOGGER.debug("  Found {} Suitable Biomes:", suitableBiomes.size());

				for (ResourceLocation biome : suitableBiomes) {
					Forestry.LOGGER.debug("    {}", biome);
				}
			}

		} catch (Throwable throwable) {
			Forestry.LOGGER.debug("Uh oh", throwable);
		}

		return 1;
	}

	private static int chromosomes(CommandContext<CommandSourceStack> ctx) {
		for (ISpeciesType<?, ?> speciesType : IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypes()) {
			ImmutableList<IChromosome<?>> chromosomes = speciesType.getKaryotype().getChromosomes();
			Forestry.LOGGER.debug("ISpeciesType \"{}\" has {} chromosomes:", speciesType.id(), chromosomes.size());

			for (IChromosome<?> chromosome : chromosomes) {
				Forestry.LOGGER.debug("  {} - {} -> \"{}\"", chromosome.id(), chromosome.getChromosomeTranslationKey(), chromosome.getChromosomeDisplayName().getString());
			}
		}

		return 1;
	}
}

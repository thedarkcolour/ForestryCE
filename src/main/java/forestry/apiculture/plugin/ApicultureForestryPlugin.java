package forestry.apiculture.plugin;

import forestry.apiculture.client.plugin.ApicultureClientRegistration;
import forestry.api.client.plugin.IClientRegistration;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.ForestryActivityTypes;
import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.ForestryFlowerTypes;
import forestry.api.apiculture.LightPreference;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.core.ForestryError;
import forestry.api.core.genetics.ForestrySpeciesTypes;
import forestry.api.core.genetics.alleles.Allele;
import forestry.api.core.genetics.alleles.BeeChromosomes;
import forestry.api.core.genetics.alleles.ForestryAlleles;
import forestry.api.modules.ForestryModuleIds;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IGeneticRegistration;
import forestry.apiculture.bees.ApicultureFilterRule;
import forestry.apiculture.bees.ApicultureFilterRuleType;
import forestry.apiculture.bees.CathemeralActivityType;
import forestry.apiculture.bees.CrepuscularActivityType;
import forestry.apiculture.bees.SingleActivityType;
import forestry.apiculture.features.ApicultureEffects;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.bees.genetics.BeeSpeciesType;
import forestry.apiculture.bees.genetics.DefaultBeeJubilance;
import forestry.apiculture.bees.genetics.HermitBeeJubilance;
import forestry.apiculture.bees.genetics.effects.AscensionBeeEffect;
import forestry.apiculture.bees.genetics.effects.CreeperBeeEffect;
import forestry.apiculture.bees.genetics.effects.DummyBeeEffect;
import forestry.apiculture.bees.genetics.effects.ExplorationBeeEffect;
import forestry.apiculture.bees.genetics.effects.FertileBeeEffect;
import forestry.apiculture.bees.genetics.effects.FungificationBeeEffect;
import forestry.apiculture.bees.genetics.effects.GuardianBeeEffect;
import forestry.apiculture.bees.genetics.effects.IgnitionBeeEffect;
import forestry.apiculture.bees.genetics.effects.PhasingBeeEffect;
import forestry.apiculture.bees.genetics.effects.PotionBeeEffectExclusive;
import forestry.apiculture.bees.genetics.effects.RadioactiveBeeEffect;
import forestry.apiculture.bees.genetics.effects.RepulsionBeeEffect;
import forestry.apiculture.bees.genetics.effects.SculkSpreadBeeEffect;
import forestry.apiculture.bees.genetics.effects.SnowingBeeEffect;
import forestry.apiculture.hives.HiveDefinition;
import forestry.apiculture.bees.EnumHoneyComb;

/**
 * Base Forestry's apiculture registrations. Split out of {@code forestry.core.plugin.DefaultForestryPlugin}
 * so the base artifact does not register bee content.
 */
public class ApicultureForestryPlugin implements IForestryPlugin {
	@Override
	public void registerGenetics(IGeneticRegistration genetics) {
		// Bee type
		genetics.registerSpeciesType(ForestrySpeciesTypes.BEE, BeeSpeciesType::new)
			.setKaryotype(karyotype -> {
				karyotype.setSpecies(BeeChromosomes.SPECIES, ForestryBeeSpecies.FOREST);
				karyotype.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
				karyotype.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER);
				karyotype.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_2);
				karyotype.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
					.setWeaklyInherited(true);
				karyotype.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
					.setWeaklyInherited(true);
				karyotype.set(BeeChromosomes.ACTIVITY, ForestryActivityTypes.DIURNAL)
					.setWeaklyInherited(true);
				karyotype.set(BeeChromosomes.CAVE_DWELLING, false)
					.setWeaklyInherited(true);
				karyotype.set(BeeChromosomes.TOLERATES_RAIN, false)
					.setWeaklyInherited(true);
				karyotype.set(BeeChromosomes.FLOWER_TYPE, ForestryFlowerTypes.VANILLA);
				karyotype.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_AVERAGE);
				karyotype.set(BeeChromosomes.EFFECT, ForestryBeeEffects.NONE);
				karyotype.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST);
			})
			.addStages(BeeLifeStage.DRONE, BeeLifeStage.PRINCESS, BeeLifeStage.QUEEN, BeeLifeStage.LARVAE)
			.setDefaultStage(BeeLifeStage.DRONE);


		genetics.registerFilterRuleTypes(ApicultureFilterRuleType.values());
		ApicultureFilterRule.init();
	}

	@Override
	public void registerApiculture(IApicultureRegistration apiculture) {
		// Bee species themselves are no longer registered here; they are generated as datapack JSON
		// by BeeSpeciesProvider (which calls DefaultBeeSpecies) and loaded at runtime by the
		// datapack reload listener (BeeSpeciesManager). DefaultBeeSpecies stays in the source tree
		// as datagen input only.

		// Default hives
		Supplier<List<ItemStack>> honeyComb = getHoneyComb(EnumHoneyComb.HONEY);
		Supplier<List<ItemStack>> parchedComb = getHoneyComb(EnumHoneyComb.PARCHED);
		Supplier<List<ItemStack>> silkyComb = getHoneyComb(EnumHoneyComb.SILKY);
		Supplier<List<ItemStack>> mysteriousComb = getHoneyComb(EnumHoneyComb.MYSTERIOUS);
		Supplier<List<ItemStack>> frozenComb = getHoneyComb(EnumHoneyComb.FROZEN);
		Supplier<List<ItemStack>> mossyComb = getHoneyComb(EnumHoneyComb.MOSSY);
		Supplier<List<ItemStack>> spongeComb = getHoneyComb(EnumHoneyComb.SPONGY);
		Supplier<List<ItemStack>> simmerComb = getHoneyComb(EnumHoneyComb.SIMMERING);

		apiculture.registerHive(ForestryBeeSpecies.FOREST, HiveDefinition.FOREST)
			.setGenerationChance(HiveDefinition.FOREST.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.FOREST, honeyComb, 0.7f)
			.addDrop(0.08, ForestryBeeSpecies.FOREST, honeyComb, 0.0f, Map.of(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE))
			.addDrop(0.08, ForestryBeeSpecies.VALIANT, honeyComb);

		apiculture.registerHive(ForestryBeeSpecies.MEADOWS, HiveDefinition.MEADOWS)
			.setGenerationChance(HiveDefinition.MEADOWS.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.MEADOWS, honeyComb, 0.7f)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, honeyComb);

		apiculture.registerHive(ForestryBeeSpecies.MODEST, HiveDefinition.DESERT)
			.setGenerationChance(HiveDefinition.DESERT.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.MODEST, parchedComb, 0.7f)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, parchedComb);

		apiculture.registerHive(ForestryBeeSpecies.TROPICAL, HiveDefinition.JUNGLE)
			.setGenerationChance(HiveDefinition.JUNGLE.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.TROPICAL, silkyComb, 0.7f)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, silkyComb);

		apiculture.registerHive(ForestryBeeSpecies.ENDED, HiveDefinition.END)
			.setGenerationChance(HiveDefinition.END.defaultGenChance())
			.addDrop(0.90, ForestryBeeSpecies.ENDED, mysteriousComb, 0.7f)
			.addDrop(0.09, ForestryBeeSpecies.ENDED, mysteriousComb, 0.7f, Map.of(BeeChromosomes.EFFECT, Allele.reference(ForestryBeeEffects.PHASING)))
			.addDrop(0.03, ForestryBeeSpecies.ENDED, mysteriousComb, 0.7f, Map.of(BeeChromosomes.EFFECT, Allele.reference(ForestryBeeEffects.ASCENSION)));

		apiculture.registerHive(ForestryBeeSpecies.WINTRY, HiveDefinition.SNOW)
			.setGenerationChance(HiveDefinition.SNOW.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.WINTRY, frozenComb, 0.5f)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, frozenComb);

		apiculture.registerHive(ForestryBeeSpecies.MARSHY, HiveDefinition.SWAMP)
			.setGenerationChance(HiveDefinition.SWAMP.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.MARSHY, mossyComb, 0.7f)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, mossyComb);

		apiculture.registerHive(ForestryBeeSpecies.SAVANNA, HiveDefinition.SAVANNA)
			.setGenerationChance(HiveDefinition.SAVANNA.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.SAVANNA, parchedComb, 0.7f)
			.addDrop(0.35, ForestryBeeSpecies.SAVANNA, parchedComb, 0.7f, Map.of(BeeChromosomes.EFFECT, Allele.reference(ForestryBeeEffects.AGGRESSIVE)))
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, parchedComb);

		apiculture.registerHive(ForestryBeeSpecies.LUSH, HiveDefinition.LUSH)
			.setGenerationChance(HiveDefinition.LUSH.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.LUSH, honeyComb, 0.5F)
			.addDrop(0.08, ForestryBeeSpecies.VALIANT, honeyComb);

		apiculture.registerHive(ForestryBeeSpecies.AQUATIC, HiveDefinition.AQUATIC)
			.setGenerationChance(HiveDefinition.AQUATIC.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.AQUATIC, spongeComb, 0.4F)
			.addDrop(0.03, ForestryBeeSpecies.VALIANT, spongeComb);

		apiculture.registerHive(ForestryBeeSpecies.EMBITTERED, HiveDefinition.NETHER)
			.setGenerationChance(HiveDefinition.NETHER.defaultGenChance())
			.addDrop(0.80, ForestryBeeSpecies.EMBITTERED, simmerComb, 0.7F);

		// Common village bees
		apiculture.addVillageBee(ForestryBeeSpecies.FOREST, false);
		apiculture.addVillageBee(ForestryBeeSpecies.MEADOWS, false);
		apiculture.addVillageBee(ForestryBeeSpecies.MODEST, false);
		apiculture.addVillageBee(ForestryBeeSpecies.MARSHY, false);
		apiculture.addVillageBee(ForestryBeeSpecies.WINTRY, false);
		apiculture.addVillageBee(ForestryBeeSpecies.TROPICAL, false);
		apiculture.addVillageBee(ForestryBeeSpecies.SAVANNA, false);

		// Rare village bees
		apiculture.addVillageBee(ForestryBeeSpecies.FOREST, true, Map.of(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE));
		apiculture.addVillageBee(ForestryBeeSpecies.COMMON, true, Map.of(
			BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1,
			BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1
		));
		apiculture.addVillageBee(ForestryBeeSpecies.VALIANT, true);

		apiculture.registerBeeEffect(ForestryBeeEffects.NONE, new DummyBeeEffect(true));
		apiculture.registerBeeEffect(ForestryBeeEffects.RADIOACTIVE, new RadioactiveBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.CREEPER, new CreeperBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.IGNITION, new IgnitionBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.EXPLORATION, new ExplorationBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.EASTER, new DummyBeeEffect(true));
		apiculture.registerBeeEffect(ForestryBeeEffects.SNOWING, new SnowingBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.REPULSION, new RepulsionBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.FERTILE, new FertileBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.MYCOPHILIC, new FungificationBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.HAKUNA_MATATA, new PotionBeeEffectExclusive(false, ApicultureEffects.HAKUNA_MATATA, 20 * 60 * 3, 100, 1.0f, ApicultureEffects.MATATA));
		apiculture.registerBeeEffect(ForestryBeeEffects.GUARDIAN, new GuardianBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.PHASING, new PhasingBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.ASCENSION, new AscensionBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.SCULK, new SculkSpreadBeeEffect());

		apiculture.registerBeeJubilance(ForestryBeeJubilances.DEFAULT, DefaultBeeJubilance.INSTANCE);
		apiculture.registerBeeJubilance(ForestryBeeJubilances.HERMIT, HermitBeeJubilance.INSTANCE);

		apiculture.registerActivityType(ForestryActivityTypes.DIURNAL, new SingleActivityType(0, 12000, ForestryError.NOT_DAY, LightPreference.ANY));
		apiculture.registerActivityType(ForestryActivityTypes.NOCTURNAL, new SingleActivityType(12000, 24000, ForestryError.NOT_NIGHT, LightPreference.DARK));
		apiculture.registerActivityType(ForestryActivityTypes.METATURNAL, new SingleActivityType(0, 24000, ForestryError.INVALID, LightPreference.ANY));
		apiculture.registerActivityType(ForestryActivityTypes.CREPUSCULAR, new CrepuscularActivityType());
		apiculture.registerActivityType(ForestryActivityTypes.CATHEMERAL, new CathemeralActivityType());
	}

	private static Supplier<List<ItemStack>> getHoneyComb(EnumHoneyComb type) {
		return () -> List.of(ApicultureItems.BEE_COMBS.stack(type));
	}

	@Override
	public ResourceLocation id() {
		return ForestryModuleIds.APICULTURE;
	}

	@Override
	public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
		registrar.accept(new ApicultureClientRegistration());
	}
}

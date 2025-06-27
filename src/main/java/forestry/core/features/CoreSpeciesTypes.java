package forestry.core.features;

import forestry.api.ForestryRegistries;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.bee.BeeLifeStage;
import forestry.api.apiculture.bee.IBeeSpeciesType;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterflySpeciesType;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.genetics.BeeSpeciesType;
import forestry.apiimpl.plugin.SpeciesTypeBuilder;
import forestry.arboriculture.genetics.TreeSpeciesType;
import forestry.lepidopterology.genetics.ButterflySpeciesType;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@FeatureProvider
public class CoreSpeciesTypes {
	private static final DeferredRegister<ISpeciesType<?, ?>> REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE).getRegistry(ForestryRegistries.Keys.SPECIES_TYPE);

	public static final DeferredHolder<ISpeciesType<?, ?>, IBeeSpeciesType> BEE = REGISTRY.register("bee", CoreSpeciesTypes::bee);
	public static final DeferredHolder<ISpeciesType<?, ?>, ITreeSpeciesType> TREE = REGISTRY.register("tree", CoreSpeciesTypes::tree);
	public static final DeferredHolder<ISpeciesType<?, ?>, IButterflySpeciesType> BUTTERFLY = REGISTRY.register("butterfly", CoreSpeciesTypes::butterfly);

	private static BeeSpeciesType bee() {
		SpeciesTypeBuilder<BeeSpeciesType> builder = new SpeciesTypeBuilder<>(BeeSpeciesType::new);

		builder.setKaryotype(karyotype -> {
			karyotype.setSpecies(BeeChromosomes.SPECIES, ForestryBeeSpecies.FOREST);
			karyotype.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST)
				.addAlleles(ForestryAlleles.DEFAULT_SPEEDS);
			karyotype.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER)
				.addAlleles(ForestryAlleles.DEFAULT_LIFESPANS);
			karyotype.set(BeeChromosomes.FERTILITY, ForestryAlleles.FERTILITY_2)
				.addAlleles(ForestryAlleles.DEFAULT_BEE_FERTILITIES);
			karyotype.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
				.addAlleles(ForestryAlleles.DEFAULT_TEMPERATURE_TOLERANCES)
				.setWeaklyInherited(true);
			karyotype.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
				.addAlleles(ForestryAlleles.DEFAULT_HUMIDITY_TOLERANCES)
				.setWeaklyInherited(true);
			karyotype.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_DIURNAL)
				.setWeaklyInherited(true);
			karyotype.set(BeeChromosomes.CAVE_DWELLING, false)
				.setWeaklyInherited(true);
			karyotype.set(BeeChromosomes.TOLERATES_RAIN, false)
				.setWeaklyInherited(true);
			karyotype.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_VANILLA);
			karyotype.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_AVERAGE)
				.addAlleles(ForestryAlleles.DEFAULT_TERRITORIES);
			karyotype.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_NONE);
			karyotype.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWEST)
				.addAlleles(ForestryAlleles.DEFAULT_POLLINATIONS);
		});

		builder.addStages(BeeLifeStage.DRONE, BeeLifeStage.PRINCESS, BeeLifeStage.QUEEN, BeeLifeStage.LARVAE);
		builder.setDefaultStage(BeeLifeStage.DRONE);

		return builder.build();
	}

	private static TreeSpeciesType tree() {
		SpeciesTypeBuilder<TreeSpeciesType> builder = new SpeciesTypeBuilder<>(TreeSpeciesType::new);

		builder.setKaryotype(karyotype -> {
			karyotype.setSpecies(TreeChromosomes.SPECIES, ForestryTreeSpecies.OAK);
			karyotype.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL)
				.addAlleles(ForestryAlleles.DEFAULT_HEIGHTS);
			karyotype.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER)
				.addAlleles(ForestryAlleles.DEFAULT_SAPLINGS);
			karyotype.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_NONE);
			karyotype.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOWEST)
				.addAlleles(ForestryAlleles.DEFAULT_YIELDS);
			karyotype.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWEST)
				.addAlleles(ForestryAlleles.DEFAULT_SAPPINESSES);
			karyotype.set(TreeChromosomes.EFFECT, ForestryAlleles.TREE_EFFECT_NONE);
			karyotype.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE)
				.addAlleles(ForestryAlleles.DEFAULT_MATURATIONS);
			karyotype.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_1)
				.addAlleles(ForestryAlleles.DEFAULT_GIRTHS);
			karyotype.set(TreeChromosomes.FIREPROOF, false);
		});
		builder.addStages(TreeLifeStage.SAPLING, TreeLifeStage.POLLEN);
		builder.setDefaultStage(TreeLifeStage.SAPLING);

		return builder.build();
	}

	private static ButterflySpeciesType butterfly() {
		SpeciesTypeBuilder<ButterflySpeciesType> builder = new SpeciesTypeBuilder<>(ButterflySpeciesType::new);

		builder.setKaryotype(karyotype -> {
			karyotype.setSpecies(ButterflyChromosomes.SPECIES, ForestryButterflySpecies.MONARCH);
			karyotype.set(ButterflyChromosomes.SIZE, ForestryAlleles.SIZE_SMALL)
				.addAlleles(ForestryAlleles.DEFAULT_SIZES);
			karyotype.set(ButterflyChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST)
				.addAlleles(ForestryAlleles.DEFAULT_SPEEDS);
			karyotype.set(ButterflyChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER)
				.addAlleles(ForestryAlleles.DEFAULT_LIFESPANS);
			karyotype.set(ButterflyChromosomes.METABOLISM, ForestryAlleles.METABOLISM_SLOWER)
				.addAlleles(ForestryAlleles.DEFAULT_METABOLISMS);
			karyotype.set(ButterflyChromosomes.FERTILITY, ForestryAlleles.FERTILITY_3)
				.addAlleles(ForestryAlleles.DEFAULT_BUTTERFLY_FERTILITIES);
			karyotype.set(ButterflyChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
				.addAlleles(ForestryAlleles.DEFAULT_TEMPERATURE_TOLERANCES)
				.setWeaklyInherited(true);
			karyotype.set(ButterflyChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE)
				.addAlleles(ForestryAlleles.DEFAULT_HUMIDITY_TOLERANCES)
				.setWeaklyInherited(true);
			karyotype.set(ButterflyChromosomes.NEVER_SLEEPS, false)
				.setWeaklyInherited(true);
			karyotype.set(ButterflyChromosomes.TOLERATES_RAIN, false)
				.setWeaklyInherited(true);
			karyotype.set(ButterflyChromosomes.FIREPROOF, false);
			karyotype.set(ButterflyChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_VANILLA);
			karyotype.set(ButterflyChromosomes.EFFECT, ForestryAlleles.BUTTERFLY_EFFECT_NONE);
			karyotype.set(ButterflyChromosomes.COCOON, ForestryAlleles.COCOON_DEFAULT);
		});
		builder.addStages(ButterflyLifeStage.BUTTERFLY, ButterflyLifeStage.SERUM, ButterflyLifeStage.CATERPILLAR, ButterflyLifeStage.COCOON);
		builder.setDefaultStage(ButterflyLifeStage.BUTTERFLY);
		builder.addResearchMaterials(map -> map.put(Items.GLASS_BOTTLE, 0.9f));

		return builder.build();
	}
}

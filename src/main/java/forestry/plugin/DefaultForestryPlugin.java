package forestry.plugin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import forestry.api.ForestryConstants;
import forestry.api.ForestryTags;
import forestry.api.apiculture.ForestryBeeEffects;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.ForestryFlowerTypes;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryFruits;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.circuits.ForestryCircuitLayouts;
import forestry.api.circuits.ForestryCircuitSocketTypes;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;
import forestry.api.core.Product;
import forestry.api.farming.ForestryFarmTypes;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ButterflyChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IArboricultureRegistration;
import forestry.api.plugin.ICircuitRegistration;
import forestry.api.plugin.IErrorRegistration;
import forestry.api.plugin.IFarmingRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IGeneticRegistration;
import forestry.api.plugin.ILepidopterologyRegistration;
import forestry.api.plugin.IPollenRegistration;
import forestry.apiculture.ApicultureFilterRule;
import forestry.apiculture.ApicultureFilterRuleType;
import forestry.apiculture.EndFlowerType;
import forestry.apiculture.FlowerType;
import forestry.apiculture.features.ApicultureEffects;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.genetics.BeeSpeciesType;
import forestry.apiculture.genetics.effects.AggressiveBeeEffect;
import forestry.apiculture.genetics.effects.CreeperBeeEffect;
import forestry.apiculture.genetics.effects.DummyBeeEffect;
import forestry.apiculture.genetics.effects.ExplorationBeeEffect;
import forestry.apiculture.genetics.effects.FertileBeeEffect;
import forestry.apiculture.genetics.effects.FungificationBeeEffect;
import forestry.apiculture.genetics.effects.GlacialBeeEffect;
import forestry.apiculture.genetics.effects.HeroicBeeEffect;
import forestry.apiculture.genetics.effects.IgnitionBeeEffect;
import forestry.apiculture.genetics.effects.MisanthropeBeeEffect;
import forestry.apiculture.genetics.effects.PotionBeeEffect;
import forestry.apiculture.genetics.effects.PotionBeeEffectExclusive;
import forestry.apiculture.genetics.effects.RadioactiveBeeEffect;
import forestry.apiculture.genetics.effects.RepulsionBeeEffect;
import forestry.apiculture.genetics.effects.ResurrectionBeeEffect;
import forestry.apiculture.genetics.effects.SifterBeeEffect;
import forestry.apiculture.genetics.effects.SnowingBeeEffect;
import forestry.apiculture.hives.HiveDefinition;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.arboriculture.ArboricultureFilterRuleType;
import forestry.arboriculture.DummyFruit;
import forestry.arboriculture.PodFruit;
import forestry.arboriculture.RipeningFruit;
import forestry.arboriculture.blocks.ForestryPodType;
import forestry.arboriculture.genetics.DummyTreeEffect;
import forestry.arboriculture.genetics.TreePollenType;
import forestry.arboriculture.genetics.TreeSpeciesType;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemFruit;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.factory.circuits.CircuitSpeedUpgrade;
import forestry.farming.circuits.CircuitFarmLogic;
import forestry.lepidopterology.DummyButterflyEffect;
import forestry.lepidopterology.LepidopterologyFilterRule;
import forestry.lepidopterology.LepidopterologyFilterRuleType;
import forestry.lepidopterology.genetics.ButterflySpeciesType;
import forestry.lepidopterology.genetics.DefaultCocoon;
import forestry.plugin.client.DefaultForestryClientRegistration;
import forestry.sorting.DefaultFilterRuleType;

public class DefaultForestryPlugin implements IForestryPlugin {
	public static final ResourceLocation ID = ForestryConstants.forestry("default");

	@Override
	public void registerGenetics(IGeneticRegistration genetics) {
		// Bee type
		genetics.registerSpeciesType(ForestrySpeciesTypes.BEE, BeeSpeciesType::new)
				.setKaryotype(karyotype -> {
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
					karyotype.set(BeeChromosomes.NEVER_SLEEPS, false)
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
				})
				.addStages(BeeLifeStage.DRONE, BeeLifeStage.PRINCESS, BeeLifeStage.QUEEN, BeeLifeStage.LARVAE)
				.setDefaultStage(BeeLifeStage.DRONE);

		// Tree type
		genetics.registerSpeciesType(ForestrySpeciesTypes.TREE, TreeSpeciesType::new)
				.setKaryotype(karyotype -> {
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
				})
				.addStages(TreeLifeStage.SAPLING, TreeLifeStage.POLLEN)
				.setDefaultStage(TreeLifeStage.SAPLING);

		// Butterfly type
		genetics.registerSpeciesType(ForestrySpeciesTypes.BUTTERFLY, ButterflySpeciesType::new)
				.setKaryotype(karyotype -> {
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
				})
				.addStages(ButterflyLifeStage.BUTTERFLY, ButterflyLifeStage.SERUM, ButterflyLifeStage.CATERPILLAR, ButterflyLifeStage.COCOON)
				.setDefaultStage(ButterflyLifeStage.BUTTERFLY)
				.addResearchMaterials(map -> map.put(Items.GLASS_BOTTLE, 0.9f));

		// Taxonomy
		BeeTaxonomy.defineTaxa(genetics);
		TreeTaxonomy.defineTaxa(genetics);
		ButterflyTaxonomy.defineTaxa(genetics);

		// Filter rules for the Genetic Filter
		genetics.registerFilterRuleTypes(DefaultFilterRuleType.values());
		genetics.registerFilterRuleTypes(ApicultureFilterRuleType.values());
		genetics.registerFilterRuleTypes(ArboricultureFilterRuleType.values());
		genetics.registerFilterRuleTypes(LepidopterologyFilterRuleType.values());
		LepidopterologyFilterRule.init();
		ApicultureFilterRule.init();
	}

	@Override
	public void registerApiculture(IApicultureRegistration apiculture) {
		DefaultBeeSpecies.register(apiculture);

		// Default hives
		Supplier<List<ItemStack>> honeyComb = getHoneyComb(EnumHoneyComb.HONEY);
		Supplier<List<ItemStack>> parchedComb = getHoneyComb(EnumHoneyComb.PARCHED);
		Supplier<List<ItemStack>> silkyComb = getHoneyComb(EnumHoneyComb.SILKY);
		Supplier<List<ItemStack>> mysteriousComb = getHoneyComb(EnumHoneyComb.MYSTERIOUS);
		Supplier<List<ItemStack>> frozenComb = getHoneyComb(EnumHoneyComb.FROZEN);
		Supplier<List<ItemStack>> mossyComb = getHoneyComb(EnumHoneyComb.MOSSY);

		apiculture.registerHive(ForestryBeeSpecies.FOREST, HiveDefinition.FOREST)
				.addDrop(0.80, ForestryBeeSpecies.FOREST, honeyComb, 0.7f)
				.addDrop(0.08, ForestryBeeSpecies.FOREST, honeyComb, 0.0f, Map.of(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE))
				.addDrop(0.08, ForestryBeeSpecies.VALIANT, honeyComb);

		apiculture.registerHive(ForestryBeeSpecies.MEADOWS, HiveDefinition.MEADOWS)
				.addDrop(0.80, ForestryBeeSpecies.MEADOWS, honeyComb, 0.7f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, honeyComb);

		apiculture.registerHive(ForestryBeeSpecies.MODEST, HiveDefinition.DESERT)
				.addDrop(0.80, ForestryBeeSpecies.MODEST, parchedComb, 0.7f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, parchedComb);

		apiculture.registerHive(ForestryBeeSpecies.TROPICAL, HiveDefinition.JUNGLE)
				.addDrop(0.80, ForestryBeeSpecies.TROPICAL, silkyComb, 0.7f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, silkyComb);

		apiculture.registerHive(ForestryBeeSpecies.ENDED, HiveDefinition.END)
				.addDrop(0.90, ForestryBeeSpecies.ENDED, mysteriousComb);

		apiculture.registerHive(ForestryBeeSpecies.WINTRY, HiveDefinition.SNOW)
				.addDrop(0.80, ForestryBeeSpecies.WINTRY, frozenComb, 0.5f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, frozenComb);

		apiculture.registerHive(ForestryBeeSpecies.MARSHY, HiveDefinition.SWAMP)
				.addDrop(0.80, ForestryBeeSpecies.MARSHY, mossyComb, 0.4f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, mossyComb);

		apiculture.registerHive(ForestryBeeSpecies.SAVANNA, HiveDefinition.SAVANNA)
				.addDrop(0.80, ForestryBeeSpecies.SAVANNA, parchedComb, 0.7f)
				.addDrop(0.03, ForestryBeeSpecies.VALIANT, parchedComb);


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
		apiculture.addVillageBee(ForestryBeeSpecies.COMMON, true);
		apiculture.addVillageBee(ForestryBeeSpecies.VALIANT, true);

		// Default flower types
		// todo plantable flower tags
		apiculture.registerFlowerType(ForestryFlowerTypes.VANILLA, new FlowerType(ForestryTags.Blocks.VANILLA_FLOWERS, true));
		apiculture.registerFlowerType(ForestryFlowerTypes.NETHER, new FlowerType(ForestryTags.Blocks.NETHER_FLOWERS, false));
		apiculture.registerFlowerType(ForestryFlowerTypes.CACTI, new FlowerType(ForestryTags.Blocks.CACTI_FLOWERS, false));
		apiculture.registerFlowerType(ForestryFlowerTypes.MUSHROOMS, new FlowerType(ForestryTags.Blocks.MUSHROOMS_FLOWERS, false));
		apiculture.registerFlowerType(ForestryFlowerTypes.END, new EndFlowerType(ForestryTags.Blocks.END_FLOWERS, false));
		apiculture.registerFlowerType(ForestryFlowerTypes.JUNGLE, new FlowerType(ForestryTags.Blocks.JUNGLE_FLOWERS, false));
		apiculture.registerFlowerType(ForestryFlowerTypes.SNOW, new FlowerType(ForestryTags.Blocks.SNOW_FLOWERS, true));
		apiculture.registerFlowerType(ForestryFlowerTypes.WHEAT, new FlowerType(ForestryTags.Blocks.WHEAT_FLOWERS, true));
		apiculture.registerFlowerType(ForestryFlowerTypes.GOURD, new FlowerType(ForestryTags.Blocks.GOURD_FLOWERS, true));

		apiculture.registerBeeEffect(ForestryBeeEffects.NONE, new DummyBeeEffect(true));
		apiculture.registerBeeEffect(ForestryBeeEffects.AGGRESSIVE, new AggressiveBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.HEROIC, new HeroicBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.BEATIFIC, new PotionBeeEffect(false, MobEffects.REGENERATION, 100));
		apiculture.registerBeeEffect(ForestryBeeEffects.MIASMIC, new PotionBeeEffect(false, MobEffects.POISON, 600, 100, 0.1f));
		apiculture.registerBeeEffect(ForestryBeeEffects.MISANTHROPE, new MisanthropeBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.GLACIAL, new GlacialBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.RADIOACTIVE, new RadioactiveBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.CREEPER, new CreeperBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.IGNITION, new IgnitionBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.EXPLORATION, new ExplorationBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.EASTER, new DummyBeeEffect(true));
		apiculture.registerBeeEffect(ForestryBeeEffects.SNOWING, new SnowingBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.DRUNKARD, new PotionBeeEffect(false, MobEffects.CONFUSION, 100));
		apiculture.registerBeeEffect(ForestryBeeEffects.REANIMATION, new ResurrectionBeeEffect(ResurrectionBeeEffect.getReanimationList()));
		apiculture.registerBeeEffect(ForestryBeeEffects.RESURRECTION, new ResurrectionBeeEffect(ResurrectionBeeEffect.getResurrectionList()));
		apiculture.registerBeeEffect(ForestryBeeEffects.REPULSION, new RepulsionBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.FERTILE, new FertileBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.MYCOPHILIC, new FungificationBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.SIFTER, new SifterBeeEffect());
		apiculture.registerBeeEffect(ForestryBeeEffects.HAKUNA_MATATA, new PotionBeeEffectExclusive(false, ApicultureEffects.HAKUNA_MATATA.get(), 20 * 60 * 3, 100, 1.0f, ApicultureEffects.MATATA.get()));

		apiculture.registerSwarmerMaterial(ApicultureItems.ROYAL_JELLY.get(), 0.01f);
	}

	private static Supplier<List<ItemStack>> getHoneyComb(EnumHoneyComb type) {
		return () -> List.of(ApicultureItems.BEE_COMBS.stack(type));
	}

	@Override
	public void registerArboriculture(IArboricultureRegistration arboriculture) {
		DefaultTreeSpecies.register(arboriculture);

		ResourceLocation pomes = ForestryConstants.forestry("block/leaves/fruits.pomes");
		ResourceLocation nuts = ForestryConstants.forestry("block/leaves/fruits.nuts");
		ResourceLocation berries = ForestryConstants.forestry("block/leaves/fruits.berries");
		ResourceLocation citrus = ForestryConstants.forestry("block/leaves/fruits.citrus");
		ResourceLocation plums = ForestryConstants.forestry("block/leaves/fruits.plums");

		arboriculture.registerFruit(ForestryFruits.NONE, new DummyFruit(false));
		arboriculture.registerFruit(ForestryFruits.APPLE, new RipeningFruit(false, 10, pomes, 0xff2e2e, 0xe3f49c, List.of(Product.of(Items.APPLE))));
		// todo match vanilla cocoa and use fortune
		arboriculture.registerFruit(ForestryFruits.COCOA, new PodFruit(false, ForestryPodType.COCOA, List.of(Product.of(Items.COCOA_BEANS))));
		arboriculture.registerFruit(ForestryFruits.CHESTNUT, new RipeningFruit(true, 6, nuts, 0x7f333d, 0xc4d24a, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.CHESTNUT)))));
		arboriculture.registerFruit(ForestryFruits.WALNUT, new RipeningFruit(true, 8, nuts, 0xfba248, 0xc4d24a, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.WALNUT)))));
		arboriculture.registerFruit(ForestryFruits.CHERRY, new RipeningFruit(true, 10, berries, 0xff2e2e, 0xc4d24a, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.CHERRY)))));
		arboriculture.registerFruit(ForestryFruits.DATES, new PodFruit(false, ForestryPodType.DATES, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.DATES)))));
		arboriculture.registerFruit(ForestryFruits.PAPAYA, new PodFruit(false, ForestryPodType.PAPAYA, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.PAPAYA)))));
		arboriculture.registerFruit(ForestryFruits.LEMON, new RipeningFruit(true, 10, citrus, 0xeeee00, 0x99ff00, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.LEMON)))));
		arboriculture.registerFruit(ForestryFruits.PLUM, new RipeningFruit(true, 10, plums, 0x663446, 0xeeff1a, List.of(Product.of(CoreItems.FRUITS.item(ItemFruit.EnumFruit.PLUM)))));

		arboriculture.registerTreeEffect(ForestryAlleles.TREE_EFFECT_NONE.alleleId(), new DummyTreeEffect(false));
	}

	@Override
	public void registerLepidopterology(ILepidopterologyRegistration lepidopterology) {
		DefaultButterflySpecies.register(lepidopterology);

		lepidopterology.registerCocoon(ForestryAlleles.COCOON_DEFAULT.alleleId(), new DefaultCocoon("default", List.of(
				Product.of(Items.STRING, 2, 1f),
				Product.of(Items.STRING, 1, 0.75f),
				Product.of(Items.STRING, 3, 0.25f)
		)));

		lepidopterology.registerCocoon(ForestryAlleles.COCOON_SILK.alleleId(), new DefaultCocoon("silk", List.of(
				Product.of(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.SILK_WISP), 3, 0.75f),
				Product.of(CoreItems.CRAFTING_MATERIALS.item(EnumCraftingMaterial.SILK_WISP), 2, 0.25f)
		)));

		lepidopterology.registerEffect(ForestryAlleles.BUTTERFLY_EFFECT_NONE.alleleId(), new DummyButterflyEffect());
	}

	@Override
	public void registerCircuits(ICircuitRegistration circuits) {
		// Layouts
		circuits.registerLayout(ForestryCircuitLayouts.MANAGED_FARM, ForestryCircuitSocketTypes.FARM);
		circuits.registerLayout(ForestryCircuitLayouts.MANUAL_FARM, ForestryCircuitSocketTypes.FARM);
		circuits.registerLayout(ForestryCircuitLayouts.MACHINE_UPGRADE, ForestryCircuitSocketTypes.MACHINE);

		// Managed Farms
		registerFarmCircuit(circuits, EnumElectronTube.COPPER, ForestryFarmTypes.ARBOREAL, false);
		registerFarmCircuit(circuits, EnumElectronTube.TIN, ForestryFarmTypes.PEAT, false);
		registerFarmCircuit(circuits, EnumElectronTube.BRONZE, ForestryFarmTypes.CROPS, false);
		registerFarmCircuit(circuits, EnumElectronTube.IRON, ForestryFarmTypes.ENDER, false);
		registerFarmCircuit(circuits, EnumElectronTube.BLAZE, ForestryFarmTypes.INFERNAL, false);
		registerFarmCircuit(circuits, EnumElectronTube.APATITE, ForestryFarmTypes.SHROOM, false);

		// Manual Farms
		registerFarmCircuit(circuits, EnumElectronTube.COPPER, ForestryFarmTypes.ORCHARD, true);
		registerFarmCircuit(circuits, EnumElectronTube.TIN, ForestryFarmTypes.PEAT, true);
		registerFarmCircuit(circuits, EnumElectronTube.BRONZE, ForestryFarmTypes.CROPS, true);
		registerFarmCircuit(circuits, EnumElectronTube.IRON, ForestryFarmTypes.ENDER, true);
		registerFarmCircuit(circuits, EnumElectronTube.GOLD, ForestryFarmTypes.SUCCULENTES, true);
		registerFarmCircuit(circuits, EnumElectronTube.DIAMOND, ForestryFarmTypes.POALES, true);
		registerFarmCircuit(circuits, EnumElectronTube.OBSIDIAN, ForestryFarmTypes.GOURD, true);
		registerFarmCircuit(circuits, EnumElectronTube.APATITE, ForestryFarmTypes.SHROOM, true);
		registerFarmCircuit(circuits, EnumElectronTube.LAPIS, ForestryFarmTypes.COCOA, true);

		// Factory
		circuits.registerCircuit(ForestryCircuitLayouts.MACHINE_UPGRADE, CoreItems.ELECTRON_TUBES.stack(EnumElectronTube.EMERALD, 1), new CircuitSpeedUpgrade("machine.speed.boost.1", 0.125f, 0.05f));
		circuits.registerCircuit(ForestryCircuitLayouts.MACHINE_UPGRADE, CoreItems.ELECTRON_TUBES.stack(EnumElectronTube.BLAZE, 1), new CircuitSpeedUpgrade("machine.speed.boost.2", 0.250f, 0.10f));
		circuits.registerCircuit(ForestryCircuitLayouts.MACHINE_UPGRADE, CoreItems.ELECTRON_TUBES.stack(EnumElectronTube.GOLD, 1), new CircuitSpeedUpgrade("machine.efficiency.1", 0, -0.10f));
	}

	private static void registerFarmCircuit(ICircuitRegistration circuits, EnumElectronTube tube, ResourceLocation typeId, boolean manual) {
		String id = manual ? "farm.manual." + typeId.getPath() : "farm.managed." + typeId.getPath();
		circuits.registerCircuit(manual ? ForestryCircuitLayouts.MANUAL_FARM : ForestryCircuitLayouts.MANAGED_FARM, CoreItems.ELECTRON_TUBES.stack(tube, 1), new CircuitFarmLogic(id, typeId, manual));
	}

	@Override
	public void registerErrors(IErrorRegistration errors) {
		for (IError error : ForestryError.values()) {
			errors.registerError(error);
		}
	}

	@Override
	public void registerFarming(IFarmingRegistration farming) {
		DefaultFarms.registerFarmTypes(farming);

		farming.registerFertilizer(CoreItems.FERTILIZER_COMPOUND.get(), 500);
	}

	@Override
	public void registerPollen(IPollenRegistration pollen) {
		pollen.registerPollenType(new TreePollenType());
	}

	@Override
	public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
		registrar.accept(new DefaultForestryClientRegistration());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}

package forestry.plugin;

import java.awt.Color;

import forestry.arboriculture.VanillaWoodType;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.arboriculture.ForestryWoodType;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.IArboricultureRegistration;
import forestry.arboriculture.worldgen.FeatureAcacia;
import forestry.arboriculture.worldgen.FeatureAcaciaVanilla;
import forestry.arboriculture.worldgen.FeatureBalsa;
import forestry.arboriculture.worldgen.FeatureBaobab;
import forestry.arboriculture.worldgen.FeatureBirch;
import forestry.arboriculture.worldgen.FeatureCherry;
import forestry.arboriculture.worldgen.FeatureChestnut;
import forestry.arboriculture.worldgen.FeatureCocobolo;
import forestry.arboriculture.worldgen.FeatureDarkOak;
import forestry.arboriculture.worldgen.FeatureDate;
import forestry.arboriculture.worldgen.FeatureEbony;
import forestry.arboriculture.worldgen.FeatureGiganteum;
import forestry.arboriculture.worldgen.FeatureGreenheart;
import forestry.arboriculture.worldgen.FeatureIpe;
import forestry.arboriculture.worldgen.FeatureJungle;
import forestry.arboriculture.worldgen.FeatureKapok;
import forestry.arboriculture.worldgen.FeatureLarch;
import forestry.arboriculture.worldgen.FeatureLemon;
import forestry.arboriculture.worldgen.FeatureMahoe;
import forestry.arboriculture.worldgen.FeatureMahogany;
import forestry.arboriculture.worldgen.FeatureMaple;
import forestry.arboriculture.worldgen.FeatureOak;
import forestry.arboriculture.worldgen.FeaturePadauk;
import forestry.arboriculture.worldgen.FeaturePapaya;
import forestry.arboriculture.worldgen.FeaturePine;
import forestry.arboriculture.worldgen.FeaturePlum;
import forestry.arboriculture.worldgen.FeaturePoplar;
import forestry.arboriculture.worldgen.FeatureSequoia;
import forestry.arboriculture.worldgen.FeatureSilverLime;
import forestry.arboriculture.worldgen.FeatureSpruce;
import forestry.arboriculture.worldgen.FeatureTeak;
import forestry.arboriculture.worldgen.FeatureWalnut;
import forestry.arboriculture.worldgen.FeatureWenge;
import forestry.arboriculture.worldgen.FeatureWillow;
import forestry.arboriculture.worldgen.FeatureZebrawood;

import static forestry.api.genetics.ForestryTaxa.*;

// todo fix IRL inaccuracies
public class DefaultTreeSpecies {
	public static void register(IArboricultureRegistration arboriculture) {
		// Oak (English Oak)
		arboriculture.registerSpecies(ForestryTreeSpecies.OAK, GENUS_QUERCUS, SPECIES_OAK, false, new Color(4764952), VanillaWoodType.OAK)
				.setTreeFeature(FeatureOak::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_APPLE);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Dark Oak (Black Oak)
		arboriculture.registerSpecies(ForestryTreeSpecies.DARK_OAK, GENUS_QUERCUS, SPECIES_DARK_OAK, false, new Color(4764952), VanillaWoodType.DARK_OAK)
				.setTreeFeature(FeatureDarkOak::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				});

		// Birch (Silver Birch)
		arboriculture.registerSpecies(ForestryTreeSpecies.BIRCH, GENUS_BETULA, SPECIES_BIRCH, false, new Color(8431445), VanillaWoodType.BIRCH)
				.setTreeFeature(FeatureBirch::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Silver Lime
		arboriculture.registerSpecies(ForestryTreeSpecies.LIME, GENUS_TILIA, SPECIES_LIME, true, new Color(0x5ea107), ForestryWoodType.LIME)
				.setTreeFeature(FeatureSilverLime::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.BIRCH, ForestryTreeSpecies.OAK, 15);
				})
				.setRarity(0.005f);

		// Walnut (English Walnut)
		arboriculture.registerSpecies(ForestryTreeSpecies.WALNUT, GENUS_JUGLANS, SPECIES_WALNUT, true, new Color(0x798c55), ForestryWoodType.WALNUT)
				.setTreeFeature(FeatureWalnut::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_WALNUT);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.CHERRY, 10);
				})
				.setHasFruitLeaves(true);

		// Chestnut (Spanish Chestnut)
		arboriculture.registerSpecies(ForestryTreeSpecies.CHESTNUT, GENUS_CASTANEA, SPECIES_CHESTNUT, true, new Color(0x5ea107), ForestryWoodType.CHESTNUT)
				.setTreeFeature(FeatureChestnut::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_CHESTNUT);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.LIME, 10);
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.CHERRY, 10);
				})
				.setHasFruitLeaves(true);

		// Cherry (East Asian Cherry) TODO This should be replaced by Vanilla cherry tree in 1.20
		// The real life version of this tree doesn't actually produce fruit.
		arboriculture.registerSpecies(ForestryTreeSpecies.CHERRY, GENUS_PRUNUS, SPECIES_CHERRY, true, new Color(0xe691da), ForestryWoodType.CHERRY)
				.setTreeFeature(FeatureCherry::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_CHERRY);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.OAK, 10);
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.BIRCH, 10);
				})
				.setHasFruitLeaves(true);

		// Lemon
		arboriculture.registerSpecies(ForestryTreeSpecies.LEMON, GENUS_CITRUS, SPECIES_LEMON, true, new Color(0x88af54), ForestryWoodType.CITRUS)
				.setTreeFeature(FeatureLemon::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_LEMON);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOWER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);

				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.CHERRY, 5);
				})
				.setHasFruitLeaves(true);

		// Plum (Common Plum)
		arboriculture.registerSpecies(ForestryTreeSpecies.PLUM, GENUS_PRUNUS, SPECIES_PLUM, true, new Color(0x589246), ForestryWoodType.PLUM)
				.setTreeFeature(FeaturePlum::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PLUM);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGH);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LEMON, ForestryTreeSpecies.CHERRY, 5);
				})
				.setRarity(0.005f)
				.setHasFruitLeaves(true);

		// Maple (Sugar Maple)
		arboriculture.registerSpecies(ForestryTreeSpecies.MAPLE, GENUS_ACER, SPECIES_MAPLE, true, new Color(0xd4f425), ForestryWoodType.MAPLE)
				.setTreeFeature(FeatureMaple::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.LARCH, 5);
				})
				.setRarity(0.0025f);

		// Spruce (Norway Spruce)
		arboriculture.registerSpecies(ForestryTreeSpecies.SPRUCE, GENUS_PICEA, SPECIES_SPRUCE, false, new Color(6396257), VanillaWoodType.SPRUCE)
				.setTreeFeature(FeatureSpruce::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Larch (European Larch)
		arboriculture.registerSpecies(ForestryTreeSpecies.LARCH, GENUS_LARIX, SPECIES_LARCH, true, new Color(0x698f90), ForestryWoodType.LARCH)
				.setTreeFeature(FeatureLarch::new)
				.setTemperature(TemperatureType.COLD)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.BIRCH, 10);
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.OAK, 10);
				})
				.setRarity(0.0025f);

		// Pine (Bull Pine)
		arboriculture.registerSpecies(ForestryTreeSpecies.PINE, GENUS_PINUS, SPECIES_PINE, true, new Color(0xfeff8f), ForestryWoodType.PINE)
				.setTreeFeature(FeaturePine::new)
				.setTemperature(TemperatureType.COLD)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.LARCH, 10);
				})
				.setRarity(0.0025f);

		// Sequoia (Coast Redwood)
		arboriculture.registerSpecies(ForestryTreeSpecies.SEQUOIA, GENUS_SEQUOIA, SPECIES_SEQUOIA, false, new Color(0x418e71), ForestryWoodType.SEQUOIA)
				.setTreeFeature(FeatureSequoia::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
					genome.set(TreeChromosomes.FIREPROOF, true);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LARCH, ForestryTreeSpecies.PINE, 5);
				});

		// Giant Sequoia
		arboriculture.registerSpecies(ForestryTreeSpecies.GIANT_SEQUOIA, GENUS_SEQUOIADENDRON, SPECIES_GIANT_SEQUOIA, false, new Color(0x738434), ForestryWoodType.GIGANTEUM)
				.setTreeFeature(FeatureGiganteum::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_GIGANTIC);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWEST);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWEST);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_4);
					genome.set(TreeChromosomes.FIREPROOF, true);
				})
				.setComplexity(10)
				.addMutations(mutations -> {
					// From GTNH fork of Forestry. No longer requires a villager trade.
					mutations.add(ForestryTreeSpecies.SEQUOIA, ForestryTreeSpecies.BAOBAB, 1);
				});

		// Jungle (Might be based on Teak, not sure)
		arboriculture.registerSpecies(ForestryTreeSpecies.JUNGLE, GENUS_TROPICAL, SPECIES_JUNGLE, false, new Color(4764952), VanillaWoodType.JUNGLE)
				.setTreeFeature(FeatureJungle::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_COCOA);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
				});

		// Teak
		arboriculture.registerSpecies(ForestryTreeSpecies.TEAK, GENUS_TECTONA, SPECIES_TEAK, true, new Color(0xfeff8f), ForestryWoodType.TEAK)
				.setTreeFeature(FeatureTeak::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DARK_OAK, ForestryTreeSpecies.JUNGLE, 10);
				})
				.setRarity(0.0025f);

		// Ipe (Yellow Ipe) TODO: Should be Handroanthus serratifolius
		arboriculture.registerSpecies(ForestryTreeSpecies.IPE, GENUS_TABEBUIA, SPECIES_IPE, true, new Color(0xfdd207), ForestryWoodType.IPE)
				.setTreeFeature(FeatureIpe::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.DARK_OAK, 10);
				});

		// Kapok
		arboriculture.registerSpecies(ForestryTreeSpecies.KAPOK, GENUS_CEIBA, SPECIES_KAPOK, true, new Color(0x89987b), ForestryWoodType.KAPOK)
				.setTreeFeature(FeatureKapok::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.TEAK, 10);
				});

		// Ebony (Myrtle Ebony)
		arboriculture.registerSpecies(ForestryTreeSpecies.EBONY, GENUS_DIOSPYROS, SPECIES_EBONY, true, new Color(0xa2d24a), ForestryWoodType.EBONY)
				.setTreeFeature(FeatureEbony::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
				})
				.setRarity(0.0005f);

		// Zebrawood (Wood is called "zebrawood" but species is glassywood. should this be changed?)
		arboriculture.registerSpecies(ForestryTreeSpecies.ZEBRAWOOD, GENUS_ASTRONIUM, SPECIES_ZEBRAWOOD, false, new Color(0xa2d24a), ForestryWoodType.ZEBRAWOOD)
				.setTreeFeature(FeatureZebrawood::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.EBONY, ForestryTreeSpecies.POPLAR, 5);
				})
				.setRarity(0.0005f);

		// Mahogany TODO taxonomy is wrong
		arboriculture.registerSpecies(ForestryTreeSpecies.MAHOGANY, GENUS_MAHOGANY, SPECIES_MAHOGONY, true, new Color(0x8ab154), ForestryWoodType.MAHOGANY)
				.setTreeFeature(FeatureMahogany::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.KAPOK, ForestryTreeSpecies.EBONY, 10);
				})
				.setRarity(0.0005f);

		// Vanilla Acacia TODO should probably switch with desert acacia since aneura are from Australia
		arboriculture.registerSpecies(ForestryTreeSpecies.ACACIA_VANILLA, GENUS_ACACIA, SPECIES_ACACIA, true, new Color(0x616101), VanillaWoodType.ACACIA)
				.setTreeFeature(FeatureAcaciaVanilla::new);

		// Desert Acacia
		arboriculture.registerSpecies(ForestryTreeSpecies.DESERT_ACACIA, GENUS_ACACIA, SPECIES_DESERT_ACACIA, true, new Color(0x748C1C), ForestryWoodType.ACACIA_DESERT)
				.setTreeFeature(FeatureAcacia::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.BALSA, 10);
				})
				.setRarity(0.005f);

		// Padauk (African Padauk)
		arboriculture.registerSpecies(ForestryTreeSpecies.PADAUK, GENUS_PTEROCARPUS, SPECIES_PADAUK, true, new Color(0xd0df8c), ForestryWoodType.PADAUK)
				.setTreeFeature(FeaturePadauk::new)
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ACACIA_VANILLA, ForestryTreeSpecies.JUNGLE, 10);
				})
				.setRarity(0.005f);

		// Balsa
		arboriculture.registerSpecies(ForestryTreeSpecies.BALSA, GENUS_OCHROMA, SPECIES_BALSA, true, new Color(0x59ac00), ForestryWoodType.BALSA)
				.setTreeFeature(FeatureBalsa::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_HIGH);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.ACACIA_VANILLA, 10);
				})
				.setRarity(0.0005f);

		// Cocobolo
		arboriculture.registerSpecies(ForestryTreeSpecies.COCOBOLO, GENUS_DALBERGIA, SPECIES_COCOBOLO, false, new Color(0x6aa17a), ForestryWoodType.COCOBOLO)
				.setTreeFeature(FeatureCocobolo::new)
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DESERT_ACACIA, ForestryTreeSpecies.DARK_OAK, 10);
				})
				.setRarity(0.0005f);

		// Wenge
		arboriculture.registerSpecies(ForestryTreeSpecies.WENGE, GENUS_MILLETTIA, SPECIES_WENGE, true, new Color(0xada157), ForestryWoodType.WENGE)
				.setTreeFeature(FeatureWenge::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWEST);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.COCOBOLO, ForestryTreeSpecies.BALSA, 10);
				})
				.setRarity(0.0005F);

		// Baobab
		arboriculture.registerSpecies(ForestryTreeSpecies.BAOBAB, GENUS_ADANSONIA, SPECIES_BAOBAB, true, new Color(0xfeff8f), ForestryWoodType.BAOBAB)
				.setTreeFeature(FeatureBaobab::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.BALSA, ForestryTreeSpecies.WENGE, 10);
				})
				.setRarity(0.005f);

		// Mahoe
		arboriculture.registerSpecies(ForestryTreeSpecies.MAHOE, GENUS_TALIPARITI, SPECIES_MAHOE, true, new Color(0xa0ba1b), ForestryWoodType.MAHOE)
				.setTreeFeature(FeatureMahoe::new)
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGH);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.BALSA, ForestryTreeSpecies.DESERT_ACACIA, 5);
				})
				.setRarity(0.000005f);

		// Willow
		arboriculture.registerSpecies(ForestryTreeSpecies.WILLOW, GENUS_SALIX, SPECIES_WILLOW, true, new Color(0xa3b8a5), ForestryWoodType.WILLOW)
				.setTreeFeature(FeatureWillow::new)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.OAK, ForestryTreeSpecies.BIRCH, 10)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
					mutations.add(ForestryTreeSpecies.OAK, ForestryTreeSpecies.LIME, 10)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.BIRCH, 10)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.0025f);

		// Sipiri
		arboriculture.registerSpecies(ForestryTreeSpecies.SIPIRI, GENUS_CHLOROCARDIUM, SPECIES_SIPIRI, true, new Color(0x678911), ForestryWoodType.GREENHEART)
				.setTreeFeature(FeatureGreenheart::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.KAPOK, ForestryTreeSpecies.MAHOGANY, 10)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.0025f);

		// Papaya
		arboriculture.registerSpecies(ForestryTreeSpecies.PAPAYA, GENUS_CARICA, SPECIES_PAPAYA, true, new Color(0x6d9f58), ForestryWoodType.PAPAYA)
				.setTreeFeature(FeaturePapaya::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PAPAYA);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.CHERRY, 5);
				})
				.setRarity(0.005f);

		// Date
		arboriculture.registerSpecies(ForestryTreeSpecies.DATE, GENUS_PHOENIX, SPECIES_DATE, true, new Color(0xcbcd79), ForestryWoodType.PALM)
				.setTreeFeature(FeatureDate::new)
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_DATES);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.PAPAYA, 5);
				})
				.setRarity(0.005f);

		// Poplar
		arboriculture.registerSpecies(ForestryTreeSpecies.POPLAR, GENUS_POPULUS, SPECIES_POPLAR, true, new Color(0xa3b8a5), ForestryWoodType.POPLAR)
				.setTreeFeature(FeaturePoplar::new)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.BIRCH, ForestryTreeSpecies.WILLOW, 5);
					mutations.add(ForestryTreeSpecies.OAK, ForestryTreeSpecies.WILLOW, 5);
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.WILLOW, 5);
				});
	}
}
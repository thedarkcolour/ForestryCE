package forestry.plugin;

import forestry.arboriculture.worldgen.*;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.IArboricultureRegistration;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.VanillaWoodType;
import forestry.arboriculture.blocks.ForestryLeafType;
import forestry.arboriculture.features.ArboricultureBlocks;

import static forestry.api.genetics.ForestryTaxa.*;

public class DefaultTreeSpecies {
	public static void register(IArboricultureRegistration arboriculture) {

		//TEMPERATE LINE

		// Apple Oak (English Oak) https://www.catalogueoflife.org/data/taxon/4R5YN
		arboriculture.registerSpecies(ForestryTreeSpecies.OAK, GENUS_QUERCUS, SPECIES_OAK, false, TextColor.fromRgb(4764952), VanillaWoodType.OAK)
				.setTreeFeature(FeatureTreeVanilla::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.OAK))
				.addVanillaStates(Blocks.OAK_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.OAK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.OAK).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.OAK_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_APPLE);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Silver Birch https://www.catalogueoflife.org/data/taxon/LPCQ
		arboriculture.registerSpecies(ForestryTreeSpecies.BIRCH, GENUS_BETULA, SPECIES_BIRCH, false, TextColor.fromRgb(8431445), VanillaWoodType.BIRCH)
				.setTreeFeature(FeatureTreeVanilla::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.BIRCH))
				.addVanillaStates(Blocks.BIRCH_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.BIRCH).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.BIRCH).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.BIRCH_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Silver Lime https://www.catalogueoflife.org/data/taxon/56WVQ
		arboriculture.registerSpecies(ForestryTreeSpecies.LIME, GENUS_TILIA, SPECIES_LIME, true, TextColor.fromRgb(0x5ea107), ForestryWoodType.LIME)
				.setTreeFeature(FeatureSilverLime::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.LIME))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.LIME).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LIME).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.OAK, ForestryTreeSpecies.BIRCH, 0.15f);
				})
				.setRarity(0.005f);

		// Sour Cherry https://www.catalogueoflife.org/data/taxon/4N8QS
		// Previously known as Hill Cherry, and this introduced a world of hurt.
		arboriculture.registerSpecies(ForestryTreeSpecies.SOUR_CHERRY, GENUS_PRUNUS, SPECIES_SOUR_CHERRY, true, TextColor.fromRgb(0x84AA37), ForestryWoodType.HILL_CHERRY)
				.setTreeFeature(FeatureSourCherry::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.SOUR_CHERRY))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.SOUR_CHERRY).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.SOUR_CHERRY).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_CHERRY);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.OAK, 0.10f);
				})
				.setRarity(0.0015f);

		// Common Walnut https://www.catalogueoflife.org/data/taxon/6NFN8
		arboriculture.registerSpecies(ForestryTreeSpecies.WALNUT, GENUS_JUGLANS, SPECIES_WALNUT, true, TextColor.fromRgb(0x798c55), ForestryWoodType.WALNUT)
				.setTreeFeature(FeatureWalnut::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.WALNUT))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.WALNUT).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.WALNUT).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_WALNUT);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SOUR_CHERRY, ForestryTreeSpecies.DARK_OAK, 0.10f);
				});

		// Sweet Chestnut https://www.catalogueoflife.org/data/taxon/5XCVW
		arboriculture.registerSpecies(ForestryTreeSpecies.CHESTNUT, GENUS_CASTANEA, SPECIES_CHESTNUT, true, TextColor.fromRgb(0x7E8E4D), ForestryWoodType.CHESTNUT)
				.setTreeFeature(FeatureChestnut::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.CHESTNUT))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.CHESTNUT).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.CHESTNUT).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_CHESTNUT);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.LIME, 0.05f)
							.restrictTemperature(TemperatureType.NORMAL, TemperatureType.NORMAL)
							.restrictHumidity(HumidityType.NORMAL);
				});

		// Pear (D'Anjou) https://www.catalogueoflife.org/data/taxon/4QWMZ
		arboriculture.registerSpecies(ForestryTreeSpecies.PEAR, GENUS_PYRUS, SPECIES_PEAR, true, TextColor.fromRgb(0x448944), ForestryWoodType.PEAR)
				.setTreeFeature(FeaturePear::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PEAR))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PEAR).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PEAR).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PEAR);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SOUR_CHERRY, ForestryTreeSpecies.OAK, 0.10f);
				})
				.setAuthority("Spear");

		// Plum https://www.catalogueoflife.org/data/taxon/4N8SY
		arboriculture.registerSpecies(ForestryTreeSpecies.PLUM, GENUS_PRUNUS, SPECIES_PLUM, true, TextColor.fromRgb(0x589246), ForestryWoodType.PLUM)
				.setTreeFeature(FeaturePlum::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PLUM))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PLUM).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PLUM).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PLUM);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGH);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.PEAR, ForestryTreeSpecies.SOUR_CHERRY, 0.05f)
							.restrictTemperature(TemperatureType.NORMAL, TemperatureType.NORMAL)
							.restrictHumidity(HumidityType.NORMAL);
				})
				.setRarity(0.005f);


		// Feijoa https://www.catalogueoflife.org/data/taxon/3DXCX
		arboriculture.registerSpecies(ForestryTreeSpecies.FEIJOA, GENUS_FEIJOA, SPECIES_FEIJOA, true, TextColor.fromRgb(0x99BAA4), ForestryWoodType.FEIJOA)
				.setTreeFeature(FeatureFeijoa::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.FEIJOA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.FEIJOA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.FEIJOA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_FEIJOA);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGHER); //This is (currently) the highest yield of all trees
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.PEAR, ForestryTreeSpecies.LIME, 0.05f)
							.restrictTemperature(TemperatureType.NORMAL, TemperatureType.WARM) //Feijoas also thrive in tropical conditions
							.restrictHumidity(HumidityType.NORMAL, HumidityType.DAMP); //See above
				})
				.setAuthority("Spear");


		// Golden Elm https://www.catalogueoflife.org/data/taxon/7DFJZ
		arboriculture.registerSpecies(ForestryTreeSpecies.ELM, GENUS_ULMUS, SPECIES_ELM, true, TextColor.fromRgb(0xDDFA52), ForestryWoodType.ELM)
				.setTreeFeature(FeatureElm::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ELM))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ELM).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ELM).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.BIRCH, 0.10f);
				})
				.setAuthority("Spear");

		// Sugar Maple https://www.catalogueoflife.org/data/taxon/94JK
		arboriculture.registerSpecies(ForestryTreeSpecies.MAPLE, GENUS_ACER, SPECIES_MAPLE, true, TextColor.fromRgb(0xd4f425), ForestryWoodType.MAPLE)
				.setTreeFeature(FeatureMaple::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.MAPLE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.MAPLE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.MAPLE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ELM, ForestryTreeSpecies.OAK, 0.05f);
				})
				.setRarity(0.0025f);

		// Copper Beech https://www.catalogueoflife.org/data/taxon/3DSK5
		arboriculture.registerSpecies(ForestryTreeSpecies.BEECH, GENUS_FAGUS, SPECIES_BEECH, true, TextColor.fromRgb(0xAD301A), ForestryWoodType.BEECH)
				.setTreeFeature(FeatureBeech::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.BEECH))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.BEECH).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.BEECH).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ELM, ForestryTreeSpecies.LIME, 0.05f);
				})
				.setAuthority("Spear");

		// White Poplar https://www.catalogueoflife.org/data/taxon/4LVJ5
		arboriculture.registerSpecies(ForestryTreeSpecies.POPLAR, GENUS_POPULUS, SPECIES_POPLAR, true, TextColor.fromRgb(0xa3b8a5), ForestryWoodType.POPLAR)
				.setTreeFeature(FeaturePoplar::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.POPLAR))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.POPLAR).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.POPLAR).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ELM, ForestryTreeSpecies.BIRCH, 0.05f);
				});

		// SWAMP LINE

		// Dark Oak (Black Oak) https://www.catalogueoflife.org/data/taxon/6X452
		// Dark Oak is the closest thing we have to a swamp tree rn. It's sorta used as a stand-in as such.
		arboriculture.registerSpecies(ForestryTreeSpecies.DARK_OAK, GENUS_QUERCUS, SPECIES_DARK_OAK, false, TextColor.fromRgb(4764952), VanillaWoodType.DARK_OAK)
				.setTreeFeature(FeatureTreeVanilla::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.DARK_OAK))
				.addVanillaStates(Blocks.DARK_OAK_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.DARK_OAK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.DARK_OAK).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.DARK_OAK_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.setAuthority("Binnie");

		// White Willow https://www.catalogueoflife.org/data/taxon/6XCGV
		arboriculture.registerSpecies(ForestryTreeSpecies.WILLOW, GENUS_SALIX, SPECIES_WILLOW, true, TextColor.fromRgb(0xa3b8a5), ForestryWoodType.WILLOW)
				.setTreeFeature(FeatureWillow::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.WILLOW))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.WILLOW).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.WILLOW).block().getStateDefinition().getPossibleStates())
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.POPLAR, ForestryTreeSpecies.DARK_OAK, 0.10f)
							.restrictTemperature(TemperatureType.NORMAL)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.0025f);

		//FLORAL LINE

		// Cherry Blossom https://www.catalogueoflife.org/data/taxon/4N97T
		arboriculture.registerSpecies(ForestryTreeSpecies.CHERRY_VANILLA, GENUS_PRUNUS, SPECIES_CHERRY_BLOSSOM, false, TextColor.fromRgb(0xf7b9dc), VanillaWoodType.CHERRY)
				.setTreeFeature(FeatureCherryVanilla::new)
				.setDecorativeLeaves(new ItemStack(Items.CHERRY_LEAVES))
				.addVanillaStates(Blocks.CHERRY_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.CHERRY_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.CHERRY_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.CHERRY_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.EFFECT, ForestryAlleles.TREE_EFFECT_BLOSSOMING);
				});

		// Flowering Dogwood https://www.catalogueoflife.org/data/taxon/YGJT
		arboriculture.registerSpecies(ForestryTreeSpecies.DOGWOOD, GENUS_CORNUS, SPECIES_DOGWOOD, true, TextColor.fromRgb(0xF4F4F4), ForestryWoodType.DOGWOOD)
				.setTreeFeature(FeatureDogwood::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.DOGWOOD))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.DOGWOOD).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.DOGWOOD).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.CHERRY_VANILLA, 0.10f);
				})
				.setAuthority("Spear");

		// Jacaranda https://www.catalogueoflife.org/data/taxon/99NRZ
		arboriculture.registerSpecies(ForestryTreeSpecies.JACARANDA, GENUS_JACARANDA, SPECIES_JACARANDA, true, TextColor.fromRgb(0xC18FFB), ForestryWoodType.JACARANDA)
				.setTreeFeature(FeatureJacaranda::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.JACARANDA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.JACARANDA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.JACARANDA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DOGWOOD, ForestryTreeSpecies.CHERRY_VANILLA, 0.05f)
							.restrictTemperature(TemperatureType.NORMAL, TemperatureType.WARM) // Jacaranda trees are tropical, but because they're bred from temperate trees, it seems fair to be a bit forgiving.
							.restrictHumidity(HumidityType.NORMAL, HumidityType.DAMP);
				})
				.setAuthority("Spear");

		// Ipe (Yellow Ipe) https://www.catalogueoflife.org/data/taxon/99M93
		arboriculture.registerSpecies(ForestryTreeSpecies.IPE, GENUS_HANDROANTHUS, SPECIES_IPE, true, TextColor.fromRgb(0xfdd207), ForestryWoodType.IPE)
				.setTreeFeature(FeatureIpe::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.IPE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.IPE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.IPE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DOGWOOD, ForestryTreeSpecies.TEAK, 0.05f)
							.restrictTemperature(TemperatureType.WARM)
							.restrictHumidity(HumidityType.DAMP);
				});

		// ANCIENT LINE

		// Ginkgo https://www.catalogueoflife.org/data/taxon/3G3B3
		arboriculture.registerSpecies(ForestryTreeSpecies.GINKGO, GENUS_GINKGO, SPECIES_GINKGO, true, TextColor.fromRgb(0xFCD54A), ForestryWoodType.GINKGO)
				.setTreeFeature(FeatureGinkgo::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.GINKGO))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.GINKGO).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.GINKGO).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGHER);//Highest of all trees. Best for Biofuel. Not, to my knowledge, representative of real life Ginkgo trees.
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWEST);
				})
				.setAuthority("Spear");
		//No mutations for this tree, as it comes from Sniffers.

		// CONIFEROUS LINE

		// Black Spruce https://www.catalogueoflife.org/data/taxon/4HQ3K
		arboriculture.registerSpecies(ForestryTreeSpecies.SPRUCE, GENUS_PICEA, SPECIES_SPRUCE, false, TextColor.fromRgb(6396257), VanillaWoodType.SPRUCE)
				.setTreeFeature(FeatureSpruce::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.SPRUCE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.SPRUCE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.SPRUCE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(Blocks.SPRUCE_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.SPRUCE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.SPRUCE).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.SPRUCE_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FASTER);
				});

		// Mundane Larch (European Larch) https://www.catalogueoflife.org/data/taxon/6NYWF
		arboriculture.registerSpecies(ForestryTreeSpecies.LARCH, GENUS_LARIX, SPECIES_LARCH, true, TextColor.fromRgb(0x698f90), ForestryWoodType.LARCH)
				.setTreeFeature(FeatureLarch::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.LARCH))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.LARCH).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LARCH).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.COLD)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.OAK, 0.15f);
				})
				.setRarity(0.0025f);

		// Ponderosa Pine https://www.catalogueoflife.org/data/taxon/4J2F3
		arboriculture.registerSpecies(ForestryTreeSpecies.PINE, GENUS_PICEA, SPECIES_PINE, true, TextColor.fromRgb(0xfeff8f), ForestryWoodType.PINE)
				.setTreeFeature(FeaturePine::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PINE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PINE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PINE).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.COLD)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LARCH, ForestryTreeSpecies.SPRUCE, 0.10f);
				})
				.setRarity(0.0025f);

		// Balsam Fir https://www.catalogueoflife.org/data/taxon/63Z6Q
		arboriculture.registerSpecies(ForestryTreeSpecies.FIR, GENUS_ABIES, SPECIES_FIR, true, TextColor.fromRgb(0x395A39), ForestryWoodType.FIR)
				.setTreeFeature(FeatureFir::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.FIR))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.FIR).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.FIR).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGH);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LARCH, ForestryTreeSpecies.OAK, 0.10f);
				})
				.setAuthority("Spear");

		// Macrocarpa (Monterey Cypress) https://www.catalogueoflife.org/data/taxon/3L5D5
		// Technically speaking it's a Monterey Cypress but I've called it Macrocarpa for two reasons:
		// 1 - No other trees have geographical places in their name. Monterey is a place in California
		// 2 - In New Zealand, we call them Macrocarpa trees, and supposedly they're known as such in other places too.
		// So yes it should give Cypress wood as well (which would be good for addons so they don't have to add it) but
		// we've also agreed that trees with mismatched names to their timbers are bad. So here we are.
		arboriculture.registerSpecies(ForestryTreeSpecies.MACROCARPA, GENUS_HESPEROCYPARIS, SPECIES_MACROCARPA, true, TextColor.fromRgb(0x5D7121), ForestryWoodType.MACROCARPA)
				.setTreeFeature(FeatureMacrocarpa::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.MACROCARPA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.MACROCARPA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.MACROCARPA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.PINE, ForestryTreeSpecies.FIR, 0.10f);
				})
				.setAuthority("Spear");

		// Coast Sequoia (Coast Redwood) https://www.catalogueoflife.org/data/taxon/4WSQG
		arboriculture.registerSpecies(ForestryTreeSpecies.SEQUOIA, GENUS_SEQUOIA, SPECIES_SEQUOIA, true, TextColor.fromRgb(0x418e71), ForestryWoodType.SEQUOIA)
				.setTreeFeature(FeatureSequoia::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.SEQUOIA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.SEQUOIA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.SEQUOIA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
					genome.set(TreeChromosomes.FIREPROOF, true);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.PINE, ForestryTreeSpecies.LARCH, 0.10f);
				});

		// Giant Sequoia https://www.catalogueoflife.org/data/taxon/4WSQK
		arboriculture.registerSpecies(ForestryTreeSpecies.GIANT_SEQUOIA, GENUS_SEQUOIADENDRON, SPECIES_GIANT_SEQUOIA, true, TextColor.fromRgb(0x738434), ForestryWoodType.GIGANTEUM)
				.setTreeFeature(FeatureGiganteum::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.GIANT_SEQUOIA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.GIANT_SEQUOIA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.GIANT_SEQUOIA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_GIGANTIC);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWEST);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWEST);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_4);
					genome.set(TreeChromosomes.FIREPROOF, true);
				})
				.setComplexity(10)
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.SEQUOIA, ForestryTreeSpecies.GINKGO, 0.05f)
							.restrictTemperature(TemperatureType.ICY, TemperatureType.COLD)
							.restrictHumidity(HumidityType.NORMAL);
				});

		// Pewen https://www.catalogueoflife.org/data/taxon/G67B
		arboriculture.registerSpecies(ForestryTreeSpecies.PEWEN, GENUS_ARAUCARIA, SPECIES_PEWEN, true, TextColor.fromRgb(0x455419), ForestryWoodType.PEWEN)
				.setTreeFeature(FeaturePewen::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PEWEN))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PEWEN).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PEWEN).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					//It was a Girth 2 tree in Extra Trees, but 1x1 is more accurate to real life
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGH);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
				}).addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.MACROCARPA, ForestryTreeSpecies.FIR, 0.05f)
							.restrictTemperature(TemperatureType.ICY, TemperatureType.COLD)
							.restrictHumidity(HumidityType.NORMAL);
				})
				.setAuthority("Spear");

		// Kauri https://www.catalogueoflife.org/data/taxon/5TQT6
		arboriculture.registerSpecies(ForestryTreeSpecies.KAURI, GENUS_AGATHIS, SPECIES_AUSTRALIS, true, TextColor.fromRgb(0x97AF64), ForestryWoodType.KAURI)
				.setTreeFeature(FeatureKauri::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.KAURI))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.KAURI).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.KAURI).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGH);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWEST);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWEST);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.MACROCARPA, ForestryTreeSpecies.PINE, 0.05f)
							.restrictTemperature(TemperatureType.ICY, TemperatureType.COLD)
							.restrictHumidity(HumidityType.NORMAL);
				})
				.setAuthority("Spear");

		// JUNGLE LINE

		// Jungle (Cocoa Tree) https://www.catalogueoflife.org/data/taxon/56BND
		arboriculture.registerSpecies(ForestryTreeSpecies.JUNGLE, GENUS_THEOBROMA, SPECIES_CACAO, false, TextColor.fromRgb(0x4764952), VanillaWoodType.JUNGLE)
				.setTreeFeature(FeatureJungle::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.JUNGLE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.JUNGLE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.JUNGLE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(Blocks.JUNGLE_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.JUNGLE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.JUNGLE).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.JUNGLE_SAPLING)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_COCOA);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
				});

		// Teak https://www.catalogueoflife.org/data/taxon/553LY
		arboriculture.registerSpecies(ForestryTreeSpecies.TEAK, GENUS_TECTONA, SPECIES_TEAK, true, TextColor.fromRgb(0xfeff8f), ForestryWoodType.TEAK)
				.setTreeFeature(FeatureTeak::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.TEAK))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.TEAK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.TEAK).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.DARK_OAK, 0.15f);
				})
				.setRarity(0.0025f);

		// Kapok https://www.catalogueoflife.org/data/taxon/S2C6
		arboriculture.registerSpecies(ForestryTreeSpecies.KAPOK, GENUS_CEIBA, SPECIES_KAPOK, true, TextColor.fromRgb(0x89987b), ForestryWoodType.KAPOK)
				.setTreeFeature(FeatureKapok::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.KAPOK))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.KAPOK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.KAPOK).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.JUNGLE, 0.10f);
				});

		// Balsa https://www.catalogueoflife.org/data/taxon/6SF4P
		arboriculture.registerSpecies(ForestryTreeSpecies.BALSA, GENUS_OCHROMA, SPECIES_BALSA, true, TextColor.fromRgb(0x59ac00), ForestryWoodType.BALSA)
				.setTreeFeature(FeatureBalsa::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.BALSA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.BALSA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.BALSA).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_HIGH);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.BIRCH, 0.10f);
				})
				.setRarity(0.0005f);

		// Sweet Orange https://en.wikipedia.org/wiki/Citrus_%C3%97_sinensis
		arboriculture.registerSpecies(ForestryTreeSpecies.ORANGE, GENUS_CITRUS, SPECIES_ORANGE, true, TextColor.fromRgb(0x57AD3F), ForestryWoodType.ORANGE)
				.setTreeFeature(FeatureOrange::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ORANGE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ORANGE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ORANGE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_ORANGE);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGH);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.JUNGLE, 0.10f);
				})
				.setAuthority("Spear");

		// Myrtle Ebony https://www.catalogueoflife.org/data/taxon/6CWPR
		arboriculture.registerSpecies(ForestryTreeSpecies.EBONY, GENUS_DIOSPYROS, SPECIES_EBONY, true, TextColor.fromRgb(0xa2d24a), ForestryWoodType.EBONY)
				.setTreeFeature(FeatureEbony::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.EBONY))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.EBONY).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.EBONY).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.BALSA, ForestryTreeSpecies.TEAK, 0.10f);
				})
				.setRarity(0.0005f);

		// Greenheart https://www.catalogueoflife.org/data/taxon/5XW95
		arboriculture.registerSpecies(ForestryTreeSpecies.GREENHEART, GENUS_CHLOROCARDIUM, SPECIES_GREENHEART, true, TextColor.fromRgb(0x678911), ForestryWoodType.GREENHEART)
				.setTreeFeature(FeatureGreenheart::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.GREENHEART))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.GREENHEART).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.GREENHEART).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					//Previously this had a restriction on it, but no longer.
					mutations.add(ForestryTreeSpecies.KAPOK, ForestryTreeSpecies.TEAK, 0.05f);
				})
				.setRarity(0.0025f);

		// Lemon https://www.catalogueoflife.org/data/taxon/9XK4K
		arboriculture.registerSpecies(ForestryTreeSpecies.LEMON, GENUS_CITRUS, SPECIES_LEMON, true, TextColor.fromRgb(0x5C8429), ForestryWoodType.CITRUS)
				.setTreeFeature(FeatureLemon::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.LEMON))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.LEMON).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LEMON).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_LEMON);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGH);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);

				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ORANGE, ForestryTreeSpecies.LIME, 0.10f);
				});

		// Zebrano https://www.catalogueoflife.org/data/taxon/42RTY
		arboriculture.registerSpecies(ForestryTreeSpecies.ZEBRANO, GENUS_MICROBERLINIA, SPECIES_ZEBRANO, true, TextColor.fromRgb(0xa2d24a), ForestryWoodType.ZEBRAWOOD)
				.setTreeFeature(FeatureZebrano::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ZEBRANO))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ZEBRANO).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ZEBRANO).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.EBONY, ForestryTreeSpecies.BALSA, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.0005f);

		// (Big-Leaf) Mahogany https://www.catalogueoflife.org/data/taxon/53K5Y
		arboriculture.registerSpecies(ForestryTreeSpecies.MAHOGANY, GENUS_SWIETENIA, SPECIES_MAHOGANY, true, TextColor.fromRgb(0x8ab154), ForestryWoodType.MAHOGANY)
				.setTreeFeature(FeatureMahogany::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.MAHOGANY))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.MAHOGANY).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.MAHOGANY).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.EBONY, ForestryTreeSpecies.KAPOK, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.0005f);

		// Coconut https://www.catalogueoflife.org/data/taxon/WP6H
		arboriculture.registerSpecies(ForestryTreeSpecies.COCONUT, GENUS_COCOS, SPECIES_COCONUT, true, TextColor.fromRgb(0x4F750F), ForestryWoodType.COCONUT)
				.setTreeFeature(FeatureCoconut::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.COCONUT))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.COCONUT).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.COCONUT).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_COCONUT);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_HIGH);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.KAPOK, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setAuthority("Spear");

		// Papaya https://www.catalogueoflife.org/data/taxon/RCZK
		arboriculture.registerSpecies(ForestryTreeSpecies.PAPAYA, GENUS_CARICA, SPECIES_PAPAYA, true, TextColor.fromRgb(0x74B225), ForestryWoodType.PAPAYA)
				.setTreeFeature(FeaturePapaya::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PAPAYA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PAPAYA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PAPAYA).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PAPAYA);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LEMON, ForestryTreeSpecies.KAPOK, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.DAMP);
				})
				.setRarity(0.005f);

		// ARID LINE

		// Acacia https://www.catalogueoflife.org/data/taxon/BSJF7
		arboriculture.registerSpecies(ForestryTreeSpecies.ACACIA_VANILLA, GENUS_ACACIA, SPECIES_ACACIA, false, TextColor.fromRgb(0x616101), VanillaWoodType.ACACIA)
				.setTreeFeature(FeatureAcacia::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ACACIA_VANILLA))
				.addVanillaStates(Blocks.ACACIA_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ACACIA_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ACACIA_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.ACACIA_SAPLING)
				.setAuthority("Binnie");

		// Camelthorn https://www.catalogueoflife.org/data/taxon/BTCD3
		arboriculture.registerSpecies(ForestryTreeSpecies.CAMELTHORN, GENUS_VACHELLIA, SPECIES_CAMELTHORN, true, TextColor.fromRgb(0x748C1C), ForestryWoodType.ACACIA_DESERT)
				.setTreeFeature(FeatureCamelthorn::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.CAMELTHORN))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.CAMELTHORN).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.CAMELTHORN).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWEST);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.ACACIA_VANILLA, ForestryTreeSpecies.JUNGLE, 0.15f);
				})
				.setRarity(0.005f);

		// Padauk (African Padauk) https://www.catalogueoflife.org/data/taxon/4PVKG
		arboriculture.registerSpecies(ForestryTreeSpecies.PADAUK, GENUS_PTEROCARPUS, SPECIES_PADAUK, true, TextColor.fromRgb(0xd0df8c), ForestryWoodType.PADAUK)
				.setTreeFeature(FeaturePadauk::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PADAUK))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PADAUK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PADAUK).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.CAMELTHORN, ForestryTreeSpecies.JUNGLE, 0.10f);
				})
				.setRarity(0.005f);

		// Cocobolo https://www.catalogueoflife.org/data/taxon/33Z8J
		arboriculture.registerSpecies(ForestryTreeSpecies.COCOBOLO, GENUS_DALBERGIA, SPECIES_COCOBOLO, true, TextColor.fromRgb(0x6aa17a), ForestryWoodType.COCOBOLO)
				.setTreeFeature(FeatureCocobolo::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.COCOBOLO))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.COCOBOLO).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.COCOBOLO).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.CAMELTHORN, ForestryTreeSpecies.DARK_OAK, 0.10f);
				})
				.setRarity(0.0005f);

		// Wenge https://www.catalogueoflife.org/data/taxon/43D8S
		arboriculture.registerSpecies(ForestryTreeSpecies.WENGE, GENUS_MILLETTIA, SPECIES_WENGE, true, TextColor.fromRgb(0xada157), ForestryWoodType.WENGE)
				.setTreeFeature(FeatureWenge::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.WENGE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.WENGE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.WENGE).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWEST);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_2);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.CAMELTHORN, ForestryTreeSpecies.ACACIA_VANILLA, 0.10f);
				})
				.setRarity(0.0005F);

		// Blue Mahoe  https://www.catalogueoflife.org/data/taxon/54LNR
		arboriculture.registerSpecies(ForestryTreeSpecies.MAHOE, GENUS_TALIPARITI, SPECIES_MAHOE, true, TextColor.fromRgb(0xa0ba1b), ForestryWoodType.MAHOE)
				.setTreeFeature(FeatureMahoe::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.MAHOE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.MAHOE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.MAHOE).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALL);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_HIGH);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOWEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.COCOBOLO, ForestryTreeSpecies.CAMELTHORN, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.ARID);
				})
				.setRarity(0.000005f);

		// Grandidier's Baobab https://www.catalogueoflife.org/data/taxon/9X66
		arboriculture.registerSpecies(ForestryTreeSpecies.BAOBAB, GENUS_ADANSONIA, SPECIES_BAOBAB, true, TextColor.fromRgb(0xfeff8f), ForestryWoodType.BAOBAB)
				.setTreeFeature(FeatureBaobab::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.BAOBAB))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.BAOBAB).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.BAOBAB).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_SLOW);
					genome.set(TreeChromosomes.GIRTH, ForestryAlleles.GIRTH_3);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.PADAUK, ForestryTreeSpecies.WENGE, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.ARID);
				})
				.setRarity(0.005f);

		// Date Palm https://www.catalogueoflife.org/data/taxon/4GKRK
		// TODO: Should this be renamed to Date wood, or stay as Palm wood?
		arboriculture.registerSpecies(ForestryTreeSpecies.DATE, GENUS_PHOENIX, SPECIES_DATE, true, TextColor.fromRgb(0xcbcd79), ForestryWoodType.PALM)
				.setTreeFeature(FeatureDate::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.DATE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.DATE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.DATE).block().getStateDefinition().getPossibleStates())
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
					mutations.add(ForestryTreeSpecies.COCOBOLO, ForestryTreeSpecies.SOUR_CHERRY, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.ARID);
				})
				.setRarity(0.005f);

		// Olive https://www.catalogueoflife.org/data/taxon/493JT
		arboriculture.registerSpecies(ForestryTreeSpecies.OLIVE, GENUS_OLEA, SPECIES_OLIVE, true, TextColor.fromRgb(0xB7B792), ForestryWoodType.OLIVE)
				.setTreeFeature(FeatureOlive::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.OLIVE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.OLIVE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.OLIVE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOW);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_FAST);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_OLIVE);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_AVERAGE);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.WENGE, ForestryTreeSpecies.SOUR_CHERRY, 0.05f)
							.restrictTemperature(TemperatureType.WARM, TemperatureType.HOT)
							.restrictHumidity(HumidityType.ARID);
				})
				.setAuthority("Spear");

	}
}

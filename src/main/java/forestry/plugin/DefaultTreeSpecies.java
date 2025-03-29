package forestry.plugin;

import java.awt.Color;

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

// todo fix IRL inaccuracies
public class DefaultTreeSpecies {
	public static void register(IArboricultureRegistration arboriculture) {
		// Oak (English Oak)
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

		// Dark Oak (Black Oak)
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

		// Birch (Silver Birch)
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

		// Silver Lime
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
					mutations.add(ForestryTreeSpecies.BIRCH, ForestryTreeSpecies.OAK, 15);
				})
				.setRarity(0.005f);

		// Walnut (English Walnut)
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
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.HILL_CHERRY, 10);
				});

		// Chestnut (Spanish Chestnut)
		arboriculture.registerSpecies(ForestryTreeSpecies.CHESTNUT, GENUS_CASTANEA, SPECIES_CHESTNUT, true, TextColor.fromRgb(0x5ea107), ForestryWoodType.CHESTNUT)
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
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.LIME, 10);
					mutations.add(ForestryTreeSpecies.WALNUT, ForestryTreeSpecies.HILL_CHERRY, 10);
				});

		// Hill Cherry (East Asian Cherry)
		// The real life version of this tree doesn't actually produce fruit.
		arboriculture.registerSpecies(ForestryTreeSpecies.HILL_CHERRY, GENUS_PRUNUS, SPECIES_HILL_CHERRY, true, TextColor.fromRgb(0xe691da), ForestryWoodType.HILL_CHERRY)
				.setTreeFeature(FeatureBushCherry::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.HILL_CHERRY))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.HILL_CHERRY).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.HILL_CHERRY).block().getStateDefinition().getPossibleStates())
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
				.setRarity(0.0015f);

		// Cherry Blossom (East Asian Cherry)
		// In real life, this is the same species as Hill Cherry. For the sake of keeping old content, we'll pretend they're different.
		arboriculture.registerSpecies(ForestryTreeSpecies.CHERRY_VANILLA, GENUS_PRUNUS, SPECIES_CHERRY_VANILLA, true, TextColor.fromRgb(0xf7b9dc), VanillaWoodType.CHERRY)
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

		// Lemon
		arboriculture.registerSpecies(ForestryTreeSpecies.LEMON, GENUS_CITRUS, SPECIES_LEMON, true, TextColor.fromRgb(0x88af54), ForestryWoodType.CITRUS)
				.setTreeFeature(FeatureLemon::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.LEMON))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.LEMON).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.LEMON).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_LEMON);
					genome.set(TreeChromosomes.YIELD, ForestryAlleles.YIELD_LOWER);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);

				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.HILL_CHERRY, 5);
				});

		// Plum (Common Plum)
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
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LEMON, ForestryTreeSpecies.HILL_CHERRY, 5);
				})
				.setRarity(0.005f);

		// Maple (Sugar Maple)
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
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.LARCH, 5);
				})
				.setRarity(0.0025f);

		// Spruce (Norway Spruce)
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

		// Larch (European Larch)
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
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.BIRCH, 10);
					mutations.add(ForestryTreeSpecies.SPRUCE, ForestryTreeSpecies.OAK, 10);
				})
				.setRarity(0.0025f);

		// Pine (Bull Pine)
		arboriculture.registerSpecies(ForestryTreeSpecies.PINE, GENUS_PINUS, SPECIES_PINE, true, TextColor.fromRgb(0xfeff8f), ForestryWoodType.PINE)
				.setTreeFeature(FeaturePine::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PINE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PINE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PINE).block().getStateDefinition().getPossibleStates())
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
		arboriculture.registerSpecies(ForestryTreeSpecies.SEQUOIA, GENUS_SEQUOIA, SPECIES_SEQUOIA, false, TextColor.fromRgb(0x418e71), ForestryWoodType.SEQUOIA)
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
					mutations.add(ForestryTreeSpecies.LARCH, ForestryTreeSpecies.PINE, 5);
				});

		// Giant Sequoia
		arboriculture.registerSpecies(ForestryTreeSpecies.GIANT_SEQUOIA, GENUS_SEQUOIADENDRON, SPECIES_GIANT_SEQUOIA, false, TextColor.fromRgb(0x738434), ForestryWoodType.GIGANTEUM)
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
					// From GTNH fork of Forestry. No longer requires a villager trade.
					mutations.add(ForestryTreeSpecies.SEQUOIA, ForestryTreeSpecies.BAOBAB, 0.01f);
				});

		// Jungle (Might be based on Teak, not sure)
		arboriculture.registerSpecies(ForestryTreeSpecies.JUNGLE, GENUS_TROPICAL, SPECIES_JUNGLE, false, TextColor.fromRgb(4764952), VanillaWoodType.JUNGLE)
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

		// Teak
		arboriculture.registerSpecies(ForestryTreeSpecies.TEAK, GENUS_TECTONA, SPECIES_TEAK, true, TextColor.fromRgb(0xfeff8f), ForestryWoodType.TEAK)
				.setTreeFeature(FeatureTeak::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.TEAK))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.TEAK).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.TEAK).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DARK_OAK, ForestryTreeSpecies.JUNGLE, 10);
				})
				.setRarity(0.0025f);

		// Ipe (Yellow Ipe)
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
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.DARK_OAK, 10);
				});

		// Kapok
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
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.TEAK, 10);
				});

		// Ebony (Myrtle Ebony)
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
					mutations.add(ForestryTreeSpecies.DARK_OAK, ForestryTreeSpecies.KAPOK, 10);
				})
				.setRarity(0.0005f);

		// Zebrawood (Wood is called "zebrawood" but species is glassywood. should this be changed?)
		arboriculture.registerSpecies(ForestryTreeSpecies.ZEBRAWOOD, GENUS_ASTRONIUM, SPECIES_ZEBRAWOOD, false, TextColor.fromRgb(0xa2d24a), ForestryWoodType.ZEBRAWOOD)
				.setTreeFeature(FeatureZebrawood::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ZEBRAWOOD))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ZEBRAWOOD).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ZEBRAWOOD).block().getStateDefinition().getPossibleStates())
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
		arboriculture.registerSpecies(ForestryTreeSpecies.MAHOGANY, GENUS_MAHOGANY, SPECIES_MAHOGONY, true, TextColor.fromRgb(0x8ab154), ForestryWoodType.MAHOGANY)
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
					mutations.add(ForestryTreeSpecies.KAPOK, ForestryTreeSpecies.EBONY, 10);
				})
				.setRarity(0.0005f);

		// Vanilla Acacia TODO should probably switch with desert acacia since aneura are from Australia
		arboriculture.registerSpecies(ForestryTreeSpecies.ACACIA_VANILLA, GENUS_ACACIA, SPECIES_ACACIA, true, TextColor.fromRgb(0x616101), VanillaWoodType.ACACIA)
				.setTreeFeature(FeatureTreeVanilla::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ACACIA_VANILLA))
				.addVanillaStates(Blocks.ACACIA_LEAVES.getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ACACIA_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ACACIA_VANILLA).block().getStateDefinition().getPossibleStates())
				.addVanillaSapling(Items.ACACIA_SAPLING)
				.setAuthority("Binnie");

		// Desert Acacia
		arboriculture.registerSpecies(ForestryTreeSpecies.DESERT_ACACIA, GENUS_ACACIA, SPECIES_DESERT_ACACIA, true, TextColor.fromRgb(0x748C1C), ForestryWoodType.ACACIA_DESERT)
				.setTreeFeature(FeatureAcacia::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.DESERT_ACACIA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.DESERT_ACACIA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.DESERT_ACACIA).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.ARID)
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.BALSA, 10);
				})
				.setRarity(0.005f);

		// Padauk (African Padauk)
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
					mutations.add(ForestryTreeSpecies.ACACIA_VANILLA, ForestryTreeSpecies.JUNGLE, 10);
				})
				.setRarity(0.005f);

		// Balsa
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
					mutations.add(ForestryTreeSpecies.TEAK, ForestryTreeSpecies.ACACIA_VANILLA, 10);
				})
				.setRarity(0.0005f);

		// Cocobolo
		arboriculture.registerSpecies(ForestryTreeSpecies.COCOBOLO, GENUS_DALBERGIA, SPECIES_COCOBOLO, false, TextColor.fromRgb(0x6aa17a), ForestryWoodType.COCOBOLO)
				.setTreeFeature(FeatureCocobolo::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.COCOBOLO))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.COCOBOLO).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.COCOBOLO).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGEST);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.DESERT_ACACIA, ForestryTreeSpecies.DARK_OAK, 10);
				})
				.setRarity(0.0005f);

		// Wenge
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
					mutations.add(ForestryTreeSpecies.COCOBOLO, ForestryTreeSpecies.BALSA, 10);
				})
				.setRarity(0.0005F);

		// Baobab
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
					mutations.add(ForestryTreeSpecies.BALSA, ForestryTreeSpecies.WENGE, 10);
				})
				.setRarity(0.005f);

		// Mahoe
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
					mutations.add(ForestryTreeSpecies.BALSA, ForestryTreeSpecies.DESERT_ACACIA, 5);
				})
				.setRarity(0.000005f);

		// Willow
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
		arboriculture.registerSpecies(ForestryTreeSpecies.SIPIRI, GENUS_CHLOROCARDIUM, SPECIES_SIPIRI, true, TextColor.fromRgb(0x678911), ForestryWoodType.GREENHEART)
				.setTreeFeature(FeatureGreenheart::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.SIPIRI))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.SIPIRI).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.SIPIRI).block().getStateDefinition().getPossibleStates())
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
		arboriculture.registerSpecies(ForestryTreeSpecies.PAPAYA, GENUS_CARICA, SPECIES_PAPAYA, true, TextColor.fromRgb(0x6d9f58), ForestryWoodType.PAPAYA)
				.setTreeFeature(FeaturePapaya::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PAPAYA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PAPAYA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PAPAYA).block().getStateDefinition().getPossibleStates())
				.setTemperature(TemperatureType.WARM)
				.setHumidity(HumidityType.DAMP)
				.setGenome(genome -> {
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PAPAYA);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_LOWER);
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.HILL_CHERRY, 5);
				})
				.setRarity(0.005f);

		// Date
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
					mutations.add(ForestryTreeSpecies.JUNGLE, ForestryTreeSpecies.PAPAYA, 5);
				})
				.setRarity(0.005f);

		// Poplar
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
					mutations.add(ForestryTreeSpecies.BIRCH, ForestryTreeSpecies.WILLOW, 5);
					mutations.add(ForestryTreeSpecies.OAK, ForestryTreeSpecies.WILLOW, 5);
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.WILLOW, 5);
				});

		// Golden Elm
		arboriculture.registerSpecies(ForestryTreeSpecies.ELM, GENUS_ULMUS, SPECIES_ELM, true, TextColor.fromRgb(0xDDFA52), ForestryWoodType.ELM)
				.setTreeFeature(FeatureElm::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ELM))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ELM).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ELM).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.SAPLINGS, ForestryAlleles.SAPLINGS_LOW);
				})
				.addMutations(mutations -> {
					mutations.add(ForestryTreeSpecies.LIME, ForestryTreeSpecies.CHERRY_VANILLA, 0.05f);
				})
				.setAuthority("Spear");

		// Balsam Fir
		arboriculture.registerSpecies(ForestryTreeSpecies.FIR, GENUS_ABIES, SPECIES_BALSAMEA, true, TextColor.fromRgb(0x395A39), ForestryWoodType.FIR)
				.setTreeFeature(FeatureFir::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.FIR))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.FIR).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.FIR).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Coconut
		arboriculture.registerSpecies(ForestryTreeSpecies.COCONUT, GENUS_COCOS, SPECIES_NUCIFERA, true, TextColor.fromRgb(0x6C8031), ForestryWoodType.COCONUT)
				.setTreeFeature(FeatureCoconut::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.COCONUT))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.COCONUT).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.COCONUT).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_LARGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_COCONUT);
				})
				.setAuthority("Spear");

		// Copper Beech
		arboriculture.registerSpecies(ForestryTreeSpecies.BEECH, GENUS_FAGUS, SPECIES_SYLVATICA, true, TextColor.fromRgb(0xAD301A), ForestryWoodType.BEECH)
				.setTreeFeature(FeatureBeech::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.BEECH))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.BEECH).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.BEECH).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Feijoa
		arboriculture.registerSpecies(ForestryTreeSpecies.FEIJOA, GENUS_FEIJOA, SPECIES_SELLOWIANA, true, TextColor.fromRgb(0x93A1A2), ForestryWoodType.FEIJOA)
				.setTreeFeature(FeatureFeijoa::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.FEIJOA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.FEIJOA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.FEIJOA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_SMALLEST);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_FEIJOA);
				})
				.setAuthority("Spear");

		// Flowering Dogwood
		arboriculture.registerSpecies(ForestryTreeSpecies.DOGWOOD, GENUS_CORNUS, SPECIES_FLORIDA, true, TextColor.fromRgb(0xF4F4F4), ForestryWoodType.DOGWOOD)
				.setTreeFeature(FeatureDogwood::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.DOGWOOD))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.DOGWOOD).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.DOGWOOD).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Ginkgo
		arboriculture.registerSpecies(ForestryTreeSpecies.GINKGO, GENUS_GINKGO, SPECIES_BILBOA, true, TextColor.fromRgb(0xFFE554), ForestryWoodType.GINKGO)
				.setTreeFeature(FeatureGinkgo::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.GINKGO))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.GINKGO).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.GINKGO).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Jacaranda
		arboriculture.registerSpecies(ForestryTreeSpecies.JACARANDA, GENUS_JACARANDA, SPECIES_MIMOSIFOLIA, true, TextColor.fromRgb(0xC18FFB), ForestryWoodType.JACARANDA)
				.setTreeFeature(FeatureJacaranda::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.JACARANDA))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.JACARANDA).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.JACARANDA).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Monkey Puzzle
		arboriculture.registerSpecies(ForestryTreeSpecies.MONKEY_PUZZLE, GENUS_ARAUCARIA, SPECIES_ARAUCANA, true, TextColor.fromRgb(0x6C8031), ForestryWoodType.MONKEY_PUZZLE)
				.setTreeFeature(FeatureMonkeyPuzzle::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.MONKEY_PUZZLE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.MONKEY_PUZZLE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.MONKEY_PUZZLE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Monterey Cypress (May be renamed to Macrocarpa)
		arboriculture.registerSpecies(ForestryTreeSpecies.CYPRESS, GENUS_HESPEROCYPARIS, SPECIES_MACROCARPA, true, TextColor.fromRgb(0x6C8031), ForestryWoodType.CYPRESS)
				.setTreeFeature(FeatureCypress::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.CYPRESS))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.CYPRESS).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.CYPRESS).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Olive
		arboriculture.registerSpecies(ForestryTreeSpecies.OLIVE, GENUS_OLEA, SPECIES_EUROPAEA, true, TextColor.fromRgb(0x6C8031), ForestryWoodType.OLIVE)
				.setTreeFeature(FeatureOlive::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.OLIVE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.OLIVE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.OLIVE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_OLIVE);
				})
				.setAuthority("Spear");

		// Sweet Orange
		arboriculture.registerSpecies(ForestryTreeSpecies.ORANGE, GENUS_CITRUS, SPECIES_SINENSIS, true, TextColor.fromRgb(0x57AD3F), ForestryWoodType.ORANGE)
				.setTreeFeature(FeatureOrange::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.ORANGE))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.ORANGE).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.ORANGE).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_ORANGE);
				})
				.setAuthority("Spear");

		// D'Anjou Pear
		arboriculture.registerSpecies(ForestryTreeSpecies.PEAR, GENUS_PYRUS, SPECIES_COMMUNIS, true, TextColor.fromRgb(0x6C8031), ForestryWoodType.PEAR)
				.setTreeFeature(FeaturePear::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.PEAR))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.PEAR).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.PEAR).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
					genome.set(TreeChromosomes.FRUIT, ForestryAlleles.FRUIT_PEAR);
				})
				.setAuthority("Spear");

		// Rainbow Eucalyptus
		arboriculture.registerSpecies(ForestryTreeSpecies.EUCALYPTUS, GENUS_EUCALYPTUS, SPECIES_DEGLUPTA, true, TextColor.fromRgb(0x70922D), ForestryWoodType.EUCALYPTUS)
				.setTreeFeature(FeatureEucalyptus::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.EUCALYPTUS))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.EUCALYPTUS).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.EUCALYPTUS).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Swamp Gum
		arboriculture.registerSpecies(ForestryTreeSpecies.GUM, GENUS_EUCALYPTUS, SPECIES_OVATA, true, TextColor.fromRgb(0x989855), ForestryWoodType.GUM)
				.setTreeFeature(FeatureGum::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.GUM))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.GUM).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.GUM).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");

		// Rocky Mountain Juniper
		arboriculture.registerSpecies(ForestryTreeSpecies.JUNIPER, GENUS_JUNIPERUS, SPECIES_SCOPULORUM, true, TextColor.fromRgb(0x8BC0C4), ForestryWoodType.JUNIPER)
				.setTreeFeature(FeatureJuniper::new)
				.setDecorativeLeaves(ArboricultureBlocks.LEAVES_DECORATIVE.stack(ForestryLeafType.JUNIPER))
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT.get(ForestryLeafType.JUNIPER).block().getStateDefinition().getPossibleStates())
				.addVanillaStates(ArboricultureBlocks.LEAVES_DEFAULT_FRUIT.get(ForestryLeafType.JUNIPER).block().getStateDefinition().getPossibleStates())
				.setGenome(genome -> {
					genome.set(TreeChromosomes.HEIGHT, ForestryAlleles.HEIGHT_AVERAGE);
					genome.set(TreeChromosomes.SAPPINESS, ForestryAlleles.SAPPINESS_AVERAGE);
					genome.set(TreeChromosomes.MATURATION, ForestryAlleles.MATURATION_AVERAGE);
				})
				.setAuthority("Spear");
	}
}

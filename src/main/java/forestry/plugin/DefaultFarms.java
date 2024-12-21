package forestry.plugin;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;

import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.core.IProduct;
import forestry.api.farming.ForestryFarmTypes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.genetics.alleles.IValueAllele;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.api.plugin.IFarmTypeBuilder;
import forestry.api.plugin.IFarmingRegistration;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.items.ItemFruit;
import forestry.core.utils.SpeciesUtil;
import forestry.farming.logic.FarmLogicArboreal;
import forestry.farming.logic.FarmLogicCocoa;
import forestry.farming.logic.FarmLogicCrops;
import forestry.farming.logic.FarmLogicEnder;
import forestry.farming.logic.FarmLogicGourd;
import forestry.farming.logic.FarmLogicInfernal;
import forestry.farming.logic.FarmLogicMushroom;
import forestry.farming.logic.FarmLogicOrchard;
import forestry.farming.logic.FarmLogicPeat;
import forestry.farming.logic.FarmLogicReeds;
import forestry.farming.logic.FarmLogicSucculent;
import forestry.farming.logic.farmables.FarmableAgingCrop;
import forestry.farming.logic.farmables.FarmableChorus;
import forestry.farming.logic.farmables.FarmableGE;
import forestry.farming.logic.farmables.FarmableGourd;
import forestry.farming.logic.farmables.FarmableMangroveTree;
import forestry.farming.logic.farmables.FarmableSapling;
import forestry.farming.logic.farmables.FarmableStacked;

public class DefaultFarms {
	public static void registerFarmTypes(IFarmingRegistration farming) {
		// Trees
		// TODO separate compatibility plugins that add all the extra junk items (ex. drops from mods like Delightful, Twig)
		IFarmTypeBuilder arboreal = farming.createFarmType(ForestryFarmTypes.ARBOREAL, FarmLogicArboreal::new, new ItemStack(Blocks.OAK_SAPLING))
				.setFertilizerConsumption(10)
				.setWaterConsumption(hydrationModifier -> (int) (10 * hydrationModifier))
				.addSoil(new ItemStack(Blocks.DIRT), CoreBlocks.HUMUS.defaultState())
				.addSoil(CoreBlocks.HUMUS.stack(), CoreBlocks.HUMUS.defaultState());
		addTreeFarmables(arboreal);

		// Crops
		IFarmTypeBuilder crops = farming.createFarmType(ForestryFarmTypes.CROPS, FarmLogicCrops::new, new ItemStack(Items.WHEAT))
				.setWaterConsumption(hydrationModifier -> (int) (20 * hydrationModifier))
				.setFertilizerConsumption(5)
				.addSoil(new ItemStack(Blocks.DIRT), Blocks.FARMLAND.defaultBlockState());
		addCropFarmables(crops);

		// Gourd (Pumpkin and Melon)
		IFarmTypeBuilder gourd = farming.createFarmType(ForestryFarmTypes.GOURD, FarmLogicGourd::new, new ItemStack(Items.MELON))
				.setFertilizerConsumption(10)
				.setWaterConsumption(hydrationModifier -> (int) (40 * hydrationModifier));
		addGourdFarmables(gourd);

		// Mushroom
		IFarmTypeBuilder shroom = farming.createFarmType(ForestryFarmTypes.SHROOM, FarmLogicMushroom::new, new ItemStack(Blocks.RED_MUSHROOM))
				.setFertilizerConsumption(20)
				.setWaterConsumption(hydrationModifier -> (int) (80 * hydrationModifier))
				.addSoil(Blocks.MYCELIUM)
				.addSoil(Blocks.PODZOL);

		// Nether Wart
		IFarmTypeBuilder infernal = farming.createFarmType(ForestryFarmTypes.INFERNAL, FarmLogicInfernal::new, new ItemStack(Items.NETHER_WART))
				.setFertilizerConsumption(20)
				.setWaterConsumption(0)
				.addSoil(Blocks.SOUL_SAND);
		infernal.addFarmable(new FarmableAgingCrop(Items.NETHER_WART, Blocks.NETHER_WART, NetherWartBlock.AGE, 3));

		// Sugarcane
		IFarmTypeBuilder poales = farming.createFarmType(ForestryFarmTypes.POALES, FarmLogicReeds::new, new ItemStack(Items.SUGAR_CANE))
				.setFertilizerConsumption(10)
				.setWaterConsumption(hydrationModifier -> (int) (20 * hydrationModifier))
				.addSoil(Blocks.SAND)
				.addSoil(Blocks.DIRT);
		poales.addFarmable(new FarmableStacked(new ItemStack(Items.SUGAR_CANE), Blocks.SUGAR_CANE, 3));

		// Cactus
		IFarmTypeBuilder cactus = farming.createFarmType(ForestryFarmTypes.SUCCULENTES, FarmLogicSucculent::new, new ItemStack(Items.GREEN_DYE))
				.setFertilizerConsumption(10)
				.setWaterConsumption(1)
				.addSoil(Blocks.SAND);
		cactus.addFarmable(new FarmableStacked(new ItemStack(Blocks.CACTUS), Blocks.CACTUS, 3));

		// Chorus Fruit
		IFarmTypeBuilder ender = farming.createFarmType(ForestryFarmTypes.ENDER, FarmLogicEnder::new, new ItemStack(Items.ENDER_EYE))
				.setFertilizerConsumption(20)
				.setWaterConsumption(0)
				.addSoil(Blocks.END_STONE);
		ender.addFarmable(FarmableChorus.INSTANCE);

		// Peat (???)
		IFarmTypeBuilder peat = farming.createFarmType(ForestryFarmTypes.PEAT, FarmLogicPeat::new, CoreItems.PEAT.stack())
				.setWaterConsumption((hydrationModifier) -> (int) (20 * hydrationModifier))
				.setFertilizerConsumption(2)
				.addSoil(CoreBlocks.BOG_EARTH.stack(), CoreBlocks.BOG_EARTH.defaultState())
				.addProducts(List.of(CoreItems.PEAT.stack(), new ItemStack(Blocks.DIRT)));

		// Fruit Trees
		IFarmTypeBuilder orchard = farming.createFarmType(ForestryFarmTypes.ORCHARD, FarmLogicOrchard::new, CoreItems.FRUITS.stack(ItemFruit.EnumFruit.CHERRY))
				.setFertilizerConsumption(10)
				.setWaterConsumption(hydrationModifier -> (int) (40 * hydrationModifier));
		for (ITreeSpecies species : SpeciesUtil.TREE_TYPE.get().getAllSpecies()) {
			IValueAllele<IFruit> fruitAllele = species.getDefaultGenome().getActiveAllele(TreeChromosomes.FRUIT);

			if (fruitAllele != ForestryAlleles.FRUIT_NONE) {
				IFruit fruit = fruitAllele.value();
				orchard.addGermling(species.createStack(TreeLifeStage.SAPLING))
						.addProducts(fruit.getProducts().stream().map(IProduct::createStack).toList())
						.addProducts(fruit.getSpecialty().stream().map(IProduct::createStack).toList());
			}
		}

		/*BlockState plantedBrownMushroom = FarmingBlocks.MUSHROOM.with(BlockMushroom.VARIANT, BlockMushroom.MushroomType.BROWN);
		registry.registerFarmables(ForestryFarmIdentifier.SHROOM, new FarmableVanillaMushroom(new ItemStack(Blocks.BROWN_MUSHROOM), plantedBrownMushroom, Blocks.BROWN_MUSHROOM_BLOCK));

		BlockState plantedRedMushroom = FarmingBlocks.MUSHROOM.with(BlockMushroom.VARIANT, BlockMushroom.MushroomType.RED);
		registry.registerFarmables(ForestryFarmIdentifier.SHROOM, new FarmableVanillaMushroom(new ItemStack(Blocks.RED_MUSHROOM), plantedRedMushroom, Blocks.RED_MUSHROOM_BLOCK));*/

		// Cocoa
		IFarmTypeBuilder cocoa = farming.createFarmType(ForestryFarmTypes.COCOA, FarmLogicCocoa::new, new ItemStack(Items.COCOA_BEANS))
				.setFertilizerConsumption(120)
				.setWaterConsumption(hydrationModifier -> (int) (20 * hydrationModifier))
				.addGermling(new ItemStack(Items.COCOA_BEANS))
				.addProduct(new ItemStack(Items.COCOA_BEANS));
	}

	private static void addGourdFarmables(IFarmTypeBuilder gourd) {
		gourd.addFarmable(new FarmableGourd(new ItemStack(Items.PUMPKIN_SEEDS), Blocks.PUMPKIN_STEM, Blocks.PUMPKIN));
		gourd.addFarmable(new FarmableGourd(new ItemStack(Items.MELON_SEEDS), Blocks.MELON_STEM, Blocks.MELON));
	}

	private static void addTreeFarmables(IFarmTypeBuilder arboreal) {
		arboreal.addWindfallFarmable(Items.OAK_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(List.of(Items.APPLE, Items.STICK)));
		arboreal.addWindfallFarmable(Items.BIRCH_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		arboreal.addWindfallFarmable(Items.SPRUCE_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		arboreal.addWindfallFarmable(Items.JUNGLE_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(List.of(Items.STICK, Items.COCOA_BEANS)));
		arboreal.addWindfallFarmable(Items.DARK_OAK_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		arboreal.addWindfallFarmable(Items.ACACIA_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		//arboreal.addWindfallFarmable(Items.AZALEA, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		//arboreal.addWindfallFarmable(Items.FLOWERING_AZALEA, FarmableSapling::new, builder -> builder.addWindfall(List.of(Items.STICK, Items.AZALEA)));
		arboreal.addWindfallFarmable(Items.MANGROVE_PROPAGULE, FarmableMangroveTree::new, builder -> builder.addWindfall(List.of(Items.STICK, Items.MOSS_CARPET)));
		arboreal.addWindfallFarmable(Items.CHERRY_SAPLING, FarmableSapling::new, builder -> builder.addWindfall(Items.STICK));
		arboreal.addFarmable(new FarmableGE());
	}

	private static void addCropFarmables(IFarmTypeBuilder crops) {
		crops.addFarmable(new FarmableAgingCrop(Items.WHEAT_SEEDS, Blocks.WHEAT, new ItemStack(Items.WHEAT), CropBlock.AGE, 7, 0));
		crops.addFarmable(new FarmableAgingCrop(Items.POTATO, Blocks.POTATOES, new ItemStack(Items.POTATO), CropBlock.AGE, 7, 0));
		crops.addFarmable(new FarmableAgingCrop(Items.CARROT, Blocks.CARROTS, new ItemStack(Items.CARROT), CropBlock.AGE, 7, 0));
		crops.addFarmable(new FarmableAgingCrop(Items.BEETROOT_SEEDS, Blocks.BEETROOTS, new ItemStack(Items.BEETROOT), BeetrootBlock.AGE, 3, 0));
	}
}

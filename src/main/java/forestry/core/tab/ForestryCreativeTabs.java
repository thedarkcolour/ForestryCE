package forestry.core.tab;

import java.util.Collection;

import net.minecraft.nbt.ByteTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.IWoodAccess;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.ILifeStage;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.blocks.BlockHiveType;
import forestry.apiculture.blocks.NaturalistChestBlockType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.ItemCreativeHiveFrame;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.WoodAccess;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.core.blocks.BlockTypeCoreTesr;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.fluids.ForestryFluids;
import forestry.core.items.definitions.EnumContainerType;
import forestry.core.items.definitions.FluidHandlerItemForestry;
import forestry.core.utils.SpeciesUtil;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.features.CultivationBlocks;
import forestry.energy.features.EnergyBlocks;
import forestry.factory.blocks.BlockTypeFactoryPlain;
import forestry.factory.blocks.BlockTypeFactoryTesr;
import forestry.factory.features.FactoryBlocks;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.features.FarmingBlocks;
import forestry.mail.blocks.BlockTypeMail;
import forestry.mail.features.MailBlocks;
import forestry.mail.features.MailItems;
import forestry.mail.items.ItemLetter;
import forestry.modules.features.FeatureCreativeTab;
import forestry.modules.features.FeatureItem;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.sorting.features.SortingBlocks;
import forestry.storage.features.BackpackItems;
import forestry.storage.features.CrateItems;
import forestry.storage.items.ItemCrated;
import forestry.worktable.features.WorktableBlocks;

@FeatureProvider
public class ForestryCreativeTabs {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureCreativeTab FORESTRY = REGISTRY.creativeTab(ForestryConstants.MOD_ID, tab -> {
		tab.icon(CoreItems.PORTABLE_ALYZER::stack);
		tab.displayItems(ForestryCreativeTabs::addForestryItems);
		tab.withTabsBefore(CreativeModeTabs.SPAWN_EGGS);
		tab.withTabsAfter(ForestryCreativeTabs.STORAGE.getKey(), ForestryCreativeTabs.APICULTURE.getKey(), ForestryCreativeTabs.ARBORICULTURE.getKey(), ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
	});
	public static final FeatureCreativeTab APICULTURE = REGISTRY.creativeTab("apiculture", tab -> {
		tab.icon(() -> SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.FOREST, BeeLifeStage.QUEEN));
		tab.displayItems(ForestryCreativeTabs::addApicultureItems);
		tab.withTabsBefore(ForestryCreativeTabs.FORESTRY.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.ARBORICULTURE.getKey());
	});
	public static final FeatureCreativeTab ARBORICULTURE = REGISTRY.creativeTab("arboriculture", tab -> {
		tab.icon(() -> SpeciesUtil.TREE_TYPE.get().createStack(ForestryTreeSpecies.OAK, TreeLifeStage.SAPLING));
		tab.withTabsBefore(ForestryCreativeTabs.APICULTURE.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
		tab.displayItems(ForestryCreativeTabs::addArboricultureItems);
	});
	public static final FeatureCreativeTab LEPIDOPTEROLOGY = REGISTRY.creativeTab("lepidopterology", tab -> {
		tab.icon(() -> SpeciesUtil.BUTTERFLY_TYPE.get().createStack(ForestryButterflySpecies.MONARCH, ButterflyLifeStage.BUTTERFLY));
		tab.displayItems(ForestryCreativeTabs::addLepidopterologyItems);
		tab.withTabsBefore(ForestryCreativeTabs.ARBORICULTURE.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.AGRICULTURE.getKey());
	});
	public static final FeatureCreativeTab AGRICULTURE = REGISTRY.creativeTab("agriculture", tab -> {
		tab.icon(() -> CultivationBlocks.MANAGED_PLANTER.stack(BlockTypePlanter.ARBORETUM));
		tab.displayItems(ForestryCreativeTabs::addAgricultureItems);
		tab.withTabsBefore(ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.STORAGE.getKey());
	});
	public static final FeatureCreativeTab STORAGE = REGISTRY.creativeTab("storage", tab -> {
		tab.icon(BackpackItems.MINER_BACKPACK::stack);
		tab.displayItems(ForestryCreativeTabs::addStorageItems);
		tab.withTabsBefore(ForestryCreativeTabs.AGRICULTURE.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.MAIL.getKey());
	});
	public static final FeatureCreativeTab MAIL = REGISTRY.creativeTab("mail", tab -> {
		tab.icon(() -> MailBlocks.BASE.stack(BlockTypeMail.MAILBOX));
		tab.displayItems(ForestryCreativeTabs::addMailItems);
		tab.withTabsBefore(ForestryCreativeTabs.STORAGE.getKey());
	});

	private static void addForestryItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics tools
		addGeneticBasics(items);
		items.accept(CoreItems.FORESTERS_MANUAL);
		items.accept(ApicultureItems.SCOOP);
		items.accept(ApicultureItems.SMOKER);
		items.accept(ArboricultureItems.GRAFTER);
		items.accept(ArboricultureItems.GRAFTER_PROVEN);
		items.accept(CoreItems.SPECTACLES);
		items.accept(SortingBlocks.FILTER);

		// Storages
		items.accept(BackpackItems.APIARIST_BACKPACK);
		items.accept(BackpackItems.ARBORIST_BACKPACK);
		items.accept(BackpackItems.LEPIDOPTERIST_BACKPACK);
		CoreBlocks.NATURALIST_CHEST.getItems().forEach(items::accept);

		// Machine tools
		items.accept(CoreItems.WRENCH);
		items.accept(CoreItems.PIPETTE);
		items.accept(CoreItems.SOLDERING_IRON);
		items.accept(WorktableBlocks.WORKTABLE);
		// Engines
		EnergyBlocks.ENGINES.getItems().forEach(items::accept);
		// Machines
		FactoryBlocks.TESR.getItems().forEach(items::accept);
		// Circuit boards
		items.accept(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.FABRICATOR));
		CoreItems.CIRCUITBOARDS.getItems().forEach(items::accept);
		CoreItems.ELECTRON_TUBES.getItems().forEach(items::accept);

		// Ores
		items.accept(CoreBlocks.APATITE_ORE);
		items.accept(CoreBlocks.DEEPSLATE_APATITE_ORE);
		items.accept(CoreBlocks.TIN_ORE);
		items.accept(CoreBlocks.DEEPSLATE_TIN_ORE);
		// Raw Ores
		items.accept(CoreItems.APATITE);
		items.accept(CoreItems.RAW_TIN);
		// Processed ores
		items.accept(CoreItems.FERTILIZER_COMPOUND);
		items.accept(CoreItems.INGOT_TIN);
		items.accept(CoreItems.INGOT_BRONZE);
		// Block forms
		items.accept(CoreBlocks.RAW_TIN_BLOCK);
		CoreBlocks.RESOURCE_STORAGE.getItems().forEach(items::accept);
		items.accept(CharcoalBlocks.CHARCOAL);
		// Gears
		items.accept(CoreItems.GEAR_COPPER);
		items.accept(CoreItems.GEAR_TIN);
		items.accept(CoreItems.GEAR_BRONZE);
		// Casings
		items.accept(CoreItems.STURDY_CASING);
		items.accept(CoreItems.HARDENED_CASING);
		items.accept(CoreItems.IMPREGNATED_CASING);
		items.accept(CoreItems.FLEXIBLE_CASING);

		items.accept(CoreItems.FERTILIZER_COMPOUND);
		items.accept(CoreItems.CARTON);
		items.accept(CoreItems.BRONZE_PICKAXE);
		items.accept(CoreItems.BRONZE_SHOVEL);
		items.accept(CoreItems.KIT_PICKAXE);
		items.accept(CoreItems.KIT_SHOVEL);
		items.accept(CoreItems.GEAR_TIN);
		items.accept(CoreItems.GEAR_COPPER);
		items.accept(CoreItems.GEAR_BRONZE);
		items.accept(CoreItems.SOLDERING_IRON);
		items.accept(CoreItems.SPECTACLES);
		items.accept(CoreItems.ASH);
		items.accept(CoreItems.PEAT);
		items.accept(CoreItems.BITUMINOUS_PEAT);
		// todo merge more items into crafting materials
		CoreItems.CRAFTING_MATERIALS.getItems().forEach(items::accept);
	}

	private static void addApicultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics
		addGeneticBasics(items);
		items.accept(BackpackItems.APIARIST_BACKPACK);
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));

		// Gear
		items.accept(ApicultureItems.SCOOP);
		items.accept(CoreItems.SPECTACLES);
		items.accept(ApicultureItems.APIARIST_HELMET);
		items.accept(ApicultureItems.APIARIST_CHEST);
		items.accept(ApicultureItems.APIARIST_LEGS);
		items.accept(ApicultureItems.APIARIST_BOOTS);

		// Hives
		ApicultureBlocks.BASE.getItems().forEach(items::accept);
		for (BlockHiveType type : BlockHiveType.values()) {
			if (type != BlockHiveType.SWARM) {
				items.accept(ApicultureBlocks.BEEHIVE.get(type));
			}
		}

		// Alveary
		ApicultureBlocks.ALVEARY.getItems().forEach(items::accept);

		// Frames
		items.accept(ApicultureItems.FRAME_UNTREATED);
		items.accept(ApicultureItems.FRAME_IMPREGNATED);
		items.accept(ApicultureItems.FRAME_PROVEN);
		ItemStack creativeFrameMaxMutation = ApicultureItems.FRAME_CREATIVE.stack();
		creativeFrameMaxMutation.addTagElement(ItemCreativeHiveFrame.NBT_FORCE_MUTATIONS, ByteTag.valueOf((byte) 1));
		items.accept(ApicultureItems.FRAME_CREATIVE);
		items.accept(creativeFrameMaxMutation);

		// Food
		items.accept(ApicultureItems.HONEYED_SLICE);
		items.accept(ApicultureItems.AMBROSIA);
		items.accept(ApicultureItems.HONEY_POT);

		// Misc items
		ApicultureItems.BEE_COMBS.getItems().forEach(items::accept);
		ApicultureBlocks.BEE_COMB.getItems().forEach(items::accept);
		ApicultureItems.PROPOLIS.getItems().forEach(items::accept);
		ApicultureItems.POLLEN_CLUSTER.getItems().forEach(items::accept);
		items.accept(ApicultureItems.ROYAL_JELLY);

		for (ILifeStage stage : SpeciesUtil.BEE_TYPE.get().getLifeStages()) {
			for (IBeeSpecies species : SpeciesUtil.getAllBeeSpecies()) {
				items.accept(species.createStack(stage));
			}
		}
	}

	private static void addArboricultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics
		addGeneticBasics(items);
		items.accept(BackpackItems.ARBORIST_BACKPACK);
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));

		// Gear
		items.accept(CoreItems.SPECTACLES);
		items.accept(ArboricultureItems.GRAFTER);
		items.accept(ArboricultureItems.GRAFTER_PROVEN);

		// Fruits
		CoreItems.FRUITS.getItems().forEach(items::accept);

		// Blocks
		items.accept(CharcoalBlocks.LOG_PILE);
		items.accept(CharcoalBlocks.DECORATIVE_LOG_PILE);
		items.accept(CoreItems.ASH);
		IWoodAccess access = WoodAccess.INSTANCE;
		for (IWoodType type : access.getRegisteredWoodTypes()) {
			addAllWoodBlocks(items, access, type, false);
		}
		for (IWoodType type : access.getRegisteredWoodTypes()) {
			addAllWoodBlocks(items, access, type, true);
		}

		for (ForestryWoodType type : ForestryWoodType.VALUES) {
			items.accept(ArboricultureItems.BOAT.item(type));
			items.accept(ArboricultureItems.CHEST_BOAT.item(type));
			items.accept(ArboricultureBlocks.SIGN.get(type));
			items.accept(ArboricultureBlocks.HANGING_SIGN.get(type));
		}

		// Specimens
		Collection<ILifeStage> stages = SpeciesUtil.TREE_TYPE.get().getLifeStages();

		for (ILifeStage stage : stages) {
			for (ITreeSpecies species : SpeciesUtil.getAllTreeSpecies()) {
				items.accept(species.createStack(stage));
			}
		}

		ArboricultureBlocks.LEAVES_DECORATIVE.getItems().forEach(items::accept);
	}

	private static void addLepidopterologyItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics
		addGeneticBasics(items);
		items.accept(BackpackItems.LEPIDOPTERIST_BACKPACK);
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));

		// Gear
		items.accept(ApicultureItems.SCOOP);

		// Specimens
		Collection<ILifeStage> stages = SpeciesUtil.BUTTERFLY_TYPE.get().getLifeStages();
		for (ILifeStage stage : stages) {
			for (IButterflySpecies species : SpeciesUtil.getAllButterflySpecies()) {
				items.accept(species.createStack(stage));
			}
		}
	}

	private static void addAgricultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Machine tools
		items.accept(CoreItems.WRENCH);
		items.accept(CoreItems.PIPETTE);
		items.accept(CoreItems.SOLDERING_IRON);

		// Circuit boards
		CoreItems.CIRCUITBOARDS.getItems().forEach(items::accept);
		CoreItems.ELECTRON_TUBES.getItems().forEach(items::accept);

		// Engines
		EnergyBlocks.ENGINES.getItems().forEach(items::accept);
		// Machines
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CENTRIFUGE));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.FERMENTER));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.MOISTENER));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.SQUEEZER));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.STILL));
		items.accept(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.RAINTANK));

		// Rainmaker
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.RAINMAKER));
		items.accept(CoreItems.IODINE_CHARGE);
		items.accept(CoreItems.DISSIPATION_CHARGE);

		// Misc items
		items.accept(CoreItems.PEAT);
		items.accept(CoreItems.BITUMINOUS_PEAT);
		items.accept(CoreBlocks.HUMUS);
		items.accept(CoreBlocks.BOG_EARTH);
		items.accept(CoreItems.COMPOST);
		items.accept(CoreItems.MOULDY_WHEAT);
		items.accept(CoreItems.DECAYING_WHEAT);
		items.accept(CoreItems.MULCH);

		// Multi farm
		for (EnumFarmMaterial material : EnumFarmMaterial.values()) {
			for (EnumFarmBlockType type : EnumFarmBlockType.values()) {
				items.accept(FarmingBlocks.FARM.stack(type, material));
			}
		}

		// Single farm (boo)
		for (BlockTypePlanter type : BlockTypePlanter.values()) {
			items.accept(CultivationBlocks.MANAGED_PLANTER.stack(type));
			items.accept(CultivationBlocks.MANUAL_PLANTER.stack(type));
		}
	}

	private static void addStorageItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics backpacks
		items.accept(BackpackItems.APIARIST_BACKPACK);
		items.accept(BackpackItems.ARBORIST_BACKPACK);
		items.accept(BackpackItems.LEPIDOPTERIST_BACKPACK);

		// T1
		items.accept(BackpackItems.MINER_BACKPACK);
		items.accept(BackpackItems.DIGGER_BACKPACK);
		items.accept(BackpackItems.FORESTER_BACKPACK);
		items.accept(BackpackItems.HUNTER_BACKPACK);
		items.accept(BackpackItems.ADVENTURER_BACKPACK);
		items.accept(BackpackItems.BUILDER_BACKPACK);

		// T2
		items.accept(BackpackItems.MINER_BACKPACK_T_2);
		items.accept(BackpackItems.DIGGER_BACKPACK_T_2);
		items.accept(BackpackItems.FORESTER_BACKPACK_T_2);
		items.accept(BackpackItems.HUNTER_BACKPACK_T_2);
		items.accept(BackpackItems.ADVENTURER_BACKPACK_T_2);
		items.accept(BackpackItems.BUILDER_BACKPACK_T_2);

		// Packing machines
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.BOTTLER));
		items.accept(FactoryBlocks.TESR.get(BlockTypeFactoryTesr.CARPENTER));

		// Misc gear
		items.accept(CoreItems.PIPETTE);
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));
		items.accept(SortingBlocks.FILTER);

		// Empty containers
		items.accept(CoreItems.CARTON);
		FluidsItems.CONTAINERS.getItems().forEach(items::accept);
		items.accept(CrateItems.CRATE);

		// Filled cartons
		items.accept(CoreItems.KIT_PICKAXE);
		items.accept(CoreItems.KIT_SHOVEL);

		// Filled containers
		for (EnumContainerType type : EnumContainerType.values()) {
			for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
				if (fluid instanceof FlowingFluid flowing && flowing.getSource() != fluid) {
					continue;
				}

				ItemStack itemStack = FluidsItems.CONTAINERS.stack(type);

				IFluidHandlerItem fluidHandler = new FluidHandlerItemForestry(itemStack, type);
				if (fluidHandler.fill(new FluidStack(fluid, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE) == FluidType.BUCKET_VOLUME) {
					ItemStack filled = fluidHandler.getContainer();
					items.accept(filled);
				}
			}
		}

		// Filled buckets
		for (ForestryFluids type : ForestryFluids.values()) {
			items.accept(type.getBucket());
		}

		// Filled crates
		for (FeatureItem<ItemCrated> crate : CrateItems.getCrates()) {
			items.accept(crate);
		}
	}

	private static void addMailItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		MailBlocks.BASE.getItems().forEach(items::accept);
		items.accept(MailItems.CATALOGUE);
		MailItems.STAMPS.getItems().forEach(items::accept);
		MailItems.LETTERS.getColumnFeatures(ItemLetter.State.FRESH).forEach(items::accept);
	}

	private static void addAllWoodBlocks(CreativeModeTab.Output items, IWoodAccess access, IWoodType type, boolean fireproof) {
		items.accept(access.getStack(type, WoodBlockKind.LOG, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.WOOD, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.STRIPPED_LOG, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.STRIPPED_WOOD, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.PLANKS, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.STAIRS, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.SLAB, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.FENCE, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.FENCE_GATE, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.DOOR, fireproof));
		// one day...
		items.accept(access.getStack(type, WoodBlockKind.TRAPDOOR, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.PRESSURE_PLATE, fireproof));
		items.accept(access.getStack(type, WoodBlockKind.BUTTON, fireproof));
	}

	private static void addGeneticBasics(CreativeModeTab.Output items) {
		items.accept(CoreItems.PORTABLE_ALYZER);
		items.accept(ApicultureItems.HONEY_DROP);
		items.accept(ApicultureItems.HONEYDEW);
		items.accept(CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE));
		items.accept(CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER));
	}
}

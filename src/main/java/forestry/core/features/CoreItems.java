package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.content.resources.ItemBeesWax;
import forestry.core.content.tools.ItemScoop;
import forestry.core.content.resources.ItemRefractoryWax;
import forestry.core.engine.circuits.EnumCircuitBoardType;
import forestry.core.engine.circuits.ItemCircuitBoard;
import forestry.core.engine.genetics.ItemResearchNote;
import forestry.core.platform.item.*;
import forestry.core.content.tools.*;
import forestry.core.content.analyzer.*;
import forestry.core.content.resources.*;
import forestry.core.content.resources.EnumCraftingMaterial;
import forestry.core.content.resources.EnumElectronTube;
import forestry.core.content.tools.ToolTier;
import forestry.core.platform.registration.*;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemContainerContents;

@FeatureProvider
public class CoreItems {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	/* Forester's Manual */
	public static final FeatureItem<ForestersManualItem> FORESTERS_MANUAL = REGISTRY.item(ForestersManualItem::new, "foresters_manual");

	/* Fertilizer */
	public static final FeatureItem<ItemFertilizer> COMPOST = REGISTRY.item(ItemFertilizer::new, "compost");
	public static final FeatureItem<ItemFertilizer> FERTILIZER_COMPOUND = REGISTRY.item(ItemFertilizer::new, "fertilizer");

	/* Gems and raw ores */
	public static final FeatureItem<ItemForestry> APATITE = REGISTRY.item(ItemForestry::new, "apatite");
	public static final FeatureItem<ItemForestry> RAW_TIN = REGISTRY.item(ItemForestry::new, "raw_tin");
	public static final FeatureItem<ItemForestry> AMBER = REGISTRY.item(ItemForestry::new, "amber");
	public static final FeatureItem<ItemForestry> SILICON = REGISTRY.item(ItemForestry::new, "silicon");

	/* Research */
	public static final FeatureItem<ItemResearchNote> RESEARCH_NOTE = REGISTRY.item(ItemResearchNote::new, "research_note");

	/* Alyzer */
	public static final FeatureItem<PortableAnalyzerItem> PORTABLE_ANALYZER = REGISTRY.item(PortableAnalyzerItem::new, () -> new Item.Properties()
		.stacksTo(1)
		.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
		.component(CoreDataComponents.ALYZER_CHARGES.get(), 0), "portable_analyzer");

	/* Ingots */
	public static final FeatureItem<ItemForestry> TIN_INGOT = REGISTRY.item(ItemForestry::new, "tin_ingot");
	public static final FeatureItem<ItemForestry> BRONZE_INGOT = REGISTRY.item(ItemForestry::new, "bronze_ingot");

	/* Nuggets */
	public static final FeatureItem<ItemForestry> TIN_NUGGET = REGISTRY.item(ItemForestry::new, "tin_nugget");

	/* Tools */
	public static final FeatureItem<ItemWrench> WRENCH = REGISTRY.item(ItemWrench::new, "wrench");
	public static final FeatureItem<ItemPipette> PIPETTE = REGISTRY.item(ItemPipette::new, "pipette");

	/* Packaged Tools */
	public static final FeatureItem<ItemForestry> CARTON = REGISTRY.item(ItemForestry::new, "carton");
	// remnants of a broken Survivalist's tool, salvageable for a little bronze
	public static final FeatureItem<ItemForestry> BROKEN_SURVIVALISTS_PICKAXE = REGISTRY.item(ItemForestry::new, "broken_survivalists_pickaxe");
	public static final FeatureItem<ItemForestry> BROKEN_SURVIVALISTS_SHOVEL = REGISTRY.item(ItemForestry::new, "broken_survivalists_shovel");
	public static final FeatureItem<ItemForestry> BROKEN_SURVIVALISTS_AXE = REGISTRY.item(ItemForestry::new, "broken_survivalists_axe");
	public static final FeatureItem<ItemForestry> BROKEN_SURVIVALISTS_SWORD = REGISTRY.item(ItemForestry::new, "broken_survivalists_sword");
	public static final FeatureItem<ItemForestry> BROKEN_SURVIVALISTS_HOE = REGISTRY.item(ItemForestry::new, "broken_survivalists_hoe");
	public static final FeatureItem<PickaxeItem> SURVIVALISTS_PICKAXE = REGISTRY.item(() -> new HasRemnants.Pickaxe(ToolTier.SURVIVALIST, 1, -2.8f, new Item.Properties(), BROKEN_SURVIVALISTS_PICKAXE::stack), "survivalists_pickaxe");
	public static final FeatureItem<ShovelItem> SURVIVALISTS_SHOVEL = REGISTRY.item(() -> new HasRemnants.Shovel(ToolTier.SURVIVALIST, 1.5f, -3.0f, new Item.Properties(), BROKEN_SURVIVALISTS_SHOVEL::stack), "survivalists_shovel");
	public static final FeatureItem<AxeItem> SURVIVALISTS_AXE = REGISTRY.item(() -> new HasRemnants.Axe(ToolTier.SURVIVALIST, 5.5f, -3.2f, new Item.Properties(), BROKEN_SURVIVALISTS_AXE::stack), "survivalists_axe");
	public static final FeatureItem<SwordItem> SURVIVALISTS_SWORD = REGISTRY.item(() -> new HasRemnants.Sword(ToolTier.SURVIVALIST, 3, -2.4f, new Item.Properties(), BROKEN_SURVIVALISTS_SWORD::stack), "survivalists_sword");
	public static final FeatureItem<HoeItem> SURVIVALISTS_HOE = REGISTRY.item(() -> new HasRemnants.Hoe(ToolTier.SURVIVALIST, -2, -1.0f, new Item.Properties(), BROKEN_SURVIVALISTS_HOE::stack), "survivalists_hoe");
	public static final FeatureItem<ItemAssemblyKit> SHOVEL_KIT = REGISTRY.item(() -> new ItemAssemblyKit(SURVIVALISTS_SHOVEL::stack), "shovel_kit");
	public static final FeatureItem<ItemAssemblyKit> PICKAXE_KIT = REGISTRY.item(() -> new ItemAssemblyKit(SURVIVALISTS_PICKAXE::stack), "pickaxe_kit");
	public static final FeatureItem<ItemAssemblyKit> AXE_KIT = REGISTRY.item(() -> new ItemAssemblyKit(SURVIVALISTS_AXE::stack), "axe_kit");
	public static final FeatureItem<ItemAssemblyKit> SWORD_KIT = REGISTRY.item(() -> new ItemAssemblyKit(SURVIVALISTS_SWORD::stack), "sword_kit");
	public static final FeatureItem<ItemAssemblyKit> HOE_KIT = REGISTRY.item(() -> new ItemAssemblyKit(SURVIVALISTS_HOE::stack), "hoe_kit");

	/* Machine Parts */
	public static final FeatureItem<ItemForestry> STURDY_CASING = REGISTRY.item(ItemForestry::new, "sturdy_casing");
	public static final FeatureItem<ItemForestry> HARDENED_CASING = REGISTRY.item(ItemForestry::new, "hardened_casing");
	public static final FeatureItem<ItemForestry> IMPREGNATED_CASING = REGISTRY.item(ItemForestry::new, "impregnated_casing");
	public static final FeatureItem<ItemForestry> FLEXIBLE_CASING = REGISTRY.item(ItemForestry::new, "flexible_casing");
	public static final FeatureItem<ItemForestry> GEAR_BRONZE = REGISTRY.item(ItemForestry::new, "bronze_gear");
	public static final FeatureItem<ItemForestry> GEAR_COPPER = REGISTRY.item(ItemForestry::new, "copper_gear");
	public static final FeatureItem<ItemForestry> GEAR_TIN = REGISTRY.item(ItemForestry::new, "tin_gear");
	// Deviation from 1.20.1: the id follows the target's <material>_gear naming, so "gear_iron" became "iron_gear"
	public static final FeatureItem<ItemForestry> GEAR_IRON = REGISTRY.item(ItemForestry::new, "iron_gear");
	public static final FeatureItem<ItemForestry> SOLAR_CELL = REGISTRY.item(ItemForestry::new, "solar_cell");

	/* Soldering */
	public static final FeatureItem<SolderingIronItem> SOLDERING_IRON = REGISTRY.item(SolderingIronItem::new, () -> new Item.Properties()
		.durability(5)
		.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY), "soldering_iron");
	public static final FeatureItemGroup<ItemCircuitBoard, EnumCircuitBoardType> CIRCUITBOARDS = REGISTRY.itemGroup(ItemCircuitBoard::new, EnumCircuitBoardType.values()).identifier("circuit_board", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureItemGroup<ItemElectronTube, EnumElectronTube> ELECTRON_TUBES = REGISTRY.itemGroup(ItemElectronTube::new, EnumElectronTube.values()).identifier(type -> switch (type) {
		case GOLD -> "golden_electron_tube";
		case DIAMOND -> "diamantine_electron_tube";
		case APATITE -> "apatine_electron_tube";
		case BLAZE -> "blazing_electron_tube";
		default -> type.getSerializedName() + "_electron_tube";
	}).create();

	/* Armor */
	public static final FeatureItem<ItemSpectacles> SPECTACLES = REGISTRY.item(ItemSpectacles::new, "spectacles");

	/* Peat */
	public static final FeatureItem<ItemForestry> PEAT = REGISTRY.item(ItemForestry::new, "peat");
	public static final FeatureItem<ItemForestry> ASH = REGISTRY.item(ItemForestry::new, "ash");
	// Deviation from 1.20.1: the id follows this tree's <material>_<form> naming, so "brick_ash" became "ash_brick"
	public static final FeatureItem<ItemForestry> ASH_BRICK = REGISTRY.item(ItemForestry::new, "ash_brick");
	public static final FeatureItem<ItemForestry> BITUMINOUS_PEAT = REGISTRY.item(ItemForestry::new, "bituminous_peat");

	/* Moistener */
	public static final FeatureItem<ItemForestry> MOULDY_WHEAT = REGISTRY.item(ItemForestry::new, "mouldy_wheat");
	public static final FeatureItem<ItemForestry> DECAYING_WHEAT = REGISTRY.item(ItemForestry::new, "decaying_wheat");
	public static final FeatureItem<ItemFertilizer> MULCH = REGISTRY.item(ItemFertilizer::new, "mulch");

	/* Rainmaker */
	public static final FeatureItem<ItemForestry> IODINE_CHARGE = REGISTRY.item(ItemForestry::new, "iodine_capsule");
	public static final FeatureItem<ItemForestry> DISSIPATION_CHARGE = REGISTRY.item(ItemForestry::new, "dissipation_charge");

	/* Misc */
	// Deviation from 1.20.1: forestry.core.items.ItemProperties no longer exists, so this uses a plain Item.Properties.
	public static final FeatureItem<Item> PHOSPHOR_TORCH_ITEM = REGISTRY.item(() -> new StandingAndWallBlockItem(CoreBlocks.PHOSPHOR_TORCH.block(), CoreBlocks.PHOSPHOR_WALL_TORCH.block(), new Item.Properties(), Direction.DOWN), "phosphor_torch");
	public static final FeatureItemGroup<ItemCraftingMaterial, EnumCraftingMaterial> CRAFTING_MATERIALS = REGISTRY.itemGroup(ItemCraftingMaterial::new, EnumCraftingMaterial.values()).create();
	public static final FeatureItemGroup<ItemForestryFood, FruitItemType> FRUITS = REGISTRY
		.itemGroup(type -> new ItemForestryFood(type.heal, type.saturation, type.useTime), FruitItemType.values())
		.create();
	// moved out of apiculture: the scoop catches butterflies as well as bees, and the honey
	// drops are the Portable Analyzer's fuel, so all three are base concerns
	public static final FeatureItem<ItemScoop> SCOOP = REGISTRY.item(() -> new ItemScoop(10), "scoop");
	// Deviation from 1.20.1: the id follows the target's proven_<tool> naming, so "scoop_proven" became "proven_scoop"
	public static final FeatureItem<ItemScoop> PROVEN_SCOOP = REGISTRY.item(() -> new ItemScoop(160), "proven_scoop");
	public static final FeatureItem<Item> HONEY_DROP = REGISTRY.item("honey_drop");
	public static final FeatureItem<Item> HONEYDEW = REGISTRY.item("honeydew");
	public static final FeatureItem<ItemBeesWax> BEESWAX = REGISTRY.item(ItemBeesWax::new, "beeswax");
	public static final FeatureItem<ItemRefractoryWax> REFRACTORY_WAX = REGISTRY.item(ItemRefractoryWax::new, "refractory_wax");
	// Deviation from 1.20.1: ids follow this tree's <material>_<form> naming, so "brick_wax" became
	// "wax_brick" and "brick_refractory_wax" became "refractory_wax_brick"
	public static final FeatureItem<ItemForestry> WAX_BRICK = REGISTRY.item(ItemForestry::new, "wax_brick");
	public static final FeatureItem<ItemForestry> REFRACTORY_WAX_BRICK = REGISTRY.item(ItemForestry::new, "refractory_wax_brick");
}

package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.items.ItemBeesWax;
import forestry.apiculture.items.ItemRefractoryWax;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.circuits.ItemCircuitBoard;
import forestry.core.genetics.ItemResearchNote;
import forestry.core.items.*;
import forestry.core.items.definitions.EnumCraftingMaterial;
import forestry.core.items.definitions.EnumElectronTube;
import forestry.core.items.definitions.ToolTier;
import forestry.modules.features.*;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

@FeatureProvider
public class CoreItems {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	/* Foresters' Manual */
	public static final FeatureItem<ForestersManualItem> FORESTERS_MANUAL = REGISTRY.item(ForestersManualItem::new, "foresters_manual");

	/* Fertilizer */
	public static final FeatureItem<ItemForestry> COMPOST = REGISTRY.item(ItemForestry::new, "fertilizer_bio");
	public static final FeatureItem<ItemFertilizer> FERTILIZER_COMPOUND = REGISTRY.item(ItemFertilizer::new, "fertilizer_compound");

	/* Gems and raw ores */
	public static final FeatureItem<ItemForestry> APATITE = REGISTRY.item(ItemForestry::new, "apatite");
	public static final FeatureItem<ItemForestry> RAW_TIN = REGISTRY.item(ItemForestry::new, "raw_tin");

	/* Research */
	public static final FeatureItem<ItemResearchNote> RESEARCH_NOTE = REGISTRY.item(ItemResearchNote::new, "research_note");

	/* Alyzer */
	public static final FeatureItem<ItemAlyzer> PORTABLE_ALYZER = REGISTRY.item(ItemAlyzer::new, "portable_alyzer");

	/* Ingots */
	public static final FeatureItem<ItemForestry> INGOT_TIN = REGISTRY.item(ItemForestry::new, "ingot_tin");
	public static final FeatureItem<ItemForestry> INGOT_BRONZE = REGISTRY.item(ItemForestry::new, "ingot_bronze");

	/* Tools */
	public static final FeatureItem<ItemWrench> WRENCH = REGISTRY.item(ItemWrench::new, "wrench");
	public static final FeatureItem<ItemPipette> PIPETTE = REGISTRY.item(ItemPipette::new, "pipette");

	/* Packaged Tools */
	public static final FeatureItem<ItemForestry> CARTON = REGISTRY.item(ItemForestry::new, "carton");
	// todo change IDs and names in 1.21.1
	public static final FeatureItem<ItemForestry> BROKEN_BRONZE_PICKAXE = REGISTRY.item(ItemForestry::new, "broken_bronze_pickaxe");
	public static final FeatureItem<ItemForestry> BROKEN_BRONZE_SHOVEL = REGISTRY.item(ItemForestry::new, "broken_bronze_shovel");
	public static final FeatureItem<ItemForestry> BROKEN_BRONZE_AXE = REGISTRY.item(ItemForestry::new, "broken_axe");
	public static final FeatureItem<ItemForestry> BROKEN_BRONZE_SWORD = REGISTRY.item(ItemForestry::new, "broken_sword");
	public static final FeatureItem<ItemForestry> BROKEN_BRONZE_HOE = REGISTRY.item(ItemForestry::new, "broken_hoe");
	public static final FeatureItem<PickaxeItem> BRONZE_PICKAXE = REGISTRY.item(() -> new HasRemnants.Pickaxe(ToolTier.SURVIVALIST, 1, -2.8f, new Item.Properties(), BROKEN_BRONZE_PICKAXE::stack), "bronze_pickaxe");
	public static final FeatureItem<ShovelItem> BRONZE_SHOVEL = REGISTRY.item(() -> new HasRemnants.Shovel(ToolTier.SURVIVALIST, 1.5f, -3.0f, new Item.Properties(), BROKEN_BRONZE_SHOVEL::stack), "bronze_shovel");
	public static final FeatureItem<AxeItem> BRONZE_AXE = REGISTRY.item(() -> new HasRemnants.Axe(ToolTier.SURVIVALIST, 5.5f, -3.2f, new Item.Properties(), BROKEN_BRONZE_AXE::stack), "bronze_axe");
	public static final FeatureItem<SwordItem> BRONZE_SWORD = REGISTRY.item(() -> new HasRemnants.Sword(ToolTier.SURVIVALIST, 3, -2.4f, new Item.Properties(), BROKEN_BRONZE_SWORD::stack), "bronze_sword");
	public static final FeatureItem<HoeItem> BRONZE_HOE = REGISTRY.item(() -> new HasRemnants.Hoe(ToolTier.SURVIVALIST, -2, -1.0f, new Item.Properties(), BROKEN_BRONZE_HOE::stack), "bronze_hoe");
	public static final FeatureItem<ItemAssemblyKit> KIT_SHOVEL = REGISTRY.item(() -> new ItemAssemblyKit(BRONZE_SHOVEL::stack), "kit_shovel");
	public static final FeatureItem<ItemAssemblyKit> KIT_PICKAXE = REGISTRY.item(() -> new ItemAssemblyKit(BRONZE_PICKAXE::stack), "kit_pickaxe");
	public static final FeatureItem<ItemAssemblyKit> KIT_AXE = REGISTRY.item(() -> new ItemAssemblyKit(BRONZE_AXE::stack), "axe_kit");
	public static final FeatureItem<ItemAssemblyKit> KIT_SWORD = REGISTRY.item(() -> new ItemAssemblyKit(BRONZE_SWORD::stack), "sword_kit");
	public static final FeatureItem<ItemAssemblyKit> KIT_HOE = REGISTRY.item(() -> new ItemAssemblyKit(BRONZE_HOE::stack), "hoe_kit");

	/* Machine Parts */
	public static final FeatureItem<ItemForestry> STURDY_CASING = REGISTRY.item(ItemForestry::new, "sturdy_machine");
	public static final FeatureItem<ItemForestry> HARDENED_CASING = REGISTRY.item(ItemForestry::new, "hardened_machine");
	public static final FeatureItem<ItemForestry> IMPREGNATED_CASING = REGISTRY.item(ItemForestry::new, "impregnated_casing");
	public static final FeatureItem<ItemForestry> FLEXIBLE_CASING = REGISTRY.item(ItemForestry::new, "flexible_casing");
	public static final FeatureItem<ItemForestry> GEAR_BRONZE = REGISTRY.item(ItemForestry::new, "gear_bronze");
	public static final FeatureItem<ItemForestry> GEAR_COPPER = REGISTRY.item(ItemForestry::new, "gear_copper");
	public static final FeatureItem<ItemForestry> GEAR_TIN = REGISTRY.item(ItemForestry::new, "gear_tin");

	/* Soldering */
	public static final FeatureItem<ItemSolderingIron> SOLDERING_IRON = REGISTRY.item(ItemSolderingIron::new, "soldering_iron");
	public static final FeatureItemGroup<ItemCircuitBoard, EnumCircuitBoardType> CIRCUITBOARDS = REGISTRY.itemGroup(ItemCircuitBoard::new, "circuit_board", EnumCircuitBoardType.values());
	public static final FeatureItemGroup<ItemElectronTube, EnumElectronTube> ELECTRON_TUBES = REGISTRY.itemGroup(ItemElectronTube::new, "electron_tube", EnumElectronTube.values());

	/* Armor */
	public static final FeatureItem<ItemSpectacles> SPECTACLES = REGISTRY.item(ItemSpectacles::new, "naturalist_helmet");

	/* Peat */
	public static final FeatureItem<ItemForestry> PEAT = REGISTRY.item(() -> new ItemForestry(new ItemProperties().burnTime(2000)), "peat");
	public static final FeatureItem<ItemForestry> ASH = REGISTRY.item(ItemForestry::new, "ash");
	public static final FeatureItem<ItemForestry> BITUMINOUS_PEAT = REGISTRY.item(() -> new ItemForestry(new ItemProperties().burnTime(4200)), "bituminous_peat");

	/* Moistener */
	public static final FeatureItem<ItemForestry> MOULDY_WHEAT = REGISTRY.item(ItemForestry::new, "mouldy_wheat");
	public static final FeatureItem<ItemForestry> DECAYING_WHEAT = REGISTRY.item(ItemForestry::new, "decaying_wheat");
	public static final FeatureItem<ItemForestry> MULCH = REGISTRY.item(ItemForestry::new, "mulch");

	/* Rainmaker */
	public static final FeatureItem<ItemForestry> IODINE_CHARGE = REGISTRY.item(ItemForestry::new, "iodine_capsule");
	public static final FeatureItem<ItemForestry> DISSIPATION_CHARGE = REGISTRY.item(ItemForestry::new, "dissipation_charge");

	/* Misc */
	public static final FeatureItemGroup<ItemCraftingMaterial, EnumCraftingMaterial> CRAFTING_MATERIALS = REGISTRY.itemGroup(ItemCraftingMaterial::new, EnumCraftingMaterial.values()).create();
	public static final FeatureItemGroup<ItemFruit, ItemFruit.EnumFruit> FRUITS = REGISTRY.itemGroup(ItemFruit::new, "fruit", ItemFruit.EnumFruit.values());
	public static final FeatureItem<ItemBeesWax> BEESWAX = REGISTRY.item(ItemBeesWax::new, "beeswax");
	public static final FeatureItem<ItemRefractoryWax> REFRACTORY_WAX = REGISTRY.item(ItemRefractoryWax::new, "refractory_wax");
}

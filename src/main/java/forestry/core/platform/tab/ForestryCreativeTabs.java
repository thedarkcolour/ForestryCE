package forestry.core.platform.tab;

import forestry.api.ForestryConstants;
import forestry.api.arboriculture.IWoodType;
import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.features.ApicultureItems;
import forestry.arboriculture.features.CharcoalBlocks;
import forestry.arboriculture.tab.ArboricultureCreativeTab;
import forestry.arboriculture.wood.WoodAccess;
import forestry.core.content.decorative.BlockTypeMetalPlating;
import forestry.core.platform.block.NaturalistChestBlockType;
import forestry.core.platform.block.BlockTypeCoreTesr;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.features.FluidsItems;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.item.FluidContainerType;
import forestry.core.platform.item.FluidHandlerItemForestry;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.machines.blocks.BlockTypeFactoryPlain;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.core.platform.registration.*;
import forestry.core.platform.util.SpeciesUtil;
import forestry.core.content.sorting.features.SortingBlocks;
import forestry.core.content.backpacks.features.BackpackItems;
import forestry.core.content.backpacks.features.CrateItems;
import forestry.core.content.backpacks.items.ItemCrated;
import forestry.core.content.worktable.features.WorktableBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

@FeatureProvider
public class ForestryCreativeTabs {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureCreativeTab FORESTRY = REGISTRY.creativeTab(ForestryConstants.MOD_ID, tab -> {
		tab.icon(CoreItems.PORTABLE_ANALYZER::stack);
		tab.displayItems(ForestryCreativeTabs::addForestryItems);
		tab.withTabsBefore(CreativeModeTabs.SPAWN_EGGS);
		tab.withTabsAfter(tabKey("building_blocks"), tabKey("storage"), tabKey("apiculture"), tabKey("arboriculture"), tabKey("lepidopterology"));
	});
	public static final FeatureCreativeTab BUILDING = REGISTRY.creativeTab("building_blocks", tab -> {
		tab.icon(() -> CoreBlocks.METAL_PLATING.get(BlockTypeMetalPlating.BLUE).stack());
		tab.displayItems(ForestryCreativeTabs::addBuildingItems);
		tab.withTabsBefore(tabKey("forestry"));
		tab.withTabsAfter(tabKey("apiculture"));
	});
	public static final FeatureCreativeTab STORAGE = REGISTRY.creativeTab("storage", tab -> {
		tab.icon(BackpackItems.MINER_BACKPACK::stack);
		tab.displayItems(ForestryCreativeTabs::addStorageItems);
		tab.withTabsBefore(tabKey("agriculture"));
		tab.withTabsAfter(tabKey("mail"));
	});

	/**
	 * Builds a creative tab ordering key from its id. Lets a tab reference tabs owned by other
	 * modules without naming their holder classes, so the ordering survives the module split.
	 */
	public static ResourceKey<CreativeModeTab> tabKey(String path) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, ForestryConstants.forestry(path));
	}

	private static void addForestryItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics tools
		addGeneticBasics(items);
		items.accept(CoreItems.FORESTERS_MANUAL);
		items.accept(CoreItems.SCOOP);
		items.accept(CoreItems.PROVEN_SCOOP);
		items.accept(CoreItems.SPECTACLES);
		items.accept(SortingBlocks.FILTER);

		// Storages
		items.accept(BackpackItems.APIARIST_BACKPACK);
		items.accept(BackpackItems.ARBORIST_BACKPACK);
		if (butterfliesInstalled()) {
			items.accept(BackpackItems.LEPIDOPTERIST_BACKPACK);
		}
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));
		if (butterfliesInstalled()) {
			items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));
		}

		// Machine tools
		items.accept(CoreItems.WRENCH);
		items.accept(CoreItems.PIPETTE);
		items.accept(CoreItems.SOLDERING_IRON);
		items.accept(WorktableBlocks.WORKTABLE);
		// Engines
		EnergyBlocks.ENGINES.getItems().forEach(items::accept);
		items.accept(EnergyBlocks.SOLAR_PANEL);
		// Machines
		FactoryBlocks.PLAIN.getItems().forEach(items::accept);
		FactoryBlocks.TESR.getItems().forEach(items::accept);
		items.accept(CoreBlocks.BURN_BARREL);
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
		items.accept(CoreItems.AMBER);
		items.accept(CoreItems.SILICON);
		// Processed ores
		items.accept(CoreItems.FERTILIZER_COMPOUND);
		items.accept(CoreItems.TIN_INGOT);
		items.accept(CoreItems.TIN_NUGGET);
		items.accept(CoreItems.BRONZE_INGOT);
		// Block forms
		items.accept(CoreBlocks.RAW_TIN_BLOCK);
		CoreBlocks.RESOURCE_STORAGE.getItems().forEach(items::accept);
		// Gears
		items.accept(CoreItems.GEAR_COPPER);
		items.accept(CoreItems.GEAR_TIN);
		items.accept(CoreItems.GEAR_IRON);
		items.accept(CoreItems.GEAR_BRONZE);
		// Casings
		items.accept(CoreItems.STURDY_CASING);
		items.accept(CoreItems.HARDENED_CASING);
		items.accept(CoreItems.IMPREGNATED_CASING);
		items.accept(CoreItems.FLEXIBLE_CASING);
		// Misc machine parts
		items.accept(CoreItems.SOLAR_CELL);

		items.accept(CoreItems.FERTILIZER_COMPOUND);
		items.accept(CoreItems.CARTON);
		items.accept(CoreItems.SURVIVALISTS_PICKAXE);
		items.accept(CoreItems.SURVIVALISTS_SHOVEL);
		items.accept(CoreItems.SURVIVALISTS_AXE);
		items.accept(CoreItems.SURVIVALISTS_SWORD);
		items.accept(CoreItems.SURVIVALISTS_HOE);
		items.accept(CoreItems.PICKAXE_KIT);
		items.accept(CoreItems.SHOVEL_KIT);
		items.accept(CoreItems.AXE_KIT);
		items.accept(CoreItems.SWORD_KIT);
		items.accept(CoreItems.HOE_KIT);
		items.accept(CoreItems.GEAR_TIN);
		items.accept(CoreItems.GEAR_COPPER);
		items.accept(CoreItems.GEAR_BRONZE);
		items.accept(CoreItems.GEAR_IRON);
		items.accept(CoreItems.SOLDERING_IRON);
		items.accept(CoreItems.SPECTACLES);
		items.accept(CoreItems.ASH);
		items.accept(CoreItems.ASH_BRICK);
		items.accept(CoreItems.PEAT);
		items.accept(CoreItems.BITUMINOUS_PEAT);
		items.accept(CoreItems.BEESWAX);
		items.accept(CoreItems.REFRACTORY_WAX);
		items.accept(CoreItems.WAX_BRICK);
		items.accept(CoreItems.REFRACTORY_WAX_BRICK);
		// todo merge more items into crafting materials
		CoreItems.CRAFTING_MATERIALS.getItems().forEach(items::accept);

		// The decorative blocks that used to sit here now live in the building blocks tab, as on 1.20.1

		// Escritoire output — research notes ship players hint about mutations. Worth
		// surfacing in creative so they can be inspected without needing the workflow.
		items.accept(CoreItems.RESEARCH_NOTE);
	}

	private static void addBuildingItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		addAllBuildingBlocks(items);

		// Wood blocks. These stay listed in the arboriculture tab as well, as on 1.20.1
		WoodAccess access = WoodAccess.INSTANCE;
		for (IWoodType type : access.getRegisteredWoodTypes()) {
			ArboricultureCreativeTab.addAllWoodBlocks(items, access, type, false);
		}
		for (IWoodType type : access.getRegisteredWoodTypes()) {
			ArboricultureCreativeTab.addAllWoodBlocks(items, access, type, true);
		}
	}

	/**
	 * Adds every decorative block Forestry makes to a tab, grouped by material.
	 *
	 * @param items The tab's output
	 */
	private static void addAllBuildingBlocks(CreativeModeTab.Output items) {
		// Wood and soil
		items.accept(CharcoalBlocks.LOG_PILE);
		items.accept(CharcoalBlocks.DECORATIVE_LOG_PILE);
		items.accept(CoreBlocks.TURF_BLOCK);
		items.accept(CoreBlocks.TURF);
		items.accept(CoreBlocks.PLYWOOD_BLOCK);
		items.accept(CoreBlocks.PLYWOOD_SHEET);
		items.accept(CoreBlocks.CORK);

		// Lighting
		items.accept(CoreItems.PHOSPHOR_TORCH_ITEM);
		items.accept(CoreBlocks.PHOSPHOR_LANTERN);
		items.accept(CoreBlocks.TIN_CHAIN);

		// Ash
		items.accept(CharcoalBlocks.ASH);
		addStoneFamily(items, CoreBlocks.ASH_BRICKS, CoreBlocks.CHISELED_ASH_BRICKS);

		// Wax
		items.accept(ApicultureBlocks.WAX_BLOCK);
		addStoneFamily(items, CoreBlocks.WAX_BRICKS, CoreBlocks.CHISELED_WAX_BRICKS);
		items.accept(ApicultureBlocks.REFRACTORY_WAX_BLOCK);
		addStoneFamily(items, CoreBlocks.REFRACTORY_WAX_BRICKS, CoreBlocks.CHISELED_REFRACTORY_WAX_BRICKS);

		// Waxstone, refractory waxstone and honeystone
		for (CoreBlocks.StoneSet set : CoreBlocks.STONE_SETS) {
			addStoneFamily(items, set.stone(), set.chiseled());
			addStoneFamily(items, set.cobbled(), null);
			addStoneFamily(items, set.bricks(), null);
			addStoneFamily(items, set.polished(), null);
		}
		// Left commented as on 1.20.1, where the smelting recipes that make these are commented out too.
		// Deliberately unfinished there, so enabling only the tab entry would be worse than leaving both off
		//items.accept(CoreBlocks.ASHEN_WAX_BLOCK);
		//items.accept(CoreBlocks.CRISPY_HONEY_BLOCK);

		// Metal plating
		CoreBlocks.METAL_PLATING.getItems().forEach(items::accept);

		// Candles. Deviation from 1.20.1: that tree listed the seventeen vanilla candles here as well, which
		// belong to vanilla's own tab. Only forestry's own are listed
		items.accept(CoreBlocks.REFRACTORY_CANDLE);
		items.accept(CoreBlocks.RAINBOW_CANDLE);
		CoreBlocks.BIG_CANDLES.getItems().forEach(items::accept);
		CoreBlocks.JUMBO_CANDLES.getItems().forEach(items::accept);
	}

	// The lepidopterist chest and backpack are registered by base but do nothing without the butterfly
	// jar, so a base-only install leaves them out of the tabs rather than offering two inert items
	private static boolean butterfliesInstalled() {
		return SpeciesUtil.getButterflyTypeSafe() != null;
	}

	private static void addStorageItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
		// Genetics backpacks
		items.accept(BackpackItems.APIARIST_BACKPACK);
		items.accept(BackpackItems.ARBORIST_BACKPACK);
		if (butterfliesInstalled()) {
			items.accept(BackpackItems.LEPIDOPTERIST_BACKPACK);
		}

		// T1
		items.accept(BackpackItems.MINER_BACKPACK);
		items.accept(BackpackItems.DIGGER_BACKPACK);
		items.accept(BackpackItems.FORESTER_BACKPACK);
		items.accept(BackpackItems.HUNTER_BACKPACK);
		items.accept(BackpackItems.ADVENTURER_BACKPACK);
		items.accept(BackpackItems.BUILDER_BACKPACK);
		items.accept(BackpackItems.BREWER_BACKPACK);

		// T2
		items.accept(BackpackItems.MINER_BACKPACK_T_2);
		items.accept(BackpackItems.DIGGER_BACKPACK_T_2);
		items.accept(BackpackItems.FORESTER_BACKPACK_T_2);
		items.accept(BackpackItems.HUNTER_BACKPACK_T_2);
		items.accept(BackpackItems.ADVENTURER_BACKPACK_T_2);
		items.accept(BackpackItems.BUILDER_BACKPACK_T_2);
		items.accept(BackpackItems.BREWER_BACKPACK_T_2);

		// Packing machines
		items.accept(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.BOTTLER));
		items.accept(FactoryBlocks.PLAIN.get(BlockTypeFactoryPlain.CARPENTER));

		// Misc gear
		items.accept(CoreItems.PIPETTE);
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.APIARIST_CHEST));
		items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.ARBORIST_CHEST));
		if (butterfliesInstalled()) {
			items.accept(CoreBlocks.NATURALIST_CHEST.get(NaturalistChestBlockType.LEPIDOPTERIST_CHEST));
		}
		items.accept(SortingBlocks.FILTER);

		// Empty containers
		items.accept(CoreItems.CARTON);
		FluidsItems.CONTAINERS.getItems().forEach(items::accept);
		items.accept(CrateItems.CRATE);

		// Filled cartons
		items.accept(CoreItems.PICKAXE_KIT);
		items.accept(CoreItems.SHOVEL_KIT);
		items.accept(CoreItems.AXE_KIT);
		items.accept(CoreItems.SWORD_KIT);
		items.accept(CoreItems.HOE_KIT);

		// Filled containers
		for (FluidContainerType type : FluidContainerType.values()) {
			for (Fluid fluid : BuiltInRegistries.FLUID) {
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

	/**
	 * Adds one decorative shape family to a tab, base block first and the chiseled block last.
	 *
	 * @param items    The tab's output
	 * @param family   The family to list
	 * @param chiseled The chiseled block to list after it, or null when the family has none
	 */
	private static void addStoneFamily(CreativeModeTab.Output items, CoreBlocks.StoneFamily family, @Nullable FeatureBlock<Block, BlockItem> chiseled) {
		family.features().forEach(items::accept);

		if (chiseled != null) {
			items.accept(chiseled);
		}
	}

	public static void addGeneticBasics(CreativeModeTab.Output items) {
		items.accept(CoreItems.PORTABLE_ANALYZER);
		items.accept(CoreItems.HONEY_DROP);
		items.accept(CoreItems.HONEYDEW);
		items.accept(ApicultureItems.EXPERIENCE_DROP);
		items.accept(ApicultureItems.MAGMATIC_DROP);
		items.accept(CoreBlocks.BASE.get(BlockTypeCoreTesr.ESCRITOIRE));
		items.accept(CoreBlocks.BASE.get(BlockTypeCoreTesr.ANALYZER));
	}
}

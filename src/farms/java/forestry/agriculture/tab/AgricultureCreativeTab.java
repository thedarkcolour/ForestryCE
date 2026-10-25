package forestry.agriculture.tab;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.features.CoreBlocks;
import forestry.core.features.CoreItems;
import forestry.core.platform.tab.ForestryCreativeTabs;
import forestry.core.platform.registration.FeatureCreativeTab;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import net.minecraft.world.item.CreativeModeTab;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.agriculture.features.MinifarmBlocks;
import forestry.core.content.energy.features.EnergyBlocks;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.blocks.SpecialFactoryBlockType;
import forestry.core.content.machines.features.FactoryBlocks;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.features.MultifarmBlocks;

/**
 * The farming creative tab. Ordering keys are built from tab ids rather than tab objects so
 * this module does not depend on the others' holder classes.
 */
@FeatureProvider
public class AgricultureCreativeTab {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureCreativeTab AGRICULTURE = REGISTRY.creativeTab("agriculture", tab -> {
		tab.icon(() -> MinifarmBlocks.MANAGED_PLANTER.stack(MinifarmBlockType.ARBORETUM));
		tab.displayItems(AgricultureCreativeTab::addAgricultureItems);
		tab.withTabsBefore(ForestryCreativeTabs.tabKey("lepidopterology"));
		tab.withTabsAfter(ForestryCreativeTabs.tabKey("storage"));
	});

	static void addAgricultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
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
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.CARPENTER));
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.CENTRIFUGE));
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.FERMENTER));
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.MOISTENER));
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.SQUEEZER));
		items.accept(FactoryBlocks.PLAIN.get(FactoryBlockType.STILL));

		// Rainmaker
		items.accept(FactoryBlocks.TESR.get(SpecialFactoryBlockType.RAINMAKER));
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
		for (MultifarmMaterialType material : MultifarmMaterialType.values()) {
			for (MultifarmBlockType type : MultifarmBlockType.values()) {
				items.accept(MultifarmBlocks.FARM.stack(type, material));
			}
		}

		// Single farm (boo)
		for (MinifarmBlockType type : MinifarmBlockType.values()) {
			items.accept(MinifarmBlocks.MANAGED_PLANTER.stack(type));
			items.accept(MinifarmBlocks.MANUAL_PLANTER.stack(type));
		}
	}
}

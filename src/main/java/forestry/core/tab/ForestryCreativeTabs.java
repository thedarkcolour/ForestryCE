package forestry.core.tab;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import forestry.api.ForestryConstants;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.arboriculture.ForestryTreeSpecies;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.lepidopterology.ForestryButterflySpecies;
import forestry.api.lepidopterology.genetics.ButterflyLifeStage;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.features.CoreItems;
import forestry.core.utils.SpeciesUtil;
import forestry.modules.features.FeatureCreativeTab;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.storage.features.BackpackItems;

@FeatureProvider
public class ForestryCreativeTabs {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureCreativeTab FORESTRY = REGISTRY.creativeTab(ForestryConstants.MOD_ID, tab -> {
		tab.icon(CoreItems.FERTILIZER_COMPOUND::stack);
		tab.displayItems(ForestryCreativeTabs::addForestryItems);
		tab.withTabsBefore(CreativeModeTabs.SPAWN_EGGS);
		tab.withTabsAfter(ForestryCreativeTabs.STORAGE.getKey(), ForestryCreativeTabs.APICULTURE.getKey(), ForestryCreativeTabs.ARBORICULTURE.getKey(), ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
	});
	public static final FeatureCreativeTab STORAGE = REGISTRY.creativeTab("storage", tab -> {
		tab.icon(BackpackItems.MINER_BACKPACK::stack);
		tab.displayItems(ForestryCreativeTabs::addStorageItems);
		tab.withTabsBefore(ForestryCreativeTabs.FORESTRY.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.APICULTURE.getKey(), ForestryCreativeTabs.ARBORICULTURE.getKey(), ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
	});
	public static final FeatureCreativeTab APICULTURE = REGISTRY.creativeTab("apiculture", tab -> {
		tab.icon(() -> SpeciesUtil.BEE_TYPE.get().createStack(ForestryBeeSpecies.FOREST, BeeLifeStage.DRONE));
		tab.displayItems(ForestryCreativeTabs::addApicultureItems);
		tab.withTabsBefore(ForestryCreativeTabs.FORESTRY.getKey(), ForestryCreativeTabs.STORAGE.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.ARBORICULTURE.getKey(), ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
	});
	public static final FeatureCreativeTab ARBORICULTURE = REGISTRY.creativeTab("arboriculture", tab -> {
		tab.icon(() -> SpeciesUtil.TREE_TYPE.get().createStack(ForestryTreeSpecies.OAK, TreeLifeStage.SAPLING));
		tab.withTabsBefore(ForestryCreativeTabs.FORESTRY.getKey(), ForestryCreativeTabs.STORAGE.getKey(), ForestryCreativeTabs.APICULTURE.getKey());
		tab.withTabsAfter(ForestryCreativeTabs.LEPIDOPTEROLOGY.getKey());
		tab.displayItems(ForestryCreativeTabs::addArboricultureItems);
	});
	public static final FeatureCreativeTab LEPIDOPTEROLOGY = REGISTRY.creativeTab("lepidopterology", tab -> {
		tab.icon(() -> SpeciesUtil.BUTTERFLY_TYPE.get().createStack(ForestryButterflySpecies.BRIMSTONE, ButterflyLifeStage.BUTTERFLY));
		tab.displayItems(ForestryCreativeTabs::addLepidopterologyItems);
		tab.withTabsBefore(ForestryCreativeTabs.FORESTRY.getKey(), ForestryCreativeTabs.STORAGE.getKey(), ForestryCreativeTabs.APICULTURE.getKey(), ForestryCreativeTabs.ARBORICULTURE.getKey());
	});


	private static void addForestryItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
	}

	private static void addStorageItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
	}

	private static void addApicultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
	}
 	
	private static void addArboricultureItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
	}

	private static void addLepidopterologyItems(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output items) {
	}
}

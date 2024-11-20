package forestry.farming.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.farming.blocks.EnumFarmBlockType;
import forestry.farming.blocks.EnumFarmMaterial;
import forestry.farming.blocks.FarmBlock;
import forestry.farming.items.ItemBlockFarm;
import forestry.modules.features.FeatureBlockTable;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class FarmingBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureBlockTable<FarmBlock, EnumFarmBlockType, EnumFarmMaterial> FARM = REGISTRY.blockTable(FarmBlock::create, EnumFarmBlockType.values(), EnumFarmMaterial.values()).item(ItemBlockFarm::new).identifier("farm").create();
}

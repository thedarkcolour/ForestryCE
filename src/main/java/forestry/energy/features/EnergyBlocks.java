package forestry.energy.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.items.ItemBlockTesr;
import forestry.energy.blocks.EngineBlock;
import forestry.energy.blocks.EngineBlockType;
import forestry.modules.features.FeatureBlockGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class EnergyBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ENERGY);

	public static final FeatureBlockGroup<EngineBlock, EngineBlockType> ENGINES = REGISTRY.blockGroup(EngineBlock::new, EngineBlockType.values()).item(ItemBlockTesr::new).identifier("engine").create();
}

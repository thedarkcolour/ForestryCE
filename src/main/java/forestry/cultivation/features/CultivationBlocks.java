package forestry.cultivation.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.cultivation.blocks.BlockPlanter;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.items.ItemBlockPlanter;
import forestry.modules.features.FeatureBlockGroup;
import forestry.modules.features.FeatureGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CultivationBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CULTIVATION);

	public static final FeatureBlockGroup<BlockPlanter, BlockTypePlanter> MANAGED_PLANTER = REGISTRY.blockGroup(type -> new BlockPlanter(type, false), BlockTypePlanter.values()).item(ItemBlockPlanter::new).identifier("managed", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureBlockGroup<BlockPlanter, BlockTypePlanter> MANUAL_PLANTER = REGISTRY.blockGroup(type -> new BlockPlanter(type, true), BlockTypePlanter.values()).item(ItemBlockPlanter::new).identifier("manual", FeatureGroup.IdentifierType.SUFFIX).create();
}

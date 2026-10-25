package forestry.agriculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import forestry.agriculture.multifarm.items.MultifarmBlockItem;
import forestry.core.platform.registration.FeatureBlockTable;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

@FeatureProvider
public class MultifarmBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	// todo fix switch case
	public static final FeatureBlockTable<MultifarmBlock, MultifarmBlockType, MultifarmMaterialType> FARM = REGISTRY.blockTable(MultifarmBlock::create, MultifarmBlockType.values(), MultifarmMaterialType.values()).item(MultifarmBlockItem::new).identifier((part, material) -> {
		String mat = switch (material) {
			case SANDSTONE_CHISELED -> "chiseled_sandstone";
			case BRICK_NETHER -> "nether_brick";
			case BRICK_CHISELED -> "chiseled_stone_brick";
			case QUARTZ_CHISELED -> "chiseled_quartz";
			case QUARTZ_LINES -> "quartz_pillar";
			default -> material.getSerializedName();
		};
		String partName = (part == MultifarmBlockType.PLAIN && material == MultifarmMaterialType.STONE_BRICK) ? "block" : part.getSerializedName();
		return mat + "_farm_" + partName;
	}).create();
}

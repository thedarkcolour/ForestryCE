package forestry.agriculture.features;

import forestry.agriculture.multifarm.blocks.MultifarmBlock;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;
import forestry.agriculture.multifarm.items.MultifarmBlockItem;
import forestry.api.modules.ForestryModuleIds;
import forestry.agriculture.minifarm.blocks.MinifarmBlock;
import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.agriculture.minifarm.items.MinifarmBlockItem;
import forestry.core.platform.registration.*;

@FeatureProvider
public class FarmingBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureBlockTable<MultifarmBlock, MultifarmBlockType, MultifarmMaterialType> FARM = REGISTRY.blockTable(MultifarmBlock::create, MultifarmBlockType.values(), MultifarmMaterialType.values()).item(MultifarmBlockItem::new).identifier((part, material) -> {
		String partName = (part == MultifarmBlockType.PLAIN && material == MultifarmMaterialType.STONE_BRICK) ? "block" : part.getSerializedName();
		return material.getSerializedName() + "_farm_" + partName;
	}).create();

	public static final FeatureBlockGroup<MinifarmBlock, MinifarmBlockType> MANAGED_MINIFARM = REGISTRY
		.blockGroup(type -> new MinifarmBlock(type, false), MinifarmBlockType.VALUES)
		.item(MinifarmBlockItem::new)
		.identifier("managed", FeatureGroup.IdentifierType.SUFFIX)
		.create();
	public static final FeatureBlockGroup<MinifarmBlock, MinifarmBlockType> MANUAL_MINIFARM = REGISTRY
		.blockGroup(type -> new MinifarmBlock(type, true), MinifarmBlockType.VALUES)
		.item(MinifarmBlockItem::new)
		.identifier("manual", FeatureGroup.IdentifierType.SUFFIX)
		.create();
}

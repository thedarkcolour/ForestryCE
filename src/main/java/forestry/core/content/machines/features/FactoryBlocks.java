package forestry.core.content.machines.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.item.SpecialRendererBlockItem;
import forestry.core.content.machines.blocks.FactoryBlock;
import forestry.core.content.machines.blocks.SpecialFactoryBlock;
import forestry.core.content.machines.blocks.FactoryBlockType;
import forestry.core.content.machines.blocks.SpecialFactoryBlockType;
import forestry.core.platform.registration.FeatureBlockGroup;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;

import java.util.List;

@FeatureProvider
public class FactoryBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FACTORY);

	public static final FeatureBlockGroup<SpecialFactoryBlock, SpecialFactoryBlockType> TESR = REGISTRY.blockGroup(SpecialFactoryBlock::new, List.of(SpecialFactoryBlockType.values())).item(SpecialRendererBlockItem::new).create();
	public static final FeatureBlockGroup<FactoryBlock, FactoryBlockType> PLAIN = REGISTRY.blockGroup(FactoryBlock::create, List.of(FactoryBlockType.values())).item(ForestryBlockItem::new).create();
}

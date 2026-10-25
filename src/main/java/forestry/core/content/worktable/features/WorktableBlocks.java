package forestry.core.content.worktable.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IBlockFeature;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.worktable.blocks.WorktableBlock;
import forestry.core.content.worktable.blocks.WorktableBlockType;

@FeatureProvider
public class WorktableBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.WORKTABLE);

	public static final IBlockFeature<WorktableBlock, ForestryBlockItem<?>> WORKTABLE = REGISTRY.block((properties) -> new WorktableBlock(properties, WorktableBlockType.WORKTABLE), ForestryBlockItem::new, "worktable");
}

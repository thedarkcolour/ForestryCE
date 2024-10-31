package forestry.worktable.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.items.ItemBlockForestry;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IBlockFeature;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import forestry.worktable.blocks.WorktableBlock;
import forestry.worktable.blocks.WorktableBlockType;

@FeatureProvider
public class WorktableBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.WORKTABLE);

	public static final IBlockFeature<WorktableBlock, ItemBlockForestry<?>> WORKTABLE = REGISTRY.block(() -> new WorktableBlock(WorktableBlockType.WORKTABLE), ItemBlockForestry::new, "worktable");
}

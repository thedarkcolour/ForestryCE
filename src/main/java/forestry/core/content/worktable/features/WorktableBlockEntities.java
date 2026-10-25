package forestry.core.content.worktable.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import forestry.core.content.worktable.tiles.WorktableBlockEntity;

import java.util.List;

@FeatureProvider
public class WorktableBlockEntities {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.WORKTABLE);

	public static final FeatureTileType<WorktableBlockEntity> WORKTABLE = REGISTRY.tile(WorktableBlockEntity::new, "worktable", () -> List.of(WorktableBlocks.WORKTABLE.block()));
}

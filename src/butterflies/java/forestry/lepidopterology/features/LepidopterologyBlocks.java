package forestry.lepidopterology.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.lepidopterology.cocoons.CocoonBlock;
import forestry.lepidopterology.cocoons.SolidCocoonBlock;
import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import net.minecraft.world.item.BlockItem;

@FeatureProvider
public class LepidopterologyBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.LEPIDOPTEROLOGY);

	public static final FeatureBlock<CocoonBlock, BlockItem> COCOON = REGISTRY.block(CocoonBlock::new, "cocoon");
	// used only in world generation
	public static final FeatureBlock<SolidCocoonBlock, BlockItem> COCOON_SOLID = REGISTRY.block(SolidCocoonBlock::new, "cocoon_solid");
}

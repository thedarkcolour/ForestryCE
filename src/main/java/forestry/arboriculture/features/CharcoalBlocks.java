package forestry.arboriculture.features;

import net.minecraft.world.item.BlockItem;

import forestry.api.core.ItemGroups;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.blocks.BlockAsh;
import forestry.arboriculture.blocks.BlockCharcoal;
import forestry.arboriculture.blocks.BlockDecorativeWoodPile;
import forestry.arboriculture.blocks.BlockWoodPile;
import forestry.core.items.ItemBlockForestry;
import forestry.core.items.ItemProperties;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CharcoalBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CHARCOAL);

	public static final FeatureBlock<BlockCharcoal, ItemBlockForestry<BlockCharcoal>> CHARCOAL = REGISTRY.block(BlockCharcoal::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties(ItemGroups.tabArboriculture).burnTime(16000)), "charcoal");
	// todo 1.20.1 change ID to "log pile"
	public static final FeatureBlock<BlockWoodPile, BlockItem> WOOD_PILE = REGISTRY.block(BlockWoodPile::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties(ItemGroups.tabArboriculture).burnTime(1200)), "wood_pile");
	public static final FeatureBlock<BlockDecorativeWoodPile, BlockItem> WOOD_PILE_DECORATIVE = REGISTRY.block(BlockDecorativeWoodPile::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties(ItemGroups.tabArboriculture).burnTime(1200)), "wood_pile_decorative");
	public static final FeatureBlock<BlockAsh, BlockItem> ASH = REGISTRY.block(BlockAsh::new, "ash_block");
}

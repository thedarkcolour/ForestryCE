package forestry.arboriculture.features;

import net.minecraft.world.item.BlockItem;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.blocks.BlockAsh;
import forestry.arboriculture.blocks.BlockCharcoal;
import forestry.arboriculture.blocks.DecorativeLogPileBlock;
import forestry.arboriculture.blocks.LogPileBlock;
import forestry.core.items.ItemBlockForestry;
import forestry.core.items.ItemProperties;
import forestry.modules.features.FeatureBlock;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CharcoalBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CHARCOAL);

	public static final FeatureBlock<BlockCharcoal, ItemBlockForestry<BlockCharcoal>> CHARCOAL = REGISTRY.block(BlockCharcoal::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties().burnTime(16000)), "charcoal");
	public static final FeatureBlock<LogPileBlock, BlockItem> WOOD_PILE = REGISTRY.block(LogPileBlock::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties().burnTime(1200)), "log_pile");
	public static final FeatureBlock<DecorativeLogPileBlock, BlockItem> WOOD_PILE_DECORATIVE = REGISTRY.block(DecorativeLogPileBlock::new, (block) -> new ItemBlockForestry<>(block, new ItemProperties().burnTime(1200)), "wood_pile_decorative");
	public static final FeatureBlock<BlockAsh, BlockItem> ASH = REGISTRY.block(BlockAsh::new, "ash_block");
}

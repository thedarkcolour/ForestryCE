package forestry.arboriculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.charcoal.AshBlock;
import forestry.arboriculture.charcoal.DecorativeLogPileBlock;
import forestry.arboriculture.charcoal.LogPileBlock;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.registration.FeatureBlock;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

@FeatureProvider
public class CharcoalBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ARBORICULTURE);

	public static final FeatureBlock<Block, ForestryBlockItem<Block>> CHARCOAL = REGISTRY.block(
		Block::new,
		() -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.STONE),
		ForestryBlockItem::new,
		Item.Properties::new,
		"charcoal_block"
	);
	public static final FeatureBlock<LogPileBlock, BlockItem> LOG_PILE = REGISTRY.block(LogPileBlock::new, BlockBehaviour.Properties::of, ForestryBlockItem::new, Item.Properties::new, "log_pile");
	public static final FeatureBlock<DecorativeLogPileBlock, BlockItem> DECORATIVE_LOG_PILE = REGISTRY.block(DecorativeLogPileBlock::new, BlockBehaviour.Properties::of, ForestryBlockItem::new, Item.Properties::new, "decorative_log_pile");
	public static final FeatureBlock<AshBlock, BlockItem> ASH = REGISTRY.block(AshBlock::new, BlockBehaviour.Properties::of, ForestryBlockItem::new, Item.Properties::new, "ash_block");
}

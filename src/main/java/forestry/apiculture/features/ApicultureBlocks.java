package forestry.apiculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.blocks.*;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.apiculture.items.ItemBlockHoneyComb;
import forestry.core.items.ItemBlockForestry;
import forestry.modules.features.FeatureBlockGroup;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;
import net.minecraft.world.item.Item;

import java.util.List;

@FeatureProvider
public class ApicultureBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final FeatureBlockGroup<BlockApiculture, BlockTypeApiculture> BASE = REGISTRY.blockGroup(BlockApiculture::new, BlockTypeApiculture.values()).item((block) -> new ItemBlockForestry<>(block, new Item.Properties())).create();

	public static final FeatureBlockGroup<BlockBeeHive, BlockHiveType> BEEHIVE = REGISTRY.blockGroup(BlockBeeHive::new, BlockHiveType.values()).itemWithType((block, type) -> new ItemBlockForestry<>(block, new Item.Properties())).identifier("beehive").create();

	public static final FeatureBlockGroup<BlockHoneyComb, EnumHoneyComb> BEE_COMB = REGISTRY.blockGroup(BlockHoneyComb::new, EnumHoneyComb.VALUES).item(ItemBlockHoneyComb::new).identifier("block_bee_comb").create();
	public static final FeatureBlockGroup<BlockAlveary, BlockAlveary.Type> ALVEARY = REGISTRY.blockGroup(BlockAlveary::new, List.of(
		BlockAlveary.Type.PLAIN,
		BlockAlveary.Type.SWARMER,
		BlockAlveary.Type.FAN,
		BlockAlveary.Type.HEATER,
		BlockAlveary.Type.HYGRO,
		BlockAlveary.Type.STABILISER,
		BlockAlveary.Type.SIEVE
	)).item(blockAlveary -> new ItemBlockForestry<>(blockAlveary, new Item.Properties())).identifier("alveary").create();
}

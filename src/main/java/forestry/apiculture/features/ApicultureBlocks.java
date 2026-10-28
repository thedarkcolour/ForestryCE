package forestry.apiculture.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.apiary.ApicultureBlockType;
import forestry.apiculture.bees.HoneyCombBlock;
import forestry.apiculture.bees.WaxBlock;
import forestry.apiculture.bees.EnumHoneyComb;
import forestry.apiculture.bees.HoneyCombBlockItem;
import forestry.apiculture.hives.HiveBlock;
import forestry.apiculture.hives.HiveBlockType;
import forestry.core.platform.block.BaseBlock;
import forestry.core.platform.fluids.ForestryFluids;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.registration.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

@FeatureProvider
public class ApicultureBlocks {
	private static final FeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.APICULTURE);

	public static final FeatureBlockGroup<BaseBlock<ApicultureBlockType>, ApicultureBlockType> BASE = REGISTRY
		.blockGroup(BaseBlock::new, ApicultureBlockType.values())
		.blockProperties(props -> props.sound(SoundType.WOOD).strength(2.0f))
		.item(ForestryBlockItem::new)
		.create();

	public static final FeatureBlockGroup<HiveBlock, HiveBlockType> HIVE = REGISTRY
		.blockGroup(HiveBlock::new, HiveBlockType.values())
		.item(ForestryBlockItem::new)
		.identifierSuffix("hive")
		.create();

	public static final FeatureBlockGroup<HoneyCombBlock, EnumHoneyComb> COMB_BLOCK = REGISTRY
		.blockGroup(HoneyCombBlock::new, EnumHoneyComb.VALUES)
		.item(HoneyCombBlockItem::new)
		.identifierSuffix("comb_block")
		.create();

	public static final FeatureBlockGroup<AlvearyBlock, AlvearyBlock.Type> ALVEARY = REGISTRY
		.blockGroup(AlvearyBlock::new, AlvearyBlock.Type.DEFAULT_VALUES)
		.item(ForestryBlockItem::new)
		.identifierPrefix("alveary")
		.create();

	public static final FeatureBlock<WaxBlock, BlockItem> WAX_BLOCK = REGISTRY.block(
		properties -> new WaxBlock(properties, true, ForestryFluids.WAX::getFluid),
		() -> Block.Properties.ofFullCopy(Blocks.HONEYCOMB_BLOCK).sound(SoundType.HONEY_BLOCK).mapColor(MapColor.COLOR_YELLOW).ignitedByLava(),
		ForestryBlockItem::new,
		"wax_block"
	);
	public static final FeatureBlock<WaxBlock, BlockItem> REFRACTORY_WAX_BLOCK = REGISTRY.block(
		properties -> new WaxBlock(properties, false, null),
		() -> Block.Properties.ofFullCopy(Blocks.HONEYCOMB_BLOCK).sound(SoundType.HONEY_BLOCK).mapColor(MapColor.COLOR_RED).ignitedByLava(),
		ForestryBlockItem::new,
		"refractory_wax_block"
	);
}

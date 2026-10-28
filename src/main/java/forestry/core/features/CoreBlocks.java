package forestry.core.features;

import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.block.NaturalistChestBlockType;
import forestry.core.platform.block.*;
import forestry.core.content.burnbarrel.BurnBarrelBlock;
import forestry.core.content.decorative.BigCandleBlock;
import forestry.core.content.decorative.JumboCandleBlock;
import forestry.core.content.decorative.PlywoodBlock;
import forestry.core.content.decorative.PlywoodSheetBlock;
import forestry.core.content.decorative.BigCandleBlockType;
import forestry.core.content.decorative.JumboCandleBlockType;
import forestry.core.content.decorative.MetalPlatingBlockType;
import forestry.core.content.decorative.RefractoryCandleBlock;
import forestry.core.content.decorative.CorkBlock;
import forestry.core.content.soil.*;
import forestry.core.content.lighting.WaterloggableTorchBlock;
import forestry.core.content.lighting.WaterloggableWallTorchBlock;
import forestry.core.content.resources.*;
import forestry.core.platform.item.ForestryBlockItem;
import forestry.core.platform.item.SpecialRendererBlockItem;
import forestry.core.platform.registration.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

@FeatureProvider
public class CoreBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CORE);

	public static final FeatureBlockGroup<CoreMachineBlock, CoreMachineBlockType> BASE = REGISTRY.blockGroup((properties, type) -> new CoreMachineBlock(type, properties), List.of(CoreMachineBlockType.values()))
		.blockProperties((properties, type) -> type.getBlockProperties())
		.item(SpecialRendererBlockItem::new).create();

	public static final FeatureBlock<BurnBarrelBlock, ForestryBlockItem<?>> BURN_BARREL = REGISTRY.block(BurnBarrelBlock::new, () -> Properties.ofFullCopy(Blocks.FURNACE)
		.mapColor(MapColor.COLOR_GRAY)
		.sound(SoundType.METAL)
		.lightLevel(state -> state.getValue(BurnBarrelBlock.LIT) ? 15 : 0), ForestryBlockItem::new, "burn_barrel");

	public static final FeatureBlock<BogEarthBlock, ForestryBlockItem<?>> BOG_EARTH = REGISTRY.block(BogEarthBlock::new, ForestryBlockItem::new, "bog_earth");
	public static final FeatureBlock<PeatBlock, ForestryBlockItem<?>> PEAT = REGISTRY.block(PeatBlock::new, null, "peat");
	public static final FeatureBlock<HumusBlock, ForestryBlockItem<?>> HUMUS = REGISTRY.block(HumusBlock::new, ForestryBlockItem::new, "humus");
	public static final FeatureBlockGroup<ResourceStorageBlock, ResourceStorageBlockType> RESOURCE_STORAGE = REGISTRY.blockGroup(ResourceStorageBlock::new, List.of(ResourceStorageBlockType.values())).item(ForestryBlockItem::new).identifier("block", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureBlock<Block, BlockItem> APATITE_ORE = REGISTRY.block((properties) -> new DropExperienceBlock(UniformInt.of(0, 4), properties), () -> Properties.ofFullCopy(Blocks.COAL_ORE), ForestryBlockItem::new, "apatite_ore");
	public static final FeatureBlock<Block, BlockItem> DEEPSLATE_APATITE_ORE = REGISTRY.block((properties) -> new DropExperienceBlock(UniformInt.of(0, 4), properties), () -> Properties.ofFullCopy(APATITE_ORE.block()).mapColor(MapColor.DEEPSLATE).strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), ForestryBlockItem::new, "deepslate_apatite_ore");
	public static final FeatureBlock<Block, BlockItem> TIN_ORE = REGISTRY.block(Block::new, () -> Properties.ofFullCopy(Blocks.COPPER_ORE), ForestryBlockItem::new, "tin_ore");
	public static final FeatureBlock<Block, BlockItem> DEEPSLATE_TIN_ORE = REGISTRY.block(Block::new, () -> Properties.ofFullCopy(TIN_ORE.block()).mapColor(MapColor.DEEPSLATE).strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE), ForestryBlockItem::new, "deepslate_tin_ore");
	public static final FeatureBlock<Block, BlockItem> RAW_TIN_BLOCK = REGISTRY.block(Block::new, () -> Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK), ForestryBlockItem::new, "raw_tin_block");

	/* Turf */
	public static final FeatureBlock<Block, BlockItem> TURF_BLOCK = REGISTRY.block(Block::new, () -> Properties.ofFullCopy(Blocks.GRASS_BLOCK), ForestryBlockItem::new, "turf_block");
	public static final FeatureBlock<CarpetBlock, BlockItem> TURF = REGISTRY.block(CarpetBlock::new, () -> Properties.ofFullCopy(Blocks.GRASS_BLOCK), ForestryBlockItem::new, "turf");

	/* Plywood and cork */
	// todo sheet blocks can only be wood
	public static final FeatureBlock<PlywoodSheetBlock, BlockItem> PLYWOOD_SHEET = REGISTRY.block(PlywoodSheetBlock::new, () -> Properties.ofFullCopy(Blocks.OAK_PLANKS), ForestryBlockItem::new, "plywood");
	public static final FeatureBlock<PlywoodBlock, BlockItem> PLYWOOD_BLOCK = REGISTRY.block(PlywoodBlock::new, () -> Properties.ofFullCopy(Blocks.OAK_PLANKS).ignitedByLava(), ForestryBlockItem::new, "plywood_block");
	public static final FeatureBlock<CorkBlock, BlockItem> CORK = REGISTRY.block(CorkBlock::new, () -> Properties.ofFullCopy(Blocks.OAK_PLANKS).sound(SoundType.CHERRY_WOOD).strength(2.0f, 3.0f).ignitedByLava(), ForestryBlockItem::new, "cork");

	/* Lighting */
	public static final FeatureBlock<ChainBlock, BlockItem> TIN_CHAIN = REGISTRY.block(ChainBlock::new, () -> Properties.ofFullCopy(Blocks.CHAIN), ForestryBlockItem::new, "tin_chain");
	public static final FeatureBlock<LanternBlock, BlockItem> PHOSPHOR_LANTERN = REGISTRY.block(LanternBlock::new, () -> Properties.ofFullCopy(Blocks.SOUL_LANTERN).lightLevel(state -> 13), ForestryBlockItem::new, "phosphor_lantern");
	public static final FeatureBlock<WaterloggableTorchBlock, ForestryBlockItem<?>> PHOSPHOR_TORCH = REGISTRY.block(WaterloggableTorchBlock::new, () -> Properties.ofFullCopy(Blocks.SOUL_TORCH).lightLevel(state -> 13), null, "phosphor_torch");
	public static final FeatureBlock<WaterloggableWallTorchBlock, ForestryBlockItem<?>> PHOSPHOR_WALL_TORCH = REGISTRY.block(WaterloggableWallTorchBlock::new, () -> Properties.ofFullCopy(Blocks.SOUL_TORCH).lightLevel(state -> 13), null, "phosphor_wall_torch");

	public static final FeatureBlockGroup<SpecialRendererBlock<NaturalistChestBlockType>, NaturalistChestBlockType> NATURALIST_CHEST = REGISTRY.blockGroup(type -> new SpecialRendererBlock<>(type, Properties.of().sound(SoundType.WOOD)), List.of(NaturalistChestBlockType.values())).item(SpecialRendererBlockItem::new).create();

	/* Decorative stone and brick block sets */

	// Ash bricks
	public static final StoneFamily ASH_BRICKS = stoneFamily("ash_bricks", MapColor.COLOR_LIGHT_GRAY, SoundType.MUD_BRICKS);
	public static final FeatureBlock<Block, BlockItem> CHISELED_ASH_BRICKS = chiseled("ash_bricks", MapColor.COLOR_LIGHT_GRAY, SoundType.MUD_BRICKS);

	// Wax bricks
	public static final StoneFamily WAX_BRICKS = stoneFamily("wax_bricks", MapColor.COLOR_YELLOW, SoundType.MUD);
	public static final FeatureBlock<Block, BlockItem> CHISELED_WAX_BRICKS = chiseled("wax_bricks", MapColor.COLOR_YELLOW, SoundType.MUD);

	// Refractory wax bricks
	public static final StoneFamily REFRACTORY_WAX_BRICKS = stoneFamily("refractory_wax_bricks", MapColor.COLOR_RED, SoundType.MUD);
	public static final FeatureBlock<Block, BlockItem> CHISELED_REFRACTORY_WAX_BRICKS = chiseled("refractory_wax_bricks", MapColor.COLOR_RED, SoundType.MUD);

	public static final StoneSet WAXSTONE = stoneSet("waxstone", MapColor.SAND, SoundType.CANDLE);
	public static final StoneSet REFRACTORY_WAXSTONE = stoneSet("refractory_waxstone", MapColor.SAND, SoundType.CANDLE);
	public static final StoneSet HONEYSTONE = stoneSet("honeystone", MapColor.COLOR_ORANGE, SoundType.CANDLE);

	// Misc
	public static final FeatureBlock<Block, BlockItem> ASHEN_WAX_BLOCK = REGISTRY.block(Block::new, mudBrickProperties(Blocks.MUD_BRICKS, MapColor.SAND, SoundType.MUD_BRICKS), ForestryBlockItem::new, "ashen_wax_block");
	public static final FeatureBlock<Block, BlockItem> CRISPY_HONEY_BLOCK = REGISTRY.block(Block::new, mudBrickProperties(Blocks.MUD_BRICKS, MapColor.COLOR_BROWN, SoundType.MUD_BRICKS), ForestryBlockItem::new, "crispy_honey_block");

	/* Metal plating */
	public static final FeatureBlockGroup<Block, MetalPlatingBlockType> METAL_PLATING = REGISTRY.blockGroup((properties, type) -> new Block(properties), List.of(MetalPlatingBlockType.values()))
		.blockProperties((properties, type) -> Properties.ofFullCopy(Blocks.WAXED_COPPER_BLOCK).mapColor(type.getMapColor()))
		.item(ForestryBlockItem::new)
		.identifier("metal_plating")
		.create();

	/* Candles */
	public static final FeatureBlockGroup<JumboCandleBlock, JumboCandleBlockType> JUMBO_CANDLES = REGISTRY.blockGroup(JumboCandleBlock::new, List.of(JumboCandleBlockType.values()))
		.blockProperties((properties, type) -> Properties.ofFullCopy(Blocks.CANDLE)
			.mapColor(type.getMapColor())
			.lightLevel(state -> candleLight(state.getValue(JumboCandleBlock.LIT), type == JumboCandleBlockType.REFRACTORY)))
		.item(ForestryBlockItem::new)
		.identifier("jumbo_candle")
		.create();

	public static final FeatureBlockGroup<BigCandleBlock, BigCandleBlockType> BIG_CANDLES = REGISTRY.blockGroup(BigCandleBlock::new, List.of(BigCandleBlockType.values()))
		.blockProperties((properties, type) -> Properties.ofFullCopy(Blocks.CANDLE)
			.lightLevel(state -> candleLight(state.getValue(BigCandleBlock.LIT), type == BigCandleBlockType.REFRACTORY)))
		.item(ForestryBlockItem::new)
		.identifier("big_candle")
		.create();

	public static final FeatureBlock<CandleBlock, BlockItem> RAINBOW_CANDLE = REGISTRY.block(CandleBlock::new, () -> Properties.ofFullCopy(Blocks.MAGENTA_CANDLE), ForestryBlockItem::new, "rainbow_candle");
	public static final FeatureBlock<RefractoryCandleBlock, BlockItem> REFRACTORY_CANDLE = REGISTRY.block(RefractoryCandleBlock::new, () -> Properties.ofFullCopy(Blocks.RED_CANDLE)
		.lightLevel(state -> state.getValue(CandleBlock.LIT) ? state.getValue(CandleBlock.CANDLES) * 2 : 0), ForestryBlockItem::new, "refractory_candle");
	// todo the cake variants are still missing

	// for datagen
	public static final List<StoneSet> STONE_SETS = List.of(WAXSTONE, REFRACTORY_WAXSTONE, HONEYSTONE);
	public static final List<StoneFamily> DECORATIVE_FAMILIES = Stream.concat(
		Stream.of(ASH_BRICKS, WAX_BRICKS, REFRACTORY_WAX_BRICKS),
		STONE_SETS.stream().flatMap(set -> set.families().stream())
	).toList();
	public static final List<FeatureBlock<Block, BlockItem>> DECORATIVE_CHISELED = List.of(
		CHISELED_ASH_BRICKS, CHISELED_WAX_BRICKS, CHISELED_REFRACTORY_WAX_BRICKS,
		WAXSTONE.chiseled(), REFRACTORY_WAXSTONE.chiseled(), HONEYSTONE.chiseled()
	);

	public record StoneFamily(FeatureBlock<Block, BlockItem> base, FeatureBlock<StairBlock, BlockItem> stairs, FeatureBlock<SlabBlock, BlockItem> slab, FeatureBlock<WallBlock, BlockItem> wall) {
		public List<FeatureBlock<? extends Block, BlockItem>> features() {
			return List.of(this.base, this.stairs, this.slab, this.wall);
		}

		public Block[] blocks() {
			return new Block[]{this.base.block(), this.stairs.block(), this.slab.block(), this.wall.block()};
		}
	}

	public record StoneSet(StoneFamily stone, StoneFamily cobbled, StoneFamily bricks, StoneFamily polished, FeatureBlock<Block, BlockItem> chiseled) {
		public List<StoneFamily> families() {
			return List.of(this.stone, this.cobbled, this.bricks, this.polished);
		}
	}

	private static StoneFamily stoneFamily(String id, MapColor color, SoundType sound) {
		// shapes cut from an "<x>_bricks" block are named "<x>_brick_<shape>", singular
		String shape = id.endsWith("bricks") ? id.substring(0, id.length() - 1) : id;

		return new StoneFamily(
			REGISTRY.block(Block::new, mudBrickProperties(Blocks.MUD_BRICKS, color, sound), ForestryBlockItem::new, id),
			REGISTRY.block(properties -> new StairBlock(Blocks.MUD_BRICKS.defaultBlockState(), properties), mudBrickProperties(Blocks.MUD_BRICK_STAIRS, color, sound), ForestryBlockItem::new, shape + "_stairs"),
			REGISTRY.block(SlabBlock::new, mudBrickProperties(Blocks.MUD_BRICK_SLAB, color, sound), ForestryBlockItem::new, shape + "_slab"),
			REGISTRY.block(WallBlock::new, mudBrickProperties(Blocks.MUD_BRICK_WALL, color, sound), ForestryBlockItem::new, shape + "_wall")
		);
	}

	private static FeatureBlock<Block, BlockItem> chiseled(String id, MapColor color, SoundType sound) {
		return REGISTRY.block(Block::new, mudBrickProperties(Blocks.MUD_BRICKS, color, sound), ForestryBlockItem::new, "chiseled_" + id);
	}

	private static StoneSet stoneSet(String id, MapColor color, SoundType sound) {
		return new StoneSet(
			stoneFamily(id, color, sound),
			stoneFamily("cobbled_" + id, color, sound),
			stoneFamily(id + "_bricks", color, sound),
			stoneFamily("polished_" + id, color, sound),
			chiseled(id, color, sound)
		);
	}

	private static int candleLight(boolean lit, boolean refractory) {
		if (!lit) {
			return 0;
		}
		return refractory ? 10 : 15;
	}

	private static Supplier<Properties> mudBrickProperties(Block copyFrom, MapColor color, SoundType sound) {
		return () -> Properties.ofFullCopy(copyFrom).mapColor(color).sound(sound);
	}
}

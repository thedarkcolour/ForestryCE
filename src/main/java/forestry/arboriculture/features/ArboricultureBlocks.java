package forestry.arboriculture.features;

import com.mojang.datafixers.util.Function3;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.wood.ForestryWoodType;
import forestry.arboriculture.wood.IWoodTyped;
import forestry.arboriculture.wood.VanillaWoodType;
import forestry.arboriculture.wood.WoodAccess;
import forestry.arboriculture.leaves.DecorativeLeavesBlock;
import forestry.arboriculture.leaves.DefaultLeavesBlock;
import forestry.arboriculture.leaves.DefaultFruitLeavesBlock;
import forestry.arboriculture.wood.ForestryButtonBlock;
import forestry.arboriculture.wood.ForestryDoorBlock;
import forestry.arboriculture.wood.ForestryFenceBlock;
import forestry.arboriculture.wood.ForestryFenceGateBlock;
import forestry.arboriculture.wood.ForestryHangingSignBlock;
import forestry.arboriculture.leaves.ForestryLeavesBlock;
import forestry.arboriculture.wood.ForestryLogBlock;
import forestry.arboriculture.wood.ForestryPlanksBlock;
import forestry.arboriculture.wood.ForestryPressurePlateBlock;
import forestry.arboriculture.wood.ForestrySlabBlock;
import forestry.arboriculture.wood.ForestryStairsBlock;
import forestry.arboriculture.wood.ForestryStandingSignBlock;
import forestry.arboriculture.wood.ForestryTrapdoorBlock;
import forestry.arboriculture.wood.ForestryWallHangingSignBlock;
import forestry.arboriculture.wood.ForestryWallSignBlock;
import forestry.arboriculture.fruit.FruitPodBlock;
import forestry.arboriculture.sapling.SaplingBlock;
import forestry.arboriculture.leaves.ForestryLeafType;
import forestry.arboriculture.fruit.ForestryPodType;
import forestry.arboriculture.leaves.DecorativeLeavesBlockItem;
import forestry.arboriculture.leaves.DefaultLeavesBlockItem;
import forestry.arboriculture.wood.ForestryHangingSignBlockItem;
import forestry.arboriculture.leaves.LeavesBlockItem;
import forestry.arboriculture.wood.SignBlockItem;
import forestry.arboriculture.wood.ForestryWoodBlockItem;
import forestry.arboriculture.wood.ForestryDoorBlockItem;
import forestry.arboriculture.wood.ForestrySlabBlockItem;
import forestry.core.platform.registration.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

@FeatureProvider
public class ArboricultureBlocks {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ARBORICULTURE);

	/* VANILLA LOGS & WOOD */
	public static final FeatureBlockGroup<ForestryLogBlock, VanillaWoodType> LOGS_VANILLA_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.LOG, true, VanillaWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, VanillaWoodType> WOOD_VANILLA_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.WOOD, true, VanillaWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, VanillaWoodType> STRIPPED_LOGS_VANILLA_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_LOG, true, VanillaWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, VanillaWoodType> STRIPPED_WOOD_VANILLA_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_WOOD, true, VanillaWoodType.VALUES);

	/* LOGS & WOOD */
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> LOGS = woodGroup(ForestryLogBlock::new, WoodBlockKind.LOG, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> LOGS_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.LOG, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> STRIPPED_LOGS = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_LOG, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> STRIPPED_LOGS_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_LOG, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> WOOD = woodGroup(ForestryLogBlock::new, WoodBlockKind.WOOD, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> WOOD_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.WOOD, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> STRIPPED_WOOD = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_WOOD, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryLogBlock, ForestryWoodType> STRIPPED_WOOD_FIREPROOF = woodGroup(ForestryLogBlock::new, WoodBlockKind.STRIPPED_WOOD, true, ForestryWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryPlanksBlock, ForestryWoodType> PLANKS = woodGroup(ForestryPlanksBlock::new, WoodBlockKind.PLANKS, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryPlanksBlock, ForestryWoodType> PLANKS_FIREPROOF = woodGroup(ForestryPlanksBlock::new, WoodBlockKind.PLANKS, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryPlanksBlock, VanillaWoodType> PLANKS_VANILLA_FIREPROOF = woodGroup(ForestryPlanksBlock::new, WoodBlockKind.PLANKS, true, VanillaWoodType.VALUES);

	public static final FeatureBlockGroup<ForestrySlabBlock, ForestryWoodType> SLABS = woodGroup((type) -> new ForestrySlabBlock(PLANKS.get(type).block()), ForestrySlabBlockItem::new, WoodBlockKind.SLAB, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestrySlabBlock, ForestryWoodType> SLABS_FIREPROOF = woodGroup((type) -> new ForestrySlabBlock(PLANKS_FIREPROOF.get(type).block()), ForestrySlabBlockItem::new, WoodBlockKind.SLAB, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestrySlabBlock, VanillaWoodType> SLABS_VANILLA_FIREPROOF = woodGroup((type) -> new ForestrySlabBlock(PLANKS_VANILLA_FIREPROOF.get(type).block()), ForestrySlabBlockItem::new, WoodBlockKind.SLAB, true, VanillaWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryFenceBlock, ForestryWoodType> FENCES = woodGroup(ForestryFenceBlock::new, WoodBlockKind.FENCE, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryFenceBlock, ForestryWoodType> FENCES_FIREPROOF = woodGroup(ForestryFenceBlock::new, WoodBlockKind.FENCE, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryFenceBlock, VanillaWoodType> FENCES_VANILLA_FIREPROOF = woodGroup(ForestryFenceBlock::new, WoodBlockKind.FENCE, true, VanillaWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryFenceGateBlock, ForestryWoodType> FENCE_GATES = woodGroup(ForestryFenceGateBlock::new, WoodBlockKind.FENCE_GATE, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryFenceGateBlock, ForestryWoodType> FENCE_GATES_FIREPROOF = woodGroup(ForestryFenceGateBlock::new, WoodBlockKind.FENCE_GATE, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryFenceGateBlock, VanillaWoodType> FENCE_GATES_VANILLA_FIREPROOF = woodGroup(ForestryFenceGateBlock::new, WoodBlockKind.FENCE_GATE, true, VanillaWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryStairsBlock, ForestryWoodType> STAIRS = woodGroup((type) -> new ForestryStairsBlock(PLANKS.get(type).block()), WoodBlockKind.STAIRS, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryStairsBlock, ForestryWoodType> STAIRS_FIREPROOF = woodGroup((type) -> new ForestryStairsBlock(PLANKS_FIREPROOF.get(type).block()), WoodBlockKind.STAIRS, true, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryStairsBlock, VanillaWoodType> STAIRS_VANILLA_FIREPROOF = woodGroup((type) -> new ForestryStairsBlock(PLANKS_VANILLA_FIREPROOF.get(type).block()), WoodBlockKind.STAIRS, true, VanillaWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryDoorBlock, ForestryWoodType> DOORS = woodGroup(ForestryDoorBlock::new, ForestryDoorBlockItem::new, WoodBlockKind.DOOR, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryTrapdoorBlock, ForestryWoodType> TRAPDOORS = woodGroup(ForestryTrapdoorBlock::new, WoodBlockKind.TRAPDOOR, false, ForestryWoodType.VALUES);

	public static final FeatureBlockGroup<ForestryStandingSignBlock, ForestryWoodType> SIGN = registerWood(REGISTRY.blockGroup(ForestryStandingSignBlock::new, Arrays.asList(ForestryWoodType.VALUES)).item(SignBlockItem::new).identifier("sign", FeatureGroup.IdentifierType.SUFFIX).create(), WoodBlockKind.SIGN);
	public static final FeatureBlockGroup<ForestryWallSignBlock, ForestryWoodType> WALL_SIGN = registerWood(REGISTRY.blockGroup(ForestryWallSignBlock::new, Arrays.asList(ForestryWoodType.VALUES)).identifier("wall_sign", FeatureGroup.IdentifierType.SUFFIX).create(), WoodBlockKind.WALL_SIGN);
	public static final FeatureBlockGroup<ForestryHangingSignBlock, ForestryWoodType> HANGING_SIGN = registerWood(REGISTRY.blockGroup(ForestryHangingSignBlock::new, Arrays.asList(ForestryWoodType.VALUES)).item(ForestryHangingSignBlockItem::new).identifier("hanging_sign", FeatureGroup.IdentifierType.SUFFIX).create(), WoodBlockKind.HANGING_SIGN);
	public static final FeatureBlockGroup<ForestryWallHangingSignBlock, ForestryWoodType> WALL_HANGING_SIGN = registerWood(REGISTRY.blockGroup(ForestryWallHangingSignBlock::new, Arrays.asList(ForestryWoodType.VALUES)).identifier("wall_hanging_sign", FeatureGroup.IdentifierType.SUFFIX).create(), WoodBlockKind.WALL_HANGING_SIGN);

	public static final FeatureBlockGroup<ForestryButtonBlock, ForestryWoodType> BUTTON = woodGroup(ForestryButtonBlock::new, WoodBlockKind.BUTTON, false, ForestryWoodType.VALUES);
	public static final FeatureBlockGroup<ForestryPressurePlateBlock, ForestryWoodType> PRESSURE_PLATE = woodGroup(ForestryPressurePlateBlock::new, WoodBlockKind.PRESSURE_PLATE, false, ForestryWoodType.VALUES);

	/* GENETICS */
	public static final FeatureBlock<SaplingBlock, BlockItem> SAPLING_GE = REGISTRY.block(SaplingBlock::new, "sapling_ge");
	public static final FeatureBlock<ForestryLeavesBlock, LeavesBlockItem> LEAVES = REGISTRY.block(ForestryLeavesBlock::new, LeavesBlockItem::new, "leaves");
	public static final FeatureBlockGroup<DefaultLeavesBlock, ForestryLeafType> LEAVES_DEFAULT = REGISTRY.blockGroup(DefaultLeavesBlock::new, ForestryLeafType.values()).item(DefaultLeavesBlockItem::new).identifier("default_leaves", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureBlockGroup<DefaultFruitLeavesBlock, ForestryLeafType> LEAVES_DEFAULT_FRUIT = REGISTRY.blockGroup(DefaultFruitLeavesBlock::new, ForestryLeafType.values()).item(DefaultLeavesBlockItem::new).identifier("default_leaves_fruit", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureBlockGroup<DecorativeLeavesBlock, ForestryLeafType> LEAVES_DECORATIVE = REGISTRY.blockGroup(DecorativeLeavesBlock::new, ForestryLeafType.values()).item(DecorativeLeavesBlockItem::new).identifier("decorative_leaves", FeatureGroup.IdentifierType.SUFFIX).create();
	public static final FeatureBlockGroup<FruitPodBlock, ForestryPodType> PODS = REGISTRY.blockGroup(FruitPodBlock::new, Arrays.asList(ForestryPodType.values())).identifier("pods").create();

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> woodGroup(Function3<WoodBlockKind, Boolean, S, B> constructor, WoodBlockKind kind, boolean fireproof, S[] types) {
		return woodGroup((fireproof1, type) -> constructor.apply(kind, fireproof1, type), ForestryWoodBlockItem::new, kind, fireproof, types);
	}

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> woodGroup(BiFunction<Boolean, S, B> constructor, WoodBlockKind kind, boolean fireproof, S[] types) {
		return woodGroup(constructor, ForestryWoodBlockItem::new, kind, fireproof, types);
	}

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> woodGroup(BiFunction<Boolean, S, B> constructor, Function<B, BlockItem> itemConstructor, WoodBlockKind kind, boolean fireproof, S[] types) {
		return registerWood(REGISTRY.blockGroup((type) -> constructor.apply(fireproof, type), Arrays.asList(types)).item((block, properties) -> itemConstructor.apply(block)).identifier((fireproof ? "fireproof_" : "") + kind.getSerializedName(), FeatureGroup.IdentifierType.SUFFIX).create(), kind);
	}

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> woodGroup(Function<S, B> constructor, WoodBlockKind kind, boolean fireproof, S[] types) {
		return woodGroup(constructor, ForestryWoodBlockItem::new, kind, fireproof, types);
	}

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> woodGroup(Function<S, B> constructor, Function<B, BlockItem> itemConstructor, WoodBlockKind kind, boolean fireproof, S[] types) {
		return registerWood(REGISTRY.blockGroup(constructor, Arrays.asList(types)).item((block, properties) -> itemConstructor.apply(block)).identifier((fireproof ? "fireproof_" : "") + kind.getSerializedName(), FeatureGroup.IdentifierType.SUFFIX).create(), kind);
	}

	private static <B extends Block & IWoodTyped, S extends IWoodType> FeatureBlockGroup<B, S> registerWood(FeatureBlockGroup<B, S> group, WoodBlockKind kind) {
		REGISTRY.addRegistryListener(Registries.ITEM, () -> WoodAccess.INSTANCE.registerFeatures(group, kind));
		return group;
	}
}

package forestry.agriculture.features;

import forestry.agriculture.minifarm.blocks.MinifarmBlockType;
import forestry.agriculture.minifarm.tiles.*;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.tiles.*;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.platform.registration.FeatureProvider;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.platform.registration.IFeatureRegistry;
import forestry.core.platform.registration.ModFeatureRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

@FeatureProvider
public class FarmingBlockEntities {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.FARMING);

	public static final FeatureTileType<MultifarmControlBlockEntity> CONTROL = REGISTRY.tile(MultifarmControlBlockEntity::new, "control", () -> FarmingBlocks.FARM.getRowBlocks(MultifarmBlockType.CONTROL));
	public static final FeatureTileType<MultifarmGearboxBlockEntity> GEARBOX = REGISTRY.tile(MultifarmGearboxBlockEntity::new, "gearbox", () -> FarmingBlocks.FARM.getRowBlocks(MultifarmBlockType.GEARBOX));
	public static final FeatureTileType<MultifarmHatchBlockEntity> HATCH = REGISTRY.tile(MultifarmHatchBlockEntity::new, "hatch", () -> FarmingBlocks.FARM.getRowBlocks(MultifarmBlockType.HATCH));
	public static final FeatureTileType<PlainMultifarmBlockEntity> PLAIN = REGISTRY.tile(PlainMultifarmBlockEntity::new, "plain", () -> FarmingBlocks.FARM.getRowBlocks(MultifarmBlockType.PLAIN));
	public static final FeatureTileType<MultifarmValveBlockEntity> VALVE = REGISTRY.tile(MultifarmValveBlockEntity::new, "valve", () -> FarmingBlocks.FARM.getRowBlocks(MultifarmBlockType.VALVE));

	public static final FeatureTileType<ArboretumBlockEntity> ARBORETUM = minifarm("arboretum", () -> MinifarmBlockType.ARBORETUM, ArboretumBlockEntity::new);
	public static final FeatureTileType<PeatBogBlockEntity> BOG = minifarm("bog", () -> MinifarmBlockType.PEAT_POG, PeatBogBlockEntity::new);
	public static final FeatureTileType<CropFarmBlockEntity> CROPS = minifarm("crops", () -> MinifarmBlockType.FARM_CROPS, CropFarmBlockEntity::new);
	public static final FeatureTileType<EnderFarmBlockEntity> ENDER = minifarm("ender", () -> MinifarmBlockType.FARM_ENDER, EnderFarmBlockEntity::new);
	public static final FeatureTileType<GoardFarmBlockEntity> GOURD = minifarm("gourd", () -> MinifarmBlockType.FARM_GOURD, GoardFarmBlockEntity::new);
	public static final FeatureTileType<MushroomFarmBlockEntity> MUSHROOM = minifarm("mushroom", () -> MinifarmBlockType.FARM_MUSHROOM, MushroomFarmBlockEntity::new);
	public static final FeatureTileType<InfernalFarmBlockEntity> NETHER = minifarm("nether", () -> MinifarmBlockType.FARM_NETHER, InfernalFarmBlockEntity::new);

	// type supplier is needed to avoid class init cycle
	private static <T extends BlockEntity> FeatureTileType<T> minifarm(String id, Supplier<MinifarmBlockType> type, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.tile(supplier, id, () -> {
			MinifarmBlockType t = type.get();
			return List.of(FarmingBlocks.MANAGED_MINIFARM.get(t).block(), FarmingBlocks.MANUAL_MINIFARM.get(t).block());
		});
	}
}

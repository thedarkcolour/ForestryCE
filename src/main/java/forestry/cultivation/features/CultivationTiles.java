package forestry.cultivation.features;

import java.util.List;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import forestry.api.modules.ForestryModuleIds;
import forestry.cultivation.blocks.BlockTypePlanter;
import forestry.cultivation.tiles.TileArboretum;
import forestry.cultivation.tiles.TileBog;
import forestry.cultivation.tiles.TileFarmCrops;
import forestry.cultivation.tiles.TileFarmEnder;
import forestry.cultivation.tiles.TileFarmGourd;
import forestry.cultivation.tiles.TileFarmMushroom;
import forestry.cultivation.tiles.TileFarmNether;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureTileType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class CultivationTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.CULTIVATION);

	public static final FeatureTileType<TileArboretum> ARBORETUM = createTile("arboretum", BlockTypePlanter.ARBORETUM, TileArboretum::new);
	public static final FeatureTileType<TileBog> BOG = createTile("bog", BlockTypePlanter.PEAT_POG, TileBog::new);
	public static final FeatureTileType<TileFarmCrops> CROPS = createTile("crops", BlockTypePlanter.FARM_CROPS, TileFarmCrops::new);
	public static final FeatureTileType<TileFarmEnder> ENDER = createTile("ender", BlockTypePlanter.FARM_ENDER, TileFarmEnder::new);
	public static final FeatureTileType<TileFarmGourd> GOURD = createTile("gourd", BlockTypePlanter.FARM_GOURD, TileFarmGourd::new);
	public static final FeatureTileType<TileFarmMushroom> MUSHROOM = createTile("mushroom", BlockTypePlanter.FARM_MUSHROOM, TileFarmMushroom::new);
	public static final FeatureTileType<TileFarmNether> NETHER = createTile("nether", BlockTypePlanter.FARM_NETHER, TileFarmNether::new);

	private static <T extends BlockEntity> FeatureTileType<T> createTile(String id, BlockTypePlanter type, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.tile(supplier, id, () -> List.of(CultivationBlocks.MANAGED_PLANTER.get(type).block(), CultivationBlocks.MANUAL_PLANTER.get(BlockTypePlanter.ARBORETUM).block()));
	}
}

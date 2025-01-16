package forestry.arboriculture.features;

import java.util.stream.Stream;

import net.minecraft.world.level.block.entity.SignBlockEntity;

import forestry.api.modules.ForestryModuleIds;
import forestry.arboriculture.tiles.TileFruitPod;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.arboriculture.tiles.TileSapling;
import forestry.modules.features.FeatureProvider;
import forestry.modules.features.FeatureTileType;
import forestry.modules.features.IFeatureRegistry;
import forestry.modules.features.ModFeatureRegistry;

@FeatureProvider
public class ArboricultureTiles {
	private static final IFeatureRegistry REGISTRY = ModFeatureRegistry.get(ForestryModuleIds.ARBORICULTURE);

	public static final FeatureTileType<TileSapling> SAPLING = REGISTRY.tile(TileSapling::new, "sapling", ArboricultureBlocks.SAPLING_GE::collect);
	public static final FeatureTileType<TileLeaves> LEAVES = REGISTRY.tile(TileLeaves::new, "leaves", ArboricultureBlocks.LEAVES::collect);
	public static final FeatureTileType<TileFruitPod> PODS = REGISTRY.tile(TileFruitPod::new, "pods", ArboricultureBlocks.PODS::getBlocks);

	public static final FeatureTileType<SignBlockEntity> SIGN = REGISTRY.tile((pos, state) -> new SignBlockEntity(ArboricultureTiles.SIGN.tileType(), pos, state), "sign", () -> Stream.concat(ArboricultureBlocks.SIGN.getList().stream(), ArboricultureBlocks.WALL_SIGN.getList().stream()).toList());
	public static final FeatureTileType<SignBlockEntity> HANGING_SIGN = REGISTRY.tile((pos, state) -> new SignBlockEntity(ArboricultureTiles.SIGN.tileType(), pos, state), "hanging_sign", () -> Stream.concat(ArboricultureBlocks.HANGING_SIGN.getList().stream(), ArboricultureBlocks.WALL_HANGING_SIGN.getList().stream()).toList());
}

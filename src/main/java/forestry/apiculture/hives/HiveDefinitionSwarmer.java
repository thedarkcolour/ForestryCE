package forestry.apiculture.hives;

import forestry.api.ForestryTags;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveGen;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.apiculture.blocks.BlockHiveType;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.apiculture.tiles.TileHive;
import forestry.core.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

public class HiveDefinitionSwarmer implements IHiveDefinition {
	private final List<ItemStack> bees;

	public HiveDefinitionSwarmer(ItemStack... bees) {
		this.bees = Arrays.asList(bees);
	}

	@Override
	public IHiveGen getHiveGen() {
		return new HiveGenGround(ForestryTags.Blocks.SWARM_BEE_GROUND);
	}

	@Override
	public BlockState getBlockState() {
		return ApicultureBlocks.BEEHIVE.get(BlockHiveType.SWARM).defaultState();
	}

	@Override
	public boolean isGoodBiome(Holder<Biome> biome) {
		return true;
	}

	@Override
	public boolean isGoodHumidity(HumidityType humidity) {
		return true;
	}

	@Override
	public boolean isGoodTemperature(TemperatureType temperature) {
		return true;
	}

	@Override
	public float getGenChance() {
		return 128.0f;
	}

	@Override
	public void postGen(WorldGenLevel level, RandomSource rand, BlockPos pos) {
		TileUtil.actOnTile(level, pos, TileHive.class, tile -> tile.setContained(this.bees));
	}
}

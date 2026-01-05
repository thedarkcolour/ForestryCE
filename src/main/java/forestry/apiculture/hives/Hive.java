package forestry.apiculture.hives;

import forestry.api.apiculture.hives.IHive;
import forestry.api.apiculture.hives.IHiveDefinition;
import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public final class Hive implements IHive {
	private final IHiveDefinition hiveDescription;
	private final List<IHiveDrop> drops;
	private final float generationChance;

	public Hive(IHiveDefinition definition, float generationChance, List<IHiveDrop> drops) {
		this.hiveDescription = definition;
		this.generationChance = generationChance;
		this.drops = drops;
	}

	@Override
	public IHiveDefinition getDefinition() {
		return this.hiveDescription;
	}

	@Override
	public BlockState getHiveBlockState() {
		return this.hiveDescription.getBlockState();
	}

	@Override
	public List<IHiveDrop> getDrops() {
		return this.drops;
	}

	@Override
	public float genChance() {
		return this.generationChance;
	}

	@Override
	public void postGen(WorldGenLevel world, RandomSource rand, BlockPos pos) {
		this.hiveDescription.postGen(world, rand, pos);
	}

	@Override
	public boolean isGoodBiome(Holder<Biome> biome) {
		return this.hiveDescription.isGoodBiome(biome);
	}

	@Override
	public boolean isGoodHumidity(HumidityType humidity) {
		return this.hiveDescription.isGoodHumidity(humidity);
	}

	@Override
	public boolean isGoodTemperature(TemperatureType temperature) {
		return this.hiveDescription.isGoodTemperature(temperature);
	}

	@Override
	public boolean isValidLocation(WorldGenLevel world, BlockPos pos) {
		return this.hiveDescription.getHiveGen().isValidLocation(world, pos);
	}

	@Override
	public boolean canReplace(WorldGenLevel world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		return this.hiveDescription.getHiveGen().canReplace(blockState, world, pos);
	}

	@Nullable
	@Deprecated
	@Override
	public BlockPos getPosForHive(WorldGenLevel level, int posX, int posZ) {
		return this.hiveDescription.getHiveGen().getPosForHive(level, posX, posZ);
	}

	@Nullable
	@Override
	public BlockPos getPosForHive(WorldGenLevel level, RandomSource rand, int posX, int posZ) {
		return this.hiveDescription.getHiveGen().getPosForHive(level, rand, posX, posZ);
	}

	@Override
	public String toString() {
		return this.hiveDescription + " hive";
	}
}

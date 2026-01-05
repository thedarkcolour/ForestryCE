package forestry.arboriculture.genetics;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.core.genetics.Individual;
import forestry.core.genetics.mutations.Mutation;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tree extends Individual<ITreeSpecies, ITree, ITreeSpeciesType> implements ITree, IPlantable {
	public static final Codec<Tree> CODEC = RecordCodecBuilder.create(instance -> {
		Codec<IGenome> genomeCodec = SpeciesUtil.TREE_TYPE.get().getKaryotype().getGenomeCodec();

		return Individual.fields(instance, genomeCodec).apply(instance, Tree::new);
	});

	public Tree(IGenome genome) {
		super(genome);
	}

	private Tree(IGenome genome, Optional<IGenome> mate, boolean analyzed) {
		super(genome, mate, analyzed);
	}

	/* EFFECTS */
	@Override
	public IEffectData[] doEffect(IEffectData[] storedData, Level level, BlockPos pos) {
		ITreeEffect effect = this.genome.getActiveValue(TreeChromosomes.EFFECT);

		storedData[0] = doEffect(effect, storedData[0], level, pos);

		// Return here if the primary can already not be combined
		if (!effect.isCombinable()) {
			return storedData;
		}

		ITreeEffect secondary = this.genome.getInactiveValue(TreeChromosomes.EFFECT);
		if (!secondary.isCombinable()) {
			return storedData;
		}

		storedData[1] = doEffect(secondary, storedData[1], level, pos);

		return storedData;
	}

	private IEffectData doEffect(ITreeEffect effect, IEffectData storedData, Level world, BlockPos pos) {
		storedData = effect.validateStorage(storedData);
		return effect.doEffect(getGenome(), storedData, world, pos);
	}

	@Override
	public IEffectData[] doFX(IEffectData[] storedData, Level level, BlockPos pos) {
		return storedData;
	}

	/* GROWTH */
	@Override
	public Feature<NoneFeatureConfiguration> getTreeGenerator(WorldGenLevel level, BlockPos pos, boolean wasBonemealed) {
		return this.species.getGenerator().getTreeFeature(getSpecies());
	}

	@Override
	public boolean canStay(BlockGetter level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState state = level.getBlockState(below);

		Block block = state.getBlock();
		return block.canSustainPlant(state, level, below, Direction.UP, this);
	}

	// IPlantable
	@Override
	public BlockState getPlant(BlockGetter level, BlockPos pos) {
		return level.getBlockState(pos);
	}

	// IPlantable
	@Override
	public PlantType getPlantType(BlockGetter level, BlockPos pos) {
		return PlantType.PLAINS;
	}

	@Override
	public int getRequiredMaturity() {
		return this.genome.getActiveValue(TreeChromosomes.MATURATION);
	}

	@Override
	public int getResilience() {
		int base = (int) (getGenome().getActiveValue(TreeChromosomes.SAPLINGS) * getGenome().getActiveValue(TreeChromosomes.SAPPINESS) * 100);
		return (Math.max(base, 1)) * 10;
	}

	/* REPRODUCTION */
	@Override
	public List<ITree> getSaplings(Level level, BlockPos pos, @Nullable GameProfile playerProfile, float modifier) {
		List<ITree> prod = new ArrayList<>();

		float chance = this.genome.getActiveValue(TreeChromosomes.SAPLINGS) * modifier;

		if (level.random.nextFloat() <= chance) {
			if (this.mate == null) {
				prod.add(copy());
			} else {
				SpeciesUtil.ISpeciesMutator mutator = (p1, p2) -> mutateSpecies(level, playerProfile, pos, p1, p2);
				prod.add(SpeciesUtil.createOffspring(level.random, this.genome, this.mate, mutator, Tree::new));
			}
		}

		return prod;
	}

	@Nullable
	private static ImmutableList<AllelePair<?>> mutateSpecies(Level treeLevel, @Nullable GameProfile profile, BlockPos treePos, IGenome parent1, IGenome parent2) {
		return SpeciesUtil.mutateSpecies(treeLevel, treePos, profile, parent1, parent2, TreeChromosomes.SPECIES, Mutation::getChance);
	}

	/* PRODUCTION */
	@Override
	public boolean hasFruitLeaves() {
		return this.genome.getActiveValue(TreeChromosomes.FRUIT).isFruitLeaf();
	}

	@Override
	public List<IProduct> getProducts() {
		return this.genome.getActiveValue(TreeChromosomes.FRUIT).getProducts();
	}

	@Override
	public List<IProduct> getSpecialties() {
		return this.genome.getActiveValue(TreeChromosomes.FRUIT).getSpecialty();
	}

	@Override
	public List<ItemStack> produceStacks(Level level, BlockPos pos, int ripeningTime) {
		return this.genome.getActiveValue(TreeChromosomes.FRUIT).getFruits(this.genome, level, ripeningTime);
	}
}

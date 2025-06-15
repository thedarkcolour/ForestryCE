package forestry.api.arboriculture.genetics;

import com.mojang.authlib.GameProfile;
import forestry.api.core.IProductProducer;
import forestry.api.core.ISpecialtyProducer;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IIndividual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nullable;
import java.util.List;

public interface ITree extends IIndividual, IProductProducer, ISpecialtyProducer {
	IEffectData[] doEffect(IEffectData[] storedData, Level level, BlockPos pos);

	/**
	 * Unimplemented. Will eventually be used to display client-only effects.
	 *
	 * @param storedData The effect data used to store information about effect state.
	 * @param level      The world to display the effect in.
	 * @param pos        The position of the leaves block where the effect should be displayed.
	 * @return storedData
	 */
	IEffectData[] doFX(IEffectData[] storedData, Level level, BlockPos pos);

	List<ITree> getSaplings(Level level, BlockPos pos, @Nullable GameProfile playerProfile, float modifier);

	List<ItemStack> produceStacks(Level level, BlockPos pos, int ripeningTime);

	/**
	 * @return Boolean indicating whether a sapling can stay planted at the given position.
	 */
	boolean canStay(BlockGetter level, BlockPos pos);

	/**
	 * @return {@code true} this tree's fruits grow in its leaves, like the Apple Oak.
	 */
	boolean hasFruitLeaves();

	/**
	 * @return Integer denoting the maturity (block ticks) required for a sapling to attempt to grow into a tree.
	 */
	int getRequiredMaturity();

	/**
	 * @return Integer denoting how resilient leaf blocks are against adverse influences (i.e. caterpillars).
	 */
	int getResilience();

	Feature<NoneFeatureConfiguration> getTreeGenerator(WorldGenLevel level, BlockPos pos, boolean wasBonemealed);

	ITree copy();

	@Override
	ITreeSpeciesType getType();

	@Override
	ITreeSpecies getSpecies();

	@Override
	ITreeSpecies getInactiveSpecies();
}

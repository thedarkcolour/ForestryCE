package forestry.api.plugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;

public interface ITreeSpeciesBuilder extends ISpeciesBuilder<ITreeSpeciesType, ITreeSpeciesBuilder> {
	/**
	 * Shortcut to create a tree generator using the tree generator built into Forestry.
	 */
	ITreeSpeciesBuilder setTreeFeature(Function<ITreeGenData, Feature<NoneFeatureConfiguration>> factory);

	ITreeSpeciesBuilder setGenerator(ITreeGenerator generator);

	/**
	 * Adds mundane states (no genetic BE) that Forestry should consider as members of this species.
	 * When Forestry encounters a mundane leaf/sapling block, a list of states are queried to see what individual
	 * the block should contain. The genome of this individual always has the DEFAULT genome.
	 * <p>
	 * An example where this is used is to treat vanilla Oak leaves and saplings as members of the Apple Oak species.
	 * There is another important case: treating "decorative" and "default" leaves as members of a species.
	 * Forestry adds default and decorative forms of all leaf blocks to avoid generating trees with tons of BEs.
	 * These forms lack BEs, so these block states are passed to this method as well.
	 *
	 * @param states A list of mundane (no BE) states to add as members of this species.
	 */
	ITreeSpeciesBuilder addVanillaStates(List<BlockState> states);

	/**
	 * Sets the decorative leaves block for this tree species. Used by shears and pick-block.
	 * The decorative form has no genome or block entity which is better for performance, but has no functionality.
	 *
	 * @param stack The item form of the decorative leaves for this species.
	 * @return The decorative form of this species's leaves block.
	 */
	ITreeSpeciesBuilder setDecorativeLeaves(ItemStack stack);

	// todo not sure if this is needed
	ITreeSpeciesBuilder setHasFruitLeaves(boolean hasFruitLeaves);

	/**
	 * Overrides the wood type set in {@link IArboricultureRegistration#registerSpecies}.
	 */
	ITreeSpeciesBuilder setWoodType(IWoodType woodType);

	/**
	 * Sets the rarity for this tree to generate during world generation.
	 *
	 * @param rarity A float between 0 and 1 that determines how often this tree spawns naturally.
	 */
	ITreeSpeciesBuilder setRarity(float rarity);

	@Nullable
	ITreeGenerator getGenerator();

	List<BlockState> getVanillaLeafStates();

	ItemStack getDecorativeLeaves();

	boolean hasFruitLeaves();

	float getRarity();
}
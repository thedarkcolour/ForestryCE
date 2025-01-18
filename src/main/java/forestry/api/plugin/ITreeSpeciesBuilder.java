package forestry.api.plugin;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import forestry.api.arboriculture.ITreeGenData;
import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;

public interface ITreeSpeciesBuilder extends ISpeciesBuilder<ITreeSpeciesType, ITreeSpecies, ITreeSpeciesBuilder> {
	/**
	 * Shortcut to create a tree generator using the tree generator built into Forestry.
	 */
	ITreeSpeciesBuilder setTreeFeature(Function<ITreeGenData, Feature<NoneFeatureConfiguration>> factory);

	/**
	 * Sets the tree generator instance used to generate trees when growing from a sapling or being placed in the world.
	 *
	 * @param generator The tree generator instance. Use {@link #setTreeFeature} to use Forestry's default generator.
	 */
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
	ITreeSpeciesBuilder addVanillaStates(Collection<BlockState> states);

	/**
	 * Adds a mundane item (no genetic component/NBT) that Forestry should consider as members of this species.
	 * Used by the portable analyzer to convert vanilla saplings into their Forestry equivalents when analyzing.
	 *
	 * @param sapling The item that should count as this species.
	 */
	ITreeSpeciesBuilder addVanillaSapling(Item sapling);

	/**
	 * Sets the decorative leaves block for this tree species. Used by shears and pick-block.
	 * The decorative form has no genome or block entity which is better for performance, but has no functionality.
	 * Calling this method is not required but highly recommended.
	 *
	 * @param stack The item form of the decorative leaves for this species.
	 * @return The decorative form of this species's leaves block.
	 */
	ITreeSpeciesBuilder setDecorativeLeaves(ItemStack stack);

	/**
	 * Overrides the wood type set in {@link IArboricultureRegistration#registerSpecies}.
	 */
	ITreeSpeciesBuilder setWoodType(IWoodType woodType);

	/**
	 * Sets the rarity for this tree to generate in a chunk during world generation.
	 * By default, the rarity is 0, which means the tree never spawns naturally.
	 *
	 * @param rarity A float between 0 and 1 that determines how often this tree spawns naturally.
	 */
	ITreeSpeciesBuilder setRarity(float rarity);

	/**
	 * Overrides the escritoire color set in {@link IArboricultureRegistration#registerSpecies}.
	 *
	 * @param color The color.
	 * @see ISpeciesBuilder#setEscritoireColor
	 */
	@Override
	ITreeSpeciesBuilder setEscritoireColor(TextColor color);

	@Nullable
	ITreeGenerator getGenerator();

	List<BlockState> getVanillaLeafStates();

	List<Item> getVanillaSaplingItems();

	ItemStack getDecorativeLeaves();

	float getRarity();
}

package forestry.api.plugin;

import java.awt.Color;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITreeEffect;

/**
 * Register your tree species, fruits, and effects here.
 */
public interface IArboricultureRegistration {
	/**
	 * Register a new tree species.
	 *
	 * @param id              The unique ID of this species. The path must start with "tree_".
	 * @param genus           The genus of this species. See {@link forestry.api.genetics.ForestryTaxa} for examples.
	 * @param species         The species name of this species, used for scientific naming.
	 * @param dominant        Whether the allele for this species is dominant or recessive.
	 * @param escritoireColor The primary color of this tree species. Used for pollen colors and tree leaf tinting.
	 * @param woodType        The wood type of this tree species.
	 */
	ITreeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, TextColor escritoireColor, IWoodType woodType);

	/**
	 * @deprecated Use the variant that accepts a TextColor
	 */
	@Deprecated(forRemoval = true)
	default ITreeSpeciesBuilder registerSpecies(ResourceLocation id, String genus, String species, boolean dominant, Color escritoireColor, IWoodType woodType) {
		return registerSpecies(id, genus, species, dominant, TextColor.fromRgb(escritoireColor.getRGB()), woodType);
	}

	/**
	 * Register a new type of fruit.
	 *
	 * @param id    The unique ID of this fruit. See {@link forestry.api.arboriculture.ForestryFruits} for defaults.
	 * @param fruit The fruit object to be wrapped in an allele for use in a tree genome.
	 */
	void registerFruit(ResourceLocation id, IFruit fruit);

	/**
	 * Registers a tree effect. There are no tree effects in base Forestry.
	 *
	 * @param id     The unique ID of this tree effect.
	 * @param effect The effect object to be wrapped in an allele for use in a tree genome.
	 */
	void registerTreeEffect(ResourceLocation id, ITreeEffect effect);

	/**
	 * Registers a block to be waxable with Refractory Wax. In base Forestry, all planks are registered
	 * to be waxed into their fireproof counterparts.
	 *
	 * @param block     The unwaxed block, such as Oak Planks
	 * @param waxedForm The waxed block, such as Oak Planks (Fireproof)
	 * @since 2.6.0
	 */
	void registerRefractoryWaxable(Block block, Block waxedForm);

	/**
	 * Registers a material for use in a Log Pile charcoal pit.
	 *
	 * @param state    The state used to surround a pit of burning Log Pile blocks when making charcoal.
	 * @param charcoal The amount of charcoal produced when using this block.
	 * @since 2.6.0
	 */
	void registerCharcoalPitWall(BlockState state, int charcoal);

	/**
	 * @since 2.6.0
	 */
	default void registerCharcoalPitWall(Block block, int charcoal) {
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			registerCharcoalPitWall(state, charcoal);
		}
	}
}

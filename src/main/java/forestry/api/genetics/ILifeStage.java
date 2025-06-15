package forestry.api.genetics;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

// todo add "getTranslationKey" that accepts a species so that moths and butterflies can have different names

/**
 * The replacement for the old ISpeciesType class. Denotes different forms of the same species (ex. larvae, princess, queen, drone)
 *
 * @see forestry.api.apiculture.bee.BeeLifeStage
 * @see forestry.api.arboriculture.genetics.TreeLifeStage
 * @see forestry.api.lepidopterology.genetics.ButterflyLifeStage
 */
public interface ILifeStage extends StringRepresentable {
	/**
	 * @return The simple name of this life stage (ex. {@code "drone"}), used in commands.
	 */
	@Override
	String getSerializedName();

	/**
	 * @return The item form of this life stage. Every life stage is associated with an item form.
	 */
	Item getItemForm();
}

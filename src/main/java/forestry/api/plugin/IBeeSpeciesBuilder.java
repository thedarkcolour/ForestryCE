package forestry.api.plugin;

import java.awt.Color;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.IBeeJubilance;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.core.Product;

/**
 * Builder used to register new bee species and configure already existing ones.
 * Use {@link IApicultureRegistration#registerSpecies} to obtain instances of this class.
 */
public interface IBeeSpeciesBuilder extends ISpeciesBuilder<IBeeSpeciesType, IBeeSpecies, IBeeSpeciesBuilder> {
	/**
	 * Adds a product to this species.
	 *
	 * @param product A product that can be produced by this species.
	 */
	IBeeSpeciesBuilder addProduct(IProduct product);

	/**
	 * Adds a product to this bee species.
	 *
	 * @param stack  The item stack the product should produce.
	 * @param chance A float between 0 and 1. The chance that this product is produced during a single work cycle.
	 */
	default IBeeSpeciesBuilder addProduct(ItemStack stack, float chance) {
		return addProduct(new Product(stack.getItem(), stack.getCount(), stack.getTag(), chance));
	}

	/**
	 * Adds a specialty product to this species, only produced when the bee in a jubilant state.
	 *
	 * @param specialty A product that can only be produced by this species when in its jubilant state.
	 */
	IBeeSpeciesBuilder addSpecialty(IProduct specialty);

	/**
	 * Adds a specialty to the bee species, a product only produced when the bee is in a jubilant state.
	 *
	 * @param stack  The item stack the product should produce.
	 * @param chance A float between 0 and 1. The chance that this product is produced during a single work cycle.
	 */
	default IBeeSpeciesBuilder addSpecialty(ItemStack stack, float chance) {
		return addSpecialty(new Product(stack.getItem(), stack.getCount(), stack.getTag(), chance));
	}

	/**
	 * Sets the color of the bee's body. The default is yellow, {@code #ffdc16}, used by most bees.
	 */
	IBeeSpeciesBuilder setBody(Color color);

	/**
	 * Sets the color of the bee's stripes. The default is {@link Color#BLACK}.
	 */
	IBeeSpeciesBuilder setStripes(Color color);

	/**
	 * Overrides the default bee outlines set in {@link IApicultureRegistration#registerSpecies}.
	 */
	IBeeSpeciesBuilder setOutline(Color color);

	/**
	 * Specify the jubilance conditions for this bee species. The default returns true if the bee's ideal temperature and humidity are met.
	 * When {@link IBeeJubilance#isJubilant} returns true, a bee can produce its specialty products.
	 */
	IBeeSpeciesBuilder setJubilance(IBeeJubilance jubilance);

	List<IProduct> buildProducts();

	List<IProduct> buildSpecialties();

	int getBody();

	int getStripes();

	int getOutline();

	IBeeJubilance getJubilance();
}

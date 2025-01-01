package forestry.api.plugin;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.hives.IHiveDrop;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;

public interface IHiveBuilder {
	default IHiveBuilder addDrop(double chance, ResourceLocation speciesId, Supplier<List<ItemStack>> extraItems) {
		return addDrop(chance, speciesId, extraItems, 0f, Map.of());
	}

	default IHiveBuilder addDrop(double chance, ResourceLocation speciesId, Supplier<List<ItemStack>> extraItems, float ignobleChance) {
		return addDrop(chance, speciesId, extraItems, ignobleChance, Map.of());
	}

	/**
	 * Adds a bee to this hive's drops, along with any extra items associated with that bee.
	 * Princess drops are rolled up to 10 times or until a princess drop is added, whichever comes first.
	 * Drone drops and extra drops are rolled only once.
	 *
	 * @param chance        The chance for this drop to be chosen per roll.
	 * @param speciesId     The ID of the bee species to add.
	 * @param extraItems    The extra items that should drop with this species.
	 * @param ignobleChance The chance that the princess dropped is Ignoble Stock instead of <i>Pristine Stock</i>.
	 * @param alleles       A map of alleles to be set on the bee when it is dropped.
	 */
	IHiveBuilder addDrop(double chance, ResourceLocation speciesId, Supplier<List<ItemStack>> extraItems, float ignobleChance, Map<IChromosome<?>, IAllele> alleles);

	/**
	 * Used to add custom implementations of {@link IHiveDrop} to this hive's drops.
	 *
	 * @param drop The supplier of the drop. Called after items and bee species are all registered.
	 */
	IHiveBuilder addCustomDrop(IHiveDrop drop);

	/**
	 * Changes the generation chance of the hive.
	 * @since 1.1.1
	 */
	void setGenerationChance(float generationChance);
}

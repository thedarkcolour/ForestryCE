
package forestry.api.core.backpacks;

import forestry.api.core.genetics.ISpeciesType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * The Backpack Interface allows you to create your own backpacks.
 * Get the instance from {@link forestry.api.IForestryApi#getBackpackInterface()}.
 * <p>
 * To create your own backpack, create an {@link IBackpackDefinition}.
 * Backpack definitions have a filter, which you can create here with
 * {@link #createBackpackFilter(TagKey, TagKey)}
 * or {@link #createNaturalistBackpackFilter(ResourceLocation)}
 * or create your own.
 * <p>
 * After you've created your backpack definition, create the item with
 * {@link #createBackpack(IBackpackDefinition, EnumBackpackType)}
 * or {@link #createNaturalistBackpack(IBackpackDefinition, ResourceLocation)}
 * and then register the returned item like any other item.
 */
public interface IBackpackInterface {

	/**
	 * Creates a backpack with the given UID and type, returning the item.
	 *
	 * @param definition The backpack definition.
	 * @param type       Type of backpack.
	 * @return Created backpack item.
	 */
	Item createBackpack(IBackpackDefinition definition, EnumBackpackType type);

	/**
	 * Create a backpack that can hold items from a specific {@link ISpeciesType}.
	 *
	 * @param definition    The backpack definition.
	 * @param speciesTypeId The species root.
	 * @return Created backpack item.
	 */
	Item createNaturalistBackpack(IBackpackDefinition definition, ResourceLocation speciesTypeId);

	/**
	 * Makes a new backpack filter that accepts items in the allow tag, unless they are also in the reject tag.
	 * Forestry's own backpacks use this filter with tags like {@code forestry:backpack/allow/miner}.
	 * Item tags load with data packs, so the filter reads the tags lazily on its first test.
	 *
	 * @param allowTag  The tag of items the backpack accepts
	 * @param rejectTag The tag of items the backpack rejects, even if they are in the allow tag
	 * @return a new backpack filter for the specified tags
	 * @since 3.0.0
	 */
	Predicate<ItemStack> createBackpackFilter(TagKey<Item> allowTag, TagKey<Item> rejectTag);

	/**
	 * Makes a new naturalist backpack filter. Only accepts items from a specific {@link ISpeciesType}.
	 * Useful for implementing {@link IBackpackDefinition} for naturalist's backpacks.
	 *
	 * @param speciesTypeId The species root's unique ID.
	 * @return a new backpack filter for the specified species root
	 * @see #createNaturalistBackpack(IBackpackDefinition, ResourceLocation)
	 */
	Predicate<ItemStack> createNaturalistBackpackFilter(ResourceLocation speciesTypeId);
}

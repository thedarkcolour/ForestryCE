package forestry.api.core.backpacks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * To make your own backpack, create a backpack definition and pass it to
 * {@link IBackpackInterface#createBackpack(IBackpackDefinition, EnumBackpackType)}.
 */
public interface IBackpackDefinition {
	/**
	 * @return Human-readable name of the backpack.
	 */
	Component getName(ItemStack backpack);

	/**
	 * @return Primary color for the backpack icon.
	 */
	int getPrimaryColour();

	/**
	 * @return Secondary color for backpack icon, normally white.
	 */
	int getSecondaryColour();

	/**
	 * Filters items that can be put into a backpack.
	 * <p>
	 * For Backpack Implementers: you can create a new filter with
	 * {@link IBackpackInterface#createBackpackFilter(net.minecraft.tags.TagKey, net.minecraft.tags.TagKey)} or
	 * {@link IBackpackInterface#createNaturalistBackpackFilter(net.minecraft.resources.ResourceLocation)}
	 * or implement your own.
	 *
	 * @return the backpack's item filter.
	 */
	Predicate<ItemStack> getFilter();
}

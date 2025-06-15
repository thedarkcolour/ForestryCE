package forestry.api.storage;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Defines appearance and behavior of a backpack, for use in {@link forestry.storage.items.ItemBackpack} and its inheritors.
 *
 * @param primaryColor   Primary color for the backpack icon, comprising the cloth part of the backpack.
 * @param secondaryColor Secondary color for backpack icon, normally white.
 *                       Can be used to tint the {@code layer1} texture if using a custom backpack model.
 * @param filter         Filters items that can be put into a backpack.
 * @see forestry.storage.BackpackFilter
 * @see forestry.storage.NaturalistBackpackFilter
 */
public record BackpackDefinition(int primaryColor, int secondaryColor, Predicate<ItemStack> filter) {
}

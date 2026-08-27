package forestry.core.platform.recipes.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Reads an item data map into a list JEI can display.
 *
 * <p>Call this when a recipe is laid out and not when the category is built. A category is built on
 * resource reload, which happens before the server has sent the client its data maps, so a list
 * built there stays empty for the rest of the session.
 */
public class JeiDataMaps {
	/**
	 * @param type The item data map to read
	 * @return The items with an entry in the data map, paired with their value, in registry order
	 */
	public static <V> List<Entry<V>> entries(DataMapType<Item, V> type) {
		Map<ResourceKey<Item>, V> data = BuiltInRegistries.ITEM.getDataMap(type);
		List<Entry<V>> entries = new ArrayList<>(data.size());

		// Sorted so a slot cycles through its items the same way every time
		for (ResourceKey<Item> key : data.keySet().stream().sorted(Comparator.comparing(ResourceKey::location)).toList()) {
			BuiltInRegistries.ITEM.getOptional(key)
					.ifPresent(item -> entries.add(new Entry<>(new ItemStack(item), data.get(key))));
		}

		return entries;
	}

	/**
	 * @return The items with an entry in the data map, in registry order
	 */
	public static <V> List<ItemStack> stacks(DataMapType<Item, V> type) {
		return entries(type).stream().map(Entry::stack).toList();
	}

	/**
	 * @param stack The item the entry is keyed by
	 * @param value The data map value for that item
	 */
	public record Entry<V>(ItemStack stack, V value) {
	}
}

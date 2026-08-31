package forestry.core.platform.recipes.jei;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.*;
import java.util.stream.Stream;

public class JeiDataMaps {
	public static <V> List<Pair<ItemStack, V>> entries(DataMapType<Item, V> type) {
		Map<ResourceKey<Item>, V> data = BuiltInRegistries.ITEM.getDataMap(type);
		List<Pair<ItemStack, V>> entries = new ArrayList<>(data.size());

		// sorted so a slot cycles through its items the same way every time
		for (ResourceKey<Item> key : sortedMapEntries(type).toList()) {
			BuiltInRegistries.ITEM.getOptional(key)
				.ifPresent(item -> entries.add(new Pair<>(new ItemStack(item), data.get(key))));
		}

		return entries;
	}

	public static <V> Stream<ResourceKey<Item>> sortedMapEntries(DataMapType<Item, V> type) {
		return BuiltInRegistries.ITEM.getDataMap(type)
			.keySet()
			.stream()
			.sorted(Comparator.comparing(ResourceKey::location));
	}

	public static <V> List<ItemStack> stacks(DataMapType<Item, V> type) {
		return sortedMapEntries(type)
			.map(BuiltInRegistries.ITEM::getOptional)
			.filter(Optional::isPresent)
			.map(op -> new ItemStack(op.get()))
			.toList();
	}
}

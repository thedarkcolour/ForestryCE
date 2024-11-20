package forestry.core.utils;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagUtil {
	public static Optional<Holder<Item>> getHolder(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return Optional.empty();
		}
		Item item = itemStack.getItem();
		return getHolder(item, BuiltInRegistries.ITEM);
	}

	public static <T> Optional<Holder<T>> getHolder(T value, Registry<T> registry) {
		return registry.getResourceKey(value)
				.flatMap(registry::getHolder);
	}
}

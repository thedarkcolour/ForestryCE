package forestry.api.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

/**
 * Default implementation of {@link IProduct}. Used in most cases.
 *
 * @param item   The item this product represents.
 * @param count  The count the produced stack should have.
 * @param data   The item's data components, if any, otherwise {@link DataComponentPatch#EMPTY}.
 * @param chance
 */
public record Product(Item item, int count, DataComponentPatch data, float chance) implements IProduct {
	public static final Codec<Product> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Product::item),
		Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Product::count),
		DataComponentPatch.CODEC.optionalFieldOf("data").forGetter(product -> Optional.ofNullable(product.data)),
		Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Product::chance)
	).apply(instance, (item, count, tag, chance) -> new Product(item, count, tag.orElse(null), chance)));
	public static final StreamCodec<RegistryFriendlyByteBuf, Product> STREAM_CODEC = StreamCodec.of(Product::toNetwork, Product::fromNetwork);

	@Override
	public ItemStack createStack() {
		return new ItemStack(this.item.builtInRegistryHolder(), this.count, this.data);
	}

	public static Product of(Item item) {
		return new Product(item, 1, DataComponentPatch.EMPTY, 1f);
	}

	public static Product of(Item item, float chance) {
		return new Product(item, 1, DataComponentPatch.EMPTY, chance);
	}

	public static Product of(Item item, int amount, float chance) {
		return new Product(item, amount, DataComponentPatch.EMPTY, chance);
	}

	public static void toNetwork(RegistryFriendlyByteBuf buffer, Product product) {
		buffer.writeById(BuiltInRegistries.ITEM::getId, product.item);
		buffer.writeByte(product.count);
		DataComponentPatch.STREAM_CODEC.encode(buffer, product.data);
		buffer.writeFloat(product.chance);
	}

	public static Product fromNetwork(RegistryFriendlyByteBuf buffer) {
		Item item = buffer.readById(BuiltInRegistries.ITEM::byId);
		int count = buffer.readByte();
		DataComponentPatch data = DataComponentPatch.STREAM_CODEC.decode(buffer);
		float chance = buffer.readFloat();

		if (item == Items.AIR) {
			throw new IllegalStateException("Received invalid item ID");
		}

		return new Product(item, count, data, chance);
	}
}

package forestry.api.core;

import javax.annotation.Nullable;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Default implementation of {@link IProduct}. Used in most cases.
 *
 * @param item   The item this product represents.
 * @param count  The count the produced stack should have.
 * @param tag    The NBT tag
 * @param chance
 */
public record Product(Item item, int count, @Nullable CompoundTag tag, float chance) implements IProduct {
	public static final Codec<Product> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Product::item),
			Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Product::count),
			CompoundTag.CODEC.optionalFieldOf("tag").forGetter(product -> Optional.ofNullable(product.tag)),
			Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Product::chance)
	).apply(instance, (item, count, tag, chance) -> new Product(item, count, tag.orElse(null), chance)));
	// todo StreamCodec in 1.21

	@Override
	public ItemStack createStack() {
		ItemStack stack = new ItemStack(item, count);
		if (this.tag != null) {
			// defensive copy
			stack.setTag(tag.copy());
		}
		return stack;
	}

	public static Product of(Item item) {
		return new Product(item, 1, null, 1f);
	}

	public static Product of(Item item, int amount, float chance) {
		return new Product(item, amount, null, chance);
	}

	public static void toNetwork(FriendlyByteBuf buffer, Product product) {
		buffer.writeId(BuiltInRegistries.ITEM, product.item);
		buffer.writeByte(product.count);
		buffer.writeNbt(product.tag);
		buffer.writeFloat(product.chance);
	}

	public static Product fromNetwork(FriendlyByteBuf buffer) {
		Item item = buffer.readById(BuiltInRegistries.ITEM);
		int count = buffer.readByte();
		CompoundTag tag = buffer.readNbt();
		float chance = buffer.readFloat();

		if (item == null) {
			throw new IllegalStateException("Received invalid item ID");
		}

		return new Product(item, count, tag, chance);
	}
}

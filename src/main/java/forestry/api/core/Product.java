package forestry.api.core;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Default implementation of {@link IProduct}. Used in most cases.
 *
 * @param item   The item this product represents.
 * @param count  The count the produced stack should have.
 * @param patch  The data components to apply to the item. Use {@link DataComponentPatch#EMPTY} in place of {@code null}.
 * @param chance The chance (from 0.0 to 1.0) that this product is produced. Support for values higher than 1.0 varies by machine.
 */
public record Product(Item item, int count, DataComponentPatch patch, float chance) implements IProduct {
	public Product {
		Preconditions.checkNotNull(patch);
	}

	public static final MapCodec<Product> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Product::item),
		Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(Product::count),
		DataComponentPatch.CODEC.optionalFieldOf("tag", DataComponentPatch.EMPTY).forGetter(Product::patch),
		Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Product::chance)
	).apply(instance, Product::new));
	public static final Codec<Product> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Product> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.ITEM), Product::item,
		ByteBufCodecs.INT, Product::count,
		DataComponentPatch.STREAM_CODEC, Product::patch,
		ByteBufCodecs.FLOAT, Product::chance,
		Product::new
	);
	/**
	 * The default product type. The dispatch codec in {@code forestry.core.genetics.ProductTypes} treats this type
	 * specially: products of this type serialize without a {@code "type"} key, and a missing {@code "type"} key on
	 * decode resolves back to it. This keeps the common case (a plain item) as clean, backwards-compatible JSON.
	 */
	public static final ProductType<Product> TYPE = new ProductType<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public ProductType<?> type() {
		return TYPE;
	}

	@Override
	public ItemStack createStack() {
		if (this.patch.isEmpty()) {
			return new ItemStack(this.item, this.count);
		} else {
			return new ItemStack(this.item.builtInRegistryHolder(), this.count, this.patch);
		}
	}

	public static Product of(Item item) {
		return new Product(item, 1, DataComponentPatch.EMPTY, 1f);
	}

	public static Product of(Item item, int amount, float chance) {
		return new Product(item, amount, DataComponentPatch.EMPTY, chance);
	}
}

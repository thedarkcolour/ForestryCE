package forestry.core.genetics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import forestry.api.ForestryConstants;
import forestry.api.core.IProduct;
import forestry.api.core.Product;
import forestry.api.core.ProductType;
import forestry.apiculture.genetics.FireworkProduct;

import java.util.List;

/**
 * Registry and dispatch codec for {@link IProduct} types. Mirrors {@code MutationConditionTypes}, with one twist:
 * the {@code "type"} key is optional. When absent, the product decodes as the default {@link Product#TYPE}, and a
 * {@link Product} encodes without writing a {@code "type"} key at all. This keeps the overwhelmingly common case (a
 * plain item stack) as clean, backwards-compatible JSON, while dynamic products (e.g. {@link FireworkProduct}, the
 * secret Patriotic bee's randomized firework) round-trip through their own type by declaring {@code "type"}.
 */
public final class ProductTypes {
	private static final Map<ResourceLocation, ProductType<?>> BY_ID = new ConcurrentHashMap<>();
	private static final Map<ProductType<?>, ResourceLocation> ID_OF = new ConcurrentHashMap<>();

	private static final String TYPE_KEY = "type";

	private static boolean builtinsRegistered = false;

	public static void register(ResourceLocation id, ProductType<?> type) {
		if (BY_ID.putIfAbsent(id, type) != null) {
			throw new IllegalStateException("Duplicate product type: " + id);
		}
		ID_OF.put(type, id);
	}

	private static DataResult<ProductType<?>> byId(ResourceLocation id) {
		ProductType<?> type = BY_ID.get(id);
		if (type == null) {
			return DataResult.error(() -> "Unknown product type: " + id);
		}
		return DataResult.success(type);
	}

	/**
	 * The dispatch codec. Unlike a stock {@link Codec#dispatch}, the {@code "type"} key is optional and defaults to
	 * {@link Product#TYPE}; encoding a {@link Product} omits the key entirely.
	 */
	public static final MapCodec<IProduct> MAP_CODEC = new MapCodec<>() {
		@Override
		public <T> DataResult<IProduct> decode(DynamicOps<T> ops, MapLike<T> input) {
			T typeValue = input.get(TYPE_KEY);
			DataResult<ProductType<?>> type = typeValue == null
				? DataResult.success(Product.TYPE)
				: ResourceLocation.CODEC.parse(ops, typeValue).flatMap(ProductTypes::byId);
			return type.flatMap(t -> t.codec().decode(ops, input).map(product -> (IProduct) product));
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> RecordBuilder<T> encode(IProduct input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			ProductType<?> type = input.type();
			if (type != Product.TYPE) {
				ResourceLocation id = ID_OF.get(type);
				if (id == null) {
					return prefix.withErrorsFrom(DataResult.error(() -> "Unregistered product type: " + type));
				}
				prefix.add(TYPE_KEY, ResourceLocation.CODEC.encodeStart(ops, id));
			}
			return ((MapCodec) type.codec()).encode(input, ops, prefix);
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString(TYPE_KEY));
		}
	};

	public static final Codec<IProduct> CODEC = MAP_CODEC.codec();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final StreamCodec<RegistryFriendlyByteBuf, IProduct> STREAM_CODEC = StreamCodec.of(
		(buf, product) -> {
			ResourceLocation id = ID_OF.get(product.type());
			ResourceLocation.STREAM_CODEC.encode(buf, id);
			((StreamCodec) product.type().streamCodec()).encode(buf, product);
		},
		buf -> {
			ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
			return byId(id).getOrThrow().streamCodec().decode(buf);
		});

	public static final Codec<List<IProduct>> LIST_CODEC = CODEC.listOf();
	public static final StreamCodec<RegistryFriendlyByteBuf, List<IProduct>> LIST_STREAM_CODEC =
		STREAM_CODEC.apply(ByteBufCodecs.list());

	/**
	 * Registers the built-in product types under the {@code forestry} namespace.
	 * <p>
	 * Must be called before any datapack parse or network sync. Idempotent: repeated calls are no-ops.
	 */
	public static synchronized void registerBuiltins() {
		if (builtinsRegistered) {
			return;
		}
		builtinsRegistered = true;

		register(ForestryConstants.forestry("item"), Product.TYPE);
		register(ForestryConstants.forestry("firework"), FireworkProduct.TYPE);
	}

	private ProductTypes() {}
}

package forestry.api.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * The serializer for a kind of {@link IProduct}, used to (de)serialize it via the dispatch codec built in
 * {@code forestry.core.genetics.ProductTypes}. Mirrors {@link forestry.api.genetics.MutationConditionType}.
 * <p>
 * A {@link MapCodec} is required (rather than a plain {@link com.mojang.serialization.Codec}) so the product's
 * fields serialize inline alongside the optional {@code "type"} key instead of nesting under it.
 *
 * @param codec       The map codec for this product type's fields.
 * @param streamCodec The network codec for this product type.
 * @param <T>         The concrete {@link IProduct} implementation this type serializes.
 */
public record ProductType<T extends IProduct>(
	MapCodec<T> codec,
	StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
) {}

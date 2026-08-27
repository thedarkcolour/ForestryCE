package forestry.api.core.machines.fuels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;

/**
 * Data map value for items usable as a resource by the Moistener.
 *
 * @param product        The item that leaves the moistener's working slot (i.e. mouldy wheat, decayed wheat, mulch)
 * @param stage          What stage this product represents. Resources with lower stage value are consumed first
 * @param moistenerValue How much this item contributes to the final product of the moistener (i.e. mycelium)
 */
public record MoistenerFuel(ItemStack product, int stage, int moistenerValue) {
	public static final Codec<MoistenerFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ItemStack.CODEC.fieldOf("product").forGetter(MoistenerFuel::product),
			Codec.INT.fieldOf("stage").forGetter(MoistenerFuel::stage),
			Codec.INT.fieldOf("moistener_value").forGetter(MoistenerFuel::moistenerValue)
	).apply(inst, MoistenerFuel::new));
}

package forestry.api.circuits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * @param id         Unique ID for this circuit layout
 * @param socketType Specifies where a circuit layout is used
 */
public record CircuitLayout(String id, ResourceLocation socketType) {
	public static final Codec<CircuitLayout> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.STRING.fieldOf("id").forGetter(CircuitLayout::id),
		ResourceLocation.CODEC.fieldOf("socket_type").forGetter(CircuitLayout::socketType)
	).apply(inst, CircuitLayout::new));

	/**
	 * @return localized name for this circuit layout
	 */
	public MutableComponent getName() {
		return Component.translatable("circuit.layout." + this.id);
	}

	/**
	 * @return localized string for how this circuit layout is used
	 */
	public MutableComponent getUsage() {
		return Component.translatable("circuit.layout." + this.id + ".usage");
	}
}

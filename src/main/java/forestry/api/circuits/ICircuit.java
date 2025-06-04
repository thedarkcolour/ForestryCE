package forestry.api.circuits;

import com.mojang.serialization.Codec;
import forestry.api.ForestryRegistries;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface ICircuit {
	Codec<ICircuit> CODEC = ForestryRegistries.CIRCUIT.byNameCodec();

	String getId();

	String getTranslationKey();

	default Component getDisplayName() {
		return Component.translatable(getTranslationKey());
	}

	boolean isCircuitable(Object tile);

	void onInsertion(int slot, Object tile);

	void onLoad(int slot, Object tile);

	void onRemoval(int slot, Object tile);

	void onTick(int slot, Object tile);

	void addTooltip(List<Component> list);
}

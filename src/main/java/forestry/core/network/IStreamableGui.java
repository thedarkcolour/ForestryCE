package forestry.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

// IStreamable variant for screen data
public interface IStreamableGui {
	void writeGuiData(RegistryFriendlyByteBuf data);

	void readGuiData(RegistryFriendlyByteBuf data);
}

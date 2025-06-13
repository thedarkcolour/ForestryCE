package forestry.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * IStreamable variant for synchronizing screen data from the server to the client
 */
public interface IStreamableGui {
	void writeGuiData(RegistryFriendlyByteBuf data);

	void readGuiData(RegistryFriendlyByteBuf data);
}

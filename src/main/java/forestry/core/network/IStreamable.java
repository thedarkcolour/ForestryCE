package forestry.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IStreamable {
	/**
	 * Called on the serverside to sync additional information about this block to the client.
	 *
	 * @param buffer The stream of data about this object to send to the client.
	 */
	void writeData(RegistryFriendlyByteBuf buffer);

	/**
	 * Called on the clientside to receive data from the server.
	 *
	 * @param buffer The stream of data about this object sent by the server.
	 */
	void readData(RegistryFriendlyByteBuf buffer);
}

package forestry.core.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface IStreamable {
	/**
	 * Called on the serverside to sync additional information about this block to the client.
	 *
	 * @param data The stream of data about this object to send to the client.
	 */
	void writeData(RegistryFriendlyByteBuf data);

	/**
	 * Called on the clientside to receive data from the server.
	 *
	 * @param data The stream of data about this object sent by the server.
	 */
	void readData(RegistryFriendlyByteBuf data);
}

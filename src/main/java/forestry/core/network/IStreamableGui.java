package forestry.core.network;

import net.minecraft.network.FriendlyByteBuf;

// IStreamable variant for screen data
public interface IStreamableGui {
	void writeGuiData(FriendlyByteBuf data);

	void readGuiData(FriendlyByteBuf data);
}

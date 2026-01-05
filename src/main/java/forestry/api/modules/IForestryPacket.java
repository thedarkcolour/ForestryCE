package forestry.api.modules;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IForestryPacket {
	// Useless until 1.21
	ResourceLocation id();

	void write(FriendlyByteBuf buffer);
}

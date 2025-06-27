package forestry.core.gui;

import net.minecraft.server.level.ServerPlayer;

public interface IGuiSelectable {
	// server
	void handleSelectionRequest(ServerPlayer player, int primary, int secondary);
}

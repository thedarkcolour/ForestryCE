package forestry.core.gui;

import net.minecraft.world.entity.player.Player;

public interface IGuiSelectable {
	// server
	void handleSelectionRequest(Player player, int primary, int secondary);
}

package forestry.core.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

public interface IPagedInventory extends Container {
	void flipPage(ServerPlayer player, short page);
}

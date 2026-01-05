package forestry.storage.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiBackpack extends GuiForestry<ContainerBackpack> {
	public GuiBackpack(ContainerBackpack container, Inventory inv, Component title) {
		super(getTextureString(container), container, inv, title);
		ContainerBackpack.Size size = container.getSize();

		if (size == ContainerBackpack.Size.T2) {
            this.imageWidth = 176;
            this.imageHeight = 192;
		}
	}

	private static String getTextureString(ContainerBackpack container) {
		ContainerBackpack.Size size = container.getSize();
		if (size == ContainerBackpack.Size.T2) {
			return Constants.TEXTURE_PATH_GUI + "/backpack_t2.png";
		}
		return Constants.TEXTURE_PATH_GUI + "/backpack.png";
	}

	@Override
	protected void addLedgers() {

	}
}

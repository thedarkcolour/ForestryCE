package forestry.apiculture.gui;

import forestry.apiculture.multiblock.TileAlvearySwarmer;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiAlvearySwarmer extends GuiForestryTitled<ContainerAlvearySwarmer> {
	private final TileAlvearySwarmer tile;

	public GuiAlvearySwarmer(ContainerAlvearySwarmer container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/swarmer.png", container, inventory, title);
		this.tile = container.getTile();
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}

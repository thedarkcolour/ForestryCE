package forestry.apiculture.gui;

import forestry.apiculture.multiblock.TileAlvearySieve;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiAlvearySieve extends GuiForestryTitled<ContainerAlvearySieve> {
	private final TileAlvearySieve tile;

	public GuiAlvearySieve(ContainerAlvearySieve container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/sieve.png", container, inventory, title);
		this.tile = container.getTile();
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}

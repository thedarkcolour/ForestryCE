package forestry.apiculture.gui;

import forestry.apiculture.multiblock.TileAlvearyHygroregulator;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiAlvearyHygroregulator extends GuiForestryTitled<ContainerAlvearyHygroregulator> {
	private final TileAlvearyHygroregulator tile;

	public GuiAlvearyHygroregulator(ContainerAlvearyHygroregulator container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/hygroregulator.png", container, inventory, title);
		this.tile = container.getTile();

        this.widgetManager.add(new TankWidget(this.widgetManager, 104, 17, 0));
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}

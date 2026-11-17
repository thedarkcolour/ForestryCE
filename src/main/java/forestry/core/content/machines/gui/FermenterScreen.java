package forestry.core.content.machines.gui;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.TankWidget;
import forestry.core.content.machines.tiles.FermenterBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FermenterScreen extends TitledForestryScreen<FermenterMenu> {
	private final FermenterBlockEntity tile;

	public FermenterScreen(FermenterMenu container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/fermenter.png", container, inventory, title);
		this.tile = container.getTile();
        this.widgetManager.add(new TankWidget(this.widgetManager, 35, 19, 0));
        this.widgetManager.add(new TankWidget(this.widgetManager, 125, 19, 1));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mX, int mY) {
		super.renderBg(graphics, partialTicks, mX, mY);

		// Fuel remaining
		int fuelRemain = this.tile.getBurnTimeRemainingScaled(16);
		if (fuelRemain > 0) {
			graphics.blit(this.textureFile, this.leftPos + 98, this.topPos + 46 + 17 - fuelRemain, 176, 78 + 17 - fuelRemain, 4, fuelRemain);
		}

		// Raw bio mush remaining
		int bioRemain = this.tile.getFermentationProgressScaled(16);
		if (bioRemain > 0) {
			graphics.blit(this.textureFile, this.leftPos + 74, this.topPos + 32 + 17 - bioRemain, 176, 60 + 17 - bioRemain, 4, bioRemain);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("fermenter");
		addPowerLedger(this.tile.getEnergyManager());
	}
}

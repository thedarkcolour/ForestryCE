package forestry.agriculture.multifarm.gui;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.SocketWidget;
import forestry.core.platform.gui.widgets.TankWidget;
import forestry.agriculture.multifarm.multiblock.IFarmControllerInternal;
import forestry.agriculture.multifarm.tiles.MultifarmBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MultifarmScreen extends TitledForestryScreen<MultifarmMenu> {
	private final MultifarmBlockEntity tile;

	public MultifarmScreen(MultifarmMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/mfarm.png", container, inv, title);
		this.tile = container.getTile();

        this.widgetManager.add(new TankWidget(this.widgetManager, 15, 19, 0).setOverlayOrigin(216, 18));

        this.widgetManager.add(new SocketWidget(this.widgetManager, 69, 40, this.tile, 0));

		IFarmControllerInternal farmController = this.tile.getMultiblockLogic().getController();

        this.widgetManager.add(new MultifarmLogicSlot(farmController, this.widgetManager, 69, 22, Direction.NORTH));
        this.widgetManager.add(new MultifarmLogicSlot(farmController, this.widgetManager, 69, 58, Direction.SOUTH));
        this.widgetManager.add(new MultifarmLogicSlot(farmController, this.widgetManager, 51, 40, Direction.WEST));
        this.widgetManager.add(new MultifarmLogicSlot(farmController, this.widgetManager, 87, 40, Direction.EAST));

		this.imageWidth = 216;
		this.imageHeight = 220;
	}

	@Override
	protected void addLedgers() {
		IFarmControllerInternal farmController = this.tile.getMultiblockLogic().getController();

		addErrorLedger(farmController);
		addClimateLedger(farmController);
        this.ledgerManager.add(new FarmLedger(this.ledgerManager, farmController.getFarmLedgerDelegate()));
		addHintLedger("farm");
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		// Fuel remaining
		int fertilizerRemain = this.tile.getMultiblockLogic().getController().getStoredFertilizerScaled(16);
		if (fertilizerRemain > 0) {
			graphics.blit(this.textureFile, this.leftPos + 81, this.topPos + 94 + 17 - fertilizerRemain, this.imageWidth, 17 - fertilizerRemain, 4, fertilizerRemain);
		}
	}
}

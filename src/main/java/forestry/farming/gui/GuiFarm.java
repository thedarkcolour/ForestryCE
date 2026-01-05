package forestry.farming.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.SocketWidget;
import forestry.core.gui.widgets.TankWidget;
import forestry.farming.multiblock.IFarmControllerInternal;
import forestry.farming.tiles.TileFarm;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiFarm extends GuiForestryTitled<ContainerFarm> {
	private final TileFarm tile;

	public GuiFarm(ContainerFarm container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/mfarm.png", container, inv, title);
		this.tile = container.getTile();

        this.widgetManager.add(new TankWidget(this.widgetManager, 15, 19, 0).setOverlayOrigin(216, 18));

        this.widgetManager.add(new SocketWidget(this.widgetManager, 69, 40, this.tile, 0));

		IFarmControllerInternal farmController = this.tile.getMultiblockLogic().getController();

        this.widgetManager.add(new FarmLogicSlot(farmController, this.widgetManager, 69, 22, Direction.NORTH));
        this.widgetManager.add(new FarmLogicSlot(farmController, this.widgetManager, 69, 58, Direction.SOUTH));
        this.widgetManager.add(new FarmLogicSlot(farmController, this.widgetManager, 51, 40, Direction.WEST));
        this.widgetManager.add(new FarmLogicSlot(farmController, this.widgetManager, 87, 40, Direction.EAST));

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
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		// Fuel remaining
		int fertilizerRemain = this.tile.getMultiblockLogic().getController().getStoredFertilizerScaled(16);
		if (fertilizerRemain > 0) {
			// todo not sure if the textureFile is the correct sprite to draw
			graphics.blit(this.textureFile, this.leftPos + 81, this.topPos + 94 + 17 - fertilizerRemain, this.imageWidth, 17 - fertilizerRemain, 4, fertilizerRemain);
		}
	}
}

package forestry.mail.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import forestry.core.render.ColourProperties;
import forestry.mail.tiles.TileTrader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiTrader extends GuiForestry<ContainerTrader> {
	private final TileTrader tile;

	public GuiTrader(ContainerTrader container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/mailtrader2.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 226;
		this.imageHeight = 220;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		Component name = this.tile.getTitle();
		graphics.drawString(this.font, name, this.textLayout.getCenteredOffset(name), 6, ColourProperties.INSTANCE.get("gui.mail.text"));

		Component receive = Component.translatable("for.gui.mail.receive");
		graphics.drawString(this.font, receive, this.textLayout.getCenteredOffset(receive, 70) + 51, 45, ColourProperties.INSTANCE.get("gui.mail.text"));

		Component send = Component.translatable("for.gui.mail.send");
		graphics.drawString(this.font, send, this.textLayout.getCenteredOffset(send, 70) + 51, 99, ColourProperties.INSTANCE.get("gui.mail.text"));

		super.renderLabels(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		graphics.drawString(this.font, this.menu.getAddress().getName(), this.leftPos + 19, this.topPos + 22, ColourProperties.INSTANCE.get("gui.mail.text"));
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addHintLedger("trade.station");
		addOwnerLedger(this.tile);
	}
}

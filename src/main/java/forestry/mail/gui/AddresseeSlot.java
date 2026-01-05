package forestry.mail.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import forestry.api.client.ForestrySprites;
import forestry.api.core.tooltips.ToolTip;
import forestry.api.mail.IPostalCarrier;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.SoundUtil;
import net.minecraft.client.gui.GuiGraphics;

public class AddresseeSlot extends Widget {

	private final ContainerLetter containerLetter;

	public AddresseeSlot(WidgetManager widgetManager, int xPos, int yPos, ContainerLetter containerLetter) {
		super(widgetManager, xPos, yPos);
		this.containerLetter = containerLetter;
		this.width = 26;
		this.height = 15;
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		IPostalCarrier carrier = this.containerLetter.getCarrier();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0F);
		RenderSystem.setShaderTexture(0, ForestrySprites.TEXTURE_ATLAS);
		graphics.blit(startX + this.xPos, startY + this.yPos, 0, 32, 32, carrier.getSprite());
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		ToolTip tooltip = new ToolTip();
		tooltip.translated(this.containerLetter.getCarrier().getDescriptionId());
		return tooltip;
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
		if (!this.containerLetter.getLetter().isProcessed()) {
            this.containerLetter.advanceCarrierType();
			SoundUtil.playButtonClick();
		}
	}
}

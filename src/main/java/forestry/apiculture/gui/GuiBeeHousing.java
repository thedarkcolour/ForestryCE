package forestry.apiculture.gui;

import forestry.core.config.Constants;
import forestry.core.gui.ContainerForestry;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.render.EnumTankLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiBeeHousing<C extends ContainerForestry & IContainerBeeHousing> extends GuiForestryTitled<C> {
	private final IGuiBeeHousingDelegate delegate;

	public enum Icon {
		APIARY("/apiary.png"),
		BEE_HOUSE("/alveary.png");

		public static final Icon[] VALUES = values();
		private final String path;

		Icon(String path) {
			this.path = path;
		}
	}

	public GuiBeeHousing(C container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + container.getIcon().path, container, inv, title);
		this.delegate = container.getDelegate();
		this.imageHeight = 190;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		drawHealthMeter(graphics, this.leftPos + 20, this.topPos + 37, this.delegate.getHealthScaled(46), EnumTankLevel.rateTankLevel(this.delegate.getHealthScaled(100)));
	}

	private void drawHealthMeter(GuiGraphics graphics, int x, int y, int height, EnumTankLevel rated) {
		int i = 176 + rated.getLevelScaled(16);
		int k = 0;

		graphics.blit(this.textureFile, x, y + 46 - height, i, k + 46 - height, 4, height);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.delegate);
		addClimateLedger(this.delegate);
		addHintLedger(this.delegate.getHintKey());
		addOwnerLedger(this.delegate);
	}
}

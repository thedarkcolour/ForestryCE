package forestry.core.gui;

import forestry.core.render.ColourProperties;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class GuiForestryTitled<C extends AbstractContainerMenu> extends GuiForestry<C> {
	protected GuiForestryTitled(String texture, C container, Inventory inv, Component title) {
		super(texture, container, inv, title);
	}

	// For Forestry addons
	protected GuiForestryTitled(ResourceLocation texture, C container, Inventory inv, Component title) {
		super(texture, container, inv, title);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

        this.textLayout.line = 6;
		if (centeredTitle()) {
            this.textLayout.drawCenteredLine(graphics, this.title, 0, ColourProperties.INSTANCE.get("gui.title"));
		} else {
            this.textLayout.drawLine(graphics, this.title.getString(), 8, ColourProperties.INSTANCE.get("gui.title"));
		}
		// todo get rid of this and make sure nothing breaks
		bindTexture(this.textureFile);
	}

	protected boolean centeredTitle() {
		return true;
	}
}

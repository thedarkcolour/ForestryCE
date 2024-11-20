package forestry.energy.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import forestry.core.config.Constants;
import forestry.core.gui.widgets.TankWidget;
import forestry.energy.menu.BiogasEngineMenu;
import forestry.energy.tiles.BiogasEngineBlockEntity;

public class BiogasEngineScreen extends EngineScreen<BiogasEngineMenu, BiogasEngineBlockEntity> {
	public BiogasEngineScreen(BiogasEngineMenu menu, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/bioengine.png", menu, inv, title, menu.getTile());

		widgetManager.add(new TankWidget(widgetManager, 89, 19, 0));
		widgetManager.add(new TankWidget(widgetManager, 107, 19, 1));

		widgetManager.add(new BiogasSlot(widgetManager, 30, 47, 2));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		int temperature = engine.getOperatingTemperatureScaled(16);
		if (temperature > 16) {
			temperature = 16;
		}
		if (temperature > 0) {
			graphics.blit(this.textureFile, leftPos + 53, topPos + 47 + 16 - temperature, 176, 60 + 16 - temperature, 4, temperature);
		}
	}
}

package forestry.core.content.machines.gui;

import forestry.core.platform.config.Constants;
import forestry.core.platform.gui.TitledForestryScreen;
import forestry.core.platform.gui.widgets.ReservoirWidget;
import forestry.core.content.machines.tiles.FabricatorBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

public class FabricatorScreen extends TitledForestryScreen<FabricatorMenu> {
	private final FabricatorBlockEntity tile;

	public FabricatorScreen(FabricatorMenu container, Inventory player, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/thermionic_fabricator.png", container, player, title);

		this.tile = container.getTile();
		this.imageHeight = 211;
		this.widgetManager.add(new ReservoirWidget(this.widgetManager, 26, 48, 0));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		int heatScaled = this.tile.getHeatScaled(52);
		if (heatScaled > 0) {
			graphics.blit(this.textureFile, this.leftPos + 55, this.topPos + 17 + 52 - heatScaled, 192, 52 - heatScaled, 4, heatScaled);
		}

		int meltingPointScaled = this.tile.getMeltingPointScaled(52);
		if (meltingPointScaled > 0) {
			graphics.blit(this.textureFile, this.leftPos + 52, this.topPos + 15 + 52 - meltingPointScaled, 196, 0, 10, 5);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		boolean mouseOverHeatingBar = (
			mouseX >= this.leftPos + 55
				&& mouseX <= this.leftPos + 59
				&& mouseY >= this.topPos + 16
				&& mouseY <= this.topPos + 70
		);

		if (mouseOverHeatingBar) {
			List<Component> messages;
			if (this.tile.getMeltingPoint() > 0) {
				messages = List.of(
					Component.translatable("for.gui.fabricator.heat", this.tile.getHeat(), FabricatorBlockEntity.MAX_HEAT),
					Component.translatable("for.gui.fabricator.requiredHeat", this.tile.getMeltingPoint()));
			} else {
				messages = List.of(Component.translatable("for.gui.fabricator.heat", this.tile.getHeat(), FabricatorBlockEntity.MAX_HEAT));
			}

			guiGraphics.renderTooltip(this.font, messages, Optional.empty(), mouseX, mouseY);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addPowerLedger(this.tile.getEnergyManager());
		addHintLedger("fabricator");
	}
}

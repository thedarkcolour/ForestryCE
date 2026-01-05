package forestry.core.gui.ledgers;

import forestry.api.client.ForestrySprites;
import forestry.api.client.IForestryClientApi;
import forestry.core.gui.GuiUtil;
import forestry.energy.ForestryEnergyStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class PowerLedger extends Ledger {
	private final ForestryEnergyStorage energyStorage;

	public PowerLedger(LedgerManager manager, ForestryEnergyStorage energyStorage) {
		super(manager, "power");
		this.energyStorage = energyStorage;
        this.maxHeight = 94;
	}

	@Override
	public void draw(GuiGraphics graphics, int y, int x) {
		// Draw background
		drawBackground(graphics, y, x);

		// Draw icon
		drawSprite(graphics, IForestryClientApi.INSTANCE.getTextureManager().getSprite(ForestrySprites.MISC_ENERGY), x + 3, y + 4);

		if (!isFullyOpened()) {
			return;
		}

		int xHeader = x + 22;
		int xBody = x + 12;

		drawHeader(graphics, Component.translatable("for.gui.energy"), xHeader, y + 8);

		drawSubheader(graphics, Component.translatable("for.gui.stored").append(":"), xBody, y + 20);
		drawText(graphics, GuiUtil.formatEnergyValue(this.energyStorage.getEnergyStored()), xBody, y + 32);

		drawSubheader(graphics, Component.translatable("for.gui.maxenergy").append(":"), xBody, y + 44);
		drawText(graphics, GuiUtil.formatEnergyValue(this.energyStorage.getMaxEnergyStored()), xBody, y + 56);

		drawSubheader(graphics, Component.translatable("for.gui.maxenergyreceive").append(":"), xBody, y + 68);
		drawText(graphics, GuiUtil.formatEnergyValue(this.energyStorage.getMaxEnergyReceived()), xBody, y + 80);
	}

	@Override
	public Component getTooltip() {
		return Component.literal(GuiUtil.formatEnergyValue(this.energyStorage.getEnergyStored()));
	}

}

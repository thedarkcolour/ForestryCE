package forestry.cultivation.gui;

import forestry.core.config.Constants;
import forestry.core.features.CoreItems;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;
import forestry.cultivation.gui.widgets.GhostItemStackWidget;
import forestry.cultivation.inventory.LegacyFarmInventory;
import forestry.cultivation.tiles.TilePlanter;
import forestry.farming.gui.FarmLedger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GuiPlanter extends GuiForestryTitled<PlanterMenu> {
	private final TilePlanter tile;

	public GuiPlanter(PlanterMenu container, Inventory playerInventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/planter.png", container, playerInventory, title);
		this.tile = container.getTile();
		this.imageWidth = 202;
		this.imageHeight = 192;

		List<ItemStack> resourceStacks = this.tile.createResourceStacks();
		List<ItemStack> germlingStacks = this.tile.createGermlingStacks();
		List<ItemStack> productionStacks = this.tile.createProductionStacks();

        this.widgetManager.add(new TankWidget(this.widgetManager, 178, 44, 0).setOverlayOrigin(this.imageWidth, 18));

		// Resources
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				int index = j + i * 2;
				if (resourceStacks.size() == 4) {
                    this.widgetManager.add(new GhostItemStackWidget(this.widgetManager, 11 + j * 18, 65 + i * 18, resourceStacks.get(index), this.getMenu().getSlot(36 + LegacyFarmInventory.CONFIG.resourcesStart + index)));
				}
			}
		}

		// Germlings
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				int index = j + i * 2;
				if (germlingStacks.size() == 4) {
                    this.widgetManager.add(new GhostItemStackWidget(this.widgetManager, 71 + j * 18, 65 + i * 18, germlingStacks.get(index), this.getMenu().getSlot(36 + LegacyFarmInventory.CONFIG.germlingsStart + index)));
				}
			}
		}

		// Production
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				int index = j + i * 2;
				if (productionStacks.size() == 4) {
                    this.widgetManager.add(new GhostItemStackWidget(this.widgetManager, 131 + j * 18, 65 + i * 18, productionStacks.get(index), getMenu().getSlot(36 + LegacyFarmInventory.CONFIG.productionStart + j + i * 2)));
				}
			}
		}

        this.widgetManager.add(new GhostItemStackWidget(this.widgetManager, 83, 22, CoreItems.FERTILIZER_COMPOUND.stack(), getMenu().getSlot(36 + LegacyFarmInventory.CONFIG.fertilizerStart)));
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
		addClimateLedger(this.tile);
        this.ledgerManager.add(new FarmLedger(this.ledgerManager, this.tile.getFarmLedgerDelegate()));
		addOwnerLedger(this.tile);
		addPowerLedger(this.tile.getEnergyManager());
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		// Fuel remaining
		int fertilizerRemain = this.tile.getStoredFertilizerScaled(16);
		if (fertilizerRemain > 0) {
			graphics.blit(this.textureFile, this.leftPos + 101, this.topPos + 21 + 17 - fertilizerRemain, this.imageWidth, 17 - fertilizerRemain, 4, fertilizerRemain);
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import forestry.apiculture.multiblock.IAlvearyControllerInternal;
import forestry.apiculture.multiblock.TileAlveary;
import forestry.core.config.Constants;
import forestry.core.gui.GuiAnalyzerProvider;
import forestry.core.render.EnumTankLevel;

public class GuiAlveary extends GuiAnalyzerProvider<ContainerAlveary> {
	private final TileAlveary tile;

	public GuiAlveary(ContainerAlveary container, Inventory inventory, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/alveary.png", container, inventory, () -> title, 25, 7, 2, 0);
		this.tile = container.getTile();
		this.imageHeight = 190;
		analyzer.init();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

		IAlvearyControllerInternal alvearyController = tile.getMultiblockLogic().getController();
		drawHealthMeter(graphics, leftPos + 20, topPos + 37, alvearyController.getHealthScaled(46), EnumTankLevel.rateTankLevel(alvearyController.getHealthScaled(100)));
	}

	private void drawHealthMeter(GuiGraphics graphics, int x, int y, int height, EnumTankLevel rated) {
		int i = 176 + rated.getLevelScaled(16);
		int k = 0;

		graphics.blit(this.textureFile, x, y + 46 - height, i, k + 46 - height, 4, height);
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
		addClimateLedger(tile);
		addHintLedger("apiary");
		addOwnerLedger(tile);
	}

	@Override
	protected void drawSelectedSlot(GuiGraphics graphics, int selectedSlot) {
		Slot slot = menu.getForestrySlot(1 + selectedSlot);
		SELECTED_COMB_SLOT.draw(graphics, topPos + slot.y - 3, leftPos + slot.x - 3);
	}

	@Override
	public ItemStack getSpecimen(int index) {
		Slot slot = menu.getForestrySlot(getSelectedSlot(index));
		return slot.getItem();
	}

	@Override
	protected boolean hasErrors() {
		return tile.getErrorLogic().hasErrors();
	}
}

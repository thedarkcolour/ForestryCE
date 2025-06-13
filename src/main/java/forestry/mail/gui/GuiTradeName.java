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
package forestry.mail.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import forestry.core.render.ColourProperties;
import forestry.mail.network.packets.PacketTraderAddressRequest;
import forestry.mail.tiles.TileTrader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class GuiTradeName extends GuiForestry<TradeNameMenu> {
	private final TileTrader tile;
	private EditBox addressNameField;

	public GuiTradeName(TradeNameMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/tradername.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 176;
		this.imageHeight = 90;
	}

	@Override
	public void init() {
		super.init();

        this.addressNameField = new EditBox(this.font, this.leftPos + 44, this.topPos + 39, 90, 14, null);
        this.addressNameField.setCanLoseFocus(true);
        this.addressNameField.setTextColor(-1);
        this.addressNameField.setTextColorUneditable(-1);
        this.addressNameField.setBordered(true);
        this.addressNameField.setMaxLength(12);
        this.addressNameField.setValue(this.menu.getAddress().getName());
		addWidget(this.addressNameField);
		setInitialFocus(this.addressNameField);
        this.addressNameField.setEditable(true);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.player.closeContainer();
		}

		if (key == GLFW.GLFW_KEY_ENTER && this.addressNameField.isFocused()) {
			setAddress();
			return true;
		}

		return this.addressNameField.keyPressed(key, scanCode, modifiers)
			|| this.addressNameField.canConsumeInput()
			|| super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTicks, mouseX, mouseY);

        this.textLayout.startPage(graphics);
        this.textLayout.newLine();
        this.textLayout.drawCenteredLine(graphics, Component.translatable("for.gui.mail.nametrader"), 0, ColourProperties.INSTANCE.get("gui.mail.text"));
        this.textLayout.newLine(38);
        this.textLayout.drawCenteredLine(graphics, Component.translatable("for.gui.mail.nametrader.finish"), 0, ColourProperties.INSTANCE.get("gui.mail.text"));
        this.textLayout.endPage(graphics);
        this.addressNameField.render(graphics, mouseY, mouseX, partialTicks);
	}

	private void setAddress() {
		String address = this.addressNameField.getValue();
		if (StringUtils.isNotBlank(address)) {
			PacketTraderAddressRequest packet = new PacketTraderAddressRequest(this.tile, address);
            PacketDistributor.sendToServer(packet);
        }
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(this.tile);
	}
}

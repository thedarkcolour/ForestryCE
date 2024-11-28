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

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.lwjgl.glfw.GLFW;

import forestry.api.mail.PostManager;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import forestry.core.render.ColourProperties;
import forestry.core.utils.NetworkUtil;
import forestry.mail.network.packets.PacketTraderAddressRequest;
import forestry.mail.tiles.TradeStationBlockEntity;

// Sets the name of a trade station when it's first placed down
public class TradeStationNamingScreen extends GuiForestry<TradeStationNamingMenu> {
	private final TradeStationBlockEntity tile;
	private EditBox addressNameField;

	public TradeStationNamingScreen(TradeStationNamingMenu container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/tradername.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 176;
		this.imageHeight = 90;

		this.addressNameField = new EditBox(this.font, leftPos + 44, topPos + 39, 90, 14, null);
		this.addressNameField.setFilter(name -> PostManager.postRegistry.isValidTradeAddress(PostManager.postRegistry.createMailAddress(name)));

	}

	@Override
	public void init() {
		super.init();

		this.addressNameField = new EditBox(this.font, leftPos + 44, topPos + 39, 90, 14, null);
		this.addressNameField.setValue(menu.getAddress().getName());
		addWidget(this.addressNameField);

		setFocused(this.addressNameField);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		// Set focus or enter text into address
		if (this.addressNameField.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER) {
				setAddress();
				return true;
			} else if (this.minecraft.options.keyInventory.matches(key, scanCode)) {
				return true;
			}
		}

		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (addressNameField.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int var3, int var2) {
		super.renderBg(graphics, partialTicks, var3, var2);

		Component prompt = Component.translatable("for.gui.mail.nametrader");
		textLayout.startPage(graphics);
		textLayout.newLine();
		textLayout.drawCenteredLine(graphics, prompt, 0, ColourProperties.INSTANCE.get("gui.mail.text"));
		textLayout.endPage(graphics);
		addressNameField.render(graphics, var2, var3, partialTicks);    //TODO correct?
	}

	@Override
	public void removed() {
		super.removed();
		setAddress();
	}

	private void setAddress() {
		String address = addressNameField.getValue();
		if (StringUtils.isNotBlank(address)) {
			PacketTraderAddressRequest packet = new PacketTraderAddressRequest(tile, address);
			NetworkUtil.sendToServer(packet);
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
	}
}

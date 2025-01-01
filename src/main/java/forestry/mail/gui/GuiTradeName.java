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

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.glfw.GLFW;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import forestry.core.render.ColourProperties;
import forestry.core.utils.NetworkUtil;
import forestry.mail.network.packets.PacketTraderAddressRequest;
import forestry.mail.tiles.TileTrader;

public class GuiTradeName extends GuiForestry<ContainerTradeName> {
	private final TileTrader tile;
	private EditBox addressNameField;

	public GuiTradeName(ContainerTradeName container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/tradername.png", container, inv, title);
		this.tile = container.getTile();
		this.imageWidth = 176;
		this.imageHeight = 90;
	}

	@Override
	public void init() {
		super.init();

		addressNameField = new EditBox(this.font, leftPos + 44, topPos + 39, 90, 14, null);
		addressNameField.setCanLoseFocus(true);
		addressNameField.setTextColor(-1);
		addressNameField.setTextColorUneditable(-1);
		addressNameField.setBordered(true);
		addressNameField.setMaxLength(12);
		addressNameField.setValue(menu.getAddress().getName());
		addWidget(this.addressNameField);
		setInitialFocus(this.addressNameField);
		addressNameField.setEditable(true);
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
	protected void renderBg(PoseStack transform, float partialTicks, int var3, int var2) {
		super.renderBg(transform, partialTicks, var3, var2);

		Component prompt = Component.translatable("for.gui.mail.nametrader");
		textLayout.startPage(transform);
		textLayout.newLine();
		textLayout.drawCenteredLine(transform, prompt, 0, ColourProperties.INSTANCE.get("gui.mail.text"));
		textLayout.endPage(transform);
		addressNameField.render(transform, var2, var3, partialTicks);    //TODO correct?
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

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

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.lwjgl.glfw.GLFW;

import forestry.api.mail.EnumAddressee;
import forestry.api.mail.IMailAddress;
import forestry.core.config.Constants;
import forestry.core.config.SessionVars;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.GuiTextBox;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.Widget;
import forestry.core.render.ColourProperties;
import forestry.core.utils.NetworkUtil;
import forestry.mail.inventory.ItemInventoryLetter;
import forestry.mail.network.packets.PacketLetterInfoRequest;

public class GuiLetter extends GuiForestry<ContainerLetter> {
	private final ItemInventoryLetter itemInventory;
	private final boolean isProcessedLetter;
	private boolean checkedSessionVars;

	private EditBox address;
	private GuiTextBox text;

	private boolean addressFocus;
	private boolean textFocus;

	private final ArrayList<Widget> tradeInfoWidgets;

	public GuiLetter(ContainerLetter container, Inventory inv, Component title) {
		super(Constants.TEXTURE_PATH_GUI + "/letter.png", container, inv, title);
		this.minecraft = Minecraft.getInstance(); //not 100% why this is needed, maybe side issues

		this.itemInventory = container.getItemInventory();
		this.imageWidth = 194;
		this.imageHeight = 227;

		this.isProcessedLetter = container.getLetter().isProcessed();
		this.widgetManager.add(new AddresseeSlot(widgetManager, 16, 12, container));
		this.tradeInfoWidgets = new ArrayList<>();
	}

	@Override
	public void init() {
		super.init();

		address = new EditBox(this.minecraft.font, leftPos + 46, topPos + 13, 93, 13, null);
		IMailAddress recipient = menu.getRecipient();
		if (recipient != null) {
			address.setValue(recipient.getName());
		}

		text = new GuiTextBox(this.minecraft.font, leftPos + 17, topPos + 31, 122, 57);
		text.setMaxLength(128);
		if (!menu.getText().isEmpty()) {
			text.setValue(menu.getText());
		}

		addWidget(address);
		addWidget(text);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_TAB) {
			// Set focus or enter text into address
			if (this.address.isFocused()) {
				if (key == GLFW.GLFW_KEY_ENTER) {
					this.address.setFocused(false);
				}/* else {
					this.address.keyPressed(key, scanCode, modifiers);
				}
				return true;*/
			}

			if (this.text.isFocused()) {
				if (key == GLFW.GLFW_KEY_ENTER) {
					if (hasShiftDown()) {
						this.text.setValue(text.getValue() + "\n");
					} else {
						this.text.setFocused(false);
					}
				} else if (key == GLFW.GLFW_KEY_DOWN) {
					text.advanceLine();
				} else if (key == GLFW.GLFW_KEY_UP) {
					text.regressLine();
				}/* else if (text.moreLinesAllowed() || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSLASH) {
					this.text.keyPressed(key, scanCode, modifiers);
				}
				return true;*/
			}
		}

		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}
		this.address.mouseClicked(mouseX, mouseY, mouseButton);
		this.text.mouseClicked(mouseX, mouseY, mouseButton);
		return true;
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		if (!isProcessedLetter && !checkedSessionVars) {
			checkedSessionVars = true;
			setFromSessionVars();
			String recipient = this.address.getValue();
			EnumAddressee recipientType = menu.getCarrierType();
			setRecipient(recipient, recipientType);
		}

		// Check for focus changes
		if (addressFocus != address.isFocused()) {
			String recipient = this.address.getValue();
			if (StringUtils.isNotBlank(recipient)) {
				EnumAddressee recipientType = menu.getCarrierType();
				setRecipient(recipient, recipientType);
			}
		}
		addressFocus = address.isFocused();
		if (textFocus != text.isFocused()) {
			setText();
		}
		textFocus = text.isFocused();

		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		if (this.isProcessedLetter) {
			graphics.drawString(this.font, address.getValue(), leftPos + 49, topPos + 16, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			graphics.drawWordWrap(this.font, Component.literal(text.getValue()), leftPos + 20, topPos + 34, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
		} else {
			clearTradeInfoWidgets();
			address.render(graphics, mouseX, mouseY, partialTicks);
			if (menu.getCarrierType() == EnumAddressee.TRADER) {
				drawTradePreview(graphics, 18, 32);
			} else {
				text.render(graphics, mouseX, mouseY, partialTicks);
			}
		}
	}

	private void drawTradePreview(GuiGraphics graphics, int x, int y) {
		Component infoString = null;
		if (menu.getTradeInfo() == null) {
			infoString = Component.translatable("for.gui.mail.no.trader");
		} else if (menu.getTradeInfo().tradegood().isEmpty()) {
			infoString = Component.translatable("for.gui.mail.nothing.to.trade");
		} else if (!menu.getTradeInfo().state().isOk()) {
			infoString = menu.getTradeInfo().state().getDescription();
		}

		if (infoString != null) {
			graphics.drawWordWrap(this.font, infoString, leftPos + x, topPos + y, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			return;
		}

		graphics.drawString(this.font, Component.translatable("for.gui.mail.pleasesend"), leftPos + x, topPos + y, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

		addTradeInfoWidget(new ItemStackWidget(widgetManager, x, y + 10, menu.getTradeInfo().tradegood()));

		graphics.drawString(this.font, Component.translatable("for.gui.mail.foreveryattached"), leftPos + x, topPos + y + 28, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

		for (int i = 0; i < menu.getTradeInfo().required().size(); i++) {
			addTradeInfoWidget(new ItemStackWidget(widgetManager, x + i * 18, y + 38, menu.getTradeInfo().required().get(i)));
		}
	}

	private void addTradeInfoWidget(Widget widget) {
		tradeInfoWidgets.add(widget);
		widgetManager.add(widget);
	}

	private void clearTradeInfoWidgets() {
		for (Widget widget : tradeInfoWidgets) {
			widgetManager.remove(widget);
		}
		tradeInfoWidgets.clear();
	}

	@Override
	public void onClose() {
		String recipientName = this.address.getValue();
		EnumAddressee recipientType = menu.getCarrierType();
		setRecipient(recipientName, recipientType);
		setText();
		super.onClose();
	}

	private void setFromSessionVars() {
		if (SessionVars.getStringVar("mail.letter.recipient") == null) {
			return;
		}

		String recipient = SessionVars.getStringVar("mail.letter.recipient");
		String typeName = SessionVars.getStringVar("mail.letter.addressee");

		if (StringUtils.isNotBlank(recipient) && StringUtils.isNotBlank(typeName)) {
			address.setValue(recipient);

			EnumAddressee type = EnumAddressee.fromString(typeName);
			menu.setCarrierType(type);
		}

		SessionVars.clearStringVar("mail.letter.recipient");
		SessionVars.clearStringVar("mail.letter.addressee");
	}

	private void setRecipient(String recipientName, EnumAddressee type) {
		if (this.isProcessedLetter || StringUtils.isBlank(recipientName)) {
			return;
		}

		PacketLetterInfoRequest packet = new PacketLetterInfoRequest(recipientName, type);
		NetworkUtil.sendToServer(packet);
	}

	@OnlyIn(Dist.CLIENT)
	private void setText() {
		if (this.isProcessedLetter) {
			return;
		}

		menu.setText(this.text.getValue());
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(itemInventory);
		addHintLedger("letter");
	}
}

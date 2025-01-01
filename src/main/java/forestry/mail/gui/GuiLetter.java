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

import forestry.api.mail.IPostalCarrier;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.lwjgl.glfw.GLFW;

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

		minecraft.keyboardHandler.setSendRepeatsToGui(true);

		address = new EditBox(this.minecraft.font, leftPos + 46, topPos + 13, 93, 13, null);
		address.setEditable(!isProcessedLetter);
		IMailAddress recipient = menu.getRecipient();
		if (recipient != null) {
			address.setValue(recipient.getName());
		}

		text = new GuiTextBox(this.minecraft.font, leftPos + 17, topPos + 31, 122, 57);
		text.setMaxLength(128);
		text.setEditable(!isProcessedLetter);
		if (!menu.getText().isEmpty()) {
			text.setValue(menu.getText());
		}

		addWidget(address);
		addWidget(text);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (isProcessedLetter) {
			return super.keyPressed(key, scanCode, modifiers);
		}

		// Set focus or enter text into address
		if (this.address.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER) {
				this.address.setFocus(false);
			} else {
				this.address.keyPressed(key, scanCode, modifiers);
			}
			return true;
		}

		if (this.text.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER) {
				if (hasShiftDown()) {
					text.setValue(text.getValue() + "\n");
				} else {
					this.text.setFocus(false);
				}
			} else if (key == GLFW.GLFW_KEY_DOWN) {
				text.advanceLine();
			} else if (key == GLFW.GLFW_KEY_UP) {
				text.regressLine();
			} else if (text.moreLinesAllowed() || key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSLASH) {
				this.text.keyPressed(key, scanCode, modifiers);
			}
			return true;
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
	protected void renderBg(PoseStack transform, float partialTicks, int mouseY, int mouseX) {

		if (!isProcessedLetter && !checkedSessionVars) {
			checkedSessionVars = true;
			setFromSessionVars();
			String recipient = this.address.getValue();
			IPostalCarrier carrier = menu.getCarrier();
			setRecipient(recipient, carrier);
		}

		// Check for focus changes
		if (addressFocus != address.isFocused()) {
			String recipient = this.address.getValue();
			IPostalCarrier carrier = menu.getCarrier();
			if (StringUtils.isNotBlank(recipient)) {
				setRecipient(recipient, carrier);
			}
		}
		addressFocus = address.isFocused();
		if (textFocus != text.isFocused()) {
			setText();
		}
		textFocus = text.isFocused();

		super.renderBg(transform, partialTicks, mouseY, mouseX);

		if (this.isProcessedLetter) {
			minecraft.font.draw(transform, address.getValue(), leftPos + 49, topPos + 16, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			minecraft.font.drawWordWrap(Component.literal(text.getValue()), leftPos + 20, topPos + 34, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
		} else {
			clearTradeInfoWidgets();
			address.render(transform, mouseX, mouseY, partialTicks);    //TODO correct?
			if (menu.getCarrier().equals(PostalCarriers.TRADER.get())) {
				drawTradePreview(transform, 18, 32);
			} else {
				text.render(transform, mouseX, mouseY, partialTicks);
			}
		}
	}

	private void drawTradePreview(PoseStack transform, int x, int y) {

		Component infoString = null;
		if (menu.getTradeInfo() == null) {
			infoString = Component.translatable("for.gui.mail.no.trader");
		} else if (menu.getTradeInfo().tradegood().isEmpty()) {
			infoString = Component.translatable("for.gui.mail.nothing.to.trade");
		} else if (!menu.getTradeInfo().state().isOk()) {
			infoString = menu.getTradeInfo().state().getDescription();
		}

		if (infoString != null) {
			minecraft.font.drawWordWrap(infoString, leftPos + x, topPos + y, 119, ColourProperties.INSTANCE.get("gui.mail.lettertext"));
			return;
		}

		minecraft.font.draw(transform, Component.translatable("for.gui.mail.pleasesend"), leftPos + x, topPos + y, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

		addTradeInfoWidget(new ItemStackWidget(widgetManager, x, y + 10, menu.getTradeInfo().tradegood()));

		minecraft.font.draw(transform, Component.translatable("for.gui.mail.foreveryattached"), leftPos + x, topPos + y + 28, ColourProperties.INSTANCE.get("gui.mail.lettertext"));

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
		IPostalCarrier carrier = menu.getCarrier();
		setRecipient(recipientName, carrier);
		setText();
		minecraft.keyboardHandler.setSendRepeatsToGui(false);
		super.onClose();
	}

	private void setFromSessionVars() {
		if (SessionVars.getStringVar("mail.letter.recipient") == null) {
			return;
		}

		String recipient = SessionVars.getStringVar("mail.letter.recipient");
		String typeName = SessionVars.getStringVar("mail.letter.carrier");
		ResourceLocation carrierId = ResourceLocation.tryParse(typeName);
		IPostalCarrier carrier = PostalCarriers.REGISTRY.get().getValue(carrierId);

		if (StringUtils.isNotBlank(recipient) && carrier != null) {
			address.setValue(recipient);

			menu.setCarrier(carrier);
		}

		SessionVars.clearStringVar("mail.letter.recipient");
		SessionVars.clearStringVar("mail.letter.carrier");
	}

	private void setRecipient(String recipientName, IPostalCarrier carrier) {
		if (this.isProcessedLetter || StringUtils.isBlank(recipientName)) {
			return;
		}

		PacketLetterInfoRequest packet = new PacketLetterInfoRequest(recipientName, carrier);
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

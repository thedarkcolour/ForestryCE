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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import forestry.api.mail.EnumAddressee;
import forestry.api.mail.ITradeStationInfo;
import forestry.core.config.SessionVars;
import forestry.core.gui.GuiForestry;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.Widget;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.render.ColourProperties;
import forestry.core.utils.NetworkUtil;

public class GuiCatalogue extends GuiForestry<ContainerCatalogue> {
	private static final String BOLD_UNDERLINE = ChatFormatting.BOLD.toString() + ChatFormatting.UNDERLINE;

	@Nullable
	private Button buttonFilter;
	@Nullable
	private Button buttonUse;

	private final List<ItemStackWidget> tradeInfoWidgets = new ArrayList<>();

	public GuiCatalogue(ContainerCatalogue container, Inventory inv, Component title) {
		super(new ResourceLocation("textures/gui/book.png"), container, inv, title);

		this.imageWidth = 192;
		this.imageHeight = 192;
	}

	@Override
	public void init() {
		super.init();

		this.renderables.clear();

		addRenderableWidget(new Button.Builder(Component.literal(">"), b -> actionPerformed(2)).pos(width / 2 + 44, topPos + 150).size(12, 20).build());
		addRenderableWidget(new Button.Builder(Component.literal("<"), b -> actionPerformed(3)).pos(width / 2 - 58, topPos + 150).size(12, 20).build());

		this.buttonFilter = new Button.Builder(Component.translatable("for.gui.mail.filter.all"), b -> actionPerformed(4)).pos(width / 2 - 44, topPos + 150).size(42, 20).build();
		addRenderableWidget(this.buttonFilter);

		this.buttonUse = new Button.Builder(Component.translatable("for.gui.mail.address.copy"), b -> actionPerformed(5)).pos(width / 2, topPos + 150).size(42, 20).build();
		addRenderableWidget(this.buttonUse);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseY, int mouseX) {
		super.renderBg(graphics, partialTicks, mouseY, mouseX);

		graphics.drawString(this.font, String.format("%s / %s", menu.getPageNumber(), menu.getPageCount()), leftPos + imageWidth - 72, topPos + 12, ColourProperties.INSTANCE.get("gui.book"));

		clearTradeInfoWidgets();

		ITradeStationInfo tradeInfo = menu.getTradeInfo();

		if (this.buttonUse != null) {
			if (tradeInfo != null) {
				drawTradePreview(graphics, tradeInfo, leftPos + 38, topPos + 30);
				this.buttonUse.visible = tradeInfo.state().isOk();
			} else {
				drawNoTrade(graphics, leftPos + 38, topPos + 30);
				this.buttonUse.visible = false;
			}
		}

		if (this.buttonFilter != null) {
			this.buttonFilter.setMessage(Component.translatable("for.gui.mail.filter." + menu.getFilterIdent()));
		}
	}

	private void drawNoTrade(GuiGraphics graphics, int x, int y) {
		graphics.drawWordWrap(this.font, Component.translatable("for.gui.mail.notrades"), x, y + 18, 119, ColourProperties.INSTANCE.get("gui.book"));
	}

	private void drawTradePreview(GuiGraphics graphics, ITradeStationInfo tradeInfo, int x, int y) {
		Font font = this.font;
		graphics.drawString(font, BOLD_UNDERLINE + tradeInfo.address().getName(), x, y, ColourProperties.INSTANCE.get("gui.book"));

		graphics.drawString(font, Component.translatable("for.gui.mail.willtrade", tradeInfo.owner().getName()), x, y + 18, ColourProperties.INSTANCE.get("gui.book"));

		addTradeInfoWidget(new ItemStackWidget(widgetManager, x - leftPos, y - topPos + 28, tradeInfo.tradegood()));

		graphics.drawString(font, Component.translatable("for.gui.mail.tradefor"), x, y + 46, ColourProperties.INSTANCE.get("gui.book"));

		for (int i = 0; i < tradeInfo.required().size(); i++) {
			ItemStack itemStack = tradeInfo.required().get(i);
			addTradeInfoWidget(new ItemStackWidget(widgetManager, x - leftPos + i * 18, y - topPos + 56, itemStack));
		}

		//TODO: Fix later
		if (tradeInfo.state().isOk()) {
			graphics.drawWordWrap(font, ((MutableComponent) tradeInfo.state().getDescription()).withStyle(ChatFormatting.DARK_GREEN), x, y + 82, 119, ColourProperties.INSTANCE.get("gui.book"));
		} else {
			graphics.drawWordWrap(font, ((MutableComponent) tradeInfo.state().getDescription()).withStyle(ChatFormatting.DARK_RED), x, y + 82, 119, ColourProperties.INSTANCE.get("gui.book"));
		}
	}

	private void addTradeInfoWidget(ItemStackWidget widget) {
		tradeInfoWidgets.add(widget);
		widgetManager.add(widget);
	}

	private void clearTradeInfoWidgets() {
		for (Widget widget : tradeInfoWidgets) {
			widgetManager.remove(widget);
		}
		tradeInfoWidgets.clear();
	}

	protected void actionPerformed(int id) {
		LocalPlayer player = Minecraft.getInstance().player;
		switch (id) {
			case 0 -> player.closeContainer();
			case 2 -> // next page
					NetworkUtil.sendToServer(new PacketGuiSelectRequest(0, 0));
			case 3 -> // previous page
					NetworkUtil.sendToServer(new PacketGuiSelectRequest(1, 0));
			case 4 -> // cycle filter
					NetworkUtil.sendToServer(new PacketGuiSelectRequest(2, 0));
			case 5 -> {
				ITradeStationInfo info = menu.getTradeInfo();
				if (info != null) {
					SessionVars.setStringVar("mail.letter.recipient", info.address().getName());
					SessionVars.setStringVar("mail.letter.addressee", EnumAddressee.TRADER.toString());
				}
				player.closeContainer();
			}
		}
	}

	@Override
	protected void addLedgers() {
	}
}

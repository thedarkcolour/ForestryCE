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
package forestry.core.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.vertex.PoseStack;

import forestry.core.render.ColourProperties;

public class TextLayoutHelper {
	private static final int LINE_HEIGHT = 12;

	private final GuiForestry<?> screen;
	private final int defaultFontColor;
	private final Font font;

	public int column0;
	public int column1;
	public int column2;
	public int line;

	public TextLayoutHelper(GuiForestry<?> gui, ColourProperties fontColour) {
		this.screen = gui;
		this.font = gui.font();
		this.defaultFontColor = fontColour.get("gui.screen");
	}

	public void startPage(GuiGraphics matrices) {
		line = LINE_HEIGHT;
		matrices.pose().pushPose();
	}

	public void startPage(GuiGraphics matrices, int column0, int column1, int column2) {
		this.column0 = column0;
		this.column1 = column1;
		this.column2 = column2;

		startPage(matrices);
	}

	public int getLineY() {
		return line;
	}

	public void newLine() {
		line += LINE_HEIGHT;
	}

	public void newLineCompressed() {
		line += LINE_HEIGHT - 2;
	}

	public void newLine(int lineHeight) {
		line += lineHeight;
	}

	public void endPage(GuiGraphics graphics) {
		graphics.pose().popPose();
	}

	public void drawRow(GuiGraphics graphics, Component text0, Component text1, Component text2, int colour0, int colour1, int colour2) {
		drawLine(graphics, text0, column0, colour0);
		drawLine(graphics, text1, column1, colour1);
		drawLine(graphics, text2, column2, colour2);
	}

	public void drawRow(GuiGraphics graphics, Component text0, Component text1, int colour0, int colour1) {
		drawLine(graphics, text0, column0, colour0);
		drawLine(graphics, text1, column1, colour1);
	}

	public void drawLine(GuiGraphics graphics, Component text, int x) {
		drawLine(graphics, text, x, defaultFontColor);
	}

	public void drawCenteredLine(GuiGraphics graphics, Component text, int x, int color) {
		drawCenteredLine(graphics, text, x, screen.getSizeX(), color);
	}

	public void drawCenteredLine(GuiGraphics graphics, Component text, int x, int width, int color) {
		drawCenteredLine(graphics, text, x, 0, width, color);
	}

	public void drawCenteredLine(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
		// todo
		//graphics.drawCenteredString();

		//guiForestry.getFontRenderer().draw(graphics, text, guiForestry.getGuiLeft() + x + getCenteredOffset(text, width), guiForestry.getGuiTop() + y + line, color);
	}

	public void drawLine(GuiGraphics graphics, String text, int x, int color) {
		drawLine(graphics, text, x, 0, color);
	}

	public void drawLine(GuiGraphics graphics, Component text, int x, int color) {
		drawLine(graphics, text, x, 0, color);
	}

	public void drawLine(GuiGraphics graphics, String text, int x, int y, int color) {
		graphics.drawString(this.font, text, this.screen.getGuiLeft() + x, this.screen.getGuiTop() + y + this.line, color);
	}

	public void drawLine(GuiGraphics graphics, Component text, int x, int y, int color) {
		graphics.drawString(this.font, text, this.screen.getGuiLeft() + x, this.screen.getGuiTop() + y + this.line, color);
	}

	public void drawSplitLine(GuiGraphics graphics, String text, int x, int maxWidth, int color) {
		drawSplitLine(graphics, Component.literal(text), x, maxWidth, color);
	}

	// todo verify
	public void drawSplitLine(GuiGraphics graphics, Component text, int x, int maxWidth, int color) {
		// Modified copy of Font.drawWordWrap that uses PoseStack
		graphics.drawWordWrap(this.font, text, x, this.line, maxWidth, color);
	}

	public int getCenteredOffset(Component text) {
		return getCenteredOffset(text, screen.getSizeX());
	}

	public int getCenteredOffset(Component text, int xWidth) {
		return (xWidth - screen.font().width(text)) / 2;
	}
}

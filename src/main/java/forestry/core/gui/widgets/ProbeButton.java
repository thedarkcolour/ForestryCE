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
package forestry.core.gui.widgets;

import forestry.api.core.tooltips.ToolTip;
import forestry.api.modules.IForestryPacketServer;
import forestry.core.gui.GuiEscritoire;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.utils.SoundUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ProbeButton extends Widget {

	private final GuiEscritoire guiEscritoire;
	private boolean pressed;

	public ProbeButton(GuiEscritoire guiEscritoire, WidgetManager manager, int xPos, int yPos) {
		super(manager, xPos, yPos);
		this.guiEscritoire = guiEscritoire;
        this.width = 22;
        this.height = 25;
	}

	@Override
	public void draw(GuiGraphics graphics, int startX, int startY) {
		graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		graphics.blit(this.manager.gui.textureFile, startX + this.xPos, startY + this.yPos, 228, this.pressed ? 47 : 22, this.width, this.height);
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		ToolTip tooltip = new ToolTip();
		tooltip.add(Component.translatable("for.gui.escritoire.probe"));
		return tooltip;
	}

	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton) {
        this.pressed = true;
        IForestryPacketServer packet = new PacketGuiSelectRequest(-1, 0);
        PacketDistributor.sendToServer(packet);
        SoundUtil.playButtonClick();
	}

	@Override
	public boolean handleMouseRelease(double mouseX, double mouseY, int eventType) {
		if (this.pressed) {
            this.pressed = false;
		}
		return false;
	}

	@Override
	public void handleMouseMove(int mouseX, int mouseY, int mouseButton, long time) {
		if (this.manager.getAtPosition(mouseX - this.guiEscritoire.getGuiLeft(), mouseY - this.guiEscritoire.getGuiTop()) != this) {
            this.pressed = false;
		}
	}
}

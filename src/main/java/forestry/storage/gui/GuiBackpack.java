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
package forestry.storage.gui;

import forestry.core.config.Constants;
import forestry.core.gui.GuiForestry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiBackpack extends GuiForestry<BackpackMenu> {
	public GuiBackpack(BackpackMenu container, Inventory inv, Component title) {
		super(getTextureString(container), container, inv, title);
		BackpackMenu.Size size = container.getSize();

		if (size == BackpackMenu.Size.T2) {
            this.imageWidth = 176;
            this.imageHeight = 192;
		}
	}

	private static String getTextureString(BackpackMenu container) {
		BackpackMenu.Size size = container.getSize();
		if (size == BackpackMenu.Size.T2) {
			return Constants.TEXTURE_PATH_GUI + "/backpack_t2.png";
		}
		return Constants.TEXTURE_PATH_GUI + "/backpack.png";
	}

	@Override
	protected void addLedgers() {

	}
}

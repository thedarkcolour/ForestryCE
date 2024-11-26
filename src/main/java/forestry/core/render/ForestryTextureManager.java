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
package forestry.core.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import forestry.api.client.ITextureManager;

public class ForestryTextureManager implements ITextureManager {
	private final ForestrySpriteUploader uploader = new ForestrySpriteUploader(Minecraft.getInstance().textureManager);

	public ForestrySpriteUploader getSpriteUploader() {
		return this.uploader;
	}

	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return this.uploader.getSprite(location);
	}
}

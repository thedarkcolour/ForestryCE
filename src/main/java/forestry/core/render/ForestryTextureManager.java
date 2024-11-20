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

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import forestry.api.IForestryApi;
import forestry.api.client.ForestrySprites;
import forestry.api.client.ITextureManager;
import forestry.api.core.IError;

public class ForestryTextureManager implements ITextureManager {
	private final ForestryAtlasHolder uploader = new ForestryAtlasHolder(Minecraft.getInstance().textureManager, ForestrySprites.TEXTURE_ATLAS);

	public ForestryAtlasHolder getSpriteUploader() {
		return this.uploader;
	}

	public void init() {
		for (IError error : IForestryApi.INSTANCE.getErrorManager().getErrors()) {
			this.uploader.accept(error.getSprite());
		}
		initDefaultSprites(this.uploader);
	}

	private static void initDefaultSprites(Consumer<ResourceLocation> registry) {
		for (ResourceLocation sprite : new ResourceLocation[]{ForestrySprites.HABITAT_DESERT, ForestrySprites.HABITAT_END, ForestrySprites.HABITAT_FOREST, ForestrySprites.HABITAT_HILLS, ForestrySprites.HABITAT_JUNGLE, ForestrySprites.HABITAT_MUSHROOM, ForestrySprites.HABITAT_NETHER, ForestrySprites.HABITAT_OCEAN, ForestrySprites.HABITAT_PLAINS, ForestrySprites.HABITAT_SNOW, ForestrySprites.HABITAT_SWAMP, ForestrySprites.HABITAT_TAIGA, ForestrySprites.MISC_ACCESS_SHARED, ForestrySprites.MISC_ENERGY, ForestrySprites.MISC_HINT, ForestrySprites.ANALYZER_ANYTHING, ForestrySprites.ANALYZER_BEE, ForestrySprites.ANALYZER_CAVE, ForestrySprites.ANALYZER_CLOSED, ForestrySprites.ANALYZER_DRONE, ForestrySprites.ANALYZER_FLYER, ForestrySprites.ANALYZER_ITEM, ForestrySprites.ANALYZER_NOCTURNAL, ForestrySprites.ANALYZER_PRINCESS, ForestrySprites.ANALYZER_PURE_BREED, ForestrySprites.ANALYZER_PURE_CAVE, ForestrySprites.ANALYZER_PURE_FLYER, ForestrySprites.ANALYZER_PURE_NOCTURNAL, ForestrySprites.ANALYZER_QUEEN, ForestrySprites.ANALYZER_TREE, ForestrySprites.ANALYZER_SAPLING, ForestrySprites.ANALYZER_POLLEN, ForestrySprites.ANALYZER_FLUTTER, ForestrySprites.ANALYZER_BUTTERFLY, ForestrySprites.ANALYZER_SERUM, ForestrySprites.ANALYZER_CATERPILLAR, ForestrySprites.ANALYZER_COCOON, ForestrySprites.ERROR_ERRORED, ForestrySprites.ERROR_UNKNOWN, ForestrySprites.SLOT_BLOCKED, ForestrySprites.SLOT_BLOCKED_2, ForestrySprites.SLOT_LIQUID, ForestrySprites.SLOT_CONTAINER, ForestrySprites.SLOT_LOCKED, ForestrySprites.SLOT_COCOON, ForestrySprites.SLOT_BEE, ForestrySprites.MAIL_CARRIER_PLAYER, ForestrySprites.MAIL_CARRIER_TRADER}) {
			registry.accept(sprite);
		}
	}

	@Override
	public TextureAtlasSprite getSprite(ResourceLocation location) {
		return this.uploader.getSprite(location);
	}
}

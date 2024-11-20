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
package forestry.arboriculture;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import net.minecraftforge.client.event.TextureStitchEvent;

import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;

// Fruits that grow unripe in tree leaves, then ripen over time.
public class RipeningFruit extends Fruit {
	private final int colourCallow;
	private final int diffR;
	private final int diffG;
	private final int diffB;
	private final ResourceLocation sprite;

	public RipeningFruit(boolean dominant, int ripeningPeriod, ResourceLocation sprite, int ripe, int callow, List<IProduct> products) {
		super(dominant, ripeningPeriod, products);
		this.sprite = sprite;

		this.colourCallow = callow;
		this.diffR = (ripe >> 16 & 255) - (callow >> 16 & 255);
		this.diffG = (ripe >> 8 & 255) - (callow >> 8 & 255);
		this.diffB = (ripe & 255) - (callow & 255);
	}

	private float getRipeningStage(int ripeningTime) {
		if (ripeningTime >= ripeningPeriod) {
			return 1.0f;
		}

		return (float) ripeningTime / ripeningPeriod;
	}

	@Override
	public boolean isFruitLeaf() {
		return true;
	}

	@Override
	public int getColour(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime) {
		float stage = getRipeningStage(ripeningTime);
		return getColour(stage);
	}

	private int getColour(float stage) {
		int r = (colourCallow >> 16 & 255) + (int) (diffR * stage);
		int g = (colourCallow >> 8 & 255) + (int) (diffG * stage);
		int b = (colourCallow & 255) + (int) (diffB * stage);

		return (r & 255) << 16 | (g & 255) << 8 | b & 255;
	}

	@Override
	public int getDecorativeColor() {
		return getColour(1.0f);
	}

	@Override
	public ResourceLocation getSprite(IGenome genome, BlockGetter world, BlockPos pos, int ripeningTime) {
		return this.sprite;
	}

	@Override
	public ResourceLocation getDecorativeSprite() {
		return this.sprite;
	}

	@Override
	public int getRipeningPeriod() {
		return this.ripeningPeriod;
	}
}

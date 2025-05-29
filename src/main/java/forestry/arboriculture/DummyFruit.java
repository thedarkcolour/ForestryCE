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

import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

public class DummyFruit implements IFruit {
	private final boolean dominant;

	public DummyFruit(boolean dominant) {
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public List<ItemStack> getFruits(IGenome genome, Level level, int ripeningTime) {
		return List.of();
	}

	@Override
	public int getColour(IGenome genome, BlockGetter level, BlockPos pos, int ripeningTime) {
		return 0xffffff;
	}

	@Override
	public int getDecorativeColor() {
		return 0xffffff;
	}

	@Override
	public boolean isFruitLeaf() {
		return false;
	}

	@Override
	public float getFruitChance(IGenome genome, LevelAccessor level) {
        return genome.getActiveValue(TreeChromosomes.YIELD) * 2.5f;
	}

	@Override
	public int getRipeningPeriod() {
		return 0;
	}

	@Override
	public List<IProduct> getProducts() {
		return List.of();
	}

	@Override
	public List<IProduct> getSpecialty() {
		return List.of();
	}

	@Nullable
	@Override
	public ResourceLocation getSprite(IGenome genome, BlockGetter level, BlockPos pos, int ripeningTime) {
		return getDecorativeSprite();
	}

}

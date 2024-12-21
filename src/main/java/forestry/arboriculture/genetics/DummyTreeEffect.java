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
package forestry.arboriculture.genetics;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import forestry.api.arboriculture.genetics.ITreeEffect;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;

public class DummyTreeEffect implements ITreeEffect {
	private final boolean dominant;

	public DummyTreeEffect(boolean dominant) {
		this.dominant = dominant;
	}

	@Override
	public boolean isDominant() {
		return this.dominant;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

	@Override
	public IEffectData validateStorage(IEffectData storedData) {
		return storedData;
	}

	@Override
	public IEffectData doEffect(IGenome genome, IEffectData storedData, Level level, BlockPos pos) {
		return storedData;
	}

	@Override
	public void doAnimationEffect(IGenome genome, Level level, BlockPos pos, RandomSource rand) {
	}
}

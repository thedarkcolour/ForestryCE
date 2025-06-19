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
package forestry.core.genetics;

import forestry.api.genetics.IEffectData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EffectData implements IEffectData {
	private final int[] intData;
	private final boolean[] boolData;

	public EffectData(int intSize, int boolSize) {
		this.intData = new int[intSize];
		this.boolData = new boolean[boolSize];
	}

	@Override
	public void setInteger(int index, int val) {
        this.intData[index] = val;
	}

	@Override
	public void setBoolean(int index, boolean val) {
        this.boolData[index] = val;
	}

	@Override
	public int getInteger(int index) {
		return this.intData[index];
	}

	@Override
	public boolean getBoolean(int index) {
		return this.boolData[index];
	}

	public int getIntSize() {
		return this.intData.length;
	}

	@Override
	public void read(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
	}

	@Override
	public CompoundTag write(CompoundTag CompoundNBT, HolderLookup.Provider registries) {
		return CompoundNBT;
	}
}

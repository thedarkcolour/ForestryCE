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
package forestry.apiculture.blocks;

import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.tiles.TileBeeHousingBase;
import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeApiculture implements IBlockType {
	BEE_HOUSE(ApicultureTiles.BEE_HOUSE, "bee_house"),
	APIARY(ApicultureTiles.APIARY, "apiary");

	private final IMachineProperties<?> machineProperties;

	<T extends TileBeeHousingBase> BlockTypeApiculture(FeatureTileType<? extends T> teClass, String name) {
		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
				.setClientTicker(TileBeeHousingBase::clientTick)
				.setServerTicker(TileBeeHousingBase::serverTick)
				.create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}

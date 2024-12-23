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
package forestry.core.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import forestry.core.features.CoreTiles;
import forestry.core.tiles.TileAnalyzer;
import forestry.core.tiles.TileEscritoire;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeCoreTesr implements IBlockType {
	ANALYZER(createAnalyzerProperties(CoreTiles.ANALYZER, "analyzer")),
	ESCRITOIRE(createEscritoireProperties(CoreTiles.ESCRITOIRE, "escritoire"));

	private final IMachineProperties<?> machineProperties;

	private static IMachineProperties<? extends TileAnalyzer> createAnalyzerProperties(FeatureTileType<TileAnalyzer> teClass, String name) {
		return new MachineProperties.Builder<>(teClass, name)
				.setShape(Shapes::block)
				.setServerTicker(TileAnalyzer::serverTick)
				.create();
	}

	private static MachineProperties<? extends TileEscritoire> createEscritoireProperties(FeatureTileType<TileEscritoire> teClass, String name) {
		VoxelShape desk = Block.box(0, 8, 0, 16, 11.5, 16);
		VoxelShape standRB = Block.box(13, 0, 13, 15, 10, 15);
		VoxelShape standRF = Block.box(13, 0, 1, 15, 10, 3);
		VoxelShape standLB = Block.box(1, 0, 13, 3, 10, 15);
		VoxelShape standLF = Block.box(1, 0, 1, 3, 10, 3);
		VoxelShape commonShape = Shapes.or(desk, standLB, standLF, standRB, standRF);

		// Order: S-W-N-E (according to Direction.get2DDataValue)
		VoxelShape[] shapes = {
				Shapes.or(commonShape, Block.box(0, 10, 0, 16, 16, 3.5)),
				Shapes.or(commonShape, Block.box(12.5, 10, 0, 16, 16, 16)),
				Shapes.or(commonShape, Block.box(0, 10, 12.5, 16, 16, 16)),
				Shapes.or(commonShape, Block.box(0, 10, 0, 3.5, 16, 16)),
		};

		return new MachineProperties.Builder<>(teClass, name)
				.setShape((state, level, pos, context) -> shapes[state.getValue(BlockBase.FACING).get2DDataValue()])
				.create();
	}

	BlockTypeCoreTesr(IMachineProperties<?> machineProperties) {
		this.machineProperties = machineProperties;
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

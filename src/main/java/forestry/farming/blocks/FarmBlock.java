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
package forestry.farming.blocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import forestry.core.blocks.BlockStructure;
import forestry.farming.tiles.TileFarmControl;
import forestry.farming.tiles.TileFarmGearbox;
import forestry.farming.tiles.TileFarmHatch;
import forestry.farming.tiles.TileFarmPlain;
import forestry.farming.tiles.TileFarmValve;

public class FarmBlock extends BlockStructure implements EntityBlock {
	private final EnumFarmBlockType type;
	private final EnumFarmMaterial farmMaterial;

	// todo replace with boolean "is_band"
	public static final BooleanProperty BAND = BooleanProperty.create("band");

	public FarmBlock(EnumFarmBlockType type, EnumFarmMaterial farmMaterial) {
		super(Block.Properties.of().strength(1.0f));
		this.type = type;
		this.farmMaterial = farmMaterial;
	}

	public static FarmBlock create(EnumFarmBlockType type, EnumFarmMaterial material) {
		if (type == EnumFarmBlockType.PLAIN) {
			return new Plain(material);
		} else {
			return new FarmBlock(type, material);
		}
	}

	public EnumFarmBlockType getType() {
		return this.type;
	}

	public EnumFarmMaterial getFarmMaterial() {
		return this.farmMaterial;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (this.type) {
			case GEARBOX -> new TileFarmGearbox(pos, state);
			case HATCH -> new TileFarmHatch(pos, state);
			case VALVE -> new TileFarmValve(pos, state);
			case CONTROL -> new TileFarmControl(pos, state);
			default -> new TileFarmPlain(pos, state);
		};
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side) {
		return this.type == EnumFarmBlockType.CONTROL;
	}

	public static class Plain extends FarmBlock {
		public Plain(EnumFarmMaterial farmMaterial) {
			super(EnumFarmBlockType.PLAIN, farmMaterial);

			registerDefaultState(defaultBlockState().setValue(BAND, false));
		}

		@Override
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
			builder.add(BAND);
		}
	}
}

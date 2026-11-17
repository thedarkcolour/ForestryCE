package forestry.agriculture.multifarm.blocks;

import forestry.core.platform.block.AbstractMultiblockBlock;
import forestry.agriculture.multifarm.tiles.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

public class MultifarmBlock extends AbstractMultiblockBlock implements EntityBlock {
	private final MultifarmBlockType type;
	private final MultifarmMaterialType farmMaterial;

	public static final BooleanProperty BAND = BooleanProperty.create("band");

	public MultifarmBlock(MultifarmBlockType type, MultifarmMaterialType farmMaterial) {
		super(Block.Properties.of().strength(1.0f));
		this.type = type;
		this.farmMaterial = farmMaterial;
	}

	public static MultifarmBlock create(MultifarmBlockType type, MultifarmMaterialType material) {
		if (type == MultifarmBlockType.PLAIN) {
			return new Plain(material);
		} else {
			return new MultifarmBlock(type, material);
		}
	}

	public MultifarmBlockType getType() {
		return this.type;
	}

	public MultifarmMaterialType getFarmMaterial() {
		return this.farmMaterial;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (this.type) {
			case GEARBOX -> new MultifarmGearboxBlockEntity(pos, state);
			case HATCH -> new MultifarmHatchBlockEntity(pos, state);
			case VALVE -> new MultifarmValveBlockEntity(pos, state);
			case CONTROL -> new MultifarmControlBlockEntity(pos, state);
			default -> new PlainMultifarmBlockEntity(pos, state);
		};
	}

	@Nullable
	@Override
	public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		// The holder may be ANY farm member type, so return a ticker for every member; the body is guarded
		// by the anchor + assembled check (spec §7.1).
		return forestry.core.platform.multiblock.MultiblockTicker.getTicker(level);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side) {
		return this.type == MultifarmBlockType.CONTROL;
	}

	public static class Plain extends MultifarmBlock {
		public Plain(MultifarmMaterialType farmMaterial) {
			super(MultifarmBlockType.PLAIN, farmMaterial);

			registerDefaultState(defaultBlockState().setValue(BAND, false));
		}

		@Override
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
			builder.add(BAND);
		}
	}
}

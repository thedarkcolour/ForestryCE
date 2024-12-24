package forestry.arboriculture.blocks;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.features.ArboricultureBlocks;

import org.jetbrains.annotations.Nullable;

public class BlockForestryLog extends RotatedPillarBlock implements IWoodTyped {
	private final WoodBlockKind kind;
	private final boolean fireproof;
	private final IWoodType woodType;

	public BlockForestryLog(WoodBlockKind kind, boolean fireproof, IWoodType woodType) {
		super(BlockForestryPlank.createWoodProperties(woodType));
		this.kind = kind;
		this.fireproof = fireproof;
		this.woodType = woodType;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return this.kind;
	}

	@Override
	public final boolean isFireproof() {
		return fireproof;
	}

	@Override
	public IWoodType getWoodType() {
		return woodType;
	}

	@Override
	public final int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		if (fireproof) {
			return 0;
		} else if (face == Direction.DOWN) {
			return 20;
		} else if (face != Direction.UP) {
			return 10;
		} else {
			return 5;
		}
	}

	@Override
	public final int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		if (fireproof) {
			return 0;
		}
		return 5;
	}

	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
		if (toolAction == ToolActions.AXE_STRIP) {
			if (this.woodType instanceof ForestryWoodType type) {
				if (this.kind == WoodBlockKind.LOG) {
					return (this.fireproof ? ArboricultureBlocks.STRIPPED_LOGS_FIREPROOF : ArboricultureBlocks.STRIPPED_LOGS)
							.get(type).defaultState().setValue(AXIS, state.getValue(AXIS));
				} else if (this.kind == WoodBlockKind.WOOD) {
					return (this.fireproof ? ArboricultureBlocks.STRIPPED_WOOD_FIREPROOF : ArboricultureBlocks.STRIPPED_WOOD)
							.get(type).defaultState().setValue(AXIS, state.getValue(AXIS));
				}
			}
		}
		return super.getToolModifiedState(state, context, toolAction, simulate);
	}
}

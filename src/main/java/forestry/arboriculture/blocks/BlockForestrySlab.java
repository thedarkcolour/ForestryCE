package forestry.arboriculture.blocks;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.IWoodTyped;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockForestrySlab extends SlabBlock implements IWoodTyped {
	private final boolean fireproof;
	private final IWoodType woodType;

	public BlockForestrySlab(BlockForestryPlank plank) {
		super(Block.Properties.ofFullCopy(plank));
		this.fireproof = plank.isFireproof();
		this.woodType = plank.getWoodType();
	}

	@Override
	public boolean isFireproof() {
		return this.fireproof;
	}

	@Override
	public IWoodType getWoodType() {
		return this.woodType;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return this.fireproof ? 0 : 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return this.fireproof ? 0 : 5;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.SLAB;
	}
}

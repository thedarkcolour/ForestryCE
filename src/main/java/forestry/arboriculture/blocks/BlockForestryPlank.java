package forestry.arboriculture.blocks;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.IWoodTyped;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockForestryPlank extends Block implements IWoodTyped {
	private final boolean fireproof;
	private final IWoodType woodType;

	public static Properties createWoodProperties(IWoodType woodType) {
		return Block.Properties.of().strength(woodType.getHardness(), woodType.getHardness() * 1.5F).sound(SoundType.WOOD);
	}

	public BlockForestryPlank(boolean fireproof, IWoodType woodType) {
		super(createWoodProperties(woodType));
		this.fireproof = fireproof;
		this.woodType = woodType;
	}

	public IWoodType getWoodType() {
		return this.woodType;
	}

	@Override
	public boolean isFireproof() {
		return this.fireproof;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.PLANKS;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return this.fireproof ? 0 : 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return this.fireproof ? 0 : 5;
	}
}

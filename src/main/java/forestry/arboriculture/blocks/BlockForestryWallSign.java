package forestry.arboriculture.blocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.features.ArboricultureTiles;

public class BlockForestryWallSign extends WallSignBlock implements IWoodTyped {
	private final ForestryWoodType type;

	public BlockForestryWallSign(ForestryWoodType type) {
		super(Properties.of().forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollission().strength(1.0F).lootFrom(ArboricultureBlocks.SIGN.get(type)::block).ignitedByLava(), type.getWoodType());

		this.type = type;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.WALL_SIGN;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return this.type;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> actual) {
		return createTickerHelper(actual, ArboricultureTiles.SIGN.tileType(), SignBlockEntity::tick);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SignBlockEntity(ArboricultureTiles.SIGN.tileType(), pos, state);
	}
}
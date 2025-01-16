package forestry.arboriculture.blocks;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.core.utils.BlockUtil;

public class BlockForestryTrapdoor extends TrapDoorBlock implements IWoodTyped {
	private final ForestryWoodType type;

	public BlockForestryTrapdoor(ForestryWoodType type) {
		super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3f).noOcclusion().isValidSpawn(BlockUtil.NEVER_SPAWN).ignitedByLava(), type.getBlockSetType());

		this.type = type;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.TRAPDOOR;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return this.type;
	}
}

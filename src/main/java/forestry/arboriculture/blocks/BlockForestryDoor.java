package forestry.arboriculture.blocks;

import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.WoodBlockKind;
import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.IWoodTyped;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;

public class BlockForestryDoor extends DoorBlock implements IWoodTyped {
	private final ForestryWoodType woodType;

	public BlockForestryDoor(ForestryWoodType woodType) {
		super(woodType.getBlockSetType(), Block.Properties.of().strength(woodType.getHardness(), woodType.getHardness() * 1.5F).sound(SoundType.WOOD).noOcclusion());
		this.woodType = woodType;
	}

	@Override
	public WoodBlockKind getBlockKind() {
		return WoodBlockKind.DOOR;
	}

	@Override
	public boolean isFireproof() {
		return false;
	}

	@Override
	public IWoodType getWoodType() {
		return this.woodType;
	}
}

package forestry.core.content.worktable.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.core.platform.tile.ForestryBlockEntity;
import forestry.core.platform.registration.FeatureTileType;
import forestry.core.content.worktable.features.WorktableBlockEntities;

public enum WorktableBlockType implements IBlockType {
	WORKTABLE(WorktableBlockEntities.WORKTABLE, "worktable");

	private final IMachineProperties<?> machineProperties;

	WorktableBlockType(FeatureTileType<? extends ForestryBlockEntity> tileType, String name) {
		this.machineProperties = new MachineProperties.Builder<>(tileType, name).create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	@Override
	public String getSerializedName() {
		return this.machineProperties.getSerializedName();
	}
}

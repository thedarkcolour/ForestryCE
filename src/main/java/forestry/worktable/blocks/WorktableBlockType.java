package forestry.worktable.blocks;

import forestry.core.blocks.IBlockType;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.TileForestry;
import forestry.modules.features.FeatureTileType;
import forestry.worktable.features.WorktableTiles;

public enum WorktableBlockType implements IBlockType {
	WORKTABLE(WorktableTiles.WORKTABLE, "worktable");

	private final IMachineProperties<?> machineProperties;

	WorktableBlockType(FeatureTileType<? extends TileForestry> tileType, String name) {
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

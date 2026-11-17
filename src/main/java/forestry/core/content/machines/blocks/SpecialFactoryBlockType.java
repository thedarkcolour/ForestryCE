package forestry.core.content.machines.blocks;

import forestry.core.platform.block.IBlockType;
import forestry.core.platform.block.IMachineProperties;
import forestry.core.platform.block.MachineProperties;
import forestry.core.platform.config.Constants;
import forestry.core.content.machines.MillBlockEntity;
import forestry.core.content.machines.features.FactoryBlockEntities;
import forestry.core.platform.registration.FeatureTileType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum SpecialFactoryBlockType implements IBlockType {
	RAINMAKER(FactoryBlockEntities.RAINMAKER, "rainmaker", Constants.TEXTURE_PATH_BLOCK + "/rainmaker_");

	private final IMachineProperties<?> machineProperties;

	<T extends MillBlockEntity> SpecialFactoryBlockType(FeatureTileType<T> teClass, String name, String renderMillTexture) {
		final VoxelShape pedestal = Block.box(0D, 0D, 0D, 16, 1, 16);
		final VoxelShape column = Block.box(5D, 1D, 4D, 11, 16, 12);
		final VoxelShape extension = Block.box(1D, 8D, 7D, 15, 10, 9);

		this.machineProperties = new MachineProperties.Builder<>(teClass, name)
			.setShape(() -> Shapes.or(pedestal, column, extension))
			.setClientTicker(MillBlockEntity::clientTick)
			.setServerTicker(MillBlockEntity::serverTick)
			.create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}

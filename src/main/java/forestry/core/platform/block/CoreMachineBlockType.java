package forestry.core.platform.block;

import forestry.core.features.CoreTiles;
import forestry.core.content.analyzer.AnalyzerBlockEntity;
import forestry.core.content.escritoire.EscritoireBlockEntity;
import forestry.core.platform.registration.FeatureTileType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;

public enum CoreMachineBlockType implements IBlockType {
	ANALYZER(createAnalyzerProperties(CoreTiles.ANALYZER, "analyzer"), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()),
	ESCRITOIRE(createEscritoireProperties(CoreTiles.ESCRITOIRE, "escritoire"), BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion()),
	;

	private final BlockBehaviour.Properties properties;
	private final IMachineProperties<?> machineProperties;

	//I, Spearkiller, do sorrowfully declare that the following code is AI-written.
	private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
	static {
		VoxelShape south = makeBaseShape();
		SHAPES.put(Direction.SOUTH, south);
		SHAPES.put(Direction.WEST,  rotateY(south));
		SHAPES.put(Direction.NORTH, rotateY(SHAPES.get(Direction.WEST)));
		SHAPES.put(Direction.EAST,  rotateY(SHAPES.get(Direction.NORTH)));
	}

	private static IMachineProperties<? extends AnalyzerBlockEntity> createAnalyzerProperties(FeatureTileType<AnalyzerBlockEntity> teClass, String name) {
		return new MachineProperties.Builder<>(teClass, name)
			.setShape(Shapes::block)
			.setServerTicker(AnalyzerBlockEntity::serverTick)
			.create();
	}

	//I, Spearkiller, do sorrowfully declare that the following code is AI-written.
	private static VoxelShape makeBaseShape() {
		VoxelShape legs = Shapes.or(
			Block.box(14, 0, 12, 15, 10, 13),
			Block.box(14, 0, 1, 15, 10, 2),
			Block.box(1, 0, 12, 2, 10, 13),
			Block.box(1, 0, 1, 2, 10, 2)
		);

		VoxelShape tabletop = Block.box(0, 8, 0, 16, 12, 14);
		VoxelShape backLip  = Block.box(0, 12, 0, 16, 16, 4);

		VoxelShape sides = Shapes.or(
			Block.box(0, 12, 4, 1, 15, 12),
			Block.box(15, 12, 4, 16, 15, 12)
		);

		return Shapes.or(legs, tabletop, backLip, sides);
	}

	//I, Spearkiller, do sorrowfully declare that the following code is AI-written.
	private static VoxelShape rotateY(VoxelShape shape) {
		VoxelShape[] result = new VoxelShape[1];
		result[0] = Shapes.empty();

		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
			double rMinX = 1.0 - maxZ;
			double rMaxX = 1.0 - minZ;
			double rMinZ = minX;
			double rMaxZ = maxX;

			VoxelShape rotated = Block.box(
				rMinX * 16.0, minY * 16.0, rMinZ * 16.0,
				rMaxX * 16.0, maxY * 16.0, rMaxZ * 16.0
			);

			result[0] = Shapes.or(result[0], rotated);
		});

		return result[0];
	}

	private static MachineProperties<? extends EscritoireBlockEntity> createEscritoireProperties(FeatureTileType<EscritoireBlockEntity> teClass, String name) {

		//I, Spearkiller, do sorrowfully declare that the following code is AI-written.
		return new MachineProperties.Builder<>(teClass, name)
			.setShape((state, level, pos, context) ->
				SHAPES.getOrDefault(
					state.getValue(BaseBlock.FACING),
					SHAPES.get(Direction.SOUTH)
				)
			)
			.create();
	}

	CoreMachineBlockType(IMachineProperties<?> machineProperties, BlockBehaviour.Properties properties) {
		this.machineProperties = machineProperties;
		this.properties = properties;
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return this.machineProperties;
	}

	public BlockBehaviour.Properties getBlockProperties() {
		return this.properties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}

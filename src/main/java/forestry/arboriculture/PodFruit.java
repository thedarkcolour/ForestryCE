package forestry.arboriculture;

import forestry.api.IForestryApi;
import forestry.api.arboriculture.IWoodAccess;
import forestry.api.arboriculture.IWoodType;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.IPodFruit;
import forestry.api.core.IProduct;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.blocks.BlockFruitPod;
import forestry.arboriculture.blocks.ForestryPodType;
import forestry.arboriculture.features.ArboricultureBlocks;
import forestry.arboriculture.tiles.TileFruitPod;
import forestry.core.ClientsideCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

// Fruits that grow on the side of a tree's trunk, like cocoa beans
// todo use loot tables like Vanilla
public class PodFruit extends Fruit implements IPodFruit {
	private final ForestryPodType type;

	public PodFruit(boolean dominant, ForestryPodType type, List<IProduct> products) {
		super(dominant, 2, products);

		this.type = type;
	}

	@Override
	public boolean canSurviveOn(BlockState state) {
		IWoodAccess manager = IForestryApi.INSTANCE.getTreeManager().getWoodAccess();

		IWoodType woodType = switch (this.type) {
			case DATES -> ForestryWoodType.PALM;
			case PAPAYA -> ForestryWoodType.PAPAYA;
			default -> VanillaWoodType.JUNGLE;
		};

		return state.is(manager.getLogBlockTag(woodType, false)) || state.is(manager.getLogBlockTag(woodType, true));
	}

	public static boolean isValidPodLocation(LevelReader world, BlockPos pos, Direction direction, IPodFruit fruit) {
		pos = pos.relative(direction);
		if (!world.hasChunkAt(pos)) {
			return false;
		}
		return fruit.canSurviveOn(world.getBlockState(pos));
	}

	@Nullable
	public static Direction getValidPodFacing(LevelAccessor world, BlockPos pos, IFruit fruit) {
		if (!(fruit instanceof IPodFruit podFruit)) {
			return null;
		}
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			if (isValidPodLocation(world, pos, facing, podFruit)) {
				return facing;
			}
		}
		return null;
	}

	@Override
	public boolean tryPlace(LevelAccessor level, BlockPos pos, IGenome genome) {
		Direction facing = getValidPodFacing(level, pos, this);
		if (facing == null) {
			return false;
		}

		BlockState state = ArboricultureBlocks.PODS.get(this.type).defaultState().setValue(BlockFruitPod.FACING, facing);

		if (level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE)) {
			if (level.getBlockEntity(pos) instanceof TileFruitPod pod) {
				pod.setProperties(genome, this, genome.getActiveValue(TreeChromosomes.YIELD));

				if (level.isClientSide()) {
					ClientsideCode.markForUpdate(pos);
				}

				return true;
			}
		}

		return false;
	}

	public ForestryPodType getType() {
		return this.type;
	}
}

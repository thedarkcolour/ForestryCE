package forestry.farming.logic;

import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmType;
import forestry.api.farming.IFarmable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class FarmLogicArboreal extends FarmLogicHomogeneous {
	@Nullable
	private List<IFarmable> farmables;

	public FarmLogicArboreal(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public List<IFarmable> getFarmables() {
		if (this.farmables == null) {
			this.farmables = new ArrayList<>(this.type.getFarmables());
		}
		return this.farmables;
	}

	@Override
	public List<ItemStack> collect(Level level, IFarmHousing farmHousing) {
		return collectEntityItems(level, farmHousing, true);
	}

	@Override
	public Collection<ICrop> harvest(Level level, IFarmHousing farmHousing, Direction direction, int extent, BlockPos pos) {
		BlockPos position = farmHousing.getValidPosition(direction, pos, extent, pos.above());
		Collection<ICrop> crops = harvestBlocks(level, position);
		farmHousing.increaseExtent(direction, pos, extent);

		return crops;
	}

	private Collection<ICrop> harvestBlocks(Level level, BlockPos pos) {
		// Determine what type we want to harvest.
		IFarmable farmable = getFarmableForBlock(level, pos, getFarmables());
		if (farmable == null) {
			return List.of();
		}

		// get all crops of the same type that are connected to the first one
		ArrayDeque<BlockPos> knownCropPositions = new ArrayDeque<>();
		knownCropPositions.add(pos);

		Set<BlockPos> checkedBlocks = new HashSet<>();
		ArrayDeque<ICrop> crops = new ArrayDeque<>();

		while (!knownCropPositions.isEmpty()) {
			BlockPos knownCropPos = knownCropPositions.pop();
			for (BlockPos mutable : BlockPos.betweenClosed(knownCropPos.offset(-1, -1, -1), knownCropPos.offset(1, 1, 1))) {
				if (!level.hasChunkAt(mutable)) {
					return crops;
				}

				BlockPos candidate = mutable.immutable();
				if (!checkedBlocks.contains(candidate)) {
					checkedBlocks.add(candidate);

					BlockState blockState = level.getBlockState(candidate);
					ICrop crop = farmable.getCropAt(level, candidate, blockState);
					if (crop != null) {
						crops.push(crop);
						knownCropPositions.push(candidate);
					}
				}
			}
		}

		return crops;
	}

	@Nullable
	private static IFarmable getFarmableForBlock(Level world, BlockPos position, Collection<IFarmable> farmables) {
		if (world.isEmptyBlock(position)) {
			return null;
		}
		BlockState blockState = world.getBlockState(position);
		for (IFarmable farmable : farmables) {
			ICrop crop = farmable.getCropAt(world, position, blockState);
			if (crop != null) {
				return farmable;
			}
		}
		return null;
	}

	@Override
	protected boolean maintainSeedlings(Level world, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos, direction, i);

			if (world.isEmptyBlock(position)) {
				BlockPos soilPosition = position.below();
				BlockState soilState = world.getBlockState(soilPosition);
				if (isAcceptedSoil(soilState)) {
					return plantSapling(world, farmHousing, position, direction);
				}
			}
		}
		return false;
	}

	private boolean plantSapling(Level world, IFarmHousing farmHousing, BlockPos position, Direction direction) {
		Collections.shuffle(getFarmables());
		for (IFarmable candidate : getFarmables()) {
			if (farmHousing.plantGermling(candidate, world, position, direction)) {
				return true;
			}
		}

		return false;
	}
}

package forestry.agriculture.farmlogic;

import forestry.api.agriculture.IFarmHousing;
import forestry.api.agriculture.IFarmType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FarmLogicMushroom extends FarmLogicArboreal {
	// each mushroom on the farm has a 1 in 500 chance to grow per tick
	private static final int GROWTH_CHANCE = 500;

	public FarmLogicMushroom(IFarmType properties, boolean isManual) {
		super(properties, isManual);
	}

	@Override
	public List<ItemStack> collect(Level level, IFarmHousing farmHousing) {
		return List.of();//Needed to override Arboreal #collect
	}

	@Override
	protected boolean maintainSeedlings(Level world, IFarmHousing farmHousing, BlockPos pos, Direction direction, int extent) {
		// grow mushrooms while the farm is not chopping
		return super.maintainSeedlings(world, farmHousing, pos, direction, extent) || growMushroom(world, pos, direction, extent);
	}

	// vanilla mushrooms only grow from bonemeal, so the farm has to grow them
	private boolean growMushroom(Level world, BlockPos pos, Direction direction, int extent) {
		if (!(world instanceof ServerLevel level)) {
			return false;
		}

		for (int i = 0; i < extent; i++) {
			BlockPos position = translateWithOffset(pos, direction, i);
			BlockState state = level.getBlockState(position);

			if (state.getBlock() instanceof MushroomBlock mushroom
				&& isAcceptedSoil(level.getBlockState(position.below()))
				&& level.random.nextInt(GROWTH_CHANCE) == 0
				&& mushroom.growMushroom(level, position, state, level.random)) {
				return true;
			}
		}
		return false;
	}
}

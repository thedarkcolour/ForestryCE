package forestry.arboriculture.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.entities.ForestryBoat;
import forestry.arboriculture.entities.ForestryChestBoat;

public class ForestryBoatDispenserBehavior extends DefaultDispenseItemBehavior {
	private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
	private final ForestryWoodType type;
	private final boolean hasChest;

	public ForestryBoatDispenserBehavior(ForestryWoodType type, boolean hasChest) {
		this.type = type;
		this.hasChest = hasChest;
	}

	@Override
	public ItemStack execute(BlockSource source, ItemStack stack) {
		Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
		Level level = source.getLevel();
		double x = 0.5625D + (double) EntityType.BOAT.getWidth() / 2.0D;
		double y = source.x() + (double) direction.getStepX() * x;
		double z = source.y() + (double) ((float) direction.getStepY() * 1.125F);
		double d3 = source.z() + (double) direction.getStepZ() * x;
		BlockPos blockpos = source.getPos().relative(direction);
		ForestryBoat boat = (this.hasChest ? new ForestryChestBoat(level, x, y, z) : new ForestryBoat(level, x, y, z));
		boat.setWoodType(this.type);
		boat.setYRot(direction.toYRot());
		double d4;
		if (boat.canBoatInFluid(level.getFluidState(blockpos))) {
			d4 = 1.0D;
		} else {
			if (!level.getBlockState(blockpos).isAir() || !boat.canBoatInFluid(level.getFluidState(blockpos.below()))) {
				return this.defaultDispenseItemBehavior.dispense(source, stack);
			}

			d4 = 0.0D;
		}

		boat.setPos(y, z + d4, d3);
		level.addFreshEntity(boat);
		stack.shrink(1);
		return stack;
	}

	@Override
	protected void playSound(BlockSource source) {
		source.getLevel().levelEvent(1000, source.getPos(), 0);
	}
}

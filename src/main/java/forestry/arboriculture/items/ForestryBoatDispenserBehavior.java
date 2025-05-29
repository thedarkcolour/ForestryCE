package forestry.arboriculture.items;

import forestry.arboriculture.ForestryWoodType;
import forestry.arboriculture.entities.ForestryBoat;
import forestry.arboriculture.entities.ForestryChestBoat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

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
		Direction direction = source.state().getValue(DispenserBlock.FACING);
		ServerLevel level = source.level();
		double x = 0.5625D + (double) EntityType.BOAT.getWidth() / 2.0D;
		Vec3 center = source.center();
		double y = center.x() + (double) direction.getStepX() * x;
		double z = center.y() + (double) ((float) direction.getStepY() * 1.125F);
		double d3 = center.z() + (double) direction.getStepZ() * x;
		BlockPos blockpos = source.pos().relative(direction);
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
		source.level().levelEvent(1000, source.pos(), 0);
	}
}

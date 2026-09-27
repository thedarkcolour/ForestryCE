package forestry.apiculture.apiarist;

import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.apiculture.apiary.ApicultureBlock;
import forestry.apiculture.bees.ForestryBeeItem;
import forestry.apiculture.beehouse.BeeHouseBlockEntity;
import forestry.core.platform.tile.TileUtil;
import forestry.core.platform.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import forestry.apiculture.bees.BeeHousingInventory;

public class ApiaristAI extends MoveToBlockGoal {
	private final Villager villager;
	private boolean hasDrone;
	private boolean hasPrincess;
	private final SimpleContainer villagerInventory;

	private static final int SLOT_PRODUCT_1 = BeeHousingInventory.SLOT_PRODUCT_1;
	private static final int SLOT_PRODUCT_COUNT = BeeHousingInventory.SLOT_PRODUCT_COUNT;
	private static final int SLOT_QUEEN = BeeHousingInventory.SLOT_QUEEN;
	private static final int SLOT_DRONE = BeeHousingInventory.SLOT_DRONE;

	public ApiaristAI(Villager villager, double speed) {
		super(villager, speed, 16);
		this.villager = villager;
        this.villagerInventory = villager.getInventory();
	}

	@Override
	public boolean canUse() {
		if (this.nextStartTick <= 0) {
			this.hasDrone = hasBeeType(BeeLifeStage.DRONE);
			this.hasPrincess = hasBeeType(BeeLifeStage.PRINCESS);
		}
		return super.canUse();
	}

	//TODO - now tick?
	@Override
	public void tick() {
		super.tick();
		BlockPos housePos = this.blockPos.north().above();
		this.villager.getLookControl().setLookAt(housePos.getX() + 0.5D, housePos.getY(), housePos.getZ() + 0.5D, 10.0F, this.villager.getMaxHeadXRot());
		if (this.isReachedTarget()) {
			Level world = this.villager.level();

			BeeHouseBlockEntity beeHouse = (BeeHouseBlockEntity) TileUtil.getTile(world, housePos);
			if (beeHouse == null) {
				return;
			}
			BeeHousingInventory inventory = (BeeHousingInventory) beeHouse.getBeeInventory();

			//fill slots from inside bee house
			for (ItemStack stack : InventoryUtil.getStacks(inventory, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT)) {
				if (!stack.isEmpty() && stack.getItem() instanceof ForestryBeeItem geneticItem) {
					BeeLifeStage type = geneticItem.getStage();
					if (inventory.getItem(SLOT_QUEEN).isEmpty() && type == BeeLifeStage.PRINCESS) {
						inventory.setQueen(stack.copy());
						stack.setCount(0);
					} else if (type == BeeLifeStage.DRONE) {
						stack.shrink(InventoryUtil.addStack(inventory, stack, SLOT_DRONE, 1, true));
					}
				}

			}

			//fill slots from villager inventory
			if (inventory.getItem(SLOT_DRONE).isEmpty() || inventory.getItem(SLOT_QUEEN).isEmpty()) {
				boolean princessAdded = false;
				boolean droneAdded = false;
				for (ItemStack stack : InventoryUtil.getStacks(this.villagerInventory)) {
					if (princessAdded && droneAdded) {
						break;
					}
					if (!stack.isEmpty() && stack.getItem() instanceof ForestryBeeItem) {
						BeeLifeStage type = ((ForestryBeeItem) stack.getItem()).getStage();
						if (type == BeeLifeStage.DRONE && inventory.getItem(SLOT_DRONE).isEmpty()) {
							InventoryUtil.addStack(inventory, stack, SLOT_DRONE, 1, true);
							droneAdded = true;
						} else if (type == BeeLifeStage.PRINCESS && inventory.getItem(SLOT_QUEEN).isEmpty()) {
							InventoryUtil.addStack(inventory, stack, SLOT_QUEEN, 1, true);
							princessAdded = true;
						}
					}
				}
			}

			// the stacks above are grown and shrunk in place
			inventory.setChanged();

			//add remaining bees to villager inventory
			for (ItemStack stack : InventoryUtil.getStacks(inventory, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT)) {
				if (stack.getItem() instanceof ForestryBeeItem) {
					InventoryUtil.addStack(this.villagerInventory, stack, true);
				}
			}
		}
		this.nextStartTick = 20;
	}

	public boolean hasBeeType(BeeLifeStage type) {
		if (this.villagerInventory.isEmpty()) {
			return false;
		}
		for (ItemStack stack : InventoryUtil.getStacks(this.villagerInventory)) {
			if (!stack.isEmpty() && stack.getItem() instanceof ForestryBeeItem) {
				if (((ForestryBeeItem) stack.getItem()).getStage() == type) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean isValidTarget(LevelReader world, BlockPos pos) {
		pos = pos.north().above();
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof ApicultureBlock && TileUtil.getTile(world, pos) instanceof BeeHouseBlockEntity beeHouse) {
			BeeHousingInventory inventory = (BeeHousingInventory) beeHouse.getBeeInventory();
			if (inventory.isEmpty()) {
				return false;
			}
			if (!inventory.getItem(SLOT_QUEEN).isEmpty()) {
				BeeLifeStage type = ((ForestryBeeItem) inventory.getItem(SLOT_QUEEN).getItem()).getStage();
				if (type == BeeLifeStage.QUEEN) {
					return false;
				}
				if (type == BeeLifeStage.PRINCESS && !inventory.getItem(SLOT_DRONE).isEmpty() && !this.hasDrone) {
					return false;
				}
			}
			boolean foundPrincess = this.hasPrincess;
			boolean foundDrone = this.hasDrone;
			if (foundDrone && foundPrincess) {
				return true;
			}
			for (ItemStack stack : InventoryUtil.getStacks(inventory, SLOT_PRODUCT_1, SLOT_PRODUCT_COUNT)) {
				if (!stack.isEmpty() && stack.getItem() instanceof ForestryBeeItem) {
					BeeLifeStage type = ((ForestryBeeItem) stack.getItem()).getStage();
					if (type == BeeLifeStage.PRINCESS) {
						foundPrincess = true;
					}
					if (type == BeeLifeStage.DRONE) {
						foundDrone = true;
					}
					if (foundDrone && foundPrincess) {
						return true;
					}
				}
			}
			return false;
			//maybe use error states instead?
		}
		return false;
	}
}

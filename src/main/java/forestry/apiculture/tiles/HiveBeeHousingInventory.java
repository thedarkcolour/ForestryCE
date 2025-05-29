package forestry.apiculture.tiles;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

class HiveBeeHousingInventory implements IBeeHousingInventory {
	private final TileHive hive;

	@Nullable
	private ItemStack queen;
	@Nullable
	private ItemStack drone;

	public HiveBeeHousingInventory(TileHive hive) {
		this.hive = hive;
	}

	@Override
	public ItemStack getQueen() {
		if (this.queen == null) {
			IBee bee = this.hive.getContainedBee();
			this.queen = bee.createStack(BeeLifeStage.QUEEN);
		}
		return this.queen;
	}

	@Override
	public ItemStack getDrone() {
		if (this.drone == null) {
			IBee bee = this.hive.getContainedBee();
			this.drone = bee.createStack(BeeLifeStage.DRONE);
		}
		return this.drone;
	}

	@Override
	public void setQueen(ItemStack stack) {
		this.queen = stack;
	}

	@Override
	public void setDrone(ItemStack stack) {
		this.drone = stack;
	}

	@Override
	public boolean addProduct(ItemStack product, boolean all) {
		return true;
	}
}

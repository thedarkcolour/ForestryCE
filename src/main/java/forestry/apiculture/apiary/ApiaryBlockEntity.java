package forestry.apiculture.apiary;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.core.genetics.IGenome;
import forestry.apiculture.features.ApicultureTiles;
import forestry.apiculture.bees.BeeHousingMenu;
import forestry.apiculture.bees.BeeHousingScreen;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import forestry.apiculture.bees.AbstractBeeHousingBlockEntity;

public class ApiaryBlockEntity extends AbstractBeeHousingBlockEntity implements IBeeHousing {
	private final ApiaryBeeModifier beeModifier = new ApiaryBeeModifier();
	private final ApiaryBeeListener beeListener = new ApiaryBeeListener();
	private final ApiaryInventory inventory = new ApiaryInventory() {
		@Override
		public void setChanged() {
			super.setChanged();
			ApiaryBlockEntity.this.setChanged();
		}
	};

	public ApiaryBlockEntity(BlockPos pos, BlockState state) {
		super(ApicultureTiles.APIARY.tileType(), pos, state, "apiary");
		setInternalInventory(this.inventory);
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return this.inventory;
	}

	@Override
	public Collection<IBeeModifier> getBeeModifiers() {
		List<IBeeModifier> beeModifiers = new ArrayList<>();

		beeModifiers.add(this.beeModifier);

		for (Tuple<IHiveFrame, ItemStack> frame : this.inventory.getFrames()) {
			IHiveFrame hiveFrame = frame.getA();
			ItemStack stack = frame.getB();
			IBeeModifier beeModifier = hiveFrame.getBeeModifier(stack);
			beeModifiers.add(beeModifier);
		}

		return beeModifiers;
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return Collections.singleton(this.beeListener);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BeeHousingMenu(windowId, player.getInventory(), this, true, BeeHousingScreen.Icon.APIARY);
	}

	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		player.openMenu(this, buffer -> {
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(true);
			NetworkUtil.writeEnum(buffer, BeeHousingScreen.Icon.APIARY);
		});
	}

	public static class ApiaryBeeModifier implements IBeeModifier {
		@Override
		public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
			return 0.1f * currentSpeed;
		}
	}

	public class ApiaryBeeListener implements IBeeListener {
		@Override
		public void wearOutEquipment(int amount) {
			ApiaryBlockEntity.this.inventory.wearOutFrames(ApiaryBlockEntity.this, amount);
		}

	}
}

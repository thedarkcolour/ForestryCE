package forestry.apiculture.beehouse;

import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.genetics.IGenome;
import forestry.api.core.genetics.IMutation;
import forestry.apiculture.bees.AbstractBeeHousingBlockEntity;
import forestry.apiculture.bees.BeeHousingMenu;
import forestry.apiculture.bees.BeeHousingScreen;
import forestry.apiculture.bees.BeeHousingInventory;
import forestry.apiculture.features.ApicultureTiles;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BeeHouseBlockEntity extends AbstractBeeHousingBlockEntity {
	private final BeeHousingInventory beeInventory;

	public BeeHouseBlockEntity(BlockPos pos, BlockState state) {
		super(ApicultureTiles.BEE_HOUSE.tileType(), pos, state, "bee.house");

		this.beeInventory = new BeeHousingInventory(12) {
			@Override
			public void setChanged() {
				super.setChanged();
				BeeHouseBlockEntity.this.setChanged();
			}
		};
		this.beeInventory.disableAutomation();
		setInternalInventory(this.beeInventory);
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return this.beeInventory;
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return Collections.singleton(BeehouseBeeModifier.INSTANCE);
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return List.of();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new BeeHousingMenu(windowId, player.getInventory(), this, false, BeeHousingScreen.Icon.BEE_HOUSE);
	}

	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		player.openMenu(this, buffer -> {
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(false);
			NetworkUtil.writeEnum(buffer, BeeHousingScreen.Icon.BEE_HOUSE);
		});
	}

	// no mutations/ignoble decay, 300% aging and flowering, 25% production
	private enum BeehouseBeeModifier implements IBeeModifier {
		INSTANCE;

		@Override
		public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
			return 0.25f * currentSpeed;
		}

		@Override
		public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
			return 0.0f;
		}

		@Override
		public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
			return currentAging / 3f;
		}

		@Override
		public float modifyPollination(IGenome genome, float currentPollination) {
			return 3.0f * currentPollination;
		}

		@Override
		public float modifyGeneticDecay(IGenome genome, float currentDecay) {
			return 0.0f;
		}
	}
}

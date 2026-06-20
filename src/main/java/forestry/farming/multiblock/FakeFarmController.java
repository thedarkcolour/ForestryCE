package forestry.farming.multiblock;

import com.mojang.authlib.GameProfile;
import forestry.api.IForestryApi;
import forestry.api.core.HumidityType;
import forestry.api.core.IErrorLogic;
import forestry.api.core.TemperatureType;
import forestry.api.farming.ForestryFarmTypes;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.errors.FakeErrorLogic;
import forestry.core.fluids.FakeTankManager;
import forestry.core.fluids.ITankManager;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.owner.FakeOwnerHandler;
import forestry.core.owner.IOwnerHandler;
import forestry.farming.FarmTarget;
import forestry.farming.gui.IFarmLedgerDelegate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The "no controller" stand-in resolved by {@code MultiblockLogicFarm.getController()} when a block is not
 * part of an assembled farm (spec §7.2, §9). Reshaped onto the trimmed public
 * {@link IFarmControllerInternal} after the engine rewrite (no engine-internal surface).
 */
public enum FakeFarmController implements IFarmControllerInternal {
	INSTANCE;

	@Override
	public BlockPos getCoords() {
		return BlockPos.ZERO;
	}

	@Override
	public Vec3i getArea() {
		return Vec3i.ZERO;
	}

	@Override
	public Vec3i getOffset() {
		return Vec3i.ZERO;
	}

	@Override
	public boolean doWork() {
		return false;
	}

	@Override
	public boolean hasLiquid(FluidStack liquid) {
		return false;
	}

	@Override
	public void removeLiquid(FluidStack liquid) {
	}

	@Override
	public boolean plantGermling(IFarmable farmable, Level world, BlockPos pos, Direction direction) {
		return false;
	}

	@Override
	public IFarmInventoryInternal getFarmInventory() {
		return FakeFarmInventory.INSTANCE;
	}

	@Override
	public void setUpFarmlandTargets(Map<Direction, List<FarmTarget>> targets) {
	}

	@Override
	public BlockPos getTopCoord() {
		return BlockPos.ZERO;
	}

	@Override
	public BlockPos getCoordinates() {
		return BlockPos.ZERO;
	}

	@Override
	public void addPendingProduct(ItemStack stack) {
	}

	@Override
	public void setFarmLogic(Direction direction, IFarmLogic logic) {
	}

	@Override
	public IFarmLogic getFarmLogic(Direction direction) {
		// BUG 2 (defensive): never throw from a render-reachable method. A transient Fake controller can be
		// resolved by the GUI during a client reload (e.g. before the holder's description packet reconstructs
		// the real controller). Return the default arboreal logic — the same default FarmController.resetFarmLogic
		// installs — so GuiFarm/FarmLogicSlot draw a sane icon instead of crashing with IllegalStateException.
		return IForestryApi.INSTANCE.getFarmingManager().getFarmType(ForestryFarmTypes.ARBOREAL).getLogic(false);
	}

	@Override
	public Collection<IFarmLogic> getFarmLogics() {
		return List.of();
	}

	@Override
	public void resetFarmLogic(Direction direction) {
	}

	@Override
	public int getStoredFertilizerScaled(int scale) {
		return 0;
	}

	@Override
	public BlockPos getFarmCorner(Direction direction) {
		return null;
	}

	@Override
	public int getSocketCount() {
		return 0;
	}

	@Override
	public ItemStack getSocket(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {
	}

	@Override
	public ResourceLocation getSocketType() {
		return null;
	}

	@Override
	public IFarmLedgerDelegate getFarmLedgerDelegate() {
		// BUG 2 (defensive): never throw from a render-reachable method. GuiFarm.addLedgers and
		// FarmLogicSlot's tooltip both call this on the resolved controller, which may transiently be the Fake
		// during a client reload. Return a no-op delegate (zeros / NORMAL climate) instead of crashing.
		return FakeFarmLedgerDelegate.INSTANCE;
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return FakeInventoryAdapter.INSTANCE;
	}

	@Override
	public ITankManager getTankManager() {
		return FakeTankManager.instance;
	}

	public String getUnlocalizedType() {
		return "for.multiblock.farm.type";
	}

	@Override
	@Nullable
	public Level getWorldObj() {
		return null;
	}

	/* IClimateProvider */
	@Override
	public TemperatureType temperature() {
		return TemperatureType.NORMAL;
	}

	@Override
	public HumidityType humidity() {
		return HumidityType.NORMAL;
	}

	/* IErrorLogicSource */
	@Override
	public IErrorLogic getErrorLogic() {
		return FakeErrorLogic.INSTANCE;
	}

	/* IOwnedTile */
	@Override
	public IOwnerHandler getOwnerHandler() {
		return FakeOwnerHandler.INSTANCE;
	}

	/* IStreamableGui */
	@Override
	public void writeGuiData(FriendlyByteBuf data) {
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
	}

	/* IMultiblockController */
	@Override
	public boolean isAssembled() {
		return false;
	}

	@Override
	public void reassemble() {
	}

	@Override
	@Nullable
	public String getLastValidationError() {
		return null;
	}

	@Override
	public Collection<IMultiblockComponent> getComponents() {
		return List.of();
	}

	@Override
	public boolean isValidPlatform(Level world, BlockPos pos) {
		return false;
	}

	@Override
	public int getExtents(Direction direction, BlockPos pos) {
		return 0;
	}

	@Override
	public void setExtents(Direction direction, BlockPos pos, int extend) {
	}

	@Override
	public void cleanExtents(Direction direction) {
	}

	/** No-op ledger delegate so the farm GUI ledgers/tooltips never crash on a transient Fake (BUG 2 defensive). */
	private enum FakeFarmLedgerDelegate implements IFarmLedgerDelegate {
		INSTANCE;

		@Override
		public float getHydrationModifier() {
			return 0;
		}

		@Override
		public float getHydrationTempModifier() {
			return 0;
		}

		@Override
		public float getHydrationHumidModifier() {
			return 0;
		}

		@Override
		public float getHydrationRainfallModifier() {
			return 0;
		}

		@Override
		public double getDrought() {
			return 0;
		}

		@Override
		public TemperatureType temperature() {
			return TemperatureType.NORMAL;
		}

		@Override
		public HumidityType humidity() {
			return HumidityType.NORMAL;
		}
	}

	private enum FakeFarmInventory implements IFarmInventoryInternal {
		INSTANCE;

		@Override
		public boolean hasResources(List<ItemStack> resources) {
			return false;
		}

		@Override
		public void removeResources(List<ItemStack> resources) {
		}

		@Override
		public boolean acceptsAsSeedling(ItemStack stack) {
			return false;
		}

		@Override
		public boolean acceptsAsResource(ItemStack stack) {
			return false;
		}

		@Override
		public boolean acceptsAsFertilizer(ItemStack stack) {
			return false;
		}

		@Override
		public Container getProductInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getGermlingsInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getResourcesInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public Container getFertilizerInventory() {
			return FakeInventoryAdapter.INSTANCE;
		}

		@Override
		public int getFertilizerValue() {
			return 0;
		}

		@Override
		public boolean useFertilizer() {
			return false;
		}

		@Override
		public void stowProducts(Iterable<ItemStack> harvested, ArrayDeque<ItemStack> pendingProduce) {
		}

		@Override
		public boolean tryAddPendingProduce(ArrayDeque<ItemStack> pendingProduce) {
			return false;
		}
	}
}

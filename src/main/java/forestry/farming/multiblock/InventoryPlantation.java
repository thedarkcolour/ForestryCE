package forestry.farming.multiblock;

import forestry.api.ForestryDataMaps;
import forestry.api.IForestryApi;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;
import forestry.core.config.ForestryConfig;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.TankManager;
import forestry.core.inventory.InventoryAdapterRestricted;
import forestry.core.inventory.wrappers.InventoryMapper;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.SlotUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

/**
 * The basic class for the two plantation inventories. (Multiblock Farm / Cultivators)
 * <p>
 * It contains the biggest part of the logic for the inventories like item validation and fertilizer consumption.
 */
public abstract class InventoryPlantation<H extends ILiquidTankTile & IFarmHousing> extends InventoryAdapterRestricted implements IFarmInventoryInternal {
	/**
	 * Farm logic object
	 */
	protected final H housing;
	/**
	 * Inventory slot config
	 */
	protected final InventoryConfig config;

	/**
	 * The part of the inventory that contains the resources.
	 */
	protected final Container resourcesInventory;
	/**
	 * The part of the inventory that contains the germlings.
	 */
	protected final Container germlingsInventory;
	/**
	 * The part of the inventory that contains the output resources.
	 */
	protected final Container productInventory;
	/**
	 * The part of the inventory that contains the fertilizer.
	 */
	protected final Container fertilizerInventory;
	private final ModConfigSpec.IntValue fertilizerModifier;

	/**
	 * Creates a inventory instance.
	 *
	 * @param housing Logic object of the farm that owns this inventory
	 * @param config  Helper object that defines the slots of the inventory
	 */
	public InventoryPlantation(H housing, InventoryConfig config, ModConfigSpec.IntValue fertilizerModifier) {
		super(config.count, "Items");
		this.housing = housing;
		this.config = config;

		this.resourcesInventory = new InventoryMapper(this, config.resourcesStart, config.resourcesCount);
		this.germlingsInventory = new InventoryMapper(this, config.germlingsStart, config.germlingsCount);
		this.productInventory = new InventoryMapper(this, config.productionStart, config.productionCount);
		this.fertilizerInventory = new InventoryMapper(this, config.fertilizerStart, config.fertilizerCount);
		this.fertilizerModifier = fertilizerModifier;
	}

	@Override
	public boolean canSlotAccept(int slotIndex, ItemStack stack) {
		if (SlotUtil.isSlotInRange(slotIndex, this.config.fertilizerStart, this.config.fertilizerCount)) {
			return acceptsAsFertilizer(stack);
		} else if (SlotUtil.isSlotInRange(slotIndex, this.config.germlingsStart, this.config.germlingsCount)) {
			return acceptsAsSeedling(stack);
		} else if (SlotUtil.isSlotInRange(slotIndex, this.config.resourcesStart, this.config.productionCount)) {
			return acceptsAsResource(stack);
		} else if (SlotUtil.isSlotInRange(slotIndex, this.config.canStart, this.config.canCount)) {
			Optional<FluidStack> fluid = FluidUtil.getFluidContained(stack);
			return fluid.map(f -> this.housing.getTankManager().canFillFluidType(f)).orElse(false);
		}
		return false;
	}

	@Override
	public boolean canTakeItemThroughFace(int slotIndex, ItemStack stack, Direction side) {
		return SlotUtil.isSlotInRange(slotIndex, this.config.productionStart, this.config.productionCount);
	}

	@Override
	public boolean hasResources(List<ItemStack> resources) {
		return InventoryUtil.contains(this.resourcesInventory, resources);
	}

	@Override
	public void removeResources(List<ItemStack> resources) {
		InventoryUtil.removeSets(this.resourcesInventory, 1, resources, null, false, false, true);
	}

	@Override
	public boolean acceptsAsSeedling(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		for (IFarmLogic logic : this.housing.getFarmLogics()) {
			if (logic.getType().isAcceptedSeedling(stack)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean acceptsAsResource(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		for (IFarmLogic logic : this.housing.getFarmLogics()) {
			if (logic.getType().isAcceptedResource(stack)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean acceptsAsFertilizer(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		return IForestryApi.INSTANCE.getFarmingManager().getFertilizeValue(stack) > 0;
	}

	@Override
	public Container getProductInventory() {
		return this.productInventory;
	}

	@Override
	public Container getGermlingsInventory() {
		return this.germlingsInventory;
	}

	@Override
	public Container getResourcesInventory() {
		return this.resourcesInventory;
	}

	@Override
	public Container getFertilizerInventory() {
		return this.fertilizerInventory;
	}

	public void drainCan(TankManager tankManager) {
		FluidHelper.drainContainers(tankManager, this, this.config.canStart);
	}

	/**
	 * Plants a germling / sapling at the given location.
	 *
	 * @param germling The germling to be placed
	 * @param player   The placer of the germling. Most likely a fake player
	 * @param pos      The position the germling should be placed on
	 * @return True if the germling was placed, false otherwise
	 */
	public abstract boolean plantGermling(IFarmable germling, Player player, BlockPos pos);

	@Override
	public void stowProducts(Iterable<ItemStack> harvested, ArrayDeque<ItemStack> pendingProduce) {
		for (ItemStack harvest : harvested) {
			int added = InventoryUtil.addStack(this.productInventory, harvest, true);
			harvest.shrink(added);
			if (!harvest.isEmpty()) {
				pendingProduce.push(harvest);
			}
		}
	}

	@Override
	public boolean tryAddPendingProduce(ArrayDeque<ItemStack> pendingProduce) {
		Container productInventory = getProductInventory();

		ItemStack next = pendingProduce.peek();
		boolean added = InventoryUtil.tryAddStack(productInventory, next, true, true);

		if (added) {
			pendingProduce.pop();
		}

		return added;
	}

	@Override
	public int getFertilizerValue() {
		ItemStack fertilizerStack = getItem(this.config.fertilizerStart);
		if (fertilizerStack.isEmpty()) {
			return 0;
		}

		Integer fertilizerValue = fertilizerStack.getItemHolder().getData(ForestryDataMaps.FARM_FERTILIZERS);
		if (fertilizerValue != null && fertilizerValue > 0) {
			return fertilizerValue * this.fertilizerModifier.get();
		}
		return 0;
	}

	@Override
	public boolean useFertilizer() {
		ItemStack fertilizer = getItem(this.config.fertilizerStart);
		if (acceptsAsFertilizer(fertilizer)) {
			removeItem(this.config.fertilizerStart, 1);
			return true;
		}
		return false;
	}

	/**
	 * Describes the slots if a farm inventory
	 */
	public static class InventoryConfig {
		/* Slots that contain the input resources (dirt, sand, ...) */
		public final int resourcesStart;
		public final int resourcesCount;
		/* Slots that contain the germlings (saplings, cactus blocks, ...) */
		public final int germlingsStart;
		public final int germlingsCount;
		/* Slots that contain the output of the farm / the products (wood, saplings, ...) */
		public final int productionStart;
		public final int productionCount;
		/* Slots that contain the fertilizer */
		public final int fertilizerStart;
		public final int fertilizerCount;
		/* Slots that contain the fluid containers for the water of the farm */
		public final int canStart;
		public final int canCount;
		/* Amount of slots in this config*/
		public final int count;

		public InventoryConfig(
			int resourcesStart, int resourcesCount,
			int germlingsStart, int germlingsCount,
			int productionStart, int productionCount,
			int fertilizerStart, int fertilizerCount,
			int canStart, int canCount
		) {
			this.resourcesStart = resourcesStart;
			this.resourcesCount = resourcesCount;
			this.germlingsStart = germlingsStart;
			this.germlingsCount = germlingsCount;
			this.productionStart = productionStart;
			this.productionCount = productionCount;
			this.fertilizerStart = fertilizerStart;
			this.fertilizerCount = fertilizerCount;
			this.canStart = canStart;
			this.canCount = canCount;
			this.count = resourcesCount + germlingsCount + productionCount + fertilizerCount + canCount;
		}
	}
}

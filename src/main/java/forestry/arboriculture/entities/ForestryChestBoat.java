package forestry.arboriculture.entities;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;

import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureItems;

public class ForestryChestBoat extends ForestryBoat implements HasCustomInventoryScreen, ContainerEntity {
	private static final int CONTAINER_SIZE = 27;

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	@Nullable
	private ResourceLocation lootTable;
	private long lootTableSeed;

	public ForestryChestBoat(EntityType<? extends Boat> type, Level level) {
		super(type, level);
	}

	public ForestryChestBoat(Level level, double x, double y, double z) {
		this(ArboricultureEntities.CHEST_BOAT.entityType(), level);

		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	public Item getDropItem() {
		return ArboricultureItems.CHEST_BOAT.item(getWoodType());
	}

	// <editor-fold desc="Vanilla copy from ChestBoat">
	@Override
	protected float getSinglePassengerXOffset() {
		return 0.15F;
	}

	@Override
	protected int getMaxPassengers() {
		return 1;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		addChestVehicleSaveData(pCompound);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		readChestVehicleSaveData(pCompound);
	}

	@Override
	public void destroy(DamageSource pDamageSource) {
		super.destroy(pDamageSource);
		chestVehicleDestroyed(pDamageSource, level(), this);
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		if (!level().isClientSide && reason.shouldDestroy()) {
			Containers.dropContents(level(), this, this);
		}

		super.remove(reason);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (canAddPassenger(player) && !player.isSecondaryUseActive()) {
			return super.interact(player, hand);
		} else {
			InteractionResult interactionresult = interactWithContainerVehicle(player);
			if (interactionresult.consumesAction()) {
				gameEvent(GameEvent.CONTAINER_OPEN, player);
				PiglinAi.angerNearbyPiglins(player, true);
			}

			return interactionresult;
		}
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		player.openMenu(this);
		if (!player.level().isClientSide) {
			this.gameEvent(GameEvent.CONTAINER_OPEN, player);
			PiglinAi.angerNearbyPiglins(player, true);
		}
	}

	@Override
	public void clearContent() {
		clearChestVehicleContent();
	}

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	public ItemStack getItem(int slot) {
		return getChestVehicleItem(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return removeChestVehicleItem(slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return removeChestVehicleItemNoUpdate(slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		setChestVehicleItem(slot, stack);
	}

	@Override
	public SlotAccess getSlot(int slot) {
		return getChestVehicleSlot(slot);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return isChestVehicleStillValid(player);
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
		if (this.lootTable != null && player.isSpectator()) {
			return null;
		} else {
			unpackChestVehicleLootTable(playerInv.player);
			return ChestMenu.threeRows(windowId, playerInv, this);
		}
	}

	@Nullable
	@Override
	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	@Override
	public void setLootTable(@Nullable ResourceLocation lootTable) {
		this.lootTable = lootTable;
	}

	@Override
	public long getLootTableSeed() {
		return this.lootTableSeed;
	}

	@Override
	public void setLootTableSeed(long lootTableSeed) {
		this.lootTableSeed = lootTableSeed;
	}

	@Override
	public NonNullList<ItemStack> getItemStacks() {
		return this.items;
	}

	@Override
	public void clearItemStacks() {
		this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	}
	// </editor-fold>

	// <editor-fold desc="Forge Start">
	private net.minecraftforge.common.util.LazyOptional<?> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER && isAlive()) {
			return this.itemHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.itemHandler.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.itemHandler = LazyOptional.of(() -> new InvWrapper(this));
	}

	public void stopOpen(Player player) {
		level().gameEvent(GameEvent.CONTAINER_CLOSE, position(), GameEvent.Context.of(player));
	}
	// </editor-fold>
}

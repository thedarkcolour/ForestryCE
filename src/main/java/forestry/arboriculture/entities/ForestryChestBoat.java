package forestry.arboriculture.entities;

import forestry.arboriculture.features.ArboricultureEntities;
import forestry.arboriculture.features.ArboricultureItems;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;

public class ForestryChestBoat extends ForestryBoat implements HasCustomInventoryScreen, ContainerEntity {
	private static final int CONTAINER_SIZE = 27;

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	@Nullable
	private ResourceKey<LootTable> lootTable;
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
		return 0.15f;
	}

	@Override
	protected int getMaxPassengers() {
		return 1;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		addChestVehicleSaveData(nbt, registryAccess());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		readChestVehicleSaveData(nbt, registryAccess());
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
		if (!player.isSecondaryUseActive()) {
			InteractionResult result = super.interact(player, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else {
			InteractionResult result = this.interactWithContainerVehicle(player);
			if (result.consumesAction()) {
				gameEvent(GameEvent.CONTAINER_OPEN, player);
				PiglinAi.angerNearbyPiglins(player, true);
			}

			return result;
		}
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		player.openMenu(this);
		if (!player.level().isClientSide) {
			gameEvent(GameEvent.CONTAINER_OPEN, player);
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
	public ResourceKey<LootTable> getLootTable() {
		return this.lootTable;
	}

	@Override
	public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
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

	@Override
	public void stopOpen(Player player) {
		level().gameEvent(GameEvent.CONTAINER_CLOSE, position(), GameEvent.Context.of(player));
	}
}

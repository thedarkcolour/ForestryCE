/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import forestry.api.apiculture.*;
import forestry.api.genetics.ForestryTaxa;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.core.ForestryError;
import forestry.api.core.IError;
import forestry.api.core.IErrorLogic;
import forestry.api.genetics.IEffectData;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.genetics.pollen.IPollen;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.core.config.Constants;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.SpeciesUtil;

public class BeekeepingLogic implements IBeekeepingLogic {
	private static final int totalBreedingTime = Constants.APIARY_BREEDING_TIME;

	private final IBeeHousing housing;
	private final IBeeModifier beeModifier;
	private final IBeeListener beeListener;

	private int beeProgress;
	private int beeProgressMax;

	private int workThrottleCounter;
	private int maxWorkThrottle = IBeekeepingLogic.DEFAULT_WORK_THROTTLE;
	private IEffectData[] effectData = new IEffectData[2];

	private final Stack<ItemStack> spawn = new Stack<>();

	private final HasFlowersCache hasFlowersCache = new HasFlowersCache();
	private final QueenCanWorkCache queenCanWorkCache = new QueenCanWorkCache();
	private final PollenHandler pollenHandler = new PollenHandler();

	// Client
	private boolean active;
	@Nullable
	private IBee queen;
	private ItemStack queenStack = ItemStack.EMPTY; // used to detect server changes and sync clientQueen

	public BeekeepingLogic(IBeeHousing housing) {
		this.housing = housing;
		this.beeModifier = IForestryApi.INSTANCE.getHiveManager().createBeeHousingModifier(housing);
		this.beeListener = IForestryApi.INSTANCE.getHiveManager().createBeeHousingListener(housing);
	}

	// / SAVING & LOADING
	@Override
	public CompoundTag write(CompoundTag compoundNBT) {
		compoundNBT.putInt("BreedingTime", this.beeProgress);
		compoundNBT.putInt("Throttle", this.workThrottleCounter);

		if (!this.queenStack.isEmpty()) {
			CompoundTag queenNbt = new CompoundTag();
			this.queenStack.save(queenNbt);
			compoundNBT.put("queen", queenNbt);
		}

		compoundNBT.putBoolean("Active", this.active);

		this.hasFlowersCache.write(compoundNBT);

		Stack<ItemStack> spawnCopy = new Stack<>();
		spawnCopy.addAll(this.spawn);
		ListTag nbttaglist = new ListTag();
		while (!spawnCopy.isEmpty()) {
			CompoundTag compoundNBT1 = new CompoundTag();
			spawnCopy.pop().save(compoundNBT1);
			nbttaglist.add(compoundNBT1);
		}
		compoundNBT.put("Offspring", nbttaglist);
		return compoundNBT;
	}

	@Override
	public void read(CompoundTag compoundNBT) {
		this.beeProgress = compoundNBT.getInt("BreedingTime");
		this.workThrottleCounter = compoundNBT.getInt("Throttle");

		// sadly this means duplicated NBT
		if (compoundNBT.contains("queen")) {
			CompoundTag queenNBT = compoundNBT.getCompound("queen");
			this.queenStack = ItemStack.of(queenNBT);
			this.queen = (IBee) IIndividualHandlerItem.getIndividual(this.queenStack);
			if (this.queen != null) {
				this.beeProgressMax = this.queen.getMaxHealth();
			}
		}

		setActive(compoundNBT.getBoolean("Active"));

		this.hasFlowersCache.read(compoundNBT);

		ListTag list = compoundNBT.getList("Offspring", 10);
		for (int i = 0; i < list.size(); i++) {
			this.spawn.add(ItemStack.of(list.getCompound(i)));
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		data.writeBoolean(this.active);
		if (this.active) {
			data.writeItem(this.queenStack);
			this.hasFlowersCache.writeData(data);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		boolean active = data.readBoolean();

		setActive(active);

		if (active) {
			this.queenStack = data.readItem();
			this.queen = (IBee) IIndividualHandlerItem.getIndividual(this.queenStack);
			this.hasFlowersCache.readData(data);
		}
	}

	/* Activatable */
	private void setActive(boolean active) {
		if (this.active == active) {
			return;
		}
		this.active = active;

		syncToClient();
	}

	/* UPDATING */
	@Override
	public boolean canWork() {
		IErrorLogic errorLogic = this.housing.getErrorLogic();
		errorLogic.clearErrors();

		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();

		boolean hasSpace = addPendingProducts(beeInventory, this.spawn);
		errorLogic.setCondition(!hasSpace, ForestryError.NO_SPACE_INVENTORY);

		ItemStack newQueenStack = beeInventory.getQueen();
		IIndividualHandlerItem handler = IIndividualHandlerItem.get(newQueenStack);
		ILifeStage beeType = handler == null ? null : handler.getStage();
		// check if we're breeding
		if (beeType == BeeLifeStage.PRINCESS) {
			boolean hasDrone = SpeciesUtil.BEE_TYPE.get().isDrone(beeInventory.getDrone());
			errorLogic.setCondition(!hasDrone, ForestryError.NO_DRONE);

			setActive(false); // not active (no bee FX) when we are breeding
			return !errorLogic.hasErrors();
		}
		if (beeType == BeeLifeStage.QUEEN) {
			IBee queen = (IBee) handler.getIndividual();

			if (!queen.isAlive()) {
				Collection<ItemStack> spawned = killQueen(queen, this.housing, this.beeListener);
				this.spawn.addAll(spawned);
				newQueenStack = ItemStack.EMPTY;
			}
		} else {
			newQueenStack = ItemStack.EMPTY;
		}

		// if the princess changed into a queen, or if a queen died
		if (this.queenStack != newQueenStack) {
			if (!newQueenStack.isEmpty()) {
				this.queen = (IBee) IIndividualHandlerItem.getIndividual(newQueenStack);
				if (this.queen != null) {
					this.hasFlowersCache.onNewQueen(this.queen, this.housing);
				}
			} else {
				this.queen = null;
			}
			this.queenStack = newQueenStack;
			this.queenCanWorkCache.clear();
		}

		if (errorLogic.setCondition(this.queen == null, ForestryError.NO_QUEEN)) {
			setActive(false);
			this.beeProgress = 0;
			return false;
		}

		Set<IError> queenErrors = this.queenCanWorkCache.queenCanWork(this.queen, this.housing);
		for (IError errorState : queenErrors) {
			errorLogic.setCondition(true, errorState);
		}

		// might be worth only running this check in absence of other errors
		this.hasFlowersCache.update(this.queen, this.housing);

		boolean hasFlowers = this.hasFlowersCache.hasFlowers();
		boolean flowerCacheNeedsSync = this.hasFlowersCache.needsSync();
		errorLogic.setCondition(!hasFlowers, ForestryError.NO_FLOWER);

		boolean canWork = !errorLogic.hasErrors();
		if (this.active != canWork) {
			setActive(canWork);
		} else if (flowerCacheNeedsSync) {
			syncToClient();
		}
		return canWork;
	}

	@Override
	public void doWork() {
		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();
		ItemStack queenStack = beeInventory.getQueen();
		IIndividualHandlerItem.ifPresent(queenStack, (individual, stage) -> {
			if (stage == BeeLifeStage.PRINCESS) {
				tickBreed();
			} else if (stage == BeeLifeStage.QUEEN) {
				queenWorkTick((IBee) individual, queenStack);
			}
		});
	}

	@Override
	public void clearCachedValues() {
		if (!this.housing.getWorldObj().isClientSide) {
			this.queenCanWorkCache.clear();
			canWork();
			if (this.queen != null) {
				this.hasFlowersCache.forceLookForFlowers(this.queen, this.housing);
			}
		}
	}

	private void queenWorkTick(@Nullable IBee queen, ItemStack queenStack) {
		if (queen == null) {
			this.beeProgress = 0;
			this.beeProgressMax = 0;
			return;
		}

		// Effects only fire when queen can work.
		this.effectData = queen.doEffect(this.effectData, this.housing);

		// Work cycles are throttled, rather than occurring every game tick.
		this.workThrottleCounter++;
		if (this.workThrottleCounter >= this.maxWorkThrottle) {
			this.workThrottleCounter = 0;

			doProduction(queen, this.housing, this.beeListener);
			Level world = this.housing.getWorldObj();
			List<BlockState> flowers = this.hasFlowersCache.getFlowers(world);
			if (flowers.size() < ModuleApiculture.maxFlowersSpawnedPerHive) {
				BlockPos blockPos = queen.plantFlowerRandom(this.housing, flowers);
				if (blockPos != null) {
					this.hasFlowersCache.addFlowerPos(blockPos);
				}
			}
			this.pollenHandler.doPollination(queen, this.housing, this.beeListener);

			// Age the queen
			IGenome mate = queen.getMate();
			float lifespanModifier = this.beeModifier.modifyAging(queen.getGenome(), mate, 1.0f);
			queen.age(world, lifespanModifier);

			// Write the changed queen back into the item stack.
			queen.saveToStack(queenStack);
			this.housing.getBeeInventory().setQueen(queenStack);
		}

		this.beeProgress = queen.getHealth();
		this.beeProgressMax = queen.getMaxHealth();
	}

	private static void doProduction(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener) {
		// Produce and add stacks
		List<ItemStack> products = queen.produceStacks(beeHousing);
		beeListener.wearOutEquipment(1);

		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		for (ItemStack stack : products) {
			beeInventory.addProduct(stack, false);
		}
	}

	private static boolean addPendingProducts(IBeeHousingInventory beeInventory, Stack<ItemStack> spawn) {
		boolean housingHasSpace = true;

		while (!spawn.isEmpty()) {
			ItemStack next = spawn.peek();
			if (beeInventory.addProduct(next, true)) {
				spawn.pop();
			} else {
				housingHasSpace = false;
				break;
			}
		}

		return housingHasSpace;
	}

	// / BREEDING
	private void tickBreed() {
		this.beeProgressMax = totalBreedingTime;

		IBeeHousingInventory beeInventory = this.housing.getBeeInventory();

		ItemStack droneStack = beeInventory.getDrone();
		ItemStack princessStack = beeInventory.getQueen();

		IIndividualHandlerItem droneType = IIndividualHandlerItem.get(droneStack);
		IIndividualHandlerItem princessType = IIndividualHandlerItem.get(princessStack);
		if (droneType == null || princessType == null || droneType.getStage() != BeeLifeStage.DRONE || princessType.getStage() != BeeLifeStage.PRINCESS) {
			this.beeProgress = 0;
			return;
		}

		if (this.beeProgress < totalBreedingTime) {
			this.beeProgress++;
		}
		if (this.beeProgress < totalBreedingTime) {
			return;
		}

		// Mate and replace princess with queen
		IBee princess = (IBee) IIndividualHandlerItem.getIndividual(princessStack);
		IBee drone = (IBee) IIndividualHandlerItem.getIndividual(droneStack);
		// If nether princess is outside nether it zombifies. Drones can breed in the short life they have so species may persist in secondary trait
		if(princess.getSpecies().getGenusName().equals(ForestryTaxa.GENUS_EMBITTERED) && housing.getWorldObj().dimension()!=Level.NETHER){
			princess = SpeciesUtil.getBeeSpecies(ForestryBeeSpecies.ZOMBIFIED).createIndividual();
		}
		princess.setMate(drone.getGenome());

		this.queenStack = princess.createStack(BeeLifeStage.QUEEN);
		beeInventory.setQueen(this.queenStack);

		// Register the new queen with the breeding tracker
		SpeciesUtil.BEE_TYPE.get().getBreedingTracker(this.housing.getWorldObj(), this.housing.getOwner()).registerQueen(princess);

		// Remove drone
		droneStack.shrink(1);
		if (droneStack.isEmpty()) {
			beeInventory.setDrone(ItemStack.EMPTY);
		}

		// Reset breeding time
		this.queen = princess;
		this.beeProgress = princess.getHealth();
		this.beeProgressMax = princess.getMaxHealth();
	}

	private static Collection<ItemStack> killQueen(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener) {
		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		Collection<ItemStack> spawn;

		if (queen.getMate() == null) {
			queen.setMate(queen.getGenome());
		}

		spawn = spawnOffspring(queen, beeHousing);
		beeListener.onQueenDeath();
		beeInventory.getQueen().setCount(0);
		beeInventory.setQueen(ItemStack.EMPTY);

		return spawn;
	}

	/**
	 * Creates the succeeding princess and between one and three drones.
	 */
	private static Collection<ItemStack> spawnOffspring(IBee queen, IBeeHousing beeHousing) {
		Level level = beeHousing.getWorldObj();
		Stack<ItemStack> offspring = new Stack<>();
		IApiaristTracker breedingTracker = SpeciesUtil.BEE_TYPE.get().getBreedingTracker(level, beeHousing.getOwner());

		// Princess
		boolean secondPrincess = level.random.nextInt(10000) < ModuleApiculture.getSecondPrincessChance() * 100;
		int count = secondPrincess ? 2 : 1;
		while (count > 0) {
			count--;
			IBee heiress = queen.spawnPrincess(beeHousing);
			if (heiress != null) {
				ItemStack princess = SpeciesUtil.BEE_TYPE.get().createStack(heiress, BeeLifeStage.PRINCESS);
				breedingTracker.registerPrincess(heiress);
				offspring.push(princess);
			}
		}

		// Drones
		List<IBee> drones = queen.spawnDrones(beeHousing);
		for (IBee drone : drones) {
			ItemStack droneStack = new ItemStack(BeeLifeStage.DRONE.getItemForm());
			drone.saveToStack(droneStack);
			breedingTracker.registerDrone(drone);
			offspring.push(droneStack);
		}

		IBeeHousingInventory beeInventory = beeHousing.getBeeInventory();

		Collection<ItemStack> spawn = new ArrayList<>();

		while (!offspring.isEmpty()) {
			ItemStack spawned = offspring.pop();
			if (!beeInventory.addProduct(spawned, true)) {
				spawn.add(spawned);
			}
		}

		return spawn;
	}

	/* CLIENT */
	@Override
	public void syncToClient() {
		Level level = this.housing.getWorldObj();
		if (level != null && !level.isClientSide) {
			NetworkUtil.sendNetworkPacket(new PacketBeeLogicActive(this.housing), this.housing.getCoordinates(), level);
		}
	}

	@Override
	public void syncToClient(ServerPlayer player) {
		Level level = this.housing.getWorldObj();
		if (level != null && !level.isClientSide) {
			NetworkUtil.sendToPlayer(new PacketBeeLogicActive(this.housing), player);
		}
	}

	@Override
	public int getBeeProgressPercent() {
		if (this.beeProgressMax == 0) {
			return 0;
		}

		return Math.round(this.beeProgress * 100f / this.beeProgressMax);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canDoBeeFX() {
		return !Minecraft.getInstance().isPaused() && this.active;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void doBeeFX() {
		if (this.queen != null) {
			this.queen.doFX(this.effectData, this.housing);
		}
	}

	@Override
	public List<BlockPos> getFlowerPositions() {
		return this.hasFlowersCache.getFlowerCoords();
	}

	@Override
	public void setWorkThrottle(int workThrottle) {
		this.maxWorkThrottle = workThrottle;
	}

	private static class QueenCanWorkCache {
		private static final int ticksPerCheckQueenCanWork = 10;

		private Set<IError> queenCanWorkCached = Collections.emptySet();
		private int queenCanWorkCooldown = 0;

		public Set<IError> queenCanWork(IBee queen, IBeeHousing beeHousing) {
			if (this.queenCanWorkCooldown <= 0) {
				this.queenCanWorkCached = queen.getCanWork(beeHousing);
				this.queenCanWorkCooldown = ticksPerCheckQueenCanWork;
			} else {
				this.queenCanWorkCooldown--;
			}

			return this.queenCanWorkCached;
		}

		public void clear() {
			this.queenCanWorkCached.clear();
			this.queenCanWorkCooldown = 0;
		}
	}

	// Not sure if this was intentional, but pollen isn't ever saved to NBT
	private static class PollenHandler {
		private static final int MAX_POLLINATION_ATTEMPTS = 20;

		@Nullable
		private IPollen<?> pollen;
		private int attemptedPollinations = 0;

		public void doPollination(IBee queen, IBeeHousing beeHousing, IBeeListener beeListener) {
			// Get pollen if none available yet
			if (this.pollen == null) {
				this.attemptedPollinations = 0;
				this.pollen = queen.retrievePollen(beeHousing);
				if (this.pollen != null) {
					if (beeListener.onPollenRetrieved(this.pollen)) {
						this.pollen = null;
					}
				}
			}

			if (this.pollen != null) {
				this.attemptedPollinations++;
				if (queen.pollinateRandom(beeHousing, this.pollen) || this.attemptedPollinations >= MAX_POLLINATION_ATTEMPTS) {
					this.pollen = null;
				}
			}
		}
	}
}

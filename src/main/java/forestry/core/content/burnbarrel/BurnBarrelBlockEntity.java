package forestry.core.content.burnbarrel;

import forestry.api.core.ForestryError;
import forestry.api.core.IErrorLogic;
import forestry.core.features.CoreItems;
import forestry.core.features.CoreTiles;
import forestry.core.platform.inventory.BurnBarrelInventory;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.tile.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class BurnBarrelBlockEntity extends BaseBlockEntity implements IStreamableGui {
	// How many ticks it takes for ash to be produced. 100 ticks means there is a 1:1 ratio between ash produced and
	// the number of items this barrel could have smelted
	public static final int ASH_PRODUCTION_TIME = 800;

	private int preheatTime = 0;

	// How long this should burn for, determined by the last accepted item's burn time
	private int currentMaxBurnTime = 0;
	// How long until the next item is burned
	private int burnTime = 0;

	// How many ticks until this produces ash
	private int ashProductionTimer = 0;

	private int errorTime = 0;

	public BurnBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(CoreTiles.BURN_BARREL.tileType(), pos, state);

		setInternalInventory(new BurnBarrelInventory(this));
	}

	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {
		int nextOutputSlotIndex = canOutput();
		boolean hasResources = false;

		BlockState newState = level.getBlockState(pos);
		boolean updateState = false;

		// If there is an item burning already, continue processing it
		if (this.burnTime > 0 && nextOutputSlotIndex > -1) {
			this.ashProductionTimer++;
			this.burnTime--;
			// We do this so it can keep looking for items to burn after burning one, since fuel is also ingredients
			this.preheatTime = 5;

			// If the production time has progressed fully, make some ash
			if (this.ashProductionTimer >= ASH_PRODUCTION_TIME) {
				ItemStack stack = getInternalInventory().getItem(nextOutputSlotIndex);
				// Update slots where ash already exists first
				if (stack.is(CoreItems.ASH.item()) && stack.getCount() < stack.getMaxStackSize()) {
					getInternalInventory().setItem(nextOutputSlotIndex, stack.copyWithCount(stack.getCount() + 1));
				} else {
					getInternalInventory().setItem(nextOutputSlotIndex, CoreItems.ASH.stack(1));
				}

				setChanged();
				newState = newState.setValue(BurnBarrelBlock.HAS_ASH, true);
				updateState = true;
				this.ashProductionTimer = 0;
			}
		}
		// If there is not an item processing, find one to process
		else if (this.preheatTime > 0 && nextOutputSlotIndex > -1) {
			int nextInputIndex = hasInput();
			if (nextInputIndex > -1) {
				hasResources = true;

				ItemStack stack = getInternalInventory().getItem(nextInputIndex);
				this.burnTime = stack.getBurnTime(null);
				this.currentMaxBurnTime = this.burnTime;
				getInternalInventory().setItem(nextInputIndex, stack.copyWithCount(stack.getCount() - 1));
			}
		}
		// No work can be done
		else {
			if (this.ashProductionTimer > 0) {
				this.ashProductionTimer--;
			}
		}

		// Decrease preheat time even if the barrel is burning
		if (this.preheatTime > 0) {
			this.preheatTime--;
			if (this.preheatTime <= 0) {
				newState = newState.setValue(BurnBarrelBlock.LIT, false);
				updateState = true;
				// We do this here so that it doesn't immediately revert every update.
				// This makes it maybe more useful in building/decoration, I suppose?
			}
		}

		boolean noSpaceInInventory = nextOutputSlotIndex == -1;
		boolean notLit = this.burnTime <= 0 || this.preheatTime <= 0;
		boolean noResources = !hasResources;
		// Seems superfluous but the inventory is not checked if the barrel is not burning, causing false positives
		if (noResources) {
			noResources = hasInput() == -1;
		}

		// This just stops errors flashing up every time a new item is processed
		if (noSpaceInInventory || notLit || noResources) {
			if (this.errorTime < 5) {
				this.errorTime++;
			}
		} else {
			this.errorTime = 0;
		}

		IErrorLogic errorLogic = getErrorLogic();
		errorLogic.setCondition(noSpaceInInventory && this.errorTime >= 5, ForestryError.NO_SPACE_INVENTORY);
		errorLogic.setCondition(notLit && this.errorTime >= 5, ForestryError.NOT_LIT);
		errorLogic.setCondition(noResources && this.errorTime >= 5, ForestryError.NO_RESOURCE);

		if (updateState) {
			level.setBlock(pos, newState, 3);
		}
	}

	public int hasInput() {
		// Loop backwards, consuming the last item first.
		// Deviation from 1.20.1: the bound was a bare SLOT_INPUT_COUNT - 1, which only lands on the last input slot
		// while SLOT_INPUT_1 is 0. Written the same way as canOutput below
		for (int i = BurnBarrelInventory.SLOT_INPUT_1 + BurnBarrelInventory.SLOT_INPUT_COUNT - 1; i >= BurnBarrelInventory.SLOT_INPUT_1; i--) {
			ItemStack stack = getInternalInventory().getItem(i);
			if (!stack.isEmpty() && stack.getBurnTime(null) > 0) {
				return i;
			}
		}
		return -1;
	}

	public int canOutput() {
		for (int i = BurnBarrelInventory.SLOT_OUTPUT_1; i < BurnBarrelInventory.SLOT_OUTPUT_1 + BurnBarrelInventory.SLOT_OUTPUT_COUNT; i++) {
			ItemStack stack = getInternalInventory().getItem(i);
			if (stack.isEmpty()) {
				return i;
			}
			// You never know what funny shenanigans people have going on.
			// Deliberately using our ash and not the tag
			else if (stack.is(CoreItems.ASH.item()) && stack.getCount() < stack.getMaxStackSize()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void openGui(ServerPlayer player, InteractionHand hand, BlockPos pos) {
		Level level = this.level;

		// Ignite the barrel, if it needs it
		if (this.preheatTime <= 0) {
			ItemStack heldItem = player.getItemInHand(hand);

			if (heldItem.is(Items.FIRE_CHARGE)) {
				if (!player.isCreative()) {
					heldItem.shrink(1);
				}
				this.preheatTime = 5;
				if (level instanceof ServerLevel serverLevel) {
					ignite(serverLevel, pos, SoundEvents.FIRECHARGE_USE);
				}
				return;
			} else if (heldItem.is(Items.FLINT_AND_STEEL)) {
				// Deviation from 1.20.1: broadcastBreakEvent is gone, the equipment slot now tells the game
				// which hand to animate
				heldItem.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
				this.preheatTime = 5;
				if (level instanceof ServerLevel serverLevel) {
					ignite(serverLevel, pos, SoundEvents.FLINTANDSTEEL_USE);
				}
				return;
			}
		}

		super.openGui(player, hand, pos);
	}

	// The fire charge and the flint and steel branches only differ in the sound they play
	private static void ignite(ServerLevel level, BlockPos pos, SoundEvent sound) {
		level.playSound(null, pos, sound, SoundSource.BLOCKS);
		level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5f, pos.getY() + 0.9f, pos.getZ() + 0.5f, 3, 0.25, 0.25, 0.25, 0.01);
		level.setBlock(pos, level.getBlockState(pos).setValue(BurnBarrelBlock.LIT, true), 3);
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		return new BurnBarrelMenu(windowId, player.getInventory(), this);
	}

	public int getAshProductionTimer() {
		return this.ashProductionTimer;
	}

	public int getBurnTime() {
		return this.burnTime;
	}

	public int getCurrentMaxBurnTime() {
		return this.currentMaxBurnTime;
	}

	public int getValueScaled(int progress, int maxAmount, int scaleAmount) {
		if (maxAmount == 0) {
			return 0;
		}
		return progress * scaleAmount / maxAmount;
	}

	/* LOADING & SAVING */
	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);

		compound.putInt("BurnTime", this.burnTime);
		compound.putInt("MaxBurnTime", this.currentMaxBurnTime);
		compound.putInt("AshTime", this.ashProductionTimer);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);

		this.burnTime = compound.getInt("BurnTime");
		this.currentMaxBurnTime = compound.getInt("MaxBurnTime");
		this.ashProductionTimer = compound.getInt("AshTime");
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
		data.writeVarInt(this.burnTime);
		data.writeVarInt(this.currentMaxBurnTime);
		data.writeVarInt(this.ashProductionTimer);
	}

	@Override
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
		this.burnTime = data.readVarInt();
		this.currentMaxBurnTime = data.readVarInt();
		this.ashProductionTimer = data.readVarInt();
	}

	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
		data.writeVarInt(this.burnTime);
		data.writeVarInt(this.currentMaxBurnTime);
		data.writeVarInt(this.ashProductionTimer);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
		this.burnTime = data.readVarInt();
		this.currentMaxBurnTime = data.readVarInt();
		this.ashProductionTimer = data.readVarInt();
	}
}

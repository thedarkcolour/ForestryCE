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
package forestry.apiculture.multiblock;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import forestry.api.IForestryApi;
import forestry.api.apiculture.genetics.BeeLifeStage;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.apiculture.blocks.BlockAlveary;
import forestry.apiculture.blocks.BlockAlvearyType;
import forestry.apiculture.gui.ContainerAlvearySwarmer;
import forestry.apiculture.hives.Hive;
import forestry.apiculture.hives.HiveDecorator;
import forestry.apiculture.hives.HiveDefinitionSwarmer;
import forestry.apiculture.inventory.InventorySwarmer;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.tiles.IActivatable;
import forestry.core.utils.SpeciesUtil;

public class TileAlvearySwarmer extends TileAlveary implements WorldlyContainer, IActivatable, IAlvearyComponent.Active<MultiblockLogicAlveary> {
	private final InventorySwarmer inventory;
	private final ArrayDeque<ItemStack> pendingSpawns = new ArrayDeque<>();

	public TileAlvearySwarmer(BlockPos pos, BlockState state) {
		super(BlockAlvearyType.SWARMER, pos, state);
		this.inventory = new InventorySwarmer(this);
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		return inventory;
	}

	@Override
	public boolean allowsAutomation() {
		return true;
	}

	/* UPDATING */
	@Override
	public void updateServer(int tickCount) {
		if (!pendingSpawns.isEmpty()) {
			setActive(true);
			if (tickCount % 300 == 0) {
				trySpawnSwarm();
			}
		} else {
			setActive(false);
		}

		if (tickCount % 500 != 0) {
			return;
		}

		ItemStack princessStack = getPrincessStack();
		if (princessStack == null) {
			return;
		}

		float chance = consumeInducerAndGetChance();
		if (chance == 0) {
			return;
		}

		// Try to spawn princess
		if (level.random.nextFloat() < chance) {
			// Queue swarm spawn
			IIndividualHandlerItem.ifPresent(princessStack, individual -> {
				if (individual instanceof IBee princess) {
					// setting pristine for the new copy is a pain in the ass so do this instead
					princess.setPristine(false);
					this.pendingSpawns.push(princess.createStack(BeeLifeStage.PRINCESS));
					princess.setPristine(true);
				}
			});
		}
	}

	@Override
	public void updateClient(int tickCount) {
	}

	@Nullable
	private ItemStack getPrincessStack() {
		ItemStack princessStack = getMultiblockLogic().getController().getBeeInventory().getQueen();

		if (SpeciesUtil.BEE_TYPE.get().isMated(princessStack)) {
			return princessStack;
		}

		return null;
	}

	private float consumeInducerAndGetChance() {
		for (int slotIndex = 0; slotIndex < getContainerSize(); slotIndex++) {
			ItemStack stack = getItem(slotIndex);
			float chance = IForestryApi.INSTANCE.getHiveManager().getSwarmingMaterialChance(stack.getItem());
			if (chance != 0.0f) {
				removeItem(slotIndex, 1);
				return chance;
			}
		}

		return 0f;
	}

	private void trySpawnSwarm() {
		ItemStack toSpawn = pendingSpawns.peek();
		HiveDefinitionSwarmer hiveDescription = new HiveDefinitionSwarmer(toSpawn);
		Hive hive = new Hive(hiveDescription, hiveDescription.getGenChance(), List.of());

		ServerLevel level = (ServerLevel) this.level;

		int x = getBlockPos().getX() + level.random.nextInt(40 * 2) - 40;
		int z = getBlockPos().getZ() + level.random.nextInt(40 * 2) - 40;

		if (HiveDecorator.tryGenHive(level, level.random, x, z, hive)) {
			pendingSpawns.pop();
		}
	}

	/* SAVING & LOADING */
	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);

		ListTag nbttaglist = compoundNBT.getList("PendingSpawns", 10);
		for (int i = 0; i < nbttaglist.size(); i++) {
			CompoundTag compoundNBT1 = nbttaglist.getCompound(i);
			pendingSpawns.add(ItemStack.of(compoundNBT1));
		}
	}

	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);

		ListTag nbttaglist = new ListTag();
		ItemStack[] offspring = pendingSpawns.toArray(new ItemStack[0]);
		for (int i = 0; i < offspring.length; i++) {
			if (offspring[i] != null) {
				CompoundTag compoundNBT1 = new CompoundTag();
				compoundNBT1.putByte("Slot", (byte) i);
				offspring[i].save(compoundNBT1);
				nbttaglist.add(compoundNBT1);
			}
		}
		compoundNBT.put("PendingSpawns", nbttaglist);
	}

	@Override
	public boolean isActive() {
		return getBlockState().getValue(BlockAlveary.STATE) == BlockAlveary.State.ON;
	}

	@Override
	public void setActive(boolean active) {
		if (isActive() != active) {
			this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockAlveary.STATE, active ? BlockAlveary.State.ON : BlockAlveary.State.OFF));
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAlvearySwarmer(windowId, inv, this);
	}
}

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
package forestry.mail;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.mail.carriers.PostalCarriers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.mail.ILetter;
import forestry.api.mail.IMailAddress;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.utils.InventoryUtil;

import java.util.HashSet;
import java.util.Set;

public class POBox implements Container, IWatchable, INbtReadable, INbtWritable {

	public static final short SLOT_SIZE = 84;

	@Nullable
	private IMailAddress address;
	private final InventoryAdapter letters = new InventoryAdapter(SLOT_SIZE, "Letters").disableAutomation();

	private final Set<Watcher> updateWatchers = new HashSet<>();

	public POBox(IMailAddress address) {
		if (!address.getCarrier().equals(PostalCarriers.PLAYER.get())) {
			throw new IllegalArgumentException("POBox address must be a player");
		}

		this.address = address;
	}

	public POBox(CompoundTag tag) {
		read(tag);
	}

	public void read(CompoundTag tag) {
		if (tag.contains("address")) {
			this.address = new MailAddress(tag.getCompound("address"));
		}

		letters.read(tag);
	}

	public CompoundTag write(CompoundTag compoundNBT) {
		if (this.address != null) {
			CompoundTag nbt = new CompoundTag();
			this.address.write(nbt);
			compoundNBT.put("address", nbt);
		}
		letters.write(compoundNBT);
		return compoundNBT;
	}

	public boolean storeLetter(ItemStack letterstack) {
		ILetter letter = LetterUtils.getLetter(letterstack);
		Preconditions.checkNotNull(letter, "Letter stack must be a valid letter");

		// Mark letter as processed
		letter.setProcessed(true);
		letter.invalidatePostage();
		CompoundTag compoundNBT = new CompoundTag();
		letter.write(compoundNBT);
		letterstack.setTag(compoundNBT);

		this.setDirty();

		return InventoryUtil.tryAddStack(letters, letterstack, true);
	}

	public POBoxInfo getPOBoxInfo() {
		int playerLetters = 0;
		int tradeLetters = 0;
		for (int i = 0; i < letters.getContainerSize(); i++) {
			if (letters.getItem(i).isEmpty()) {
				continue;
			}
			CompoundTag tagCompound = letters.getItem(i).getTag();
			if (tagCompound != null) {
				ILetter letter = new Letter(tagCompound);
				if (letter.getSender().getCarrier().equals(PostalCarriers.PLAYER.get())) {
					playerLetters++;
				} else {
					tradeLetters++;
				}
			}
		}

		return new POBoxInfo(playerLetters, tradeLetters);
	}

	/* IINVENTORY */

	@Override
	public boolean isEmpty() {
		return letters.isEmpty();
	}

	@Override
	public void setDirty() {
		updateWatchers.forEach(Watcher::onWatchableUpdate);
		letters.setChanged();
	}

	@Override
	public boolean registerUpdateWatcher(Watcher updateWatcher) {
		return updateWatchers.add(updateWatcher);
	}

	@Override
	public boolean unregisterUpdateWatcher(Watcher updateWatcher) {
		return updateWatchers.remove(updateWatcher);
	}

	@Override
	public void setItem(int var1, ItemStack var2) {
		this.setDirty();
		letters.setItem(var1, var2);
	}

	@Override
	public int getContainerSize() {
		return letters.getContainerSize();
	}

	@Override
	public ItemStack getItem(int var1) {
		return letters.getItem(var1);
	}

	@Override
	public ItemStack removeItem(int var1, int var2) {
		return letters.removeItem(var1, var2);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return letters.removeItemNoUpdate(index);
	}

	//	@Override
	//	public String getName() {
	//		return letters.getName();
	//	}

	@Override
	public int getMaxStackSize() {
		return letters.getMaxStackSize();
	}

	@Override
	public void setChanged() {

	}

	@Override
	public boolean stillValid(Player var1) {
		return letters.stillValid(var1);
	}

	@Override
	public void startOpen(Player var1) {
	}

	@Override
	public void stopOpen(Player var1) {
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemstack) {
		return letters.canPlaceItem(i, itemstack);
	}

	@Override
	public void clearContent() {
	}

}

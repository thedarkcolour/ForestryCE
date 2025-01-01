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
package forestry.mail.tiles;

import com.google.common.base.Preconditions;

import forestry.mail.carriers.trading.TradeStationRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.core.IErrorLogic;
import forestry.api.mail.IMailAddress;
import forestry.api.mail.IStamps;
import forestry.api.core.ForestryError;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.owner.IOwnedTile;
import forestry.core.owner.IOwnerHandler;
import forestry.core.owner.OwnerHandler;
import forestry.core.tiles.TileBase;
import forestry.core.utils.ItemStackUtil;
import forestry.core.utils.NetworkUtil;
import forestry.mail.MailAddress;
import forestry.mail.carriers.trading.TradeStation;
import forestry.mail.features.MailTiles;
import forestry.mail.gui.ContainerTradeName;
import forestry.mail.gui.ContainerTrader;
import forestry.mail.inventory.InventoryTradeStation;
import forestry.mail.network.packets.PacketTraderAddressResponse;

public class TileTrader extends TileBase implements IOwnedTile {
	private final OwnerHandler ownerHandler = new OwnerHandler();
	private IMailAddress address;

	public TileTrader(BlockPos pos, BlockState state) {
		super(MailTiles.TRADER.tileType(), pos, state);
		address = MailAddress.INVALID;
		setInternalInventory(new InventoryTradeStation());
	}

	@Override
	public IOwnerHandler getOwnerHandler() {
		return ownerHandler;
	}

	@Override
	public void onDropContents(ServerLevel level) {
		if (isLinked()) {
			TradeStationRegistry.getOrCreate((ServerLevel) this.level).deleteTradeStation(address);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);

		CompoundTag nbt = new CompoundTag();
		address.write(nbt);
		compoundNBT.put("address", nbt);

		ownerHandler.write(compoundNBT);
	}

	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);

		if (compoundNBT.contains("address")) {
			address = new MailAddress(compoundNBT.getCompound("address"));
		}
		ownerHandler.read(compoundNBT);
	}

	/* NETWORK */

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		ownerHandler.writeData(data);
		String addressName = address.getName();
		data.writeUtf(addressName);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
		ownerHandler.readData(data);
		String addressName = data.readUtf();
		if (!addressName.isEmpty()) {
			address = new MailAddress(addressName);
		}
	}

	/* UPDATING */

	/**
	 * The trade station should show errors for missing stamps and paper first.
	 * Once it is able to send letters, it should display other error states.
	 */
	@Override
	public void serverTick(Level level, BlockPos pos, BlockState state) {

		if (!isLinked() || !updateOnInterval(10)) {
			return;
		}

		IErrorLogic errorLogic = getErrorLogic();

		errorLogic.setCondition(!hasPostageMin(3), ForestryError.NO_STAMPS);
		errorLogic.setCondition(!hasPaperMin(2), ForestryError.NO_PAPER);

		Container inventory = getInternalInventory();
		ItemStack tradeGood = inventory.getItem(TradeStation.SLOT_TRADEGOOD);
		errorLogic.setCondition(tradeGood.isEmpty(), ForestryError.NO_TRADE);

		boolean hasRequest = hasItemCount(TradeStation.SLOT_EXCHANGE_1, TradeStation.SLOT_EXCHANGE_COUNT, ItemStack.EMPTY, 1);
		errorLogic.setCondition(!hasRequest, ForestryError.NO_TRADE);

		if (!tradeGood.isEmpty()) {
			boolean hasSupplies = hasItemCount(TradeStation.SLOT_SEND_BUFFER, TradeStation.SLOT_SEND_BUFFER_COUNT, tradeGood, tradeGood.getCount());
			errorLogic.setCondition(!hasSupplies, ForestryError.NO_SUPPLIES);
		}

		if (inventory instanceof TradeStation && updateOnInterval(200)) {
			boolean canReceivePayment = ((TradeStation) inventory).canReceivePayment();
			errorLogic.setCondition(!canReceivePayment, ForestryError.NO_SPACE_INVENTORY);
		}
	}

	/* STATE INFORMATION */
	public boolean isLinked() {
		if (!address.isValid()) {
			return false;
		}

		IErrorLogic errorLogic = getErrorLogic();

		return !errorLogic.contains(ForestryError.NOT_ALPHANUMERIC) && !errorLogic.contains(ForestryError.NOT_UNIQUE);
	}

	/**
	 * Returns true if there are 'itemCount' of 'item' in the inventory
	 * wildcard when item == null, counts all types of items
	 */
	private boolean hasItemCount(int startSlot, int countSlots, ItemStack item, int itemCount) {
		int count = 0;

		Container tradeInventory = this.getInternalInventory();
		for (int i = startSlot; i < startSlot + countSlots; i++) {
			ItemStack itemInSlot = tradeInventory.getItem(i);
			if (itemInSlot.isEmpty()) {
				continue;
			}
			if (item.isEmpty() || ItemStackUtil.isIdenticalItem(itemInSlot, item)) {
				count += itemInSlot.getCount();
			}
			if (count >= itemCount) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the percentage of the inventory that is occupied by 'item'
	 * if item == null, returns the percentage occupied by all kinds of items
	 */
	private float percentOccupied(int startSlot, int countSlots, ItemStack item) {
		int count = 0;
		int total = 0;

		Container tradeInventory = this.getInternalInventory();
		for (int i = startSlot; i < startSlot + countSlots; i++) {
			ItemStack itemInSlot = tradeInventory.getItem(i);
			if (itemInSlot.isEmpty()) {
				total += tradeInventory.getMaxStackSize();
			} else {
				total += itemInSlot.getMaxStackSize();
				if (item.isEmpty() || ItemStackUtil.isIdenticalItem(itemInSlot, item)) {
					count += itemInSlot.getCount();
				}
			}
		}

		return (float) count / (float) total;
	}

	public boolean hasPaperMin(int count) {
		return hasItemCount(TradeStation.SLOT_LETTERS_1, TradeStation.SLOT_LETTERS_COUNT, new ItemStack(Items.PAPER), count);
	}

	//	public boolean hasInputBufMin(float percentage) {
	//		IInventory inventory = getInternalInventory();
	//		ItemStack tradeGood = inventory.getStackInSlot(TradeStation.SLOT_TRADEGOOD);
	//		if (tradeGood.isEmpty()) {
	//			return true;
	//		}
	//		return percentOccupied(TradeStation.SLOT_SEND_BUFFER, TradeStation.SLOT_SEND_BUFFER_COUNT, tradeGood) > percentage;
	//	}

	//	public boolean hasOutputBufMin(float percentage) {
	//		return percentOccupied(TradeStation.SLOT_RECEIVE_BUFFER, TradeStation.SLOT_RECEIVE_BUFFER_COUNT, ItemStack.EMPTY) > percentage;
	//	}

	public boolean hasPostageMin(int postage) {

		int posted = 0;

		Container tradeInventory = this.getInternalInventory();
		for (int i = TradeStation.SLOT_STAMPS_1; i < TradeStation.SLOT_STAMPS_1 + TradeStation.SLOT_STAMPS_COUNT; i++) {
			ItemStack stamp = tradeInventory.getItem(i);
			if (!stamp.isEmpty()) {
				if (stamp.getItem() instanceof IStamps) {
					posted += ((IStamps) stamp.getItem()).getPostage(stamp).getValue() * stamp.getCount();
					if (posted >= postage) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/* ADDRESS */
	public IMailAddress getAddress() {
		return address;
	}

	public void handleSetAddressRequest(String addressName) {
		IMailAddress address = new MailAddress(addressName);
		boolean updated = setAddress(address);

		if (updated) {
			PacketTraderAddressResponse packetResponse = new PacketTraderAddressResponse(worldPosition, address);
			NetworkUtil.sendNetworkPacket(packetResponse, worldPosition, level);
		}
	}

	public boolean setAddress(IMailAddress address) {
		Preconditions.checkNotNull(address, "address must not be null");

		if (this.address.isValid() && this.address.equals(address)) {
			return false;
		}

		if (!level.isClientSide) {
			ServerLevel world = (ServerLevel) this.level;
			IErrorLogic errorLogic = getErrorLogic();

			TradeStationRegistry tradeStationRegistry = TradeStationRegistry.getOrCreate(world);

			boolean hasValidTradeAddress = tradeStationRegistry.isValidTradeAddress(address);
			errorLogic.setCondition(!hasValidTradeAddress, ForestryError.NOT_ALPHANUMERIC);

			boolean hasUniqueTradeAddress = tradeStationRegistry.isAvailableTradeAddress(address);
			errorLogic.setCondition(!hasUniqueTradeAddress, ForestryError.NOT_UNIQUE);

			if (hasValidTradeAddress & hasUniqueTradeAddress) {
				this.address = address;
				tradeStationRegistry.getOrCreateTradeStation(getOwnerHandler().getOwner(), address);
				return true;
			}
		} else {
			this.address = address;
			return true;
		}
		return false;
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		// Handle client side
		if (level.isClientSide || !address.isValid()) {
			return super.getInternalInventory();
		}

		return TradeStationRegistry.getOrCreate((ServerLevel) level).getOrCreateTradeStation(getOwnerHandler().getOwner(), address);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		if (isLinked()) {    //TODO does this sync over?
			return new ContainerTrader(windowId, inv, this);
		} else {
			return new ContainerTradeName(windowId, inv, this);
		}
	}
}

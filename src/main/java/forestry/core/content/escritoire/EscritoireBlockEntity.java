package forestry.core.content.escritoire;

import com.mojang.authlib.GameProfile;
import forestry.api.core.genetics.IIndividual;
import forestry.api.core.genetics.ISpecies;
import forestry.api.core.genetics.ISpeciesType;
import forestry.api.core.genetics.capability.IIndividualHandlerItem;
import forestry.core.features.CoreTiles;
import forestry.core.platform.inventory.AnalyzerInventory;
import forestry.core.platform.inventory.EscritoireInventory;
import forestry.core.platform.inventory.watchers.ISlotPickupWatcher;
import forestry.core.platform.network.IStreamableGui;
import forestry.core.platform.network.packets.PacketItemStackDisplay;
import forestry.core.platform.util.InventoryUtil;
import forestry.core.platform.util.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import forestry.core.platform.tile.IItemStackDisplay;
import forestry.core.platform.tile.BaseBlockEntity;

public class EscritoireBlockEntity extends BaseBlockEntity implements WorldlyContainer, ISlotPickupWatcher, IStreamableGui, IItemStackDisplay {
	private final EscritoireGame game = new EscritoireGame();
	private ItemStack individualOnDisplayClient = ItemStack.EMPTY;

	public EscritoireBlockEntity(BlockPos pos, BlockState state) {
		super(CoreTiles.ESCRITOIRE.tileType(), pos, state);
		setInternalInventory(new EscritoireInventory(this));
	}

	/* SAVING & LOADING */
	@Override
	public void loadAdditional(CompoundTag compoundNBT, HolderLookup.Provider registries) {
		super.loadAdditional(compoundNBT, registries);
        this.game.read(compoundNBT, registries);
	}


	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
        this.game.write(nbt, registries);
	}

	/* GAME */
	public EscritoireGame getGame() {
		return this.game;
	}

	public void choose(GameProfile gameProfile, int index) {
        this.game.choose(index);
		processTurnResult(gameProfile);
	}

	private void processTurnResult(GameProfile gameProfile) {
		if (getGame().getStatus() != EscritoireGame.Status.SUCCESS) {
			return;
		}

		IIndividual individual = IIndividualHandlerItem.getIndividual(getItem(EscritoireInventory.SLOT_ANALYZE));
		if (individual == null) {
			return;
		}

		ISpecies<?> species = individual.getSpecies();
		ISpeciesType<?, ?> root = species.getType();
		for (ItemStack itemstack : root.getResearchBounty(species.cast(), this.level, gameProfile, individual.cast(), this.game.getBountyLevel())) {
			InventoryUtil.addStack(getInternalInventory(), itemstack, EscritoireInventory.SLOT_RESULTS_1, EscritoireInventory.SLOTS_RESULTS_COUNT, true);
		}
	}

	private boolean areProbeSlotsFilled() {
		int filledSlots = 0;
		int required = this.game.getSampleSize(EscritoireInventory.SLOTS_INPUT_COUNT);
		for (int i = EscritoireInventory.SLOT_INPUT_1; i < EscritoireInventory.SLOT_INPUT_1 + required; i++) {
			if (!getItem(i).isEmpty()) {
				filledSlots++;
			}
		}

		return filledSlots >= required;
	}

	public void probe() {
		if (this.level.isClientSide) {
			return;
		}

		ItemStack analyze = getItem(EscritoireInventory.SLOT_ANALYZE);

		if (!analyze.isEmpty() && areProbeSlotsFilled()) {
            this.game.probe(analyze, this, EscritoireInventory.SLOT_INPUT_1, EscritoireInventory.SLOTS_INPUT_COUNT);
		}
	}

	/* NETWORK */
	@Override
	public void writeGuiData(RegistryFriendlyByteBuf data) {
        this.game.writeData(data);
	}

	@Override
	public void readGuiData(RegistryFriendlyByteBuf data) {
        this.game.readData(data);
	}

	@Override
	public void writeData(RegistryFriendlyByteBuf data) {
		super.writeData(data);
		ItemStack displayStack = getIndividualOnDisplay();
		ItemStack.OPTIONAL_STREAM_CODEC.encode(data, displayStack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(RegistryFriendlyByteBuf data) {
		super.readData(data);
        this.individualOnDisplayClient = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
	}

	/* ISlotPickupWatcher */
	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == EscritoireInventory.SLOT_ANALYZE) {
            this.game.reset();
			PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
			NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);
		}
	}

	@Override
	public void setItem(int slotIndex, ItemStack itemstack) {
		super.setItem(slotIndex, itemstack);
		if (slotIndex == EscritoireInventory.SLOT_ANALYZE) {
			if (this.level != null && !this.level.isClientSide) {
				PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
				NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);
			}
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new EscritoireMenu(windowId, player.getInventory(), this);
	}

	@Override
	public void handleItemStackForDisplay(ItemStack itemStack) {
		if (!ItemStack.matches(itemStack, this.individualOnDisplayClient)) {
            this.individualOnDisplayClient = itemStack;
		}
	}

	public ItemStack getIndividualOnDisplay() {
		if (this.level == null || this.level.isClientSide) {
			return this.individualOnDisplayClient;
		}
		return getItem(AnalyzerInventory.SLOT_ANALYZE);
	}
}

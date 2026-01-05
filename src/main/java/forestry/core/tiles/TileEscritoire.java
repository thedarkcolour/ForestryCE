package forestry.core.tiles;

import com.mojang.authlib.GameProfile;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import forestry.core.features.CoreTiles;
import forestry.core.gui.ContainerEscritoire;
import forestry.core.inventory.InventoryAnalyzer;
import forestry.core.inventory.InventoryEscritoire;
import forestry.core.inventory.watchers.ISlotPickupWatcher;
import forestry.core.network.IStreamableGui;
import forestry.core.network.packets.PacketItemStackDisplay;
import forestry.core.utils.InventoryUtil;
import forestry.core.utils.NetworkUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEscritoire extends TileBase implements WorldlyContainer, ISlotPickupWatcher, IStreamableGui, IItemStackDisplay {
	private final EscritoireGame game = new EscritoireGame();
	private ItemStack individualOnDisplayClient = ItemStack.EMPTY;

	public TileEscritoire(BlockPos pos, BlockState state) {
		super(CoreTiles.ESCRITOIRE.tileType(), pos, state);
		setInternalInventory(new InventoryEscritoire(this));
	}

	/* SAVING & LOADING */
	@Override
	public void load(CompoundTag compoundNBT) {
		super.load(compoundNBT);
        this.game.read(compoundNBT);
	}


	@Override
	public void saveAdditional(CompoundTag compoundNBT) {
		super.saveAdditional(compoundNBT);
        this.game.write(compoundNBT);
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

		IIndividual individual = IIndividualHandlerItem.getIndividual(getItem(InventoryEscritoire.SLOT_ANALYZE));
		if (individual == null) {
			return;
		}

		ISpecies<?> species = individual.getSpecies();
		ISpeciesType<?, ?> root = species.getType();
		for (ItemStack itemstack : root.getResearchBounty(species.cast(), this.level, gameProfile, individual.cast(), this.game.getBountyLevel())) {
			InventoryUtil.addStack(getInternalInventory(), itemstack, InventoryEscritoire.SLOT_RESULTS_1, InventoryEscritoire.SLOTS_RESULTS_COUNT, true);
		}
	}

	private boolean areProbeSlotsFilled() {
		int filledSlots = 0;
		int required = this.game.getSampleSize(InventoryEscritoire.SLOTS_INPUT_COUNT);
		for (int i = InventoryEscritoire.SLOT_INPUT_1; i < InventoryEscritoire.SLOT_INPUT_1 + required; i++) {
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

		ItemStack analyze = getItem(InventoryEscritoire.SLOT_ANALYZE);

		if (!analyze.isEmpty() && areProbeSlotsFilled()) {
            this.game.probe(analyze, this, InventoryEscritoire.SLOT_INPUT_1, InventoryEscritoire.SLOTS_INPUT_COUNT);
		}
	}

	/* NETWORK */
	@Override
	public void writeGuiData(FriendlyByteBuf data) {
        this.game.writeData(data);
	}

	@Override
	public void readGuiData(FriendlyByteBuf data) {
        this.game.readData(data);
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		super.writeData(data);
		ItemStack displayStack = getIndividualOnDisplay();
		data.writeItem(displayStack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void readData(FriendlyByteBuf data) {
		super.readData(data);
        this.individualOnDisplayClient = data.readItem();
	}

	/* ISlotPickupWatcher */
	@Override
	public void onTake(int slotIndex, Player player) {
		if (slotIndex == InventoryEscritoire.SLOT_ANALYZE) {
            this.game.reset();
			PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
			NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);
		}
	}

	@Override
	public void setItem(int slotIndex, ItemStack itemstack) {
		super.setItem(slotIndex, itemstack);
		if (slotIndex == InventoryEscritoire.SLOT_ANALYZE) {
			if (this.level != null && !this.level.isClientSide) {
				PacketItemStackDisplay packet = new PacketItemStackDisplay(this, getIndividualOnDisplay());
				NetworkUtil.sendNetworkPacket(packet, this.worldPosition, this.level);
			}
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerEscritoire(windowId, player.getInventory(), this);
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
		return getItem(InventoryAnalyzer.SLOT_ANALYZE);
	}
}

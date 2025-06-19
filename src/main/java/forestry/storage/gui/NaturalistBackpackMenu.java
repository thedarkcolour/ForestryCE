package forestry.storage.gui;

import forestry.api.IForestryApi;
import forestry.api.genetics.ISpeciesType;
import forestry.core.gui.ItemInventoryMenu;
import forestry.core.gui.NaturalistInventoryMenu;
import forestry.core.gui.IGuiSelectable;
import forestry.core.gui.INaturalistMenu;
import forestry.storage.features.BackpackMenuTypes;
import forestry.storage.inventory.ItemInventoryBackpackPaged;
import forestry.storage.items.ItemBackpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class NaturalistBackpackMenu extends ItemInventoryMenu<ItemInventoryBackpackPaged> implements IGuiSelectable, INaturalistMenu {
	private final int currentPage;
	private final ISpeciesType<?, ?> speciesRoot;

	public NaturalistBackpackMenu(int windowId, Inventory inv, ItemInventoryBackpackPaged inventory, int selectedPage, ResourceLocation rootUid) {
		super(BackpackMenuTypes.NATURALIST_BACKPACK.menuType(), windowId, inventory, inv, 18, 120);

		NaturalistInventoryMenu.addInventory(this, inventory, selectedPage);

		this.currentPage = selectedPage;
		this.speciesRoot = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(rootUid);
	}

	public static NaturalistBackpackMenu makeContainer(int windowId, Inventory playerInv, int slotIndex, int page, ResourceLocation typeId) {
		ItemInventoryBackpackPaged inventory = new ItemInventoryBackpackPaged(ItemBackpack.SLOTS_BACKPACK_APIARIST, playerInv.getItem(slotIndex), typeId);
		return new NaturalistBackpackMenu(windowId, playerInv, inventory, page, typeId);
	}

	@Override
	public void handleSelectionRequest(Player player, int primary, int secondary) {
        this.inventory.flipPage(player, (short) primary);
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType() {
		return this.speciesRoot;
	}

	@Override
	public int getCurrentPage() {
		return this.currentPage;
	}

	public static NaturalistBackpackMenu fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
		int slotIndex = buffer.readByte();
		int page = buffer.readByte();
		ResourceLocation typeId = buffer.readResourceLocation();

		return makeContainer(windowId, playerInv, slotIndex, page, typeId);
	}
}

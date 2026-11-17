package forestry.core.content.backpacks.gui;

import forestry.api.IForestryApi;
import forestry.api.core.genetics.ISpeciesType;
import forestry.core.content.backpacks.features.BackpackMenuTypes;
import forestry.core.content.backpacks.inventory.BackpackInventory;
import forestry.core.content.backpacks.items.BackpackItem;
import forestry.core.platform.gui.ItemInventoryMenu;
import forestry.core.platform.gui.NaturalistInventoryMenu;
import forestry.core.platform.gui.IGuiSelectable;
import forestry.core.platform.gui.INaturalistMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class NaturalistBackpackMenu extends ItemInventoryMenu<BackpackInventory> implements IGuiSelectable, INaturalistMenu {
	private final SimpleContainerData scrollData = new SimpleContainerData(1);
	private final ISpeciesType<?, ?> speciesRoot;

	public NaturalistBackpackMenu(int windowId, Inventory inv, BackpackInventory inventory, ResourceLocation rootUid) {
		super(windowId, inventory, inv, 7, 107, BackpackMenuTypes.NATURALIST_BACKPACK.menuType());

		addDataSlots(this.scrollData);
		NaturalistInventoryMenu.addScrollableInventory(this, inventory, this.scrollData);

		this.speciesRoot = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(rootUid);
	}

	public static NaturalistBackpackMenu makeContainer(int windowId, Player player, ItemStack heldItem, ResourceLocation typeId) {
		BackpackInventory inventory = new BackpackInventory(player, BackpackItem.SLOTS_BACKPACK_APIARIST, heldItem);
		return new NaturalistBackpackMenu(windowId, player.getInventory(), inventory, typeId);
	}

	@Override
	public void handleSelectionRequest(ServerPlayer player, int primary, int secondary) {
		setScrollRow(primary);
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType() {
		return this.speciesRoot;
	}

	@Override
	public int getScrollRow() {
		return this.scrollData.get(0);
	}

	@Override
	public void setScrollRow(int row) {
		this.scrollData.set(0, Mth.clamp(row, 0, NaturalistInventoryMenu.MAX_SCROLL));
	}

	public static NaturalistBackpackMenu fromNetwork(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		ResourceLocation typeId = buffer.readResourceLocation();
		ItemStack parent = playerInventory.getSelected();

		return makeContainer(windowId, playerInventory.player, parent, typeId);
	}
}

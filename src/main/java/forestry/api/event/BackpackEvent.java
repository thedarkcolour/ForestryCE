package forestry.api.event;

import forestry.api.storage.IBackpackDefinition;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class BackpackEvent extends Event {
	public final Player player;
	public final IBackpackDefinition backpackDefinition;
	public final Container backpackInventory;

	public BackpackEvent(Player player, IBackpackDefinition backpackDefinition, Container backpackInventory) {
		this.player = player;
		this.backpackDefinition = backpackDefinition;
		this.backpackInventory = backpackInventory;
	}

	/**
	 * Will fire whenever a backpack tries to store an item. Processing will stop if the stack size
	 * of stackToStow drops to 0 or less or the event is canceled.
	 */
	public static class Stow extends BackpackEvent implements ICancellableEvent {
		public final ItemStack stackToStow;

		public Stow(Player player, IBackpackDefinition backpackDefinition, Container backpackInventory, ItemStack stackToStow) {
			super(player, backpackDefinition, backpackInventory);

			this.stackToStow = stackToStow;
		}
	}

	/**
	 * Use @SubscribeEvent on a method taking this event as an argument. Will fire whenever a backpack tries to resupply to a player inventory. Processing will stop
	 * if the event is canceled.
	 */
	public static class Resupply extends BackpackEvent implements ICancellableEvent {
		public Resupply(Player player, IBackpackDefinition backpackDefinition, Container backpackInventory) {
			super(player, backpackDefinition, backpackInventory);
		}
	}
}

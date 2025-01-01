package forestry.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ForestryClientEvents {
	EventGroup GROUP = EventGroup.of("ForestryClientEvents");

	/**
	 * Called every time client resource packs are reloaded.
	 */
	EventHandler LOAD = GROUP.client("load", () -> ForestryClientEventJS.class);
}

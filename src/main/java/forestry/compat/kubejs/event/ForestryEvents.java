package forestry.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

// A neutered version of the IForestryPlugin system
public interface ForestryEvents {
	EventGroup GROUP = EventGroup.of("ForestryEvents");

	/**
	 * Used to modify karyotypes and change things about species types (ex. bee, tree, butterfly)
	 * You may also register research materials for the Escritoire here.
	 */
	EventHandler GENETICS = GROUP.startup("genetics", () -> GeneticsEventJS.class);
	/**
	 * Register new bee species, world generation for hives, custom alleles,
	 */
	EventHandler APICULTURE = GROUP.startup("apiculture", () -> ApicultureEventJS.class);
	//EventHandler ARBORICULTURE = GROUP.startup("arboriculture", () -> ArboricultureEventJS.class);
	//EventHandler LEPIDOPTEROLOGY = GROUP.startup("lepidopterology", () -> Lei)
}

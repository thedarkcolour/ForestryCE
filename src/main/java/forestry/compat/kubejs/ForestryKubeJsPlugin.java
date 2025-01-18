package forestry.compat.kubejs;

import forestry.api.IForestryApi;
import forestry.api.client.IForestryClientApi;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.compat.kubejs.event.ForestryClientEvents;
import forestry.compat.kubejs.event.ForestryEvents;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

/**
 * A KubeJS plugin that registers Forestry-specific compatibility.
 */
public class ForestryKubeJsPlugin extends KubeJSPlugin {
	@Override
	public void registerEvents() {
		ForestryEvents.GROUP.register();
		ForestryClientEvents.GROUP.register();
	}

	@Override
	public void registerBindings(BindingsEvent event) {
		event.add("ForestryAlleles", ForestryAlleles.class);
		event.add("BeeChromosomes", BeeChromosomes.class);
		event.add("IForestryApi", IForestryApi.INSTANCE);
		event.add("IForestryClientApi", IForestryClientApi.INSTANCE);
	}
}

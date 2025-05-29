package forestry.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import forestry.api.IForestryApi;
import forestry.api.client.IForestryClientApi;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.ForestryTaxa;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.compat.kubejs.event.ForestryClientEvents;
import forestry.compat.kubejs.event.ForestryEvents;

/**
 * A KubeJS plugin that registers Forestry-specific compatibility.
 */
public class ForestryKubeJsPlugin implements KubeJSPlugin {
	@Override
	public void registerEvents(EventGroupRegistry registry) {
		registry.register(ForestryEvents.GROUP);
		registry.register(ForestryClientEvents.GROUP);
	}

	@Override
	public void registerBindings(BindingRegistry bindings) {
		bindings.add("ForestryAlleles", ForestryAlleles.class);
		bindings.add("BeeChromosomes", BeeChromosomes.class);
		bindings.add("IForestryApi", IForestryApi.INSTANCE);
		bindings.add("IForestryClientApi", IForestryClientApi.INSTANCE);
		bindings.add("HumidityType", HumidityType.class);
		bindings.add("TemperatureType", TemperatureType.class);
		bindings.add("ForestryTaxa", ForestryTaxa.class);
	}
}

package forestry.compat.kubejs;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fml.ModList;

import forestry.api.ForestryConstants;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.plugin.IApicultureRegistration;
import forestry.api.plugin.IForestryPlugin;
import forestry.api.plugin.IGeneticRegistration;
import forestry.compat.kubejs.event.ApicultureEventJS;
import forestry.compat.kubejs.event.ForestryClientEventJS;
import forestry.compat.kubejs.event.ForestryClientEvents;
import forestry.compat.kubejs.event.ForestryEvents;
import forestry.compat.kubejs.event.GeneticsEventJS;

/**
 * A Forestry plugin that fires events for the KubeJS integration
 */
public class KubeForestryPlugin implements IForestryPlugin {
	public static final ResourceLocation ID = ForestryConstants.forestry("kubejs");

	@Override
	public void registerGenetics(IGeneticRegistration genetics) {
		Delegate.registerGenetics(genetics);
	}

	@Override
	public void registerApiculture(IApicultureRegistration apiculture) {
		Delegate.registerApiculture(apiculture);
	}

	@Override
	public void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
		Delegate.registerClient(registrar);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public boolean shouldLoad() {
		return ModList.get().isLoaded("kubejs");
	}

	/**
	 * Needed to avoid classloading errors when KubeJS is not present
	 */
	private static class Delegate {
		private static void registerGenetics(IGeneticRegistration genetics) {
			ForestryEvents.GENETICS.post(new GeneticsEventJS(genetics));
		}

		private static void registerApiculture(IApicultureRegistration apiculture) {
			ForestryEvents.APICULTURE.post(new ApicultureEventJS(apiculture));
		}

		private static void registerClient(Consumer<Consumer<IClientRegistration>> registrar) {
			registrar.accept(registration -> ForestryClientEvents.LOAD.post(new ForestryClientEventJS(registration)));
		}
	}
}

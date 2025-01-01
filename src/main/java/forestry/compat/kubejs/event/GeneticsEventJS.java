package forestry.compat.kubejs.event;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;

import forestry.api.plugin.IGeneticRegistration;
import forestry.api.plugin.ISpeciesTypeBuilder;

import dev.latvian.mods.kubejs.event.EventJS;

public class GeneticsEventJS extends EventJS {
	private final IGeneticRegistration wrapped;

	public GeneticsEventJS(IGeneticRegistration wrapped) {
		this.wrapped = wrapped;
	}

	public void modifySpeciesType(ResourceLocation id, Consumer<ISpeciesTypeBuilder> modifications) {
		this.wrapped.modifySpeciesType(id, modifications);
	}
}

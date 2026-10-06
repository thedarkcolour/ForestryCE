package forestry.apiculture.bees.genetics;

import java.util.List;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.core.engine.genetics.AbstractDefinitionSpeciesBuilder;

public class DefinitionBeeSpeciesBuilder
	extends AbstractDefinitionSpeciesBuilder<BeeSpeciesDefinition, IBeeSpeciesType, IBeeSpecies, IBeeSpeciesBuilder>
	implements IBeeSpeciesBuilder {

	public DefinitionBeeSpeciesBuilder(BeeSpeciesDefinition def) {
		super(def);
	}

	@Override public List<IProduct> buildProducts() { return List.copyOf(def.products()); }
	@Override public List<IProduct> buildSpecialties() { return List.copyOf(def.specialties()); }
	@Override public int getBody() { return def.body(); }
	@Override public int getStripes() { return def.stripes(); }
	@Override public int getOutline() { return def.outline(); }
	@Override public ResourceLocation getJubilance() { return def.jubilance(); }

	@Override public IBeeSpeciesBuilder addProduct(IProduct product) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder addProduct(ItemStack stack, float chance) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder addSpecialty(IProduct specialty) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder addSpecialty(ItemStack stack, float chance) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder setBody(TextColor color) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder setStripes(TextColor color) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder setOutline(TextColor color) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
	@Override public IBeeSpeciesBuilder setJubilance(ResourceLocation id) { throw new UnsupportedOperationException(READ_ONLY_MESSAGE); }
}

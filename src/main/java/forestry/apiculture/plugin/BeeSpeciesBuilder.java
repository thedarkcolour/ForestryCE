package forestry.apiculture.plugin;

import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.genetics.IBeeSpeciesType;
import forestry.api.core.IProduct;
import forestry.api.plugin.IBeeSpeciesBuilder;
import forestry.apiculture.bees.BeeSpecies;
import forestry.apiimpl.plugin.SpeciesBuilder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BeeSpeciesBuilder extends SpeciesBuilder<IBeeSpeciesType, IBeeSpecies, IBeeSpeciesBuilder> implements IBeeSpeciesBuilder {
	private final ArrayList<IProduct> products = new ArrayList<>();
	private final ArrayList<IProduct> specialties = new ArrayList<>();
	private int bodyColor = 0xffdc16;
	private int stripesColor = 0;
	private int outlineColor = -1;
	private ResourceLocation jubilance = ForestryBeeJubilances.DEFAULT;

	public BeeSpeciesBuilder(ResourceLocation id, String genus, String species) {
		super(id, genus, species);
	}

	@Override
	public ISpeciesFactory<IBeeSpeciesType, IBeeSpecies, IBeeSpeciesBuilder> createSpeciesFactory() {
		return BeeSpecies::new;
	}

	@Override
	public IBeeSpeciesBuilder addProduct(IProduct product) {
		this.products.add(product);
		return this;
	}

	@Override
	public IBeeSpeciesBuilder addSpecialty(IProduct specialty) {
		this.specialties.add(specialty);
		return this;
	}

	@Override
	public IBeeSpeciesBuilder setBody(TextColor color) {
		this.bodyColor = color.getValue();
		return this;
	}

	@Override
	public IBeeSpeciesBuilder setStripes(TextColor color) {
		this.stripesColor = color.getValue();
		return this;
	}

	@Override
	public IBeeSpeciesBuilder setOutline(TextColor color) {
		this.outlineColor = color.getValue();
		return this;
	}

	@Override
	public IBeeSpeciesBuilder setJubilance(ResourceLocation id) {
		this.jubilance = id;
		return this;
	}

	@Override
	public List<IProduct> buildProducts() {
		return List.copyOf(this.products);
	}

	@Override
	public List<IProduct> buildSpecialties() {
		return List.copyOf(this.specialties);
	}

	@Override
	public int getBody() {
		return this.bodyColor;
	}

	@Override
	public int getStripes() {
		return this.stripesColor;
	}

	@Override
	public int getOutline() {
		return this.outlineColor;
	}

	@Override
	public ResourceLocation getJubilance() {
		return this.jubilance;
	}
}

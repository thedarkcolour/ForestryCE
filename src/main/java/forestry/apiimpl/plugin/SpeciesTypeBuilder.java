package forestry.apiimpl.plugin;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.item.Item;

import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpeciesType;
import forestry.api.plugin.IKaryotypeBuilder;
import forestry.api.plugin.ISpeciesTypeBuilder;
import forestry.api.plugin.ISpeciesTypeFactory;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.items.EnumHoneyComb;
import forestry.core.genetics.Karyotype;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;

public class SpeciesTypeBuilder implements ISpeciesTypeBuilder {
	private final ISpeciesTypeFactory typeFactory;
	private final HashSet<ILifeStage> stages;

	@Nullable
	private ILifeStage defaultStage = null;
	@Nullable
	private Consumer<IKaryotypeBuilder> karyotype = null;
	private Consumer<Reference2FloatMap<Item>> researchMaterials;

	public SpeciesTypeBuilder(ISpeciesTypeFactory typeFactory) {
		this.typeFactory = typeFactory;
		this.stages = new HashSet<>();

		// The default research materials across all species in Forestry
		this.researchMaterials = map -> {
			map.put(ApicultureItems.HONEY_DROP.item(), 0.5f);
			map.put(ApicultureItems.HONEYDEW.item(), 0.7f);
			map.put(ApicultureItems.BEE_COMBS.item(EnumHoneyComb.HONEY), 0.4f);
		};
	}

	@Override
	public ISpeciesTypeBuilder setKaryotype(Consumer<IKaryotypeBuilder> karyotype) {
		if (this.karyotype == null) {
			this.karyotype = karyotype;
		} else {
			this.karyotype = this.karyotype.andThen(karyotype);
		}
		return this;
	}

	@Override
	public ISpeciesTypeBuilder addStages(ILifeStage... stages) {
		this.stages.addAll(Arrays.asList(stages));
		return this;
	}

	@Override
	public ISpeciesTypeBuilder setDefaultStage(ILifeStage stage) {
		this.defaultStage = stage;
		return this;
	}

	@Override
	public ISpeciesTypeBuilder addResearchMaterials(Consumer<Reference2FloatMap<Item>> materials) {
		this.researchMaterials = this.researchMaterials.andThen(materials);
		return this;
	}

	@Override
	public List<ILifeStage> getStages() {
		return List.copyOf(this.stages);
	}

	@Override
	public ILifeStage getDefaultStage() {
		Preconditions.checkState(this.defaultStage != null, "Missing default ILifeStage for species type");

		return this.defaultStage;
	}

	@Override
	public void buildResearchMaterials(Reference2FloatMap<Item> materialMap) {
		this.researchMaterials.accept(materialMap);
	}

	public ISpeciesType<?, ?> build() {
		Preconditions.checkState(this.karyotype != null, "Missing karyotype for species type");

		Karyotype.Builder builder = new Karyotype.Builder();
		this.karyotype.accept(builder);
		return this.typeFactory.create(builder.build(), this);
	}
}

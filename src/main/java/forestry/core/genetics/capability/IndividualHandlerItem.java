package forestry.core.genetics.capability;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

// Used for Vanilla sapling items.
public class IndividualHandlerItem implements ICapabilityProvider<ItemStack, Void, IIndividualHandlerItem>, IIndividualHandlerItem {
	protected final ISpeciesType<?, ?> speciesType;
	protected final ItemStack container;
	protected IIndividual individual;
	protected final ILifeStage stage;

	public IndividualHandlerItem(ISpeciesType<?, ?> type, ItemStack container, IIndividual individual, ILifeStage stage) {
		this.speciesType = type;
		this.container = container;
		this.individual = individual;
		this.stage = stage;
	}

	@Override
	public ISpeciesType<?, ?> getSpeciesType() {
		return this.speciesType;
	}

	@Override
	public ILifeStage getStage() {
		return this.stage;
	}

	@Override
	public IIndividual getIndividual() {
		return this.individual;
	}

	public ItemStack getContainer() {
		return this.container;
	}

	@Override
	public boolean isGeneticForm() {
		return this.container.is(getStage().getItemForm());
	}

	@Override
	public IIndividualHandlerItem getCapability(ItemStack stack, @Nullable Void v) {
		return this;
	}
}

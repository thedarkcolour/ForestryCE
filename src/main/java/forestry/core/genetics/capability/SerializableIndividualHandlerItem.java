package forestry.core.genetics.capability;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpeciesType;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SerializableIndividualHandlerItem extends IndividualHandlerItem implements INBTSerializable<Tag> {
	public SerializableIndividualHandlerItem(ISpeciesType<?, ?> type, ItemStack container, IIndividual individual, ILifeStage stage) {
		super(type, container, individual, stage);
	}

	@Override
	public Tag serializeNBT(HolderLookup.Provider registries) {
		return SpeciesUtil.serializeIndividual(this.individual);
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, Tag nbt) {
		this.individual = SpeciesUtil.deserializeIndividual(this.speciesType, nbt);
	}
}

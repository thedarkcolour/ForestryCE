package forestry.storage;

import forestry.api.genetics.capability.IIndividualHandlerItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class NaturalistBackpackFilter implements Predicate<ItemStack> {
	private final ResourceLocation speciesRootUid;

	public NaturalistBackpackFilter(ResourceLocation speciesType) {
		this.speciesRootUid = speciesType;
	}

	@Override
	public boolean test(ItemStack stack) {
		return IIndividualHandlerItem.filter(stack, individual -> this.speciesRootUid.equals(individual.getType().id()));
	}
}

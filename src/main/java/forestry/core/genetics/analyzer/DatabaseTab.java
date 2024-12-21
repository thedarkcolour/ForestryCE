package forestry.core.genetics.analyzer;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import forestry.api.genetics.IIndividual;
import forestry.api.genetics.gatgets.IDatabaseTab;

public abstract class DatabaseTab<I extends IIndividual> implements IDatabaseTab<I> {
	private final Supplier<ItemStack> stackSupplier;
	private final String name;

	public DatabaseTab(String name, Supplier<ItemStack> stackSupplier) {
		this.name = name;
		this.stackSupplier = stackSupplier;
	}

	@Override
	public ItemStack getIconStack() {
		return stackSupplier.get();
	}

	@Override
	public Component getTooltip(IIndividual individual) {
		return Component.translatable("for.gui.database.tab." + name + ".name");
	}
}

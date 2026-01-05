package forestry.storage;

import forestry.api.storage.IBackpackDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class BackpackDefinition implements IBackpackDefinition {
	private final int primaryColor;
	private final int secondaryColor;
	private final Predicate<ItemStack> filter;

	public BackpackDefinition(int primaryColor, int secondaryColor, Predicate<ItemStack> filter) {
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.filter = filter;
	}

	@Override
	public Predicate<ItemStack> getFilter() {
		return this.filter;
	}

	@Override
	public Component getName(ItemStack backpack) {
		Item item = backpack.getItem();
		Component display = Component.translatable((item.getDescriptionId(backpack)).trim());

		CompoundTag tagCompound = backpack.getTag();
		if (tagCompound != null && tagCompound.contains("display", 10)) {
			CompoundTag nbt = tagCompound.getCompound("display");

			if (nbt.contains("Name", 8)) {
				display = Component.literal(nbt.getString("Name"));
			}
		}

		return display;
	}

	@Override
	public int getPrimaryColour() {
		return this.primaryColor;
	}

	@Override
	public int getSecondaryColour() {
		return this.secondaryColor;
	}
}

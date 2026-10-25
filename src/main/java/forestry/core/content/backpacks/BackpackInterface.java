package forestry.core.content.backpacks;

import com.google.common.base.Preconditions;
import forestry.api.core.backpacks.EnumBackpackType;
import forestry.api.core.backpacks.IBackpackDefinition;
import forestry.api.core.backpacks.IBackpackInterface;
import forestry.core.content.backpacks.items.BackpackItem;
import forestry.core.content.backpacks.items.NaturalistBackpackItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.function.Predicate;

// todo this code is stupid and bloated. next time it breaks, simplify it and remove the silly API for it
public class BackpackInterface implements IBackpackInterface {
	@Override
	public Item createBackpack(IBackpackDefinition definition, EnumBackpackType type) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(type, "type must not be null");
		Preconditions.checkArgument(type != EnumBackpackType.NATURALIST, "type must not be NATURALIST. Use createNaturalistBackpack instead.");

		return new BackpackItem(definition, type, new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
	}

	@Override
	public Item createNaturalistBackpack(IBackpackDefinition definition, ResourceLocation speciesTypeId, CreativeModeTab tab) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(speciesTypeId, "rootUid must not be null");

		return new NaturalistBackpackItem(speciesTypeId, definition, new Item.Properties()
			.stacksTo(1)
			.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
	}

	@Override
	public Predicate<ItemStack> createNaturalistBackpackFilter(ResourceLocation speciesRootUid) {
		return new NaturalistBackpackFilter(speciesRootUid);
	}
}

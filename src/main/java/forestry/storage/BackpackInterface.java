package forestry.storage;

import com.google.common.base.Preconditions;
import forestry.api.storage.EnumBackpackType;
import forestry.api.storage.IBackpackDefinition;
import forestry.api.storage.IBackpackInterface;
import forestry.storage.items.ItemBackpack;
import forestry.storage.items.ItemBackpackNaturalist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

// todo this code is stupid and bloated. next time it breaks, simplify it and remove the silly API for it
public class BackpackInterface implements IBackpackInterface {
	@Override
	public Item createBackpack(IBackpackDefinition definition, EnumBackpackType type) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(type, "type must not be null");
		Preconditions.checkArgument(type != EnumBackpackType.NATURALIST, "type must not be NATURALIST. Use createNaturalistBackpack instead.");

		return new ItemBackpack(definition, type);
	}

	@Override
	public Item createNaturalistBackpack(IBackpackDefinition definition, ResourceLocation speciesTypeId, CreativeModeTab tab) {
		Preconditions.checkNotNull(definition, "definition must not be null");
		Preconditions.checkNotNull(speciesTypeId, "rootUid must not be null");

		return new ItemBackpackNaturalist(speciesTypeId, definition);
	}

	@Override
	public Predicate<ItemStack> createNaturalistBackpackFilter(ResourceLocation speciesRootUid) {
		return new BackpackFilterNaturalist(speciesRootUid);
	}
}

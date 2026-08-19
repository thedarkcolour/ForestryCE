package forestry.apiculture.compat;

import forestry.api.ForestryConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

/**
 * Prevents Jade's generic item-storage provider from serializing the entire
 * Apiary inventory. Forestry provides a purpose-built Apiary tooltip instead.
 */
public enum ApiaryItemStorageProvider implements
	IServerExtensionProvider<ItemStack>,
	IClientExtensionProvider<ItemStack, ItemView> {

	INSTANCE;

	@Override
	public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
		// Non-null means this provider handles the storage.
		// An empty list prevents Jade from falling through to its generic
		// inventory provider.
		return List.of();
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(
		Accessor<?> accessor,
		List<ViewGroup<ItemStack>> groups
	) {
		return List.of();
	}

	@Override
	public ResourceLocation getUid() {
		return ForestryConstants.forestry("apiary_item_storage");
	}

	@Override
	public int getDefaultPriority() {
		// Jade's generic storage extension has priority 9999.
		return 0;
	}
}
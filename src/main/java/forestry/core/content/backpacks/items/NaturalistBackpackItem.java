package forestry.core.content.backpacks.items;

import forestry.api.IForestryApi;
import forestry.api.core.backpacks.EnumBackpackType;
import forestry.api.core.backpacks.IBackpackDefinition;
import forestry.core.content.backpacks.gui.NaturalistBackpackMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NaturalistBackpackItem extends BackpackItem {
	public final ResourceLocation typeId;

	public NaturalistBackpackItem(ResourceLocation typeId, IBackpackDefinition definition, Item.Properties properties) {
		super(definition, EnumBackpackType.NATURALIST, properties);
		this.typeId = typeId;
	}

	@Override
	protected void writeContainerData(ServerPlayer player, ItemStack stack, RegistryFriendlyByteBuf buffer) {
		buffer.writeResourceLocation(this.typeId);
	}

	// The lepidopterist backpack is registered by base, but the butterfly jar owns its species type. The
	// menu needs that type, so a backpack whose type is missing stays shut rather than kicking the player.
	@Override
	protected void openGui(ServerPlayer serverPlayer, ItemStack heldItem) {
		if (IForestryApi.INSTANCE.getGeneticManager().getSpeciesTypeSafe(this.typeId) == null) {
			return;
		}
		super.openGui(serverPlayer, heldItem);
	}

	@Override
	public AbstractContainerMenu getContainer(int containerId, Player player, ItemStack heldItem) {
		return NaturalistBackpackMenu.makeContainer(containerId, player, heldItem, this.typeId);
	}
}

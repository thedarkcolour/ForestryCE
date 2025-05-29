package forestry.worktable;

import forestry.api.client.IClientModuleHandler;
import forestry.api.modules.ForestryModule;
import forestry.api.modules.ForestryModuleIds;
import forestry.core.network.PacketIdClient;
import forestry.core.network.PacketIdServer;
import forestry.modules.BlankForestryModule;
import forestry.worktable.client.WorktableClientHandler;
import forestry.worktable.network.packets.PacketWorktableMemoryUpdate;
import forestry.worktable.network.packets.PacketWorktableRecipeRequest;
import forestry.worktable.network.packets.PacketWorktableRecipeUpdate;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

@ForestryModule
public class ModuleWorktable extends BlankForestryModule {
	@Override
	public ResourceLocation getId() {
		return ForestryModuleIds.WORKTABLE;
	}

	@Override
	public void registerPackets(PayloadRegistrar registrar) {
		registrar.playToServer(PacketIdServer.WORKTABLE_RECIPE_REQUEST, StreamCodec.of(PacketWorktableRecipeRequest::encode, PacketWorktableRecipeRequest::decode), PacketWorktableRecipeRequest::handle);

		registrar.playToClient(PacketIdClient.WORKTABLE_MEMORY_UPDATE, StreamCodec.of(PacketWorktableMemoryUpdate::encode, PacketWorktableMemoryUpdate::decode), PacketWorktableMemoryUpdate::handle);
		registrar.playToClient(PacketIdClient.WORKTABLE_CRAFTING_UPDATE, StreamCodec.of(PacketWorktableRecipeUpdate::encode, PacketWorktableRecipeUpdate::decode), PacketWorktableRecipeUpdate::handle);
	}

	@Override
	public void registerClientHandler(Consumer<IClientModuleHandler> registrar) {
		registrar.accept(new WorktableClientHandler());
	}
}

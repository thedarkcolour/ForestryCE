package forestry.core.network;

import forestry.apiculture.network.packets.PacketAlvearyChange;
import forestry.apiculture.network.packets.PacketBeeLogicActive;
import forestry.arboriculture.network.PacketRipeningUpdate;
import forestry.core.network.packets.PacketGenomeTrackerSync;
import forestry.worktable.network.packets.PacketWorktableMemoryUpdate;
import forestry.worktable.network.packets.PacketWorktableRecipeUpdate;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static forestry.core.network.PacketIdServer.type;

/**
 * Packets sent to the client from the server
 */
public class PacketIdClient {
	// Core
	public static final CustomPacketPayload.Type<?> RECIPE_CACHE = type("recipe_cache");
	// Core Gui
	public static final CustomPacketPayload.Type<?> ERROR_UPDATE = type("error_update");
	public static final CustomPacketPayload.Type<?> GUI_UPDATE = type("gui_update");
	public static final CustomPacketPayload.Type<?> GUI_LAYOUT_SELECT = type("gui_layout_select");
	public static final CustomPacketPayload.Type<?> GUI_ENERGY = type("gui_energy");
	public static final CustomPacketPayload.Type<?> SOCKET_UPDATE = type("socket_update");
	// Core Tile Entities
	public static final CustomPacketPayload.Type<?> TILE_FORESTRY_UPDATE = type("tile_forestry_update");
	public static final CustomPacketPayload.Type<?> ITEMSTACK_DISPLAY = type("itemstack_display");
	public static final CustomPacketPayload.Type<?> TANK_LEVEL_UPDATE = type("tank_level_update");
	public static final CustomPacketPayload.Type<?> REFRACTORY_WAX_ON = type("refractory_wax_on");
	// Core Genome
	public static final CustomPacketPayload.Type<PacketGenomeTrackerSync> GENOME_TRACKER_UPDATE = type("genome_tracker_update");
	// Factory
	public static final CustomPacketPayload.Type<PacketWorktableMemoryUpdate> WORKTABLE_MEMORY_UPDATE = type("worktable_memory_update");
	public static final CustomPacketPayload.Type<PacketWorktableRecipeUpdate> WORKTABLE_CRAFTING_UPDATE = type("worktable_crafting_update");
	// Apiculture
	public static final CustomPacketPayload.Type<?> TILE_FORESTRY_ACTIVE = type("tile_forestry_active");
	public static final CustomPacketPayload.Type<PacketBeeLogicActive> BEE_LOGIC_ACTIVE = type("bee_logic_active");
	public static final CustomPacketPayload.Type<PacketAlvearyChange> ALVEARY_CONTROLLER_CHANGE = type("alveary_controller_change");
	// Arboriculture
	public static final CustomPacketPayload.Type<PacketRipeningUpdate> RIPENING_UPDATE = type("ripening_update");
	// Mail
	public static final CustomPacketPayload.Type<?> TRADING_ADDRESS_RESPONSE = type("trading_address_response");
	public static final CustomPacketPayload.Type<?> LETTER_INFO_RESPONSE_PLAYER = type("letter_info_response_player");
	public static final CustomPacketPayload.Type<?> LETTER_INFO_RESPONSE_TRADER = type("letter_info_response_trader");
	public static final CustomPacketPayload.Type<?> POBOX_INFO_RESPONSE = type("pobox_info_response");
	// Sorting
	public static final CustomPacketPayload.Type<?> GUI_UPDATE_FILTER = type("gui_update_filter");
	// JEI
	public static final CustomPacketPayload.Type<?> RECIPE_TRANSFER_UPDATE = type("recipe_transfer_update");
}

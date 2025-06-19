package forestry.core.network;

import forestry.api.ForestryConstants;
import forestry.core.network.packets.PacketChipsetClick;
import forestry.core.network.packets.PacketGuiSelectRequest;
import forestry.core.network.packets.PacketPipetteClick;
import forestry.core.network.packets.PacketSolderingIronClick;
import forestry.factory.network.packets.PacketRecipeTransferRequest;
import forestry.mail.network.packets.PacketLetterInfoRequest;
import forestry.mail.network.packets.PacketLetterTextSet;
import forestry.mail.network.packets.PacketTraderAddressRequest;
import forestry.sorting.network.packets.PacketFilterChangeGenome;
import forestry.sorting.network.packets.PacketFilterChangeRule;
import forestry.worktable.network.packets.PacketWorktableRecipeRequest;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Packets sent to the server from the client
 */
public class PacketIdServer {
	// Core Gui
	public static final CustomPacketPayload.Type<PacketGuiSelectRequest> GUI_SELECTION_REQUEST = type("gui_selection_request");
	public static final CustomPacketPayload.Type<PacketPipetteClick> PIPETTE_CLICK = type("pipette_click");
	public static final CustomPacketPayload.Type<PacketChipsetClick> CHIPSET_CLICK = type("chipset_click");
	public static final CustomPacketPayload.Type<PacketSolderingIronClick> SOLDERING_IRON_CLICK = type("soldering_iron_click");
	// Sorting
	public static final CustomPacketPayload.Type<PacketFilterChangeRule> FILTER_CHANGE_RULE = type("filter_change_rule");
	public static final CustomPacketPayload.Type<PacketFilterChangeGenome> FILTER_CHANGE_GENOME = type("filter_change_genome");
	// JEI
	public static final CustomPacketPayload.Type<PacketWorktableRecipeRequest> WORKTABLE_RECIPE_REQUEST = type("worktable_recipe_request");
	public static final CustomPacketPayload.Type<PacketRecipeTransferRequest> RECIPE_TRANSFER_REQUEST = type("recipe_transfer_request");
	// Mail
	public static final CustomPacketPayload.Type<PacketLetterInfoRequest> LETTER_INFO_REQUEST = type("letter_info_request");
	public static final CustomPacketPayload.Type<PacketTraderAddressRequest> TRADING_ADDRESS_REQUEST = type("trading_address_request");
	public static final CustomPacketPayload.Type<PacketLetterTextSet> LETTER_TEXT_SET = type("letter_text_set");

	static <P extends CustomPacketPayload> CustomPacketPayload.Type<P> type(String path) {
		return new CustomPacketPayload.Type<>(ForestryConstants.forestry(path));
	}
}

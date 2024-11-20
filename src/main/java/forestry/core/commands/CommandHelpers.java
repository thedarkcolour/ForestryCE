package forestry.core.commands;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CommandHelpers {
	public static final Predicate<CommandSourceStack> ADMIN = sender -> sender.hasPermission(2);

	public static void sendLocalizedChatMessage(CommandSourceStack sender, String locTag, Object... args) {
		sender.sendSuccess(() -> Component.translatable(locTag, args), false);
	}

	public static void sendLocalizedChatMessage(CommandSourceStack sender, Style chatStyle, String locTag, Object... args) {
		sender.sendSuccess(() -> Component.translatable(locTag, args).setStyle(chatStyle), false);
	}

	/**
	 * Avoid using this function if at all possible. Commands are processed on the server,
	 * which has no localization information.
	 * <p>
	 * StringUtil.localize() is NOT a valid alternative for sendLocalizedChatMessage().
	 * Messages will not be localized properly if you use StringUtil.localize().
	 */
	public static void sendChatMessage(CommandSourceStack sender, Component message) {
		sender.sendSuccess(() -> message, false);
	}
}

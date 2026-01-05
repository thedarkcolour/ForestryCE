package forestry.core.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import forestry.Forestry;
import forestry.api.ForestryConstants;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.ISpecies;
import forestry.core.utils.StringUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public final class CommandSaveStats implements Command<CommandSourceStack> {
	private static final Component discoveredSymbol = Component.translatable("for.chat.command.forestry.stats.save.key.discovered.symbol");
	private static final Component blacklistedSymbol = Component.translatable("for.chat.command.forestry.stats.save.key.blacklisted.symbol");
	private static final Component notCountedSymbol = Component.translatable("for.chat.command.forestry.stats.save.key.notCounted.symbol");

	private final IStatsSaveHelper saveHelper;

	public CommandSaveStats(IStatsSaveHelper saveHelper) {
		this.saveHelper = saveHelper;
	}

	public static ArgumentBuilder<CommandSourceStack, ?> register(IStatsSaveHelper saveHelper) {
		return Commands.literal("save")
			.then(Commands.argument("player", EntityArgument.player())
				.executes(new CommandSaveStats(saveHelper)));

	}

	public int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {

		ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

		Level world = ctx.getSource().getLevel();

		Collection<Component> statistics = new ArrayList<>();

		String date = DateFormat.getInstance().format(new Date());
		Component emptyLiteral = Component.literal("");

		// todo why tf are there empty components getting added?
		statistics.add(Component.translatable(this.saveHelper.getTranslationKey(), player.getDisplayName(), date));
		statistics.add(emptyLiteral);
		//statistics.add(Component.translatable("for.chat.command.forestry.stats.save.mode", modeHelper.getModeName(world)));
		statistics.add(emptyLiteral);

		IBreedingTracker tracker = this.saveHelper.getBreedingTracker(world, player.getGameProfile());
        this.saveHelper.addExtraInfo(statistics, tracker);

		Collection<? extends ISpecies<?>> species = this.saveHelper.getSpecies();

		String speciesCount = Component.translatable("for.gui.speciescount").getString();
		String speciesCountLine = String.format("%s (%s):", speciesCount, species.size());
		statistics.add(Component.literal(speciesCountLine));
		statistics.add(StringUtil.line(speciesCountLine.length()));

		statistics.add(Component.literal(discoveredSymbol + ": ").append(Component.translatable("for.chat.command.forestry.stats.save.key.discovered")));
		statistics.add(Component.literal(blacklistedSymbol + ": ").append(Component.translatable("for.chat.command.forestry.stats.save.key.blacklisted")));
		statistics.add(Component.literal(notCountedSymbol + ": ").append(Component.translatable("for.chat.command.forestry.stats.save.key.notCounted")));
		statistics.add(Component.literal(""));

		String header = generateSpeciesListHeader();
		statistics.add(Component.literal(header));

		statistics.add(StringUtil.line(header.length()));
		statistics.add(Component.literal(""));

		for (ISpecies<?> allele : species) {
			statistics.add(Component.literal(generateSpeciesListEntry(allele, tracker)));
		}

		// todo test
		File file = new File("config/" + ForestryConstants.MOD_ID + "/stats/" + player.getDisplayName().getString() + '-' + this.saveHelper.getFileSuffix() + ".log");
		try {
			File folder = file.getParentFile();
			if (folder != null && !folder.exists()) {
				boolean success = file.getParentFile().mkdirs();
				if (!success) {
					CommandHelpers.sendLocalizedChatMessage(ctx.getSource(), "for.chat.command.forestry.stats.save.error1");
					return 0;
				}
			}

			if (!file.exists() && !file.createNewFile()) {
				CommandHelpers.sendLocalizedChatMessage(ctx.getSource(), "for.chat.command.forestry.stats.save.error1");
				return 0;
			}

			if (!file.canWrite()) {
				CommandHelpers.sendLocalizedChatMessage(ctx.getSource(), "for.chat.command.forestry.stats.save.error2");
				return 0;
			}

			String newLine = System.lineSeparator();
			FileOutputStream fileout = new FileOutputStream(file);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileout, StandardCharsets.UTF_8));

			writer.write("# " + ForestryConstants.MOD_ID + newLine + "# " + ModList.get().getModContainerById(ForestryConstants.MOD_ID).get().getModInfo().getVersion() + newLine);

			for (Component line : statistics) {
				writer.write(line.getString() + newLine);
			}

			writer.close();

		} catch (IOException ex) {
			CommandHelpers.sendLocalizedChatMessage(ctx.getSource(), "for.chat.command.forestry.stats.save.error3");
			Forestry.LOGGER.error("Write operation threw an exception. Failed to save statistics.", ex);
			return 0;
		}

		CommandHelpers.sendLocalizedChatMessage(ctx.getSource(), "for.chat.command.forestry.stats.save.saved", player.getDisplayName());

		return 1;
	}

	private static String generateSpeciesListHeader() {
		Component authority = Component.translatable("for.gui.alyzer.authority");
		Component species = Component.translatable("for.gui.species");
		return speciesListEntry(discoveredSymbol, notCountedSymbol, "UID", species, authority.getString());
	}

	private static String generateSpeciesListEntry(ISpecies<?> species, IBreedingTracker tracker) {
		Component discovered = Component.empty();
		if (tracker.isDiscovered(species)) {
			discovered = discoveredSymbol;
		}

		Component notCounted = Component.empty();
		if (!species.isDominant()) {
			notCounted = notCountedSymbol;
		}

		return speciesListEntry(discovered, notCounted, species.id().toString(), species.getDisplayName(), species.getAuthority());
	}

	private static String speciesListEntry(Component discovered, Component notCounted, String UID, Component speciesName, String authority) {
		return String.format("[ %-2s ] [ %-2s ]\t%-40s %-20s %-20s", discovered, notCounted, UID, speciesName, authority);
	}
}

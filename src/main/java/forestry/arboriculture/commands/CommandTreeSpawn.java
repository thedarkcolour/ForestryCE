package forestry.arboriculture.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import forestry.api.arboriculture.genetics.ITree;
import forestry.core.commands.CommandHelpers;
import forestry.core.commands.SpeciesArgument;
import forestry.core.utils.SpeciesUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CommandTreeSpawn {
	public static ArgumentBuilder<CommandSourceStack, ?> register(String name, ITreeSpawner treeSpawner) {
		return Commands.literal(name).requires(CommandHelpers.ADMIN)
			.then(Commands.argument("type", new SpeciesArgument(SpeciesUtil.TREE_TYPE.get()))
				.executes(a -> run(treeSpawner, a.getSource(), a.getArgument("type", ITree.class))));
	}

	public static int run(ITreeSpawner treeSpawner, CommandSourceStack source, ITree tree) throws CommandSyntaxException {
		return treeSpawner.spawn(source, tree, source.getPlayerOrException());
	}
}

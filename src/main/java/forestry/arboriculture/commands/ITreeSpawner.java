package forestry.arboriculture.commands;

import forestry.api.arboriculture.genetics.ITree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ITreeSpawner {
	int spawn(CommandSourceStack source, ITree treeName, Player player);
}

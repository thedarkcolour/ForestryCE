package forestry.arboriculture.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import forestry.api.arboriculture.genetics.ITree;

public class TreeSpawner implements ITreeSpawner {
	@Override
	public int spawn(CommandSourceStack source, ITree tree, Player player) {
		Vec3 look = player.getLookAngle();

		int x = (int) Math.round(player.getX() + 3 * look.x);
		int y = (int) Math.round(player.getY());
		int z = (int) Math.round(player.getZ() + 3 * look.z);
		BlockPos pos = new BlockPos(x, y, z);

		ServerLevel level = source.getLevel();
		TreeGenHelper.generateTree(tree.getSpecies(), null, level, level.random, pos);
		return 1;
	}
}

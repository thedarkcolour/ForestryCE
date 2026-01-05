package forestry.arboriculture.commands;

import forestry.api.arboriculture.genetics.ITree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ForestSpawner implements ITreeSpawner {
	@Override
	public int spawn(CommandSourceStack source, ITree tree, Player player) {
		Vec3 look = player.getLookAngle();

		int x = (int) Math.round(player.getX() + 16 * look.x);
		int y = (int) Math.round(player.getY());
		int z = (int) Math.round(player.getZ() + 16 * look.z);
		ServerLevel level = (ServerLevel) player.level();

		for (int i = 0; i < 16; i++) {
			int spawnX = x + level.random.nextInt(32) - 16;
			int spawnZ = z + level.random.nextInt(32) - 16;
			BlockPos pos = new BlockPos(spawnX, y, spawnZ);

			TreeGenHelper.generateTree(tree.getSpecies(), null, level, level.getRandom(), pos);
		}

		return 1;
	}
}

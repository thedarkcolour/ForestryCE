package forestry.arboriculture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forestry.api.arboriculture.ICharcoalPileWall;
import forestry.api.arboriculture.ITreeManager;
import forestry.api.arboriculture.IWoodAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class TreeManager implements ITreeManager {
	private final ImmutableMap<Block, Block> refractoryWaxables;
	private final ImmutableList<ICharcoalPileWall> charcoalWalls;

	public TreeManager(ImmutableMap<Block, Block> refractoryWaxables, ImmutableList<ICharcoalPileWall> charcoalWalls) {
		this.refractoryWaxables = refractoryWaxables;
		this.charcoalWalls = charcoalWalls;
	}

	@Nullable
	@Override
	public Block getRefractoryWaxed(Block block) {
		return this.refractoryWaxables.get(block);
	}

	@Override
	public List<ICharcoalPileWall> getWalls() {
		return this.charcoalWalls;
	}

	@Nullable
	@Override
	public ICharcoalPileWall getWall(BlockState state) {
		for (ICharcoalPileWall wall : this.charcoalWalls) {
			if (wall.matches(state)) {
				return wall;
			}
		}

		return null;
	}

	@Override
	public IWoodAccess getWoodAccess() {
		// todo only make accessible after wood access is populated
		return WoodAccess.INSTANCE;
	}
}

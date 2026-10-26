package forestry.gametest;

import java.util.List;

import forestry.agriculture.features.FarmingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import forestry.api.ForestryConstants;
import forestry.apiculture.alveary.AlvearyBlock;
import forestry.apiculture.features.ApicultureBlocks;
import forestry.core.platform.multiblock.LevelStructureView;
import forestry.agriculture.multifarm.blocks.MultifarmBlockType;
import forestry.agriculture.multifarm.blocks.MultifarmMaterialType;

/**
 * Covers component recognition in {@link LevelStructureView}. A member that fills no reserved role
 * ({@code *_plain} or {@code farm_gearbox}) must still be recognised as part of its machine and let the
 * structure form.
 *
 * <p>This is the addon-extensibility path. Recognition uses the public {@code IAlvearyComponent} or
 * {@code IFarmComponent} interface plus the engine's {@code MultiblockTileEntityForestry} base. That is
 * the same rule for Forestry's own non-role parts (sieve, hatch) and for a third-party one, so placing a
 * sieve or hatch in an exterior cell exercises the code an addon part runs through, without a test-only
 * block entity type.
 */
@GameTestHolder(ForestryConstants.MOD_ID)
@PrefixGameTestTemplate(false)
public class LevelStructureViewComponentTest {
	private static final BlockPos BASE = new BlockPos(6, 1, 6);
	private static final int TIMEOUT = 240;

	/** Checks that an alveary assembles with a non-role member (a sieve) in an exterior cell. */
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void alvearyFormsWithNonRoleComponent(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, BASE);
		BlockState sieve = ApicultureBlocks.ALVEARY.get(AlvearyBlock.Type.SIEVE).defaultState();
		// exterior (x == 0), and not the plain-only top layer
		helper.setBlock(BASE.offset(0, 1, 1), sieve);

		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"an alveary containing a non-role component (sieve) must still assemble");
		helper.succeed();
	}

	/** Checks that a farm assembles with a non-role member (a hatch) in an exterior cell. */
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void farmFormsWithNonRoleComponent(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildFarm(helper, BASE);
		BlockState hatch = FarmingBlocks.FARM.get(MultifarmBlockType.HATCH, MultifarmMaterialType.STONE_BRICK).defaultState();
		// exterior, and not the level-2 plain-only band
		helper.setBlock(BASE.offset(0, 1, 1), hatch);

		ServerLevel level = helper.getLevel();
		helper.assertTrue(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"a farm containing a non-role component (hatch) must still assemble");
		helper.succeed();
	}

	/** Checks that a non-component block entity in an interior cell is not adopted as a part. */
	@GameTest(template = "empty", timeoutTicks = TIMEOUT)
	public static void nonComponentBlockEntityIsNotAPart(GameTestHelper helper) {
		List<BlockPos> members = MultiblockTestSupport.buildAlveary(helper, BASE);
		helper.setBlock(BASE.offset(1, 1, 1), Blocks.CHEST);

		ServerLevel level = helper.getLevel();
		helper.assertFalse(MultiblockTestSupport.isAssembled(level, members.get(0)),
				"a chest in the interior must not be recognised as an alveary component");
		helper.succeed();
	}
}

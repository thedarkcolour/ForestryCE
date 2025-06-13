package forestry.arboriculture.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.arboriculture.IToolGrafter;
import forestry.api.arboriculture.genetics.IFruit;
import forestry.api.arboriculture.genetics.ITree;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.IFruitBearer;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.alleles.TreeChromosomes;
import forestry.arboriculture.blocks.BlockDefaultLeavesFruit;
import forestry.core.utils.SpeciesUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GrafterLootModifier extends LootModifier {
	public static final MapCodec<GrafterLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, GrafterLootModifier::new));

	public GrafterLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null || !state.is(BlockTags.LEAVES)) {
			return generatedLoot;
		}
		ItemStack harvestingTool = context.getParamOrNull(LootContextParams.TOOL);
		if (harvestingTool == null || harvestingTool.isEmpty()) {
			return generatedLoot;
		}
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		if (!(entity instanceof Player player)) {
			return generatedLoot;
		}
		if (generatedLoot.stream().noneMatch((stack) -> stack.is(ItemTags.SAPLINGS))) {
			handleLoot(generatedLoot, player, harvestingTool, state, context);
		}
		harvestingTool.hurt(1, context.getRandom(), (ServerPlayer) player);
		if (harvestingTool.isEmpty()) {
			EventHooks.onPlayerDestroyItem(player, harvestingTool, InteractionHand.MAIN_HAND);
		}
		return generatedLoot;
	}

	public void handleLoot(List<ItemStack> generatedLoot, Player player, ItemStack harvestingTool, BlockState state, LootContext context) {
		Level world = player.level();
		BlockEntity tileEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
		ITree tree = getTree(state, tileEntity);
		if (tree == null) {
			return;
		}
		Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
		if (origin == null) {
			return;
		}
		BlockPos pos = BlockPos.containing(origin);
		Item item = harvestingTool.getItem();
		float saplingModifier = 1.0f;
		if (item instanceof IToolGrafter) {
			saplingModifier = ((IToolGrafter) item).getSaplingModifier(harvestingTool, world, player, pos);
		}
		List<ITree> saplings = tree.getSaplings(world, pos, player.getGameProfile(), saplingModifier);
		for (ITree sapling : saplings) {
			if (sapling != null) {
				generatedLoot.add(sapling.createStack(TreeLifeStage.SAPLING));
			}
		}
		if (tileEntity instanceof IFruitBearer bearer) {
			generatedLoot.addAll(bearer.pickFruit());
		}
		if (state.getBlock() instanceof BlockDefaultLeavesFruit) {
			IGenome genome = tree.getGenome();
			IFruit fruitProvider = genome.getActiveValue(TreeChromosomes.FRUIT);
			if (fruitProvider.isFruitLeaf()) {
				generatedLoot.addAll(tree.produceStacks(world, pos, Integer.MAX_VALUE));
			}
		}
	}

	@Nullable
	private ITree getTree(BlockState state, @Nullable BlockEntity entity) {
		ITreeSpeciesType type = SpeciesUtil.TREE_TYPE.get();
		ITree tree = type.getVanillaIndividual(state);
		if (tree != null || entity == null) {
			return tree;
		} else {
			return type.getTree(entity);
		}
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}

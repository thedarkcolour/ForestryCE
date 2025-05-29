package forestry.arboriculture.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.arboriculture.blocks.BlockAsh;
import forestry.core.loot.CoreLootFunctions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Set;

public class CountBlockFunction extends LootItemConditionalFunction {
	public static final MapCodec<CountBlockFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).apply(instance, CountBlockFunction::new));

	private CountBlockFunction(List<LootItemCondition> conditions) {
		super(conditions);
	}

	public static LootItemConditionalFunction.Builder<?> builder() {
		return simpleBuilder(CountBlockFunction::new);
	}

	@Override
	public LootItemFunctionType<CountBlockFunction> getType() {
		return CoreLootFunctions.COUNT.value();
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (state == null || !state.hasProperty(BlockAsh.AMOUNT)) {
			return stack;
		}
		int amount = state.getValue(BlockAsh.AMOUNT);
		stack.setCount(amount);
		return stack;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_STATE);
	}
}

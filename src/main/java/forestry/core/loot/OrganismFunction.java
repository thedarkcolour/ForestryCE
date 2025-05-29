package forestry.core.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import forestry.api.IForestryApi;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Loot function to add genetic information, an organism, to the item stack.
 */
public class OrganismFunction extends LootItemConditionalFunction {
	public static final MapCodec<OrganismFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
		.and(instance.group(
			ResourceLocation.CODEC.fieldOf("type_id").forGetter(function -> function.typeId),
			ResourceLocation.CODEC.fieldOf("species_id").forGetter(function -> function.speciesId)
		))
		.apply(instance, OrganismFunction::new)
	);

	private final ResourceLocation typeId;
	private final ResourceLocation speciesId;

	private OrganismFunction(List<LootItemCondition> conditions, ResourceLocation typeId, ResourceLocation speciesId) {
		super(conditions);
		this.typeId = typeId;
		this.speciesId = speciesId;
	}

	public static LootItemConditionalFunction.Builder<?> fromDefinition(ISpeciesType<?, ?> type, ISpecies<?> species) {
		return fromId(type.id(), species.id());
	}

	public static LootItemConditionalFunction.Builder<?> fromId(ResourceLocation typeId, ResourceLocation speciesId) {
		return simpleBuilder(conditions -> new OrganismFunction(conditions, typeId, speciesId));
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext lootContext) {
		ISpeciesType<?, ?> speciesType = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(this.typeId);
		ILifeStage stage = speciesType.getLifeStage(stack);

		if (stage != null) {
			ISpecies<?> species = speciesType.getSpecies(this.speciesId);
			return species.createStack(stage);
		}

		return stack;
	}

	@Override
	public LootItemFunctionType<OrganismFunction> getType() {
		return CoreLootFunctions.ORGANISM.value();
	}
}

package forestry.core.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import forestry.api.IForestryApi;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.ISpeciesType;

/**
 * Loot function to add genetic information, an organism, to the item stack.
 */
public class OrganismFunction extends LootItemConditionalFunction {
	private final ResourceLocation typeId;
	private final ResourceLocation speciesId;

	private OrganismFunction(LootItemCondition[] conditions, ResourceLocation typeId, ResourceLocation speciesId) {
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
	public LootItemFunctionType getType() {
		return CoreLootFunctions.ORGANISM.get();
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<OrganismFunction> {
		@Override
		public void serialize(JsonObject object, OrganismFunction function, JsonSerializationContext context) {
			super.serialize(object, function, context);
			object.addProperty("type_id", function.typeId.toString());
			object.addProperty("species_id", function.speciesId.toString());
		}

		@Override
		public OrganismFunction deserialize(JsonObject object, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] conditions) {
			String typeId = GsonHelper.getAsString(object, "type_id");
			String speciesId = GsonHelper.getAsString(object, "species_id");
			return new OrganismFunction(conditions, new ResourceLocation(typeId), new ResourceLocation(speciesId));
		}
	}
}
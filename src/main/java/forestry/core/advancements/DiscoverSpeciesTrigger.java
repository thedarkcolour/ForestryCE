package forestry.core.advancements;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class DiscoverSpeciesTrigger extends SimpleCriterionTrigger<DiscoverSpeciesTrigger.TriggerInstance> {
	public static final ResourceLocation ID = new ResourceLocation("forestry", "pickup_species_trigger");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
		ResourceLocation required = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
		return new TriggerInstance(player, required);
	}

	public void trigger(@Nullable Level level, @Nullable GameProfile gp, ResourceLocation speciesID) {
		AdvancementHelper.trigger(this, level, gp, instance -> instance.check(speciesID));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation req;

		public TriggerInstance(ContextAwarePredicate player, ResourceLocation req) {
			super(ID, player);
			this.req = req;
		}

		public static TriggerInstance checkDiscovered(ResourceLocation id) {
			return new TriggerInstance(ContextAwarePredicate.ANY, id);
		}

		public boolean check(ResourceLocation speciesID) {
			return speciesID.equals(this.req);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext context) {
			JsonObject json = super.serializeToJson(context);
			json.addProperty("tag", this.req.toString());
			return json;
		}
	}

}

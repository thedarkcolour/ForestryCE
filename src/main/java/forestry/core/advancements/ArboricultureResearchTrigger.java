package forestry.core.advancements;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

//This is a clone of the Apiculture Research Trigger. Idk if it can be simplified (it almost certainly can be simplified)
public class ArboricultureResearchTrigger extends SimpleCriterionTrigger<ArboricultureResearchTrigger.TriggerInstance> {
	public static final ResourceLocation ID = new ResourceLocation("forestry", "arboriculture_research_trigger");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
		double percentage = GsonHelper.getAsDouble(json, "percent");
		return new TriggerInstance(player, percentage);
	}

	public void trigger(@Nullable Level level, @Nullable GameProfile gp, double researchCompletion) {
		AdvancementHelper.trigger(this, level, gp, instance -> instance.check(researchCompletion));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final double percentage;

		public TriggerInstance(ContextAwarePredicate player, double p) {
			super(ID, player);
			this.percentage = p;
		}

		public static TriggerInstance checkIfResearchIsGreaterThan(double p) {
			return new TriggerInstance(ContextAwarePredicate.ANY, p);
		}

		public boolean check(double amount) {
			return amount >= this.percentage;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext context) {
			JsonObject json = super.serializeToJson(context);
			json.addProperty("percent", this.percentage);
			return json;
		}
	}

}

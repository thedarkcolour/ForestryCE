package forestry.apiculture;

import forestry.api.apiculture.bee.BeeLifeStage;
import forestry.api.client.ForestrySprites;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.filter.FilterData;
import forestry.api.genetics.filter.IFilterRuleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public enum ApicultureFilterRuleType implements IFilterRuleType {
	BEE(ForestrySprites.ANALYZER_BEE) {
		@Override
		public boolean isValid(ItemStack stack, FilterData data) {
			return true;
		}
	},
	DRONE(ForestrySprites.ANALYZER_DRONE) {
		@Override
		public boolean isValid(ItemStack stack, FilterData data) {
			return data.stage() == BeeLifeStage.DRONE;
		}
	},
	PRINCESS(ForestrySprites.ANALYZER_PRINCESS) {
		@Override
		public boolean isValid(ItemStack stack, FilterData data) {
			return data.stage() == BeeLifeStage.PRINCESS;
		}
	},
	QUEEN(ForestrySprites.ANALYZER_QUEEN) {
		@Override
		public boolean isValid(ItemStack stack, FilterData data) {
			return data.stage() == BeeLifeStage.QUEEN;
		}
	};

	private final String id;
	private final ResourceLocation sprite;

	ApicultureFilterRuleType(ResourceLocation sprite) {
		this.id = "forestry.apiculture." + name().toLowerCase(Locale.ENGLISH);
		this.sprite = sprite;
	}

	@Override
	public ResourceLocation getSprite() {
		return this.sprite;
	}

	@Override
	public ResourceLocation getSpeciesTypeId() {
		return ForestrySpeciesTypes.BEE;
	}

	@Override
	public String getId() {
		return this.id;
	}
}

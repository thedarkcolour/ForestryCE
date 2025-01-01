package forestry.compat.kubejs.event;

import net.minecraft.resources.ResourceLocation;

import forestry.api.client.arboriculture.ILeafSprite;
import forestry.api.client.arboriculture.ILeafTint;
import forestry.api.client.plugin.IClientRegistration;
import forestry.api.genetics.ILifeStage;

import dev.latvian.mods.kubejs.event.EventJS;

public class ForestryClientEventJS extends EventJS {
	private final IClientRegistration wrapped;

	public ForestryClientEventJS(IClientRegistration wrapped) {
		this.wrapped = wrapped;
	}

	public void setDefaultBeeModel(ILifeStage stage, ResourceLocation modelLocation) {
		this.wrapped.setDefaultBeeModel(stage, modelLocation);
	}

	public void setCustomBeeModel(ResourceLocation speciesId, ILifeStage stage, ResourceLocation model) {
		this.wrapped.setCustomBeeModel(speciesId, stage, model);
	}

 	public void setSaplingModel(ResourceLocation speciesId, ResourceLocation blockModel, ResourceLocation itemModel) {
		this.wrapped.setSaplingModel(speciesId, blockModel, itemModel);
	}

	public void setLeafSprite(ResourceLocation speciesId, ILeafSprite sprite) {
		this.wrapped.setLeafSprite(speciesId, sprite);
	}

	public void setLeafTint(ResourceLocation speciesId, ILeafTint tint) {
		this.wrapped.setLeafTint(speciesId, tint);
	}

	public void setButterflySprites(ResourceLocation speciesId, ResourceLocation itemTexture, ResourceLocation entityTexture) {
		this.wrapped.setButterflySprites(speciesId, itemTexture, entityTexture);
	}
}

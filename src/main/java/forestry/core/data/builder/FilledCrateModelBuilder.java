package forestry.core.data.builder;

import com.google.gson.JsonObject;
import forestry.api.ForestryConstants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class FilledCrateModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
	private static final ResourceLocation ID = ForestryConstants.forestry("forestry:filled_crate");

	@Nullable
	private ResourceLocation layer1;
	@Nullable
	private ResourceLocation layer2;

	public FilledCrateModelBuilder(T parent, ExistingFileHelper helper) {
		super(ID, parent, helper);
	}

	public static <T extends ModelBuilder<T>> FilledCrateModelBuilder<T> begin(T parent, ExistingFileHelper helper) {
		return new FilledCrateModelBuilder<>(parent, helper);
	}

	public FilledCrateModelBuilder<T> layer1(ResourceLocation texture) {
		this.layer1 = texture;
		return this;
	}

	public FilledCrateModelBuilder<T> layer2(ResourceLocation texture) {
		this.layer2 = texture;
		return this;
	}

	// doing it this way skips the NPEs from regular textures, helpful for debugging
	@Override
	public JsonObject toJson(JsonObject json) {
		json = super.toJson(json);
		JsonObject textures = new JsonObject();
		textures.addProperty("layer1", this.layer1.toString());
		if (this.layer2 != null) {
			textures.addProperty("layer2", this.layer2.toString());
		}
		json.add("textures", textures);
		return json;
	}
}

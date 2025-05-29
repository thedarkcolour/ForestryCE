package forestry.arboriculture.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class SaplingModelLoader implements IGeometryLoader<ModelSapling> {
	@Override
	public ModelSapling read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
		return new ModelSapling();
	}
}

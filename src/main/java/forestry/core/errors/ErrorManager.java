package forestry.core.errors;

import com.google.common.collect.ImmutableMap;
import forestry.api.core.IError;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorManager;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ErrorManager implements IErrorManager {
	// Numeric IDs should be used only for network synchronization
	private final Short2ObjectOpenHashMap<IError> byNumericId;
	private final Object2ShortOpenHashMap<IError> numericIdLookup;
	private final ImmutableMap<ResourceLocation, IError> byId;

	public ErrorManager(Short2ObjectOpenHashMap<IError> byNumericId, Object2ShortOpenHashMap<IError> numericIdLookup, ImmutableMap<ResourceLocation, IError> byId) {
		this.byNumericId = byNumericId;
		this.numericIdLookup = numericIdLookup;
		this.byId = byId;
	}

	@Override
	public IError getError(short id) {
		return this.byNumericId.get(id);
	}

	@Nullable
	@Override
	public IError getError(ResourceLocation errorId) {
		return this.byId.get(errorId);
	}

	@Override
	public List<IError> getErrors() {
		return this.byId.values().asList();
	}

	@Override
	public IErrorLogic createErrorLogic() {
		return new ErrorLogic();
	}

	@Override
	public short getNumericId(IError error) {
		return this.numericIdLookup.getShort(error);
	}
}

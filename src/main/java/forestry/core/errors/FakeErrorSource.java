package forestry.core.errors;

import forestry.api.core.IError;
import forestry.api.core.IErrorSource;

import java.util.Set;

public enum FakeErrorSource implements IErrorSource {
	INSTANCE;

	@Override
	public Set<IError> getErrors() {
		return Set.of();
	}
}

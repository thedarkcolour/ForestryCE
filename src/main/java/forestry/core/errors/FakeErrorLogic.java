package forestry.core.errors;

import forestry.api.core.IError;
import forestry.api.core.IErrorLogic;

import java.util.Set;

public enum FakeErrorLogic implements IErrorLogic {
	INSTANCE;

	@Override
	public boolean setCondition(boolean condition, IError error) {
		return false;
	}

	@Override
	public boolean contains(IError error) {
		return false;
	}

	@Override
	public boolean hasErrors() {
		return true;
	}

	@Override
	public void clearErrors() {
	}

	@Override
	public Set<IError> getErrors() {
		return Set.of();
	}
}

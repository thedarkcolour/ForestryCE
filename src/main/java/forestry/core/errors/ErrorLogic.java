package forestry.core.errors;

import forestry.api.core.IError;
import forestry.api.core.IErrorLogic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ErrorLogic implements IErrorLogic {
	private final Set<IError> errors = new HashSet<>();

	@Override
	public boolean setCondition(boolean condition, IError error) {
		if (condition) {
			this.errors.add(error);
		} else {
			this.errors.remove(error);
		}
		return condition;
	}

	@Override
	public boolean contains(IError error) {
		return this.errors.contains(error);
	}

	@Override
	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	@Override
	public Set<IError> getErrors() {
		return Collections.unmodifiableSet(this.errors);
	}

	@Override
	public void clearErrors() {
		this.errors.clear();
	}
}

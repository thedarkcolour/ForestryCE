package forestry.core.config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SessionVars {

	private static final Map<String, String> stringVars = new HashMap<>();

	@Nullable
	private static Class<?> openedLedger;

	public static void setOpenedLedger(@Nullable Class<?> ledgerClass) {
		openedLedger = ledgerClass;
	}

	@Nullable
	public static Class<?> getOpenedLedger() {
		return openedLedger;
	}

	public static void setStringVar(String ident, String val) {
		stringVars.put(ident, val);
	}

	@Nullable
	public static String getStringVar(String ident) {
		return stringVars.get(ident);
	}

	public static void clearStringVar(String ident) {
		stringVars.remove(ident);
	}
}

package forestry.core.utils;

import net.minecraft.network.chat.Component;

import java.util.Calendar;

/**
 * Fed up with Date and Calendar and their shenanigans
 */
public record DayMonth(int day, int month) {
	public static DayMonth now() {
		Calendar calendar = Calendar.getInstance();
		return new DayMonth(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1);
	}

	public boolean between(DayMonth start, DayMonth end) {
		if (equals(start) || equals(end)) {
			return true;
		}
		if (start.month > end.month) {
			return after(start) || before(end);
		}
		return after(start) && before(end);
	}

	public boolean before(DayMonth other) {
		if (other.month > this.month) {
			return true;
		}

		if (other.month < this.month) {
			return false;
		}

		return this.day < other.day;
	}

	public boolean after(DayMonth other) {
		if (other.month < this.month) {
			return true;
		}

		if (other.month > this.month) {
			return false;
		}

		return this.day > other.day;
	}

	public Component getDisplayName() {
		return Component.translatable("forestry.date.month." + this.month, this.day);
	}
}

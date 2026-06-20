package forestry.core.multiblock.pattern;

import forestry.core.multiblock.pattern.StructureView.CellSample;

/**
 * A predicate over a single {@link CellSample}. Returns {@code null} when the cell satisfies the
 * requirement, otherwise the parity translation key describing the failure (spec §5.1) — e.g.
 * {@code "for.multiblock.alveary.error.needSlabs"}.
 *
 * <p>Returning the failure key (rather than a boolean) lets {@code validate} surface the exact same
 * error message the old engine produced, so player feedback is unchanged.
 *
 * <p>Deliberately {@code net.minecraft}-free. The returned value may be {@code null}; no external
 * nullability annotation is used here to keep the pattern layer dependency-free.
 */
@FunctionalInterface
public interface CellPredicate {
	/**
	 * @return {@code null} if the cell is acceptable, otherwise the translation key of the failure.
	 */
	String test(CellSample sample);
}

package forestry.api.apiculture;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import forestry.api.ForestryRegistries;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.core.genetics.IGenome;

/**
 * Determines whether a bee species is jubilant in a certain environment.
 */
public interface IBeeJubilance {
	/**
	 * Used to read a jubilance from a datapack file in the {@code bee_jubilance} folder.
	 * The {@code "type"} field names an entry of {@link ForestryRegistries#BEE_JUBILANCE_TYPE}.
	 */
	Codec<IBeeJubilance> CODEC = ForestryRegistries.BEE_JUBILANCE_TYPE.byNameCodec().dispatch("type", IBeeJubilance::codec, Function.identity());

	/**
	 * Used to write this jubilance to a datapack file. A jubilance that is only registered in code does not
	 * need to override this.
	 *
	 * @return The codec registered for this jubilance in {@link ForestryRegistries#BEE_JUBILANCE_TYPE}
	 */
	default MapCodec<? extends IBeeJubilance> codec() {
		throw new UnsupportedOperationException(getClass().getName() + " cannot be written to a datapack");
	}

	/**
	 * Returns true when conditions are right to make this species Jubilant.
	 * Jubilant bees can produce their Specialty products.
	 */
	boolean isJubilant(IBeeSpecies species, IGenome genome, IBeeHousing housing);
}

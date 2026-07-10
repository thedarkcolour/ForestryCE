package forestry.gametest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

import forestry.api.apiculture.ForestryBeeJubilances;
import forestry.api.arboriculture.ITreeSpecies;
import forestry.api.core.HumidityType;
import forestry.api.core.IProduct;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.alleles.Allele;
import forestry.api.lepidopterology.genetics.IButterflySpecies;
import forestry.apiculture.genetics.BeeSpeciesDefinition;
import forestry.arboriculture.genetics.TreeSpeciesDefinition;
import forestry.lepidopterology.genetics.ButterflySpeciesDefinition;

/**
 * Fluent, default-seeded builders for the three species definitions, used by the reload / projector /
 * fallback / spawner gametests to avoid repeating 12-20-arg positional constructors. Base fields default
 * to the same values the definition codecs default to; {@code from(...)} seeds a builder from a live
 * species (copying every field the species interface exposes, leaving {@code glint=false}/
 * {@code complexity=0} as the tests do). NOT used by the {@code *DefinitionTest} codec round-trip
 * oracles, which set every field to a distinct sentinel on purpose.
 */
public final class TestSpeciesDefinitions {
	private TestSpeciesDefinitions() {
	}

	public static Bee bee(String genus, String species) {
		return new Bee(genus, species);
	}

	public static Tree tree(String genus, String species) {
		return new Tree(genus, species);
	}

	public static Tree treeFrom(ITreeSpecies s) {
		Tree b = new Tree(s.getGenusName(), s.getSpeciesName());
		b.dominant = s.isDominant();
		b.secret = s.isSecret();
		b.authority = s.getAuthority();
		b.escritoireColor = s.getEscritoireColor();
		b.temperature = s.getTemperature();
		b.humidity = s.getHumidity();
		b.rarity = s.getRarity();
		return b;
	}

	public static Butterfly butterfly(String genus, String species) {
		return new Butterfly(genus, species);
	}

	public static Butterfly butterflyFrom(IButterflySpecies s) {
		Butterfly b = new Butterfly(s.getGenusName(), s.getSpeciesName());
		b.dominant = s.isDominant();
		b.secret = s.isSecret();
		b.authority = s.getAuthority();
		b.temperature = s.getTemperature();
		b.humidity = s.getHumidity();
		b.nocturnal = s.isNocturnal();
		b.moth = s.isMoth();
		b.rarity = s.getRarity();
		b.flightDistance = s.getFlightDistance();
		b.serumColor = s.getSerumColor();
		b.spawnBiomes = Optional.ofNullable(s.getSpawnBiomes());
		b.products = s.getButterflyLoot();
		b.caterpillarProducts = s.getCaterpillarProducts();
		return b;
	}

	/** Base fields shared by all three builders, defaulted to the codec defaults. */
	private abstract static class Base {
		final String genus;
		final String species;
		boolean dominant = false;
		boolean glint = false;
		boolean secret = false;
		int complexity = 0;
		String authority = "Sengir";
		int escritoireColor = -1;
		TemperatureType temperature = TemperatureType.NORMAL;
		HumidityType humidity = HumidityType.NORMAL;
		Map<ResourceLocation, Allele<?>> genome = Map.of();

		Base(String genus, String species) {
			this.genus = genus;
			this.species = species;
		}
	}

	public static final class Bee extends Base {
		private int body = 0xffdc16;
		private int stripes = 0;
		private int outline = -1;
		private List<IProduct> products = List.of();
		private List<IProduct> specialties = List.of();
		private ResourceLocation jubilance = ForestryBeeJubilances.DEFAULT;

		private Bee(String genus, String species) {
			super(genus, species);
		}

		public Bee dominant(boolean v) { this.dominant = v; return this; }
		public Bee outline(int v) { this.outline = v; return this; }
		public Bee products(List<IProduct> v) { this.products = v; return this; }
		public Bee jubilance(ResourceLocation v) { this.jubilance = v; return this; }
		public Bee genome(Map<ResourceLocation, Allele<?>> v) { this.genome = v; return this; }

		public BeeSpeciesDefinition build() {
			return new BeeSpeciesDefinition(genus, species, dominant, glint, secret, complexity, authority,
				escritoireColor, temperature, humidity, body, stripes, outline, products, specialties, jubilance, genome);
		}
	}

	public static final class Tree extends Base {
		private float rarity = 0.0f;

		private Tree(String genus, String species) {
			super(genus, species);
		}

		public Tree escritoireColor(int v) { this.escritoireColor = v; return this; }
		public Tree genome(Map<ResourceLocation, Allele<?>> v) { this.genome = v; return this; }

		public TreeSpeciesDefinition build() {
			return new TreeSpeciesDefinition(genus, species, dominant, glint, secret, complexity, authority,
				escritoireColor, temperature, humidity, rarity, genome);
		}
	}

	public static final class Butterfly extends Base {
		private boolean nocturnal = false;
		private boolean moth = false;
		private float rarity = 0.0f;
		private float flightDistance = 5.0f;
		private int serumColor = 0;
		private Optional<net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome>> spawnBiomes = Optional.empty();
		private List<IProduct> products = List.of();
		private List<IProduct> caterpillarProducts = List.of();

		private Butterfly(String genus, String species) {
			super(genus, species);
		}

		public Butterfly rarity(float v) { this.rarity = v; return this; }
		public Butterfly genome(Map<ResourceLocation, Allele<?>> v) { this.genome = v; return this; }

		public ButterflySpeciesDefinition build() {
			return new ButterflySpeciesDefinition(genus, species, dominant, glint, secret, complexity, authority,
				escritoireColor, temperature, humidity, nocturnal, moth, rarity, flightDistance, serumColor,
				spawnBiomes, products, caterpillarProducts, genome);
		}
	}
}

package forestry.apiimpl.client.genetics;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import forestry.api.client.InteractableTextOptions;
import forestry.api.client.TextOptions;
import forestry.api.client.genetics.IAnalyzerGraphics;
import forestry.api.core.IProduct;
import forestry.api.core.ToleranceType;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpecies;
import forestry.api.genetics.alleles.AllelePair;
import forestry.api.genetics.alleles.IAllele;
import forestry.api.genetics.alleles.IChromosome;
import forestry.api.genetics.alleles.IIntegerChromosome;
import forestry.api.genetics.alleles.IValueChromosome;
import forestry.core.gui.GuiUtil;
import forestry.core.gui.PortableAnalyzerScreen;

public class AnalyzerScreenGraphics<S extends ISpecies<I>, I extends IIndividual> implements IAnalyzerGraphics<S, I> {
	private final GuiGraphics graphics;
	private final Font font;
	private final float partialTicks;
	private final int mouseX;
	private final int mouseY;
	private final IIndividual individual;
	private final IGenome genome;

	private boolean haploid;
	private int currentX;
	private int currentY;

	public AnalyzerScreenGraphics(GuiGraphics graphics, AbstractContainerScreen<?> parent, float partialTicks, int mouseX, int mouseY, I individual) {
		this.graphics = graphics;
		this.font = parent.getMinecraft().font;
		this.currentX = parent.getGuiLeft() + 12;
		this.currentY = parent.getGuiTop() + 12;
		this.partialTicks = partialTicks;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.individual = individual;
		this.genome = individual.getGenome();
	}

	@Override
	public <C extends IChromosome<A>, A extends IAllele> void drawChromosomeRow(C chromosome, @Nullable IChromosomeRowOptions<C, A> options) {
		// chromosome label
		AllelePair<A> pair = this.genome.getAllelePair(chromosome);
		drawText(chromosome.getChromosomeDisplayName());

		A active = pair.active();
		InteractableTextOptions activeOptions = new InteractableTextOptions().setColor(PortableAnalyzerScreen.getColorCoding(active.dominant()));
		Component activeText = chromosome.getDisplayName(active);
		if (options != null) {
			activeText = options.apply(true, chromosome, active, activeOptions, activeText);
		}
		drawText(activeText, PortableAnalyzerScreen.COLUMN_1, activeOptions);

		if (!this.haploid) {
			A inactive = pair.inactive();
			InteractableTextOptions inactiveOptions = new InteractableTextOptions().setColor(PortableAnalyzerScreen.getColorCoding(inactive.dominant()));
			Component inactiveText = chromosome.getDisplayName(inactive);
			if (options != null) {
				inactiveText = options.apply(false, chromosome, inactive, inactiveOptions, inactiveText);
			}
			drawText(inactiveText, PortableAnalyzerScreen.COLUMN_2, inactiveOptions);
		}

		addLineSpacing(1);
	}

	@Override
	public void drawSpeciesIconsRow(@Nullable Function<S, ItemStack> iconGetter) {
		drawText(Component.translatable("for.gui.active"), PortableAnalyzerScreen.COLUMN_1);
		S active = this.individual.getSpecies().cast();

		if (iconGetter != null) {
			GuiUtil.drawItemStack(this.graphics, this.font, iconGetter.apply(active), this.currentX + PortableAnalyzerScreen.COLUMN_1 + 43, this.currentY - 2);
		}

		if (!this.haploid) {
			S inactive = this.individual.getInactiveSpecies().cast();
			drawText(Component.translatable("for.gui.inactive"), PortableAnalyzerScreen.COLUMN_2);

			if (iconGetter != null) {
				GuiUtil.drawItemStack(this.graphics, this.font, iconGetter.apply(inactive), this.currentX + PortableAnalyzerScreen.COLUMN_2 + 43, this.currentY - 2);
			}
		}

		addLineSpacing(2);
	}

	@Override
	public void drawFertilityRow(IIntegerChromosome chromosome, ResourceLocation offspringSprite) {
		drawChromosomeRow(chromosome, (active, c, a, options, text) -> {
			if (a.value() == 0) {
				return Component.translatable("allele.forestry.fertility.0i");
			} else {
				return Component.translatable("allele.forestry.fertility.1i", a.value());
			}
		});
	}

	@Override
	public void drawClimatePreferences(IValueChromosome<ToleranceType> temperatureTolerance, IValueChromosome<ToleranceType> humidityTolerance) {

	}

	@Override
	public void drawProductList(Function<S, List<IProduct>> getProducts) {

	}

	@Override
	public void drawText(Component text, int x, @Nullable InteractableTextOptions options) {
		int color = 0xffffff;
		boolean dropShadow = false;

		int minX = this.currentX + x;
		int minY = this.currentY;

		if (options != null) {
			InteractableTextOptions.OnHover hover = options.onHover();
			if (hover != null) {
				int maxX = minX + this.font.width(text) - 1;

				if (minX <= this.mouseX && this.mouseX < maxX) {
					int maxY = minY + this.font.lineHeight;

					if (minY <= this.mouseY && this.mouseY < maxY) {
						hover.onHover(this.mouseX, this.mouseY);
					}
				}
			}

			color = options.color();
			dropShadow = options.dropShadow();
			text = options.transform(text);
		}

		this.graphics.drawString(this.font, text, minX, minY, color, dropShadow);
	}

	@Override
	public void drawTooltip(int x, int y, List<Component> tooltip, @Nullable TextOptions options) {
		this.graphics.renderTooltip(this.font, tooltip, Optional.empty(), x, y);
	}

	@Override
	public int center(Component text) {
		return 100 - this.font.width(text) / 2;
	}

	@Override
	public void addHorizontalSpacing(int x) {
		this.currentX += x;
	}

	@Override
	public void addVerticalSpacing(int y) {
		this.currentY += y;
	}

	@Override
	public void addLineSpacing(int lines) {
		this.currentY += 12 * lines;
	}

	@Override
	public void setHaploid(boolean haploid) {
		this.haploid = haploid;
	}
}

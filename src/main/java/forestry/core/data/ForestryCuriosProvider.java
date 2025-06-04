package forestry.core.data;

import forestry.api.ForestryConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class ForestryCuriosProvider extends CuriosDataProvider {
	public ForestryCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
		super(ForestryConstants.MOD_ID, output, fileHelper, registries);
	}

	@Override
	public void generate(HolderLookup.Provider provider, ExistingFileHelper existingFileHelper) {
		createSlot("head")
			.addCosmetic(true);

		createEntities("slots")
			.addPlayer()
			.addSlots("head");
	}
}

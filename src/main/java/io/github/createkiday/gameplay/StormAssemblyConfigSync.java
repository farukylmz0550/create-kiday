package io.github.createkiday.gameplay;

import io.github.createkiday.CreateKidayMod;
import io.github.createkiday.KidayServerConfig;
import io.github.createkiday.registry.ModItems;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = CreateKidayMod.MOD_ID)
public final class StormAssemblyConfigSync {
    private static final ResourceLocation RECIPE = ResourceLocation.fromNamespaceAndPath(CreateKidayMod.MOD_ID, "storm_mechanism");

    private StormAssemblyConfigSync() {}

    @SubscribeEvent
    public static void applyConfiguredChance(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        if (server == null) return;
        var holder = server.getRecipeManager().byKey(RECIPE).orElse(null);
        if (holder == null || !(holder.value() instanceof SequencedAssemblyRecipe recipe)) return;
        float chance = KidayServerConfig.STORM_ASSEMBLY_CHANCE.get().floatValue();
        for (int i = 0; i < recipe.resultPool.size(); i++) {
            ProcessingOutput output = recipe.resultPool.get(i);
            if (!output.getStack().is(ModItems.STORM_MECHANISM.get())) continue;
            recipe.resultPool.set(i, new ProcessingOutput(output.getStack(), chance));
        }
    }
}

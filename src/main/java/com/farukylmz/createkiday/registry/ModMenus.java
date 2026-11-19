package com.farukylmz.createkiday.registry;

import com.farukylmz.createkiday.CreateKidayMod;
import com.farukylmz.createkiday.menu.TagFilterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CreateKidayMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TagFilterMenu>> TAG_FILTER =
            MENUS.register("tag_filter", () -> IMenuTypeExtension.create(ModMenus::createTagFilterMenu));

    private ModMenus() {}

    private static TagFilterMenu createTagFilterMenu(int id, net.minecraft.world.entity.player.Inventory inventory,
                                                       net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        return new TagFilterMenu(TAG_FILTER.get(), id, inventory, buffer);
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}

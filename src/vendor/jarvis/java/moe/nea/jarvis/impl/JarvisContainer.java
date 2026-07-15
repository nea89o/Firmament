package moe.nea.jarvis.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import moe.nea.jarvis.api.Jarvis;
import moe.nea.jarvis.api.JarvisHud;
import moe.nea.jarvis.api.JarvisPlugin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JarvisContainer extends Jarvis {
    public List<JarvisPlugin> plugins = new ArrayList<>();
    public LoaderSupport loaderSupport;
    public KeyMapping hudKeyBinding = new KeyMapping("key.jarvis.open-gui-editor", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT, new KeyMapping.Category(Identifier.fromNamespaceAndPath("jarvis", "keys")));

    public LoaderSupport getLoaderSupport() {
        return loaderSupport;
    }

    public static JarvisContainer init(LoaderSupport loaderSupport) {
        JarvisContainer jarvisContainer = new JarvisContainer();
        jarvisContainer.loaderSupport = loaderSupport;
        return jarvisContainer;
    }


    @Override
    public @NotNull Stream<@NotNull JarvisPlugin> getAllPlugins() {
        return plugins.stream();
    }

    @Override
    public @Unmodifiable @NotNull Map<@NotNull Identifier, @NotNull JarvisHud> getIndexedHuds() {
        if (indexedHuds != null)
            return indexedHuds;
        return indexedHuds = getAllHuds().collect(Collectors.toMap(JarvisHud::getHudId, Function.identity()));
    }

    @Override
    public @NotNull Stream<@NotNull JarvisHud> getAllHuds() {
        return plugins.stream().flatMap(it -> it.getAllHuds().stream());
    }

    public @NotNull Stream<@NotNull JarvisHud> getAllEnabledHuds() {
        return getAllHuds().filter(JarvisHud::isEnabled);
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen) {
        return getHudEditor(lastScreen, getAllEnabledHuds());
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull List<@NotNull JarvisHud> hudList) {
        return new JarvisHudEditor(lastScreen, hudList, this);
    }

    @Override
    public @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull BiPredicate<@NotNull JarvisPlugin, @NotNull JarvisHud> hudFilter) {
        return getHudEditor(lastScreen, getAllPlugins().flatMap(plugin -> plugin.getAllHuds().stream().filter(it -> hudFilter.test(plugin, it))));
    }

    private @NotNull JarvisHudEditor getHudEditor(@Nullable Screen lastScreen, @NotNull Stream<@NotNull JarvisHud> rStream) {
        return getHudEditor(lastScreen, rStream.collect(Collectors.toList()));
    }

    public @Nullable Map<@NotNull Identifier, @NotNull JarvisHud> indexedHuds = null;

    public void finishLoading() {
        plugins.forEach(it -> it.onInitialize(this));
    }

    public <S> void registerCommands(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("jarvis")
            .then(LiteralArgumentBuilder.<S>literal("gui")
                .executes(context -> {
                    Minecraft.getInstance().submit(() -> Minecraft.getInstance().gui.setScreen(this.getHudEditor(null)));
                    return 0;
                }))
            .then(LiteralArgumentBuilder.<S>literal("options")
                .executes(context -> {
                    Minecraft.getInstance().submit(() -> Minecraft.getInstance().gui.setScreen(
                        new JarvisConfigSearch(this, null, getAllPlugins().flatMap(it -> it.getAllConfigOptions().stream()
                            .map(opt -> new ConfigOptionWithCustody(it, opt))).collect(Collectors.toList()))));
                    return 0;
                })));
    }

    public Component getModName(JarvisPlugin plugin) {
        Component name = plugin.getName();
        if (name != null) return name;
        return loaderSupport.getModName(plugin.getModId()).get();
    }

    public void hudKeyBindingPressed() {
        Minecraft.getInstance().submit(() -> Minecraft.getInstance().gui.setScreen(this.getHudEditor(null)));
    }
}

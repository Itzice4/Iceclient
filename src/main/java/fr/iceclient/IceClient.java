package fr.iceclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class IceClient implements ClientModInitializer {
    public static final String MOD_ID = "iceclient";
    public static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        // Touche : Maj droite (en bas à droite du clavier)
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.iceclient.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new IceScreen());
                }
            }
            ChestEsp.tick(client);
        });

        // Dessin du Chest ESP par-dessus le jeu
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "chest_esp"),
                ChestEsp::render
        );
    }
}

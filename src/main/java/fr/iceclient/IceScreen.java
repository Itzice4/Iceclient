package fr.iceclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class IceScreen extends Screen {

    public IceScreen() {
        super(Text.literal("Ice Client"));
    }

    private static Text label() {
        return Text.literal("Chest ESP : " + (ChestEsp.enabled ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(label(), button -> {
            ChestEsp.enabled = !ChestEsp.enabled;
            button.setMessage(label());
        }).dimensions(width / 2 - 75, 55, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 30, 0xFF55FFFF);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        // Maj droite referme le menu
        if (input.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

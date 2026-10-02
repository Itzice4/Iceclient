package fr.iceclient;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest ESP en 2D : on projette les coffres à l'écran et on dessine un cadre.
 * Comme c'est dessiné sur le HUD, il est visible à travers les murs.
 */
public class ChestEsp {
    public static boolean enabled = true;

    private static final double MAX_DIST = 64.0;
    private static final int COLOR = 0xFF55FFFF; // bleu glace

    private static List<BlockPos> chests = new ArrayList<>();
    private static int timer = 0;

    /** Scan des coffres chargés (toutes les 10 ticks). */
    public static void tick(MinecraftClient mc) {
        if (!enabled || mc.world == null || mc.player == null) {
            chests = new ArrayList<>();
            return;
        }
        if (timer-- > 0) return;
        timer = 10;

        List<BlockPos> found = new ArrayList<>();
        int r = mc.options.getViewDistance().getValue();
        ChunkPos pc = mc.player.getChunkPos();
        Vec3d eye = mc.player.getEyePos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(pc.x + dx, pc.z + dz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity) {
                        BlockPos p = be.getPos();
                        if (eye.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) <= MAX_DIST * MAX_DIST) {
                            found.add(p.toImmutable());
                        }
                    }
                }
            }
        }
        chests = found;
    }

    /** Dessin à chaque frame. */
    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!enabled || mc.world == null || mc.player == null || mc.options.hudHidden) return;
        if (!mc.options.getPerspective().isFirstPerson()) return; // première personne uniquement

        float td = tickCounter.getTickProgress(false);
        Vec3d cam = mc.player.getCameraPosVec(td);
        double yaw = Math.toRadians(mc.player.getYaw(td));
        double pitch = Math.toRadians(mc.player.getPitch(td));

        // Base de la caméra (forward / right / up)
        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);
        double rx = -Math.cos(yaw), ry = 0, rz = -Math.sin(yaw);
        double ux = ry * fz - rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy - ry * fx;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double aspect = (double) w / h;

        // Hitbox d'un coffre : 1/16 -> 15/16 en X/Z, 0 -> 14/16 en Y
        double[] xs = {0.0625, 0.9375};
        double[] ys = {0.0, 0.875};
        double[] zs = {0.0625, 0.9375};

        for (BlockPos pos : chests) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean visible = true;

            for (double ox : xs) {
                for (double oy : ys) {
                    for (double oz : zs) {
                        double dx = pos.getX() + ox - cam.x;
                        double dy = pos.getY() + oy - cam.y;
                        double dz = pos.getZ() + oz - cam.z;

                        double cx = dx * rx + dy * ry + dz * rz;
                        double cy = dx * ux + dy * uy + dz * uz;
                        double cz = dx * fx + dy * fy + dz * fz;

                        if (cz < 0.1) { visible = false; break; } // derrière la caméra

                        double sx = (cx / (cz * tanHalf * aspect) + 1.0) / 2.0 * w;
                        double sy = (1.0 - cy / (cz * tanHalf)) / 2.0 * h;
                        minX = Math.min(minX, sx); maxX = Math.max(maxX, sx);
                        minY = Math.min(minY, sy); maxY = Math.max(maxY, sy);
                    }
                    if (!visible) break;
                }
                if (!visible) break;
            }
            if (!visible) continue;
            if (maxX < 0 || minX > w || maxY < 0 || minY > h) continue; // hors écran

            int x1 = (int) Math.floor(minX), y1 = (int) Math.floor(minY);
            int x2 = (int) Math.ceil(maxX),  y2 = (int) Math.ceil(maxY);

            // Cadre 1px
            ctx.fill(x1, y1, x2, y1 + 1, COLOR);
            ctx.fill(x1, y2 - 1, x2, y2, COLOR);
            ctx.fill(x1, y1, x1 + 1, y2, COLOR);
            ctx.fill(x2 - 1, y1, x2, y2, COLOR);

            // Distance
            int dist = (int) cam.distanceTo(Vec3d.ofCenter(pos));
            ctx.drawText(mc.textRenderer, dist + "m", x1, y1 - 10, COLOR, true);
        }
    }
}

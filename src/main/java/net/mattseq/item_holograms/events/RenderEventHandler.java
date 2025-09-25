package net.mattseq.item_holograms.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mattseq.item_holograms.ItemHolograms;
import net.mattseq.item_holograms.ItemHologramsConfig;
import net.mattseq.item_holograms.ItemLabelCache;
import net.mattseq.item_holograms.util.BubbleRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderEventHandler {
    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Camera camera = mc.gameRenderer.getMainCamera();

        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        long gameTime = mc.level.getGameTime();

        Vec3 camPos = camera.getPosition();

        Frustum frustum = event.getFrustum();
        frustum.prepare(camPos.x, camPos.y, camPos.z);

        assert player != null;
        List<ItemEntity> items = player.level().getEntitiesOfClass(
            ItemEntity.class,
            player.getBoundingBox().inflate(ItemHologramsConfig.LABEL_DISPLAY_DISTANCE.get())
        );

        for (ItemEntity item : items) {
            // skip items outside of frustum
            if (!frustum.isVisible(item.getBoundingBox())) continue;


            // skip items in minimal mode if not looked at
            boolean minimal = ItemHologramsConfig.MINIMAL.get();
            if (minimal && !getRaycastItem(item)) {
                // In minimal mode, skip rendering if not looking at the item
                continue;
            }

            ItemLabelCache.CachedLabel cached = ItemLabelCache.get(item.getId());
            ItemStack stack = item.getItem();
            boolean updateLabel = false;
            boolean updateLOS = false;

            // Determine if label needs rebuild (only when stack changes or first time)
            if (cached == null) {
                updateLabel = true;
                updateLOS = true;
            } else if (!ItemStack.isSameItemSameTags(cached.lastStack, stack) || cached.lastStack.getCount() != stack.getCount()) {
                updateLabel = true;
            }

            // Determine if LOS needs refresh (throttled)
            if (cached == null || (gameTime - cached.lastUpdate) > 10) {
                updateLOS = true;
            }

            // Rebuild label if needed
            if (updateLabel) {

                // get name and attach number of item
                MutableComponent comp = item.getItem().getHoverName().copy();

                // apply rarity color to name
                ChatFormatting rarityFmt = item.getItem().getRarity().color;
                comp = comp.withStyle(rarityFmt);

                // add count of items
                int count = item.getItem().getCount();
                if (count > 1) {
                    comp = comp.append(Component.literal(" x" + count).withStyle(ChatFormatting.WHITE));
                }

                if (cached == null) {
                    cached = new ItemLabelCache.CachedLabel(comp, stack, true, gameTime);
                    ItemLabelCache.put(item.getId(), cached);
                } else {
                    cached.label = comp;
                    cached.lastStack = stack.copy();
                }
            }

            // Recalculate LOS if needed
            if (updateLOS) {

                Vec3 targetPos = item.getBoundingBox().getCenter();
                HitResult result = mc.level.clip(new ClipContext(
                        camPos,
                        targetPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                ));
                cached.visibleLOS = (result.getType() != HitResult.Type.BLOCK);
                cached.lastUpdate = gameTime;
            }

            if (!cached.visibleLOS) continue;

            poseStack.pushPose();

            // hovering effect like the drops themselves
            if (ItemHologramsConfig.ENABLE_HOVER_ANIMATION.get())  {
                double yOffset = Math.sin(System.currentTimeMillis() / 500.0) * 0.1;
                poseStack.translate(0, yOffset, 0);
            }

            // move to item position
            Vec3 itemPos = item.position().subtract(camPos);
            poseStack.translate(itemPos.x, itemPos.y + 1, itemPos.z);

            // rotate to face player
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

            // scale based on distance
            double distance = player.distanceTo(item);
            float scale = (float) (0.05 * (distance / 16));
            scale = (float) Math.min(scale, 0.05);
            poseStack.scale(-scale, -scale, scale);


            boolean showTooltip = ItemHologramsConfig.ENABLE_TOOLTIPS.get() && getRaycastItem(item) && getTooltip(item).size() > 1;
            if (minimal) {
                showTooltip = showTooltip && player.isShiftKeyDown();
            }

            if (showTooltip) {

                List<Component> lines = getTooltip(item);

                // calculate bubble dimensions
                int padding = 1;
                int lineHeight = mc.font.lineHeight + 2;
                int bubbleWidth = 0;
                for (Component line : lines) {
                    int w = mc.font.width(line);
                    if (w > bubbleWidth) bubbleWidth = w;
                }
                bubbleWidth += padding * 2;
                int bubbleHeight = lines.size() * lineHeight + padding * 2;

                // render bubble behind all lines
                BubbleRenderer.renderBubble(
                    poseStack,
                    -(bubbleWidth / 2),
                    -bubbleHeight / 2,
                    bubbleWidth,
                    bubbleHeight,
                    ResourceLocation.fromNamespaceAndPath(ItemHolograms.MODID, "textures/gui/tooltip.png"),
                    padding
                );

                // draw all lines
                float y = (float) -bubbleHeight / 2 + padding;
                float x = (float) -bubbleWidth / 2 + padding;
                for (Component line : lines) {
                    mc.font.drawInBatch(
                        line,
                        x,
                        y,
                        0xFFFFFF,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        15728880
                    );
                    buffer.endBatch();
                    y += lineHeight;
                }

                poseStack.popPose();
            } else {
                int textWidth = mc.font.width(cached.label);

                float x = -textWidth / 2f;
                float y = 0;

                mc.font.drawInBatch(
                        cached.label,
                        x,
                        y,
                        0xFFFFFF,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        15728880
                );


                poseStack.popPose();
                buffer.endBatch();
            }


        }

        buffer.endBatch();
    }

    // NOT USED
    private static void drawBox(PoseStack poseStack, float x1, float y1, float x2, float y2, int argbColor) {
        var matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float a = ((argbColor >> 24) & 0xFF) / 255.0F;
        float r = ((argbColor >> 16) & 0xFF) / 255.0F;
        float g = ((argbColor >> 8) & 0xFF) / 255.0F;
        float b = (argbColor & 0xFF) / 255.0F;

        buffer.vertex(matrix, x1, y2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y1, 0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();
    }

    private static boolean getRaycastItem(ItemEntity item) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;

        float pt = mc.getFrameTime();
        Vec3 eye = player.getEyePosition(pt);
        Vec3 look = player.getViewVector(pt);
        double reach = ItemHologramsConfig.LABEL_DISPLAY_DISTANCE.get();

        Vec3 end = eye.add(look.scale(reach));

        EntityHitResult result = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eye,
                end,
                player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D),
                e -> e == item
        );

        return result != null && result.getEntity() == item;
    }

    public static List<Component> getTooltip(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();

        Minecraft mc = Minecraft.getInstance();
        TooltipFlag flag = mc.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED
                : TooltipFlag.Default.NORMAL;

        return stack.getTooltipLines(mc.player, flag);
    }
}

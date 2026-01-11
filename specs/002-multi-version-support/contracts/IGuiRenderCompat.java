package fr.loudo.narrativecraft.compat.api;

import net.minecraft.client.gui.Font;

/**
 * GUI rendering compatibility layer for version-specific graphics APIs.
 *
 * Abstracts the differences between:
 * - MC 1.19.x: DrawableHelper / MatrixStack
 * - MC 1.20.x+: GuiGraphics / DrawContext
 *
 * The 'graphics' parameter is the version-specific graphics context:
 * - 1.19.x: MatrixStack + BufferSource (wrapped)
 * - 1.20.x Fabric: DrawContext
 * - 1.20.x+ NeoForge: GuiGraphics
 *
 * Usage:
 *   IGuiRenderCompat compat = adapter.getGuiRenderCompat();
 *   compat.fill(graphics, x1, y1, x2, y2, color);
 */
public interface IGuiRenderCompat {

    // ===== Basic Shapes =====

    /**
     * Fill a rectangle with a solid color.
     *
     * @param graphics Version-specific graphics context
     * @param x1 Left X coordinate
     * @param y1 Top Y coordinate
     * @param x2 Right X coordinate
     * @param y2 Bottom Y coordinate
     * @param color ARGB color value
     */
    void fill(Object graphics, int x1, int y1, int x2, int y2, int color);

    /**
     * Fill a rectangle with a gradient.
     *
     * @param graphics Version-specific graphics context
     * @param x1 Left X coordinate
     * @param y1 Top Y coordinate
     * @param x2 Right X coordinate
     * @param y2 Bottom Y coordinate
     * @param colorFrom Start ARGB color
     * @param colorTo End ARGB color
     */
    void fillGradient(Object graphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo);

    // ===== Text Rendering =====

    /**
     * Draw text with shadow.
     *
     * @param graphics Version-specific graphics context
     * @param font The font to use
     * @param text Text to render
     * @param x X coordinate
     * @param y Y coordinate
     * @param color ARGB color value
     */
    void drawString(Object graphics, Font font, String text, int x, int y, int color);

    /**
     * Draw text without shadow.
     *
     * @param graphics Version-specific graphics context
     * @param font The font to use
     * @param text Text to render
     * @param x X coordinate
     * @param y Y coordinate
     * @param color ARGB color value
     */
    void drawStringNoShadow(Object graphics, Font font, String text, int x, int y, int color);

    /**
     * Draw centered text with shadow.
     *
     * @param graphics Version-specific graphics context
     * @param font The font to use
     * @param text Text to render
     * @param centerX Center X coordinate
     * @param y Y coordinate
     * @param color ARGB color value
     */
    void drawCenteredString(Object graphics, Font font, String text, int centerX, int y, int color);

    // ===== Scissoring =====

    /**
     * Enable scissor region (clip rendering to rectangle).
     *
     * @param graphics Version-specific graphics context
     * @param x1 Left X coordinate
     * @param y1 Top Y coordinate
     * @param x2 Right X coordinate
     * @param y2 Bottom Y coordinate
     */
    void enableScissor(Object graphics, int x1, int y1, int x2, int y2);

    /**
     * Disable scissor region.
     *
     * @param graphics Version-specific graphics context
     */
    void disableScissor(Object graphics);

    // ===== Texture/Sprite Rendering =====

    /**
     * Blit a texture region.
     *
     * @param graphics Version-specific graphics context
     * @param texture Texture resource location
     * @param x Destination X
     * @param y Destination Y
     * @param u Texture U coordinate
     * @param v Texture V coordinate
     * @param width Width to draw
     * @param height Height to draw
     * @param textureWidth Full texture width
     * @param textureHeight Full texture height
     */
    void blit(Object graphics, Object texture, int x, int y, float u, float v,
              int width, int height, int textureWidth, int textureHeight);

    // ===== Matrix Stack Access =====

    /**
     * Push the matrix stack (for transformations).
     *
     * @param graphics Version-specific graphics context
     */
    void pushPose(Object graphics);

    /**
     * Pop the matrix stack.
     *
     * @param graphics Version-specific graphics context
     */
    void popPose(Object graphics);

    /**
     * Translate the matrix stack.
     *
     * @param graphics Version-specific graphics context
     * @param x X translation
     * @param y Y translation
     * @param z Z translation
     */
    void translate(Object graphics, double x, double y, double z);

    /**
     * Scale the matrix stack.
     *
     * @param graphics Version-specific graphics context
     * @param x X scale
     * @param y Y scale
     * @param z Z scale
     */
    void scale(Object graphics, float x, float y, float z);

    // ===== Color Utilities =====

    /**
     * Convert RGB to ARGB with full opacity.
     * Needed because MC 1.21.6+ uses ARGB instead of RGB.
     *
     * @param rgb RGB color (0xRRGGBB)
     * @return ARGB color (0xFFRRGGBB)
     */
    default int toARGB(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /**
     * Convert ARGB to RGB (strips alpha).
     *
     * @param argb ARGB color (0xAARRGGBB)
     * @return RGB color (0xRRGGBB)
     */
    default int toRGB(int argb) {
        return argb & 0xFFFFFF;
    }
}

/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.narrative.story.inkAction.text;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import fr.loudo.narrativecraft.util.Easing;
import fr.loudo.narrativecraft.util.FadeState;
import fr.loudo.narrativecraft.util.Translation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TextInkAction extends InkAction {

    private String action;
    private int editTextCount;
    private Attribute attribute;
    private final DialogScrollTextInkAction dialogScrollTextInkAction;
    private int waitUntilEndTick;

    private static final double TICKS_PER_SECOND = 20.0;

    private static final Map<String, BiConsumer<TextInkAction, TextInkAction>> UPDATE_ACTIONS =
            createUpdateActionsMap();

    public TextInkAction(String id, Side side, String syntax, CommandMatcher matcher) {
        super(id, side, syntax, matcher);
        this.dialogScrollTextInkAction = new DialogScrollTextInkAction(Minecraft.getInstance(), this);
    }

    @Override
    public void tick() {
        if (tick < totalTick) {
            tick++;
        }

        if (tick == totalTick && attribute.getFadeState() != null) {
            handleFadeStateTransition();
        }

        dialogScrollTextInkAction.tick();

        if (!attribute.isNoTyping() && dialogScrollTextInkAction.isFinished()) {
            handleTypingCompletion();
        }
    }

    private void handleFadeStateTransition() {
        FadeState fadeState = attribute.getFadeState();

        switch (fadeState) {
            case FADE_IN:
                if (attribute.getStay() != -1) {
                    attribute.setFadeState(FadeState.STAY);
                    totalTick = (int) (attribute.getStay() * TICKS_PER_SECOND);
                    tick = 0;
                }
                break;

            case STAY:
                totalTick = (int) (attribute.getOut() * TICKS_PER_SECOND);
                attribute.setFadeState(FadeState.FADE_OUT);
                tick = 0;
                break;

            case FADE_OUT:
                attribute.setFadeState(null);
                if (!attribute.noRemove()) {
                    isRunning = false;
                }
                if (dialogScrollTextInkAction.isBlock()) {
                    blockEndTask.run();
                }
                break;
        }
    }

    private void handleTypingCompletion() {
        waitUntilEndTick++;

        if (waitUntilEndTick >= dialogScrollTextInkAction.getEndAt()) {
            waitUntilEndTick = 0;
            FadeState fadeState = attribute.getFadeState();

            if (fadeState != FadeState.FADE_OUT) {
                if (fadeState == FadeState.STAY && tick >= totalTick) {
                    attribute.setFadeState(FadeState.FADE_OUT);
                } else {
                    isRunning = false;
                    blockEndTask.run();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        if (!attribute.isRender()) return;
        dialogScrollTextInkAction.render(guiGraphics, partialTick, applyFade(partialTick));
    }

    private double applyFade(float partialTick) {
        double originalOpacity = attribute.getOpacity();
        FadeState fadeState = attribute.getFadeState();

        if (fadeState == FadeState.STAY || fadeState == null) {
            return originalOpacity;
        }

        double t = Mth.clamp((tick + partialTick) / totalTick, 0.0, 1.0);

        return switch (fadeState) {
            case FADE_IN -> Mth.lerp(t, 0.0, originalOpacity);
            case FADE_OUT -> Mth.lerp(t, originalOpacity, 0.0);
            default -> originalOpacity;
        };
    }

    @Override
    protected InkActionResult doValidate(List<String> arguments, Scene scene) {
        if (arguments.size() == 1) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Text id"));
        }
        String id = arguments.get(1).toLowerCase();
        attribute = new Attribute(id, "");
        if (arguments.size() == 2) {
            return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Attribute (create, font...)"));
        }
        attribute.setNoRemove(InkActionUtil.getOptionalArgument(command, "no-remove"));
        action = arguments.get(2);
        switch (action) {
            case "create" -> {
                if (InkActionUtil.getOptionalArgument(command, "no-drop-shadow")) {
                    attribute.setDropShadow(false);
                }
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Text"));
                }
                attribute.setText(arguments.get(3));
                dialogScrollTextInkAction.setMuteSound(true);
                Minecraft.getInstance().execute(() -> {
                    dialogScrollTextInkAction.setText(arguments.get(3));
                    dialogScrollTextInkAction.forceFinish();
                });
                attribute.setRender(true);
                attribute.setNoTyping(true);
                if (arguments.size() == 4) return InkActionResult.ok();
                try {
                    attribute.setColor(Integer.parseInt(arguments.get(4), 16));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_COLOR, arguments.get(4)));
                }
            }
            case "remove" -> {
                return InkActionResult.ok();
            }
            case "edit" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Text"));
                }
                attribute.setText(arguments.get(3));
                Minecraft.getInstance().execute(() -> dialogScrollTextInkAction.setText(arguments.get(3)));
            }
            case "position", "pos" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Position value"));
                }
                try {
                    attribute.setPosition(Position.valueOf(arguments.get(3).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Not a valid position"));
                }
            }
            case "color" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Color value"));
                }
                try {
                    attribute.setColor(Integer.parseInt(arguments.get(3), 16));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_COLOR, arguments.get(3)));
                }
            }
            case "spacing" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "X spacing value"));
                }
                float[] spacing = new float[2];
                try {
                    spacing[0] = Float.parseFloat(arguments.get(3));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
                if (arguments.size() == 4) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Y spacing value"));
                }
                try {
                    spacing[1] = Float.parseFloat(arguments.get(4));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
                }
                attribute.setSpacing(spacing);
            }
            case "width" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Width value"));
                }
                try {
                    attribute.setWidth(Integer.parseInt(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
            }
            case "opacity" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Opacity value"));
                }
                try {
                    attribute.setOpacity(Double.parseDouble(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
            }
            case "fade" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade in value"));
                }
                try {
                    attribute.setIn(Double.parseDouble(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
                if (arguments.size() == 4) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Stay value"));
                }
                try {
                    attribute.setStay(Double.parseDouble(arguments.get(4)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
                }
                if (arguments.size() == 5) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade out value"));
                }
                try {
                    attribute.setOut(Double.parseDouble(arguments.get(5)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(5)));
                }
                totalTick = (int) (attribute.getIn() * 20.0);
                attribute.setFadeState(FadeState.FADE_IN);
            }
            case "fadein" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade in value"));
                }
                try {
                    attribute.setIn(Double.parseDouble(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
                attribute.setStay(-1);
                totalTick = (int) (attribute.getIn() * 20.0);
                attribute.setFadeState(FadeState.FADE_IN);
            }
            case "fadeout" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Fade out value"));
                }
                try {
                    attribute.setOut(Double.parseDouble(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
                totalTick = (int) (attribute.getOut() * 20.0);
                attribute.setFadeState(FadeState.FADE_OUT);
            }
            case "font" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Font value"));
                }
                attribute.setCustomFont(ResourceLocation.parse(arguments.get(3)));
            }
            case "sound" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Sound value"));
                }
                attribute.setCustomLetterSound(ResourceLocation.parse(arguments.get(3)));
            }
            case "type" -> {
                dialogScrollTextInkAction.setBlock(InkActionUtil.getOptionalArgument(command, "block"));
                if (arguments.size() > 3) {
                    try {
                        dialogScrollTextInkAction.setEndAt((int) (Double.parseDouble(arguments.get(3)) * 20.0));
                    } catch (NumberFormatException e) {
                        return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                    }
                }
                if (arguments.size() > 4) {
                    try {
                        dialogScrollTextInkAction.setTextSpeed((float) (Float.parseFloat(arguments.get(4)) * 20.0));
                    } catch (NumberFormatException e) {
                        return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(4)));
                    }
                }
            }
            case "scale" -> {
                if (arguments.size() == 3) {
                    return InkActionResult.error(Translation.message(MISS_ARGUMENT_TEXT, "Scale value"));
                }
                try {
                    attribute.setScale(Float.parseFloat(arguments.get(3)));
                } catch (NumberFormatException e) {
                    return InkActionResult.error(Translation.message(NOT_VALID_NUMBER, arguments.get(3)));
                }
            }
            case null, default -> {
                return InkActionResult.error(Translation.message(WRONG_ARGUMENT_TEXT, "Not a valid attribute"));
            }
        }
        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(PlayerSession playerSession) {
        TextInkAction textInkAction = getTextInkFromId(attribute.getId(), playerSession.getInkActions());
        if (textInkAction == null && action.equals("remove")) {
            return InkActionResult.ignored();
        }

        if (textInkAction == null && !action.equals("create")) {
            return InkActionResult.error(String.format("Text id '%s' not found!", attribute.getId()));
        }

        if (textInkAction != null && action.equals("create")) {
            return InkActionResult.error(String.format("Text id '%s' is already set!", attribute.getId()));
        }

        if (!action.equals("create")) {
            BiConsumer<TextInkAction, TextInkAction> updater = UPDATE_ACTIONS.get(action.toLowerCase());
            if (updater != null) {
                updater.accept(textInkAction, this);
            }
            isRunning = false;
            dialogScrollTextInkAction.setMuteSound(true);
            if (textInkAction.dialogScrollTextInkAction.isBlock() && action.equals("edit")) {
                return InkActionResult.block();
            }
        }

        return dialogScrollTextInkAction.isBlock() ? InkActionResult.block() : InkActionResult.ok();
    }

    private static Map<String, BiConsumer<TextInkAction, TextInkAction>> createUpdateActionsMap() {
        Map<String, BiConsumer<TextInkAction, TextInkAction>> map = new HashMap<>();

        map.put("position", (target, source) -> target.attribute.setPosition(source.attribute.getPosition()));

        map.put("pos", (target, source) -> target.attribute.setPosition(source.attribute.getPosition()));

        map.put("edit", (target, source) -> {
            target.isRunning = true;
            target.attribute.setText(source.attribute.getText());
            Minecraft.getInstance().execute(() -> {
                target.dialogScrollTextInkAction.setText(source.attribute.getText());
                target.dialogScrollTextInkAction.reset();
                if (target.attribute.isNoTyping()) {
                    target.dialogScrollTextInkAction.forceFinish();
                }
            });
            target.editTextCount++;
        });

        map.put("remove", (target, source) -> {
            target.isRunning = false;
        });

        map.put("spacing", (target, source) -> target.attribute.setSpacing(source.attribute.getSpacing()));

        map.put("width", (target, source) -> {
            target.attribute.setWidth(source.attribute.getWidth());
            Minecraft.getInstance().execute(() -> target.dialogScrollTextInkAction.setText(source.attribute.getText()));
        });

        map.put("text", (target, source) -> {
            target.attribute.setText(source.attribute.getText());
            Minecraft.getInstance().execute(() -> target.dialogScrollTextInkAction.setText(source.attribute.getText()));
        });

        map.put("font", (target, source) -> target.attribute.setCustomFont(source.attribute.getCustomFont()));

        map.put("sound", (target, source) -> {
            target.attribute.setCustomLetterSound(source.attribute.getCustomLetterSound());
            target.dialogScrollTextInkAction.setLetterSound(source.attribute.getCustomLetterSound());
        });

        map.put("color", (target, source) -> target.attribute.setColor(source.attribute.getColor()));

        map.put("opacity", (target, source) -> target.attribute.setOpacity(source.attribute.getOpacity()));

        map.put("fade", (target, source) -> {
            target.attribute.setFadeState(source.attribute.getFadeState());
            target.attribute.setIn(source.attribute.getIn());
            target.attribute.setStay(source.attribute.getStay());
            target.attribute.setOut(source.attribute.getOut());

            FadeState fadeState = source.attribute.getFadeState();
            target.totalTick = switch (fadeState) {
                case FADE_IN -> (int) (source.attribute.getIn() * TICKS_PER_SECOND);
                case STAY -> (int) (source.attribute.getStay() * TICKS_PER_SECOND);
                case FADE_OUT -> (int) (source.attribute.getOut() * TICKS_PER_SECOND);
            };
        });

        map.put("fadein", (target, source) -> {
            target.attribute.setFadeState(FadeState.FADE_IN);
            target.attribute.setIn(source.attribute.getIn());
            target.attribute.setStay(-1);
            target.totalTick = (int) (source.attribute.getIn() * TICKS_PER_SECOND);
        });

        map.put("fadeout", (target, source) -> {
            target.attribute.setFadeState(FadeState.FADE_OUT);
            target.attribute.setOut(source.attribute.getOut());
            target.tick = 0;
            target.totalTick = (int) (source.attribute.getOut() * TICKS_PER_SECOND);
        });

        map.put("type", (target, source) -> {
            target.dialogScrollTextInkAction.setTextSpeed(source.dialogScrollTextInkAction.getTextSpeed());
            target.dialogScrollTextInkAction.setBlock(source.dialogScrollTextInkAction.isBlock());
            target.setBlockEndTask(target.blockEndTask);
            target.dialogScrollTextInkAction.setEndAt(source.dialogScrollTextInkAction.getEndAt());
            target.attribute.setNoTyping(false);
            target.dialogScrollTextInkAction.setMuteSound(false);
            target.isRunning = true;
            if (!target.dialogScrollTextInkAction.isFinished() || target.editTextCount == 0) {
                Minecraft.getInstance().execute(target.dialogScrollTextInkAction::reset);
            } else {
                target.waitUntilEndTick = source.dialogScrollTextInkAction.getEndAt();
            }
            target.editTextCount++;
        });

        map.put("scale", (target, source) -> {
            target.attribute.setScale(source.attribute.getScale());
        });

        return map;
    }

    @Override
    public boolean needScene() {
        return false;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public static TextInkAction getTextInkFromId(String id, List<InkAction> inkActions) {
        for (InkAction inkAction : inkActions) {
            if (inkAction instanceof TextInkAction textInkAction
                    && textInkAction.attribute.getId().equalsIgnoreCase(id)) {
                return textInkAction;
            }
        }
        return null;
    }
}

class Attribute {

    private final String id;
    private final Font font = Minecraft.getInstance().font;
    private String text;
    private ResourceLocation customFont, customLetterSound;
    private Position position = Position.MIDDLE;
    private FadeState fadeState;
    private float[] spacing;
    private int width = 1000;
    private int color = 0xFFFFFF;
    private double opacity = 1.0;
    private float scale = 1.0f;
    private double in, stay, out;
    private boolean dropShadow = true;
    private boolean render, noTyping, noRemove;
    private Animation animation;

    public Attribute(String id, String text) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public ResourceLocation getCustomFont() {
        return customFont;
    }

    public ResourceLocation getCustomLetterSound() {
        return customLetterSound;
    }

    public void setCustomLetterSound(ResourceLocation customLetterSound) {
        this.customLetterSound = customLetterSound;
    }

    public void setCustomFont(ResourceLocation customFont) {
        this.customFont = customFont;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public FadeState getFadeState() {
        return fadeState;
    }

    public void setFadeState(FadeState fadeState) {
        this.fadeState = fadeState;
    }

    public float[] getSpacing() {
        return spacing;
    }

    public void setSpacing(float[] spacing) {
        this.spacing = spacing;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getIn() {
        return in;
    }

    public void setIn(double in) {
        this.in = in;
    }

    public double getStay() {
        return stay;
    }

    public void setStay(double stay) {
        this.stay = stay;
    }

    public double getOut() {
        return out;
    }

    public void setOut(double out) {
        this.out = out;
    }

    public boolean isDropShadow() {
        return dropShadow;
    }

    public void setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
    }

    public boolean isRender() {
        return render;
    }

    public boolean isNoTyping() {
        return noTyping;
    }

    public void setNoTyping(boolean noTyping) {
        this.noTyping = noTyping;
    }

    public boolean noRemove() {
        return noRemove;
    }

    public void setNoRemove(boolean noRemove) {
        this.noRemove = noRemove;
    }

    public void setRender(boolean render) {
        this.render = render;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }
}

record Animation(double[] offset, Type type, Easing easing, double time) {
    enum Type {
        APPEAR,
        DISAPPEAR
    }
}

enum Position {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM,
    BOTTOM_RIGHT
}

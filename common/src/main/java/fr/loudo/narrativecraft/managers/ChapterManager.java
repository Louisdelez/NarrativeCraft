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

package fr.loudo.narrativecraft.managers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;

public class ChapterManager {

    private final List<Chapter> chapters = new ArrayList<>();

    public SuggestionProvider<CommandSourceStack> getChapterSuggestions() {
        return (context, builder) -> {
            for (Chapter chapter : chapters) {
                builder.suggest(chapter.getIndex());
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSceneSuggestionsByChapter() {
        return (context, builder) -> {
            int chapterIndex = IntegerArgumentType.getInteger(context, "chapter_index");
            Chapter chapter = getChapterByIndex(chapterIndex);
            if (chapter == null) return builder.buildFuture();
            for (Scene scene : chapter.getSortedSceneList()) {
                if (scene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + scene.getName() + "\"");
                } else {
                    builder.suggest(scene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSubscenesOfScenesSuggestions() {
        return (context, builder) -> {
            PlayerSession playerSession = NarrativeCraftMod.getInstance()
                    .getPlayerSessionManager()
                    .getSessionByPlayer(context.getSource().getPlayer());
            if (playerSession == null) return builder.buildFuture();
            if (!playerSession.isSessionSet()) return builder.buildFuture();
            for (Subscene subscene : playerSession.getScene().getSubscenes()) {
                if (subscene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + subscene.getName() + "\"");
                } else {
                    builder.suggest(subscene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public void addChapter(Chapter chapter) {
        if (chapters.contains(chapter)) return;
        chapters.add(chapter);
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
    }

    public Chapter getChapterByName(String name) {
        for (Chapter chapter : chapters) {
            if (chapter.getName().equalsIgnoreCase(name)) {
                return chapter;
            }
        }
        return null;
    }

    public Chapter getChapterByIndex(int index) {
        for (Chapter chapter : chapters) {
            if (chapter.getIndex() == index) {
                return chapter;
            }
        }
        return null;
    }

    public boolean chapterExists(String name) {
        for (Chapter chapter : chapters) {
            if (chapter.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean chapterExists(int index) {
        for (Chapter chapter : chapters) {
            if (chapter.getIndex() == index) {
                return true;
            }
        }
        return false;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}

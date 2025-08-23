package fr.loudo.narrativecraft.managers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.Subscene;
import fr.loudo.narrativecraft.narrative.session.PlayerSession;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.List;

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
            if(chapter == null) return builder.buildFuture();
            for (Scene scene : chapter.getSortedSceneList()) {
                if(scene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + scene.getName() + "\"");
                } else {
                    builder.suggest(scene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public SuggestionProvider<CommandSourceStack> getSubscenesOfScenesSuggestions(PlayerSession playerSession) {
        return (context, builder) -> {
            if(!playerSession.isSessionSet()) return builder.buildFuture();
            for (Subscene subscene : playerSession.getScene().getSubscenes()) {
                if(subscene.getName().split(" ").length > 1) {
                    builder.suggest("\"" + subscene.getName() + "\"");
                } else {
                    builder.suggest(subscene.getName());
                }
            }
            return builder.buildFuture();
        };
    }

    public void addChapter(Chapter chapter) {
        if(chapters.contains(chapter)) return;
        chapters.add(chapter);
    }

    public void removeChapter(Chapter chapter) {
        chapters.remove(chapter);
    }

    public Chapter getChapterByName(String name) {
        for(Chapter chapter : chapters) {
            if(chapter.getName().equalsIgnoreCase(name)) {
                return chapter;
            }
        }
        return null;
    }

    public Chapter getChapterByIndex(int index) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == index) {
                return chapter;
            }
        }
        return null;
    }

    public boolean chapterExists(String name) {
        for(Chapter chapter : chapters) {
            if(chapter.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean chapterExists(int index) {
        for(Chapter chapter : chapters) {
            if(chapter.getIndex() == index) {
                return true;
            }
        }
        return false;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }
}

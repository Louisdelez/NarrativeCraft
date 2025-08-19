package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.chapter.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterManager {

    private final List<Chapter> chapters = new ArrayList<>();

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

    public List<Chapter> getChapters() {
        return chapters;
    }
}

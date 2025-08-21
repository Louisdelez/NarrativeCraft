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

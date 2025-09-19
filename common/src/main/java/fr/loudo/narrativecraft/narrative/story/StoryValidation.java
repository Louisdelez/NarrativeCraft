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

package fr.loudo.narrativecraft.narrative.story;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionRegistry;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFile;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.scene.Scene;
import fr.loudo.narrativecraft.util.ErrorLine;
import fr.loudo.narrativecraft.util.Translation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryValidation {

    public static final String TAG_REGEX = "#\\s*([^#]+)\\s*";

    public static List<ErrorLine> validate() throws Exception {
        List<ErrorLine> errorLines = new ArrayList<>();
        List<Chapter> chapters =
                NarrativeCraftMod.getInstance().getChapterManager().getChapters();

        for (Chapter chapter : chapters) {
            try {
                errorLines.addAll(validateChapter(chapter));
            } catch (IOException e) {
                throw new Exception(Translation.message("validation.chapter_file_no_exists", chapter.getIndex())
                        .getString());
            }

            for (Scene scene : chapter.getSortedSceneList()) {
                try {
                    errorLines.addAll(validateScene(scene));
                } catch (IOException e) {
                    throw new Exception(
                            Translation.message("validation.scene_file_no_exists", scene.getName(), chapter.getIndex())
                                    .getString());
                }
            }
        }
        return errorLines;
    }

    private static List<ErrorLine> validateChapter(Chapter chapter) throws IOException {
        return validate(
                NarrativeCraftFile.getScriptFile(chapter),
                chapter,
                chapter.getSortedSceneList().getFirst());
    }

    private static List<ErrorLine> validateScene(Scene scene) throws IOException {
        return validate(NarrativeCraftFile.getScriptFile(scene), null, scene);
    }

    private static List<ErrorLine> validate(File scriptFile, Chapter chapter, Scene scene) throws IOException {
        List<ErrorLine> errorLines = new ArrayList<>();
        Pattern tagPattern = Pattern.compile(TAG_REGEX);

        String[] lines = Files.readString(scriptFile.toPath()).split("\n");
        for (int i = 0; i < lines.length; i++) {
            String rawLine = lines[i].trim();
            if ((!rawLine.equals("# on enter") && !rawLine.equals("#on enter")) && i + 1 == 2 && chapter == null) {
                errorLines.add(new ErrorLine(
                        i + 1,
                        null,
                        scene,
                        Translation.message("validation.on_enter_required").getString(),
                        "",
                        false));
            }
            if (!rawLine.startsWith("#")) continue;
            Matcher matcher = tagPattern.matcher(rawLine);
            while (matcher.find()) {
                String command = matcher.group(1).trim();
                InkAction inkAction = InkActionRegistry.findByCommand(command);

                if (inkAction == null) continue;

                InkActionResult result = inkAction.validate(command, scene);
                if (result.isError() || result.isWarn()) {
                    errorLines.add(new ErrorLine(
                            i + 1,
                            chapter,
                            chapter == null ? scene : null,
                            result.errorMessage(),
                            command,
                            result.isWarn()));
                }
            }
        }
        return errorLines;
    }
}

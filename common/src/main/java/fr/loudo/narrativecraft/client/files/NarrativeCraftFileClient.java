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

package fr.loudo.narrativecraft.client.files;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.AbstractNarrativeCraftFile;
import fr.loudo.narrativecraft.options.NarrativeClientOption;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import net.minecraft.client.Minecraft;

public class NarrativeCraftFileClient extends AbstractNarrativeCraftFile {

    public static final String USER_OPTIONS_FILE_NAME = "user_options" + EXTENSION_DATA_FILE;

    public static File rootDirectory;

    public static void init() {
        rootDirectory = createDirectory(Minecraft.getInstance().gameDirectory, DIRECTORY_NAME);
    }

    public static void updateUserOptions(NarrativeClientOption narrativeClientOption) {
        File userOptionsFile = createFile(rootDirectory, USER_OPTIONS_FILE_NAME);
        try {
            try (Writer writer = new BufferedWriter(new FileWriter(userOptionsFile))) {
                new Gson().toJson(narrativeClientOption, writer);
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Couldn't update user options! ", e);
            }
        } catch (JsonIOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't update user options! ", e);
        }
    }

    public static NarrativeClientOption loadUserOptions() {
        File userOptionsFile = createFile(rootDirectory, USER_OPTIONS_FILE_NAME);
        try {
            String content = Files.readString(userOptionsFile.toPath());
            if (content.isEmpty()) {
                updateUserOptions(new NarrativeClientOption());
            }
            NarrativeClientOption option = new Gson().fromJson(content, NarrativeClientOption.class);
            return Objects.requireNonNullElseGet(option, NarrativeClientOption::new);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Couldn't read user options! ", e);
        }
        return null;
    }
}

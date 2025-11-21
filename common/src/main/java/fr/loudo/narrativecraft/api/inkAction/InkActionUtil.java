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

package fr.loudo.narrativecraft.api.inkAction;

import com.bladecoder.ink.runtime.Story;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InkActionUtil {

    public static final Pattern SCENE_KNOT_PATTERN = Pattern.compile("chapter_\\d+_[a-zA-Z0-9_]+");
    public static final Pattern OPTIONAL_ARGUMENT_PATTERN = Pattern.compile("--(\\S+)");
    public static final Pattern VARIABLE_NAME = Pattern.compile("%([A-Za-z0-9_]+)%");
    public static final Pattern NAME_PATTERN = Pattern.compile("\"([^\"]+)\"|(\\S+)");

    public static List<String> getArguments(String command) {
        Matcher matcher = NAME_PATTERN.matcher(command);
        List<String> arguments = new ArrayList<>();
        while (matcher.find()) {
            String matchString = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Matcher argumentMatcher = OPTIONAL_ARGUMENT_PATTERN.matcher(matchString);
            if (argumentMatcher.matches()) continue;
            if (matchString.equals("\"\"")) {
                arguments.add("");
                continue;
            }
            arguments.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return arguments;
    }

    public static double getSecondsFromTimeValue(double seconds, String timeValue) {
        double originalSeconds = seconds;
        if (timeValue.contains("second")) {
            seconds = originalSeconds;
        } else if (timeValue.contains("minute")) {
            seconds *= 60.0;
        } else if (timeValue.contains("hour")) {
            seconds *= 60.0 * 2;
        } else {
            seconds = -1;
        }
        return seconds;
    }

    public static boolean getOptionalArgument(String command, String arg) {
        Matcher matcher = OPTIONAL_ARGUMENT_PATTERN.matcher(command);
        while (matcher.find()) {
            String value = matcher.group(1);
            if (arg.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static String parseVariables(Story story, String tag) {
        if (story == null) return tag;
        Matcher matcher = VARIABLE_NAME.matcher(tag);
        while (matcher.find()) {
            Object variable = story.getVariablesState().get(matcher.group(1));
            if (variable == null) continue;
            tag = tag.replace("%" + matcher.group(1) + "%", variable.toString());
        }
        return tag;
    }
}

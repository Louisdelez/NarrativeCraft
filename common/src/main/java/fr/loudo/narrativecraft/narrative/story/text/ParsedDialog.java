package fr.loudo.narrativecraft.narrative.story.text;

import fr.loudo.narrativecraft.narrative.dialog.DialogAnimationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ParsedDialog(String cleanedText, List<TextEffect> effects, String characterName) {
    public static ParsedDialog parse(String rawText) {
        String characterName = "";
        String dialogContent = rawText;

        String[] splitText = rawText.split(":", 2);
        if(splitText.length == 2 && splitText[0].charAt(0) != ':') {
            characterName = splitText[0].trim();
            dialogContent = splitText[1].trim();
        }
        dialogContent = dialogContent.replace("::", ":");

        if (dialogContent.startsWith("\"") && dialogContent.endsWith("\"")) {
            dialogContent = dialogContent.substring(1, dialogContent.length() - 1);
        }

        List<TextEffect> effects = new ArrayList<>();
        StringBuilder cleanText = new StringBuilder();

        Pattern pattern = Pattern.compile("\\[(\\w+)((?:\\s+\\w+=[^\\]\\s]+)*?)\\](.*?)\\[/\\1\\]");
        Matcher matcher = pattern.matcher(dialogContent);

        int currentIndex = 0;

        while (matcher.find()) {
            cleanText.append(dialogContent, currentIndex, matcher.start());

            String effectName = matcher.group(1);
            String paramString = matcher.group(2).trim();
            String innerText = matcher.group(3);

            int effectStart = cleanText.length();
            cleanText.append(innerText);
            int effectEnd = cleanText.length();

            Map<String, String> params = new HashMap<>();
            if (!paramString.isEmpty()) {
                String[] parts = paramString.split("\\s+");
                for (String part : parts) {
                    String[] kv = part.split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }

            DialogAnimationType type;
            try {
                type = DialogAnimationType.valueOf(effectName.toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }

            effects.add(new TextEffect(type, effectStart, effectEnd, params));
            currentIndex = matcher.end();
        }
        dialogContent = dialogContent.replace("\n", "");
        cleanText.append(dialogContent.substring(currentIndex));

        return new ParsedDialog(cleanText.toString(), effects, characterName);
    }
}
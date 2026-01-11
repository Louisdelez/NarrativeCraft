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

package fr.loudo.narrativecraft.narrative.security;

import fr.loudo.narrativecraft.narrative.validation.ValidationError;
import fr.loudo.narrativecraft.narrative.validation.ValidationResult;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Security proxy for Minecraft command execution.
 *
 * Provides a whitelist-based approach to controlling which commands
 * can be executed from Ink scripts. This prevents narrative creators
 * from accidentally (or intentionally) executing dangerous commands.
 *
 * Security model:
 * - Commands must be explicitly whitelisted
 * - Dangerous commands are explicitly blocked
 * - Unknown commands are blocked by default
 * - Commands are validated before execution
 */
public class CommandProxy {

    /**
     * Commands that are always safe for narrative use.
     * These commands don't modify world state in dangerous ways.
     */
    private static final Set<String> WHITELISTED_COMMANDS = Set.of(
        // Visual/Audio effects (cosmetic only)
        "effect",           // Apply status effects to entities
        "particle",         // Spawn particles
        "playsound",        // Play sounds
        "stopsound",        // Stop sounds
        "title",            // Display title text
        "subtitle",         // Display subtitle text

        // Entity manipulation (for NPCs/narratives)
        "tp",               // Teleport entities
        "teleport",         // Teleport entities (alias)
        "summon",           // Summon entities
        "kill",             // Kill specific entities

        // Time/Weather (cosmetic world changes)
        "time",             // Set/query time
        "weather",          // Set weather

        // Messages
        "say",              // Broadcast message
        "tell",             // Private message
        "tellraw",          // JSON formatted message
        "msg",              // Private message (alias)

        // Item/Inventory (for rewards/story items)
        "give",             // Give items to players
        "clear",            // Clear items from inventory

        // Game mechanics
        "gamemode",         // Change gamemode
        "xp",               // Add/remove XP
        "experience",       // Add/remove XP (alias)
        "scoreboard",       // Scoreboard operations
        "tag",              // Entity tags
        "attribute",        // Entity attributes
        "spawnpoint",       // Set spawn point

        // Function execution
        "function",         // Execute data pack functions

        // Advancement
        "advancement"       // Grant/revoke advancements
    );

    /**
     * Commands that are explicitly blocked for security reasons.
     * These commands can cause severe damage or security issues.
     */
    private static final Set<String> BLOCKED_COMMANDS = Set.of(
        // Server administration (critical security risk)
        "op",               // Grant operator status
        "deop",             // Revoke operator status
        "ban",              // Ban players
        "ban-ip",           // IP ban
        "banlist",          // View ban list
        "pardon",           // Unban player
        "pardon-ip",        // Unban IP
        "kick",             // Kick players
        "stop",             // Stop server
        "save-all",         // Force world save
        "save-off",         // Disable auto-save
        "save-on",          // Enable auto-save

        // World destruction
        "fill",             // Can fill large areas (lag/griefing)
        "clone",            // Clone blocks (lag risk)
        "setblock",         // Modify blocks (griefing)

        // File system access
        "debug",            // Debug commands
        "data",             // NBT data modification
        "datapack",         // Data pack management

        // Player data modification
        "whitelist",        // Server whitelist management

        // Dangerous defaults
        "execute",          // Can execute any command (bypass)
        "spreadplayers",    // Teleport many players (abuse)
        "forceload",        // Chunk loading (performance)
        "worldborder",      // World border (can trap players)

        // Seed exposure
        "seed"              // Reveals world seed
    );

    /**
     * Pattern for validating command format.
     */
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^/?([a-zA-Z_][a-zA-Z0-9_]*).*$");

    /**
     * Custom whitelist (can be extended by configuration).
     */
    private final Set<String> customWhitelist;

    /**
     * Custom blacklist (can be extended by configuration).
     */
    private final Set<String> customBlacklist;

    public CommandProxy() {
        this.customWhitelist = new HashSet<>();
        this.customBlacklist = new HashSet<>();
    }

    /**
     * Validates a command before execution.
     *
     * @param command The full command string (with or without leading /)
     * @param storyName The story context for error reporting
     * @param sceneName The scene context for error reporting
     * @param lineNumber The line number for error reporting
     * @return ValidationResult indicating if the command is allowed
     */
    public ValidationResult validateCommand(String command, String storyName,
                                            String sceneName, int lineNumber) {
        if (command == null || command.trim().isEmpty()) {
            return ValidationResult.failure(
                ValidationError.securityViolation("command", command, storyName, sceneName, lineNumber,
                    "Empty command is not allowed")
            );
        }

        String trimmedCommand = command.trim();
        String commandName = extractCommandName(trimmedCommand);

        if (commandName == null) {
            return ValidationResult.failure(
                ValidationError.securityViolation("command", trimmedCommand, storyName, sceneName, lineNumber,
                    "Invalid command format")
            );
        }

        String lowerCommandName = commandName.toLowerCase();

        // Check explicit blocklist first (highest priority)
        if (isBlocked(lowerCommandName)) {
            return ValidationResult.failure(
                ValidationError.securityViolation("command", trimmedCommand, storyName, sceneName, lineNumber,
                    "Command '" + commandName + "' is blocked for security reasons")
            );
        }

        // Check whitelist
        if (!isWhitelisted(lowerCommandName)) {
            return ValidationResult.failure(
                ValidationError.securityViolation("command", trimmedCommand, storyName, sceneName, lineNumber,
                    "Command '" + commandName + "' is not in the allowed command list")
            );
        }

        return ValidationResult.success();
    }

    /**
     * Checks if a command is safe to execute (convenience method).
     */
    public boolean isSafeCommand(String command) {
        return validateCommand(command, "", "", 0).isValid();
    }

    /**
     * Extracts the command name from a full command string.
     */
    private String extractCommandName(String command) {
        // Remove leading slash if present
        String cleanCommand = command.startsWith("/") ? command.substring(1) : command;

        // Extract first word
        int spaceIndex = cleanCommand.indexOf(' ');
        if (spaceIndex == -1) {
            return cleanCommand;
        }
        return cleanCommand.substring(0, spaceIndex);
    }

    /**
     * Checks if a command name is whitelisted.
     */
    private boolean isWhitelisted(String commandName) {
        return WHITELISTED_COMMANDS.contains(commandName) ||
               customWhitelist.contains(commandName);
    }

    /**
     * Checks if a command name is blocked.
     */
    private boolean isBlocked(String commandName) {
        return BLOCKED_COMMANDS.contains(commandName) ||
               customBlacklist.contains(commandName);
    }

    /**
     * Adds a command to the custom whitelist.
     *
     * @param commandName The command name (without /)
     * @throws SecurityException if command is in blocked list
     */
    public void addToWhitelist(String commandName) {
        String lower = commandName.toLowerCase();
        if (BLOCKED_COMMANDS.contains(lower)) {
            throw new SecurityException("Cannot whitelist blocked command: " + commandName);
        }
        customWhitelist.add(lower);
    }

    /**
     * Adds a command to the custom blacklist.
     *
     * @param commandName The command name (without /)
     */
    public void addToBlacklist(String commandName) {
        customBlacklist.add(commandName.toLowerCase());
    }

    /**
     * Removes a command from the custom whitelist.
     */
    public void removeFromWhitelist(String commandName) {
        customWhitelist.remove(commandName.toLowerCase());
    }

    /**
     * Removes a command from the custom blacklist.
     */
    public void removeFromBlacklist(String commandName) {
        customBlacklist.remove(commandName.toLowerCase());
    }

    /**
     * Gets the set of all whitelisted commands.
     */
    public Set<String> getWhitelistedCommands() {
        Set<String> all = new HashSet<>(WHITELISTED_COMMANDS);
        all.addAll(customWhitelist);
        return Collections.unmodifiableSet(all);
    }

    /**
     * Gets the set of all blocked commands.
     */
    public Set<String> getBlockedCommands() {
        Set<String> all = new HashSet<>(BLOCKED_COMMANDS);
        all.addAll(customBlacklist);
        return Collections.unmodifiableSet(all);
    }

    /**
     * Clears custom whitelist and blacklist.
     */
    public void resetCustomLists() {
        customWhitelist.clear();
        customBlacklist.clear();
    }
}

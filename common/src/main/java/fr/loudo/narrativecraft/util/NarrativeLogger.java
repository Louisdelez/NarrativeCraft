package fr.loudo.narrativecraft.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structured logging utility for NarrativeCraft.
 *
 * Provides consistent logging with session context and subsystem tags.
 * All log messages include contextual information for easier debugging.
 *
 * Usage:
 *   NarrativeLogger.info("dialog", sessionId, "Dialog started for character: {}", characterName);
 *   NarrativeLogger.error("playback", sessionId, "Playback failed", exception);
 */
public final class NarrativeLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("NarrativeCraft");

    // Subsystem tags for structured logging
    public static final String SUBSYSTEM_DIALOG = "dialog";
    public static final String SUBSYSTEM_PLAYBACK = "playback";
    public static final String SUBSYSTEM_RECORDING = "recording";
    public static final String SUBSYSTEM_STORY = "story";
    public static final String SUBSYSTEM_VALIDATION = "validation";
    public static final String SUBSYSTEM_CLEANUP = "cleanup";
    public static final String SUBSYSTEM_STATE = "state";
    public static final String SUBSYSTEM_TRIGGER = "trigger";
    public static final String SUBSYSTEM_INK = "ink";
    public static final String SUBSYSTEM_SESSION = "session";

    private NarrativeLogger() {
        // Utility class
    }

    /**
     * Log a debug message with subsystem and session context.
     */
    public static void debug(String subsystem, String sessionId, String message, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(format(subsystem, sessionId, message), args);
        }
    }

    /**
     * Log an info message with subsystem and session context.
     */
    public static void info(String subsystem, String sessionId, String message, Object... args) {
        LOGGER.info(format(subsystem, sessionId, message), args);
    }

    /**
     * Log a warning message with subsystem and session context.
     */
    public static void warn(String subsystem, String sessionId, String message, Object... args) {
        LOGGER.warn(format(subsystem, sessionId, message), args);
    }

    /**
     * Log an error message with subsystem and session context.
     */
    public static void error(String subsystem, String sessionId, String message, Object... args) {
        LOGGER.error(format(subsystem, sessionId, message), args);
    }

    /**
     * Log an error message with exception.
     */
    public static void error(String subsystem, String sessionId, String message, Throwable throwable) {
        LOGGER.error(format(subsystem, sessionId, message), throwable);
    }

    /**
     * Log without session context (for system-level events).
     */
    public static void system(String subsystem, String message, Object... args) {
        LOGGER.info("[{}] {}", subsystem, String.format(message.replace("{}", "%s"), args));
    }

    /**
     * Log a state transition.
     */
    public static void stateTransition(String sessionId, String fromState, String toState) {
        info(SUBSYSTEM_STATE, sessionId, "State transition: {} -> {}", fromState, toState);
    }

    /**
     * Log story start.
     */
    public static void storyStart(String sessionId, String storyName, String sceneName) {
        info(SUBSYSTEM_STORY, sessionId, "Story started: {} / Scene: {}", storyName, sceneName);
    }

    /**
     * Log story end.
     */
    public static void storyEnd(String sessionId, String storyName, String reason) {
        info(SUBSYSTEM_STORY, sessionId, "Story ended: {} ({})", storyName, reason);
    }

    /**
     * Log validation error.
     */
    public static void validationError(String sessionId, String tagName, String errorMessage) {
        warn(SUBSYSTEM_VALIDATION, sessionId, "Tag validation failed: {} - {}", tagName, errorMessage);
    }

    /**
     * Log cleanup action.
     */
    public static void cleanup(String sessionId, String handlerName, boolean success) {
        if (success) {
            debug(SUBSYSTEM_CLEANUP, sessionId, "Cleanup completed: {}", handlerName);
        } else {
            warn(SUBSYSTEM_CLEANUP, sessionId, "Cleanup failed: {}", handlerName);
        }
    }

    /**
     * Format a log message with subsystem and session context.
     */
    private static String format(String subsystem, String sessionId, String message) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return String.format("[%s][%s] %s", subsystem, sessionId, message);
        }
        return String.format("[%s] %s", subsystem, message);
    }

    /**
     * Get a short session ID for logging (first 8 chars of UUID).
     */
    public static String shortSessionId(String fullSessionId) {
        if (fullSessionId == null || fullSessionId.length() < 8) {
            return fullSessionId;
        }
        return fullSessionId.substring(0, 8);
    }
}

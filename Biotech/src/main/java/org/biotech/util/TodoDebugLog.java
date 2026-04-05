package org.biotech.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Lightweight temporary debug logger.
 * TODO: Remove this class and its call sites after quick-move/merge debug is done.
 */
public final class TodoDebugLog {

    private static final Logger LOGGER = LogUtils.getLogger();

    private TodoDebugLog() {
    }

    /**
     * Enable by JVM arg: -Dbiotech.debug.todoLog=true
     */
    private static final boolean ENABLED = Boolean.getBoolean("biotech.debug.todoLog");

    public static void info(String tag, Supplier<String> message) {
        if (!ENABLED) {
            return;
        }
        LOGGER.info("[TODO-REMOVE][{}] {}", tag, message.get());
    }

    public static void info(String tag, String message) {
        if (!ENABLED) {
            return;
        }
        LOGGER.info("[TODO-REMOVE][{}] {}", tag, message);
    }

    public static void warn(String tag, Supplier<String> message) {
        if (!ENABLED) {
            return;
        }
        LOGGER.warn("[TODO-REMOVE][{}] {}", tag, message.get());
    }

    public static void warn(String tag, String message) {
        if (!ENABLED) {
            return;
        }
        LOGGER.warn("[TODO-REMOVE][{}] {}", tag, message);
    }
}


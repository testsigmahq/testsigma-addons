package com.testsigma.addons.util;

/**
 * Central configuration for which approach the text/image actions use to locate
 * content on screen.
 *
 * <ul>
 *   <li>{@link Approach#VISUAL_SERVER} — call the Testsigma visual server
 *       (OCR text-points / find-image endpoints) via {@link OCRUtils} /
 *       {@link FindImageUtils}.</li>
 *   <li>{@link Approach#AI} — use the LLM via the {@code @AI} SDK capability to
 *       reason about a screenshot directly (no visual server call).</li>
 * </ul>
 *
 * The active approach is stored in {@link #CURRENT_APPROACH}. It defaults to
 * {@link Approach#VISUAL_SERVER} and can be overridden at runtime, without a
 * recompile, via the JVM system property:
 * <pre>
 *   -Daddon.approach=AI
 *   -Daddon.approach=VISUAL_SERVER
 * </pre>
 */
public class ApproachConfig {

    public enum Approach {
        VISUAL_SERVER,
        AI
    }

    /** System property used to override the approach at runtime. */
    public static final String APPROACH_PROPERTY = "addon.approach";

    /**
     * The approach currently in use. Change this default to switch the whole
     * addon, or override it at runtime with -Daddon.approach=AI.
     */
    public static Approach CURRENT_APPROACH = resolveDefault();

    private ApproachConfig() {
    }

    private static Approach resolveDefault() {
        String override = System.getProperty(APPROACH_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            try {
                return Approach.valueOf(override.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Unknown value -> fall back to the safe default below.
            }
        }
        return Approach.VISUAL_SERVER;
    }

    public static Approach current() {
        return CURRENT_APPROACH;
    }

    public static boolean isAi() {
        return CURRENT_APPROACH == Approach.AI;
    }

    public static boolean isVisualServer() {
        return CURRENT_APPROACH == Approach.VISUAL_SERVER;
    }
}

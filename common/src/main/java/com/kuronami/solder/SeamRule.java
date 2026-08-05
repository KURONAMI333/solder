package com.kuronami.solder;

import java.util.Map;

/**
 * Decides whether the seam this mod exists to close is actually open.
 *
 * <p>The condition is not "Iris is installed" but "Sodium's own
 * {@code features.render.world.sky} mixins are switched off by another mod".
 * Two redirects on one instruction is a mixin conflict — a hard class-load
 * failure, not a double speed-up — so the guard reads the same source Sodium
 * reads when it decides which of its own mixins to apply: the
 * {@code sodium:options} block that mods declare in their metadata. Where that
 * block lives differs per loader, so each loader supplies the rules and this
 * resolves them.
 *
 * <p>Resolution mirrors Sodium's: of the rules covering the sky package, the
 * most specific one decides. A mod disabling a parent
 * ({@code mixin.features.render.world}) takes the sky package down with it, and
 * a more specific rule setting it back to true leaves Sodium's mixins in place —
 * in which case this mod must stay out of the way.
 */
public final class SeamRule {

    /** The rule Sodium consults for its sky package, in Sodium's own key form. */
    public static final String SKY = "mixin.features.render.world.sky";

    /** The metadata key mods use to declare Sodium mixin overrides. */
    public static final String SODIUM_OPTIONS = "sodium:options";

    private SeamRule() {}

    /** Accumulates rules from every mod, then answers the one question. */
    public static final class Resolver {

        private String key;
        private boolean enabled = true;

        public void offer(String candidate, boolean value) {
            if (candidate == null || !covers(candidate)) {
                return;
            }
            if (key == null || candidate.length() > key.length()) {
                key = candidate;
                enabled = value;
            }
        }

        public void offerAll(Map<?, ?> rules) {
            for (Map.Entry<?, ?> rule : rules.entrySet()) {
                if (rule.getKey() instanceof String name && rule.getValue() instanceof Boolean value) {
                    offer(name, value);
                }
            }
        }

        /** True when a mod has switched Sodium's sky mixins off. */
        public boolean seamIsOpen() {
            return key != null && !enabled;
        }

        /** The rule that decided, for the startup log. Null when none applied. */
        public String decidingRule() {
            return key;
        }
    }

    /** True when {@code key} is the sky rule itself or one of its ancestors. */
    static boolean covers(String key) {
        return SKY.equals(key) || SKY.startsWith(key + ".");
    }
}

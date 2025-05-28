/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */

package org.eolang;

/**
 * Default attribute that just keeps single object.
 * Used for tests mostly.
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 0.1
 */
@SuppressWarnings({
    "JTCOP.RuleAllTestsHaveProductionClass",
    "JTCOP.RuleCorrectTestName",
    "JTCOP.RuleInheritanceInTests"
})
final class AtSimple extends PhEnvelope {
    /**
     * Ctor.
     */
    AtSimple() {
        this(Phi.Φ);
    }

    /**
     * Ctor.
     * @param object Object that attribute keeps
     */
    AtSimple(final Phi object) {
        super(new PhComposite(object, arg -> object));
    }
}

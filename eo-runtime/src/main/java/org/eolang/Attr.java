/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */

package org.eolang;

/**
 * Attribute.
 *
 * @since 0.1
 */
public interface Attr {
<<<<<<< HEAD
    /**
     * Lambda attribute.
     */
    String LAMBDA = "λ";

    /**
     * Phi attribute.
     */
    String PHI = "φ";

    /**
     * Rho attribute.
     */
    String RHO = "ρ";
=======
>>>>>>> parent of 0ffc35622 (bug(#3480): fails)

    /**
     * Make a copy of it.
     *
     * @param self The object that this attribute will belong to
     * @return A copy
     */
    Attr copy(Phi self);

    /**
     * Take the object out.
     *
     * <p>If attribute is not set - throws {@link ExUnset}.</p>
     *
     * @return The object
     */
    Phi get();

    /**
     * Put a new object in.
     *
     * @param phi The object to put
     */
    void put(Phi phi);
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */

package org.eolang;

/**
 * The attribute that tries to copy object and set \rho to it if it has not already set.
 * @since 0.36.0
 */
final class PhiWithRho implements Phi {
    /**
     * Original attribute.
     */
    private final Phi original;

    /**
     * Rho.
     */
    private final Phi rho;

    /**
     * Ctor.
     * @param attr Attribute
     * @param rho Rho
     */
    PhiWithRho(final Phi attr, final Phi rho) {
        this.original = attr;
        this.rho = rho;
    }

    /**
     * Returns the original attribute.
     * @return The original attribute
     */
    Phi origin() {
        return this.original;
    }

    @Override
    public Phi copy() {
        return new PhiWithRho(
            this.original.copy(),
            this
        );
    }

    @Override
    public boolean hasRho() {
        return this.original.hasRho();
    }

    @Override
    public Phi take(final String name) {
        Phi ret = this.original.take(name);
        if (!ret.hasRho()) {
            ret = ret.copy();
            ret.put(Phi.RHO, this.rho);
        }
        return ret;
    }

    @Override
    public Phi take(final int pos) {
        Phi ret = this.original.take(pos);
        if (!ret.hasRho()) {
            ret = ret.copy();
            ret.put(Phi.RHO, this.rho);
        }
        return ret;
    }

    @Override
    public void put(final int pos, final Phi object) {
        this.original.put(pos, object);
    }

    @Override
    public void put(final String name, final Phi object) {
        this.original.put(name, object);
    }

    @Override
    public String locator() {
        return this.original.locator();
    }

    @Override
    public String forma() {
        return this.original.forma();
    }

    @Override
    public byte[] delta() {
        return this.original.delta();
    }
}

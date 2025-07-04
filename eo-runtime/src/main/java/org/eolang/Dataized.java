/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */

package org.eolang;

import EOorg.EOeolang.EOerror;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A dataized object.
 *
 * <p>The class automates the process of turning EO objects into data. The
 * mechanism is explained in details in our canonical paper. Simply put,
 * it makes an attempt to either type-cast the provided object into data
 * or find "Δ" attribute inside it. If neither of that works, there is a
 * runtime exception.
 *
 * @see <a href="https://arxiv.org/abs/2111.13384">Canonical explanation of the Dataization concept</a>
 * @since 0.1
 */
@SuppressWarnings("java:S5164")
public final class Dataized {
    /**
     * The object to dataize.
     */
    private final Phi phi;

    /**
     * Logger.
     */
    private final Logger logger;

    /**
     * Ctor.
     * @param src The object
     */
    public Dataized(final Phi src) {
        this(src, Logger.getLogger(Dataized.class.getName()));
    }

    /**
     * Ctor.
     * @param src The object
     * @param log Logger
     */
    public Dataized(final Phi src, final Logger log) {
        this.phi = src;
        this.logger = log;
    }

    /**
     * Extracts the data from the EO object as a byte array.
     *
     * <p>This method performs the dataization process, which involves converting
     * the EO object into a byte array. It logs the dataization process if the
     * logging level is set to FINE and the current dataization level is within
     * the maximum allowed level. If an error occurs during dataization, it logs
     * the error details and rethrows the exception.</p>
     *
     * <p>Usage example:</p>
     *
     * <pre>{@code
     * Phi phi = ...; // Initialize your EO object
     * Dataized dataized = new Dataized(phi);
     * byte[] data = dataized.take();
     * }</pre>
     *
     * @return The data
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public byte[] take() {
        try {
            return this.phi.delta();
        } catch (final EOerror.ExError ex) {
            final List<String> raw = new ArrayList<>(ex.messages().size());
            raw.addAll(ex.messages());
            Collections.reverse(raw);
            final Phi enc = ex.enclosure();
            if ("org.eolang.go.to.token.jump".equals(enc.forma())) {
                throw new EOerror.ExError(enc);
            }
            if (String.format("%s.org.eolang.string", PhPackage.GLOBAL).equals(enc.forma())) {
                raw.add(
                    String.format(
                        "\"%s\"",
                        new Dataized(enc).take(String.class)
                    )
                );
            }
            final String fmt = String.format("%%%dd) %%s", (int) Math.log10(raw.size()) + 1);
            final List<String> clean = new ArrayList<>(raw.size());
            int idx = 1;
            for (final String line : raw) {
                clean.add(String.format(fmt, idx, line));
                ++idx;
            }
            this.logger.log(
                Level.SEVERE,
                String.format(
                    "Dataized to org.eolang.error with %s inside, at:%n  ⇢ %s",
                    enc.forma(),
                    String.join("\n  ⇢ ", clean)
                )
            );
            throw new EOerror.ExError(enc);
        }
    }

    /**
     * Take the data with a type.
     * @param type The type
     * @param <T> The type
     * @return The data
     */
    public <T> T take(final Class<T> type) {
        final Object res;
        if (type.equals(Long.class)) {
            res = new BytesOf(this.take()).asNumber(Long.class);
        } else if (type.equals(Double.class)) {
            res = this.asNumber();
        } else if (type.equals(Integer.class)) {
            res = new BytesOf(this.take()).asNumber(Integer.class);
        } else if (type.equals(Short.class)) {
            res = new BytesOf(this.take()).asNumber(Short.class);
        } else if (type.equals(byte[].class)) {
            res = this.take();
        } else if (type.equals(String.class)) {
            res = this.asString();
        } else if (type.equals(Boolean.class)) {
            res = this.asBool();
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Unknown type \"%s\", bytes are: %s",
                    type.getCanonicalName(),
                    Arrays.toString(this.asBytes().take())
                )
            );
        }
        return type.cast(res);
    }

    /**
     * Extract the data from the object and convert to string.
     * @return Data as string
     */
    public String asString() {
        return new String(this.take(), StandardCharsets.UTF_8);
    }

    /**
     * Extract the data from the object and convert to number.
     * @return Data as number
     */
    public Double asNumber() {
        return new BytesOf(this.take()).asNumber();
    }

    /**
     * Extract the data from the object and convert to boolean.
     * @return Data as boolean
     */
    public Boolean asBool() {
        final byte[] weak = this.take();
        if (weak.length != 1) {
            throw new ExFailure(
                "Can't dataize given bytes of length > 1 to boolean, bytes are: %s",
                Arrays.toString(weak)
            );
        }
        return weak[0] == 1;
    }

    /**
     * Extract the data from the object and convert to {@link Bytes}.
     * @return Data as {@link Bytes}
     */
    public Bytes asBytes() {
        return new BytesOf(this.take());
    }
}

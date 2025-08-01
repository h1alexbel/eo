/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.maven;

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputWithFallback;

/**
 * The simple HTTP Objectionary server.
 * <p>This class is supposed to be used together with {@link OyCached}.</p>
 *
 * @since 0.1
 */
final class OyRemote implements Objectionary {

    /**
     * The address template.
     */
    private final UrlOy template;

    /**
     * Constructor.
     * @param hash Commit hash
     */
    OyRemote(final CommitHash hash) {
        this.template = new UrlOy(
            "https://raw.githubusercontent.com/objectionary/home/%s/objects/%s.eo",
            hash
        );
    }

    @Override
    public String toString() {
        return this.template.toString();
    }

    @Override
    public Input get(final String name) throws MalformedURLException {
        final URL url = this.template.value(name);
        Logger.debug(
            this, "The object '%s' will be pulled from %s...",
            name, url
        );
        return new InputWithFallback(
            new InputOf(url),
            input -> {
                throw new IOException(
                    String.format(
                        "EO object '%s' is not found in this GitHub repository: https://github.com/objectionary/home by url: %s. This means that you either misspelled the name of it or simply referred to your own local object somewhere in your code as if it was an object of 'org.eolang' package. Check the sources and make sure you always use +alias meta when you refer to an object outside of 'org.eolang', even if this object belongs to your package.",
                        url,
                        name
                    ),
                    input
                );
            }
        );
    }

    @Override
    @RetryOnFailure(delay = 1L, unit = TimeUnit.SECONDS)
    public boolean contains(final String name) throws IOException {
        final int code = ((HttpURLConnection) this.template.value(name).openConnection())
            .getResponseCode();
        return code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * Objectionary URL template.
     *
     * <p>Assumes two placeholders in terms of
     * {@link String#format(String, Object...)}: 1st for version hash,
     * 2nd for program name, for
     * <a href="https://raw.githubusercontent.com/objectionary/home/%s/objects/%s.eo">example</a>.</p>
     *
     * @since 0.1.0
     */
    static final class UrlOy {

        /**
         * URL template.
         *
         * <p>Expects two placeholders in terms of
         * {@link String#format(String, Object...)}: 1st for hash,
         * 2nd for program name, for
         * <a href="https://raw.githubusercontent.com/objectionary/home/%s/objects/%s.eo">example</a>.</p>
         */
        private final String template;

        /**
         * Objects version hash.
         */
        private final CommitHash hash;

        /**
         * Ctor for testing.
         * @param template URL template
         * @param hash Commit hash
         */
        UrlOy(final String template, final String hash) {
            this(template, () -> hash);
        }

        /**
         * Ctor.
         * @param template URL template.
         * @param hash Objects version hash.
         */
        UrlOy(final String template, final CommitHash hash) {
            this.template = template;
            this.hash = hash;
        }

        /**
         * URL for the program.
         * @param name Fully qualified EO program name as specified by {@link Place}
         * @return URL
         * @throws MalformedURLException in case of incorrect URL
         */
        public URL value(final String name) throws MalformedURLException {
            return new URL(
                String.format(
                    this.template,
                    this.hash.value(),
                    name.replace(".", "/")
                )
            );
        }

        @Override
        public String toString() {
            return this.template;
        }
    }

}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.maven;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.cactoos.text.TextOf;
import org.eolang.parser.Xmir;

/**
 * Read XMIR files and translate them to the phi-calculus expression.
 * @since 0.34.0
 */
@Mojo(
    name = "xmir-to-phi",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    threadSafe = true
)
public final class MjPhi extends MjSafe {
    /**
     * Extension of the file where we put phi-calculus expression (.phi).
     */
    static final String EXT = "phi";

    /**
     * Subdirectory for parsed cache.
     */
    static final String CACHE = "phied";

    /**
     * The directory where to take xmir files for translation from.
     * @checkstyle MemberNameCheck (10 lines)
     */
    @Parameter(
        property = "eo.phiInputDir",
        required = true,
        defaultValue = "${project.build.directory}/eo/1-parse"
    )
    private File phiInputDir;

    /**
     * The directory where to save phi files to.
     * @checkstyle MemberNameCheck (10 lines)
     */
    @Parameter(
        property = "eo.phiOutputDir",
        required = true,
        defaultValue = "${project.build.directory}/eo/phi"
    )
    private File phiOutputDir;



    /**
     * Convert to PHI without syntax sugar.
     * @checkstyle MemberNameCheck (10 lines)
     */
    @Parameter(property = "eo.phiNoSugar", required = true, defaultValue = "false")
    @SuppressWarnings("PMD.ImmutableField")
    private boolean phiNoSugar;

    @Override
    public void exec() {
        final AtomicInteger passed = new AtomicInteger();
        final Walk walk = new Walk(this.phiInputDir.toPath());
        final int total = walk.size();
        final int count = new Threaded<>(
            walk,
            xmir -> {
                final int position = passed.addAndGet(1);
                return this.translate(xmir, position, total);
            }
        ).total();
        if (count > 0) {
            Logger.info(
                this, "Translated %d XMIR file(s) from %[file]s to %[file]s",
                count, this.phiInputDir, this.phiOutputDir
            );
        } else {
            Logger.info(
                this, "No XMIR files translated from %[file]s to %[file]s",
                this.phiInputDir, this.phiOutputDir
            );
        }
    }

    /**
     * Translate one XMIR file to .phi file.
     * @param xmir The XMIR file
     * @param position Its position in the entire pack
     * @param total How many files are there
     * @return How many files translated (either 1 or 0)
     * @throws Exception If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private int translate(final Path xmir, final int position, final int total)
        throws Exception {
        final long start = System.currentTimeMillis();
        Logger.debug(
            this,
            "Processing XMIR (#%d/%d): %[file]s (%[size]s)...",
            position, total, xmir, xmir.toFile().length()
        );
        final Path relative = Paths.get(
            this.phiInputDir.toPath().relativize(xmir).toString().replace(
                String.format(".%s", MjAssemble.XMIR),
                String.format(".%s", MjPhi.EXT)
            )
        );
        final Path target = this.phiOutputDir.toPath().resolve(relative);
        final XML xml = new XMLDocument(new TextOf(xmir).asString());
        try {
            new Saved(this.translated(xml), target).value();
            Logger.debug(
                this,
                "Translated to phi (#%d/%d): %[file]s (%[size]s) -> %[file]s (%[size]s) in %[ms]s",
                position, total, xmir,
                xmir.toFile().length(),
                relative,
                target.toFile().length(),
                System.currentTimeMillis() - start
            );
        } catch (final ImpossibleToPhiTranslationException ex) {
            Logger.debug(this, "XML is not translatable to phi:\n%s", xml.toString());
            throw new IllegalStateException(
                String.format("Couldn't translate %s to phi", xmir), ex
            );
        }
        return 1;
    }

    /**
     * Translate given xmir to phi calculus expression.
     * @param xml Text of xmir
     * @return Translated xmir
     * @throws ImpossibleToPhiTranslationException If fails to translate given XMIR to phi
     */
    private String translated(final XML xml) throws ImpossibleToPhiTranslationException {
        final Xmir xmir = new Xmir(xml);
        final String phi;
        try {
            if (this.phiNoSugar) {
                phi = xmir.toSaltyPhi();
            } else {
                phi = xmir.toPhi();
            }
        } catch (final IndexOutOfBoundsException exception) {
            throw new ImpossibleToPhiTranslationException(
                String.format("Xpath 'phi/text()' is not found in the translated XMIR: \n%s", xmir),
                exception
            );
        }
        return phi;
    }

    /**
     * Exception which indicates that translation to phi can't be processed.
     * @since 0.36.0
     */
    private static class ImpossibleToPhiTranslationException extends Exception {
        /**
         * Ctor.
         * @param message Exception message
         * @param cause Previous exception
         */
        ImpossibleToPhiTranslationException(final String message, final Exception cause) {
            super(message, cause);
        }
    }
}

/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.maven;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goes through all .class files and deletes those that
 * were created from autogenerated sources.
 *
 * @since 0.1
 */
@Mojo(
    name = "unspile",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    threadSafe = true
)
@SuppressWarnings("PMD.ImmutableField")
public final class UnspileMojo extends SafeMojo {
    /**
     * Pattern for matching paths ended with .class.
     */
    private static final Pattern JAVA = Pattern.compile("\\.java$");

    /**
     * Inner auto generated classes.
     */
    private static final Collection<String> INNER = List.of(
        "**/EO*$[1-9]*.class", "**/*$EOΦ*.class"
    );

    @Override
    public void exec() throws IOException {
        final Walk classes = new Walk(this.classesDir.toPath());
        if (classes.isEmpty()) {
            Logger.warn(this, "No .class files in %[file]s, nothing to unspile", this.classesDir);
        } else {
            this.unspile(classes);
        }
    }

    /**
     * Unspile classes.
     * @param classes Collection of compiled classes
     */
    private void unspile(final Walk classes) {
        final Path generated = this.generatedDir.toPath();
        final Set<String> included = new Walk(generated)
            .stream()
            .map(
                path -> UnspileMojo.JAVA.matcher(
                    generated.relativize(path).toString()
                ).replaceAll(".class")
            )
            .collect(Collectors.toSet());
        included.addAll(UnspileMojo.INNER);
        final Collection<Path> filtered = new ArrayList<>(
            classes.excludes(this.keepBinaries).includes(included)
        );
        if (filtered.isEmpty()) {
            Logger.info(
                this, "No .class files out of %d deleted in %[file]s",
                classes.size(), this.classesDir
            );
        } else {
            final int unspiled = new Threaded<>(
                filtered,
                path -> {
                    final int deleted;
                    if (Files.deleteIfExists(path)) {
                        Logger.debug(
                            this,
                            "Deleted %[file]s since was compiled only from %[file]s",
                            path, generated
                        );
                        deleted = 1;
                    } else {
                        deleted = 0;
                    }
                    return deleted;
                }
            ).total();
            new EmptyDirectoriesIn(this.classesDir.toPath()).clear();
            Logger.info(
                this,
                "Deleted %d .class files in %[file]s",
                unspiled, this.classesDir.toPath()
            );
        }
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Objectionary.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.eolang.maven;

import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.cactoos.io.InputOf;
import org.cactoos.scalar.Unchecked;
import org.cactoos.set.SetOf;
import org.eolang.maven.tojos.PlacedTojo;
import org.eolang.maven.tojos.PlacedTojosCached;
import org.eolang.maven.util.Home;
import org.eolang.maven.util.Rel;
import org.eolang.maven.util.Walk;

/**
 * Take binary files from where ResolveMojo placed them and
 * copy to target/classes.
 *
 * @since 0.11
 * @see <a href="https://news.eolang.org/2022-10-19-placed-catalog.html">Place catalog</a>
 */
@Mojo(
    name = "place",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    threadSafe = true
)
@SuppressWarnings({"PMD.ImmutableField", "PMD.AvoidDuplicateLiterals"})
public final class PlaceMojo extends SafeMojo {

    /**
     * Output.
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter(
        property = "eo.outputDir",
        required = true,
        defaultValue = "${project.build.outputDirectory}"
    )
    private File outputDir;

    /**
     * List of inclusion GLOB filters for finding class files.
     * @since 0.15
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter
    private Set<String> includeBinaries = new SetOf<>("**");

    /**
     * List of exclusion GLOB filters for finding class files.
     * @since 0.15
     * @checkstyle MemberNameCheck (7 lines)
     */
    @Parameter
    private Set<String> excludeBinaries = new SetOf<>();

    /**
     * Placed cached tojos.
     * @since 0.30
     * @checkstyle MemberNameCheck (7 lines)
     */
    private final PlacedTojosCached placedTojosCached = new PlacedTojosCached(this.placedTojos);

    @Override
    public void exec() throws IOException {
        final Path home = this.targetDir.toPath().resolve(ResolveMojo.DIR);
        if (Files.exists(home)) {
            final Collection<String> deps = new DepDirs(home);
            final long copied = deps.stream()
                .mapToLong(dep -> this.placeDep(home, dep))
                .sum();
            if (copied == 0) {
                Logger.debug(
                    this, "No binary files placed from %d dependencies",
                    deps.size()
                );
            } else {
                Logger.info(
                    this, "Placed %d binary files found in %d dependencies",
                    copied, deps.size()
                );
            }
        } else {
            Logger.info(
                this, "The directory is absent, nothing to place: %s",
                new Rel(home)
            );
        }
    }

    /**
     * Place one dep.
     * @param home Home to read from
     * @param dep The name of dep
     * @return How many binaries placed
     * @checkstyle ExecutableStatementCountCheck (300 lines)
     * @checkstyle CyclomaticComplexityCheck (300 lines)
     * @checkstyle NPathComplexityCheck (300 lines)
     * @todo #1320:30min Refactor PlaceMojo#place method in order to reduce complexity.
     *  When the method will be refactored we have to remove all warning suppressing from
     *  checkstyle and PMD.
     */
    @SuppressWarnings("PMD.NPathComplexity")
    private long placeDep(final Path home, final String dep) {
        if (this.placedTojos.findJar(dep).isPresent()) {
            Logger.info(this, "Found placed binaries from %s", dep);
        }
        final Path dir = home.resolve(dep);
        final long copied = new Walk(dir)
            .includes(this.includeBinaries)
            .excludes(this.excludeBinaries)
            .stream()
            .filter(file -> this.isNotEoSource(dir, file))
            .filter(file -> this.isNotAlreadyPlaced(dir, file))
            .filter(file -> this.isUnplaced(dir, file))
            .peek(file -> this.log(dir, file))
            .peek(file -> this.place(dir, file, dep))
            .count();
        this.placedTojos.placeJar(dep);
        if (copied > 0) {
            Logger.info(
                this, "Placed %d binary file(s) out of %d, found in %s",
                copied, new Walk(dir).size(), dep
            );
        } else {
            Logger.info(
                this, "No binary file(s) out of %d were placed from %s",
                new Walk(dir).size(), dep
            );
        }
        return copied;
    }

    private void place(final Path dir, final Path file, final String dep) {
        try {
            final Path path = dir.relativize(file);
            final Path target = this.outputDir.toPath().resolve(path);
            new Home(this.outputDir).save(new InputOf(file), path);
            this.placedTojosCached.placeClass(
                target,
                target.toString().substring(this.outputDir.toString().length() + 1),
                dep
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(
                String.format(
                    "Failed to place %s to %s",
                    file, this.outputDir
                ),
                ex
            );
        }
    }

    private void log(final Path dir, final Path file) {
        final Path target = this.outputDir.toPath().resolve(dir.relativize(file));
        final Optional<PlacedTojo> before = this.placedTojosCached.find(target);
        if (before.isPresent()) {
            if (!Files.exists(target)) {
                Logger.info(
                    this,
                    "The file %s has been placed to %s, but now it's gone, replacing",
                    new Rel(file),
                    new Rel(target)
                );
            }
            if (Files.exists(target) && !PlaceMojo.sameLength(target, file)) {
                Logger.debug(
                    this,
                    "File %s (%d bytes) was already placed at %s (%d bytes!) by %s, replacing",
                    new Rel(file), file.toFile().length(),
                    new Rel(target), target.toFile().length(),
                    before.get().dependency()
                );
            }
        }
    }

    private boolean isUnplaced(final Path dir, final Path file) {
        final Path target = this.outputDir.toPath().resolve(dir.relativize(file));
        final Optional<PlacedTojo> tojo = this.placedTojosCached.find(target);
        return !(tojo.isPresent() && Files.exists(target) && !tojo.get().unplaced());
    }

    private boolean isNotAlreadyPlaced(final Path dir, final Path file) {
        final Path target = this.outputDir.toPath().resolve(dir.relativize(file));
        final Optional<PlacedTojo> tojo = this.placedTojosCached.find(target);
        final boolean res;
        if (tojo.isPresent() && Files.exists(target) && PlaceMojo.sameLength(target, file)) {
            Logger.debug(
                this,
                "The same file %s is already placed to %s maybe by %s, skipping",
                new Rel(file),
                new Rel(target),
                tojo.get().dependency()
            );
            res = false;
        } else {
            res = true;
        }
        return res;
    }

    private boolean isNotEoSource(final Path dir, final Path file) {
        final Path path = dir.relativize(file);
        final boolean res;
        if (path.startsWith(CopyMojo.DIR)) {
            Logger.debug(
                this,
                "File %s is not a binary, but a source, won't place it",
                new Rel(file)
            );
            res = false;
        } else {
            res = true;
        }
        return res;
    }

    private static boolean sameLength(final Path first, final Path second) {
        return new Unchecked<>(() -> Files.size(first) == Files.size(second)).value();
    }
}

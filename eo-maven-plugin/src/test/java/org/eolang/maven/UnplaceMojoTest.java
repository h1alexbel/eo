/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package org.eolang.maven;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link UnplaceMojo}.
 *
 * @since 0.1
 * @checkstyle LocalFinalVariableNameCheck (100 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
@ExtendWith(MktmpResolver.class)
final class UnplaceMojoTest {
    @Test
    void cleansAllTheFiles(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp);
        final Path clazz = UnplaceMojoTest.placed(temp, maven, "class");
        final Path text = UnplaceMojoTest.placed(temp, maven, "txt");
        final Path bin = UnplaceMojoTest.placed(temp, maven, "so");
        MatcherAssert.assertThat(
            "After executing UnplaceMojo, all the placed files must be removed",
            maven
                .execute(UnplaceMojo.class)
                .result(),
            Matchers.allOf(
                Matchers.not(Matchers.hasValue(clazz)),
                Matchers.not(Matchers.hasValue(text)),
                Matchers.not(Matchers.hasValue(bin))
            )
        );
    }

    @Test
    void keepsClasses(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp).with("keepBinaries", Set.of("**/*.class"));
        final Path clazz = UnplaceMojoTest.placed(temp, maven, "class");
        final Path text = UnplaceMojoTest.placed(clazz, maven, "txt");
        MatcherAssert.assertThat(
            "UnplaceMojo must keep .class files and remove .txt file",
            maven
                .execute(UnplaceMojo.class)
                .result(),
            Matchers.allOf(
                Matchers.hasValue(clazz),
                Matchers.not(Matchers.hasValue(text))
            )
        );
    }

    @Test
    void removesClasses(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp).with("removeBinaries", Set.of("**/*.class"));
        final Path clazz = UnplaceMojoTest.placed(temp, maven, "class");
        final Path text = UnplaceMojoTest.placed(clazz, maven, "txt");
        MatcherAssert.assertThat(
            "UnplaceMojo must keep .class files and remove .txt file",
            maven.execute(UnplaceMojo.class).result(),
            Matchers.allOf(
                Matchers.hasValue(text),
                Matchers.not(Matchers.hasValue(clazz))
            )
        );
    }

    @Test
    void keepsAndRemovesSpecifiedFiles(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp)
            .with("removeBinaries", Set.of("**/*.sh"))
            .with("keepBinaries", Set.of("org/eolang/**"));
        final Path sh1 = UnplaceMojoTest.placed(temp, maven, "sh");
        final Path sh2 = UnplaceMojoTest.placed(
            temp, maven, Paths.get("org/other.sh")
        );
        final Path remained = UnplaceMojoTest.placed(
            temp, maven, Paths.get("org/eolang/my.sh")
        );
        MatcherAssert.assertThat(
            "UnplaceMojo must keep files org/eolang/ directory but remove all .sh files",
            maven.execute(UnplaceMojo.class).result(),
            Matchers.allOf(
                Matchers.not(Matchers.hasValue(sh1)),
                Matchers.not(Matchers.hasValue(sh2)),
                Matchers.hasValue(remained)
            )
        );
    }

    @Test
    void updatesPlacedTojosFile(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp);
        final Path file = UnplaceMojoTest.placed(temp, maven, "bat");
        MatcherAssert.assertThat(
            "Tojo must be marked as unplaced",
            maven.execute(UnplaceMojo.class).placed().find(file).get().unplaced(),
            Matchers.is(true)
        );
    }

    @Test
    void deletesAllEmptyDirectories(@Mktmp final Path temp) throws IOException {
        final FakeMaven maven = new FakeMaven(temp);
        UnplaceMojoTest.placed(temp, maven, Paths.get("org/eolang/index.html"));
        UnplaceMojoTest.placed(temp, maven, Paths.get("org/styles.css"));
        MatcherAssert.assertThat(
            "UnplaceMojo must delete all empty directories",
            maven.execute(UnplaceMojo.class).result(),
            Matchers.allOf(
                Matchers.not(Matchers.hasKey("target/classes/org/eolang")),
                Matchers.not(Matchers.hasKey("target/classes/org")),
                Matchers.hasKey("target/classes")
            )
        );
    }

    /**
     * Place file to the placed tojos file.
     * @param temp Temporary directory
     * @param maven Maven instance
     * @param ext File extension
     * @return Path to placed file
     */
    private static Path placed(
        final Path temp, final FakeMaven maven, final String ext
    ) throws IOException {
        return UnplaceMojoTest.placed(
            temp,
            maven,
            Paths.get(
                String.format("a/b/c/%d_foo.%s", new SecureRandom().nextInt(), ext)
            )
        );
    }

    /**
     * Place file into the placed tojos file.
     * @param temp Temporary directory
     * @param maven Maven instance
     * @param relative Relative path to file
     * @return Path to placed file
     * @throws IOException If Fails to place
     */
    private static Path placed(
        final Path temp, final FakeMaven maven, final Path relative
    ) throws IOException {
        final Path file = new Saved(
            UUID.randomUUID().toString(),
            maven.classesPath().resolve(relative)
        ).value();
        maven.placed().placeClass(
            file,
            temp.relativize(file).toString(),
            "eo-lib"
        );
        return file;
    }
}

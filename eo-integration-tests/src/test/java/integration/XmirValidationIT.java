/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */
package integration;

import com.jcabi.xml.XMLDocument;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eolang.parser.StrictXmir;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Integration test for checking validity of parsed EO as XMIR documents.
 *
 * @since 0.58.3
 */
final class XmirValidationIT {

    @Test
    void checksWithXsd() throws IOException {
        Files.walk(
                Paths.get("").toAbsolutePath().getParent()
                    .resolve("eo-runtime")
                    .resolve("target")
                    .resolve("eo")
                    .resolve("1-parse")
            ).filter(Files::isRegularFile)
            .forEach(
                xmir -> {
                    try {
                        Assertions.assertDoesNotThrow(
                            new StrictXmir(new XMLDocument(xmir))::inner,
                            "validation should pass as normal"
                        );
                    } catch (final FileNotFoundException exception) {
                        throw new IllegalStateException(
                            String.format("Failed to find XMIR for %s", xmir), exception
                        );
                    }
                }
            );
    }
}

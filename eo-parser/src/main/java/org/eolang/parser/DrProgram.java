/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2025 Objectionary.com
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
package org.eolang.parser;

import com.jcabi.manifests.Manifests;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * The {@code program} element in XML as Xembly {@link Directives}.
 *
 * @since 0.44.0
 */
public final class DrProgram implements Iterable<Directive> {

    /**
     * Name of the program.
     */
    private final String name;

    /**
     * Ctor.
     * @param nme Name of the program
     */
    public DrProgram(final String nme) {
        this.name = nme;
    }

    @Override
    public Iterator<Directive> iterator() {
        final String when = ZonedDateTime.now(ZoneOffset.UTC).format(
            DateTimeFormatter.ISO_INSTANT
        );
        final String nme;
        if (this.name.startsWith("Q.")) {
            nme = this.name.substring(2);
        } else {
            nme = this.name;
        }
        return new Directives()
            .comment(
                String.join(
                    "\n  ",
                    "",
                    "This is XMIR, a dialect of XML, which is used to represent a parsed",
                    "EO program. For more information about XMIR format please visit:",
                    "https://news.eolang.org/2022-11-25-xmir-guide.html. Also, XSD schema",
                    String.format(
                        "is documented here: https://www.eolang.org/xsd/XMIR-%s.html.",
                        Manifests.read("EO-Version")
                    ),
                    "",
                    String.format(
                        "The file was auto-generated by the parser %s (%s)",
                        Manifests.read("EO-Version"),
                        Manifests.read("EO-Revision")
                    ),
                    String.format(
                        "at %s. Do not edit it manually.",
                        when
                    ),
                    "The source code of the parser is available",
                    "on GitHub, at https://github.com/objectionary/eo (bug reports are welcome).",
                    ""
                )
            )
            .add("program")
            .attr(
                "noNamespaceSchemaLocation xsi http://www.w3.org/2001/XMLSchema-instance",
                DrProgram.schema()
            )
            .attr("name", nme)
            .attr("version", Manifests.read("EO-Version"))
            .attr("revision", Manifests.read("EO-Revision"))
            .attr("dob", Manifests.read("EO-Dob"))
            .attr("time", when)
            .iterator();
    }

    /**
     * Find the location of XSD schema.
     *
     * <p>In production, the XSD is located online at the eolang.org
     * website, were we deploy it on every release cycle (see the {@code .rultor.yml}
     * file. However, during testing cycle, we must use the local file,
     * allowing its most recent changes to be visible to the code. However,
     * we don't know exactly where from the tests are being executed
     * (what is the current directory). Because of this, we try to find the
     * file using a number of options.</p>
     *
     * @return The location of the XSD schema file/URL
     */
    private static String schema() {
        String schema = String.format(
            "https://www.eolang.org/xsd/XMIR-%s.xsd",
            Manifests.read("EO-Version")
        );
        final String[] opts = {
            "XMIR.xsd",
            "src/main/resources/XMIR.xsd",
            "eo-parser/src/main/resources/XMIR.xsd",
            "../eo-parser/src/main/resources/XMIR.xsd",
            System.getProperty("xmir.xsd", ""),
        };
        for (final String opt : opts) {
            if (opt.isEmpty()) {
                continue;
            }
            final Path path = Paths.get(opt).toAbsolutePath();
            if (path.toFile().exists()) {
                schema = String.format(
                    "file:///%s",
                    path.toString().replace("\\", "/")
                );
                break;
            }
        }
        return schema;
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Objectionary.com
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

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.yegor256.xsline.TrClasspath;
import com.yegor256.xsline.Xsline;
import org.eolang.jucs.ClasspathSource;
import org.eolang.xax.XaxStory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test case for {@link TrParsing}.
 *
 * @since 0.23
 */
final class TrParsingTest {

    @Test
    void buildsList() {
        MatcherAssert.assertThat(
            "ParsingTrain is not iterable with more than 1 item, but it must be",
            new TrParsing(),
            Matchers.iterableWithSize(Matchers.greaterThan(1))
        );
    }

    @Test
    void passesPipelineEvenWithErrors() {
        final XML xml = new XMLDocument(
            String.join(
                "\n",
                "<program>",
                "  <objects>",
                "    <o abstract=\"\" line=\"1\" name=\"main\" pos=\"0\">",
                "      <o base=\"bool\" data=\"bytes\" line=\"2\" name=\"x\" pos=\"2\">01</o>",
                "      <o base=\"bool\" data=\"bytes\" line=\"3\" name=\"x\" pos=\"2\">00</o>",
                "    </o>",
                "  </objects>",
                "</program>"
            )
        );
        MatcherAssert.assertThat(
            "XSL transformation works properly.",
            new Xsline(
                new TrParsing()
            ).pass(xml),
            XhtmlMatchers.hasXPaths(
                "/program/sheets[count(sheet)>1]",
                "/program[not(errors)]"
            )
        );
    }

    /**
     * Test for {@see _func.xsl}.
     * @param bytes Bytes to convert
     * @param num Expected number
     * @since 1.0
     */
    @ParameterizedTest
    @CsvSource({
        "00     , 0",
        "0000   , 0",
        "000000 , 0",
        "000001 , 1",
        "000010 , 16",
        "000100 , 256",
        "FFFFFF , 16777215"
    })
    void runsXslFunction(final String bytes, final String num) {
        MatcherAssert.assertThat(
            "Failed to convert byte to int using the apply-func.xsl transformation",
            new Xsline(
                new TrClasspath<>("/org/eolang/parser/apply-func.xsl").back()
            ).pass(new XMLDocument(String.format("<o>%s</o>", bytes))),
            XhtmlMatchers.hasXPath(
                String.format("/o[text()='%s']", num)
            )
        );
    }

    @ParameterizedTest
    @ClasspathSource(value = "org/eolang/parser/packs/", glob = "**.yaml")
    void parsesPacks(final String pack) throws Exception {
        final CheckPack check = new CheckPack(pack);
        if (check.skip()) {
            Assumptions.abort(
                String.format("%s is not ready", pack)
            );
        }
        MatcherAssert.assertThat(
            String.format(
                "The check pack has failed: %n%s",
                pack
            ),
            check.failures(),
            Matchers.empty()
        );
    }

    @ParameterizedTest
    @ClasspathSource(value = "org/eolang/parser/xax/", glob = "**.yml")
    void createsXaxStoryWithXslStylesheets(final String yaml) {
        MatcherAssert.assertThat(
            String.format(
                "The %s xax check is failed",
                yaml
            ),
            new XaxStory(yaml),
            Matchers.is(true)
        );
    }
}

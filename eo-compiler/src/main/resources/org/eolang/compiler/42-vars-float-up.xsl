<?xml version="1.0"?>
<!--
The MIT License (MIT)

Copyright (c) 2016-2020 Yegor Bugayenko

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eo="https://www.eolang.org" xmlns:xs="http://www.w3.org/2001/XMLSchema" id="vars-float-up" version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:import href="/org/eolang/compiler/funcs.xsl"/>
  <xsl:function name="eo:var-name" as="xs:string">
    <xsl:param name="object" as="element()"/>
    <xsl:variable name="n">
      <xsl:value-of select="$object/@name"/>
    </xsl:variable>
    <xsl:value-of select="$n"/>
  </xsl:function>
  <xsl:template match="o[eo:abstract(.)]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="./o//o[@name]">
        <xsl:copy>
          <xsl:apply-templates select="@* except @name"/>
          <xsl:attribute name="name">
            <xsl:value-of select="eo:var-name(.)"/>
          </xsl:attribute>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:for-each>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="o[eo:abstract(.)]/o//o[@name]">
    <xsl:copy>
      <xsl:apply-templates select="@* except (@name|@const)"/>
      <xsl:attribute name="base">
        <xsl:value-of select="eo:var-name(.)"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

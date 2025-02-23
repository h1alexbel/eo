<?xml version="1.0"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eo="https://www.eolang.org" xmlns:xs="http://www.w3.org/2001/XMLSchema" id="to-dot" version="2.0">
  <!--
  Here we convert the graph generated by Xembly into DOT language
  program, which is rendered at https://dreampuf.github.io/GraphvizOnline/
  -->
  <xsl:output encoding="UTF-8" method="text"/>
  <xsl:variable name="EOL">
    <xsl:value-of select="'&#10;'"/>
  </xsl:variable>
  <xsl:function name="eo:node" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:value-of select="replace($name, '^\$', '')"/>
  </xsl:function>
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
      <xsl:element name="dot">
        <xsl:text>/* Render it at https://dreampuf.github.io/GraphvizOnline/ */</xsl:text>
        <xsl:value-of select="$EOL"/>
        <xsl:text>digraph {</xsl:text>
        <xsl:value-of select="$EOL"/>
        <xsl:text>  node [fixedsize=true,width=1,fontname="Arial"];</xsl:text>
        <xsl:value-of select="$EOL"/>
        <xsl:text>  edge [fontname="Arial"];</xsl:text>
        <xsl:value-of select="$EOL"/>
        <xsl:apply-templates select="/graph/v" mode="dot"/>
        <xsl:apply-templates select="/graph/v/e" mode="dot"/>
        <xsl:text>}</xsl:text>
        <xsl:value-of select="$EOL"/>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="v" mode="dot">
    <xsl:variable name="v" select="."/>
    <xsl:text>  </xsl:text>
    <xsl:value-of select="eo:node($v/@id)"/>
    <xsl:text>[shape=</xsl:text>
    <xsl:choose>
      <xsl:when test="lambda">
        <xsl:text>doublecircle</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>circle</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="data">
      <xsl:text>,color="#f96900"</xsl:text>
    </xsl:if>
    <xsl:text>,label="</xsl:text>
    <xsl:choose>
      <xsl:when test="@id='ν0'">
        <xsl:text>Φ</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="eo:node(@id)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="lambda">
      <xsl:text>\n</xsl:text>
      <xsl:value-of select="lambda"/>
    </xsl:if>
    <xsl:text>"</xsl:text>
    <xsl:text>];</xsl:text>
    <xsl:value-of select="$EOL"/>
  </xsl:template>
  <xsl:template match="e[@title != 'σ']" mode="dot">
    <xsl:variable name="e" select="."/>
    <xsl:text>  </xsl:text>
    <xsl:value-of select="eo:node($e/parent::v/@id)"/>
    <xsl:text> -&gt; </xsl:text>
    <xsl:value-of select="eo:node($e/@to)"/>
    <xsl:text> [</xsl:text>
    <xsl:text>label="</xsl:text>
    <xsl:value-of select="@title"/>
    <xsl:text>"</xsl:text>
    <xsl:choose>
      <xsl:when test="starts-with(@title, 'π')">
        <xsl:text>,style=dashed</xsl:text>
      </xsl:when>
      <xsl:when test="starts-with(@title, 'ρ') or starts-with(@title, 'σ')">
        <xsl:text>,color=gray,fontcolor=gray</xsl:text>
      </xsl:when>
    </xsl:choose>
    <xsl:text>];</xsl:text>
    <xsl:value-of select="$EOL"/>
  </xsl:template>
  <xsl:template match="node()|@*" mode="#default">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

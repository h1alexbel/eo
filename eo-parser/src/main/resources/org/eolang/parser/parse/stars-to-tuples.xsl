<?xml version="1.0"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="stars-to-tuples" version="2.0">
  <!--
    Converts such XMIR with @star attributes:

    <o star="" base="tuple">
      <o base="1".../>
      <o base="2".../>
      <o base="3".../>
    </o>

    Into the next one without @star:

    <o base=".with">
      <o base=".with">
        <o base=".with">
          <o base=".empty">
            <o base="tuple"/>
          </o>
          <o base="1"/>
        </o>
        <o base="2"/>
      </o>
      <o base="3"/>
    </o>
  -->
  <xsl:output encoding="UTF-8" method="xml"/>
  <xsl:template match="o[@star]">
    <xsl:choose>
      <xsl:when test="count(o)&gt;0">
        <xsl:variable name="nested">
          <xsl:element name="o">
            <xsl:attribute name="star"/>
            <xsl:apply-templates select="o[position()!=last()]"/>
          </xsl:element>
        </xsl:variable>
        <xsl:element name="o">
          <xsl:attribute name="base" select="'.with'"/>
          <xsl:apply-templates select="@* except (@star | @base)"/>
          <xsl:apply-templates select="$nested"/>
          <xsl:apply-templates select="o[last()]"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="o">
          <xsl:attribute name="base" select="'.empty'"/>
          <xsl:apply-templates select="@* except (@star | @base)"/>
          <xsl:element name="o">
            <xsl:attribute name="base" select="'Q.org.eolang.tuple'"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

/*
 * SPDX-FileCopyrightText: Copyright (c) 2016-2025 Objectionary.com
 * SPDX-License-Identifier: MIT
 */

/*
 * @checkstyle PackageNameCheck (4 lines)
 * @checkstyle TrailingCommentCheck (3 lines)
 */
package EOorg.EOeolang; // NOPMD

import org.eolang.AtVoid;
import org.eolang.Atom;
import org.eolang.Attr;
import org.eolang.Dataized;
import org.eolang.Expect;
import org.eolang.PhDefault;
import org.eolang.Phi;
import org.eolang.XmirObject;

/**
 * Malloc.of.allocated.resized object.
 * @since 0.41.0
 * @checkstyle TypeNameCheck (5 lines)
 */
@XmirObject(oname = "malloc.of.allocated.resized")
@SuppressWarnings("PMD.AvoidDollarSigns")
public final class EOmalloc$EOof$EOallocated$EOresized extends PhDefault implements Atom {
    /**
     * Ctor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    EOmalloc$EOof$EOallocated$EOresized() {
        this.add("new-size", new AtVoid("new-size"));
    }

    @Override
    public Phi lambda() {
        final Phi rho = this.take(Attr.RHO);
        final int id = Expect.at(rho, "id")
            .that(phi -> new Dataized(phi).asNumber())
            .otherwise("must be a number")
            .that(Double::intValue)
            .it();
        final int size = Expect.at(this, "new-size")
            .that(phi -> new Dataized(phi).asNumber())
            .otherwise("must be a number")
            .that(Double::intValue)
            .it();
        Heaps.INSTANCE.resize(id, size);
        return rho;
    }
}

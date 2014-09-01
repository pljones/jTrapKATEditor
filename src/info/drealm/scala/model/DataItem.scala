/**
 * **************************************************************************
 *                                                                          *
 *   (C) Copyright 2014 by Peter L Jones                                    *
 *   pljones@users.sf.net                                                   *
 *                                                                          *
 *   This file is part of jTrapKATEditor.                                   *
 *                                                                          *
 *   jTrapKATEditor is free software; you can redistribute it and/or modify *
 *   it under the terms of the GNU General Public License as published by   *
 *   the Free Software Foundation; either version 3 of the License, or      *
 *   (at your option) any later version.                                    *
 *                                                                          *
 *   jTrapKATEditor is distributed in the hope that it will be useful,      *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *   GNU General Public License for more details.                           *
 *                                                                          *
 *   You should have received a copy of the GNU General Public License      *
 *   along with jTrapKATEditor.  If not, see http://www.gnu.org/licenses/   *
 *                                                                          *
 * **************************************************************************
 */

package info.drealm.scala.model

import java.io._
import scala.io._

trait DataItem {
    // The C# has this as protected
    def deserialize(in: InputStream): Unit

    // The C# has this as public
    final def serialize(out: OutputStream): Unit = serialize(out, false)

    // And this is the one you MUST define
    // ALWAYS ALWAYS! ALWAYS!!! call save(out) if saving is true when
    // implementing this!!!!!
    def serialize(out: OutputStream, saving: Boolean): Unit

    // The C# has "internalchg" as protected but not used, so not implemented!

    // The C# has this as private
    // _changed tracks whether update has been called on this DataItem
    protected[DataItem] var _changed: Boolean = false

    // Require data items to explain this (hence _changed being protected not private)
    def changed: Boolean

    protected def update(u: => Unit) = {
        u
        _changed = true
    }

    // In strange circumstances you may want to override this
    def save(out: OutputStream): Unit = {
        serialize(out, true)
        _changed = false
    }
}

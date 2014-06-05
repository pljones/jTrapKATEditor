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

trait DataItem extends scala.swing.Publisher {
    // The C# has this as protected
    final def deserialize(in: FileInputStream): Unit = deserialize(new DataInputStream(in))
    def deserialize(in: DataInputStream): Unit

    // The C# has this as public
    final def serialize(out: FileOutputStream): Unit = serialize(out, false)
    final def serialize(out: DataOutputStream): Unit = serialize(out, false)

    // The C# has this as protected
    final def serialize(out: FileOutputStream, saving: Boolean): Unit = serialize(new DataOutputStream(out), saving)
    // And this is the one you MUST define
    // ALWAYS ALWAYS! ALWAYS!!! call save(out) if saving is true when
    // implementing this!!!!!
    def serialize(out: DataOutputStream, saving: Boolean): Unit

    // The C# has "internalchg" as protected but not used, so not implemented!

    // The C# has this as private
    // _changed tracks whether update has been called on this DataItem
    private[this] var _changed: Boolean = false

    // The C# is a little different here
    // It has "OnDataChanged" which the subclasses call when they've done
    // their own updating - no update(u) call.  There's also no check
    // on _changed before notifying listeners.  I think this implementation
    // is a little nicer, both for subclasses and listeners.
    // (Don't use update(u) if you want to only send the DataItemChanged
    // after a series of changes - do them (all the "u" functions) and then call
    // dataItemChanged directly.)
    protected def dataItemChanged = if (!_changed) {
        _changed = true
        publish(new info.drealm.scala.eventX.DataItemChanged(this))
    }
    protected def update(u: => Unit) = {
        u
        dataItemChanged
    }

    // The C# has this as public
    final def save(out: FileOutputStream): Unit = save(new DataOutputStream(out))
    // In strange circumstances you may want to override this
    def save(out: DataOutputStream): Unit = {
        serialize(out, true)
        _changed = false
    }
    // The C# has this as public
    def changed = _changed
}

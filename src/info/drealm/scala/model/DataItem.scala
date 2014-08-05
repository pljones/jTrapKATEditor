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
    def deserialize(in: FileInputStream): Unit

    // The C# has this as public
    final def serialize(out: FileOutputStream): Unit = serialize(out, false)

    // And this is the one you MUST define
    // ALWAYS ALWAYS! ALWAYS!!! call save(out) if saving is true when
    // implementing this!!!!!
    def serialize(out: FileOutputStream, saving: Boolean): Unit

    // The C# has "internalchg" as protected but not used, so not implemented!

    // The C# has this as private
    // _changed tracks whether update has been called on this DataItem
    private[this] var _changed: Boolean = false

    // The C# is a little different here
    // It has "OnDataChanged" which the subclasses call when they've done
    // their own updating - no update(u) call.  I think this implementation
    // is a little nicer.
    // (If you want to send the DataItemChanged after a series of changes,
    // put them (all the "u" functions) in a block and then call
    // update with that block.)
    private[this] def dataItemChanged() = {
        _changed = true
        publish(new info.drealm.scala.eventX.DataItemChanged(this, None))
    }
    protected def update(u: => Unit) = {
        u
        dataItemChanged()
    }
    // Not every subclass will need this but enough will that it makes
    // sense to do it here: catch any listened-to DataItemChanged,
    // mark this as dirty and publish the change, tracing where it came from.
    // This is also part of how the C# uses OnDataChanged - it
    // makes it the handler for the event from subclasses.
    reactions += {
        case e: info.drealm.scala.eventX.DataItemChanged => {
            _changed = true
            deafTo(this)
            publish(new info.drealm.scala.eventX.DataItemChanged(this, Some(e)))
            listenTo(this)
        }
    }

    // In strange circumstances you may want to override this
    def save(out: FileOutputStream): Unit = {
        serialize(out, true)
        _changed = false
    }
    // The C# has this as public
    def changed = _changed

    // Hm, need a public way to make the data item appear dirty without needing to update it
    def makeChanged() = _changed = true
}

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

package info.drealm.scala

import scala.swing._
import scala.swing.event._
import info.drealm.scala.eventX._

trait Bindings extends Publisher {
    protected def _get(): Unit
    protected def _set(): Unit
    protected def _chg(): Unit

    protected def setDisplay(): Unit = {
        deafTo(this)
        _get()
        listenTo(this)
    }
    protected def setValue(): Unit = {
        deafTo(jTrapKATEditor)
        _set()
        _chg()
        listenTo(jTrapKATEditor)
    }

    listenTo(jTrapKATEditor)

    reactions += {
        case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
    }
}

trait ComboBoxBindings[T] extends RichComboBox[T] with Bindings {
    protected override def setDisplay(): Unit = {
        deafTo(selection)
        super.setDisplay()
        listenTo(selection)
    }

    listenTo(selection)

    reactions += {
        case e: SelectionChanged => setValue()
    }
}

trait EditableComboBoxBindings[T] extends ComboBoxBindings[T] {
    reactions += {
        case e: ValueChanged => setValue()
    }
}

trait KitBindings extends Bindings {
    protected def _isKit(): Boolean
    protected def _toKit(): Unit
    protected def _cp(): Component

    protected override def setValue(): Unit = {
        val isKit = _isKit()
        super.setValue()
        if (isKit) {
            _toKit()
            jTrapKATEditor.padChangedBy(_cp())
        }
    }
}
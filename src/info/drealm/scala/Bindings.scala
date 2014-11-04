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
        try {
            deafTo(this)
            _get()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(this) }
    }
    protected def setValue(): Unit = {
        try {
            deafTo(jTrapKATEditor)
            _set()
            _chg()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(jTrapKATEditor) }
    }
    protected def setUndoRedo(action: () => Unit) = {
        try {
            deafTo(jTrapKATEditor)
            deafTo(this)
            action()
            _get()
            _chg()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(this); listenTo(jTrapKATEditor) }
    }

    listenTo(jTrapKATEditor)

    reactions += {
        case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
    }
}

trait ValueChangedBindings extends Bindings {
    reactions += {
        case e: ValueChanged => setValue()
    }
}

trait ComboBoxBindings[T] extends RichComboBox[T] with Bindings {
    protected override def setDisplay(): Unit = {
        try {
            deafTo(selection)
            super.setDisplay()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(selection) }
    }
    protected override def setUndoRedo(action: () => Unit) = {
        try {
            deafTo(selection)
            super.setUndoRedo(action)
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(selection) }
    }

    listenTo(selection)

    reactions += {
        case e: SelectionChanged => setValue()
    }
}

trait EditableComboBoxBindings[T] extends ComboBoxBindings[T] with ValueChangedBindings

trait V3V4ComboBoxBindings[T, TP <: ComboBox[T], T3 <: TP, T4 <: TP] extends V3V4ComboBox[T, TP, T3, T4] with Bindings {
    protected override def setDisplay(): Unit = {
        try {
            deafTo(cbx)
            deafTo(selection)
            super.setDisplay()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(cbx); listenTo(selection) }
    }
    protected override def setUndoRedo(action: () => Unit) = {
        try {
            deafTo(cbx)
            deafTo(selection)
            super.setUndoRedo(action)
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(cbx); listenTo(selection) }
    }

    listenTo(selection)

    reactions += {
        case e: event.SelectionChanged                                 => setValue()
        case e: eventX.V3V4SelectionChanged                            => setValue()
        case e: eventX.CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
    }
}

trait PadBindings extends Bindings  {
    protected def _setHelper(update: (model.Pad, Byte) => Unit, before: Byte, after: Byte, name: String): Unit = {
        val currentPad = jTrapKATEditor.currentPad
        if (before != after) {
            EditHistory.add(new HistoryAction {
                val actionName = s"actionPad${name}"
                def undoAction = setUndoRedo(() => update(currentPad, before))
                def redoAction = setUndoRedo(() => update(currentPad, after))
            })
            update(currentPad, after)
        }
    }
}

trait KitBindings extends Bindings {
    protected def _isKit(): Boolean
    protected def _toKit(): Unit
    protected def _cp(): Component

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Byte) => Unit, before: Byte, after: Byte, name: String): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        if (before != after) {
            EditHistory.add(new HistoryAction {
                val actionName = s"actionKit${name}"
                def undoAction = setUndoRedo(() => update(currentKit, before))
                def redoAction = setUndoRedo(() => update(currentKit, after))
            })
            val isKit = _isKit()
            update(currentKit, after)
            if (isKit) {
                _toKit()
                jTrapKATEditor.padChangedBy(_cp())
            }
        }
    }
    protected override def setUndoRedo(action: () => Unit) = {
        try {
            val isKit = _isKit()
            super.setUndoRedo(action)
            if (isKit) {
                _toKit()
                jTrapKATEditor.padChangedBy(_cp())
            }
        }
        catch { case e: Exception => e.printStackTrace() }
        finally {}
    }
}

trait GlobalBindings extends Bindings {
    protected def _setHelper(update: (Byte) => Unit, before: Byte, after: Byte, name: String): Unit = {
        if (before != after) {
            EditHistory.add(new HistoryAction {
                val actionName = s"actionGlobal${name.capitalize}"
                def undoAction = setUndoRedo(() => update(before))
                def redoAction = setUndoRedo(() => update(after))
            })
            update(after)
        }
    }

    reactions += {
        case e: GlobalChanged if e.source == jTrapKATEditor => setDisplay()
    }
}

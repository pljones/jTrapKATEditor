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

trait DisplayBindings extends Reactor {
    protected def _isChg: Boolean
    protected def _get(): Unit

    protected def setDisplay(): Unit = _get()

    reactions += {
        case e: CurrentKitChanged if e.source == jTrapKATEditor && _isChg => setDisplay()
        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor && _isChg => setDisplay()
    }

    listenTo(jTrapKATEditor)
}

trait ValueBindings extends Publisher {
    protected def _isChg: Boolean
    protected def _set(): Unit
    protected def _chg(): Unit

    protected def setValue(): Unit = if (_isChg) {
        _set()
        try {
            deafTo(this)
            _chg()
        } finally { listenTo(this) }
    }
}

trait Bindings extends DisplayBindings with ValueBindings {
    override protected def setDisplay(): Unit = try {
        deafTo(this)
        super.setDisplay()
    } finally { listenTo(this) }

    protected def setUndoRedo(action: () => Unit) = try {
        deafTo(jTrapKATEditor)
        deafTo(this)
        action()
        _get()
        _chg()
    } finally { listenTo(this); listenTo(jTrapKATEditor) }
}

trait ValueChangedBindings extends Bindings {
    reactions += {
        case e: ValueChanged => setValue()
    }
}

trait ButtonBindings extends Bindings {
    protected def _isChg: Boolean = true // because we really did get clicked
}

trait ComboBoxBindings[T] extends RichComboBox[T] with Bindings {
    protected override def setDisplay(): Unit = try {
        deafTo(selection)
        super.setDisplay()
    } finally { listenTo(selection) }

    protected override def setUndoRedo(action: () => Unit) = try {
        deafTo(selection)
        super.setUndoRedo(action)
    } finally { listenTo(selection) }

    listenTo(selection)

    reactions += {
        case e: SelectionChanged => setValue()
    }
}

trait EditableComboBoxBindings[T] extends ComboBoxBindings[T] {
    reactions += {
        case e: DocumentChanged => setValue()
    }
}

trait V3V4ComboBoxBindings[T, TP <: ComboBox[T], T3 <: TP, T4 <: TP] extends V3V4ComboBox[T, TP, T3, T4] with Bindings {
    protected override def setDisplay(): Unit = try {
        deafTo(selection)
        super.setDisplay()
    } finally { listenTo(selection) }

    protected override def setUndoRedo(action: () => Unit) = try {
        deafTo(selection)
        super.setUndoRedo(action)
    } finally { listenTo(selection) }

    listenTo(selection)

    reactions += {
        case e: eventX.V3V4SelectionChanged => setValue()
    }
}

trait CurveComboBoxV3V4Bindings extends V3V4ComboBoxBindings[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] with Bindings

trait PadBindings extends Bindings {
    protected def _getBefore: () => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore() != _getAfter()

    protected def _setHelper(update: (model.Pad, Byte) => Unit, name: String): Unit = {
        val before = _getBefore()
        val after = _getAfter()
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

trait KitVariesBindings extends Bindings {
    protected def _getBefore: (model.Kit[_ <: model.Pad]) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentKit) != _getAfter()
    protected def _isKit: (model.Kit[_ <: model.Pad]) => Boolean
    protected def _toKit: (model.Kit[_ <: model.Pad]) => Unit
    protected def _cp(): Component

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Byte) => Unit, name: String): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val before: Byte = _getBefore(currentKit)
        val after: Byte = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionKit${name}"
            def undoAction = setUndoRedo(() => _updateHelper(currentKit, before, update))
            def redoAction = setUndoRedo(() => _updateHelper(currentKit, after, update))
        })
        _updateHelper(currentKit, after, update)
    }

    private[this] def _updateHelper(kit: model.Kit[_ <: model.Pad], value: Byte, update: (model.Kit[_ <: model.Pad], Byte) => Unit) = {
        val isKit = _isKit(kit)
        update(kit, value)
        if (isKit) {
            _toKit(kit)
            if (kit == jTrapKATEditor.currentKit)
                jTrapKATEditor.padChangedBy(_cp())
        }
    }
}

trait KitBindings extends Bindings {
    protected def _getBefore: (model.Kit[_ <: model.Pad]) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentKit) != _getAfter()

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Byte) => Unit, name: String): Unit = {
        val before: Byte = _getBefore(jTrapKATEditor.currentKit)
        val after: Byte = _getAfter()
        val currentKit = jTrapKATEditor.currentKit
        EditHistory.add(new HistoryAction {
            val actionName = s"actionKit${name}"
            def undoAction = setUndoRedo(() => update(currentKit, before))
            def redoAction = setUndoRedo(() => update(currentKit, after))
        })
        update(currentKit, after)
    }
}

trait GlobalPadDynamicsBindings extends Bindings {
    protected def _getBefore: (model.PadDynamics) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = jTrapKATEditor.currentPadNumber < 24 && _getBefore(jTrapKATEditor.pd) != _getAfter()

    protected def _setHelper(update: (model.PadDynamics, Byte) => Unit, name: String): Unit = {
        val currentPD = jTrapKATEditor.pd
        val before = _getBefore(currentPD)
        val after = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${name.capitalize}"
            def undoAction = setUndoRedo(() => update(currentPD, before))
            def redoAction = setUndoRedo(() => update(currentPD, after))
        })
        update(currentPD, after)
    }

    reactions += {
        case e: GlobalChanged if e.source == jTrapKATEditor && _isChg => setDisplay()
    }
}

trait GlobalBindings extends Bindings {
    protected def _getBefore: (model.Global[_ <: model.Pad]) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentGlobal) != _getAfter()

    protected def _setHelper(update: (model.Global[_ <: model.Pad], Byte) => Unit, name: String): Unit = {
        val global = jTrapKATEditor.currentGlobal
        val before = _getBefore(global)
        val after = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${name.capitalize}"
            def undoAction = setUndoRedo(() => update(global, before))
            def redoAction = setUndoRedo(() => update(global, after))
        })
        update(global, after)
    }

    reactions += {
        case e: GlobalChanged if e.source == jTrapKATEditor && _isChg => setDisplay()
    }
}

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
}

trait PadSelectionReactor extends DisplayBindings {
    reactions += {
        case e: SelectedPadChanged if _isChg => setDisplay()
    }
}

trait KitSelectionReactor extends DisplayBindings {
    reactions += {
        case e: SelectedKitChanged if _isChg => setDisplay()
    }
}

trait SoundControlSelectionReactor extends DisplayBindings {
    reactions += {
        case e: SelectedSoundControlChanged if _isChg => setDisplay()
    }
}

trait GlobalSelectionReactor extends DisplayBindings {
    reactions += {
        case e: SelectedGlobalChanged if _isChg => setDisplay()
    }
}

trait AllMemorySelectionReactor extends DisplayBindings {
    reactions += {
        case e: SelectedAllMemoryChanged if _isChg => setDisplay()
    }
}

trait PadValueReactor extends DisplayBindings {
    reactions += {
        case e: CurrentPadChanged if _isChg => setDisplay()
    }
}

trait KitValueReactor extends DisplayBindings {
    reactions += {
        case e: CurrentKitChanged if _isChg => setDisplay()
    }
}

trait SoundControlValueReactor extends DisplayBindings {
    reactions += {
        case e: CurrentSoundControlChanged if _isChg => setDisplay()
    }
}

trait GlobalValueReactor extends DisplayBindings {
    reactions += {
        case e: CurrentGlobalChanged if _isChg => setDisplay()
    }
}

trait AllMemoryValueReactor extends DisplayBindings {
    reactions += {
        case e: CurrentAllMemoryChanged if _isChg => setDisplay()
    }
}

trait AnyValueReactor extends DisplayBindings {
    reactions += {
        case e: SomethingChanged => setDisplay()
    }
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

trait ValueChangedBindings extends ValueBindings {
    reactions += {
        case e: ValueChanged => setValue()
    }
}

trait ButtonBindings extends ValueBindings {
    reactions += {
        case e: ButtonClicked => setValue()
    }
}

trait DocumentChangedBindings extends ValueBindings {
    reactions += {
        case e: DocumentChanged => setValue()
    }
}

trait Bindings extends AllMemorySelectionReactor with ValueBindings {
    override protected def setDisplay(): Unit = try {
        deafTo(this)
        super.setDisplay()
    } finally { listenTo(this) }

    protected def doUndoRedo(action: () => Unit) = try {
        deafTo(jTrapKATEditor)
        deafTo(this)
        action()
        _get()
        _chg()
    } finally { listenTo(this); listenTo(jTrapKATEditor) }

    listenTo(jTrapKATEditor)
}

trait ComboBoxBindings[T] extends Bindings {
    this: RichComboBox[T] =>
    override protected def setDisplay(): Unit = try {
        deafTo(selection)
        super.setDisplay()
    } finally { listenTo(selection) }

    override protected def doUndoRedo(action: () => Unit) = try {
        deafTo(selection)
        super.doUndoRedo(action)
    } finally { listenTo(selection) }

    listenTo(selection)

    reactions += {
        case SelectionChanged(source) => setValue()
    }
}

trait EditableComboBoxBindings[T] extends ComboBoxBindings[T] with DocumentChangedBindings { this: RichComboBox[T] => }

trait V3V4ComboBoxBindings[T, TP <: ComboBox[T], T3 <: TP, T4 <: TP] extends Bindings {
    this: V3V4ComboBox[T, TP, T3, T4] =>
    override protected def setDisplay(): Unit = try {
        deafTo(selection)
        super.setDisplay()
    } finally { listenTo(selection) }

    override protected def doUndoRedo(action: () => Unit) = try {
        deafTo(selection)
        super.doUndoRedo(action)
    } finally { listenTo(selection) }

    listenTo(selection)

    reactions += {
        case e: V3V4SelectionChanged => setValue()
    }
}

trait CurveComboBoxV3V4Bindings extends V3V4ComboBoxBindings[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] { this: V3V4ComboBox[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] => }

trait PadBindings extends Bindings with KitSelectionReactor with PadValueReactor {
    protected def _getCurrentPad: () => model.Pad
    protected def _getBefore: (model.Pad) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(_getCurrentPad()) != _getAfter()

    protected def _setHelper(update: (model.Pad, Byte) => Unit, name: String): Unit = {
        val currentPad = _getCurrentPad()
        val valueBefore = _getBefore(_getCurrentPad())
        val valueAfter = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionPad${name}"
            def undoAction = doUndoRedo(() => update(currentPad, valueBefore))
            def redoAction = doUndoRedo(() => update(currentPad, valueAfter))
        })
        update(currentPad, valueAfter)
    }
}

trait SelectedPadBindings extends PadBindings with PadSelectionReactor

trait KitVariesBindings extends Bindings with KitSelectionReactor {
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
            def undoAction = doUndoRedo(() => _updateHelper(currentKit, before, update))
            def redoAction = doUndoRedo(() => _updateHelper(currentKit, after, update))
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

trait KitBindings extends Bindings with KitSelectionReactor {
    protected def _getBefore: (model.Kit[_ <: model.Pad]) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentKit) != _getAfter()

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Byte) => Unit, name: String): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val before: Byte = _getBefore(currentKit)
        val after: Byte = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionKit${name}"
            def undoAction = doUndoRedo(() => update(currentKit, before))
            def redoAction = doUndoRedo(() => update(currentKit, after))
        })
        update(currentKit, after)
    }
}

trait SoundControlEnabledBindings extends Bindings with SoundControlSelectionReactor with KitSelectionReactor with ButtonBindings {
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentKit, jTrapKATEditor.currentSoundControlNumber) != _getAfter()
    protected def _getBefore: (model.Kit[_ <: model.Pad], Int) => Boolean
    protected def _getAfter: () => Boolean
    protected def _spn: spinner.Spinner

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Int, Boolean, spinner.Spinner, Int) => Unit, name: String): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val soundControl: Int = jTrapKATEditor.currentSoundControlNumber
        val before: Boolean = _getBefore(currentKit, soundControl)
        val after: Boolean = _getAfter()
        val _spnBefore = _spn.value.asInstanceOf[java.lang.Number].intValue()
        update(currentKit, soundControl, after, _spn, _spnBefore)
        val _spnAfter = _spn.value.asInstanceOf[java.lang.Number].intValue()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionSC${name}"
            def undoAction = doUndoRedo(() => update(currentKit, soundControl, before, _spn, _spnBefore))
            def redoAction = doUndoRedo(() => update(currentKit, soundControl, after, _spn, _spnAfter))
        })
    }
}

trait SoundControlBindings extends Bindings with SoundControlSelectionReactor with KitSelectionReactor {
    protected def _getBefore: (model.Kit[_ <: model.Pad], Int) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentKit, jTrapKATEditor.currentSoundControlNumber) != _getAfter()

    protected def _setHelper(update: (model.Kit[_ <: model.Pad], Int, Byte) => Unit, name: String): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val soundControl: Int = jTrapKATEditor.currentSoundControlNumber
        val before: Byte = _getBefore(currentKit, soundControl)
        val after: Byte = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionSC${name}"
            def undoAction = doUndoRedo(() => update(currentKit, soundControl, before))
            def redoAction = doUndoRedo(() => update(currentKit, soundControl, after))
        })
        update(currentKit, soundControl, after)
    }
}

trait GlobalPadDynamicsBindings extends Bindings with PadSelectionReactor with GlobalSelectionReactor {
    protected def _getBefore: (model.PadDynamics) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = jTrapKATEditor.currentPadNumber < 24 && _getBefore(jTrapKATEditor.pd) != _getAfter()

    protected def _setHelper(update: (model.PadDynamics, Byte) => Unit, name: String): Unit = {
        val currentPD = jTrapKATEditor.pd
        val before = _getBefore(currentPD)
        val after = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${name.capitalize}"
            def undoAction = doUndoRedo(() => update(currentPD, before))
            def redoAction = doUndoRedo(() => update(currentPD, after))
        })
        update(currentPD, after)
    }
}

trait GlobalBindings extends Bindings with GlobalSelectionReactor {
    protected def _getBefore: (model.Global[_ <: model.Pad]) => Byte
    protected def _getAfter: () => Byte
    protected def _isChg: Boolean = _getBefore(jTrapKATEditor.currentGlobal) != _getAfter()

    protected def _setHelper(update: (model.Global[_ <: model.Pad], Byte) => Unit, name: String): Unit = {
        val global = jTrapKATEditor.currentGlobal
        val before = _getBefore(global)
        val after = _getAfter()
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${name.capitalize}"
            def undoAction = doUndoRedo(() => update(global, before))
            def redoAction = doUndoRedo(() => update(global, after))
        })
        update(global, after)
    }
}

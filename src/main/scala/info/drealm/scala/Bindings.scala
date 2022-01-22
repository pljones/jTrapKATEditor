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
import info.drealm.scala.util.getInt

/*
 * A base trait for UI-affecting reactions
 * All events come from jTrapKATEditor
 * Nothing done in _uiReaction() should trigger the same event from jTrapKATEditor
 *
 * Applied to publishers, including Components and a few special classes
 */

trait UIReactor extends Publisher {

    protected def _isUIChange: Boolean
    protected def _uiReaction(): Unit

    protected def setDisplay(): Unit = {
        try {
            deafTo(this)
            _uiReaction()
        } finally { listenTo(this) }
    }

    listenTo(this)
    listenTo(jTrapKATEditor)
}

/*
 * A set of reactions to selected item events that require a UI update
 */

trait PadSelectionReactor extends UIReactor {
    reactions += {
        case e: SelectedPadChanged if _isUIChange => setDisplay()
    }
}

trait KitSelectionReactor extends UIReactor {
    reactions += {
        case e: SelectedKitChanged if _isUIChange => setDisplay()
    }
}

trait SoundControlSelectionReactor extends UIReactor {
    reactions += {
        case e: SelectedSoundControlChanged if _isUIChange => setDisplay()
    }
}

trait GlobalSelectionReactor extends UIReactor {
    reactions += {
        case e: SelectedGlobalChanged if _isUIChange => setDisplay()
    }
}

trait AllMemorySelectionReactor extends UIReactor {
    reactions += {
        case e: SelectedAllMemoryChanged if _isUIChange => setDisplay()
    }
}

/*
 * A set of reactions to Model events that require a UI update
 */

trait PadValueReactor extends UIReactor {
    reactions += {
        case e: CurrentPadChanged if _isUIChange => setDisplay()
    }
}

trait KitValueReactor extends UIReactor {
    reactions += {
        case e: CurrentKitChanged if _isUIChange => setDisplay()
    }
}

trait SoundControlValueReactor extends UIReactor {
    reactions += {
        case e: CurrentSoundControlChanged if _isUIChange => setDisplay()
    }
}

trait GlobalValueReactor extends UIReactor {
    reactions += {
        case e: CurrentGlobalChanged if _isUIChange => setDisplay()
    }
}

trait AllMemoryValueReactor extends UIReactor {
    reactions += {
        case e: CurrentAllMemoryChanged if _isUIChange => setDisplay()
    }
}

trait AnyValueReactor extends UIReactor {
    reactions += {
        case e: SomethingChanged if _isUIChange => setDisplay()
    }
}

/*
 * A base trait for Model-affecting reactions
 * All related events come from jTrapKATEditor
 */

trait ModelReactor extends Reactor {

    protected def _isModelChange: Boolean
    protected def _modelReaction(): Unit

    protected def setValue(): Unit = {
        try {
            deafTo(jTrapKATEditor)
            _modelReaction()
        } finally { listenTo(jTrapKATEditor) }
    }

    listenTo(jTrapKATEditor)
}

/*
 * A set of reactions to UI events used to trigger a Model update
 */

trait ValueChangedReactor extends ModelReactor {
    reactions += {
        case e: ValueChanged if _isModelChange => setValue()
    }
}

trait ButtonClickedReactor extends ModelReactor {
    reactions += {
        case e: ButtonClicked if _isModelChange => setValue()
    }
}

trait DocumentChangedReactor extends ModelReactor {
    reactions += {
        case e: DocumentChanged if _isModelChange => setValue()
    }
}

/*
 * RichComboBox needs to have its selection handled with care
 */

trait RichComboBoxReactor[T] extends UIReactor with ModelReactor { this: RichComboBox[T] =>

    override protected def setValue(): Unit = {
        try {
            deafTo(selection)
            super.setValue()
        } finally { listenTo(selection) }
    }

    override protected def setDisplay(): Unit = try {
        deafTo(selection)
        super.setDisplay()
    } finally { listenTo(selection) }

    listenTo(selection)

    reactions += {
        case e: SelectionChanged if _isModelChange => setValue()
    }
}

/*
 * No comment required...
 */

trait EditableComboBoxReactor[T] extends RichComboBoxReactor[T] with DocumentChangedReactor { this: RichComboBox[T] => }

/*
 * The majority of UI components bound to model values
 * -> require updating when the current all memory sysex dump changes (if not also global, kit or pad)
 * -> affect the model
 * so they use this base trait or one of the specialisations
 */

trait Bindings[T] extends AllMemorySelectionReactor with ModelReactor {
    // These are defined here as they're not as useful before this point in the model - and we can now more happily say "Byte"
    protected def _modelValue: T
    protected def _modelValue_=(value: T): Unit
    protected def _uiValue: T
    protected def _uiValue_=(value: T): Unit

    // In terms of change, model != UI?
    // Keep the getters as "cheap" as possible
    protected def _isUIChange: Boolean = _modelValue != _uiValue
    protected def _isModelChange: Boolean = _modelValue != _uiValue

    // And we may want to notify the change
    protected def _chg(): Unit

    // With the above in place, we have
    protected def _uiReaction() = _uiValue = _modelValue
    protected def _modelReaction() = _modelValue = _uiValue

    // We want all model-changing UI components to support undo/redo but how is specific to the component:
    // "action" has to reflect the state at the time of the original action and how to undo or redo it -
    // there may be a different current kit or pad, etc.
    protected def doUndoRedo(action: () => Unit) = try {
        deafTo(jTrapKATEditor)
        deafTo(this)
        action()
        _uiReaction()
        _chg()
    } finally { listenTo(this); listenTo(jTrapKATEditor) }
}

/*
 * No comment required...
 */

trait EditableComboBoxBindings[T] extends Bindings[Byte] with RichComboBoxReactor[T] { this: RichComboBox[T] => }

/*
 * Base trait for all model.Pad-affecting UI components.
 * Any change of selected kit or the value of the pad will need checking by the component.
 */

trait PadBindings extends Bindings[Byte] with KitSelectionReactor with PadValueReactor {
    // We have the following in place to work with
    //protected def _modelReaction() = _modelValue = _uiValue
    // _uiValue remains the component's responsibility for now

    protected def _pad: model.Pad
    protected def _padActionName: String
    protected final def _modelValue: Byte = _getModelValue(_pad)
    protected final def _modelValue_=(value: Byte) = {
        val currentPad = _pad
        val modelValue = _modelValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionPad${_padActionName}"
            def undoAction = doUndoRedo(() => _setModelValue(currentPad, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(currentPad, value))
        })
        _setModelValue(currentPad, value)
        _chg()
    }

    protected def _getModelValue: (model.Pad) => Byte
    protected def _setModelValue: (model.Pad, Byte) => Unit
}

/*
 * No comment required...
 */

trait SelectedPadBindings extends PadBindings with PadSelectionReactor {
    protected def _pad = jTrapKATEditor.currentPad
}

/*
 * Where the kit setting does not affect pad settings, this trait is used
 */
trait KitBindings extends Bindings[Byte] with KitSelectionReactor {
    protected def _kitActionName: String
    protected final def _modelValue: Byte = _getModelValue(jTrapKATEditor.currentKit)
    protected def _modelValue_=(value: Byte) = {
        val currentKit = jTrapKATEditor.currentKit
        val modelValue: Byte = _modelValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionKit${_kitActionName}"
            def undoAction = doUndoRedo(() => _setModelValue(currentKit, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(currentKit, value))
        })
        _setModelValue(currentKit, value)
        _chg()
    }

    protected def _getModelValue: (model.Kit[_ <: model.Pad]) => Byte
    protected def _setModelValue: (model.Kit[_ <: model.Pad], Byte) => Unit
}

/*
 * A number of kit settings may overrule the pad settings and use this trait
 */
trait KitVariesBindings extends KitBindings {

    // This is where KitVariesBindings inserts its _updateHelper call
    override protected final def _modelValue_=(value: Byte) = {
        val currentKit = jTrapKATEditor.currentKit
        val modelValue: Byte = _modelValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionKit${_kitActionName}"
            def undoAction = doUndoRedo(() => _updateHelper(currentKit, modelValue))
            def redoAction = doUndoRedo(() => _updateHelper(currentKit, value))
        })
        _updateHelper(currentKit, value)
    }

    // Here are the extra bits for a pad-affecting kit binding
    protected def _cp: Component
    protected def _isKit: (model.Kit[_ <: model.Pad]) => Boolean
    protected def _toKit: (model.Kit[_ <: model.Pad]) => Unit

    // This is where _setModelValue is called for KitVariesBindings
    private[this] def _updateHelper(kit: model.Kit[_ <: model.Pad], value: Byte) = {
        val isKit = _isKit(kit)
        _setModelValue(kit, value)
        if (isKit) {
            _toKit(kit)
            if (kit == jTrapKATEditor.currentKit)
                jTrapKATEditor.padChangedBy(_cp)
            _chg()
        }
    }
}

trait SoundControlBindings extends Bindings[Byte] with SoundControlSelectionReactor with KitSelectionReactor { this: info.drealm.scala.spinner.Spinner =>
    protected def _scActionName: String
    protected final def _modelValue: Byte = _getModelValue(jTrapKATEditor.currentKit, jTrapKATEditor.currentSoundControlNumber)
    protected final def _modelValue_=(value: Byte): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val soundControl: Int = jTrapKATEditor.currentSoundControlNumber
        val modelValue: Byte = _modelValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionSC${_scActionName}"
            def undoAction = doUndoRedo(() => _setModelValue(currentKit, soundControl, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(currentKit, soundControl, value))
        })
        _setModelValue(currentKit, soundControl, value)
        _chg()
    }

    protected final def _uiValue = value.asInstanceOf[java.lang.Number].intValue().toByte
    protected final def _chg() = jTrapKATEditor.soundControlChangedBy(this)

    protected def _getModelValue: (info.drealm.scala.model.Kit[_ <: info.drealm.scala.model.Pad], Int) => Byte
    protected def _setModelValue: (info.drealm.scala.model.Kit[_ <: info.drealm.scala.model.Pad], Int, Byte) => Unit
}

trait SoundControlEnabledBindings extends Bindings[Byte] with SoundControlSelectionReactor with KitSelectionReactor with ButtonClickedReactor { this: CheckBox =>
    protected def _sceActionName: String
    protected def _modelValue: Byte = _getModelValue(jTrapKATEditor.currentKit, jTrapKATEditor.currentSoundControlNumber)
    protected def _modelValue_=(value: Byte): Unit = {
        val currentKit = jTrapKATEditor.currentKit
        val soundControl: Int = jTrapKATEditor.currentSoundControlNumber
        val modelValue: Byte = _modelValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionSC${_sceActionName}"
            def undoAction = doUndoRedo(() => _setModelValue(currentKit, soundControl, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(currentKit, soundControl, value))
        })
        _setModelValue(currentKit, soundControl, value)
        _chg()
    }

    protected def _uiValue: Byte = if (selected) 1 else 0
    protected def _chg() = jTrapKATEditor.soundControlChangedBy(this)

    protected def _getModelValue: (model.Kit[_ <: model.Pad], Int) => Byte
    protected def _setModelValue: (model.Kit[_ <: model.Pad], Int, Byte) => Unit
}

trait GlobalBindings extends Bindings[Byte] with GlobalSelectionReactor with GlobalValueReactor {
    protected def _globalActionName: String
    protected def _modelValue: Byte = _getModelValue(jTrapKATEditor.currentGlobal)
    protected def _modelValue_=(value: Byte): Unit = {
        val global = jTrapKATEditor.currentGlobal
        val modelValue = _modelValue
        val value = _uiValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${_globalActionName.capitalize}"
            def undoAction = doUndoRedo(() => _setModelValue(global, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(global, value))
        })
        _setModelValue(global, value)
        _chg()
    }

    protected def _getModelValue: model.Global[_ <: model.Pad] => Byte
    protected def _setModelValue: (model.Global[_ <: model.Pad], Byte) => Unit
}

trait GlobalPadDynamicsBindings extends Bindings[Byte] with GlobalSelectionReactor with PadSelectionReactor with GlobalValueReactor with ValueChangedReactor { this: info.drealm.scala.spinner.Spinner =>
    protected def _pdActionName: String
    protected def _modelValue: Byte = _getModelValue(jTrapKATEditor.pd)
    protected def _modelValue_=(value: Byte): Unit = {
        val currentPD = jTrapKATEditor.pd
        val modelValue = _modelValue
        val value = _uiValue
        EditHistory.add(new HistoryAction {
            val actionName = s"actionGlobal${_pdActionName.capitalize}"
            def undoAction = doUndoRedo(() => _setModelValue(currentPD, modelValue))
            def redoAction = doUndoRedo(() => _setModelValue(currentPD, value))
        })
        _setModelValue(currentPD, value)
        _chg()
    }

    protected def _uiValue: Byte = value.asInstanceOf[java.lang.Number].byteValue()
    protected def _uiValue_=(_value: Byte): Unit = value = getInt(_value)

    override protected def _isUIChange = {
        visible = _visible()
        enabled = jTrapKATEditor.currentPadNumber < 25
        enabled && _uiValue != _modelValue
    }

    override protected def _uiReaction() = _uiValue = if (enabled) _modelValue else 0

    protected def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

    protected def _getModelValue: (info.drealm.scala.model.PadDynamics) => Byte
    protected def _setModelValue: (info.drealm.scala.model.PadDynamics, Byte) => Unit

    protected def _visible: () => Boolean
}

/*
 * As V3V4ComboBox is two things at once, it is easier to treat it specially to start with
 * It mirrors a RichComboBox - and this mirrors Bindings
 */

trait V3V4ComboBoxBindings[T, TP <: ComboBox[T], T3 <: TP, T4 <: TP] extends Bindings[Byte] {
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
        case e: V3V4SelectionChanged if _isModelChange => setValue()
    }
}

/*
 * No comment required...
 */

trait CurveComboBoxV3V4Bindings extends V3V4ComboBoxBindings[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] {
    this: V3V4ComboBox[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] =>
}

/*
 * PadSlotComboBoxV3V4 is the 28 pads/rims/pedals plus the additional 15 slots for each
 * Much of this may not be needed... it's just protecting stuff from the cbx events
 */

trait PadSlotComboBoxV3V4Bindings extends V3V4ComboBoxBindings[String, PadSlotComboBoxParent, PadSlotComboBoxV3, PadSlotComboBoxV4] with PadBindings with DocumentChangedReactor {
    this: PadSlotComboBoxV3V4 =>

    protected def _slot: Int
    protected def _getModelValue = _(_slot)
    protected def _setModelValue = (p, v) => p(_slot) = v

    // For a PadSlotComboBoxV3V4, value is magic
    protected def _uiValue: Byte = value.toByte
    protected def _uiValue_=(_value: Byte): Unit = value = _value

    override protected def _isUIChange: Boolean = cbx.selection.item != cbx.toString(_modelValue)

    protected def _chg() = jTrapKATEditor.padChangedBy(cbx)

    listenTo(prefs.Preferences)
    reactions += {
        case e: prefs.Preferences.NotesAsPreferencChanged if _isUIChange => setDisplay()
    }
}

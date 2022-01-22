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

import javax.swing.{ JComponent, JComboBox, JTextField, InputVerifier }
import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.{ Localization => L }
import info.drealm.scala.prefs.{ Preferences => P }

/**
 * NoteNameToNumber
 *
 * This trait provides a method to convert note names (in the form "F#6")
 * to note numbers in the MIDI range 0..127.
 *
 * @param octave This should be set to the octave number for middle C
 *               (i.e. 3 for C3=60 or 4 for C4=60).
 */
trait NoteNameToNumber {
    val notes = "C D EF G A B"
    val octave: Int
    /**
     * Used to convert note name to note number
     *
     * @param  value  String representation of a note (such as "C#2")
     * @return        The integer note number (based on the octave set for middle C)
     */
    def toNumber(value: String): Int = {
        (try { value.toInt } catch {
            case _: Throwable => {
                val noteName = value.trim().toLowerCase().capitalize
                val note = notes.indexOf(noteName.head)
                if (note < 0) throw new IllegalArgumentException("Invalid note")
                val sharp = noteName.drop(1).take(1) == "#"
                val flat = noteName.drop(1).take(1) == "b"
                val octaveNum = noteName.drop(1 + (if (sharp || flat) 1 else 0)).toInt - octave + 5
                if (octaveNum < 0 || octaveNum > 10) throw new IllegalArgumentException("Invalid octave")
                note +
                    (if (sharp) 1 else 0) +
                    (if (flat) -1 else 0) +
                    (octaveNum * 12)
            }
        }) match {
            case bad if bad < 0 || bad > 127 => throw new IllegalArgumentException("Note out of range")
            case good                        => good
        }
    }
}

/**
 * NoteNumberToName
 *
 * This trait provides a method to convert note numbers in the MIDI range 0..127
 * to note names (in the form "F#6").
 *
 * @param octave This should be set to the octave number for middle C
 *               (i.e. 3 for C3=60 or 4 for C4=60).
 */
trait NoteNumberToName {
    val notes = "C C#D D#E F F#G G#A A#B "
    val octave: Int
    /**
     * Used to convert note number to note name
     *
     * @param  number  Integer representation of a note (such as 64)
     * @return         The string note name
     */
    def toName(number: Int): String = {
        if (number < 0 || number > 127) throw new IllegalArgumentException("Note out of range")
        notes.drop((number % 12) * 2).take(2).trim() + ((number / 12) - 5 + octave)
    }
}

object DisplayMode extends Enumeration {
    type DisplayMode = Value
    val AsNumber, AsNamesC3, AsNamesC4 = Value
    val NotSet = Value(-1)
}

trait PadSlot {
    import DisplayMode._

    var displayMode: DisplayMode.DisplayMode = P.notesAs
    val padFunction: Seq[String]

    val toNumberC3 = (new NoteNameToNumber { val octave = 3 }).toNumber _
    val toNumberC4 = (new NoteNameToNumber { val octave = 4 }).toNumber _
    def toPadSlot(value: String): Byte = (padFunction.indexOf(value) match {
        case -1 => displayMode match {
            case AsNumber => value.toInt match {
                case bad if bad < 0 || bad > 127 => throw new IllegalArgumentException("Note out of range")
                case good                        => good
            }
            case AsNamesC3 => toNumberC3(value)
            case AsNamesC4 => toNumberC4(value)
        }
        case x => x + 128
    }).toByte

    val toNameC3 = (new NoteNumberToName { val octave = 3 }).toName _
    val toNameC4 = (new NoteNumberToName { val octave = 4 }).toName _
    def toString(value: Byte): String = (0x000000ff & value) match {
        case x if x < 128 => displayMode match {
            case AsNumber  => s"${x}"
            case AsNamesC3 => toNameC3(x)
            case AsNamesC4 => toNameC4(x)
        }
        case x if (x - 128) < padFunction.length => padFunction(x - 128)
        case x                                   => s"${x}"
    }
}

object PadSlotV3 extends PadSlot {
    override lazy val padFunction: Seq[String] = L.G("PadSlotV3").split("\n").toSeq
}

object PadSlotV4 extends PadSlot {
    override lazy val padFunction: Seq[String] = L.G("PadSlotV4").split("\n").toSeq
}

abstract class PadSlotComboBoxParent(padSlot: PadSlot, name: String, tooltip: String, stepped: Boolean = false) extends RichComboBox[String](padSlot.padFunction, name, tooltip, stepped = stepped) {
    makeEditable()
    editorPeer.setColumns(4)
    editorPeer.setInputVerifier(Verifier)
    selection.index = 0
    peer.setMaximumRowCount(padSlot.padFunction.length)

    if (stepped) {
        prototypeDisplayValue = Some("WWWW")
    }

    def value: Byte = padSlot.toPadSlot(selection.item)
    def value_=(_value: Byte): Unit = selection.item = padSlot.toString(_value)

    def toString(_value: Byte): String = padSlot.toString(_value)

    private[this] object Verifier extends InputVerifier {

        // Use pattern matching to neatly get the ComboBox
        // If the verifier is happy, tidy the value up and commit it
        // Either way, set the editor to the ComboBox value
        override def shouldYieldFocus(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] if super.shouldYieldFocus(c) => {
                        cb.setSelectedItem(e.getText())
                        if (cb.getSelectedIndex() == -1) {
                            e.setText(padSlot.toString(padSlot.toPadSlot(e.getText())))
                            cb.setSelectedItem(e.getText())
                        }
                        true
                    }
                    case cb: JComboBox[_] => {
                        e.setText(cb.getSelectedItem().asInstanceOf[String])
                        e.selectAll()
                        true
                    }
                    case _ => false
                }
                case _ => false
            }
        }

        // Use pattern matching to neatly get the ComboBox
        // Then see if we can convert the content of the editor to a PadSlot value
        def verify(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] => try {
                        padSlot.toPadSlot(e.getText())
                        true
                    } catch {
                        case _: Throwable => false
                    }
                    case _ => false
                }
                case _ => false
            }
        }
    }

}
class PadSlotComboBoxV3(name: String, tooltip: String, stepped: Boolean = false) extends PadSlotComboBoxParent(PadSlotV3, name + "V3", tooltip, stepped)
class PadSlotComboBoxV4(name: String, tooltip: String, stepped: Boolean = false) extends PadSlotComboBoxParent(PadSlotV4, name + "V4", tooltip, stepped)

abstract class PadSlotComboBoxV3V4(name: String, ttRoot: String, label: swing.Label, stepped: Boolean = false)
    extends V3V4ComboBox[String, PadSlotComboBoxParent, PadSlotComboBoxV3, PadSlotComboBoxV4] {

    val cbxV3: PadSlotComboBoxV3 = new PadSlotComboBoxV3(name, L.G(s"tt${ttRoot}V3"), stepped)
    val cbxV4: PadSlotComboBoxV4 = new PadSlotComboBoxV4(name, L.G(s"tt${ttRoot}V4"), stepped)
    val lbl: Label = label

    def value: Byte = cbx.value
    def value_=(value: Byte): Unit = cbx.value = value

    init()
}

object Pad {
    val padColorSelected = java.awt.SystemColor.textHighlight
    val padColorSelectedText = java.awt.SystemColor.textHighlightText
    val padColorPad = new Color(224, 255, 255)
    val padColorRim = new Color(230, 230, 250)
    val padColorPedal = new Color(228, 228, 228)
    val padColorText = java.awt.SystemColor.textText
}
class Pad(pad: Int) extends MigPanel("insets 4 2 4 2, hidemode 3", "[grow,right][fill,left]", "[]") with PadSelectionReactor {
    name = s"pnPad${pad}"

    protected def _isUIChange = true
    protected def _uiReaction = background = if (selected) Pad.padColorSelected else { if (pad < 11) Pad.padColorPad else { if (pad < 25) Pad.padColorRim else Pad.padColorPedal } }

    private[this] def selected = jTrapKATEditor.currentPadNumber == pad - 1

    private[this] val lblPad = new Label(if (pad < 25) s"${pad}" else L.G(s"lbPad${pad}")) with PadSelectionReactor {
        name = s"lblPad${pad}"

        protected def _isUIChange = true
        protected def _uiReaction = foreground = if (selected) Pad.padColorSelectedText else Pad.padColorText

        listenTo(jTrapKATEditor)

        setDisplay()
    }
    contents += (lblPad, "cell 0 0,alignx trailing,aligny baseline")

    private[this] val cbxPad = new PadSlotComboBoxV3V4(s"cbxPad${pad}", "Pad", lblPad, true) with PadSlotComboBoxV3V4Bindings {
        protected def _pad = jTrapKATEditor.currentKit(pad - 1)
        protected def _slot: Int = 0
        protected def _padActionName = "Value"

        setDisplay()

        override protected def setDisplay(): Unit = try {
            deafTo(cbxV3)
            deafTo(cbxV4)
            super.setDisplay()
        } finally { listenTo(cbxV3); listenTo(cbxV4) }

        override protected def setValue(): Unit = try {
            deafTo(cbxV3)
            deafTo(cbxV4)
            super.setValue()
        } finally { listenTo(cbxV3); listenTo(cbxV4) }

        override protected def doUndoRedo(action: () => Unit): Unit = try {
            deafTo(cbxV3)
            deafTo(cbxV4)
            super.doUndoRedo(action)
        } finally { listenTo(cbxV3); listenTo(cbxV4) }

        listenTo(cbxV3)
        listenTo(cbxV4)

        reactions += {
            case e: eventX.CbxEditorFocused if (e.source == cbxV3 || e.source == cbxV4) && (jTrapKATEditor.currentPadNumber != pad - 1) => try {
                deafTo(this)
                deafTo(cbxV3)
                deafTo(cbxV4)
                jTrapKATEditor.currentPadNumber = pad - 1
            } finally { listenTo(this); listenTo(cbxV3); listenTo(cbxV4) }
        }
    }
    contents += (cbxPad.cbxV3, "cell 1 0,grow")
    contents += (cbxPad.cbxV4, "cell 1 0,grow")
    tooltip = cbxPad.tooltip

    override def requestFocusInWindow() = cbxPad.requestFocusInWindow()

    listenTo(jTrapKATEditor)

    setDisplay()

    reactions += {
        case e: eventX.SelectedAllMemoryChanged => tooltip = cbxPad.tooltip
    }
}

class Slot(slot: Int) {
    val lblSlot = new Label(s"${slot}") { name = s"lblSlot${slot}"; peer.setDisplayedMnemonic(s"${slot}".last) }
    val cbxSlot = new PadSlotComboBoxV3V4(s"cbxSlot${slot}", "Slot", lblSlot) with PadSlotComboBoxV3V4Bindings with SelectedPadBindings {
        protected def _slot: Int = slot - 1
        protected def _padActionName = "Slot"

        private[this] def v3v4(f: () => Unit): Unit = jTrapKATEditor.doV3V4V5(if (slot <= 6) f(), f(), f())
        override protected def setValue = v3v4(super.setValue)
        override protected def setDisplay = v3v4(super.setDisplay)
        override protected def _isUIChange = jTrapKATEditor.doV3V4V5(if (slot <= 6) super._isUIChange else false, super._isUIChange, super._isUIChange)
        override protected def _uiReaction = v3v4(super._uiReaction)

        setDisplay()
    }
}

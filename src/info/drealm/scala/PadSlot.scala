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
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs, Localization => L }

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
            (octaveNum * 12) match {
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
    val padFunction: Seq[String]

    import DisplayMode._

    val toNumberC3 = (new NoteNameToNumber { val octave = 3 }).toNumber _
    val toNumberC4 = (new NoteNameToNumber { val octave = 4 }).toNumber _
    def toPadSlot(value: String): Byte = (padFunction.indexOf(value) match {
        case -1 => prefs.notesAs match {
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
        case x if x < 128 => prefs.notesAs match {
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

abstract class PadSlotComboBoxParent(v3v4: PadSlot, name: String, stepped: Boolean = false) extends RichComboBox[String](v3v4.padFunction, name, stepped) {
    makeEditable()
    editorPeer.setColumns(4)
    editorPeer.setInputVerifier(Verifier)
    selection.index = 0
    peer.setMaximumRowCount(v3v4.padFunction.length)

    if (stepped) {
        prototypeDisplayValue = Some("WWWW")
    }

    def value: Byte = v3v4.toPadSlot(selection.item)
    def value_=(value: Byte): Unit = selection.item = v3v4.toString(value)

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
                            e.setText(v3v4.toString(v3v4.toPadSlot(e.getText())))
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
                        v3v4.toPadSlot(e.getText())
                        true
                    }
                    catch {
                        case _: Throwable => false
                    }
                    case _ => false
                }
                case _ => false
            }
        }
    }

    private[this] var _displayMode: DisplayMode.DisplayMode = prefs.notesAs
    listenTo(jTrapKATEditorPreferences)
    reactions += {
        case e: jTrapKATEditorPreferences.NotesAsPreferencChanged if _displayMode != prefs.notesAs => {
            if (!v3v4.padFunction.contains(editorPeer.getText())) {
                val oldVal: Byte = (_displayMode match {
                    case DisplayMode.AsNumber  => editorPeer.getText().toInt
                    case DisplayMode.AsNamesC3 => v3v4.toNumberC3(editorPeer.getText())
                    case DisplayMode.AsNamesC4 => v3v4.toNumberC4(editorPeer.getText())
                }).toByte
                val newVal: String = prefs.notesAs match {
                    case DisplayMode.AsNumber  => s"${oldVal}"
                    case DisplayMode.AsNamesC3 => v3v4.toNameC3(oldVal)
                    case DisplayMode.AsNamesC4 => v3v4.toNameC4(oldVal)
                }
                editorPeer.setText(newVal)
                selection.item = newVal
            }
            _displayMode = prefs.notesAs
        }
    }
}
class PadSlotComboBoxV3(name: String, stepped: Boolean = false) extends PadSlotComboBoxParent(PadSlotV3, name + "V3", stepped)
class PadSlotComboBoxV4(name: String, stepped: Boolean = false) extends PadSlotComboBoxParent(PadSlotV4, name + "V4", stepped)

class PadSlotComboBoxV3V4(name: String, label: swing.Label, stepped: Boolean = false) extends V3V4ComboBox[String, PadSlotComboBoxParent, PadSlotComboBoxV3, PadSlotComboBoxV4] {
    def this(name: String) = this(name, null)

    val cbxV3: PadSlotComboBoxV3 = new PadSlotComboBoxV3(name, stepped)
    val cbxV4: PadSlotComboBoxV4 = new PadSlotComboBoxV4(name, stepped)
    val lbl: Label = label

    def requestFocus(): Unit = cbx.peer.getEditor().getEditorComponent().requestFocus()

    def value: Byte = cbx.value
    def value_=(value: Byte): Unit = cbx.value = value

    listenTo(cbxV3)
    listenTo(cbxV4)

    reactions += {
        case e: eventX.CbxEditorFocused => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: ValueChanged if e.source.isInstanceOf[PadSlotComboBoxParent] => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }
    init()
}

class Pad(pad: Int) extends MigPanel("insets 4 2 4 2, hidemode 3", "[grow,right][fill,left]", "[]") {
    name = s"pnPad${pad}"
    private[this] val lblPad = new Label(if (pad < 25) s"${pad}" else L.G(s"lbPad${pad}")) { name = s"lblPad${pad}" }
    private[this] val cbxPad = new PadSlotComboBoxV3V4(s"cbxPad${pad}", lblPad, true)
    contents += (lblPad, "cell 0 0,alignx trailing,aligny baseline")
    contents += (cbxPad.cbxV3, "cell 1 0,grow")
    contents += (cbxPad.cbxV4, "cell 1 0,grow")

    override def requestFocus() = cbxPad.requestFocus()

    private[this] def myPad = jTrapKATEditor.currentKit(pad - 1)

    private[this] def setDisplay(): Unit = {
        deafTo(cbxPad)
        cbxPad.value = myPad(0)
        listenTo(cbxPad)
    }

    private[this] def setValue(): Unit = {
        deafTo(jTrapKATEditor)
        myPad(0) = cbxPad.value
        jTrapKATEditor.padChangedBy(this)
        listenTo(jTrapKATEditor)
    }

    listenTo(cbxPad)
    listenTo(jTrapKATEditor)

    reactions += {
        case e: eventX.CbxEditorFocused => {
            deafTo(jTrapKATEditor)
            deafTo(this)
            jTrapKATEditor.currentPadNumber = pad - 1
            listenTo(this)
            listenTo(jTrapKATEditor)
        }
        case e: ValueChanged if (myPad(0) != cbxPad.value)                   => setValue()
        case e: eventX.CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
        case e: eventX.CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
    }

    setDisplay()
}

class Slot(slot: Int) extends Reactor {
    val lblSlot = new Label(s"${slot}") { name = s"lblSlot${slot}"; peer.setDisplayedMnemonic(s"${slot}".last) }
    val cbxSlot = new PadSlotComboBoxV3V4(s"cbxSlot${slot}", lblSlot)

    private[this] def setDisplay(): Unit = {
        deafTo(cbxSlot)
        cbxSlot.value = jTrapKATEditor.currentPad(slot - 1)
        listenTo(cbxSlot)
    }

    private[this] def setValue(value: Byte): Unit = {
        deafTo(jTrapKATEditor)
        jTrapKATEditor.currentPad(slot - 1) = cbxSlot.value
        jTrapKATEditor.padChangedBy(jTrapKATEditor.doV3V4(cbxSlot.cbxV3, cbxSlot.cbxV4))
        listenTo(jTrapKATEditor)
    }

    private[this] def v3v4(f: => Unit): Unit = jTrapKATEditor.doV3V4(if (slot <= 6) f, f)

    listenTo(cbxSlot)
    listenTo(jTrapKATEditor)

    reactions += {
        case e: ValueChanged if (jTrapKATEditor.currentPad(slot - 1) != cbxSlot.value) => v3v4(setValue(cbxSlot.value))
        case e: eventX.CurrentPadChanged if e.source == jTrapKATEditor                 => v3v4(setDisplay())
        case e: eventX.CurrentKitChanged if e.source == jTrapKATEditor                 => v3v4(setDisplay())
        case e: eventX.CurrentAllMemoryChanged if e.source == jTrapKATEditor           => v3v4(setDisplay())
    }
}

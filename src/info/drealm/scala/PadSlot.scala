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
            case AsNumber  => x.toString
            case AsNamesC3 => toNameC3(x)
            case AsNamesC4 => toNameC4(x)
        }
        case x if (x - 128) < padFunction.length => padFunction(x - 128)
        case x                                   => s"${x}"
    }
}

// For note names, we need to convert to 0..127 based on note and octave
// number.
// "octave" should be set to indicate the octave for middle C (note 60),
// e.g. 3 for C3=60 or 4 for C4=60.
trait NoteNameToNumber {
    val notes = "C D EF G A B"
    val octave: Int
    /**
     * Used to convert note name to note number
     *
     * @param noteName String representation of a note (such as "C#2")
     * @param octave   Number that follows "C" for middle C (note 60)
     * @return         The integer note number
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

// For note names, we need to convert to 0..127 based on note and octave
// number.
// "octave" should be set to indicate the octave for middle C (note 60),
// e.g. 3 for C3=60 or 4 for C4=60.
trait NoteNumberToName {
    val notes = "C C#D D#E F F#G G#A A#B "
    val octave: Int
    /**
     * Used to convert note number to note name
     *
     * @param number   Integer representation of a note (such as 64)
     * @param octave   Number that should follows "C" for middle C (note 60)
     * @return         The string note name
     */
    def toName(number: Int): String = {
        if (number < 0 || number > 127) throw new IllegalArgumentException("Note out of range")
        notes.drop((number % 12) * 2).take(2).trim() + ((number / 12) - 5 + octave)
    }
}

object PadSlotV3 extends PadSlot {
    override lazy val padFunction: Seq[String] = L.G("PadSlotV3").split("\n").toSeq
}

object PadSlotV4 extends PadSlot {
    override lazy val padFunction: Seq[String] = L.G("PadSlotV4").split("\n").toSeq
}

abstract class PadSlotComboBoxParent(v3v4: PadSlot, name: String) extends RichComboBox[String](v3v4.padFunction, name) {
    makeEditable()
    editorPeer.setColumns(4)
    editorPeer.setInputVerifier(Verifier)
    selection.index = 0
    peer.setMaximumRowCount(v3v4.padFunction.length)

    object Verifier extends InputVerifier {

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

    var _displayMode: DisplayMode.DisplayMode = prefs.notesAs
    listenTo(jTrapKATEditorPreferences)
    reactions += {
        case e: jTrapKATEditorPreferences.NotesAsPreferencChanged if _displayMode != prefs.notesAs && !v3v4.padFunction.contains(editorPeer.getText()) => {
            val oldVal: Byte = (_displayMode match {
                case DisplayMode.AsNumber  => editorPeer.getText().toInt
                case DisplayMode.AsNamesC3 => v3v4.toNumberC3(editorPeer.getText())
                case DisplayMode.AsNamesC4 => v3v4.toNumberC4(editorPeer.getText())
            }).toByte
            val newVal: String = prefs.notesAs match {
                case DisplayMode.AsNumber  => "" + oldVal
                case DisplayMode.AsNamesC3 => v3v4.toNameC3(oldVal)
                case DisplayMode.AsNamesC4 => v3v4.toNameC4(oldVal)
            }
            editorPeer.setText(newVal)
            selection.item = newVal
            _displayMode = prefs.notesAs
        }
    }
}
class PadSlotComboBoxV3(name: String) extends PadSlotComboBoxParent(PadSlotV3, name + "V3")
class PadSlotComboBoxV4(name: String) extends PadSlotComboBoxParent(PadSlotV4, name + "V4")

class PadSlotComboBoxV3V4(name: String, label: swing.Label, stepped: Boolean = false) extends V3V4ComboBox[String, PadSlotComboBoxParent, PadSlotComboBoxV3, PadSlotComboBoxV4] {
    def this(name: String) = this(name, null)

    var _value: String = (if (jTrapKATEditor.isV3) PadSlotV3 else PadSlotV4).padFunction(0)
    val cbxV3: PadSlotComboBoxV3 = new PadSlotComboBoxV3(name) {
        if (stepped) {
            prototypeDisplayValue = Some("WWWW")
            peer.setUI(SteppedComboBoxUI.getSteppedComboBoxUI(peer.asInstanceOf[JComboBox[_]]))
        }
    }
    val cbxV4: PadSlotComboBoxV4 = new PadSlotComboBoxV4(name) {
        if (stepped) {
            prototypeDisplayValue = Some("WWWW")
            peer.setUI(SteppedComboBoxUI.getSteppedComboBoxUI(peer.asInstanceOf[JComboBox[_]]))
        }
    }
    val lbl: Label = label

    def focus: Unit = cbx.peer.getEditor().getEditorComponent().requestFocus()

    listenTo(cbxV3)
    listenTo(cbxV4)

    reactions += {
        case e: eventX.CbxEditorFocused => {
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

    listenTo(cbxPad.cbxV3.selection)
    listenTo(cbxPad.cbxV4.selection)
    listenTo(cbxPad)

    override def requestFocus = cbxPad.focus

    reactions += {
        case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: eventX.CbxEditorFocused => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }
}

class Slot(slot: Integer) extends Reactor {
    val lblSlot = new Label("" + slot) { name = s"lblSlot${slot}" }
    val cbxSlot = new PadSlotComboBoxV3V4(s"cbxSlot${slot}", lblSlot)
}

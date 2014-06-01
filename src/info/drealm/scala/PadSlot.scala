/****************************************************************************
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
 ****************************************************************************/

package info.drealm.scala

import javax.swing.{ JComponent, JComboBox, JTextField, InputVerifier }
import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._

object PadSlot extends Publisher {
    val padFunction = Seq("Off", "Seq start", "Seq stop", "Seq cont", "Alt reset", "Next kit", "Prev kit")

    object DisplayMode extends Enumeration {
        type DisplayMode = Value
        val AsNumber, AsNamesC3, AsNamesC4 = Value
    }
    import DisplayMode._

    private[this] var _displayMode = AsNumber
    def displayMode: DisplayMode = _displayMode
    def displayMode_=(value: DisplayMode): Unit = {
        val oldMode = _displayMode
        _displayMode = value
        publish(new eventX.DisplayNotesAs(oldMode, _displayMode))
    }

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
            case AsNumber  => x.toString
            case AsNamesC3 => toNameC3(x)
            case AsNamesC4 => toNameC4(x)
        }
        case x if (x - 128) < padFunction.length => padFunction(x - 128)
        case x                                   => "" + x
    }

    def verifier = new InputVerifier {

        // Use pattern matching to neatly get the ComboBox
        // If the verifier is happy, tidy the value up and commit it
        // Either way, set the editor to the ComboBox value
        override def shouldYieldFocus(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] if super.shouldYieldFocus(c) => {
                        cb.setSelectedItem(e.getText())
                        if (cb.getSelectedIndex() == -1) {
                            e.setText(PadSlot.toString(toPadSlot(e.getText())))
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
                        toPadSlot(e.getText())
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

class PadSlotComboBox(name: String, label: swing.Label) extends RichComboBox[String](PadSlot.padFunction, name, label) {
    def this(name: String) = this(name, null)

    makeEditable()
    editorPeer.setColumns(4)
    editorPeer.setInputVerifier(PadSlot.verifier)
    selection.index = 0

    listenTo(PadSlot)
    reactions += {
        case e: eventX.DisplayNotesAs if e.oldMode != e.newMode && !PadSlot.padFunction.contains(editorPeer.getText()) => {
            val oldVal: Byte = (e.oldMode match {
                case PadSlot.DisplayMode.AsNumber  => editorPeer.getText().toInt
                case PadSlot.DisplayMode.AsNamesC3 => PadSlot.toNumberC3(editorPeer.getText())
                case PadSlot.DisplayMode.AsNamesC4 => PadSlot.toNumberC4(editorPeer.getText())
            }).toByte
            val newVal: String = e.newMode match {
                case PadSlot.DisplayMode.AsNumber  => "" + oldVal
                case PadSlot.DisplayMode.AsNamesC3 => PadSlot.toNameC3(oldVal)
                case PadSlot.DisplayMode.AsNamesC4 => PadSlot.toNameC4(oldVal)
            }
            Console.println(f"PadSlotComboBox $name  eventX.DisplayNotesAs ${e.oldMode} -> ${e.newMode} (edit: ${editorPeer.getText()}). oldVal=$oldVal -> newVal=$newVal")
            editorPeer.setText(newVal)
            selection.item = newVal
        }
    }
}

class Pad(pad: String) extends MigPanel("insets 4 2 4 2", "[grow,right][fill,left]", "[]") {

    name = "pnPad" + pad
    private[this] val lblPad = new Label("" + pad)
    private[this] val cbxPad = new PadSlotComboBox("cbxPad" + pad, lblPad)
    contents += (lblPad, "cell 0 0,alignx trailing,aligny baseline")
    contents += (cbxPad, "cell 1 0,grow")

    listenTo(cbxPad.selection)
    listenTo(cbxPad)

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

class Slot(private[this] val slot: Integer) extends Tuple3(slot, new Label("" + slot), new PadSlotComboBox("cbxSlot" + slot)) {
    _2.peer.setDisplayedMnemonic(("" + slot).last)
    // Uhhhhh, right...
    _2.peer.setLabelFor(_3.peer.asInstanceOf[java.awt.Component])
}

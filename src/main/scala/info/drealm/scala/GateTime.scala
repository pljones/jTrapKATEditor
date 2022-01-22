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

import info.drealm.scala.{ Localization => L }

object GateTime {
    val gateSelection = L.G("GateTime").split("\n").toSeq

    /*
     * Minor rant time.
     * If I want a signed byte value, I will say I want a signed value.
     * Byte should be 0 to 255.
     * No one ever uses "signed bytes" in the real world.  Ever...
     * Forcing use of larger data types by not supporting unsigned values at all
     * is simply ridiculous.
     * To avoid some of that, magic (intMask & byteValue) is used to up-cast
     * without sign extension, to keep the logic readable.
     */

    def toGateTime(value: String): Byte = gateSelection.indexOf(value.trim().toLowerCase().capitalize) match {
        case -1 => value.toDouble match {
            case tooSmall if tooSmall < 0.005 => throw new IllegalArgumentException("Numeric value must be 0.005 or over and under 6.4")
            case tooBig if tooBig >= 6.4      => throw new IllegalArgumentException("Numeric value must be 0.005 or over and under 6.4")
            case tiny if tiny < 0.4           => ((tiny - 0.005) / 0.005).toByte
            case medium if medium < 4.2       => (79 + ((medium - 0.4) / 0.025)).toByte
            case large                        => (231 + ((large - 4.2) / 0.1)).toByte
        }
        case x => (253 + x).toByte
    }
    def toString(v: Byte): String = (0x000000ff & v) match {
        case tiny if tiny <= 79      => f"${0.005 + (tiny * 0.005)}%.3f"
        case medium if medium <= 231 => f"${0.4 + ((medium - 79) * 0.025)}%.3f"
        case large if large < 253    => f"${4.2 + ((large - 231) * 0.1)}%.3f"
        case index                   => gateSelection(index - 253)
    }

    val verifier = new javax.swing.InputVerifier {

        import swing._
        import javax.swing.{ JComponent, JComboBox, JTextField }

        // Use pattern matching to neatly get the ComboBox
        // If super.shouldYieldFocus is true and this is not a dropdown value
        // round it to nearest known value
        override def shouldYieldFocus(c: JComponent, t: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] if super.shouldYieldFocus(c, t) => {
                        cb.setSelectedItem(e.getText())
                        if (cb.getSelectedIndex() == -1) {
                            e.setText(GateTime.toString(toGateTime(e.getText())))
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
        // Then see if we can convert the content of the editor to a GateTime value
        def verify(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] => try {
                        toGateTime(e.getText())
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

class GateTimeComboBox(name: String, tooltip: String, label: swing.Label) extends RichComboBox[String](GateTime.gateSelection, name, tooltip, label) {
    makeEditable()
    editorPeer.setInputVerifier(GateTime.verifier)
    selection.index = -1
    selection.item = 0.115f.toString()
}

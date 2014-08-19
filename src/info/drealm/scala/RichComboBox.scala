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

import javax.swing.JComboBox
import scala.swing.{ ComboBox, Label, Swing }
import scala.swing.event._

class RichComboBox[A](items: Seq[A], override val name: String, _label: Label, stepped: Boolean = false) extends ComboBox[A](items) {
    def this(items: Seq[A], _name: String, stepped: Boolean) = this(items, _name, null, stepped)
    def this(items: Seq[A], _name: String) = this(items, _name, null)
    def this(items: Seq[A], stepped: Boolean) = this(items, "", null, stepped)
    def this(items: Seq[A]) = this(items, "", null)

    if (stepped) {
        peer.setUI(SteppedComboBoxUI.getSteppedComboBoxUI(peer.asInstanceOf[JComboBox[_]]))
    }

    if (_label != null) {
        // Uhhhhh, right...
        _label.peer.setLabelFor(peer.asInstanceOf[JComboBox[_]])
    }

    var editreset: A = null.asInstanceOf[A]
    override def makeEditable()(implicit editor: ComboBox[A] => ComboBox.Editor[A]) {
        super.makeEditable()
        val theEditor = (editor(this).component)
        listenTo(this)
        listenTo(theEditor)
        listenTo(theEditor.keys)
        reactions += {
            case e: eventX.ItemDeselected => editreset = e.item.asInstanceOf[A]
            case e: eventX.ItemSelected if editorPeer.getInputVerifier() != null && !editorPeer.getInputVerifier().verify(editorPeer) => {
                deafTo(this)
                selection.item = editreset
                listenTo(this)
            }
            case e: FocusGained if e.source == theEditor => {
                deafTo(this)
                publish(new eventX.CbxEditorFocused(this))
                listenTo(this)
            }

            // I tried reading about key maps
            // This looked a lot easier :)
            // Catch ESC and replace editor text with current selected item value:
            case e: KeyTyped if e.char == 27 && e.modifiers == 0 => {
                editorPeer.setText(this.selection.item.asInstanceOf[String])
                editorPeer.selectAll()
            }
        }

        peer.addActionListener(Swing.ActionListener { e =>
            e match {
                case aep: java.awt.event.ActionEvent if e.getID() == java.awt.event.ActionEvent.ACTION_PERFORMED => aep match {
                    case cbxChg if aep.getActionCommand() == "comboBoxChanged" => publish(new ValueChanged(this))
                    case cbxEdt if aep.getActionCommand() == "comboBoxEdited" => //publish(new ValueChanged(this)) // Rather EditDone(...) but what is ...?
                    case _ => Console.println(s"Unknown action performed: ${aep.getActionCommand()}")
                }
                case _ => Console.println(s"Unexpected event: ${e}")
            }
        })
    }

    peer.addItemListener(ItemListener(
        e => {
            publish(new eventX.ItemDeselected(this, e.getItem()))
        },
        e => {
            publish(new eventX.ItemSelected(this, e.getItem()))
        }
    ))

    def editorPeer: javax.swing.JTextField = editable match {
        case true  => peer.getEditor().getEditorComponent().asInstanceOf[javax.swing.JTextField]
        case false => null
    }

    final def ItemListener(deselected: java.awt.event.ItemEvent => Unit,
                           selected: java.awt.event.ItemEvent => Unit) = new java.awt.event.ItemListener {
        def itemStateChanged(e: java.awt.event.ItemEvent) = e.getStateChange() match {
            case java.awt.event.ItemEvent.DESELECTED => deselected(e)
            case java.awt.event.ItemEvent.SELECTED   => selected(e)
        }
    }
}

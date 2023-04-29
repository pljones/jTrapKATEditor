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

// This trait is responsible for managing two things between V3 and V4:
// - merging the view of selections
// - switching visibility of the two ComboBoxes supplied
trait V3V4ComboBox[A, CP <: ComboBox[A], C1 <: CP, C2 <: CP] extends Publisher {

    val cbxV3: C1
    val cbxV4: C2
    val lbl: Label
    protected def cbx: CP = jTrapKATEditor.doV3V4V5(cbxV3, cbxV4, cbxV4)
    private[this] def peer = cbx.peer.asInstanceOf[javax.swing.JComboBox[_]]

    private[this] def selectionV3 = cbxV3.selection
    private[this] def selectionV4 = cbxV4.selection
    object selection extends Publisher {
        def index: Int = cbx.selection.index
        def index_=(n: Int): Unit = {
            val nn = if (n >= peer.getItemCount) -1 else n
            selectionV3.index = nn
            selectionV4.index = nn
        }
        def item: A = cbx.selection.item
        def item_=(a: A): Unit = {
            selectionV3.item = a
            selectionV4.item = a
        }

        listenTo(selectionV3)
        listenTo(selectionV4)
        reactions += {
            case e: event.SelectionChanged => publish(new eventX.V3V4SelectionChanged(V3V4ComboBox.this))
        }
    }

    // What we are here for is to handle
    private[this] var _visible: Boolean = true
    def v3v4visible: Boolean = _visible && cbx.visible
    def v3v4visible_=(value: Boolean): Unit = {
        _visible = value
        cbxV3.visible = jTrapKATEditor.doV3V4V5(_visible, false, false)
        cbxV4.visible = jTrapKATEditor.doV3V4V5(false, _visible, _visible)
    }

    def enabled: Boolean = cbx.enabled
    def enabled_=(value: Boolean): Unit = { cbxV3.enabled = value; cbxV4.enabled = value }

    def requestFocusInWindow() = cbx.requestFocusInWindow()

    def tooltip: String = cbx.tooltip

    private[this] def allMemoryChanged(toVisible: CP, toHidden: CP): Unit = {
        // Need to avoid creeping focus when this control disappears from the UI
        toVisible.visible = _visible
        if (toHidden.hasFocus || (toHidden.editable && toHidden.peer.getEditor().getEditorComponent().hasFocus())) toVisible.requestFocusInWindow()
        toHidden.visible = false
        if (lbl != null) {
            // Uhhhhh, right...
            lbl.peer.setLabelFor(toVisible.peer.asInstanceOf[java.awt.Component])
            if (cbx.tooltip != null) lbl.tooltip = cbx.tooltip
        }
    }

    protected final def init() = jTrapKATEditor.doV3V4V5(allMemoryChanged(cbxV3, cbxV4), allMemoryChanged(cbxV4, cbxV3), allMemoryChanged(cbxV4, cbxV3))

    listenTo(jTrapKATEditor)

    reactions += {
        case e: eventX.SelectedAllMemoryChanged => init()
    }
}

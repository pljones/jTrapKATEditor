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
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._
import info.drealm.scala.{ Localization => L }

object pnSelector extends MigPanel("insets 0", "[][][][grow,fill][][][grow,fill][][][]", "[]") {
    name = "pnSelector"

    private[this] val lblSelectKit = new Label(L.G("lblSelectKit")) { peer.setDisplayedMnemonic(L.G("mneSelectKit").charAt(0)) }
    contents += (lblSelectKit, "cell 0 0,alignx right")

    private[this] val kitNames = new Array[String](24)
    private[this] val cbxSelectKit = new RichComboBox(kitNames, "cbxSelectKit", L.G("ttSelectKit"), lblSelectKit) with ComboBoxBindings[String] {
        peer.setMaximumRowCount(24)
        prototypeDisplayValue = Some("WWWWWWWWWWWW")

        private[this] def updateKitName(idx: Int): Unit = kitNames(idx) = s"${idx + 1}: ${jTrapKATEditor.currentAllMemory(idx).kitName}"
        private[this] def updateAllKitNames(): Unit = (0 to 23) foreach updateKitName _
        protected override def _get() = selection.index = jTrapKATEditor.currentKitNumber
        protected override def _set() = jTrapKATEditor.currentKitNumber = selection.index
        protected override def _chg() = {}

        reactions += {
            case e: CurrentKitChanged if e.source == txtKitName || e.source == jTrapKATEditor => { updateKitName(jTrapKATEditor.currentKitNumber); setDisplay() }
            case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor                     => { updateAllKitNames(); setDisplay() }
        }

        updateAllKitNames()
        setDisplay()
    }
    contents += (cbxSelectKit, "cell 1 0")

    private[this] val lblKitEdited = new Label(L.G("lblXEdited")) {
        tooltip = L.G("ttKitEdited")
        private[this] def setDisplay() = visible = jTrapKATEditor.currentKit.changed

        listenTo(jTrapKATEditor)

        reactions += {
            case e: CurrentKitChanged                                     => setDisplay()
            case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
        }

        setDisplay()
    }
    contents += (lblKitEdited, "cell 2 0")

    private[this] val lblKitName = new Label(L.G("lblKitName"))
    contents += (lblKitName, "cell 4 0,alignx right")

    private[this] val txtKitName = new TextField with Bindings {
        name = "txtKitName"
        columns = 16
        tooltip = L.G("ttKitName")
        lblKitName.peer.setLabelFor(peer)
        lblKitName.tooltip = tooltip

        protected override def _get() = text = jTrapKATEditor.currentKit.kitName.trim()
        protected override def _set() = jTrapKATEditor.currentKit.kitName = text
        protected override def _chg() = jTrapKATEditor.kitChangedBy(this)

        reactions += {
            case e: ValueChanged => setValue()
        }

        setDisplay()
    }
    contents += (txtKitName, "cell 5 0")

    private[this] val lblSelectPad = new Label(L.G("lblSelectPad")) { peer.setDisplayedMnemonic(L.G("mneSelectPad").charAt(0)) }
    contents += (lblSelectPad, "cell 7 0,alignx right")

    private[this] val cbxSelectPad = new RichComboBox((1 to 28) map (x => x match {
        case x if x < 25 => s"${x}"
        case x           => L.G(s"lbPad${x}")
    }), "cbxSelectPad", L.G("ttSelectPad"), lblSelectPad) with ComboBoxBindings[String] {
        peer.setMaximumRowCount(24)
        prototypeDisplayValue = Some("88 mmmm")
        protected override def _get() = selection.index = jTrapKATEditor.currentPadNumber
        protected override def _set() = jTrapKATEditor.currentPadNumber = selection.index
        protected override def _chg() = {}

        reactions += {
            case e: CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
        }

        setDisplay()
    }
    contents += (cbxSelectPad, "cell 8 0")

    private[this] val lblPadEdited = new Label(L.G("lblXEdited")) {
        tooltip = L.G("ttPadEdited")
        private[this] def setDisplay(): Unit = visible = jTrapKATEditor.currentPad.changed

        listenTo(jTrapKATEditor)

        reactions += {
            case e: CurrentPadChanged                                     => setDisplay()
            case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
            case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
        }

        setDisplay()
    }
    contents += (lblPadEdited, "cell 9 0")
}

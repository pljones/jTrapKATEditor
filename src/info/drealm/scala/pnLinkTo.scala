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

object pnLinkTo extends MigPanel("insets 5", "[grow,right][left,fill]", "[]") {
    name = "pnLinkTo"
    tooltip = L.G("ttLinkTo")

    private[this] val lblLinkTo = new Label(L.G("lblLinkTo")) { peer.setDisplayedMnemonic(L.G("mneLinkTo").charAt(0)) }
    contents += (lblLinkTo, "cell 0 0")

    private[this] val linkTo: Array[String] = new Array[String](28)
    private[this] val cbxLinkTo = new RichComboBox(linkTo, "cbxLinkTo", tooltip, lblLinkTo) with ComboBoxBindings[String] with SelectedPadBindings {
        prototypeDisplayValue = Some("88 mmmm")
        peer.setMaximumRowCount(24)

        protected def _setHelper(update: (model.PadV4, Int, Byte) => Unit): Unit = {
            val currentPad = _getCurrentPad()
            val currentPadNo = _currentPadNo()
            val valueBefore = _getBefore(currentPad)
            val valueAfter = _getAfter()
            EditHistory.add(new HistoryAction {
                val actionName = "actionLinkTo"
                def undoAction = doUndoRedo(() => update(currentPad, currentPadNo, valueBefore))
                def redoAction = doUndoRedo(() => update(currentPad, currentPadNo, valueAfter))
            })
            update(currentPad, currentPadNo, valueAfter)
        }

        protected def _getCurrentPad = () => jTrapKATEditor.currentKitV4(_currentPadNo())
        protected def _currentPadNo = () => jTrapKATEditor.currentPadNumber
        protected def _getBefore = _.asInstanceOf[model.PadV4].linkTo
        protected def _getAfter = () => getLinkTo(_currentPadNo(), selection.index)
        override protected def _isChg = jTrapKATEditor.doV3V4(false, super._isChg)

        protected def _get() = selection.index = getSelectionIndex(_getBefore(_getCurrentPad()))
        protected def _set() = _setHelper((p, n, v) => p.linkTo = getLinkTo(n, v))
        protected def _chg() = jTrapKATEditor.padChangedBy(this)

        protected override def setDisplay = jTrapKATEditor.doV3V4({}, super.setDisplay())
        protected override def setValue = jTrapKATEditor.doV3V4({}, super.setValue())

        private[this] def getSelectionIndex(linkTo: Byte): Int = linkTo - 1 match {
            case e if e == _currentPadNo() => 0 // Equal means Off
            case e if e < _currentPadNo() => e + 1 // Before
            case e => e // After
        }
        private[this] def getLinkTo(padNo: Int, selectionIndex: Int): Byte = (selectionIndex match {
            case 0 => padNo + 1
            case e if e + 1 < padNo => e
            case e => e + 1
        }).toByte

        private[this] def setAllKitLinks(pad: Int): Unit = ((0 to 28) filter (x => x != pad) map (x => x match {
            case 0 => L.G("cbxLinkToOff")
            case x if x < 25 => s"${x}"
            case x => L.G(s"lbPad${x}")
        }) zip (0 to 27)) foreach (x => linkTo(x._2) = x._1)

        jTrapKATEditor.doV3V4({}, {
            setAllKitLinks(jTrapKATEditor.currentPadNumber + 1)
            setDisplay()
        })
    }
    contents += (cbxLinkTo, "cell 1 0")

    listenTo(jTrapKATEditor)

    reactions += {
        case e: SelectedAllMemoryChanged => visible = jTrapKATEditor.doV3V4(false, true)
    }
}

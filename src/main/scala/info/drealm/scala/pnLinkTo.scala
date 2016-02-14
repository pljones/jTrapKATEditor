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
    private[this] val cbxLinkTo = new RichComboBox(linkTo, "cbxLinkTo", tooltip, lblLinkTo) with SelectedPadBindings with RichComboBoxReactor[String] {
        prototypeDisplayValue = Some("88 mmmm")
        peer.setMaximumRowCount(24)

        protected def _padActionName = "LinkTo"
        protected def _getModelValue = _.asInstanceOf[model.PadV4].linkTo
        protected def _setModelValue = _.asInstanceOf[model.PadV4].linkTo = _

        protected def _uiValue: Byte = {
            val padNo = jTrapKATEditor.currentPadNumber + 1
            (selection.index match {
                case 0              => padNo
                case e if e < padNo => e
                case e              => e + 1
            }).toByte
        }
        protected def _uiValue_=(_value: Byte): Unit = {
            val padNo = jTrapKATEditor.currentPadNumber
            selection.index = _value - 1 match {
                case e if e == padNo => 0 // Equal means Off
                case e if e < padNo  => e + 1 // Before
                case e               => e // After
            }
        }

        protected def _chg() = jTrapKATEditor.padChangedBy(this)

        override protected def setDisplay = jTrapKATEditor.doV3V4({}, super.setDisplay())
        override protected def setValue = jTrapKATEditor.doV3V4({}, super.setValue())
        override protected def _isUIChange = jTrapKATEditor.doV3V4(false, super._isUIChange)
        override protected def _uiReaction = jTrapKATEditor.doV3V4({}, super._uiReaction)

        private[this] def setAllKitLinks(pad: Int): Unit = ((0 to 28) filter (x => x != pad) map (x => x match {
            case 0           => L.G("cbxLinkToOff")
            case x if x < 25 => s"${x}"
            case x           => L.G(s"lbPad${x}")
        }) zip (0 to 27)) foreach (x => linkTo(x._2) = x._1)

        reactions += {
            case e: SelectedPadChanged       => jTrapKATEditor.doV3V4({}, setAllKitLinks(jTrapKATEditor.currentPadNumber + 1))
            case e: SelectedKitChanged       => jTrapKATEditor.doV3V4({}, setAllKitLinks(jTrapKATEditor.currentPadNumber + 1))
            case e: SelectedAllMemoryChanged => jTrapKATEditor.doV3V4({}, setAllKitLinks(jTrapKATEditor.currentPadNumber + 1))
        }

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

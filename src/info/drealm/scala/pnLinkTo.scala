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
    private[this] val cbxLinkTo = new RichComboBox(linkTo, "cbxLinkTo", tooltip, lblLinkTo) with ComboBoxBindings[String] {
        prototypeDisplayValue = Some("88 mmmm")
        peer.setMaximumRowCount(24)

        private[this] def setAllKitLinks(pad: Int): Unit = ((0 to 28) filter (x => x != pad) map (x => x match {
            case 0           => L.G("cbxLinkToOff")
            case x if x < 25 => s"${x}"
            case x           => L.G(s"lbPad${x}")
        }) zip (0 to 27)) foreach (x => linkTo(x._2) = x._1)

        private[this] def getSelectionIndex(pad: Int): Int = pad match {
            case e if e == jTrapKATEditor.currentPadNumber => 0 // Equal means Off
            case e if e < jTrapKATEditor.currentPadNumber  => e + 1 // Before
            case e                                         => e // After
        }

        protected override def _get() = {
            setAllKitLinks(jTrapKATEditor.currentPadNumber + 1)
            selection.index = getSelectionIndex(jTrapKATEditor.currentPadV4.linkTo - 1)
        }
        protected override def _set() = jTrapKATEditor.currentPadV4.linkTo = (selection.index match {
            case 0 => jTrapKATEditor.currentPadNumber + 1
            case e if e + 1 < jTrapKATEditor.currentPadNumber => e
            case e => e + 1
        }).toByte
        protected override def _chg() = jTrapKATEditor.padChangedBy(this)

        override def setDisplay = jTrapKATEditor.doV3V4({}, super.setDisplay())
        override def setValue = jTrapKATEditor.doV3V4({}, super.setValue())

        reactions += {
            case e: CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
        }

        setDisplay()
    }
    contents += (cbxLinkTo, "cell 1 0")

    listenTo(jTrapKATEditor)

    reactions += {
        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => visible = jTrapKATEditor.doV3V4(false, true)
    }
}

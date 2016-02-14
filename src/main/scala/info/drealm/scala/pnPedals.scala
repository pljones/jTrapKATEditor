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

import javax.swing.border._
import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.spinner._
import info.drealm.scala.eventX._
import info.drealm.scala.layout._
import info.drealm.scala.{ Localization => L }

object pnPedals extends MigPanel("insets 0", "[grow,leading][][][grow,fill][][grow,fill][][][grow,trailing]", "[]") {
    name = "pnPedals"

    Seq(("cell 1 0", 25), ("cell 2 0", 26), ("cell 6 0", 27), ("cell 7 0", 28)) foreach (pad => {
        val pn = new Pad(pad._2)
        contents += (pn, pad._1 + ",gapx 1, pad 0 -1 0 1,grow")
        pn
    })

    object pnHH extends MigPanel("insets 2, gap 0", "[grow,right][left,fill]", "[]") {
        name = "pnHH"
        tooltip = L.G("ttHH")
        border = new LineBorder(java.awt.Color.BLACK)

        private[this] val lblHH = new Label(L.G("lblHH")) { peer.setDisplayedMnemonic(L.G("mneHH").charAt(0)) }
        contents += (lblHH, "cell 0 0,alignx trailing,aligny baseline, gapafter 2")

        (1 to 4) foreach { x =>
            val cbxHH = new RichComboBox(Seq(L.G("cbxHHOff")) ++ ((1 to 24) map (p => s"${p}")), s"cbxHH${x}", pnHH.tooltip, if (x == 1) lblHH else null) with Bindings[Byte] with KitSelectionReactor with RichComboBoxReactor[String] {
                peer.setMaximumRowCount(13)
                name = s"cbxHH${x}"

                protected final def _modelValue: Byte = jTrapKATEditor.currentKit.hhPads(x - 1)
                protected final def _modelValue_=(value: Byte) = {
                    val kit = jTrapKATEditor.currentKit
                    val modelValue = _modelValue
                    EditHistory.add(new HistoryAction {
                        val actionName = "actionHH"
                        def undoAction = doUndoRedo(() => _setModelValue(kit, value, modelValue))
                        def redoAction = doUndoRedo(() => _setModelValue(kit, modelValue, value))
                    })
                    _setModelValue(kit, modelValue, value)
                }

                protected def _uiValue: Byte = selection.index.toByte
                protected def _uiValue_=(_value: Byte): Unit = selection.index = _value

                // This mess needs to happen on undo/redo as well as UI updates
                def _setModelValue(kit: model.Kit[_ <: model.Pad], was: Byte, now: Byte) = {
                    val padChangeSideEffect =
                        (if (was > 0 && kit(was - 1).flags != (0x7f & kit(was - 1).flags).toByte) { kit(was - 1).flags = (0x7f & kit(was - 1).flags).toByte; true } else false) ||
                            (if (now > 0 && kit(now - 1).flags != (0x80 | kit(now - 1).flags).toByte) { kit(now - 1).flags = (0x80 | kit(now - 1).flags).toByte; true } else false)
                    kit.hhPads(x - 1, now)
                    if (padChangeSideEffect && (kit(was) == jTrapKATEditor.currentPad || kit(now) == jTrapKATEditor.currentPad))
                        jTrapKATEditor.padChangedBy(this)
                }
                protected def _chg() = jTrapKATEditor.kitChangedBy(pnHH)

                setDisplay()
            }
            contents += (cbxHH, s"cell ${x} 0, grow")
        }
    }
    contents += (pnHH, "cell 4 0")

    peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((25 to 28) flatMap { n => Seq(s"cbxPad${n}V3", s"cbxPad${n}V4") }) ++ ((1 to 4) map (x => s"cbxHH${x}"))) {
        override def getDefaultComponent(pn: java.awt.Container): java.awt.Component = jTrapKATEditor.currentPadNumber match {
            case x if x > 23 => {
                val _defaultCp = s"cbxPad${jTrapKATEditor.currentPadNumber + 1}${jTrapKATEditor.doV3V4("V3", "V4")}"
                if (containerValid(pn)) stepBy(getPeer(_defaultCp), _ + 1, true) else null
            }
            case _ => getFirstComponent(pn)
        }
    })
    peer.setFocusTraversalPolicyProvider(true)
}

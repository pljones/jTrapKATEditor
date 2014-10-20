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
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._
import info.drealm.scala.layout._
import info.drealm.scala.{ Localization => L }

object pnPads extends MigPanel("insets 0, gapx 2", "[grow][grow][grow][grow][grow][grow][grow][grow]", "[][][][]") {
    name = "pnPads"

    (for {
        row <- (0 to 3) zip Seq(
            List(0, 0, 18, 19, 20, 21, 0, 0),
            List(0, 17, 6, 7, 8, 9, 22, 0),
            List(16, 5, 1, 2, 3, 4, 10, 23),
            List(15, 0, 11, 12, 13, 14, 0, 24)
        );
        col <- (0 to 7) zip row._2;
        if col._2 != 0
    } yield (s"cell ${col._1} ${row._1}", col._2)) foreach { pad =>
        val pn = new Pad(pad._2)
        contents += (pn, pad._1 + ",grow")
        pn
    }

    peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((1 to 24) flatMap { n => Seq(s"cbxPad${n}V3", s"cbxPad${n}V4") })) {
        override def getDefaultComponent(pn: java.awt.Container): java.awt.Component = jTrapKATEditor.currentPadNumber match {
            case x if x < 24 => {
                val _defaultCp = s"cbxPad${jTrapKATEditor.currentPadNumber + 1}${jTrapKATEditor.doV3V4("V3", "V4")}"
                if (containerValid(pn)) stepBy(getPeer(_defaultCp), _ + 1, true) else null
            }
            case _ => null
        }
    })
    peer.setFocusTraversalPolicyProvider(true)
}

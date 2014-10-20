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

import swing.Label
import info.drealm.scala.eventX._
import info.drealm.scala.{ Localization => L }

trait Curve {
    def curveSelection: Seq[String]
}

object CurveV3 extends Curve {
    val curveSelection = L.G("CurvesV3").split("\n").toSeq
}

object CurveV4 extends Curve {
    val curveSelection = L.G("CurvesV4").split("\n").toSeq
}

abstract class CurveComboBoxParent(seq: Seq[String], _name: String, tooltip: String) extends RichComboBox[String](seq, _name, tooltip)
class CurveComboBoxV3(_name: String, tooltip: String) extends CurveComboBoxParent(CurveV3.curveSelection, _name + "V3", tooltip)
class CurveComboBoxV4(_name: String, tooltip: String) extends CurveComboBoxParent(CurveV4.curveSelection, _name + "V4", tooltip)

class CurveComboBoxV3V4(_name: String, tooltip: String, label: Label, _getVal: () => Byte, _setVal: Byte => Unit, _chgBy: CurveComboBoxParent => Unit)
    extends V3V4ComboBox[String, CurveComboBoxParent, CurveComboBoxV3, CurveComboBoxV4] with Bindings {

    val cbxV3: CurveComboBoxV3 = new CurveComboBoxV3(_name, tooltip)
    val cbxV4: CurveComboBoxV4 = new CurveComboBoxV4(_name, tooltip)
    val lbl: Label = label
    protected override def _get() = {
        try {
            deafTo(selection)
            selection.index = _getVal()
        }
        catch { case e: Exception => e.printStackTrace() }
        finally { listenTo(selection) }
    }
    protected override def _set() = _setVal(selection.index.toByte)
    protected override def _chg() = _chgBy(cbx)

    listenTo(selection)

    reactions += {
        case e: V3V4SelectionChanged => setValue()
    }

    init()
}

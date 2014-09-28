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

import javax.swing.border.TitledBorder
import swing._
import info.drealm.scala.migPanel.MigPanel
import info.drealm.scala.spinner.Spinner
import info.drealm.scala.eventX._
import info.drealm.scala.{ Localization => L }

object pnGlobal extends MigPanel("insets 5", "[]", "[]") {
    name = "pnGlobal"

    // Unsigned byte, please...
    private[this] def getInt(value: Byte) = 0x000000ff & value

    trait GlobalComponentParams
    case class GlobalSpinnerParams(ini: Int, min: Int, max: Int) extends GlobalComponentParams
    case class GlobalComboBoxParams(items: Seq[String]) extends GlobalComponentParams
    case class EditableGlobalComboBoxParams(_toItem: (Byte, Seq[String]) => String, _fromItem: (Int, String) => Byte, items: Seq[String], _verifier: (Int, String) => Boolean) extends GlobalComponentParams

    private[this] class GlobalSpinner(_name: String, lbl: Label, _getVal: () => Byte, _setVal: (Byte) => Unit, params: GlobalSpinnerParams)
        extends Spinner(new javax.swing.SpinnerNumberModel(params.ini, params.min, params.max, 1), s"spn${_name.capitalize}", lbl) with GlobalBindings {

        protected override def _get() = value = getInt(_getVal())
        protected override def _set() = _setVal(value.asInstanceOf[java.lang.Number].byteValue())
        protected override def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

        setDisplay()
    }

    private[this] class GlobalComboBox(_name: String, lbl: Label, _getVal: () => Byte, _setVal: (Byte) => Unit, params: GlobalComboBoxParams)
        extends RichComboBox(params.items, s"cbx${_name.capitalize}", lbl) with ComboBoxBindings[String] with GlobalBindings {

        protected override def _get() = selection.item = params.items(_getVal())
        protected override def _set() = _setVal(selection.index.toByte)
        protected override def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

        setDisplay()
    }

    private[this] class OffOnGlobalComboBox(_name: String, lbl: Label, _getVal: () => Byte, _setVal: (Byte) => Unit)
        extends GlobalComboBox(_name, lbl, () => _getVal(), value => _setVal(value), GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("itemOn"))))

    private[this] class EditableGlobalComboBox(_name: String, lbl: Label, _getVal: () => Byte, _setVal: (Byte) => Unit, params: EditableGlobalComboBoxParams)
        extends GlobalComboBox(_name, lbl, () => _getVal(), (value) => _setVal(value), GlobalComboBoxParams(params.items)) {

        protected override def _get() = selection.item = params._toItem(_getVal(), params.items)
        protected override def _set() = _setVal(params._fromItem(selection.index, selection.item))
        protected override def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

        makeEditable()
        editorPeer.setInputVerifier(new javax.swing.InputVerifier {
            def verify(input: javax.swing.JComponent) = params._verifier(selection.index, selection.item)
        })
    }

    private[this] class GlobalPanel(_name: String) extends MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
        name = s"pnGlobal${_name}"
        border = new TitledBorder(L.G(name))

        private[this] var row = 0

        def addSpinner(_name: String, _getVal: () => Byte, _setVal: (Byte) => Unit, _params: Option[GlobalSpinnerParams]): Unit = {

            val lbl = new Label(L.G(_name))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val spn = _params match {
                case Some(gsp) => new GlobalSpinner(_name, lbl, _getVal, _setVal, gsp)
                case _         => new GlobalSpinner(_name, lbl, _getVal, _setVal, GlobalSpinnerParams(0, 0, 255))
            }
            this.contents += (spn, s"cell 1 ${row}")

            row += 1
        }

        def addComboBox(_name: String, _getVal: () => Byte, _setVal: (Byte) => Unit, gcbp: GlobalComboBoxParams): Unit = {

            val lbl = new Label(L.G(_name))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new GlobalComboBox(_name, lbl, _getVal, _setVal, gcbp)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }

        def addOffOnComboBox(_name: String, _getVal: () => Byte, _setVal: (Byte) => Unit): Unit = {

            val lbl = new Label(L.G(_name))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new OffOnGlobalComboBox(_name, lbl, _getVal, _setVal)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }

        def addEditableComboBox(_name: String, _getVal: () => Byte, _setVal: (Byte) => Unit, gcbp: EditableGlobalComboBoxParams): Unit = {

            val lbl = new Label(L.G(_name))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new EditableGlobalComboBox(_name, lbl, _getVal, _setVal, gcbp)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }
    }

    private[this] val pnGlobalGeneral = new GlobalPanel("General")
    contents += (pnGlobalGeneral, "cell 0 0")

    private[this] val pnGlobalFC = new GlobalPanel("FC")
    contents += (pnGlobalFC, "cell 1 0")

    private[this] val pnGlobalBC = new GlobalPanel("BC")
    contents += (pnGlobalBC, "cell 2 0")

    private[this] val pnGlobalKit = new GlobalPanel("Kit")
    contents += (pnGlobalKit, "cell 0 1")

    private[this] val pnGlobalGroove = new GlobalPanel("Groove")
    contents += (pnGlobalGroove, "cell 1 1")

    private[this] val pnGlobalMotif = new GlobalPanel("Motif")
    contents += (pnGlobalMotif, "cell 2 1")

    // Global Pad 4
    pnGlobalGeneral.addOffOnComboBox("midiMergeStatus", () => jTrapKATEditor.currentGlobal.midiMergeStatus, (value: Byte) => jTrapKATEditor.currentGlobal.midiMergeStatus = value)
    // Global Pad 7
    pnGlobalGeneral.addComboBox("prgChgRcvChn", () => jTrapKATEditor.currentGlobal.prgChgRcvChn, (value: Byte) => jTrapKATEditor.currentGlobal.prgChgRcvChn = value,
        GlobalComboBoxParams((1 to 16).map(x => s"${x}") ++ Seq(L.G("chnOmni"), L.G("itemOff"))))
    // Global Pad 8
    pnGlobalGeneral.addComboBox("chokeFunction", () => jTrapKATEditor.currentGlobal.chokeFunction, (value: Byte) => jTrapKATEditor.currentGlobal.chokeFunction = value,
        GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("chokeAT"), L.G("choke96"))))
    // Global Pad 16
    pnGlobalGeneral.addOffOnComboBox("noteNamesStatus", () => jTrapKATEditor.currentGlobal.noteNamesStatus, (value: Byte) => jTrapKATEditor.currentGlobal.noteNamesStatus = value)
    // Global Pad 18
    pnGlobalGeneral.addComboBox("trigGain", () => jTrapKATEditor.currentGlobal.trigGain, (value: Byte) => jTrapKATEditor.currentGlobal.trigGain = value,
        GlobalComboBoxParams(Seq(L.G("trigMIN"), L.G("trigMID"), L.G("trigMAX"))))
    // V3 Global Pads: 19 is beeperStatus, 20 is display Angle; V4 mixes it up...
    pnGlobalGeneral.addOffOnComboBox("beeperStatus", () => jTrapKATEditor.currentGlobal.beeperStatus, (value: Byte) => jTrapKATEditor.currentGlobal.beeperStatus = value)
    pnGlobalGeneral.addComboBox("displayAngle", () => jTrapKATEditor.currentGlobal.displayAngle, (value: Byte) => jTrapKATEditor.currentGlobal.displayAngle = value,
        GlobalComboBoxParams(Seq(L.G("displayStraight"), L.G("displayEdge"))))

    pnGlobalGeneral.addSpinner("instrumentID", () => jTrapKATEditor.currentGlobal.instrumentID, (value: Byte) => jTrapKATEditor.currentGlobal.instrumentID = value,
        Some(GlobalSpinnerParams(0, 0, 127)))

    pnGlobalGeneral.addOffOnComboBox("hearSoundStatus", () => jTrapKATEditor.currentGlobal.hearSoundStatus, (value: Byte) => jTrapKATEditor.currentGlobal.hearSoundStatus = value)

    // Global Pad 6
    pnGlobalFC.addComboBox("fcSplashEase", () => (jTrapKATEditor.currentGlobal.fcSplashEase - 1).toByte, (value: Byte) => jTrapKATEditor.currentGlobal.fcSplashEase = (value + 1).toByte,
        GlobalComboBoxParams(Seq(L.G("itemOff")) ++ (1 to 10).map(x => s"${x}")))
    // Global Pad 17 is FC train...
    pnGlobalFC.addSpinner("fcLowLevel", () => jTrapKATEditor.currentGlobal.fcLowLevel, (value: Byte) => jTrapKATEditor.currentGlobal.fcLowLevel = value, None)
    pnGlobalFC.addSpinner("fcVelocityLevel", () => jTrapKATEditor.currentGlobal.fcVelocityLevel, (value: Byte) => jTrapKATEditor.currentGlobal.fcVelocityLevel = value, None)
    pnGlobalFC.addSpinner("fcWaitModeLevel", () => jTrapKATEditor.currentGlobal.fcWaitModeLevel, (value: Byte) => jTrapKATEditor.currentGlobal.fcWaitModeLevel = value, None)
    pnGlobalFC.addSpinner("fcClosedRegion", () => jTrapKATEditor.currentGlobal.fcClosedRegion, (value: Byte) => jTrapKATEditor.currentGlobal.fcClosedRegion = value, None)
    pnGlobalFC.addSpinner("fcHighLevel", () => jTrapKATEditor.currentGlobal.fcHighLevel, (value: Byte) => jTrapKATEditor.currentGlobal.fcHighLevel = value, None)
    pnGlobalFC.addComboBox("fcPolarity", () => jTrapKATEditor.currentGlobal.fcPolarity, (value: Byte) => jTrapKATEditor.currentGlobal.fcPolarity = value,
        GlobalComboBoxParams(Seq(L.G("fcExpr"), L.G("fcFC"))))

    // V3 Global Pad 21 (becomes "Save User Kit")
    pnGlobalBC.addComboBox("bcFunction", () => jTrapKATEditor.currentGlobal.bcFunction, (value: Byte) => jTrapKATEditor.currentGlobal.bcFunction = value,
        GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("bcBendUp"), L.G("bcBendDown"), L.G("bcExpression"), L.G("bcSustain"))))
    // V3 (and V4 with hardware) Pad 22 is BC train...
    pnGlobalBC.addSpinner("bcLowLevel", () => jTrapKATEditor.currentGlobal.bcLowLevel, (value: Byte) => jTrapKATEditor.currentGlobal.bcLowLevel = value, None)
    pnGlobalBC.addSpinner("bcHighLevel", () => jTrapKATEditor.currentGlobal.bcHighLevel, (value: Byte) => jTrapKATEditor.currentGlobal.bcHighLevel = value, None)
    pnGlobalBC.addComboBox("bcPolarity", () => jTrapKATEditor.currentGlobal.bcPolarity, (value: Byte) => jTrapKATEditor.currentGlobal.bcPolarity = value,
        GlobalComboBoxParams(Seq(L.G("bcHighHard"), L.G("bcHighSoft"))))

    // Global Pad 1
    pnGlobalKit.addComboBox("playMode", () => jTrapKATEditor.currentGlobal.playMode, (value: Byte) => jTrapKATEditor.currentGlobal.playMode = value,
        GlobalComboBoxParams(Seq(L.G("kitFactory"), L.G("kitUser"), L.G("kitKAT"))))
    pnGlobalKit.addSpinner("kitNumber", () => jTrapKATEditor.currentGlobal.kitNumber, (value: Byte) => jTrapKATEditor.currentGlobal.kitNumber = value,
        Some(GlobalSpinnerParams(1, 1, 24)))
    pnGlobalKit.addSpinner("kitNumberUser", () => jTrapKATEditor.currentGlobal.kitNumberUser, (value: Byte) => jTrapKATEditor.currentGlobal.kitNumberUser = value,
        Some(GlobalSpinnerParams(1, 1, 24)))
    pnGlobalKit.addSpinner("kitNumberDemo", () => jTrapKATEditor.currentGlobal.kitNumberDemo, (value: Byte) => jTrapKATEditor.currentGlobal.kitNumberDemo = value,
        Some(GlobalSpinnerParams(1, 1, 24)))
    pnGlobalKit.addSpinner("kitNumberKAT", () => jTrapKATEditor.currentGlobal.kitNumberKAT, (value: Byte) => jTrapKATEditor.currentGlobal.kitNumberKAT = value,
        Some(GlobalSpinnerParams(1, 1, 6)))

    // Global Pad 2
    pnGlobalGroove.addOffOnComboBox("grooveStatus", () => jTrapKATEditor.currentGlobal.grooveStatus, (value: Byte) => jTrapKATEditor.currentGlobal.grooveStatus = value)
    // Global Pad 5
    pnGlobalGroove.addOffOnComboBox("grooveAutoOff", () => jTrapKATEditor.currentGlobal.grooveAutoOff, (value: Byte) => jTrapKATEditor.currentGlobal.grooveAutoOff = value)
    // Global Pad 9
    pnGlobalGroove.addSpinner("grooveVol", () => jTrapKATEditor.currentGlobal.grooveVol, (value: Byte) => jTrapKATEditor.currentGlobal.grooveVol = value,
        Some(GlobalSpinnerParams(0, 0, 10)))
    // Grooves Status On, Pad 23 tap tempo:
    pnGlobalMotif.addSpinner("ttMeter", () => jTrapKATEditor.currentGlobal.ttMeter, (value: Byte) => jTrapKATEditor.currentGlobal.ttMeter = value, None)

    // Should be 0 + _motifNumberPerc (if PG) or 11 + _motifNumberMel (if MG)
    pnGlobalMotif.addSpinner("motifNumber", () => jTrapKATEditor.currentGlobal.motifNumber, (value: Byte) => jTrapKATEditor.currentGlobal.motifNumber = value, None)
    // Grooves Status On, Pad 16 selects; ?? 0 to 10
    pnGlobalMotif.addSpinner("motifNumberPerc", () => jTrapKATEditor.currentGlobal.motifNumberPerc, (value: Byte) => jTrapKATEditor.currentGlobal.motifNumberPerc = value, None)
    // Grooves Status On, Pad 15 selects; ?? 0 to ..?s
    pnGlobalMotif.addSpinner("motifNumberMel", () => jTrapKATEditor.currentGlobal.motifNumberMel, (value: Byte) => jTrapKATEditor.currentGlobal.motifNumberMel = value, None)

}

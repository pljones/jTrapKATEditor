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
import info.drealm.scala.util.getInt

object pnGlobal extends MigPanel("insets 5", "[]", "[]") {
    name = "pnGlobal"

    trait GlobalComponentParams
    case class GlobalSpinnerParams(ini: Int, min: Int, max: Int) extends GlobalComponentParams
    case class GlobalComboBoxParams(items: Seq[String]) extends GlobalComponentParams
    case class EditableGlobalComboBoxParams(_toItem: (Byte, Seq[String]) => String, _fromItem: (Int, String) => Byte, items: Seq[String], _verifier: (Int, String) => Boolean) extends GlobalComponentParams

    private[this] class GlobalSpinner(_name: String, lbl: Label, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, params: GlobalSpinnerParams)
        extends Spinner(new javax.swing.SpinnerNumberModel(params.ini, params.min, params.max, 1), s"spn${_name}", L.G(s"ttGlobal${_name}"), lbl)
        with GlobalBindings with ValueChangedBindings {

        protected def _getBefore = _getVal
        protected def _getAfter = () => value.asInstanceOf[java.lang.Number].byteValue()
        protected def _get() = value = getInt(_getBefore(jTrapKATEditor.currentAllMemory.global))
        protected def _set() = _setHelper(_setVal, _name)
        protected def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

        setDisplay()
    }

    private[this] class GlobalComboBox(_name: String, lbl: Label, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, params: GlobalComboBoxParams)
        extends RichComboBox(params.items, s"cbx${_name}", L.G(s"ttGlobal${_name}"), lbl) with ComboBoxBindings[String] with GlobalBindings {

        protected def _getBefore = _getVal
        protected def _getAfter = () => selection.index.toByte
        protected def _get() = selection.item = params.items(_getBefore(jTrapKATEditor.currentAllMemory.global))
        protected def _set() = _setHelper(_setVal, _name)
        protected def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

        setDisplay()
    }

    private[this] class OffOnGlobalComboBox(_name: String, lbl: Label, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit)
        extends GlobalComboBox(_name, lbl, _getVal, _setVal, GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("itemOn"))))

    private[this] class EditableGlobalComboBox(_name: String, lbl: Label, _before: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, params: EditableGlobalComboBoxParams)
        extends GlobalComboBox(_name, lbl, _before, _setVal, GlobalComboBoxParams(params.items)) {

        protected override def _getAfter = () => params._fromItem(selection.index, selection.item)
        protected override def _get() = selection.item = params._toItem(_getBefore(jTrapKATEditor.currentAllMemory.global), params.items)

        makeEditable()
        editorPeer.setInputVerifier(new javax.swing.InputVerifier {
            def verify(input: javax.swing.JComponent) = params._verifier(selection.index, selection.item)
        })
    }

    private[this] class GlobalPanel(_name: String) extends MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
        name = s"pnGlobal${_name}"
        border = new TitledBorder(L.G(name))

        private[this] var row = 0

        def addSpinner(_name: String, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, _params: Option[GlobalSpinnerParams]): Unit = {

            val lbl = new Label(L.G(s"lblGlobal${_name}"))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val spn = _params match {
                case Some(gsp) => new GlobalSpinner(_name, lbl, _getVal, _setVal, gsp)
                case _ => new GlobalSpinner(_name, lbl, _getVal, _setVal, GlobalSpinnerParams(0, 0, 255))
            }
            this.contents += (spn, s"cell 1 ${row}")

            row += 1
        }

        def addComboBox(_name: String, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, gcbp: GlobalComboBoxParams): Unit = {

            val lbl = new Label(L.G(s"lblGlobal${_name}"))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new GlobalComboBox(_name, lbl, _getVal, _setVal, gcbp)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }

        def addOffOnComboBox(_name: String, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit): Unit = {

            val lbl = new Label(L.G(s"lblGlobal${_name}"))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new OffOnGlobalComboBox(_name, lbl, _getVal, _setVal)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }

        def addEditableComboBox(_name: String, _getVal: (model.Global[_ <: model.Pad]) => Byte, _setVal: (model.Global[_ <: model.Pad], Byte) => Unit, gcbp: EditableGlobalComboBoxParams): Unit = {

            val lbl = new Label(L.G(s"lblGlobal${_name}"))
            this.contents += (lbl, s"cell 0 ${row},alignx right")

            val cbx = new EditableGlobalComboBox(_name, lbl, _getVal, _setVal, gcbp)
            this.contents += (cbx, s"cell 1 ${row}")

            row += 1
        }
    }

    private[this] val pnGlobalGeneral = new GlobalPanel("General") {
        // Global Pad 4
        addOffOnComboBox("MidiMergeStatus", _.midiMergeStatus, _.midiMergeStatus = _)
        // Global Pad 7
        addComboBox("PrgChgRcvChn", _.prgChgRcvChn, _.prgChgRcvChn = _,
            GlobalComboBoxParams((1 to 16).map(x => s"${x}") ++ Seq(L.G("chnOmni"), L.G("itemOff"))))
        // Global Pad 8
        addComboBox("ChokeFunction", _.chokeFunction, _.chokeFunction = _,
            GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("chokeAT"), L.G("choke96"))))
        // Global Pad 16
        addOffOnComboBox("NoteNamesStatus", _.noteNamesStatus, _.noteNamesStatus = _)
        // Global Pad 18
        addComboBox("TrigGain", _.trigGain, _.trigGain = _,
            GlobalComboBoxParams(Seq(L.G("trigMIN"), L.G("trigMID"), L.G("trigMAX"))))
        // V3 Global Pads: 19 is beeperStatus, 20 is display Angle; V4 mixes it up...
        addOffOnComboBox("BeeperStatus", _.beeperStatus, _.beeperStatus = _)
        addComboBox("DisplayAngle", _.displayAngle, _.displayAngle = _,
            GlobalComboBoxParams(Seq(L.G("displayStraight"), L.G("displayEdge"))))

        addSpinner("InstrumentID", _.instrumentID, _.instrumentID = _,
            Some(GlobalSpinnerParams(0, 0, 127)))

        addOffOnComboBox("HearSoundStatus", _.hearSoundStatus, _.hearSoundStatus = _)
    }
    contents += (pnGlobalGeneral, "cell 0 0")

    private[this] val pnGlobalFC = new GlobalPanel("FC") {
        // Global Pad 6
        addComboBox("FCSplashEase", (g) => (g.fcSplashEase - 1).toByte, (g, v) => g.fcSplashEase = (v + 1).toByte,
            GlobalComboBoxParams(Seq(L.G("itemOff")) ++ (1 to 10).map(x => s"${x}")))
        // Global Pad 17 is FC train...
        addSpinner("FCLowLevel", _.fcLowLevel, _.fcLowLevel = _, None)
        addSpinner("FCVelocityLevel", _.fcVelocityLevel, _.fcVelocityLevel = _, None)
        addSpinner("FCWaitModeLevel", _.fcWaitModeLevel, _.fcWaitModeLevel = _, None)
        addSpinner("FCClosedRegion", _.fcClosedRegion, _.fcClosedRegion = _, None)
        addSpinner("FCHighLevel", _.fcHighLevel, _.fcHighLevel = _, None)
        addComboBox("FCPolarity", _.fcPolarity, _.fcPolarity = _,
            GlobalComboBoxParams(Seq(L.G("fcExpr"), L.G("fcFC"))))
    }
    contents += (pnGlobalFC, "cell 1 0")

    private[this] val pnGlobalBC = new GlobalPanel("BC") {
        // Global Pad 21
        addComboBox("BCFunction", _.bcFunction, _.bcFunction = _,
            GlobalComboBoxParams(Seq(L.G("itemOff"), L.G("bcBendUp"), L.G("bcBendDown"), L.G("bcExpression"), L.G("bcSustain"))))
        // Global Pad 22
        addSpinner("BCLowLevel", _.bcLowLevel, _.bcLowLevel = _, None)
        addSpinner("BCHighLevel", _.bcHighLevel, _.bcHighLevel = _, None)
        addComboBox("BCPolarity", _.bcPolarity, _.bcPolarity = _,
            GlobalComboBoxParams(Seq(L.G("bcHighHard"), L.G("bcHighSoft"))))
    }
    contents += (pnGlobalBC, "cell 2 0")

    private[this] val pnGlobalKit = new GlobalPanel("Kit") {
        // Global Pad 1
        addComboBox("PlayMode", _.playMode, _.playMode = _,
            GlobalComboBoxParams(Seq(L.G("kitFactory"), L.G("kitUser"), L.G("kitKAT"))))
        addSpinner("KitNumber", _.kitNumber, _.kitNumber = _,
            Some(GlobalSpinnerParams(1, 1, 24)))
        addSpinner("KitNumberUser", _.kitNumberUser, _.kitNumberUser = _,
            Some(GlobalSpinnerParams(1, 1, 24)))
        addSpinner("KitNumberDemo", _.kitNumberDemo, _.kitNumberDemo = _,
            Some(GlobalSpinnerParams(1, 1, 24)))
        addSpinner("KitNumberKAT", _.kitNumberKAT, _.kitNumberKAT = _,
            Some(GlobalSpinnerParams(1, 1, 6)))
    }
    contents += (pnGlobalKit, "cell 0 1")

    private[this] val pnGlobalGroove = new GlobalPanel("Groove") {
        // Global Pad 2
        addOffOnComboBox("GrooveStatus", _.grooveStatus, _.grooveStatus = _)
        // Global Pad 5
        addOffOnComboBox("GrooveAutoOff", _.grooveAutoOff, _.grooveAutoOff = _)
        // Global Pad 9
        addSpinner("GrooveVol", _.grooveVol, _.grooveVol = _,
            Some(GlobalSpinnerParams(0, 0, 10)))
    }
    contents += (pnGlobalGroove, "cell 1 1")

    private[this] val pnGlobalMotif = new GlobalPanel("Motif") {
        // Grooves Status On, Pad 23 tap tempo:
        addSpinner("TTMeter", _.ttMeter, _.ttMeter = _, None)
        // Should be 0 + _motifNumberPerc (if PG) or 11 + _motifNumberMel (if MG)
        addSpinner("MotifNumber", _.motifNumber, _.motifNumber = _, None)
        // Grooves Status On, Pad 16 selects; ?? 0 to 10
        addSpinner("MotifNumberPerc", _.motifNumberPerc, _.motifNumberPerc = _, None)
        // Grooves Status On, Pad 15 selects; ?? 0 to ..?s
        addSpinner("MotifNumberMel", _.motifNumberMel, _.motifNumberMel = _, None)
    }
    contents += (pnGlobalMotif, "cell 2 1")

}

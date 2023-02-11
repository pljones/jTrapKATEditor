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
import info.drealm.scala.util.getInt

object pnKitsPads extends MigPanel("insets 3", "[grow]", "[][grow]") {
    name = "pnKitsPads"

    private[this] object pnKitsPadsTop extends MigPanel("insets 0", "[grow]", "[][][grow]") {
        name = "pnKitsPadsTop"

        contents += (pnSelector, "cell 0 0,growx,aligny baseline")
        contents += (pnPads, "cell 0 1, grow")
        contents += (pnPedals, "cell 0 2,grow")

    }

    private[this] object tpnKitPadsDetails extends TabbedPane() with AllMemorySelectionReactor {
        name = "tpnKitPadsDetails"

        private[this] def isSourceComponent(source: Any, value: String*): Boolean = source match {
            case target: Component => value.contains(target.name)
            case _                 => false
        }

        private[this] object pnPadDetails extends MigPanel("insets 5, gapx 2, gapy 0, hidemode 3", "[][16px:n,right][][16px:n][][][16px:n][][16px:n][]", "[][][][][][][grow]") {
            name = "pnPadDetails"

            contents += (new Label(L.G("lblSlots")), "cell 0 0")

            (2 to 6) foreach { slot =>
                {
                    val s = new Slot(slot)
                    contents += (s.lblSlot, s"cell 1 ${slot - 2},alignx right")
                    contents += (s.cbxSlot.cbxV3, s"cell 2 ${slot - 2},gapy 2,grow")
                    contents += (s.cbxSlot.cbxV4, s"cell 2 ${slot - 2},gapy 2,grow")
                }
            }

            contents += (pnLinkTo, "cell 0 5 4 1,gapy 5,alignx left,aligny center,hidemode 0")

            private[this] val lblPadCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mnePadCurve").charAt(0)) }
            contents += (lblPadCurve, "cell 4 0,alignx right")

            private[this] val cbxPadCurve = new CurveComboBoxV3V4("cbxPadCurve", L.G("ttPadCurve"), lblPadCurve) with SelectedPadBindings with CurveComboBoxV3V4Bindings {
                protected def _padActionName = "Curve"
                protected def _getModelValue = _.curve
                protected def _setModelValue = _.curve = _
                protected def _uiValue = selection.index.toByte
                protected def _uiValue_=(_value: Byte): Unit = selection.index = _value

                protected def _chg() = jTrapKATEditor.padChangedBy(cbx)

                setDisplay()
            }
            contents += (cbxPadCurve.cbxV3, "cell 5 0")
            contents += (cbxPadCurve.cbxV4, "cell 5 0")

            private[this] val lblPadGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mnePadGate").charAt(0)) }
            contents += (lblPadGate, "cell 4 1,alignx right")

            private[this] val cbxPadGate = new GateTimeComboBox("cbxPadGate", L.G("ttPadGate"), lblPadGate) with EditableComboBoxBindings[String] with SelectedPadBindings {
                protected def _padActionName = "Gate"
                protected def _getModelValue = _.gate
                protected def _setModelValue = _.gate = _
                protected def _uiValue = GateTime.toGateTime(selection.item)
                protected def _uiValue_=(_value: Byte): Unit = selection.item = GateTime.toString(_value)

                protected def _chg() = jTrapKATEditor.padChangedBy(this)

                setDisplay()
            }
            contents += (cbxPadGate, "cell 5 1")

            private[this] val lblPadChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mnePadChannel").charAt(0)) }
            contents += (lblPadChannel, "cell 4 2,alignx right")

            private[this] val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnPadChannel", L.G("ttPadChannel"), lblPadChannel) with SelectedPadBindings with ValueChangedReactor {
                protected def _padActionName = "Channel"
                protected def _getModelValue = _.channel
                protected def _setModelValue = _.channel = _
                protected def _uiValue = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected def _uiValue_=(_value: Byte): Unit = value = _value + 1

                protected def _chg() = jTrapKATEditor.padChangedBy(this)

                setDisplay()
            }
            contents += (spnPadChannel, "cell 5 2")

            private[this] object pnPadVelocity extends MigPanel("insets 3, gap 0", "[][]", "[][][]") {
                name = "pnPadVelocity"
                tooltip = L.G("ttPadVelocity")
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq(
                    (0, "Min", 1, (pad: model.Pad) => pad.minVelocity, (pad: model.Pad, value: Byte) => pad.minVelocity = value),
                    (1, "Max", 127, (pad: model.Pad) => pad.maxVelocity, (pad: model.Pad, value: Byte) => pad.maxVelocity = value)) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5) match {
                            case (_x, _name, _ini, _getVel, _setVel) => {
                                val lbl = new Label(L.G(s"lbl${_name}"))
                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnPadVel${_name}", tooltip, lbl) with SelectedPadBindings with ValueChangedReactor {
                                    protected def _padActionName = s"Vel${_name}"
                                    protected def _getModelValue = _getVel
                                    protected def _setModelValue = _setVel
                                    protected def _uiValue = value.asInstanceOf[java.lang.Number].byteValue()
                                    protected def _uiValue_=(_value: Byte): Unit = value = _value

                                    protected def _chg() = jTrapKATEditor.padChangedBy(this)

                                    setDisplay()
                                }
                                contents += (lbl, s"cell ${_x} 1,alignx center")
                                contents += (spn, s"cell ${_x} 2")
                            }
                        }
                    }
            }
            contents += (pnPadVelocity, "cell 7 0 1 3,growx,aligny top")

            private[this] val pnFlags: MigPanel = new MigPanel("insets 0, gap 0", "[][][][][][][][]", "[][][]") {
                name = "pnFlags"
                tooltip = L.G("ttPadFlags")

                contents += (new Label(L.G("lblFlags")), "cell 0 0 9 1,alignx center")

                (7 to 0 by -1) foreach { flag =>
                    val ckbFlag = new CheckBox(s"${flag}") with SelectedPadBindings with ButtonClickedReactor {
                        name = s"ckbFlag${flag}"
                        tooltip = L.G("ttPadFlags")
                        margin = new Insets(0, 0, 0, 0)
                        horizontalTextPosition = Alignment.Center
                        verticalTextPosition = Alignment.Top

                        protected def _padActionName = "Flag"
                        protected def _getModelValue = p => ((1 << flag) & p.flags).toByte
                        protected def _setModelValue = (p, v) => p.flags = (v | (~(1 << flag) & p.flags)).toByte
                        protected def _uiValue = ((if (selected) 1 else 0) << flag).toByte
                        protected def _uiValue_=(_value: Byte): Unit = selected = _value != 0

                        protected def _chg() = jTrapKATEditor.padChangedBy(this)

                        setDisplay()
                    }
                    contents += (ckbFlag, s"cell ${8 - flag} 1 1 2,alignx center")
                }
            }
            contents += (pnFlags, "cell 9 0 1 3,growx,aligny top")

            private[this] val pnGlobalPadDynamics = new MigPanel("insets 0,gapx 2, gapy 0", "[4px:n][][][4px:n][][][4px:n][][][4px:n]", "[4px:n][][][4px:n]") {
                name = "pnGlobalPadDynamics"
                border = new TitledBorder(L.G("pnGlobalPadDynamics"))

                private[this] class PadDynamicsSpinner(_name: String, label: Label, _getPD: (model.PadDynamics) => Byte, _setPD: (model.PadDynamics, Byte) => Unit, _v: () => Boolean)
                    extends Spinner(new javax.swing.SpinnerNumberModel(199, 0, 255, 1), s"spn${_name.capitalize}", L.G(s"ttGlobal${_name.capitalize}"), label)
                    with GlobalPadDynamicsBindings {

                    protected def _pdActionName = _name
                    protected def _getModelValue = _getPD
                    protected def _setModelValue = _setPD
                    protected def _visible = _v

                    setDisplay()
                }

                Seq[(Int, Int, String, model.PadDynamics => Byte, (model.PadDynamics, Byte) => Unit, () => Boolean)](
                    (0, 0, "lowLevel", _.lowLevel, _.lowLevel = _, () => true), (1, 0, "thresholdManual", _.thresholdManual, _.thresholdManual = _, () => true), (2, 0, "internalMargin", _.internalMargin, _.internalMargin = _, () => true),
                    (0, 1, "highLevel", _.highLevel, _.highLevel = _, () => true), (1, 1, "thresholdActual", _.thresholdActual, _.thresholdActual = _, () => true), (2, 1, "userMargin", _.userMargin, _.userMargin = _, () => jTrapKATEditor.doV3V4V5(true, true, false))) foreach (tuple => (tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6) match {
                        case (_x, _y, _name, _getPD, _setPD, _visible) => {
                            val lbl = new Label(L.G(_name)) with AllMemorySelectionReactor {
                                protected def _isUIChange = true
                                protected def _uiReaction = visible = _visible()
                                setDisplay()
                            }
                            contents += (lbl, s"cell ${1 + 3 * _x} ${1 + _y},alignx right")

                            val spn = new PadDynamicsSpinner(_name, lbl, _getPD, _setPD, tuple._6)
                            contents += (spn, s"cell ${2 + 3 * _x} ${1 + _y}")
                        }
                    })
            }
            contents += (pnGlobalPadDynamics, "cell 4 3 6 4,aligny center")

            private[this] val tabOrder = (2 to 6 flatMap (slot => Seq(s"cbxSlot${slot}V3", s"cbxSlot${slot}V4"))) ++
                Seq("cbxLinkTo") ++
                Seq("cbxPadCurveV3", "cbxPadCurveV4", "cbxPadGate", "spnPadChannel") ++
                Seq("spnPadVelMin", "spnPadVelMax") ++
                (7 to 0 by -1 map { flag => s"ckbFlag${flag}" }) ++
                Seq("spnLowLevel", "spnHighLevel", "spnThresholdManual", "spnThresholdActual", "spnInternalMargin", "spnUserMargin")

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, tabOrder))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] object pnMoreSlots extends MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][16px:n,right][]", "[][][][][]") {
            name = "pnMoreSlots"

            contents += (new Label(L.G("lblSlots")), "cell 0 0")

            (7 to 11) foreach { slot =>
                {
                    val s = new Slot(slot)
                    contents += (s.lblSlot, s"cell 1 ${slot - 7},alignx right")
                    contents += (s.cbxSlot.cbxV4, s"cell 2 ${slot - 7},gapy 2,grow")
                }
            }

            (12 to 16) foreach { slot =>
                {
                    val s = new Slot(slot)
                    contents += (s.lblSlot, s"cell 4 ${slot - 12},alignx right")
                    contents += (s.cbxSlot.cbxV4, s"cell 5 ${slot - 12},gapy 2,grow")
                }
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((7 to 16) map (slot => s"cbxSlot${slot}V4"))))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] object pnKitDetails extends MigPanel("insets 5, gapx 2, gapy 0, hidemode 3", "[][][][16px:n,grow][][16px:n,grow][][][][4px:n][][][]", "[][][][][][][]") {
            name = "pnKitDetails"

            private[this] val order = scala.collection.mutable.ArrayBuffer.empty[String]

            trait CanBeEnabled {
                def enabled: Boolean
                def enabled_=(value: Boolean): Unit
                def tooltip: String
            }
            implicit def maybeItCanBeEnabled(o: AnyRef): CanBeEnabled = o match {
                case cp: Component                => new CanBeEnabled { def enabled = cp.enabled; def enabled_=(value: Boolean): Unit = cp.enabled = value; def tooltip = cp.tooltip }
                case cp: V3V4ComboBox[_, _, _, _] => new CanBeEnabled { def enabled = cp.enabled; def enabled_=(value: Boolean): Unit = cp.enabled = value; def tooltip = cp.tooltip }
                case _                            => throw new ClassCastException(s"Class ${o.getClass().getName()} cannot be cast to CanBeEnabled")
            }
            private[this] class VarXCheckBox(_name: String, _cp: CanBeEnabled, _isKit: (model.Kit[_ <: model.Pad]) => Boolean, _toKit: (model.Kit[_ <: model.Pad]) => Unit, padCpName: String*) extends CheckBox(L.G("lblXVarious"))
                with AllMemorySelectionReactor with KitSelectionReactor with ButtonClickedReactor {
                name = s"ckbVar${_name}"
                tooltip = _cp.tooltip

                protected def _uiValue: Boolean = !selected
                protected def _uiValue_=(value: Boolean): Unit = selected = !value
                protected def _isUIChange = _uiValue != _modelValue
                protected def _uiReaction() = {
                    _uiValue = _modelValue
                    padCpName foreach (n => setPadEnabled(n, selected))
                    _cp.enabled = !selected
                    tooltip = _cp.tooltip
                }

                protected def _modelValue: Boolean = _isKit(jTrapKATEditor.currentKit)
                protected def _isModelChange = true // the button really did get clicked...
                protected def _modelReaction() = _toKit(jTrapKATEditor.currentKit)
                override protected def setValue(): Unit = {
                    // So the button got clicked but will anything need updating?
                    if (_uiValue != _modelValue) try {
                        deafTo(this)
                        if (!selected && !okToGoKit(_name)) selected = true
                        else if (!selected) {
                            super.setValue()
                            EditHistory.clear()
                            jTrapKATEditor.padChangedBy(this)
                        }
                    } finally { listenTo(this) }
                    // Regardless, we need to update the UI
                    padCpName foreach (n => setPadEnabled(n, selected))
                    _cp.enabled = !selected
                    tooltip = _cp.tooltip
                }

                private[this] def okToGoKit(name: String): Boolean = {
                    Dialog.showConfirmation(
                        tpnMain,
                        L.G("ToKit", name),
                        L.G("ApplicationProductName"),
                        Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok
                }
                private[this] def setPadEnabled(name: String, isVarious: Boolean): Unit = {
                    Focus.findInComponent(pnPadDetails, name) match {
                        case Some(cp) => cp.enabled = isVarious
                        case _        => Console.println(s"${name} not found")
                    }
                }

                setDisplay()
            }

            private[this] def okToGoSCOff(name: String): Boolean = {
                Dialog.showConfirmation(
                    tpnMain,
                    L.G("ToSCOff", name),
                    L.G("ApplicationProductName"),
                    Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok
            }

            private[this] val lblKitCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mneKitCurve").charAt(0)) }
            contents += (lblKitCurve, "cell 0 0,alignx right")

            private[this] val cbxKitCurve: CurveComboBoxV3V4 = new CurveComboBoxV3V4("cbxKitCurve", L.G("ttKitCurve"), lblKitCurve) with CurveComboBoxV3V4Bindings with KitVariesBindings with KitValueReactor {
                protected def _kitActionName = "Curve"
                protected def _getModelValue = _.curve
                protected def _setModelValue = _.curve = _
                protected def _uiValue = selection.index.toByte
                protected def _uiValue_=(_value: Byte): Unit = selection.index = _value

                protected def _isKit = _.isKitCurve
                protected def _toKit = _.toKitCurve()
                protected def _cp() = cbx

                protected def _chg() = jTrapKATEditor.kitChangedBy(cbx)

                setDisplay()
            }
            contents += (cbxKitCurve.cbxV3, "cell 1 0")
            contents += (cbxKitCurve.cbxV4, "cell 1 0")
            order += cbxKitCurve.cbxV3.name
            order += cbxKitCurve.cbxV4.name

            private[this] val ckbVarCurve = new VarXCheckBox("Curve", cbxKitCurve, _.isKitCurve, _.toKitCurve(), "cbxPadCurveV3", "cbxPadCurveV4")
            contents += (ckbVarCurve, "cell 2 0")
            order += ckbVarCurve.name

            private[this] val lblKitGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mneKitGate").charAt(0)) }
            contents += (lblKitGate, "cell 0 1,alignx right")

            private[this] val cbxKitGate: GateTimeComboBox = new GateTimeComboBox("cbxKitGate", L.G("ttKitGate"), lblKitGate) with EditableComboBoxBindings[String] with KitVariesBindings with KitValueReactor {
                protected def _kitActionName = "Gate"
                protected def _getModelValue = _.gate
                protected def _setModelValue = _.gate = _
                protected def _uiValue = GateTime.toGateTime(selection.item)
                protected def _uiValue_=(_value: Byte): Unit = selection.item = GateTime.toString(_value)

                protected def _isKit = _.isKitGate
                protected def _toKit = _.toKitGate()
                protected def _cp() = this

                protected def _chg() = jTrapKATEditor.kitChangedBy(this)

                setDisplay()
            }
            contents += (cbxKitGate, "cell 1 1,spanx 2")
            order += cbxKitGate.name

            private[this] val ckbVarGate = new VarXCheckBox("Gate", cbxKitGate, _.isKitGate, _.toKitGate(), "cbxPadGate")
            contents += (ckbVarGate, "cell 1 1")
            order += ckbVarGate.name

            private[this] val lblKitChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mneKitChannel").charAt(0)) }
            contents += (lblKitChannel, "cell 0 2,alignx right")

            private[this] val spnKitChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnKitChannel", L.G("ttKitChannel"), lblKitChannel) with KitVariesBindings with ValueChangedReactor with KitValueReactor {
                protected def _kitActionName = "Channel"
                protected def _getModelValue = _.channel
                protected def _setModelValue = _.channel = _
                protected def _uiValue = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected def _uiValue_=(_value: Byte): Unit = value = _value + 1

                protected def _isKit = _.isKitChannel
                protected def _toKit = _.toKitChannel()
                protected def _cp() = this

                protected def _chg() = jTrapKATEditor.kitChangedBy(this)

                setDisplay()
            }
            contents += (spnKitChannel, "cell 1 2,spanx 2")
            order += spnKitChannel.name

            private[this] val ckbVarChannel = new VarXCheckBox("Channel", spnKitChannel, _.isKitChannel, _.toKitChannel(), "spnPadChannel")
            contents += (ckbVarChannel, "cell 1 2")
            order += ckbVarChannel.name

            contents += (new Label(L.G("lblFootController")), "cell 0 3 2 1, alignx center, aligny bottom")

            val lblFCFunction = new Label(L.G("lblFCFunction")) { peer.setDisplayedMnemonic(L.G("mneFCFunction").charAt(0)) }
            contents += (lblFCFunction, "cell 0 4,alignx right")

            val cbxFCFunction = new RichComboBox(L.G("fcFunctions").split("\n"), "cbxFCFunction", L.G("ttFCFunction"), lblFCFunction) with KitBindings with RichComboBoxReactor[String] {
                protected def _kitActionName = "FCFunction"
                protected def _getModelValue = _.fcFunction
                protected def _setModelValue = _.fcFunction = _
                protected def _uiValue = selection.index.toByte
                protected def _uiValue_=(_value: Byte): Unit = selection.index = _value

                protected def _chg() = jTrapKATEditor.kitChangedBy(this)

                setDisplay()
            }
            contents += (cbxFCFunction, "cell 1 4")
            order += "cbxFCFunction"

            private[this] val lblFCChannel = new Label(L.G("lblFCChannel")) { peer.setDisplayedMnemonic(L.G("mneFCChannel").charAt(0)) }
            contents += (lblFCChannel, "cell 0 5,alignx right")

            // Reacts to "as chick" and chick channel
            private[this] val spnFCChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnFCChannel", L.G("ttFCChannel"), lblFCChannel) with KitBindings with ValueChangedReactor with PadValueReactor with KitValueReactor {
                protected def _getModelValue = _.fcChannel
                protected def _setModelValue = _.fcChannel = _
                protected def _uiValue = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected def _uiValue_=(_value: Byte): Unit = (if (_value < 16) _value else jTrapKATEditor.currentKit(25).channel) + 1

                protected def _kitActionName = "FCChannel"
                protected def _chg() = jTrapKATEditor.kitChangedBy(this)

                setDisplay()
            }
            contents += (spnFCChannel, "cell 1 5,spanx 2")
            order += "spnFCChannel"

            private[this] val ckbAsChick = new CheckBox(L.G("ckbAsChick")) with KitBindings with ButtonClickedReactor {
                name = "ckbAsChick"
                tooltip = spnFCChannel.tooltip

                protected def _getModelValue = (k) => if (k.fcChannel >= 16) 1 else 0
                protected def _setModelValue = (k, v) => {
                    k.fcChannel = if (v != 0) 16 else k(25).channel
                    if (jTrapKATEditor.currentKit == k) {
                        spnFCChannel.enabled = v == 0
                    }
                }
                protected def _uiValue = if (selected) 1 else 0
                protected def _uiValue_=(_value: Byte): Unit = selected = _getModelValue(jTrapKATEditor.currentKit) != 0

                override protected def _kitActionName = "FCAsChick"
                override protected def _chg() = jTrapKATEditor.kitChangedBy(this)

                setDisplay()
                spnFCChannel.enabled = !selected
            }
            contents += (ckbAsChick, "cell 1 5")
            order += "ckbAsChick"

            val lblFCCurve = new Label(L.G("lblFCCurve")) { peer.setDisplayedMnemonic(L.G("mneFCCurve").charAt(0)) }
            contents += (lblFCCurve, "cell 0 6,alignx right")

            val cbxFCCurve = new RichComboBox(L.G("fcCurves").split("\n"), "cbxFCCurve", L.G("ttFCCurve"), lblFCCurve) with KitBindings with RichComboBoxReactor[String] {
                protected def _kitActionName = "FCCurve"
                protected def _getModelValue = _.fcCurve
                protected def _setModelValue = _.fcCurve = _
                protected def _uiValue = selection.index.toByte
                protected def _uiValue_=(_value: Byte): Unit = selection.index = _value

                protected def _chg() = jTrapKATEditor.kitChangedBy(this)
                setDisplay()
            }
            contents += (cbxFCCurve, "cell 1 6,growx") //
            order += "cbxFCCurve"

            private[this] object pnKitVelocity extends MigPanel("insets 3, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"
                tooltip = L.G("ttKitVelocity")
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq(
                    (0, "Min", 1, (kit: model.Kit[_ <: model.Pad]) => kit.minVelocity, (kit: model.Kit[_ <: model.Pad], value: Byte) => kit.minVelocity = value,
                        (kit: model.Kit[_ <: model.Pad]) => kit.isKitMinVel, (kit: model.Kit[_ <: model.Pad]) => kit.toKitMinVel()),
                    (1, "Max", 127, (kit: model.Kit[_ <: model.Pad]) => kit.maxVelocity, (kit: model.Kit[_ <: model.Pad], value: Byte) => kit.maxVelocity = value,
                        (kit: model.Kit[_ <: model.Pad]) => kit.isKitMaxVel, (kit: model.Kit[_ <: model.Pad]) => kit.toKitMaxVel())) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5, x._6, x._7) match {
                            case (_x, _name, _ini, _getVel, _setVel, _isKitVel, _toKitVel) => {

                                val lbl = new Label(L.G(s"lbl${_name}"))
                                contents += (lbl, s"cell ${_x} 1,alignx center")

                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnKitVel${_name}", tooltip, lbl) with ValueChangedReactor with KitVariesBindings {
                                    protected def _getModelValue = _getVel
                                    protected def _setModelValue = _setVel
                                    protected def _uiValue = value.asInstanceOf[java.lang.Number].intValue().toByte
                                    protected def _uiValue_=(_value: Byte): Unit = value = _value

                                    protected def _kitActionName = s"Vel${_name}"
                                    protected def _chg() = jTrapKATEditor.kitChangedBy(this)
                                    protected def _isKit = _isKitVel
                                    protected def _toKit = _toKitVel
                                    protected def _cp() = this

                                    setDisplay()
                                }
                                contents += (spn, s"cell ${_x} 2")
                                order += spn.name

                                val ckb = new VarXCheckBox(s"Vel${_name}", spn, _isKitVel, _toKitVel, s"spnPadVel${_name}") { background = new Color(228, 228, 228) }
                                contents += (ckb, s"cell ${_x} 3")
                                order += ckb.name
                            }
                        }
                    }

            }
            contents += (pnKitVelocity, "cell 4 0 1 7,growx,aligny top")

            private[this] object pnSoundControl extends MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") with AllMemorySelectionReactor {
                name = "pnSoundControl"

                protected def _isUIChange = true
                protected def _uiReaction = visible = jTrapKATEditor.doV3V4V5(false, true, true)

                private[this] val lblSoundControl = new Label(L.G("lblSoundControl"))
                contents += (lblSoundControl, "cell 0 0,alignx right")

                private[this] val cbxSoundControl = new RichComboBox((1 to 4), "cbxSoundControl", L.G("ttSoundControl"), lblSoundControl) with RichComboBoxReactor[Int] {
                    protected def _isUIChange = jTrapKATEditor.currentSoundControlNumber != selection.index
                    protected def _uiReaction() = selection.index = jTrapKATEditor.currentSoundControlNumber
                    protected def _isModelChange = jTrapKATEditor.currentSoundControlNumber != selection.index
                    protected def _modelReaction() = jTrapKATEditor.currentSoundControlNumber = selection.index

                    setDisplay()
                }
                contents += (cbxSoundControl, "cell 1 0")
                order += cbxSoundControl.name

                listenTo(jTrapKATEditor)

                setDisplay()
            }
            contents += (pnSoundControl, "cell 6 0 7 1,center,hidemode 0")

            private[this] def prgChgOff(isOff: Boolean): Unit = {
                Seq("BankMSB", "BankLSB") foreach (n => {
                    Focus.findInComponent(pnKitDetails, "ckb" + n) match {
                        case Some(ckb: CheckBox) => Focus.findInComponent(pnKitDetails, "spn" + n) match {
                            case Some(spn: Spinner) => {
                                ckb.enabled = !isOff
                                spn.enabled = !isOff && !ckb.selected
                            }
                            case _ => {}
                        }
                        case _ => {}
                    }
                })
                jTrapKATEditor.doV3V4V5({
                    Focus.findInComponent(pnKitDetails, "spnBank") match {
                        case Some(spn: Spinner) => spn.enabled = !isOff
                        case _                  => {}
                    }
                }, {}, {})
            }

            //_name, _ini, _min, _max, _getModel, _setModel, ?_meansOff?, ?_offMeans?, ?_ckbCallback
            Seq[(String, Int, Int, Int, (model.Kit[_ <: model.Pad], Int) => Byte, (model.Kit[_ <: model.Pad], Int, Byte) => Unit, Option[Int => Boolean], Option[Boolean => Int], Option[Boolean => Unit])](
                ("PrgChg", 1, 1, 128,
                    _.soundControls(_).prgChg, _.soundControls(_).prgChg = _,
                    Some(_ == 0), Some(p => if (p) 0 else 1), Some(prgChgOff)),
                ("SndCtlTxmChn", 10, 1, 16,
                    (kit, sc) => (kit.soundControls(sc).sndCtlTxmChn + 1).toByte, (kit, sc, value: Byte) => kit.soundControls(sc).sndCtlTxmChn = (value - 1).toByte,
                    None, None, None),
                ("BankMSB", 0, 0, 127,
                    _.soundControls(_).bankMSB, _.soundControls(_).bankMSB = _,
                    Some(_ >= 128), Some(p => if (p) 128 else 0), None),
                ("BankLSB", 0, 0, 127,
                    _.soundControls(_).bankLSB, _.soundControls(_).bankLSB = _,
                    Some(_ >= 128), Some(p => if (p) 128 else 0), None),
                ("Bank", 0, 0, 127,
                    (kit, sc) => jTrapKATEditor.doV3V4V5(kit.asInstanceOf[model.KitV3].bank, 0, 0),
                    (kit, sc, value) => jTrapKATEditor.doV3V4V5(kit.asInstanceOf[model.KitV3].bank= value, {}, {}),
                    jTrapKATEditor.doV3V4V5(Some(_ >= 128), None, None),
                    jTrapKATEditor.doV3V4V5(Some(p => if (p) 128 else 0), None, None),
                    None),
                ("Volume", 127, 0, 127,
                    _.soundControls(_).volume, _.soundControls(_).volume = _,
                    Some(_ >= 128), Some(p => if (p) 128 else 127), None)) map { t =>
                    (t._1, t._2, t._3, t._4, t._5, t._6, t._7) match {
                        case (_name, _ini, _min, _max, _getVal, _setVal, optMeansOff) => {
                            val lbl = new Label(L.G(s"lbl${_name}")) {
                                name = s"lbl${_name}"
                                L.G(s"mne${_name}") match {
                                    case x if x.length != 0 => peer.setDisplayedMnemonic(x.charAt(0))
                                    case _                  => {}
                                }
                            }
                            val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, _min, _max, 1), s"spn${_name}", L.G(s"ttSC${_name}"), lbl) with SoundControlBindings with ValueChangedReactor with SoundControlValueReactor {
                                protected def _scActionName = _name
                                protected def _getModelValue = _getVal
                                protected def _setModelValue = _setVal

                                // to save polluting SoundControlBindings too much...
                                protected def _uiValue_=(_value: Byte): Unit = value = if (optMeansOff.map(f => f(getInt(_value))).getOrElse(false)) _ini.toByte else _value

                                setDisplay()
                            }

                            val ckb: Option[CheckBox] = (t._7, t._8, t._9) match {
                                case (Some(_meansOff), Some(_offMeans), _optCkbCallback) => Some(new CheckBox(L.G("ckbSCOff")) with SoundControlEnabledBindings {
                                    name = s"ckb${_name}"

                                    protected def _sceActionName = _name
                                    protected def _getModelValue = (k, s) => if (_meansOff(getInt(_getVal(k, s)))) 1 else 0
                                    protected def _setModelValue = (k, s, v) => {
                                        val value = v != 0
                                        _setVal(k, s, _offMeans(value).toByte)
                                        if (k.soundControls(s) == jTrapKATEditor.sc) {
                                            spn.enabled = !value
                                            _optCkbCallback foreach (_(value))
                                        }
                                    }

                                    protected def _uiValue_=(value: Byte): Unit = {
                                        selected = (value != 0)
                                        spn.enabled = !selected
                                        _optCkbCallback foreach (_(selected))
                                    }

                                    setDisplay()
                                })
                                case _ => None
                            }
                            (lbl, spn, ckb)
                        }
                        case _ => (new Label, new Spinner(new javax.swing.SpinnerNumberModel), None)
                    }
                } zip Seq((2, 1), (0, 1), (1, 0), (1, 1), (2, 0), (0, 0)) foreach { t =>
                    contents += (t._1._1, s"cell ${6 + 3 * t._2._2} ${1 + t._2._1},alignx right,hidemode 0")
                    contents += (t._1._2, s"cell ${7 + 3 * t._2._2} ${1 + t._2._1},growx,hidemode 0")
                    order += t._1._2.name

                    t._1._3 match {
                        case Some(ckb) => {
                            contents += (ckb, s"cell ${8 + 3 * t._2._2} ${1 + t._2._1},hidemode 0")
                            order += ckb.name
                        }
                        case None => {}
                    }
                }

            private[this] def onSelectedAllMemoryChanged(): Unit = {
                Focus.findInComponent(this, "ckbPrgChg") match {
                    case Some(ckb: CheckBox) => prgChgOff(ckb.selected)
                    case _                   => Console.println("ckbPrgChg not found")
                }
                Seq("lbl", "spn", "ckb") foreach (x => Focus.findInComponent(this, s"${x}Bank") match {
                    case Some(cp) => cp.visible = jTrapKATEditor.doV3V4V5(true, false, false)
                    case _        => {}
                })
            }

            listenTo(jTrapKATEditor)
            reactions += {
                case e: SelectedAllMemoryChanged => onSelectedAllMemoryChanged
            }

            // Have to do this on start up, too
            onSelectedAllMemoryChanged()

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, Seq(Seq(), order).flatten))
            peer.setFocusTraversalPolicyProvider(true)
        }

        val tpnPadDetails = new TabbedPane.Page("Pad Details", pnPadDetails, L.G("ttPadDetails")) { name = "tpPadDetails" }
        val tpnMoreSlots = new TabbedPane.Page("More Slots", pnMoreSlots, L.G("ttMoreSlots")) { name = "tpMoreSlots" }
        val tpnKitDetails = new TabbedPane.Page("Kit Details", pnKitDetails, L.G("ttKitDetails")) { name = "tpKitDetails" }

        listenTo(jTrapKATEditor)

        protected def _isUIChange = pages.length != jTrapKATEditor.doV3V4V5(2, 3, 3)
        protected def _uiReaction = {
            val seln = if (selection.index < 0) null else selection.page
            while (pages.length > 0) {
                pages.remove(pages.length - 1)
                pages.runCount
            }
            pages += tpnPadDetails
            jTrapKATEditor.doV3V4V5({}, pages += tpnMoreSlots, pages += tpnMoreSlots)
            pages += tpnKitDetails

            if (seln != null) selection.page = jTrapKATEditor.doV3V4V5(if (seln == tpnMoreSlots) tpnPadDetails else seln, seln, seln)
        }

        setDisplay()

    }

    contents += (pnKitsPadsTop, "cell 0 0,grow")
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

}

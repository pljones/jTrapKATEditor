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

object pnKitsPads extends MigPanel("insets 3", "[grow]", "[][grow]") {
    name = "pnKitsPads"

    private[this] object pnKitsPadsTop extends MigPanel("insets 0", "[grow]", "[][][grow]") {
        name = "pnKitsPadsTop"

        contents += (pnSelector, "cell 0 0,growx,aligny baseline")
        contents += (pnPads, "cell 0 1, grow")
        contents += (pnPedals, "cell 0 2,grow")

    }
    contents += (pnKitsPadsTop, "cell 0 0,grow")

    private[this] object tpnKitPadsDetails extends TabbedPane() {
        name = "tpnKitPadsDetails"

        private[this] def isSourceComponent(source: Any, value: String, component: Component): Boolean = source match {
            case target: Component if target.name == value => Focus.findInComponent(component, target == _).isDefined
            case _                                         => false
        }

        // Unsigned byte, please...
        def getInt(value: Byte) = 0x000000ff & value

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

            private[this] val cbxPadCurve = new CurveComboBoxV3V4("cbxPadCurve", L.G("ttPadCurve"), lblPadCurve,
                () => jTrapKATEditor.currentPad.curve,
                (value: Byte) => jTrapKATEditor.currentPad.curve = value,
                (cbx: Component) => jTrapKATEditor.padChangedBy(cbx)
            ) {
                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "cbxKitCurveV3", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "cbxKitCurveV4", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarCurve", tpnKitPadsDetails) => setDisplay()
                }

                setDisplay()
            }
            contents += (cbxPadCurve.cbxV3, "cell 5 0")
            contents += (cbxPadCurve.cbxV4, "cell 5 0")

            private[this] val lblPadGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mnePadGate").charAt(0)) }
            contents += (lblPadGate, "cell 4 1,alignx right")

            private[this] val cbxPadGate = new GateTimeComboBox("cbxPadGate", L.G("ttPadGate"), lblPadGate) with EditableComboBoxBindings[String] {
                protected override def _get() = selection.item = GateTime.toString(jTrapKATEditor.currentPad.gate)
                protected override def _set() = jTrapKATEditor.currentPad.gate = GateTime.toGateTime(selection.item)
                protected override def _chg() = jTrapKATEditor.padChangedBy(this)

                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "cbxKitGate", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarGate", tpnKitPadsDetails) => setDisplay()
                }

                setDisplay()
            }
            contents += (cbxPadGate, "cell 5 1")

            private[this] val lblPadChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mnePadChannel").charAt(0)) }
            contents += (lblPadChannel, "cell 4 2,alignx right")

            private[this] val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnPadChannel", L.G("ttPadChannel"), lblPadChannel) with Bindings {
                protected override def _get() = value = jTrapKATEditor.currentPad.channel + 1
                protected override def _set() = jTrapKATEditor.currentPad.channel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected override def _chg() = jTrapKATEditor.padChangedBy(this)

                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "spnKitChannel", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarChannel", tpnKitPadsDetails) => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (spnPadChannel, "cell 5 2")

            private[this] object pnPadVelocity extends MigPanel("insets 3, gap 0", "[][]", "[][][]") {
                name = "pnPadVelocity"
                tooltip = L.G("ttPadVelocity")
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq(
                    (0, "Min", 1, () => jTrapKATEditor.currentPad.minVelocity, (value: Byte) => jTrapKATEditor.currentPad.minVelocity = value),
                    (1, "Max", 127, () => jTrapKATEditor.currentPad.maxVelocity, (value: Byte) => jTrapKATEditor.currentPad.maxVelocity = value)
                ) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5) match {
                            case (_x, _name, _ini, _getVel, _setVel) => {
                                val lbl = new Label(L.G(s"lbl${_name}"))
                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnPadVel${_name}", tooltip, lbl) with Bindings {
                                    protected override def _get() = value = _getVel()
                                    protected override def _set() = _setVel(value.asInstanceOf[java.lang.Number].byteValue())
                                    protected override def _chg() = jTrapKATEditor.padChangedBy(this)

                                    reactions += {
                                        case e: CurrentPadChanged if e.source == jTrapKATEditor
                                            || isSourceComponent(e.source, s"spnKitVel${_name}", tpnKitPadsDetails)
                                            || isSourceComponent(e.source, s"ckbVarVel${_name}", tpnKitPadsDetails) => setDisplay()
                                        case e: ValueChanged => setValue()
                                    }

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
                    val ckbFlag = new CheckBox(s"${flag}") with Bindings {
                        name = s"ckbFlag${flag}"
                        tooltip = L.G("ttPadFlags")
                        margin = new Insets(0, 0, 0, 0)
                        horizontalTextPosition = Alignment.Center
                        verticalTextPosition = Alignment.Top

                        protected override def _get() = selected = ((1 << flag) & jTrapKATEditor.currentPad.flags) != 0
                        protected override def _set() = jTrapKATEditor.currentPad.flags = ((~(1 << flag) & jTrapKATEditor.currentPad.flags) | ((if (selected) 1 else 0) << flag)).toByte
                        protected override def _chg() = jTrapKATEditor.padChangedBy(this)

                        listenTo(pnPedals.pnHH)
                        reactions += {
                            case e: CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
                            case e: CurrentKitChanged if e.source == pnPedals.pnHH  => setDisplay()
                            case e: ButtonClicked                                   => setValue()
                        }

                        setDisplay()
                    }
                    contents += (ckbFlag, s"cell ${8 - flag} 1 1 2,alignx center")
                }
            }
            contents += (pnFlags, "cell 9 0 1 3,growx,aligny top")

            private[this] val pnGlobalPadDynamics = new MigPanel("insets 0,gapx 2, gapy 0", "[4px:n][][][4px:n][][][4px:n][][][4px:n]", "[4px:n][][][4px:n]") {
                name = "pnGlobalPadDynamics"
                border = new TitledBorder(L.G("pnGlobalPadDynamics"))

                private[this] class PadDynamicsSpinner(_name: String, label: Label, _getPD: () => Byte, _setPD: (Byte) => Unit)
                    extends Spinner(new javax.swing.SpinnerNumberModel(199, 0, 255, 1), s"spn${_name.capitalize}", L.G(s"ttGlobal${_name.capitalize}"), label) with Bindings {
                    protected override def _get() = {
                        enabled = jTrapKATEditor.currentPadNumber < 25
                        value = if (enabled) getInt(_getPD()) else 0
                    }
                    protected override def _set() = _setPD(value.asInstanceOf[java.lang.Number].byteValue())
                    protected override def _chg() = jTrapKATEditor.globalMemoryChangedBy(this)

                    reactions += {
                        case e: CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
                        case e: ValueChanged                                    => setValue()
                    }

                    setDisplay()
                }

                Seq(
                    (0, 0, "lowLevel", () => jTrapKATEditor.pd.lowLevel, (value: Byte) => jTrapKATEditor.pd.lowLevel = value),
                    (1, 0, "thresholdManual", () => jTrapKATEditor.pd.thresholdManual, (value: Byte) => jTrapKATEditor.pd.thresholdManual = value),
                    (2, 0, "internalMargin", () => jTrapKATEditor.pd.internalMargin, (value: Byte) => jTrapKATEditor.pd.internalMargin = value),
                    (0, 1, "highLevel", () => jTrapKATEditor.pd.highLevel, (value: Byte) => jTrapKATEditor.pd.highLevel = value),
                    (1, 1, "thresholdActual", () => jTrapKATEditor.pd.thresholdActual, (value: Byte) => jTrapKATEditor.pd.thresholdActual = value),
                    (2, 1, "userMargin", () => jTrapKATEditor.pd.userMargin, (value: Byte) => jTrapKATEditor.pd.userMargin = value)
                ) foreach (tuple => (tuple._1, tuple._2, tuple._3, tuple._4, tuple._5) match {
                        case (_x, _y, _name, _getPD, _setPD) => {
                            val lbl = new Label(L.G(_name))
                            contents += (lbl, s"cell ${1 + 3 * _x} ${1 + _y},alignx right")

                            val spn = new PadDynamicsSpinner(_name, lbl, _getPD, _setPD)
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

            private[this] def okToGoKit(name: String): Boolean = {
                Dialog.showConfirmation(tpnMain,
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
            private[this] class VarXCheckBox(_name: String, _cp: CanBeEnabled, _isKit: () => Boolean, _toKit: () => Unit, padCpName: String*) extends CheckBox(L.G("lblXVarious")) with Bindings {
                name = s"ckbVar${_name}"
                tooltip = _cp.tooltip
                protected override def _get() = {
                    val isKit = _isKit()
                    selected = !isKit
                    padCpName foreach (n => setPadEnabled(n, selected))
                    _cp.enabled = !selected
                }

                protected override def _set() = _toKit()
                protected override def _chg() = jTrapKATEditor.padChangedBy(this)
                protected override def setValue() = {
                    val isKit = _isKit()
                    if (!selected && !isKit && !okToGoKit(_name)) {
                        // was checked -> various, now unchecked -> kit
                        // currently various pad values
                        // not okay to splat, so revert the checkbox (yuck!)
                        try {
                            deafTo(this)
                            selected = true
                        }
                        catch { case e: Exception => e.printStackTrace() }
                        finally { listenTo(this) }
                    }
                    else {
                        padCpName foreach (n => setPadEnabled(n, selected))
                        _cp.enabled = !selected

                        if (!selected && !isKit) {
                            // was checked -> various, now unchecked -> kit
                            // currently various pad values
                            // we got the okay to splat, so splat!
                            super.setValue()
                            // You cannot undo the splat...
                            EditHistory.clear()
                        }
                    }
                }

                reactions += {
                    case e: ButtonClicked => setValue()
                    case e: CurrentKitChanged if (e.source == jTrapKATEditorMenuBar.mnEdit) => setDisplay()
                    case e: CurrentAllMemoryChanged if (e.source == jTrapKATEditor) => tooltip = _cp.tooltip
                }

                setDisplay()
            }

            private[this] def okToGoSCOff(name: String): Boolean = {
                Dialog.showConfirmation(tpnMain,
                    L.G("ToSCOff", name),
                    L.G("ApplicationProductName"),
                    Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok
            }

            private[this] val lblKitCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mneKitCurve").charAt(0)) }
            contents += (lblKitCurve, "cell 0 0,alignx right")

            private[this] val cbxKitCurve: CurveComboBoxV3V4 = new CurveComboBoxV3V4("cbxKitCurve", L.G("ttKitCurve"), lblKitCurve,
                () => jTrapKATEditor.currentKit.curve,
                value => jTrapKATEditor.currentKit.curve = value,
                cbx => jTrapKATEditor.kitChangedBy(cbx)
            ) with KitBindings {
                protected override def _isKit() = jTrapKATEditor.currentKit.isKitCurve
                protected override def _toKit() = jTrapKATEditor.currentKit.toKitCurve()
                protected override def _cp() = cbx

                reactions += {
                    case e: CurrentKitChanged if e.source == ckbVarCurve => setDisplay()
                }

                setDisplay()
            }
            contents += (cbxKitCurve.cbxV3, "cell 1 0")
            contents += (cbxKitCurve.cbxV4, "cell 1 0")
            order += cbxKitCurve.cbxV3.name
            order += cbxKitCurve.cbxV4.name

            private[this] val ckbVarCurve = new VarXCheckBox("Curve", cbxKitCurve, () => jTrapKATEditor.currentKit.isKitCurve, () => jTrapKATEditor.currentKit.toKitCurve(), "cbxPadCurveV3", "cbxPadCurveV4")
            contents += (ckbVarCurve, "cell 2 0")
            order += ckbVarCurve.name

            private[this] val lblKitGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mneKitGate").charAt(0)) }
            contents += (lblKitGate, "cell 0 1,alignx right")

            private[this] val cbxKitGate: GateTimeComboBox = new GateTimeComboBox("cbxKitGate", L.G("ttKitGate"), lblKitGate) with EditableComboBoxBindings[String] with KitBindings {
                protected override def _get() = selection.item = GateTime.toString(jTrapKATEditor.currentKit.gate)
                protected override def _set() = jTrapKATEditor.currentKit.gate = GateTime.toGateTime(selection.item)
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)
                protected override def _isKit() = jTrapKATEditor.currentKit.isKitGate
                protected override def _toKit() = jTrapKATEditor.currentKit.toKitGate()
                protected override def _cp() = this

                reactions += {
                    case e: CurrentKitChanged if e.source == ckbVarGate => setDisplay()
                }

                setDisplay()
            }
            contents += (cbxKitGate, "cell 1 1,spanx 2")
            order += cbxKitGate.name

            private[this] val ckbVarGate = new VarXCheckBox("Gate", cbxKitGate, () => jTrapKATEditor.currentKit.isKitGate, () => jTrapKATEditor.currentKit.toKitGate(), "cbxPadGate")
            contents += (ckbVarGate, "cell 1 1")
            order += ckbVarGate.name

            private[this] val lblKitChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mneKitChannel").charAt(0)) }
            contents += (lblKitChannel, "cell 0 2,alignx right")

            private[this] val spnKitChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnKitChannel", L.G("ttKitChannel"), lblKitChannel) with KitBindings {
                protected override def _get() = value = jTrapKATEditor.currentKit.channel + 1
                protected override def _set() = jTrapKATEditor.currentKit.channel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)
                protected override def _isKit() = jTrapKATEditor.currentKit.isKitChannel
                protected override def _toKit() = jTrapKATEditor.currentKit.toKitChannel()
                protected override def _cp() = this

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor
                        || e.source == ckbVarChannel => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (spnKitChannel, "cell 1 2,spanx 2")
            order += spnKitChannel.name

            private[this] val ckbVarChannel = new VarXCheckBox("Channel", spnKitChannel, () => jTrapKATEditor.currentKit.isKitChannel, () => jTrapKATEditor.currentKit.toKitChannel(), "spnPadChannel")
            contents += (ckbVarChannel, "cell 1 2")
            order += ckbVarChannel.name

            contents += (new Label(L.G("lblFootController")), "cell 0 3 2 1, alignx center, aligny bottom")

            val lblFCFunction = new Label(L.G("lblFCFunction")) { peer.setDisplayedMnemonic(L.G("mneFCFunction").charAt(0)) }
            contents += (lblFCFunction, "cell 0 4,alignx right")

            val cbxFCFunction = new RichComboBox(L.G("fcFunctions").split("\n"), "cbxFCFunction", L.G("ttFCFunction"), lblFCFunction) with ComboBoxBindings[String] {
                protected override def _get() = selection.index = jTrapKATEditor.currentKit.fcFunction
                protected override def _set() = jTrapKATEditor.currentKit.fcFunction = selection.index.toByte
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)
                setDisplay()
            }
            contents += (cbxFCFunction, "cell 1 4")
            order += "cbxFCFunction"

            private[this] val lblFCChannel = new Label(L.G("lblFCChannel")) { peer.setDisplayedMnemonic(L.G("mneFCChannel").charAt(0)) }
            contents += (lblFCChannel, "cell 0 5,alignx right")

            private[this] val spnFCChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnFCChannel", L.G("ttFCChannel"), lblFCChannel) with Bindings {
                protected override def _get() = value = (if (jTrapKATEditor.currentKit.fcChannel < 16) jTrapKATEditor.currentKit.fcChannel else jTrapKATEditor.currentKit(25).channel) + 1
                protected override def _set() = jTrapKATEditor.currentKit.fcChannel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)

                reactions += {
                    case e: CurrentPadChanged if !enabled
                        && (isSourceComponent(e.source, "spnPadChannel", tpnKitPadsDetails)
                            || e.source == spnKitChannel
                            || e.source == ckbVarChannel) => setDisplay()
                    case e: CurrentKitChanged if e.source == ckbAsChick => setDisplay()
                    case e: ValueChanged                                => setValue()
                }

                setDisplay()
            }
            contents += (spnFCChannel, "cell 1 5,spanx 2")
            order += "spnFCChannel"

            private[this] val ckbAsChick = new CheckBox(L.G("ckbAsChick")) with Bindings {
                name = "ckbAsChick"
                tooltip = spnFCChannel.tooltip
                protected override def _get() = {
                    selected = jTrapKATEditor.currentKit.fcChannel >= 16
                    spnFCChannel.enabled = !selected
                }
                protected override def _set() = {
                    spnFCChannel.enabled = !selected
                    jTrapKATEditor.currentKit.fcChannel = if (selected) 16 else jTrapKATEditor.currentKit(25).channel
                }
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)

                reactions += {
                    case e: ButtonClicked => setValue()
                }

                setDisplay()
            }
            contents += (ckbAsChick, "cell 1 5")
            order += "ckbAsChick"

            val lblFCCurve = new Label(L.G("lblFCCurve")) { peer.setDisplayedMnemonic(L.G("mneFCCurve").charAt(0)) }
            contents += (lblFCCurve, "cell 0 6,alignx right")

            val cbxFCCurve = new RichComboBox(L.G("fcCurves").split("\n"), "cbxFCCurve", L.G("ttFCCurve"), lblFCCurve) with ComboBoxBindings[String] {
                protected override def _get() = selection.index = jTrapKATEditor.currentKit.fcCurve
                protected override def _set() = jTrapKATEditor.currentKit.fcCurve = selection.index.toByte
                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)
                setDisplay()
            }
            contents += (cbxFCCurve, "cell 1 6,growx") //
            order += "cbxFCCurve"

            private[this] object pnKitVelocity extends MigPanel("insets 3, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"
                tooltip = L.G("ttKitVelocity")
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq((0, "Min", 1, () => jTrapKATEditor.currentKit.minVelocity, (value: Byte) => jTrapKATEditor.currentKit.minVelocity = value, () => jTrapKATEditor.currentKit.isKitMinVel, () => jTrapKATEditor.currentKit.toKitMinVel()),
                    (1, "Max", 127, () => jTrapKATEditor.currentKit.maxVelocity, (value: Byte) => jTrapKATEditor.currentKit.maxVelocity = value, () => jTrapKATEditor.currentKit.isKitMaxVel, () => jTrapKATEditor.currentKit.toKitMaxVel())
                ) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5, x._6, x._7) match {
                            case (_x, _name, _ini, _getVel, _setVel, _isKitVel, _toKitVel) => {

                                val lbl = new Label(L.G(s"lbl${_name}"))
                                contents += (lbl, s"cell ${_x} 1,alignx center")

                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnKitVel${_name}", tooltip, lbl) with KitBindings {
                                    protected override def _get() = value = _getVel()
                                    protected override def _set() = _setVel(value.asInstanceOf[java.lang.Number].intValue().toByte)
                                    protected override def _chg() = jTrapKATEditor.kitChangedBy(this)
                                    protected override def _isKit() = _isKitVel()
                                    protected override def _toKit() = _toKitVel()
                                    protected override def _cp() = this

                                    reactions += {
                                        case e: ValueChanged => setValue()
                                    }

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

            private[this] object pnSoundControl extends MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
                name = "pnSoundControl"

                private[this] val lblSoundControl = new Label(L.G("lblSoundControl"))
                contents += (lblSoundControl, "cell 0 0,alignx right")

                private[this] val cbxSoundControl = new RichComboBox((1 to 4), "cbxSoundControl", L.G("ttSoundControl"), lblSoundControl) {
                    selection.index = jTrapKATEditor.currentSoundControlNumber
                    listenTo(selection)
                    reactions += {
                        case e: SelectionChanged => jTrapKATEditor.currentSoundControlNumber = selection.index
                    }
                }
                contents += (cbxSoundControl, "cell 1 0")
                order += cbxSoundControl.name

                listenTo(jTrapKATEditor)
                reactions += {
                    case e: CurrentAllMemoryChanged => visible = jTrapKATEditor.doV3V4(false, true)
                }
            }
            contents += (pnSoundControl, "cell 6 0 7 1,center,hidemode 0")

            //_name, _ini, _min, _max, _get, _set, ?_getIsOff, ?_setIsOff
            Seq[(String, Int, Int, Int, () => Byte, Byte => Unit, Option[() => Boolean], Option[Boolean => Unit])](
                ("Volume", 127, 0, 127,
                    () => jTrapKATEditor.sc.volume, (value: Byte) => jTrapKATEditor.sc.volume = value,
                    Some(() => getInt(jTrapKATEditor.sc.volume) >= 128), Some((value: Boolean) => jTrapKATEditor.sc.volume = (if (value) 128 else 127).toByte)),
                ("PrgChg", 1, 1, 128,
                    () => jTrapKATEditor.sc.prgChg, (value: Byte) => jTrapKATEditor.sc.prgChg = value,
                    Some(() => getInt(jTrapKATEditor.sc.prgChg) == 0),
                    Some((value: Boolean) => {
                        jTrapKATEditor.sc.prgChg == (if (value) 0 else 1).toByte
                        Focus.findInComponent(tpnKitPadsDetails, "spnPrgChgTxmChn") match {
                            case Some(cp: Spinner) => cp.enabled = !value
                            case _                 => Console.println("spnPrgChgTxmChn not found")
                        }
                    })),
                ("PrgChgTxmChn", 10, 1, 16,
                    () => {
                        (jTrapKATEditor.sc.prgChgTxmChn + 1).toByte
                    }, (value: Byte) => jTrapKATEditor.sc.prgChgTxmChn = (value - 1).toByte,
                    None, None),
                ("BankMSB", 0, 0, 127,
                    () => jTrapKATEditor.sc.bankMSB, (value: Byte) => jTrapKATEditor.sc.bankMSB = value,
                    Some(() => getInt(jTrapKATEditor.sc.bankMSB) >= 128), Some((value: Boolean) => jTrapKATEditor.sc.bankMSB = (if (value) 128 else 0).toByte)),
                ("BankLSB", 0, 0, 127,
                    () => jTrapKATEditor.sc.bankLSB, (value: Byte) => jTrapKATEditor.sc.bankLSB = value,
                    Some(() => getInt(jTrapKATEditor.sc.bankLSB) >= 128), Some((value: Boolean) => jTrapKATEditor.sc.bankLSB = (if (value) 128 else 0).toByte)),
                ("Bank", 0, 0, 127,
                    () => { jTrapKATEditor.doV3V4(jTrapKATEditor.currentKitV3.bank, 0) },
                    (value: Byte) => jTrapKATEditor.doV3V4(jTrapKATEditor.scBank = value, {}),
                    jTrapKATEditor.doV3V4(Some(() => getInt(jTrapKATEditor.scBank) >= 128), None),
                    jTrapKATEditor.doV3V4(Some((value: Boolean) => jTrapKATEditor.scBank = (if (value) 128 else 0).toByte), None))
            ) map { t =>
                    (t._1, t._2, t._3, t._4, t._5, t._6) match {
                        case (_name, _ini, _min, _max, _getVal, _setVal) => {

                            val lbl = new Label(L.G(s"lbl${_name}")) {
                                name = s"lbl${_name}"
                                L.G(s"mne${_name}") match {
                                    case x if x.length != 0 => peer.setDisplayedMnemonic(x.charAt(0))
                                    case _                  => {}
                                }
                            }
                            val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, _min, _max, 1), s"spn${_name}", L.G(s"ttSC${_name}"), lbl) with Bindings {
                                protected override def _get() = value = if (t._7.getOrElse(() => false)()) value.asInstanceOf[java.lang.Number].intValue()/*_ini*/ else getInt(_getVal())
                                protected override def _set() = _setVal(value.asInstanceOf[java.lang.Number].intValue().toByte)
                                protected override def _chg() = jTrapKATEditor.kitChangedBy(this)

                                protected override def setValue(): Unit = {
                                    val self = this
                                    EditHistory.add(new HistoryAction {
                                        val valueBefore = getInt(_getVal())
                                        val valueAfter = value.asInstanceOf[java.lang.Number].intValue()
                                        val actionName = s"action${_name}"
                                        def urSetVal(value: Int) = {
                                            try {
                                                deafTo(jTrapKATEditor)
                                                deafTo(self)
                                                _setVal(value.toByte)
                                                _get()
                                            }
                                            catch { case e: Exception => e.printStackTrace() }
                                            finally { listenTo(self); listenTo(jTrapKATEditor) }

                                        }
                                        def undoAction = urSetVal(valueBefore)
                                        def redoAction = urSetVal(valueAfter)
                                    })
                                    super.setValue()
                                }

                                reactions += {
                                    case e: CurrentSoundControlChanged => setDisplay()
                                    case e: CurrentKitChanged if isSourceComponent(e.source, s"ckb${_name}", pnKitDetails) => setDisplay()
                                    case e: ValueChanged => setValue()
                                }

                                setDisplay()
                            }

                            val ckb: Option[CheckBox] = (t._7, t._8) match {
                                case (Some(_isOff), Some(_toOff)) => Some(new CheckBox(L.G("ckbSCOff")) with Bindings {
                                    name = s"ckb${_name}"

                                    protected override def _get() = { selected = _isOff(); spn.enabled = !selected }
                                    protected override def _set() = { spn.enabled = !selected; _toOff(selected) }
                                    protected override def _chg() = jTrapKATEditor.kitChangedBy(this)

                                    protected override def setValue(): Unit = {
                                        val self = this
                                        val valueBefore = getInt(_getVal())
                                        super.setValue()
                                        val valueAfter = getInt(_getVal())

                                        EditHistory.add(new HistoryAction {
                                            val stateBefore = !selected
                                            val stateAfter = selected
                                            val actionName = s"action${_name}Off"
                                            def urSetVal(state: Boolean, value: Int) = {
                                                try {
                                                    // Restore state
                                                    deafTo(self)
                                                    selected = state
                                                    // Act on restored state
                                                    deafTo(jTrapKATEditor)
                                                    _set()
                                                    // Restore value
                                                    _setVal(value.toByte)
                                                }
                                                catch { case e: Exception => e.printStackTrace() }
                                                finally { listenTo(self); listenTo(jTrapKATEditor) }
                                                // Let the spinner know something happened
                                                _chg()
                                            }
                                            def undoAction = urSetVal(stateBefore, valueBefore)
                                            def redoAction = urSetVal(stateAfter, valueAfter)
                                        })
                                    }

                                    listenTo(jTrapKATEditor)

                                    reactions += {
                                        case e: CurrentSoundControlChanged                            => setDisplay()
                                        case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                                        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                                        case e: ButtonClicked                                         => setValue()
                                    }

                                    setDisplay()
                                })
                                case _ => None
                            }
                            (lbl, spn, ckb)
                        }
                        case _ => (new Label, new Spinner(new javax.swing.SpinnerNumberModel), None)
                    }
                } zip Seq((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)) foreach { t =>
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

            listenTo(jTrapKATEditor)
            reactions += {
                case e: CurrentAllMemoryChanged => {
                    Seq("lbl", "spn", "ckb") foreach (x => Focus.findInComponent(this, s"${x}Bank") match {
                        case Some(cp) => cp.visible = jTrapKATEditor.doV3V4(true, false)
                        case _        => {}
                    })
                }
            }

            // This is only needed the once at start up... bit of a hack, though.
            // spnPrgChgTxmChn is just that little bit too different...
            Focus.findInComponent(this, "ckbPrgChg") match {
                case Some(ckb: CheckBox) => Focus.findInComponent(this, "spnPrgChgTxmChn") match {
                    case Some(cp: Spinner) => cp.enabled = !ckb.selected
                    case _                 => Console.println("spnPrgChgTxmChn not found")
                }
                case _ => Console.println("ckbPrgChg not found")
            }
            Seq("lbl", "spn", "ckb") foreach (x => Focus.findInComponent(this, s"${x}Bank") match {
                case Some(cp) => cp.visible = jTrapKATEditor.doV3V4(true, false)
                case _        => {}
            })

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, order))
            peer.setFocusTraversalPolicyProvider(true)
        }

        val tpnPadDetails = new TabbedPane.Page("Pad Details", pnPadDetails, L.G("ttPadDetails")) { name = "tpPadDetails" }
        val tpnMoreSlots = new TabbedPane.Page("More Slots", pnMoreSlots, L.G("ttMoreSlots")) { name = "tpMoreSlots" }
        val tpnKitDetails = new TabbedPane.Page("Kit Details", pnKitDetails, L.G("ttKitDetails")) { name = "tpKitDetails" }

        listenTo(jTrapKATEditor)

        reactions += {
            case e: CurrentAllMemoryChanged => {
                val seln = if (selection.index < 0) null else selection.page
                while (pages.length > 0) {
                    pages.remove(pages.length - 1)
                    pages.runCount
                }
                pages += tpnPadDetails
                jTrapKATEditor.doV3V4({}, pages += tpnMoreSlots)
                pages += tpnKitDetails

                if (seln != null) selection.page = jTrapKATEditor.doV3V4(if (seln == tpnMoreSlots) tpnPadDetails else seln, seln)
            }
        }

        pages += tpnPadDetails
        jTrapKATEditor.doV3V4({}, { pages += tpnMoreSlots; {} })
        pages += tpnKitDetails

    }
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

}

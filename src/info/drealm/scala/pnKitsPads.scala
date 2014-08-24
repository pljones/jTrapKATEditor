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

        private[this] object pnSelector extends MigPanel("insets 0", "[][][][grow,fill][][][grow,fill][][][]", "[]") {
            name = "pnSelector"

            private[this] val lblSelectKit = new Label(L.G("lblSelectKit")) { peer.setDisplayedMnemonic(L.G("mneSelectKit").charAt(0)) }
            contents += (lblSelectKit, "cell 0 0,alignx right")

            private[this] val kitNames = new Array[String](24)
            private[this] val cbxSelectKit = new RichComboBox(kitNames, "cbxSelectKit", lblSelectKit) with ComboBoxBindings[String] {
                peer.setMaximumRowCount(24)
                prototypeDisplayValue = Some("WWWWWWWWWWWW")

                private[this] def updateKitName(idx: Int): Unit = kitNames(idx) = s"${idx + 1}: ${jTrapKATEditor.currentAllMemory(idx).kitName}"
                private[this] def updateAllKitNames(): Unit = (0 to 23) foreach updateKitName _
                override protected val _get = () => selection.index = jTrapKATEditor.currentKitNumber
                override protected val _set = () => jTrapKATEditor.currentKitNumber = selection.index

                listenTo(selection)
                listenTo(jTrapKATEditor)

                reactions += {
                    case e: SelectionChanged if e.source == this        => setValue()
                    case e: CurrentKitChanged if e.source == txtKitName => { updateKitName(jTrapKATEditor.currentKitNumber); setDisplay() }
                    case e: CurrentKitChanged                           => setDisplay() //?? will this happen?
                    case e: CurrentAllMemoryChanged                     => { updateAllKitNames(); setDisplay() }
                }

                updateAllKitNames()
                setDisplay()
            }
            contents += (cbxSelectKit, "cell 1 0")

            private[this] val lblKitEdited = new Label(L.G("lblXEdited")) {
                private[this] def setDisplay() = visible = jTrapKATEditor.currentKit.changed

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                }

                setDisplay()
            }
            contents += (lblKitEdited, "cell 2 0")

            private[this] val lblKitName = new Label(L.G("lblKitName"))
            contents += (lblKitName, "cell 4 0,alignx right")

            private[this] val txtKitName = new TextField with Bindings {
                override protected val _get = () => text = jTrapKATEditor.currentKit.kitName.trim()
                override protected val _set = () => {
                    jTrapKATEditor.currentKit.kitName = text
                    jTrapKATEditor.kitChangedBy(this)
                }

                lblKitName.peer.setLabelFor(peer)
                name = "txtKitName"
                columns = 16

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                    case e: ValueChanged            => setValue()
                }

                setDisplay()
            }
            contents += (txtKitName, "cell 5 0")

            private[this] val lblSelectPad = new Label(L.G("lblSelectPad")) { peer.setDisplayedMnemonic(L.G("mneSelectPad").charAt(0)) }
            contents += (lblSelectPad, "cell 7 0,alignx right")

            private[this] val cbxSelectPad = new RichComboBox((1 to 28) map (x => x match {
                case x if x < 25 => s"${x}"
                case x           => L.G(s"lbPad${x}")
            }), "cbxSelectPad", lblSelectPad) with ComboBoxBindings[String] {
                peer.setMaximumRowCount(28)
                prototypeDisplayValue = Some("88 mmmm")
                override protected val _get = () => selection.index = jTrapKATEditor.currentPadNumber
                override protected val _set = () => jTrapKATEditor.currentPadNumber = selection.index

                listenTo(selection)
                listenTo(jTrapKATEditor)

                reactions += {
                    case e: SelectionChanged  => setValue()
                    case e: CurrentPadChanged => setDisplay()
                }

                setDisplay()
            }
            contents += (cbxSelectPad, "cell 8 0")

            private[this] val lblPadEdited = new Label(L.G("lblXEdited")) {
                private[this] def setDisplay(): Unit = visible = jTrapKATEditor.currentPad.changed

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentPadChanged       => setDisplay()
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                }

                setDisplay()
            }
            contents += (lblPadEdited, "cell 9 0")
        }

        private[this] object pnPads extends MigPanel("insets 0, gapx 2", "[grow][grow][grow][grow][grow][grow][grow][grow]", "[][][][]") {
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
                val pn = new Pad(pad._2) {
                    background = if (pad._2 < 11) new Color(224, 255, 255) else new Color(230, 230, 250)
                }
                contents += (pn, pad._1 + ",grow")
                pn
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((1 to 24) map { n => s"cbxPad${n}V3" }) ++ ((1 to 24) map { n => s"cbxPad${n}V4" })))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] object pnPedals extends MigPanel("insets 0", "[grow,leading][][][grow,fill][][grow,fill][][][grow,trailing]", "[]") {
            name = "pnPedals"

            Seq(("cell 1 0", 25), ("cell 2 0", 26), ("cell 6 0", 27), ("cell 7 0", 28)) foreach (pad => {
                val pn = new Pad(pad._2) { background = new Color(228, 228, 228) }
                contents += (pn, pad._1 + ",gapx 1, pad 0 -1 0 1,grow")
                listenTo(pn)
                pn
            })

            private[this] val pnHH = new MigPanel("insets 2, gap 0", "[grow,right][left,fill]", "[]") {
                name = "pnHH"
                border = new LineBorder(java.awt.Color.BLACK)

                private[this] val lblHH = new Label(L.G("lblHH")) { peer.setDisplayedMnemonic(L.G("mneHH").charAt(0)) }
                contents += (lblHH, "cell 0 0,alignx trailing,aligny baseline, gapafter 2")
                (1 to 4) foreach { x =>
                    val cbxHH = new RichComboBox(Seq(L.G("cbxHHOff")) ++ ((1 to 24) map (p => s"${p}")), s"cbxHH${x}", if (x == 1) lblHH else null) with ComboBoxBindings[String] {
                        peer.setMaximumRowCount(25)

                        protected override val _get = () => selection.index = jTrapKATEditor.currentKit.hhPads(x - 1)
                        protected override val _set = () => {
                            jTrapKATEditor.currentKit.hhPads(x - 1, this.selection.index.toByte)
                            jTrapKATEditor.kitChangedBy(this)
                        }

                        listenTo(jTrapKATEditor)

                        reactions += {
                            case e: CurrentKitChanged       => setDisplay()
                            case e: CurrentAllMemoryChanged => setDisplay()
                            case e: SelectionChanged        => setValue()
                        }

                        setDisplay()
                    }
                    contents += (cbxHH, s"cell ${x} 0, grow")
                }
            }
            contents += (pnHH, "cell 4 0")
        }

        contents += (pnSelector, "cell 0 0,growx,aligny baseline")
        contents += (pnPads, "cell 0 1, grow")
        contents += (pnPedals, "cell 0 2,grow")
    }

    private[this] object tpnKitPadsDetails extends TabbedPane() {
        name = "tpnKitPadsDetails"

        private[this] def isSourceComponent(o: Any, name: String, source: Component): Boolean = o match {
            case target: Component if target.name == name => Focus.findInComponent(source, target == _).isDefined
            case _                                        => false
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

            private[this] object pnLinkTo extends MigPanel("insets 5", "[grow,right][left,fill]", "[]") {
                name = "pnLinkTo"

                private[this] val lblLinkTo = new Label(L.G("lblLinkTo")) { peer.setDisplayedMnemonic(L.G("mneLinkTo").charAt(0)) }
                contents += (lblLinkTo, "cell 0 0")

                private[this] val linkTo: Array[String] = new Array[String](28)
                private[this] val cbxLinkTo = new RichComboBox(linkTo, "cbxLinkTo", lblLinkTo) with ComboBoxBindings[String] {
                    prototypeDisplayValue = Some("88 mmmm")

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

                    protected override val _get = () => {
                        setAllKitLinks(jTrapKATEditor.currentPadNumber + 1)
                        selection.index = getSelectionIndex(jTrapKATEditor.currentPadV4.linkTo - 1)
                    }
                    protected override val _set = () => {
                        jTrapKATEditor.currentPadV4.linkTo = (selection.index match {
                            case 0 => jTrapKATEditor.currentPadNumber + 1
                            case e if e + 1 < jTrapKATEditor.currentPadNumber => e
                            case e => e + 1
                        }).toByte
                        jTrapKATEditor.padChangedBy(this)
                    }

                    listenTo(jTrapKATEditor)
                    listenTo(selection)

                    reactions += {
                        case e: CurrentPadChanged if e.source == jTrapKATEditor       => jTrapKATEditor.doV3V4({}, setDisplay())
                        case e: CurrentKitChanged if e.source == jTrapKATEditor       => jTrapKATEditor.doV3V4({}, setDisplay())
                        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => jTrapKATEditor.doV3V4({}, setDisplay())
                        case e: SelectionChanged                                      => setValue()
                    }

                    jTrapKATEditor.doV3V4({}, setDisplay())
                }
                contents += (cbxLinkTo, "cell 1 0")

                listenTo(jTrapKATEditor)
                reactions += {
                    case e: CurrentAllMemoryChanged => visible = jTrapKATEditor.doV3V4(false, true)
                }
            }
            contents += (pnLinkTo, "cell 0 5 4 1,gapy 5,alignx left,aligny center,hidemode 0")

            private[this] val lblPadCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mnePadCurve").charAt(0)) }

            val cbxPadCurve = new CurveComboBoxV3V4("cbxPadCurve", lblPadCurve) with Bindings {
                protected override val _get = () => {
                    deafTo(selection)
                    selection.index = jTrapKATEditor.currentPad.curve
                    listenTo(selection)
                }
                protected override val _set = () => {
                    jTrapKATEditor.currentPad.curve = selection.index.toByte
                    jTrapKATEditor.padChangedBy(cbx)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "cbxKitCurveV3", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "cbxKitCurveV4", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarCurveV3", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarCurveV4", tpnKitPadsDetails) => setDisplay()
                    case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: V3V4SelectionChanged                                  => setValue()
                }

                setDisplay()
            }
            contents += (lblPadCurve, "cell 4 0,alignx right")
            contents += (cbxPadCurve.cbxV3, "cell 5 0")
            contents += (cbxPadCurve.cbxV4, "cell 5 0")

            val lblPadGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mnePadGate").charAt(0)) }
            val cbxPadGate = new GateTimeComboBox("cbxPadGate", lblPadGate) with ComboBoxBindings[String] {
                protected override val _get = () => selection.item = GateTime.toString(jTrapKATEditor.currentPad.gate)
                protected override val _set = () => {
                    jTrapKATEditor.currentPad.gate = GateTime.toGateTime(selection.item)
                    jTrapKATEditor.padChangedBy(this)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "cbxKitGate", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarGate", tpnKitPadsDetails) => setDisplay()
                    case e: CurrentKitChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (lblPadGate, "cell 4 1,alignx right")
            contents += (cbxPadGate, "cell 5 1")

            private[this] val lblPadChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mnePadChannel").charAt(0)) }
            private[this] val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnPadChannel", lblPadChannel) with Bindings {
                protected override val _get = () => value = jTrapKATEditor.currentPad.channel + 1
                protected override val _set = () => {
                    jTrapKATEditor.currentPad.channel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                    jTrapKATEditor.padChangedBy(this)
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentPadChanged if e.source == jTrapKATEditor
                        || isSourceComponent(e.source, "spnKitChannel", tpnKitPadsDetails)
                        || isSourceComponent(e.source, "ckbVarChannel", tpnKitPadsDetails) => setDisplay()
                    case e: CurrentKitChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (lblPadChannel, "cell 4 2,alignx right")
            contents += (spnPadChannel, "cell 5 2")

            private[this] val pnPadVelocity = new MigPanel("insets 3, gap 0", "[][]", "[][][]") {
                name = "pnPadVelocity"
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq(
                    (0, "Min", 1, () => jTrapKATEditor.currentPad.minVelocity, (value: Byte) => jTrapKATEditor.currentPad.minVelocity = value),
                    (1, "Max", 127, () => jTrapKATEditor.currentPad.maxVelocity, (value: Byte) => jTrapKATEditor.currentPad.maxVelocity = value)
                ) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5) match {
                            case (_x, _name, _ini, _getVel, _setVel) => {
                                val lbl = new Label(L.G(s"lbl${_name}"))
                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnPadVel${_name}", lbl) with Bindings {
                                    protected override val _get = () => value = _getVel()
                                    protected override val _set = () => {
                                        _setVel(value.asInstanceOf[java.lang.Number].byteValue())
                                        jTrapKATEditor.padChangedBy(this)
                                    }

                                    listenTo(jTrapKATEditor)

                                    reactions += {
                                        case e: CurrentPadChanged if e.source == jTrapKATEditor
                                            || isSourceComponent(e.source, s"spnKitVel${_name}", tpnKitPadsDetails)
                                            || isSourceComponent(e.source, s"ckbVarVel${_name}", tpnKitPadsDetails) => setDisplay()
                                        case e: CurrentKitChanged if e.source == jTrapKATEditor => setDisplay()
                                        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
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

                contents += (new Label(L.G("lblFlags")), "cell 0 0 9 1,alignx center")

                (7 to 0 by -1) foreach { flag =>
                    val ckbFlag = new CheckBox(s"${flag}") with Bindings {
                        name = s"ckbFlag${flag}"
                        margin = new Insets(0, 0, 0, 0)
                        horizontalTextPosition = Alignment.Center
                        verticalTextPosition = Alignment.Top

                        protected override val _get = () => this.selected = ((1 << flag) & jTrapKATEditor.currentPad.flags) != 0
                        protected override val _set = () => {
                            jTrapKATEditor.currentPad.flags = ((~(1 << flag) & jTrapKATEditor.currentPad.flags) | ((if (this.selected) 1 else 0) << flag)).toByte
                            jTrapKATEditor.padChangedBy(this)
                        }

                        listenTo(jTrapKATEditor)

                        reactions += {
                            case e: CurrentPadChanged if e.source == jTrapKATEditor       => setDisplay()
                            case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                            case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                            case e: ButtonClicked                                         => setValue()
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
                            val spn = new Spinner(new javax.swing.SpinnerNumberModel(199, 0, 255, 1), s"spn${_name.capitalize}", lbl) with Bindings {
                                protected override val _get = () => {
                                    enabled = jTrapKATEditor.currentPadNumber < 25
                                    value = if (enabled) 0x000000ff & _getPD() else 0
                                }
                                protected override val _set = () => {
                                    _setPD(value.asInstanceOf[java.lang.Number].byteValue())
                                    jTrapKATEditor.globalMemoryChangedBy(this)
                                }

                                listenTo(jTrapKATEditor)

                                reactions += {
                                    case e: CurrentPadChanged if e.source == jTrapKATEditor => setDisplay()
                                    case e: CurrentKitChanged if e.source == jTrapKATEditor => setDisplay()
                                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                                    case e: ValueChanged => setValue()
                                }

                                setDisplay()
                            }

                            contents += (lbl, s"cell ${1 + 3 * _x} ${1 + _y},alignx right")
                            contents += (spn, s"cell ${2 + 3 * _x} ${1 + _y}")
                        }
                    })
            }
            contents += (pnGlobalPadDynamics, "cell 4 3 6 4,aligny center")

            private[this] val tabOrder = (2 to 6 map { slot => s"cbxSlot${slot}V3" }) ++ (2 to 6 map { slot => s"cbxSlot${slot}V4" }) ++
                Seq("cbxLinkTo") ++
                Seq("cbxPadCurveV3", "cbxPadCurveV4", "cbxPadGate", "spnPadChannel") ++
                Seq("spnPadVelMin", "spnPadVelMax") ++
                (7 to 0 by -1 map { flag => s"ckbFlag${flag}" }) ++
                Seq("spnLowLevel", "spnHighLevel", "spnThresholdManual", "spnInternalMargin", "spnThresholdActual", "spnUserMargin")

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

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((7 to 16) map { slot => s"cbxSlot${slot}V3" }) ++ ((7 to 16) map { slot => s"cbxSlot${slot}V4" })))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] object pnKitDetails extends MigPanel("insets 5, gapx 2, gapy 0, hidemode 3", "[][][][16px:n,grow][][16px:n,grow][][][][4px:n][][][]", "[][][][][][][]") {
            name = "pnKitDetails"

            private[this] val order = scala.collection.mutable.ArrayBuffer.empty[String]

            private[this] def okToGoKit(name: String): Boolean = {
                Dialog.showConfirmation(null,
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
            private[this] def okToGoSCOff(name: String): Boolean = {
                Dialog.showConfirmation(null,
                    L.G("ToSCOff", name),
                    L.G("ApplicationProductName"),
                    Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok
            }

            private[this] val lblKitCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mneKitCurve").charAt(0)) }
            contents += (lblKitCurve, "cell 0 0,alignx right")

            private[this] val cbxKitCurve: CurveComboBoxV3V4 = new CurveComboBoxV3V4("cbxKitCurve", lblKitCurve) with Bindings {
                protected override val _get = () => {
                    deafTo(selection)
                    selection.index = jTrapKATEditor.currentKit.curve
                    listenTo(selection)
                }
                protected override val _set = () => {
                    val isKit = jTrapKATEditor.isKitCurve
                    jTrapKATEditor.currentKit.curve = selection.index.toByte
                    jTrapKATEditor.kitChangedBy(cbx)
                    if (isKit) {
                        jTrapKATEditor.toKitCurve()
                        jTrapKATEditor.padChangedBy(cbx)
                    }
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor || e.source == ckbVarCurve => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: V3V4SelectionChanged => setValue()
                }

                setDisplay()
            }
            contents += (cbxKitCurve.cbxV3, "cell 1 0")
            contents += (cbxKitCurve.cbxV4, "cell 1 0")
            order += cbxKitCurve.cbxV3.name
            order += cbxKitCurve.cbxV4.name

            trait CanBeEnabled {
                def enabled: Boolean
                def enabled_=(value: Boolean): Unit
            }
            implicit def maybeItCanBeEnabled(o: AnyRef): CanBeEnabled = o match {
                case cp: Component                => new CanBeEnabled { def enabled = cp.enabled; def enabled_=(value: Boolean): Unit = cp.enabled = value }
                case cp: V3V4ComboBox[_, _, _, _] => new CanBeEnabled { def enabled = cp.enabled; def enabled_=(value: Boolean): Unit = cp.enabled = value }
                case _                            => throw new ClassCastException(s"Class ${o.getClass().getName()} cannot be cast to CanBeEnabled")
            }
            private[this] class VarXCheckBox(_name: String, _cp: CanBeEnabled, _isKit: () => Boolean, _toKit: () => Unit, padCpName: String*) extends CheckBox(L.G("lblXVarious")) with Bindings {
                name = s"ckbVar${_name}"
                protected override val _get = () => {
                    val isKit = _isKit()
                    Console.println(s"VarXCheckBox ${name} _get set selected to !${isKit}")
                    selected = !_isKit()
                    padCpName foreach (n => setPadEnabled(n, selected))
                    _cp.enabled = !selected
                }
                protected override val _set = () => {
                    if (!selected && !_isKit() && !okToGoKit(_name)) {
                        // was checked -> various, now unchecked -> kit
                        // currently various pad values
                        // not okay to splat, so revert the checkbox (yuck!)
                        deafTo(this)
                        selected = true
                        listenTo(this)
                    }
                    else {
                        padCpName foreach (n => setPadEnabled(n, selected))
                        _cp.enabled = !selected
                        if (!selected && !_isKit()) {
                            // was checked -> various, now unchecked -> kit
                            // currently various pad values
                            // we got the okay to splat, so splat!
                            _toKit()
                            jTrapKATEditor.padChangedBy(this)
                        }
                    }
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ButtonClicked                                         => setValue()
                }

                setDisplay()
            }

            private[this] val ckbVarCurve = new VarXCheckBox("Curve", cbxKitCurve, () => jTrapKATEditor.isKitCurve, () => jTrapKATEditor.toKitCurve(), "cbxPadCurveV3", "cbxPadCurveV4")
            contents += (ckbVarCurve, "cell 2 0")
            order += ckbVarCurve.name

            private[this] val lblKitGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mneKitGate").charAt(0)) }
            contents += (lblKitGate, "cell 0 1,alignx right")

            private[this] val cbxKitGate: GateTimeComboBox = new GateTimeComboBox("cbxKitGate", lblKitGate) with ComboBoxBindings[String] {
                protected override val _get = () => selection.item = GateTime.toString(jTrapKATEditor.currentKit.gate)
                protected override val _set = () => {
                    val isKit = jTrapKATEditor.isKitGate
                    jTrapKATEditor.currentKit.gate = GateTime.toGateTime(selection.item)
                    jTrapKATEditor.kitChangedBy(this)
                    if (isKit) {
                        jTrapKATEditor.toKitGate()
                        jTrapKATEditor.padChangedBy(this)
                    }
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor
                        || e.source == ckbVarGate => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (cbxKitGate, "cell 1 1,spanx 2")
            order += cbxKitGate.name

            private[this] val ckbVarGate = new VarXCheckBox("Gate", cbxKitGate, () => jTrapKATEditor.isKitGate, () => jTrapKATEditor.toKitGate(), "cbxPadGate")
            contents += (ckbVarGate, "cell 1 1")
            order += ckbVarGate.name

            private[this] val lblKitChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mneKitChannel").charAt(0)) }
            contents += (lblKitChannel, "cell 0 2,alignx right")

            private[this] val spnKitChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnKitChannel", lblKitChannel) with Bindings {
                protected override val _get = () => value = jTrapKATEditor.currentKit.channel + 1
                protected override val _set = () => {
                    val isKit = jTrapKATEditor.isKitChannel
                    jTrapKATEditor.currentKit.channel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                    jTrapKATEditor.kitChangedBy(this)
                    if (isKit) {
                        jTrapKATEditor.toKitChannel()
                        jTrapKATEditor.padChangedBy(this)
                    }
                }

                listenTo(jTrapKATEditor)

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

            private[this] val ckbVarChannel = new VarXCheckBox("Channel", spnKitChannel, () => jTrapKATEditor.isKitChannel, () => jTrapKATEditor.toKitChannel(), "spnPadChannel")
            contents += (ckbVarChannel, "cell 1 2")
            order += ckbVarChannel.name

            contents += (new Label(L.G("lblFootController")), "cell 0 3 2 1, alignx center, aligny bottom")

            val lblFCFunction = new Label(L.G("lblFCFunction")) { peer.setDisplayedMnemonic(L.G("mneFCFunction").charAt(0)) }
            contents += (lblFCFunction, "cell 0 4,alignx right")

            val cbxFCFunction = new RichComboBox(L.G("fcFunctions").split("\n"), "cbxFCFunction", lblFCFunction) with ComboBoxBindings[String] {
                protected override val _get = () => selection.index = jTrapKATEditor.currentKit.fcFunction
                protected override val _set = () => {
                    jTrapKATEditor.currentKit.fcFunction = selection.index.toByte
                    jTrapKATEditor.kitChangedBy(this)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: SelectionChanged                                      => setValue()
                }

                setDisplay()
            }
            contents += (cbxFCFunction, "cell 1 4")
            order += "cbxFCFunction"

            val lblFCCurve = new Label(L.G("lblFCCurve")) { peer.setDisplayedMnemonic(L.G("mneFCCurve").charAt(0)) }
            contents += (lblFCCurve, "cell 0 6,alignx right")

            val cbxFCCurve = new RichComboBox(L.G("fcCurves").split("\n"), "cbxFCCurve", lblFCCurve) with ComboBoxBindings[String] {
                protected override val _get = () => selection.index = jTrapKATEditor.currentKit.fcCurve
                protected override val _set = () => {
                    jTrapKATEditor.currentKit.fcCurve = selection.index.toByte
                    jTrapKATEditor.kitChangedBy(this)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: SelectionChanged                                      => setValue()
                }

                setDisplay()
            }
            contents += (cbxFCCurve, "cell 1 6,growx") //
            order += "cbxFCCurve"

            private[this] val lblFCChannel = new Label(L.G("lblFCChannel")) { peer.setDisplayedMnemonic(L.G("mneFCChannel").charAt(0)) }
            contents += (lblFCChannel, "cell 0 5,alignx right")

            private[this] val spnFCChannel: Spinner = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnFCChannel", lblFCChannel) with Bindings {
                protected override val _get = () => value = (if (enabled) jTrapKATEditor.currentKit.fcChannel else jTrapKATEditor.currentKit(25).channel) + 1
                protected override val _set = () => {
                    jTrapKATEditor.currentKit.fcChannel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                    jTrapKATEditor.kitChangedBy(this)
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentPadChanged if !enabled
                        && (isSourceComponent(e.source, "spnPadChannel", tpnKitPadsDetails)
                            || e.source == spnKitChannel
                            || e.source == ckbVarChannel) => setDisplay()
                    case e: CurrentKitChanged if e.source == jTrapKATEditor || e.source == ckbAsChick => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }
            contents += (spnFCChannel, "cell 1 5,spanx 2")
            order += "spnFCChannel"

            private[this] val ckbAsChick = new CheckBox(L.G("ckbAsChick")) with Bindings {
                name = "ckbAsChick"
                protected override val _get = () => {
                    selected = jTrapKATEditor.currentKit.fcChannel >= 16
                    spnFCChannel.enabled = !selected
                }
                protected override val _set = () => {
                    spnFCChannel.enabled = !selected
                    jTrapKATEditor.currentKit.fcChannel = if (selected) 16 else jTrapKATEditor.currentKit(25).channel
                    jTrapKATEditor.kitChangedBy(this)
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentKitChanged if e.source == jTrapKATEditor       => setDisplay()
                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                    case e: ButtonClicked                                         => setValue()
                }

                setDisplay()
            }
            contents += (ckbAsChick, "cell 1 5")
            order += "ckbAsChick"

            private[this] val pnKitVelocity = new MigPanel("insets 3, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq((0, "Min", 1, () => jTrapKATEditor.currentKit.minVelocity, (value: Byte) => jTrapKATEditor.currentKit.minVelocity = value, () => jTrapKATEditor.isKitMinVel, () => jTrapKATEditor.toKitMinVel()),
                    (1, "Max", 127, () => jTrapKATEditor.currentKit.maxVelocity, (value: Byte) => jTrapKATEditor.currentKit.maxVelocity = value, () => jTrapKATEditor.isKitMaxVel, () => jTrapKATEditor.toKitMaxVel())
                ) foreach { x =>
                        (x._1, x._2, x._3, x._4, x._5, x._6, x._7) match {
                            case (_x, _name, _ini, _getVel, _setVel, _isKit, _toKit) => {

                                val lbl = new Label(L.G(s"lbl${_name}"))
                                contents += (lbl, s"cell ${_x} 1,alignx center")

                                val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, 0, 127, 1), s"spnKitVel${_name}", lbl) with Bindings {
                                    protected override val _get = () => value = _getVel()
                                    protected override val _set = () => {
                                        val isKit = _isKit()
                                        _setVel(value.asInstanceOf[java.lang.Number].intValue().toByte)
                                        if (isKit) { _toKit(); jTrapKATEditor.padChangedBy(this) }
                                        jTrapKATEditor.kitChangedBy(this)
                                    }

                                    listenTo(jTrapKATEditor)

                                    reactions += {
                                        case e: CurrentKitChanged if e.source == jTrapKATEditor => setDisplay()
                                        case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                                        case e: ValueChanged => setValue()
                                    }

                                    setDisplay()
                                }
                                contents += (spn, s"cell ${_x} 2")
                                order += spn.name

                                val ckb = new VarXCheckBox(s"Vel${_name}", spn, _isKit, _toKit, s"spnPadVel${_name}") {
                                    background = new Color(228, 228, 228)
                                }
                                contents += (ckb, s"cell ${_x} 3")
                                order += ckb.name
                            }
                        }
                    }

            }
            contents += (pnKitVelocity, "cell 4 0 1 7,growx,aligny top")

            private[this] val pnSoundControl = new MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
                name = "pnSoundControl"

                private[this] val lblSoundControl = new Label(L.G("lblSoundControl"))
                contents += (lblSoundControl, "cell 0 0,alignx right")

                private[this] val cbxSoundControl = new RichComboBox((1 to 4), "cbxSoundControl", lblSoundControl) {
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

            // Unsigned byte, please...
            def getInt(value: Byte) = 0x000000ff & value
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
                            case Some(cp: Spinner) => cp.enabled = value
                            case _                 => Console.println("spnPrgChgTxmChn not found")
                        }
                    })),
                ("PrgChgTxmChn", 10, 1, 16,
                    () => (jTrapKATEditor.sc.prgChgTxmChn + 1).toByte, (value: Byte) => jTrapKATEditor.sc.prgChgTxmChn = (value - 1).toByte,
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
                            val spn = new Spinner(new javax.swing.SpinnerNumberModel(_ini, _min, _max, 1), s"spn${_name}", lbl) with Bindings {
                                protected override val _get = () => value = if (t._7.isDefined && t._7.get()) _ini else getInt(_getVal())
                                protected override val _set = () => {
                                    _setVal(value.asInstanceOf[java.lang.Number].intValue().toByte)
                                    jTrapKATEditor.kitChangedBy(this)
                                }

                                listenTo(jTrapKATEditor)

                                reactions += {
                                    case e: CurrentSoundControlChanged => setDisplay()
                                    case e: CurrentKitChanged if e.source == jTrapKATEditor || isSourceComponent(e.source, s"ckb${_name}", pnKitDetails) => setDisplay()
                                    case e: CurrentAllMemoryChanged if e.source == jTrapKATEditor => setDisplay()
                                    case e: ValueChanged => setValue()
                                }
                            }
                            val ckb: Option[CheckBox] = (t._7, t._8) match {
                                // Very similar to VarXCheckBox - maybe abstract to a supertype?
                                case (Some(_isOff), Some(_toOff)) => Some(new CheckBox(L.G("ckbSCOff")) {
                                    name = s"ckb${_name}"

                                    private[this] def setDisplay(): Unit = {
                                        deafTo(this)
                                        selected = _isOff()
                                        listenTo(this)
                                        spn.enabled = !selected
                                    }
                                    private[this] def setValue(): Unit = {
                                        if (selected && !_isOff() && !okToGoSCOff(_name)) {
                                            // changed to Off, was !Off, not OK, abort...
                                            deafTo(this)
                                            selected = true
                                            listenTo(this)
                                        }
                                        else {
                                            spn.enabled = !selected
                                            deafTo(jTrapKATEditor)
                                            _toOff(selected)
                                            jTrapKATEditor.kitChangedBy(this)
                                            listenTo(jTrapKATEditor)
                                        }
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

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, order))
            peer.setFocusTraversalPolicyProvider(true)
        }

        val tpnPadDetails = new TabbedPane.Page("Pad Details", pnPadDetails) { name = "tpPadDetails" }
        val tpnMoreSlots = new TabbedPane.Page("More Slots", pnMoreSlots) { name = "tpMoreSlots" }
        val tpnKitDetails = new TabbedPane.Page("Kit Details", pnKitDetails) { name = "tpKitDetails" }

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
        jTrapKATEditor.doV3V4({}, pages += tpnMoreSlots)
        pages += tpnKitDetails

    }

    contents += (pnKitsPadsTop, "cell 0 0,grow")
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

}

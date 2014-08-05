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

    private[this] val fcFunctions = L.G("fcFunctions").split("\n").toSeq
    private[this] val fcCurves = L.G("fcCurves").split("\n").toSeq

    private[this] object pnKitsPadsTop extends MigPanel("insets 0", "[grow]", "[][][grow]") {
        name = "pnKitsPadsTop"

        private[this] object pnSelector extends MigPanel("insets 0", "[][][][grow,fill][][][grow,fill][][][]", "[]") {
            name = "pnSelector"

            private[this] val lblSelectKit = new Label(L.G("lblSelectKit")) { peer.setDisplayedMnemonic(L.G("mneSelectKit").charAt(0)) }

            private[this] val kitNames: Array[String] = ((1 to 24) map (x => s"${x}: ${jTrapKATEditor.currentAllMemory(x - 1).kitName}")).toArray
            private[this] val cbxSelectKit = new RichComboBox(kitNames, "cbxSelectKit", lblSelectKit) {
                peer.setMaximumRowCount(24)
                prototypeDisplayValue = Some("WWWWWWWWWWWW")
                selection.index = jTrapKATEditor.currentKitNumber

                def updateKitName(idx: Int): Unit = kitNames(idx) = s"${idx + 1}: ${jTrapKATEditor.currentAllMemory(idx).kitName}"
                def updateAllKitNames(): Unit = {
                    (0 to 23) foreach updateKitName _
                    selectCurrentKit
                }
                def selectCurrentKit(): Unit = {
                    deafTo(this)
                    deafTo(selection)
                    selection.index = jTrapKATEditor.currentKitNumber
                    listenTo(selection)
                    listenTo(this)
                }

                listenTo(selection)
                listenTo(jTrapKATEditor)
                listenTo(jTrapKATEditor.currentAllMemory)

                reactions += {
                    case e: SelectionChanged if e.source == this => {
                        deafTo(jTrapKATEditor)
                        jTrapKATEditor.currentKitNumber = selection.index
                        listenTo(jTrapKATEditor)
                    }
                    case e: CurrentAllMemoryChanged => updateAllKitNames()
                    case e: CurrentKitChanged       => selectCurrentKit()
                    case e: DataItemChanged if e.source == jTrapKATEditor.currentKit => {
                        updateKitName(jTrapKATEditor.currentKitNumber)
                        selectCurrentKit()
                    }
                }

                selectCurrentKit()
            }

            private[this] val lblKitEdited = new Label(L.G("lblXEdited")) {
                visible = jTrapKATEditor.currentKit.changed

                listenTo(jTrapKATEditor)
                listenTo(jTrapKATEditor.currentAllMemory)

                reactions += {
                    case e: CurrentKitChanged                                        => visible = jTrapKATEditor.currentKit.changed
                    case e: CurrentAllMemoryChanged                                  => visible = jTrapKATEditor.currentKit.changed
                    case e: DataItemChanged if e.contains(jTrapKATEditor.currentKit) => visible = jTrapKATEditor.currentKit.changed
                }
            }

            private[this] val lblKitName = new Label(L.G("lblKitName"))

            private[this] val txtKitName = new TextField {
                private[this] def setDisplay(): Unit = {
                    deafTo(this)
                    text = jTrapKATEditor.currentKit.kitName.trim()
                    listenTo(this)
                }
                private[this] def setValue(): Unit = {
                    deafTo(jTrapKATEditor.currentAllMemory)
                    jTrapKATEditor.currentKit.kitName = text
                    listenTo(jTrapKATEditor.currentAllMemory)
                }

                lblKitName.peer.setLabelFor(peer)
                name = "txtKitName"
                columns = 16

                listenTo(jTrapKATEditor)
                listenTo(jTrapKATEditor.currentAllMemory)

                reactions += {
                    case e: CurrentKitChanged => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                    case e: DataItemChanged if e.source == jTrapKATEditor.currentKit => setDisplay()
                    case e: ValueChanged => setValue()
                }

                setDisplay()
            }

            private[this] val lblSelectPad = new Label(L.G("lblSelectPad")) { peer.setDisplayedMnemonic(L.G("mneSelectPad").charAt(0)) }
            private[this] val cbxSelectPad = new RichComboBox((1 to 28) map (x => x match {
                case x if x < 25 => s"${x}"
                case x           => L.G(s"lbPad${x}")
            }), "cbxSelectPad", lblSelectPad) {
                private[this] def setDisplay(): Unit = {
                    deafTo(this)
                    deafTo(selection)
                    selection.index = jTrapKATEditor.currentPadNumber
                    listenTo(selection)
                    listenTo(this)
                }
                private[this] def setValue(): Unit = {
                    deafTo(jTrapKATEditor)
                    jTrapKATEditor.currentPadNumber = selection.index
                    listenTo(jTrapKATEditor)
                }

                peer.setMaximumRowCount(28)
                prototypeDisplayValue = Some("88 mmmm")

                listenTo(selection)
                listenTo(jTrapKATEditor)

                reactions += {
                    case e: SelectionChanged  => setValue()
                    case e: CurrentPadChanged => setDisplay()
                }

                setDisplay()
            }

            def selectedPad: Int = cbxSelectPad.selection.index
            def selectedPad_=(value: Int): Unit = cbxSelectPad.selection.index = value

            private[this] val lblPadEdited = new Label(L.G("lblXEdited")) {
                private[this] def setDisplay(): Unit = visible = jTrapKATEditor.currentPad.changed

                listenTo(jTrapKATEditor)
                listenTo(jTrapKATEditor.currentAllMemory)

                reactions += {
                    case e: CurrentPadChanged                                        => setDisplay()
                    case e: CurrentKitChanged                                        => setDisplay()
                    case e: CurrentAllMemoryChanged                                  => setDisplay()
                    case e: DataItemChanged if e.contains(jTrapKATEditor.currentPad) => setDisplay()
                }

                setDisplay()
            }

            contents += (lblSelectKit, "cell 0 0,alignx right")
            contents += (cbxSelectKit, "cell 1 0")
            contents += (lblKitEdited, "cell 2 0")
            contents += (lblKitName, "cell 4 0,alignx right")
            contents += (txtKitName, "cell 5 0")
            contents += (lblSelectPad, "cell 7 0,alignx right")
            contents += (cbxSelectPad, "cell 8 0")
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
                    val cbxHH = new RichComboBox(Seq(L.G("cbxHHOff")) ++ ((1 to 24) map (p => s"${p}")), s"cbxHH${x}", if (x == 1) lblHH else null) { peer.setMaximumRowCount(25) }
                    contents += (cbxHH, s"cell ${x} 0, grow")
                    listenTo(cbxHH.selection)
                }

                reactions += {
                    case e: SelectionChanged => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }
            }
            contents += (pnHH, "cell 4 0")
            listenTo(pnHH)

            reactions += {
                case e: SelectionChanged => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: CbxEditorFocused => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
            }
        }

        contents += (pnSelector, "cell 0 0,growx,aligny baseline")
        contents += (pnPads, "cell 0 1, grow")
        contents += (pnPedals, "cell 0 2,grow")

        listenTo(pnPads)
        listenTo(pnPedals)

        val padMatch = """^(?:cbxPad)(\d\d?)(?:V[34])""".r
        reactions += {
            case e: CbxEditorFocused => {
                // This is a bit gruesome, too.
                // I guess I could publish and subscribe... bah...
                // TODO: Or, really, a Pad could do this itself...
                padMatch findFirstIn e.source.name match {
                    case Some(padMatch(pad)) => {
                        deafTo(jTrapKATEditor)
                        jTrapKATEditor.currentPadNumber = pad.toInt - 1
                        listenTo(jTrapKATEditor)
                    }
                    case _ => { Console.println("NotAMatch") }
                }
            }
        }
    }

    private[this] object tpnKitPadsDetails extends TabbedPane() {
        name = "tpnKitPadsDetails"

        private[this] object pnPadDetails extends MigPanel("insets 5, gapx 2, gapy 0, hidemode 3", "[][16px:n,right][][16px:n][][][16px:n][][16px:n][]", "[][][][][][][grow]") {
            name = "pnPadDetails"

            contents += (new Label(L.G("lblSlots")), "cell 0 0")

            (2 to 6) foreach { slot =>
                {
                    val s = new Slot(slot)
                    s.lblSlot.peer.setDisplayedMnemonic(s.lblSlot.name.last)
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
                private[this] val cbxLinkTo = new RichComboBox(linkTo, "cbxLinkTo", lblLinkTo) {
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

                    private[this] def onChange(): Unit = {
                        setAllKitLinks(jTrapKATEditor.currentPadNumber + 1)
                        selection.index = getSelectionIndex(jTrapKATEditor.currentPadV4.linkTo - 1)
                    }

                    listenTo(jTrapKATEditor)
                    listenTo(selection)

                    reactions += {
                        case e: CurrentPadChanged       => jTrapKATEditor.doV3V4({}, onChange())
                        case e: CurrentKitChanged       => jTrapKATEditor.doV3V4({}, onChange())
                        case e: CurrentAllMemoryChanged => jTrapKATEditor.doV3V4({}, onChange())
                        case e: SelectionChanged => {
                            deafTo(jTrapKATEditor)
                            jTrapKATEditor.currentPadV4.linkTo = (selection.index match {
                                case 0 => jTrapKATEditor.currentPadNumber + 1
                                case e if e + 1 < jTrapKATEditor.currentPadNumber => e
                                case e => e + 1
                            }).toByte
                            listenTo(jTrapKATEditor)
                        }
                    }

                    jTrapKATEditor.doV3V4({}, onChange())
                }
                contents += (cbxLinkTo, "cell 1 0")
            }
            contents += (pnLinkTo, "cell 0 5 4 1,gapy 5,alignx left,aligny center,hidemode 0")

            val lblPadCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mnePadCurve").charAt(0)) }

            val cbxPadCurve = new CurveComboBoxV3V4("cbxPadCurve", lblPadCurve) {
                private[this] def setDisplay(): Unit = {
                    deafTo(this)
                    deafTo(selection)
                    selection.index = jTrapKATEditor.currentPad.curve
                    listenTo(selection)
                    listenTo(this)
                }
                private[this] def setValue(): Unit = {
                    deafTo(jTrapKATEditor)
                    jTrapKATEditor.currentPad.curve = selection.index.toByte
                    listenTo(jTrapKATEditor)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentPadChanged       => setDisplay()
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                    case e: V3V4SelectionChanged    => setValue()
                }

                setDisplay()
            }
            contents += (lblPadCurve, "cell 4 0,alignx right")
            contents += (cbxPadCurve.cbxV3, "cell 5 0")
            contents += (cbxPadCurve.cbxV4, "cell 5 0")

            val lblPadGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mnePadGate").charAt(0)) }
            val cbxPadGate = new GateTimeComboBox("cbxPadGate", lblPadGate) {
                private[this] def setDisplay(): Unit = {
                    deafTo(selection)
                    deafTo(this)
                    selection.item = GateTime.toString(jTrapKATEditor.currentPad.gate)
                    listenTo(selection)
                    listenTo(this)
                }
                private[this] def setValue(): Unit = {
                    deafTo(jTrapKATEditor)
                    jTrapKATEditor.currentPad.gate = GateTime.toGateTime(selection.item)
                    listenTo(jTrapKATEditor)
                }

                listenTo(jTrapKATEditor)
                listenTo(selection)

                reactions += {
                    case e: CurrentPadChanged       => setDisplay()
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                    case e: ValueChanged            => setValue()
                }

                setDisplay()
            }
            contents += (lblPadGate, "cell 4 1,alignx right")
            contents += (cbxPadGate, "cell 5 1")

            private[this] val lblPadChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mnePadChannel").charAt(0)) }
            private[this] val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnPadChannel", lblPadChannel) {
                private[this] def setDisplay(): Unit = {
                    listenTo(this)
                    value = jTrapKATEditor.currentPad.channel + 1
                    listenTo(this)
                }
                private[this] def setValue(): Unit = {
                    deafTo(jTrapKATEditor)
                    jTrapKATEditor.currentPad.channel = (value.asInstanceOf[java.lang.Number].intValue() - 1).toByte
                    listenTo(jTrapKATEditor)
                }

                listenTo(jTrapKATEditor)

                reactions += {
                    case e: CurrentPadChanged       => setDisplay()
                    case e: CurrentKitChanged       => setDisplay()
                    case e: CurrentAllMemoryChanged => setDisplay()
                    case e: ValueChanged            => setValue()
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
                    (0, "Min", () => jTrapKATEditor.currentPad.minVelocity, (value: Byte) => jTrapKATEditor.currentPad.minVelocity = value),
                    (1, "Max", () => jTrapKATEditor.currentPad.maxVelocity, (value: Byte) => jTrapKATEditor.currentPad.maxVelocity = value)
                ) foreach { x =>
                        val lbl = new Label(L.G(s"lbl${x._2}"))
                        val spn = new Spinner(new javax.swing.SpinnerNumberModel(127, 0, 127, 1), s"spnPadVel${x._2}", lbl) {
                            private[this] def setDisplay(): Unit = {
                                deafTo(this)
                                value = x._3()
                                listenTo(this)
                            }
                            private[this] def setValue(): Unit = {
                                deafTo(jTrapKATEditor)
                                x._4(value.asInstanceOf[java.lang.Number].byteValue())
                                listenTo(jTrapKATEditor)
                            }

                            listenTo(jTrapKATEditor)

                            reactions += {
                                case e: CurrentPadChanged       => setDisplay()
                                case e: CurrentKitChanged       => setDisplay()
                                case e: CurrentAllMemoryChanged => setDisplay()
                                case e: ValueChanged            => setValue()
                            }

                            setDisplay()
                        }
                        contents += (lbl, s"cell ${x._1} 1,alignx center")
                        contents += (spn, s"cell ${x._1} 2")
                    }
            }
            contents += (pnPadVelocity, "cell 7 0 1 3,growx,aligny top")

            private[this] val pnFlags: MigPanel = new MigPanel("insets 0, gap 0", "[][][][][][][][]", "[][][]") {
                name = "pnFlags"

                contents += (new Label(L.G("lblFlags")), "cell 0 0 9 1,alignx center")

                (7 to 0 by -1) foreach { flag =>
                    val ckbFlag = new CheckBox(s"${flag}") {
                        name = s"ckbFlag${flag}"
                        margin = new Insets(0, 0, 0, 0)
                        horizontalTextPosition = Alignment.Center
                        verticalTextPosition = Alignment.Top

                        private[this] def setDisplay(): Unit = {
                            deafTo(this)
                            this.selected = ((1 << flag) & jTrapKATEditor.currentPad.flags) != 0
                            listenTo(this)
                        }
                        private[this] def setValue(): Unit = {
                            deafTo(jTrapKATEditor)
                            jTrapKATEditor.currentPad.flags = ((~(1 << flag) & jTrapKATEditor.currentPad.flags) | ((if (this.selected) 1 else 0) << flag)).toByte
                            listenTo(jTrapKATEditor)
                        }

                        listenTo(jTrapKATEditor)

                        reactions += {
                            case e: CurrentPadChanged       => setDisplay()
                            case e: CurrentKitChanged       => setDisplay()
                            case e: CurrentAllMemoryChanged => setDisplay()
                            case e: ButtonClicked           => setValue()
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
                    (0, 0, "lowLevel", (pd: model.PadDynamics) => pd.lowLevel, (pd: model.PadDynamics, value: Byte) => pd.lowLevel = value),
                    (1, 0, "thresholdManual", (pd: model.PadDynamics) => pd.thresholdManual, (pd: model.PadDynamics, value: Byte) => pd.thresholdManual = value),
                    (2, 0, "thresholdActual", (pd: model.PadDynamics) => pd.thresholdActual, (pd: model.PadDynamics, value: Byte) => pd.thresholdActual = value),
                    (0, 1, "highLevel", (pd: model.PadDynamics) => pd.highLevel, (pd: model.PadDynamics, value: Byte) => pd.highLevel = value),
                    (1, 1, "internalMargin", (pd: model.PadDynamics) => pd.internalMargin, (pd: model.PadDynamics, value: Byte) => pd.internalMargin = value),
                    (2, 1, "userMargin", (pd: model.PadDynamics) => pd.userMargin, (pd: model.PadDynamics, value: Byte) => pd.userMargin = value)
                ) foreach (tuple => {
                        val lbl = new Label(L.G(tuple._3))
                        val spn = new Spinner(new javax.swing.SpinnerNumberModel(199, 0, 255, 1), s"spn${tuple._3.capitalize}", lbl) {
                            private[this] def setDisplay(): Unit = {
                                deafTo(this)
                                value = 0x000000ff & tuple._4(jTrapKATEditor.currentAllMemory.global.padDynamics(jTrapKATEditor.currentPadNumber))
                                listenTo(this)
                            }
                            private[this] def setValue(): Unit = {
                                deafTo(jTrapKATEditor)
                                tuple._5(jTrapKATEditor.currentAllMemory.global.padDynamics(jTrapKATEditor.currentPadNumber), value.asInstanceOf[java.lang.Number].byteValue())
                                listenTo(jTrapKATEditor)
                            }

                            listenTo(jTrapKATEditor)

                            reactions += {
                                case e: CurrentPadChanged       => setDisplay()
                                case e: CurrentKitChanged       => setDisplay()
                                case e: CurrentAllMemoryChanged => setDisplay()
                                case e: ValueChanged            => setValue()
                            }

                            setDisplay()
                        }

                        contents += (lbl, s"cell ${1 + 3 * tuple._1} ${1 + tuple._2},alignx right")
                        contents += (spn, s"cell ${2 + 3 * tuple._1} ${1 + tuple._2}")
                    })
            }
            contents += (pnGlobalPadDynamics, "cell 4 3 6 4,aligny center")

            private[this] val tabOrder = (2 to 6 map { slot => s"cbxSlot${slot}V3" }) ++ (2 to 6 map { slot => s"cbxSlot${slot}V4" }) ++
                Seq("cbxLinkTo") ++
                Seq("cbxPadCurveV3", "cbxPadCurveV4", "cbxPadGate", "spnPadChannel") ++
                Seq("spnPadVelMin", "spnPadVelMax") ++
                (7 to 0 by -1 map { flag => s"ckbFlag${flag}" }) ++
                Seq("spnLowLevel", "spnHighLevel", "spnThresholdManual", "spnInternalMargin", "spnThresholdActual", "spnUserMargin")

            listenTo(jTrapKATEditor)
            reactions += {
                case e: SelectionChanged => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: CbxEditorFocused => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: ValueChanged => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: ButtonClicked => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: CurrentAllMemoryChanged => pnLinkTo.visible = jTrapKATEditor.doV3V4(false, true)
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, tabOrder))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] val pnMoreSlots = new MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][16px:n,right][]", "[][][][][]") {
            name = "pnMoreSlots"

            contents += (new Label(L.G("lblSlots")), "cell 0 0")

            (7 to 11) foreach { slot =>
                {
                    val s = new Slot(slot)
                    s.lblSlot.peer.setDisplayedMnemonic(s.lblSlot.name.last)
                    contents += (s.lblSlot, s"cell 1 ${slot - 7},alignx right")
                    contents += (s.cbxSlot.cbxV4, s"cell 2 ${slot - 7},gapy 2,grow")
                }
            }

            (12 to 16) foreach { slot =>
                {
                    val s = new Slot(slot)
                    s.lblSlot.peer.setDisplayedMnemonic(s.lblSlot.name.last)
                    contents += (s.lblSlot, s"cell 4 ${slot - 12},alignx right")
                    contents += (s.cbxSlot.cbxV4, s"cell 5 ${slot - 12},gapy 2,grow")
                }
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, ((7 to 16) map { slot => s"cbxSlot${slot}V3" }) ++ ((7 to 16) map { slot => s"cbxSlot${slot}V4" })))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] val pnKitDetails = new MigPanel("insets 5, gapx 2, gapy 0, hidemode 3", "[][][][16px:n,grow][][16px:n,grow][][][][4px:n][][][]", "[][][][][][][]") {
            name = "pnKitDetails"

            private[this] val order = scala.collection.mutable.ArrayBuffer.empty[String]

            private[this] val lblKitCurve = new Label(L.G("lblXCurve")) { peer.setDisplayedMnemonic(L.G("mneKitCurve").charAt(0)) }
            private[this] val cbxKitCurve = new CurveComboBoxV3V4("cbxKitCurve", lblKitCurve)
            private[this] val ckbVarCurve = new CheckBox(L.G("lblXVarious")) { name = "ckbVarCurve" }

            private[this] val lblKitGate = new Label(L.G("lblXGate")) { peer.setDisplayedMnemonic(L.G("mneKitGate").charAt(0)) }
            private[this] val cbxKitGate = new GateTimeComboBox("cbxKitGate", lblKitGate)
            private[this] val ckbVarGate = new CheckBox(L.G("lblXVarious")) { name = "ckbVarGate" }

            private[this] val lblKitChannel = new Label(L.G("lblXChannel")) { peer.setDisplayedMnemonic(L.G("mneKitChannel").charAt(0)) }
            private[this] val spnKitChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnKitChannel", lblKitChannel)
            private[this] val ckbVarChannel = new CheckBox(L.G("lblXVarious")) { name = "ckbVarChannel" }

            contents += (lblKitCurve, "cell 0 0,alignx right")
            contents += (cbxKitCurve.cbxV3, "cell 1 0")
            contents += (cbxKitCurve.cbxV4, "cell 1 0")
            contents += (ckbVarCurve, "cell 2 0")

            contents += (lblKitGate, "cell 0 1,alignx right")
            contents += (cbxKitGate, "cell 1 1,spanx 2")
            contents += (ckbVarGate, "cell 1 1")

            contents += (lblKitChannel, "cell 0 2,alignx right")
            contents += (spnKitChannel, "cell 1 2,spanx 2")
            contents += (ckbVarChannel, "cell 1 2")

            listenTo(cbxKitCurve.selection)
            listenTo(ckbVarCurve)

            listenTo(cbxKitGate.selection)
            listenTo(cbxKitGate)
            listenTo(ckbVarGate)

            listenTo(spnKitChannel)
            listenTo(ckbVarChannel)

            order += cbxKitCurve.cbxV3.name
            order += cbxKitCurve.cbxV4.name
            order += ckbVarCurve.name

            order += cbxKitGate.name
            order += ckbVarGate.name

            order += spnKitChannel.name
            order += ckbVarChannel.name

            contents += (new Label(L.G("lblFootController")), "cell 0 3 2 1, alignx center, aligny bottom")

            val lblFCFunction = new Label(L.G("lblFCFunction")) { peer.setDisplayedMnemonic(L.G("mneFCFunction").charAt(0)) }
            val cbxFCFunction = new RichComboBox(fcFunctions, "cbxFCFunction", lblFCFunction)
            contents += (lblFCFunction, "cell 0 4,alignx right")
            contents += (cbxFCFunction, "cell 1 4")
            listenTo(cbxFCFunction.selection)

            val lblFCCurve = new Label(L.G("lblFCCurve")) { peer.setDisplayedMnemonic(L.G("mneFCCurve").charAt(0)) }
            val cbxFCCurve = new RichComboBox(fcCurves, "cbxFCCurve", lblFCCurve)
            contents += (lblFCCurve, "cell 0 6,alignx right")
            contents += (cbxFCCurve, "cell 1 6,growx") //
            listenTo(cbxFCCurve.selection)

            private[this] val lblFCChannel = new Label(L.G("lblFCChannel")) { peer.setDisplayedMnemonic(L.G("mneFCChannel").charAt(0)) }
            private[this] val spnFCChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnFCChannel", lblFCChannel)
            private[this] val ckbAsChick = new CheckBox(L.G("ckbAsChick")) { name = "ckbAsChick" }

            contents += (lblFCChannel, "cell 0 5,alignx right")
            contents += (spnFCChannel, "cell 1 5,spanx 2")
            contents += (ckbAsChick, "cell 1 5")

            listenTo(spnFCChannel)
            listenTo(ckbAsChick)

            order += "cbxFCFunction"
            order += "spnFCChannel"
            order += "ckbAsChick"
            order += "cbxFCCurve"

            val pnKitVelocity = new MigPanel("insets 3, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"
                background = new Color(228, 228, 228)

                contents += (new Label(L.G("lblVelocity")), "cell 0 0 2 1,alignx center")

                Seq((0, "Min"), (1, "Max")) foreach { x =>
                    val lbl = new Label(L.G(s"lbl${x._2}"))
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(127, 0, 127, 1), s"spnKitVel${x._2}", lbl)
                    val ckb = new CheckBox(L.G("ckbVarVel")) {
                        name = s"ckbVarVel${x._2}"
                        background = new Color(228, 228, 228)
                    }

                    contents += (lbl, s"cell ${x._1} 1,alignx center")
                    contents += (spn, s"cell ${x._1} 2")
                    contents += (ckb, s"cell ${x._1} 3")

                    listenTo(spn)
                    listenTo(ckb)

                    order += spn.name
                    order += ckb.name
                }

                reactions += {
                    case e: ValueChanged => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                    case e: ButtonClicked => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }

            }
            contents += (pnKitVelocity, "cell 4 0 1 7,growx,aligny top")
            listenTo(pnKitVelocity)

            val pnSoundControl = new MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
                name = "pnSoundControl"

                private[this] val lblSoundControl = new Label(L.G("lblSoundControl"))
                private[this] val cbxSoundControl = new RichComboBox((1 to 4), "cbxSoundControl", lblSoundControl)

                contents += (lblSoundControl, "cell 0 0,alignx right")
                contents += (cbxSoundControl, "cell 1 0")

                order += cbxSoundControl.name
            }
            contents += (pnSoundControl, "cell 6 0 7 1,center,hidemode 0")
            listenTo(pnSoundControl)

            Seq(("Volume", 127, 0, 127, true),
                ("PrgChg", 1, 1, 128, true),
                ("TxmChn", 10, 1, 16, false),
                ("MSB", 0, 0, 127, true),
                ("LSB", 0, 0, 127, true),
                ("Bank", 0, 0, 127, true)) map { t =>
                    val lbl = new Label(L.G(s"lbl${t._1}")) {
                        name = s"lbl${t._1}"
                        L.G(s"mne${t._1}") match {
                            case x if x.length != 0 => peer.setDisplayedMnemonic(x.charAt(0))
                            case _                  => {}
                        }
                    }
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(t._2, t._3, t._4, 1), s"spn${t._1}", lbl)
                    val ckb = t._5 match {
                        case false => None
                        case true  => Some(new CheckBox(L.G("ckbSCOff")) { name = s"ckb${t._1}" })
                    }
                    (lbl, spn, ckb)
                } zip Seq((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)) foreach { t =>
                    contents += (t._1._1, s"cell ${6 + 3 * t._2._2} ${1 + t._2._1},alignx right,hidemode 0")
                    contents += (t._1._2, s"cell ${7 + 3 * t._2._2} ${1 + t._2._1},growx,hidemode 0")
                    listenTo(t._1._2)
                    order += t._1._2.name

                    t._1._3 match {
                        case Some(ckb) => {
                            contents += (ckb, s"cell ${8 + 3 * t._2._2} ${1 + t._2._1},hidemode 0")
                            listenTo(ckb)
                            order += ckb.name
                        }
                        case None => {}
                    }
                }

            listenTo(jTrapKATEditor)
            reactions += {
                case e: CurrentAllMemoryChanged => {
                    pnSoundControl.visible = jTrapKATEditor.doV3V4(false, true)
                    Seq("lbl", "spn", "ckb") foreach (x => Focus.findInContainer(this, s"${x}Bank") match {
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

    }

    contents += (pnKitsPadsTop, "cell 0 0,grow")
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

}

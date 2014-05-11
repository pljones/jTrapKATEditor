package info.drealm.scala

import javax.swing.border._
import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.spinner._
import info.drealm.scala.eventX._
import info.drealm.scala.layout._

object pnKitsPads extends MigPanel("insets 3", "[grow]", "[][grow]") {
    name = "pnKitsPads"

    private[this] val allPads = (1 to 28) map (x => x + (x match {
        case 25 => " bass"
        case 26 => " chick"
        case 27 => " splash"
        case 28 => " breath"
        case _  => ""
    }))
    private[this] val padCurves = Seq("Curve 1", "Curve 2", "Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7", "Curve 8",
        "2nd Note @ Hardest", "2nd Note @ Hard", "2nd Note @ Medium", "2nd Note @ Soft", "2 Note Layer",
        "Xfade @ Middle", "Xswitch @ Middle", "1@Medium;3@Hardest", "2@Medium;3@Hard", "2Double 1;3Medium",
        "3 Note Layer", "4 Note VelShift", "4 Note Layer", "Alternating", "Control + 3 Notes")
    private[this] val padGates = Seq("Latch Mode", "Infinite", "Roll Mode")
    private[this] val fcFunctions = Seq("Off", "CC#01 (Mod Wheel)", "CC#04 (F/C 0..64)", "CC#04 (F/C 0..127)", "Hat Note")
    private[this] val fcCurves = Seq("Curve 1", "Curve 2", "Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7")

    private[this] val pnKitsPadsTop = new MigPanel("insets 0", "[grow]", "[][][grow]") {
        name = "pnKitsPadsTop"

        private[this] val pnSelector = new MigPanel("insets 0", "[][][][grow,fill][][][grow,fill][][][]", "[]") {
            name = "pnSelector"

            private[this] val lblSelectKit = new Label("Select Kit:") { peer.setDisplayedMnemonic('K') }
            private[this] val cbxSelectKit = new RichComboBox((1 to 24) map (x => x + ". New Kit"), "cbxSelectKit", lblSelectKit) {
                peer.setMaximumRowCount(24)
                prototypeDisplayValue = Some("WWWWWWWWWWWW")
            }
            private[this] val lblKitEdited = new Label("Edited")
            private[this] val lblKitName = new Label("Name:")
            private[this] val txtKitName = new swing.TextField() {
                name = "txtKitName"
                lblKitName.peer.setLabelFor(peer)
                columns = 16
            }
            private[this] val lblSelectPad = new Label("Select Pad:") { peer.setDisplayedMnemonic('P') }
            private[this] val cbxSelectPad = new RichComboBox(allPads, "cbxSelectPad", lblSelectPad) { peer.setMaximumRowCount(28) }
            private[this] val lblPadEdited = new Label("Edited")

            contents += (lblSelectKit, "cell 0 0,alignx right")
            contents += (cbxSelectKit, "cell 1 0")
            contents += (lblKitEdited, "cell 2 0")
            contents += (lblKitName, "cell 4 0,alignx right")
            contents += (txtKitName, "cell 5 0")
            contents += (lblSelectPad, "cell 7 0,alignx right")
            contents += (cbxSelectPad, "cell 8 0")
            contents += (lblPadEdited, "cell 9 0")

            listenTo(cbxSelectKit.selection)
            listenTo(txtKitName)
            listenTo(cbxSelectPad.selection)

            reactions += {
                case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: EditDone if (e.source.isInstanceOf[TextField]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
            }
        }

        private[this] val pnPads: MigPanel = new MigPanel("insets 0, gapx 0", "[grow][grow][grow][grow][grow][grow][grow][grow]", "[][][][]") {
            name = "pnPads"

            (for (
                row <- (0 to 3) zip Seq(
                    List(0, 0, 18, 19, 20, 21, 0, 0),
                    List(0, 17, 6, 7, 8, 9, 22, 0),
                    List(16, 5, 1, 2, 3, 4, 10, 23),
                    List(15, 0, 11, 12, 13, 14, 0, 24)
                );
                col <- (0 to 7) zip row._2;
                if col._2 != 0
            ) yield ("cell " + col._1 + " " + row._1, col._2)) map (pad => {
                val pn = new Pad("" + pad._2) { background = if (pad._2 < 11) new Color(224, 255, 255) else new Color(230, 230, 250) }
                contents += (pn, pad._1 + ",grow")
                listenTo(pn)
            })

            reactions += {
                case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
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

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, 1 to 24 map { n => "cbxPad" + n }))
            peer.setFocusTraversalPolicyProvider(true)
        }

        private[this] val pnPedals = new MigPanel("insets 0", "[grow,leading][][][grow,fill][][grow,fill][][][grow,trailing]", "[]") {
            name = "pnPedals"

            Seq(("cell 1 0", "Chick"), ("cell 2 0", "Splash"), ("cell 6 0", "B/C"), ("cell 7 0", "Bass")) map (pad => {
                val pn = new Pad(pad._2)
                contents += (pn, pad._1 + ",grow")
                listenTo(pn)
            })

            val pnHH = new MigPanel("insets 2, gap 0", "[grow,right][left,fill]", "[]") {
                name = "pnHH"

                private[this] val lblHH = new Label("Hihat Pads:") { peer.setDisplayedMnemonic('d') }
                contents += (lblHH, "cell 0 0,alignx trailing,aligny baseline, gapafter 2")
                (1 to 4) map { x =>
                    val cbxHH = new RichComboBox(Seq("Off") ++ (1 to 24), "cbxHH" + x, if (x == 1) lblHH else null) { peer.setMaximumRowCount(25) }
                    contents += (cbxHH, "cell " + x + " 0, grow")
                    listenTo(cbxHH.selection)
                }

                reactions += {
                    case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }
            }
            contents += (pnHH, "cell 3 0")
            listenTo(pnHH)

            reactions += {
                case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
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

        listenTo(pnSelector)
        listenTo(pnPads)
        listenTo(pnPedals)

        reactions += {
            case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: CbxEditorFocused => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: EditDone if (e.source.isInstanceOf[TextField]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
        }
    }

    private[this] val tpnKitPadsDetails = new TabbedPane() {

        name = "tpnKitPadsDetails"

        val pnPadDetails = new MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][][][16px:n][][16px:n][]", "[][][][][][][grow]") {
            name = "pnPadDetails"

            contents += (new Label("Slots:"), "cell 0 0")

            (2 to 6) map { slot => new Slot(slot) } map { s =>
                {
                    contents += (s._2, "cell 1 " + (s._1 - 2) + ",alignx right")
                    contents += (s._3, "cell 2 " + (s._1 - 2) + ",grow")
                    listenTo(s._3.selection)
                }
            }

            val pnLinkTo = new MigPanel("insets 5", "[grow,right][left,fill]", "[]") {
                name = "pnLinkTo"

                private[this] val lblLinkTo = new Label("Link To:") { peer.setDisplayedMnemonic('L') }
                private[this] val cbxLinkTo = new RichComboBox(Seq("Off") ++ allPads, "cbxLinkTo", lblLinkTo)
                contents += (lblLinkTo, "cell 0 0")
                contents += (cbxLinkTo, "cell 1 0")
                listenTo(cbxLinkTo.selection)

                reactions += {
                    case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }
            }
            contents += (pnLinkTo, "cell 0 5 3 1,alignx center,aligny center")
            listenTo(pnLinkTo)

            Seq((0, "Curve", padCurves, false), (1, "Gate", padGates, true)) map { x =>
                val lbl = new Label(x._2 + ":") { peer.setDisplayedMnemonic(x._2.head) }
                val cbx = new RichComboBox(x._3, "cbxPad" + x._2, lbl) {
                    if (x._4) {
                        makeEditable()
                        selection.index = -1
                        selection.item = "0.115"
                    }
                }
                contents += (lbl, "cell 4 " + x._1 + ",alignx right")
                contents += (cbx, "cell 5 " + x._1)

                listenTo(cbx.selection)
            }

            val lblPadChannel = new Label("Channel:") { peer.setDisplayedMnemonic('n') }
            val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnPadChannel", lblPadChannel)
            contents += (lblPadChannel, "cell 4 2,alignx right")
            contents += (spnPadChannel, "cell 5 2")
            listenTo(spnPadChannel)

            val pnPadVelocity = new MigPanel("insets 0, gap 0", "[][]", "[][][]") {
                name = "pnPadVelocity"

                contents += (new Label("Velocity"), "cell 0 0 2 1,alignx center")

                Seq((0, "Min"), (1, "Max")) map { x =>
                    val lbl = new Label(x._2)
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1), "spnPadVel" + x._2, lbl)
                    contents += (lbl, "cell " + x._1 + " 1,alignx center")
                    contents += (spn, "cell " + x._1 + " 2")
                    listenTo(spn)
                }

                reactions += {
                    case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }
            }
            contents += (pnPadVelocity, "cell 7 0 1 3,growx,aligny top")
            listenTo(pnPadVelocity)

            val pnFlags: MigPanel = new MigPanel("insets 0, gap 0", "[][][][][][][][]", "[][][]") {
                name = "pnFlags"

                contents += (new Label("Flags"), "cell 0 0 9 1,alignx center")

                (7 to 0 by -1) map { flag =>
                    val ckbFlag = new CheckBox("" + flag) {
                        name = "ckbFlag" + flag
                        margin = new Insets(0, 0, 0, 0)
                        horizontalTextPosition = Alignment.Center
                        verticalTextPosition = Alignment.Top
                    }
                    contents += (ckbFlag, "cell " + (8 - flag) + " 1 1 2,alignx center")
                    listenTo(ckbFlag)
                }

                reactions += {
                    case e: ButtonClicked if (e.source.isInstanceOf[CheckBox]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }

            }
            contents += (pnFlags, "cell 9 0 1 3,growx,aligny top")
            listenTo(pnFlags)

            val pnGlobalPadDynamics = new MigPanel("insets 0,gapx 2, gapy 0", "[4px:n][][][4px:n][][][4px:n][][][4px:n]", "[4px:n][][][4px:n]") {
                name = "pnGlobalPadDynamics"
                border = new TitledBorder("Global Pad Dynamics")

                Seq((0, 0, "lowLevel", "Low Dyn"), (1, 0, "thresholdManual", "Threshold"), (2, 0, "thresholdActual", "thresholdActual"),
                    (0, 1, "highLevel", "High Dyn"), (1, 1, "internalMargin", "Pad Idle"), (2, 1, "userMargin", "userMargin")) map (tuple => {
                        val lbl = new Label(tuple._4 + ":")
                        val spn = new Spinner(new javax.swing.SpinnerNumberModel(199, null, 255, 1), "spn" + tuple._3.capitalize, lbl)

                        contents += (lbl, "cell " + (1 + 3 * tuple._1) + " " + (1 + tuple._2) + ",alignx right")
                        contents += (spn, "cell " + (2 + 3 * tuple._1) + " " + (1 + tuple._2))

                        listenTo(spn)
                    })

                reactions += {
                    case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }

            }
            contents += (pnGlobalPadDynamics, "cell 4 3 6 4,aligny center")
            listenTo(pnGlobalPadDynamics)

            reactions += {
                case e => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this,
                (2 to 6 map { slot => "cbxSlot" + slot }) ++
                    Seq("cbxLinkTo") ++
                    Seq("cbxPadCurve", "cbxPadGate", "spnPadChannel") ++
                    Seq("spnPadVelMin", "spnPadVelMax") ++
                    (7 to 0 by -1 map { flag => "ckbFlag" + flag }) ++
                    Seq("spnLowLevel", "spnHighLevel", "spnThresholdManual", "spnInternalMargin", "spnThresholdActual", "spnUserMargin")))
            peer.setFocusTraversalPolicyProvider(true)
        }

        val pnMoreSlots = new MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][16px:n,right][]", "[][][][][]") {
            name = "pnMoreSlots"

            contents += (new Label("Slots:"), "cell 0 0")

            (7 to 11) map { slot => new Slot(slot) } map { s =>
                {
                    contents += (s._2, "cell 1 " + (s._1 - 7) + ",alignx right")
                    contents += (s._3, "cell 2 " + (s._1 - 7) + ",grow")
                    listenTo(s._3.selection)
                }
            }

            (12 to 16) map { slot => new Slot(slot) } map { s =>
                {
                    contents += (s._2, "cell 4 " + (s._1 - 12) + ",alignx right")
                    contents += (s._3, "cell 5 " + (s._1 - 12) + ",grow")
                    listenTo(s._3.selection)
                }
            }

            reactions += {
                case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, (7 to 16 map { slot => "cbxSlot" + slot })))
            peer.setFocusTraversalPolicyProvider(true)
        }

        val pnKitDetails = new MigPanel("insets 5, gapx 2, gapy 0", "[][][][16px:n,grow][][16px:n,grow][][][][4px:n][][][]", "[][][][][][][]") {
            name = "pnKitDetails"

            var order = List[String]()

            private[this] val lblKitCurve = new Label("Curve:") { peer.setDisplayedMnemonic('C') }
            private[this] val cbxKitCurve = new RichComboBox(padCurves, "cbxKitCurve", lblKitCurve)
            private[this] val ckbVarCurve = new CheckBox("Various") { name = "ckbVarCurve" }

            private[this] val lblKitGate = new Label("Gate:") { peer.setDisplayedMnemonic('G') }
            private[this] val cbxKitGate = new RichComboBox(padGates, "cbxKitGate", lblKitGate) {
                makeEditable()
                selection.index = -1
                selection.item = "0.115"
            }
            private[this] val ckbVarGate = new CheckBox("Various") { name = "ckbVarGate" }

            private[this] val lblKitChannel = new Label("Channel:") { peer.setDisplayedMnemonic('n') }
            private[this] val spnKitChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnKitChannel", lblKitChannel)
            private[this] val ckbVarChannel = new CheckBox("Various") { name = "ckbVarChannel" }

            contents += (lblKitCurve, "cell 0 0,alignx right")
            contents += (cbxKitCurve, "cell 1 0")
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
            listenTo(ckbVarGate)

            listenTo(spnKitChannel)
            listenTo(ckbVarChannel)

            order = order ++ Seq(cbxKitCurve.name, ckbVarCurve.name)
            order = order ++ Seq(cbxKitGate.name, ckbVarGate.name)
            order = order ++ Seq(spnKitChannel.name, ckbVarChannel.name)

            contents += (new Label("Foot Controller"), "cell 0 3 2 1, alignx center, aligny bottom")

            Seq((4, "Function", fcFunctions, 'o', ""), (6, "Curve", fcCurves, 'v', ",growx")) map { x =>
                val lbl = new Label(x._2 + ":") { peer.setDisplayedMnemonic(x._4) }
                val cbx = new RichComboBox(x._3, "cbxFC" + x._2, lbl)
                contents += (lbl, "cell 0 " + x._1 + ",alignx right")
                contents += (cbx, "cell 1 " + x._1 + x._5)
                listenTo(cbx.selection)
            }

            private[this] val lblFCChannel = new Label("Channel:") { peer.setDisplayedMnemonic('l') }
            private[this] val spnFCChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1), "spnFCChannel", lblFCChannel)
            private[this] val ckbAsChick = new CheckBox("Same as Chick") { name = "ckbAsChick" }

            contents += (lblFCChannel, "cell 0 5,alignx right")
            contents += (spnFCChannel, "cell 1 5,spanx 2")
            contents += (ckbAsChick, "cell 1 5")

            listenTo(spnFCChannel)
            listenTo(ckbAsChick)

            order = order ++ Seq("cbxFCFunction", spnFCChannel.name, ckbAsChick.name, "cbxFCCurve")

            val pnKitVelocity = new MigPanel("insets 0, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"

                contents += (new Label("Velocity"), "cell 0 0 2 1,alignx center")

                Seq((0, "Min"), (1, "Max")) map { x =>
                    val lbl = new Label(x._2)
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1), "spnKitVel" + x._2, lbl)
                    val ckb = new CheckBox("Var.") { name = "ckbVarVel" + x._2 }

                    contents += (lbl, "cell " + x._1 + " 1,alignx center")
                    contents += (spn, "cell " + x._1 + " 2")
                    contents += (ckb, "cell " + x._1 + " 3")

                    listenTo(spn)
                    listenTo(ckb)

                    order = order ++ Seq(spn.name, ckb.name)
                }

                reactions += {
                    case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                    case e: ButtonClicked if (e.source.isInstanceOf[CheckBox]) => {
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

                private[this] val lblSoundControl = new Label("Sound Control:")
                private[this] val cbxSoundControl = new RichComboBox((1 to 4), "cbxSoundControl", lblSoundControl)

                contents += (lblSoundControl, "cell 0 0,alignx right")
                contents += (cbxSoundControl, "cell 1 0")

                listenTo(cbxSoundControl.selection)

                order = order ++ Seq(cbxSoundControl.name)

                reactions += {
                    case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                        deafTo(this)
                        publish(e)
                        listenTo(this)
                    }
                }

            }
            contents += (pnSoundControl, "cell 6 0 7 1,center")
            listenTo(pnSoundControl)

            Seq(("Volume", Some('m'), 127, 0, 127, true),
                ("PrgChg", Some('r'), 1, 1, 128, true),
                ("TxmChn", None, 10, 1, 16, false),
                ("MSB", None, 0, 0, 127, true),
                ("LSB", None, 0, 0, 127, true),
                ("Bank", Some('B'), 0, 0, 127, true)) map { t =>
                    val lbl = new Label(t._1) { if (t._2.isDefined) peer.setDisplayedMnemonic(t._2.get) }
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(t._3, t._4, t._5, 1), "spn" + t._1, lbl)
                    val ckb = t._6 match {
                        case false => None
                        case true  => Some(new CheckBox("Off") { name = "ckb" + t._1 })
                    }
                    (lbl, spn, ckb)
                } zip Seq((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)) map { t =>
                    contents += (t._1._1, "cell " + (6 + 3 * t._2._2) + " " + (1 + t._2._1) + ",alignx right")
                    contents += (t._1._2, "cell " + (7 + 3 * t._2._2) + " " + (1 + t._2._1) + ",growx")
                    listenTo(t._1._2)
                    order = order ++ Seq(t._1._2.name)

                    t._1._3 match {
                        case Some(ckb) => {
                            contents += (ckb, "cell " + (8 + 3 * t._2._2) + " " + (1 + t._2._1))
                            listenTo(ckb)
                            order = order ++ Seq(ckb.name)
                        }
                        case None => {}
                    }
                }

            reactions += {
                case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
                case e: ButtonClicked if (e.source.isInstanceOf[CheckBox]) => {
                    deafTo(this)
                    publish(e)
                    listenTo(this)
                }
            }

            peer.setFocusTraversalPolicy(new NameSeqOrderTraversalPolicy(this, order))
            peer.setFocusTraversalPolicyProvider(true)
        }

        pages += new TabbedPane.Page("Pad Details", pnPadDetails) { name = "tpPadDetails" }
        pages += new TabbedPane.Page("More Slots", pnMoreSlots) { name = "tpMoreSlots" }
        pages += new TabbedPane.Page("Kit Details", pnKitDetails) { name = "tpKitDetails" }

        listenTo(selection)
        listenTo(pnPadDetails)
        listenTo(pnMoreSlots)
        listenTo(pnKitDetails)

        reactions += {
            case e: SelectionChanged if (e.source.isInstanceOf[TabbedPane]) => e.source match {
                case tpn: TabbedPane => {
                    deafTo(this)
                    publish(new TabChangeEvent(tpn.selection.page))
                    listenTo(this)
                }
            }
            case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: ButtonClicked if (e.source.isInstanceOf[CheckBox]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: EditDone if (e.source.isInstanceOf[TextField]) => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
        }

    }

    contents += (pnKitsPadsTop, "cell 0 0,grow")
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

    listenTo(pnKitsPadsTop)
    listenTo(tpnKitPadsDetails)
    reactions += {
        case e: TabChangeEvent => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: CbxEditorFocused => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: ValueChanged if (e.source.isInstanceOf[Spinner]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: ButtonClicked if (e.source.isInstanceOf[CheckBox]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: EditDone if (e.source.isInstanceOf[TextField]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }

}
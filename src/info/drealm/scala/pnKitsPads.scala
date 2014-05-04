package info.drealm.scala

import javax.swing.border._
import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.spinner._

object pnKitsPads extends TabbedPane.Page("Kits & Pads", new MigPanel("insets 3", "[grow]", "[][grow]") {
    name = "pnKitsPads"

    val allPads = (1 to 28) map (x => x + (x match {
        case 25 => " bass"
        case 26 => " chick"
        case 27 => " splash"
        case 28 => " breath"
        case _  => ""
    }))
    val padFunction = Seq("", "Off", "Seq Start", "Seq Stop", "Seq Cont", "Alt Reset", "Next Kit", "Prev Kit")
    val padCurves = Seq("Curve 1", "Curve 2", "Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7", "Curve 8",
        "2nd Note @ Hardest", "2nd Note @ Hard", "2nd Note @ Medium", "2nd Note @ Soft", "2 Note Layer",
        "Xfade @ Middle", "Xswitch @ Middle", "1@Medium;3@Hardest", "2@Medium;3@Hard", "2Double 1;3Medium",
        "3 Note Layer", "4 Note VelShift", "4 Note Layer", "Alternating", "Control + 3 Notes")
    val padGates = Seq("", "Latch Mode", "Infinite", "Roll Mode")
    val fcFunctions = Seq("Off", "CC#01 (Mod Wheel)", "CC#04 (F/C 0..64)", "CC#04 (F/C 0..127)", "Hat Note")
    val fcCurves = Seq("Curve 1", "Curve 2", "Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7")

    val pnKitsPadsTop = new MigPanel("insets 0", "[grow]", "[][][grow]") {
        name = "pnKitsPadsTop"

        val pnSelector = new MigPanel("insets 0", "[][][][grow,fill][][][grow,fill][][][]", "[]") {
            name = "pnSelector"

            val lblSelectKit = new Label("Select Kit:")
            contents += (lblSelectKit, "cell 0 0,alignx right")

            val cbxSelectKit = new ComboBox((1 to 24) map (x => x + ". New Kit")) {
                name = "cbxSelectKit"
                peer.setMaximumRowCount(24)
                // Uhhhhh, right...
                lblSelectKit.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                lblSelectKit.peer.setDisplayedMnemonic('K')
                prototypeDisplayValue = Some("WWWWWWWWWWWW")
            }
            contents += (cbxSelectKit, "cell 1 0")
            listenTo(cbxSelectKit)

            val lblKitEdited = new Label("Edited")
            contents += (lblKitEdited, "cell 2 0")

            val lblKitName = new Label("Name:")
            contents += (lblKitName, "cell 4 0,alignx right")

            val txtKitName = new swing.TextField() {
                name = "txtKitName"
                lblKitName.peer.setLabelFor(peer)
                columns = 16
            }
            contents += (txtKitName, "cell 5 0")
            listenTo(txtKitName)

            val lblSelectPad = new Label("Select Pad:")
            contents += (lblSelectPad, "cell 7 0,alignx right")

            val cbxSelectPad = new ComboBox(allPads) {
                name = "cbxSelectPad"
                peer.setMaximumRowCount(28)
                lblSelectPad.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                lblSelectPad.peer.setDisplayedMnemonic('P')
            }
            contents += (cbxSelectPad, "cell 8 0")
            listenTo(cbxSelectPad)

            val lblPadEdited = new Label("Edited")
            contents += (lblPadEdited, "cell 9 0")

        }

        val pnPads = new MigPanel("insets 0, gap 0", "[grow][grow][grow][grow][grow][grow][grow][grow]", "[][][][]") {
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
                contents += (new MigPanel("insets 2", "[grow,right][fill,left]", "[]") {
                    background = if (pad._2 < 11) new Color(224, 255, 255) else new Color(230, 230, 250)
                    contents += (new Label("" + pad._2), "cell 0 0,alignx trailing,aligny baseline")
                    val cbxPad = new ComboBox(padFunction) {
                        name = "cbxPad" + pad._2
                        maximumSize = new Dimension(72, 32767)
                        makeEditable()
                    }
                    contents += (cbxPad, "cell 1 0,grow")
                    listenTo(cbxPad)
                }, pad._1 + ",grow")
            })
        }

        val pnPedals=new MigPanel("insets 0", "[grow,leading][][][grow,fill][][grow,fill][][][grow,trailing]", "[]") {
            name = "pnPedals"

            Seq(("cell 1 0", "Chick"), ("cell 2 0", "Splash"), ("cell 6 0", "B/C"), ("cell 7 0", "Bass")) map (pad => {
                contents += (new MigPanel("insets 2", "[grow,right][fill,left]", "[]") {
                    contents += (new Label(pad._2), "cell 0 0,alignx trailing,aligny baseline")
                    val cbxPad = new ComboBox(padFunction) {
                        name = "cbxPad" + pad._2
                        maximumSize = new Dimension(72, 32767)
                        makeEditable()
                    }
                    contents += (cbxPad, "cell 1 0,grow")
                    listenTo(cbxPad)
                }, pad._1)
            })

            contents += (new MigPanel("insets 2, gap 0", "[grow,right][left,fill]", "[]") {
                name = "pnHH"

                val lblHH = new Label("Hihat Pads")
                lblHH.peer.setDisplayedMnemonic('d')
                contents += (lblHH, "cell 0 0,alignx trailing,aligny baseline")
                (1 to 4) map { x =>
                    val cbxHH = new ComboBox(Seq("Off") ++ (1 to 24)) {
                        name = "cbxHH" + x
                        peer.setMaximumRowCount(25)
                        maximumSize = new Dimension(48, 32767)
                    }
                    contents += (cbxHH, "cell " + x + " 0, grow")
                    listenTo(cbxHH)
                }
                lblHH.peer.setLabelFor(contents.find(c => c.name == "cbxHH1").head.peer.asInstanceOf[java.awt.Component])

            }, "cell 3 0")

        }

        contents += (pnSelector, "cell 0 0,growx,aligny baseline")
        contents += (pnPads, "cell 0 1, grow")
        contents += (pnPedals, "cell 0 2,grow")

    }

    val tpnKitPadsDetails = new TabbedPane() {
        listenTo(selection)
        reactions += { case tpnE: SelectionChanged => publish(new eventX.TabChangeEvent(selection.page)) }

        name = "tpnKitPadsDetails"
        pages += new TabbedPane.Page("Pad Details", new MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][][][16px:n][][16px:n][]", "[][][][][][][grow]") {
            name = "pnPadDetails"

            contents += (new Label("Slots:"), "cell 0 0")

            (2 to 6) map { slot =>
                val lblSlot = new Label("" + slot)
                val cbxSlot = new ComboBox(padFunction) {
                    name = "cbsSlot" + slot
                    maximumSize = new Dimension(72, 32767)
                    makeEditable()
                    lblSlot.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lblSlot.peer.setDisplayedMnemonic(("" + slot).head)
                }
                contents += (lblSlot, "cell 1 " + (slot - 2) + ",alignx right")
                contents += (cbxSlot, "cell 2 " + (slot - 2) + ",grow")
                listenTo(cbxSlot)
            }

            contents += (new MigPanel("insets 5", "[grow,right][left,fill]", "[]") {
                name = "pnLinkTo"

                val lblLinkTo = new Label("Link To:")
                contents += (lblLinkTo, "cell 0 0")
                val cbxLinkTo = new ComboBox(Seq("Off") ++ allPads) {
                    name = "cbxLinkTo"
                    lblLinkTo.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lblLinkTo.peer.setDisplayedMnemonic('L')
                }
                contents += cbxLinkTo
                listenTo(cbxLinkTo)

            }, "cell 0 5 3 1,alignx center,aligny center")

            Seq((0, "Curve", padCurves, false), (1, "Gate", padGates, true)) map { x =>
                val lbl = new Label(x._2 + ":")
                contents += (lbl, "cell 4 " + x._1 + ",alignx right")
                val cbx = new ComboBox(x._3) {
                    name = "cbxPad" + x._2
                    if (x._4) makeEditable()
                    lbl.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lbl.peer.setDisplayedMnemonic(x._2.head)
                }
                contents += (cbx, "cell 5 " + x._1)
                listenTo(cbx)
            }

            val lblPadChannel = new Label("Channel:")
            val spnPadChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1)) {
                name = "spnPadChannel"
                lblPadChannel.peer.setLabelFor(peer)
                lblPadChannel.peer.setDisplayedMnemonic('n')
            }
            contents += (lblPadChannel, "cell 4 2,alignx right")
            contents += (spnPadChannel, "cell 5 2")
            listenTo(spnPadChannel)

            contents += (new MigPanel("insets 0, gap 0", "[][]", "[][][]") {
                name = "pnPadVelocity"

                contents += (new Label("Velocity"), "cell 0 0 2 1,alignx center")

                val lblPadVelMin = new Label("Min")
                contents += (lblPadVelMin, "cell 0 1,alignx center")
                val spnPadVelMin = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1)) {
                    name = "spnPadVelMin"
                    lblPadVelMin.peer.setLabelFor(peer)
                }
                contents += (spnPadVelMin, "cell 0 2")
                listenTo(spnPadVelMin)

                val lblPadVelMax = new Label("Max")
                contents += (lblPadVelMax, "cell 1 1,alignx center")
                val spnPadVelMax = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1)) {
                    name = "spnPadVelMax"
                    lblPadVelMax.peer.setLabelFor(peer)
                }
                contents += (spnPadVelMax, "cell 1 2")
                listenTo(spnPadVelMax)

            }, "cell 7 0 1 3,growx,aligny top")

            contents += (new MigPanel("insets 0, gap 0", "[][][][][][][][]", "[][][]") {
                name = "pnFlags"

                contents += (new Label("Flags"), "cell 0 0 9 1,alignx center")

                (7 to 0 by -1) map { flag =>
                    val lblFlag = new Label("" + flag)
                    contents += (lblFlag, "cell " + (8 - flag) + " 1,alignx center")
                    val ckbFlag = new CheckBox {
                        name = "ckbFlag" + flag
                        margin = new Insets(0, 0, 0, 0)
                        lblFlag.peer.setLabelFor(peer)
                    }
                    contents += (ckbFlag, "cell " + (8 - flag) + " 2")
                    listenTo(ckbFlag)
                }

            }, "cell 9 0 1 3,growx,aligny top")

            contents += (new MigPanel("insets 0,gapx 2, gapy 0", "[4px:n][][][][][][][4px:n]", "[][4px:n][][][4px:n]") {
                name = "pnGlobalPadDynamics"
                border = new EtchedBorder(EtchedBorder.LOWERED, null, null)

                val lblGlobalPadDynamics = new Label("Global Pad Dynamics")
                contents += (lblGlobalPadDynamics, "cell 1 0 6 1")

                Seq((0, 0, "lowLevel"), (1, 0, "thresholdManual"), (2, 0, "userMargin"),
                    (0, 1, "highLevel"), (1, 1, "thresholdActual"), (2, 1, "internalMargin")) map (tuple => {
                        val lbl = new Label(tuple._3)
                        val spn = new Spinner(new javax.swing.SpinnerNumberModel(199, null, 255, 1)) {
                            name = "spn" + tuple._3.capitalize
                            lbl.peer.setLabelFor(peer)
                        }
                        contents += (lbl, "cell " + (1 + 2 * tuple._1) + " " + (2 + tuple._2) + ",alignx right")
                        contents += (spn, "cell " + (2 + 2 * tuple._1) + " " + (2 + tuple._2))
                        listenTo(spn)
                    })

            }, "cell 4 3 6 4,aligny center")

        }) {
            name = "tpPadDetails"
        }

        pages += new TabbedPane.Page("More Slots", new MigPanel("insets 5, gapx 2, gapy 0", "[][16px:n,right][][16px:n][16px:n,right][]", "[][][][][]") {
            name = "pnMoreSlots"

            contents += (new Label("Slots:"), "cell 0 0")

            (7 to 11) map { slot =>
                val lblSlot = new Label("" + slot)
                val cbxSlot = new ComboBox(padFunction) {
                    name = "cbsSlot" + slot
                    maximumSize = new Dimension(72, 32767)
                    makeEditable()
                    lblSlot.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lblSlot.peer.setDisplayedMnemonic(("" + slot).head)
                }
                contents += (lblSlot, "cell 1 " + (slot - 7) + ",alignx right")
                contents += (cbxSlot, "cell 2 " + (slot - 7) + ",grow")
                listenTo(cbxSlot)
            }

            (12 to 16) map { slot =>
                val lblSlot = new Label("" + slot)
                val cbxSlot = new ComboBox(padFunction) {
                    name = "cbsSlot" + slot
                    maximumSize = new Dimension(72, 32767)
                    makeEditable()
                    lblSlot.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lblSlot.peer.setDisplayedMnemonic(("" + slot).last)
                }
                contents += (lblSlot, "cell 4 " + (slot - 12) + ",alignx right")
                contents += (cbxSlot, "cell 5 " + (slot - 12) + ",grow")
                listenTo(cbxSlot)
            }

        }) {
            name = "tpMoreSlots"
        }

        pages += new TabbedPane.Page("Kit Details", new MigPanel("insets 5, gapx 2, gapy 0", "[][][16px:n][][16px:n][][][][4px:n][][][]", "[][][][][][][]") {
            name = "pnKitDetails"

            Seq((0, "Curve", padCurves, false), (1, "Gate", padGates, true)) map { x =>
                val lbl = new Label(x._2 + ":")
                val cbx = new ComboBox(x._3) {
                    name = "cbxKit" + x._2
                    if (x._4) makeEditable()
                    lbl.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lbl.peer.setDisplayedMnemonic(x._2.head)
                }
                val ckb = new CheckBox("Various") {
                    name = "ckbVar" + x._2
                }
                contents += (lbl, "cell 0 " + x._1 + ",alignx right")
                contents += (cbx, "cell 1 " + x._1)
                contents += (ckb, "cell 1 " + x._1)
                listenTo(cbx)
                listenTo(ckb)
            }

            val lblKitChannel = new Label("Channel:")
            val spnKitChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1)) {
                name = "spnKitChannel"
                lblKitChannel.peer.setLabelFor(peer)
                lblKitChannel.peer.setDisplayedMnemonic('n')
            }
            val ckbVarChannel = new CheckBox("Various") {
                name = "ckbVarChannel"
            }
            contents += (lblKitChannel, "cell 0 2,alignx right")
            contents += (spnKitChannel, "cell 1 2")
            contents += (ckbVarChannel, "cell 1 2")
            listenTo(spnKitChannel)
            listenTo(ckbVarChannel)

            contents += (new Label("Foot Controller"), "cell 0 3 2 1, center")

            Seq((4, "Function", fcFunctions, 'o'), (6, "Curve", fcCurves, 'v')) map { x =>
                val lbl = new Label(x._2 + ":")
                val cbx = new ComboBox(x._3) {
                    name = "cbxFC" + x._2
                    lbl.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                    lbl.peer.setDisplayedMnemonic(x._4)
                }
                contents += (lbl, "cell 0 " + x._1 + ",alignx right")
                contents += (cbx, "cell 1 " + x._1)
                listenTo(cbx)
            }

            val lblFCChannel = new Label("Channel:")
            val spnFCChannel = new Spinner(new javax.swing.SpinnerNumberModel(1, 1, 16, 1)) {
                name = "spnFCChannel"
                lblFCChannel.peer.setLabelFor(peer)
                lblFCChannel.peer.setDisplayedMnemonic('l')
            }
            val ckbAsChick = new CheckBox("Same as Chick") {
                name = "ckbAsChick"
            }
            contents += (lblFCChannel, "cell 0 5,alignx right")
            contents += (spnFCChannel, "cell 1 5")
            contents += (ckbAsChick, "cell 1 5")
            listenTo(spnFCChannel)
            listenTo(ckbAsChick)

            contents += (new MigPanel("insets 0, gap 0", "[][]", "[][][][]") {
                name = "pnKitVelocity"

                contents += (new Label("Velocity"), "cell 0 0 2 1,alignx center")

                val lblKitVelMin = new Label("Min")
                val spnKitVelMin = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1)) {
                    name = "spnKitVelMin"
                    lblKitVelMin.peer.setLabelFor(peer)
                }
                val ckbVarVelMin = new CheckBox("Var.") {
                    name = "ckbVarVelMin"
                }
                contents += (lblKitVelMin, "cell 0 1,alignx center")
                contents += (spnKitVelMin, "cell 0 2")
                contents += (ckbVarVelMin, "cell 0 3")
                listenTo(spnKitVelMin)
                listenTo(ckbVarVelMin)

                val lblKitVelMax = new Label("Max")
                val spnKitVelMax = new Spinner(new javax.swing.SpinnerNumberModel(127, null, 127, 1)) {
                    name = "spnKitVelMax"
                    lblKitVelMax.peer.setLabelFor(peer)
                }
                val ckbVarVelMax = new CheckBox("Var.") {
                    name = "ckbVarVelMax"
                }
                contents += (lblKitVelMax, "cell 1 1,alignx center")
                contents += (spnKitVelMax, "cell 1 2")
                contents += (ckbVarVelMax, "cell 1 3")
                listenTo(spnKitVelMax)
                listenTo(ckbVarVelMax)

            }, "cell 3 0 1 7,growx,aligny top")

            contents += (new MigPanel("insets 0,gapx 2, gapy 0", "[][]", "[]") {
                name = "pnSoundControl"

                val lblSoundControl = new Label("Sound Control:")
                val cbxSoundControl = new ComboBox((1 to 4)) {
                    name = "cbxSoundControl"
                    lblSoundControl.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
                }
                contents += (lblSoundControl, "cell 0 0,alignx right")
                contents += (cbxSoundControl, "cell 1 0")
                listenTo(cbxSoundControl)

            }, "cell 5 0 7 1,center")

            Seq(("Volume", Some('l'), 127, 0, 127, true),
                ("PrgChg", Some('g'), 1, 1, 128, true),
                ("TxmChn", None, 10, 1, 16, false),
                ("MSB", None, 0, 0, 127, true),
                ("LSB", None, 0, 0, 127, true),
                ("Bank", Some('B'), 0, 0, 127, true)) map { t =>
                    val lbl = new Label(t._1)
                    val spn = new Spinner(new javax.swing.SpinnerNumberModel(t._3, t._4, t._5, 1)) {
                        name = "spn" + t._1
                        lbl.peer.setLabelFor(peer)
                        if (t._2.isDefined) lbl.peer.setDisplayedMnemonic(t._2.get)
                    }
                    val ckb = t._6 match {
                        case false => None
                        case true => Some(new CheckBox("Off") {
                            name = "ckb" + t._1
                        })
                    }
                    (lbl, spn, ckb)
                } zip Seq((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)) map { t =>
                    contents += (t._1._1, "cell " + (5 + 3 * t._2._2) + " " + (1 + t._2._1) + ",alignx right")
                    contents += (t._1._2, "cell " + (6 + 3 * t._2._2) + " " + (1 + t._2._1))
                    listenTo(t._1._2)
                    t._1._3 match {
                        case Some(cbx) => {
                            contents += (cbx, "cell " + (7 + 3 * t._2._2) + " " + (1 + t._2._1))
                            listenTo(cbx)
                        }
                        case None => {}
                    }
                }

        })
    }

    contents += (pnKitsPadsTop, "cell 0 0,grow")
    contents += (tpnKitPadsDetails, "cell 0 1,grow")

    listenTo(pnKitsPadsTop)
    listenTo(tpnKitPadsDetails)
    reactions += {
        case e => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }

})
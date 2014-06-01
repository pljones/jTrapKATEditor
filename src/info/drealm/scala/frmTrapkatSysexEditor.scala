/****************************************************************************
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
 ****************************************************************************/

package info.drealm.scala

import swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._

object frmTrapkatSysexEditor extends MainFrame {

    iconImage = toolkit.getImage("resources/tk_wild2-sq.png")
    resizable = false
    title = "TrapKAT SysEx Editor"
    bounds = new Rectangle(100, 100, 880, 516)

    menuBar = jTrapKATEditorMenuBar

    private[this] object tpnMain extends TabbedPane {

        name = "tpnMain"

        val pnGlobal = new MigPanel("insets 5", "[]", "[]") {
            name = "pnGlobal"
            contents += (new Label("Being reworked"), "cell 0 0")
        }

        pages += new TabbedPane.Page("Kits & Pads", pnKitsPads) { name = "tpKitsPads" }
        pages += new TabbedPane.Page("Global", pnGlobal) { name = "tpGlobal" }

        listenTo(selection)
        listenTo(pnKitsPads)
        listenTo(pnGlobal)

        reactions += {
            case e: TabChangeEvent => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: SelectionChanged if (e.source.isInstanceOf[TabbedPane]) => {
                val tpnE = e.source.asInstanceOf[TabbedPane]
                deafTo(this)
                publish(new TabChangeEvent(tpnE.selection.page))
                listenTo(this)
            }
            case e: KitChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: PadChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
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
        }
    }

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {

        contents += (tpnMain, "cell 0 0,grow")

        contents += (new Label("MIDI OX SysEx Transmit: 512 bytes, 8 buffers, 160ms between buffers, 320ms after F7"), "cell 0 1,alignx center")

    }

    listenTo(menuBar)
    listenTo(tpnMain)

    reactions += {
        case miE: MenuItemEvent => {
            deafTo(this)
            publish(miE)
            listenTo(this)
        }
        case mnE: MenuEvent => {
            deafTo(this)
            publish(mnE)
            listenTo(this)
        }
        case e: TabChangeEvent => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: KitChanged => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: PadChanged => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
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
    }

    layout.Focus.set(tpnMain, "cbxPad1")

    centerOnScreen
}

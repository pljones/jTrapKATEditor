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

import swing._
import swing.event._
import info.drealm.scala.eventX._
import info.drealm.scala.{ Localization => L }

object tpnMain extends TabbedPane {

    name = "tpnMain"

    pages += new TabbedPane.Page(L.G("tpKitsPads"), pnKitsPads) { name = "tpKitsPads" }
    pages += new TabbedPane.Page(L.G("tpGlobal"), pnGlobal) { name = "tpGlobal" }

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
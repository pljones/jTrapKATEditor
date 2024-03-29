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

    pages += new TabbedPane.Page(L.G("tpKitsPads"), pnKitsPads, L.G("ttKitsPads")) { name = "tpKitsPads" }
    pages += new TabbedPane.Page(L.G("tpGlobal"), pnGlobal, L.G("ttGlobal")) { name = "tpGlobal" }

}

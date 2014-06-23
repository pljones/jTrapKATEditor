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

package info.drealm.scala.updateTool

import java.util.Date
import java.text.SimpleDateFormat
import swing._
import swing.event._
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs }

object Checker extends Publisher {

    object AutoUpdateMode extends Enumeration {
        type AutoUpdateMode = Value
        val Off, Automatically = Value
        val NotSet = Value(-1)
    }
    import AutoUpdateMode._

    class AutoUpdateModeChanged(val oldMode: AutoUpdateMode, val newMode: AutoUpdateMode) extends Event

    private[this] var _autoUpdateMode: AutoUpdateMode = NotSet
    def autoUpdateMode: AutoUpdateMode = _autoUpdateMode
    def autoUpdateMode_=(value: AutoUpdateMode): Unit = {
        Console.println("Checker autoUpdateMode -> " + value)
        val oldMode = _autoUpdateMode
        _autoUpdateMode = value
        publish(new AutoUpdateModeChanged(oldMode, _autoUpdateMode))
    }

    private[this] def _dateToDay(value: Date): String = new SimpleDateFormat("YYYY-MM-DD").format(value)
    def dailyCheck: Unit = {
        if (autoUpdateMode == Automatically && prefs.lastUpdateTS != _dateToDay(new Date())) {
            prefs.lastUpdateTS = new Date()
            getUpdate(true)
        }
    }

    def getUpdate(auto: Boolean = false): Unit = {
        Console.println("updateTool Checker getUpdate " + auto)
    }
}

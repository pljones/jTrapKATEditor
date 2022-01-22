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

package info.drealm.scala.prefs

import java.util.Date
import java.text.SimpleDateFormat
import swing.event._
import info.drealm.scala._

object Preferences extends swing.Publisher {
    abstract class PreferenceChanged extends Event
    class CurrentWorkingDirectoryPreferencChanged extends PreferenceChanged
    class NotesAsPreferencChanged extends PreferenceChanged
    class UpdateAudomaticallyPreferencChanged extends PreferenceChanged
    class LastUpdateTSPreferencChanged extends PreferenceChanged
    class LastIgnoredVersionPreferencChanged extends PreferenceChanged
    class WindowLocationPreferencChanged extends PreferenceChanged

    lazy val userPreferences = java.util.prefs.Preferences.userNodeForPackage(classOf[PreferenceChanged])
    //lazy val systemPreferences = java.util.prefs.Preferences.systemNodeForPackage(classOf[PreferenceChanged])

    def currentWorkingDirectory: java.io.File = userPreferences.get("currentWorkingDirectory", "") match {
        case "" => util.getHome
        case some => new java.io.File(some)
    }
    def currentWorkingDirectory_=(value: java.io.File): Unit = {
        if (!value.isDirectory())
            throw new IllegalArgumentException(f"${value.getName()} is not a directory.")
        userPreferences.put("currentWorkingDirectory", value.getCanonicalPath())
        publish(new CurrentWorkingDirectoryPreferencChanged)
    }

    def notesAs: DisplayMode.DisplayMode = {
        if (userPreferences.get("notesAs", "NotSet") == "NotSet")
            DisplayMode.AsNumber // Do not need to know if it was not set
        else
            DisplayMode(userPreferences.getInt("notesAs", DisplayMode.AsNumber.id))
    }
    def notesAs_=(value: DisplayMode.DisplayMode): Unit = {
        userPreferences.putInt("notesAs", value.id)
        // These must be in place before the change notification goes out
        PadSlotV3.displayMode = value
        PadSlotV4.displayMode = value
        publish(new NotesAsPreferencChanged)
    }

    def updateAutomatically: updateTool.Checker.AutoUpdateMode.AutoUpdateMode =
        if (userPreferences.get("updateAutomatically", "NotSet") == "NotSet")
            updateTool.Checker.AutoUpdateMode.NotSet
        else if (userPreferences.getBoolean("updateAutomatically", false))
            updateTool.Checker.AutoUpdateMode.Automatically
        else
            updateTool.Checker.AutoUpdateMode.Off
    def updateAutomatically_=(value: updateTool.Checker.AutoUpdateMode.AutoUpdateMode): Unit = {
        userPreferences.putBoolean("updateAutomatically", value == updateTool.Checker.AutoUpdateMode.Automatically)
        publish(new UpdateAudomaticallyPreferencChanged)
    }

    def lastUpdateTS: Date = {
        new SimpleDateFormat("yyyy-MM-dd").parse(userPreferences.get("lastUpdateTS", "2000-01-01")) match {
            case null => new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01")
            case date => date
        }
    }
    def lastUpdateTS_=(value: Date): Unit = {
        userPreferences.put("lastUpdateTS", f"${value}%TF")
        publish(new LastUpdateTSPreferencChanged)
    }

    def lastIgnoredVersion: String = userPreferences.get("lastIgnoredVersion", "00-0101-0000")
    def lastIgnoredVersion_=(value: String): Unit = {
        userPreferences.put("lastIgnoredVersion", value)
        publish(new LastIgnoredVersionPreferencChanged)
    }

    def windowLocation: Tuple2[Int, Int] = {
        val value = userPreferences.get("windowLocation", "-1, -1").split(", ", 2)
        try { (value(0).toInt, value(1).toInt) }
        catch { case e: Exception => (-1, -1) }
    }
    def windowLocation_=(value: Tuple2[Int, Int]): Unit = {
        userPreferences.put("windowLocation", s"${value._1}, ${value._2}")
        publish(new WindowLocationPreferencChanged)
    }
}

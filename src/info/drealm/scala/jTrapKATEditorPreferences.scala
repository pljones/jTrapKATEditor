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

import java.util.prefs._
import java.util.Date
import java.text.SimpleDateFormat
import swing.event._

// Yes, please do rename this on import...
object jTrapKATEditorPreferences extends swing.Publisher {
    abstract class PreferenceChanged extends Event
    class CurrentWorkingDirectoryPreferencChanged extends PreferenceChanged
    class NotesAsPreferencChanged extends PreferenceChanged
    class UpdateAudomaticallyPreferencChanged extends PreferenceChanged
    class LastUpdateTSPreferencChanged extends PreferenceChanged

    lazy val userPreferences = Preferences.userNodeForPackage(classOf[PreferenceChanged])
    //lazy val systemPreferences = Preferences.systemNodeForPackage(classOf[PreferenceChanged])

    def currentWorkingDirectory: java.io.File = userPreferences.get("currentWorkingDirectory", "") match {
        case ""   => util.getHome
        case some => new java.io.File(some)
    }
    def currentWorkingDirectory_=(value: java.io.File): Unit = {
        if (!value.isDirectory())
            throw new IllegalArgumentException(f"${value.getName()} is not a directory.")
        userPreferences.put("currentWorkingDirectory", value.getCanonicalPath())
        publish(new CurrentWorkingDirectoryPreferencChanged)
    }

    def notesAs: PadSlot.DisplayMode.DisplayMode = PadSlot.DisplayMode(userPreferences.getInt("notesAs", PadSlot.DisplayMode.AsNumber.id))
    def notesAs_=(value: PadSlot.DisplayMode.DisplayMode): Unit = {
        userPreferences.putInt("notesAs", value.id)
        publish(new NotesAsPreferencChanged)
    }

    def updateAutomatically: updateTool.Checker.AutoUpdateMode.AutoUpdateMode =
        if (userPreferences.getBoolean("updateAutomatically", false))
            updateTool.Checker.AutoUpdateMode.Automatically
        else
            updateTool.Checker.AutoUpdateMode.Off
    def updateAutomatically_=(value: updateTool.Checker.AutoUpdateMode.AutoUpdateMode): Unit = {
        userPreferences.putBoolean("updateAutomatically", value == updateTool.Checker.AutoUpdateMode.Automatically)
        publish(new UpdateAudomaticallyPreferencChanged)
    }

    def lastUpdateTS: Date = {
        val date = new SimpleDateFormat("YYYY-MM-DD").parse(userPreferences.get("lastUpdateTS", "1970-01-01"))
        if (date == null) new SimpleDateFormat("YYYY-MM-DD").parse("1970-01-01") else date
    }
    def lastUpdateTS_=(value: Date): Unit = {
        userPreferences.put("lastUpdateTS", new SimpleDateFormat("YYYY-MM-DD").format(value))
        publish(new LastUpdateTSPreferencChanged)
    }
}

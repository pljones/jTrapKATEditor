package info.drealm.scala

import java.util.prefs._
import swing.event._

// Yes, please do rename this on import...
object jTrapKATEditorPreferences extends swing.Publisher {
    abstract class PreferenceChanged extends Event
    class CurrentWorkingDirectoryPreferencChanged extends PreferenceChanged
    class NotesAsPreferencChanged extends PreferenceChanged
    class UpdateAudomaticallyPreferencChanged extends PreferenceChanged

    lazy val userPreferences = Preferences.userNodeForPackage(classOf[PreferenceChanged])
    //lazy val systemPreferences = Preferences.systemNodeForPackage(classOf[PreferenceChanged])

    def currentWorkingDirectory: java.io.File = new java.io.File(userPreferences.get("currentWorkingDirectory", ""))
    def currentWorkingDirectory_=(value: java.io.File): Unit = {
        Console.println("jTrapKATEditorPreferences currentWorkingDirectory -> " + value)
        if (!value.isDirectory())
            throw new IllegalArgumentException(f"${value.getName()} is not a directory.")
        userPreferences.put("currentWorkingDirectory", value.getCanonicalPath())
        publish(new CurrentWorkingDirectoryPreferencChanged)
    }

    def notesAs: PadSlot.DisplayMode.DisplayMode = PadSlot.DisplayMode(userPreferences.getInt("notesAs", PadSlot.DisplayMode.AsNumber.id))
    def notesAs_=(value: PadSlot.DisplayMode.DisplayMode): Unit = {
        Console.println("jTrapKATEditorPreferences notesAs -> " + value)
        userPreferences.putInt("notesAs", value.id)
        publish(new NotesAsPreferencChanged)
    }

    def updateAutomatically: Boolean = userPreferences.getBoolean("updateAutomatically", true)
    def updateAutomatically_=(value: Boolean): Unit = {
        Console.println("jTrapKATEditorPreferences updateAutomatically -> " + value)
        userPreferences.putBoolean("updateAutomatically", value)
        publish(new UpdateAudomaticallyPreferencChanged)
    }
}

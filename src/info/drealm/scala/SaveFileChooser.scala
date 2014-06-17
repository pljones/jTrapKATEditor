package info.drealm.scala

import java.io.File
import swing._

object SaveFileChooser extends FileChooser {
    fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("Sysex files", "syx")
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false

    def file(dumpType: String): Option[java.io.File] = {
        title = f"Save ${dumpType} Sysex Dump"
        (showSaveDialog(null) match {
            case FileChooser.Result.Approve if (!selectedFile.exists() || okayToOverwrite(selectedFile)) => Some(selectedFile)
            case _ => None
        })
    }

    def okayToOverwrite(file: File): Boolean = Dialog.showConfirmation(null,
        f"${file.getCanonicalPath()} exists.\n\nDo you want to overwrite it?\n",
        "jTrapKATEditor",
        Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok
}

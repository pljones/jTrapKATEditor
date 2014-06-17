package info.drealm.scala

import java.io.File
import swing._

object OpenFileChooser extends FileChooser {
    fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("Sysex files", "syx")
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false

    def file: Option[java.io.File] = {
        title = "Open Sysex Dump"
        (showOpenDialog(null) match {
            case FileChooser.Result.Approve => Some(selectedFile)
            case _                          => None
        })
    }
}

package info.drealm.scala

import java.io.File
import swing._

object FileOpen extends FileChooser {
    fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("Sysex files", "syx")
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false

    def load(_load: File => Option[model.DataItem]): Option[model.DataItem] = {
        title = "Open Sysex Dump"
        (showOpenDialog(null) match {
            case FileChooser.Result.Approve => _load(selectedFile)
            case _                          => None
        })
    }
}

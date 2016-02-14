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

import java.io.File
import swing._

object OpenFileChooser extends FileChooser {
    fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("Sysex files", "syx")
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false

    def file: Option[java.io.File] = {
        title = "Open Sysex Dump"
        (showOpenDialog(tpnMain) match {
            case FileChooser.Result.Approve => Some(selectedFile)
            case _                          => None
        })
    }
}

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

import javax.swing.UIManager
import swing._
import swing.event._
import info.drealm.scala.eventX._

object jTrapKATEditor extends SimpleSwingApplication {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    UIManager.put("swing.boldMetal", false);

    //override def shutdown() = {}

    def top = frmTrapkatSysexEditor

    //TODO: This should say what is unsaved and ask for confirmation to lose it.
    def loseUnsavedChanges = {
        true
    }

    //TODO: This should live somewhere else -> frmTrapkatSysexEditor?
    def load(someDump: java.io.File): Unit = {
        Console.println("load " + someDump)
        try {
            val sysex = info.drealm.scala.model.TrapKATSysexDump.fromFile(someDump)
        } catch  {
            case ex:IllegalArgumentException => {
                Dialog.showMessage(null, f"Oh dear", "Invalid Sysex file", Dialog.Message.Error, null)
            }
        }
    }

    listenTo(frmTrapkatSysexEditor)
    reactions += {
        case mie: FileMenuEvent => {
            mie.source.name.stripPrefix("miFile") match {
                case "NewV3" if (loseUnsavedChanges) => Console.println("File NewV3")
                case "NewV4" if (loseUnsavedChanges) => Console.println("File NewV4")
                case "Open" if (loseUnsavedChanges) => {
                    val chooser = new FileChooser {
                        fileFilter = new javax.swing.filechooser.FileNameExtensionFilter("Sysex files", "syx")
                        fileSelectionMode = FileChooser.SelectionMode.FilesOnly
                        multiSelectionEnabled = false
                        title = "Open Sysex Dump"
                    }
                    chooser.showOpenDialog(null) match {
                        case FileChooser.Result.Approve => load(chooser.selectedFile)
                        case _                          => {}
                    }

                }
                case save if save.startsWith("Save") => {
                    save.stripPrefix("Save") match {
                        case saveAs if saveAs.endsWith("As") => {
                            saveAs.stripSuffix("As") match {
                                case "AllMemory"    => Console.println("FileSave AllMemory As")
                                case "GlobalMemory" => Console.println("FileSave GlobalMemory As")
                                case "CurrentKit"   => Console.println("FileSave CurrentKit As")
                                case otherwise      => {}
                            }
                        }
                        case otherwise => {
                            otherwise match {
                                case "AllMemory"    => Console.println("FileSave AllMemory")
                                case "GlobalMemory" => Console.println("FileSave GlobalMemory")
                                case "CurrentKit"   => Console.println("FileSave CurrentKit")
                                case otherwise      => {}
                            }
                        }
                    }
                }
                case "Close" if (loseUnsavedChanges) => Console.println("File Close")
                case "Exit" if (loseUnsavedChanges)  => quit
                case otherwise => {
                    Console.println("File event " + mie.source.name)
                }
            }
        }
        case mie: EditMenuEvent => {
            mie.source.name.stripPrefix("miEdit") match {
                case "Undo"     => Console.println("Edit Undo")
                case "Redo"     => Console.println("Edit Redo")
                case "CopyKit"  => Console.println("Edit CopyKit")
                case "SwapKits" => Console.println("Edit SwapKits")
                case "CopyPad"  => Console.println("Edit CopyPad")
                case "PastePad" => Console.println("Edit PastePad")
                case "SwapPads" => Console.println("Edit SwapPads")
                case otherwise => {
                    Console.println("Edit event " + mie.source.name)
                }
            }
        }
        case mie: ToolsMenuEvent => {
            mie.source.name.stripPrefix("miTools") match {
                case "OptionsDMNAsNumbers" => PadSlot.displayMode = PadSlot.DisplayMode.AsNumber
                case "OptionsDMNAsNamesC3" => PadSlot.displayMode = PadSlot.DisplayMode.AsNamesC3
                case "OptionsDMNAsNamesC4" => PadSlot.displayMode = PadSlot.DisplayMode.AsNamesC4
                case "Convert"             => Console.println("Tools Convert")
                case otherwise => {
                    Console.println("Tools event " + mie.source.name)
                }
            }
        }
        case mie: HelpMenuEvent => {
            mie.source.name.stripPrefix("miHelp") match {
                case "Contents"           => Console.println("Help Contents")
                case "CheckForUpdate"     => Console.println("Help CheckForUpdate")
                case "CheckAutomatically" => Console.println("Help CheckAutomatically")
                case "About"              => Console.println("Help About")
                case otherwise => {
                    Console.println("Help event " + mie.source.name)
                }
            }
        }
        case mne: MenuEvent => {
            mne.source.name.stripPrefix("mn") match {
                case "File"            => Console.println("File menu " + mne.action)
                case "Edit"            => Console.println("Edit menu " + mne.action)
                case "Tools"           => Console.println("Tools menu " + mne.action)
                case "ToolsOptions"    => Console.println("Tools Options menu " + mne.action)
                case "ToolsOptionsDMN" => Console.println("Tools Options DMN menu " + mne.action)
                case "Help"            => Console.println("Help menu " + mne.action)
                case otherwise => {
                    Console.println("MenuEvent" + mne.source.name + " action " + mne.action)
                }
            }
        }
        case tpe: TabChangeEvent => {
            tpe.source.content.name.stripPrefix("pn") match {
                case "KitsPads"   => Console.println("Main KitsPads")
                case "Global"     => Console.println("Main Global")
                case "PadDetails" => Console.println("KitPadsDetails PadDetails")
                case "MoreSlots"  => Console.println("KitPadsDetails MoreSlots")
                case "KitDetails" => Console.println("KitPadsDetails KitDetails")
                case otherwise => {
                    Console.println("TabChangeEvent " + otherwise)
                }
            }
        }
        case e: KitChanged => {
            Console.println("Kit change" + (if (e.oldKit >= 0) " from " + e.oldKit else "") + " to " + e.newKit)
        }
        case e: PadChanged => {
            Console.println("Pad change" + (if (e.oldPad >= 0) " from " + e.oldPad else "") + " to " + e.newPad)
        }
        case cbxE: SelectionChanged => {
            Console.println("SelectionChanged " + cbxE.source.name)
        }
        case cbxE: CbxEditorFocused => {
            Console.println("CbxEditorFocused " + cbxE.source.name)
        }
        case cpnE: ValueChanged => {
            Console.println("ValueChanged " + cpnE.source.name)
        }
        case cbxE: ButtonClicked => {
            Console.println("ButtonClicked " + cbxE.source.name)
        }
    }
}

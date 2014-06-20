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
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs }

object frmTrapkatSysexEditor extends MainFrame {

    peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
    override def closeOperation = {
        prefs.currentWorkingDirectory = if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getParentFile() else jTrapKATEditor.currentFile.getCanonicalFile()
        jTrapKATEditor.exitClose
    }

    iconImage = toolkit.getImage("resources/tk_wild2-sq.png")
    resizable = false
    title = "TrapKAT SysEx Editor"
    bounds = new Rectangle(100, 100, 880, 516)

    menuBar = jTrapKATEditorMenuBar

    private[this] object tpnMain extends TabbedPane {

        name = "tpnMain"

        pages += new TabbedPane.Page("Kits & Pads", pnKitsPads) { name = "tpKitsPads" }
        pages += new TabbedPane.Page("Global", pnGlobal) { name = "tpGlobal" }

        listenTo(selection)
        listenTo(pnKitsPads)
        listenTo(pnGlobal)

        reactions += {
            case e: TabChangeEvent => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: SelectionChanged if (e.source.isInstanceOf[TabbedPane]) => {
                val tpnE = e.source.asInstanceOf[TabbedPane]
                deafTo(this)
                publish(new TabChangeEvent(tpnE.selection.page))
                listenTo(this)
            }
            case e: KitChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: PadChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: SelectionChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: CbxEditorFocused => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: ValueChanged => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
            case e: ButtonClicked => {
                deafTo(this)
                publish(e)
                listenTo(this)
            }
        }
    }

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {

        contents += (tpnMain, "cell 0 0,grow")

        contents += (new Label("MIDI OX SysEx Transmit: 512 bytes, 8 buffers, 160ms between buffers, 320ms after F7"), "cell 0 1,alignx center")

    }

    layout.Focus.set(tpnMain, "cbxPad1")

    centerOnScreen

    listenTo(menuBar)
    listenTo(tpnMain)
    //listenTo(this)
    listenTo(jTrapKATEditor)

    reactions += {
        case wo: WindowOpened                     => windowOpened
        case amc: jTrapKATEditor.AllMemoryChanged => jTrapKATEditor_AllMemoryChanged
        case mie: FileMenuEvent => {
            mie.source.name.stripPrefix("miFile") match {
                case "NewV3" => jTrapKATEditor.reinitV3
                case "NewV4" => jTrapKATEditor.reinitV4
                case "Open" => try {
                    OpenFileChooser.selectedFile = new java.io.File((if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getParent() else jTrapKATEditor.currentFile.getCanonicalPath()) + "/.")
                    OpenFileChooser.file match {
                        case Some(file) => jTrapKATEditor.openFile(file)
                        case None       => {}
                    }
                }
                catch {
                    case ex: IllegalArgumentException => {
                        Dialog.showMessage(null, ex.getLocalizedMessage(), "Invalid Sysex file", Dialog.Message.Error, null)
                    }
                }
                case save if save.startsWith("Save") => {
                    save.stripPrefix("Save") match {
                        case saveAs if saveAs.endsWith("As") => {
                            val dumpType = saveAs.stripSuffix("As") match {
                                case "AllMemory"    => model.DumpType.AllMemory
                                case "GlobalMemory" => model.DumpType.Global
                                case "CurrentKit"   => model.DumpType.Kit
                                case _              => model.DumpType.NotSet
                            }
                            SaveFileChooser.selectedFile = if (dumpType == jTrapKATEditor.currentType)
                                jTrapKATEditor.currentFile
                            else
                                new java.io.File(
                                    (if (dumpType != model.DumpType.Kit)
                                        dumpType.toString
                                    else if (jTrapKATEditor.currentKit != null)
                                        jTrapKATEditor.currentKit.kitName
                                    else "CurrentKit" // should never happen
                                    ) + ".syx")
                            SaveFileChooser.file(dumpType.toString) match {
                                case Some(file) => jTrapKATEditor.saveFileAs(dumpType, file)
                                case _          => {}
                            }
                        }
                        case otherwise => jTrapKATEditor.saveFileAs(jTrapKATEditor.currentType, jTrapKATEditor.currentFile)
                    }
                }
                case "Close" => jTrapKATEditor.exitClose
                case "Exit"  => jTrapKATEditor.exitClose
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
                case "OptionsDMNAsNumbers" => notesAs(PadSlot.DisplayMode.AsNumber)
                case "OptionsDMNAsNamesC3" => notesAs(PadSlot.DisplayMode.AsNamesC3)
                case "OptionsDMNAsNamesC4" => notesAs(PadSlot.DisplayMode.AsNamesC4)
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

    def okayToSplat(dataItem: model.DataItem, to: String): Boolean = {
        Console.println(f"${to} - changed? ${dataItem.changed}")
        !dataItem.changed || (Dialog.showConfirmation(null,
            f"You have unsaved changes to ${to} that will be lost.\n\nDo you want to continue?\n",
            "jTrapKATEditor",
            Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok)
    }

    def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(null,
        f"You are editing a ${to} All Memory dump.\n\nDo you want to convert this ${from} ${thing} to ${to}?\n",
        f"Import ${thing}",
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes

    def okayToRenumber(into: Int, intoName: String, from: Int, fromName: String): Boolean = Dialog.showConfirmation(null,
        f"You are editing kit #${into} (${intoName}).\nThe kit being imported was #${from} (${fromName}).\n\nDo you want to overwrite the current kit?\n",
        f"Import Kit",
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes

    private[this] def windowOpened = {
        if (prefs.updateAutomatically) {
            // call the update checker
        }
        PadSlot.displayMode = prefs.notesAs
    }

    private[this] def notesAs(displayMode: PadSlot.DisplayMode.DisplayMode) = {
        prefs.notesAs = displayMode
        PadSlot.displayMode = prefs.notesAs
    }

    private[this] def jTrapKATEditor_AllMemoryChanged = {
    }
}

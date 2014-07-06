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

import java.awt.event.{ InputEvent, KeyEvent }
import javax.swing.KeyStroke
import swing._
import swing.event._
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs, Localization => L }

object jTrapKATEditorMenuBar extends MenuBar {
    class RichMenu(_name: String) extends Menu(L.G(s"mn${_name}")) {

        name = s"mn${_name}"
        L.G(s"mne${_name}") match {
            case x if x == s"<<mne${_name}>>" => {}
            case mne                          => mnemonic = scala.swing.event.Key(mne.charAt(0))
        }

        def add(mi: RichMenuItem) = {
            listenTo(mi)
            contents += mi
        }

        //reactions += {
        //    case e: eventX.MenuCancelled  => Console.println(s"RichMenu traceMenu ${e.source.name} Canceled")
        //    case e: eventX.MenuDeselected => Console.println(s"RichMenu traceMenu ${e.source.name} Deselected")
        //    case e: eventX.MenuSelected   => Console.println(s"RichMenu traceMenu ${e.source.name} Selected")
        //}

        private[this] val menu = this
        peer.addMenuListener(new javax.swing.event.MenuListener {
            def menuCanceled(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuCancelled(menu)) // not my choice of spelling on the interface...
            def menuDeselected(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuDeselected(menu))
            def menuSelected(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuSelected(menu))
        })
    }

    object RichMenuItem {
        def traceMenuItem(mi: RichMenuItem): Unit = Console.println(s"RichMenuItem traceMenuItem ${mi.name} clicked")
    }
    class RichMenuItem(protected val _name: String, miClick: RichMenuItem => Unit = null) extends MenuItem(L.G(s"mi${_name}")) {
        name = s"mi${_name}"

        // Must define any accelerator before mnemonic...
        L.G(s"acc${_name}") match {
            case x if x == s"<<acc${_name}>>" => {}
            case acc => action = new Action(L.G(name)) {
                accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(acc.charAt(0)), InputEvent.CTRL_MASK))
                override def apply = {}
            }
        }
        L.G(s"mne${_name}") match {
            case x if x == s"<<mne${_name}>>" => {}
            case mne                          => mnemonic = scala.swing.event.Key(mne.charAt(0))
        }

        reactions += {
            case e: ButtonClicked => if (miClick != null) miClick(e.source.asInstanceOf[RichMenuItem])
        }

    }

    object mnFile extends RichMenu("File") {
        add(new RichMenuItem("FileNewV3", x => jTrapKATEditor.reinitV3))
        add(new RichMenuItem("FileNewV4", x => jTrapKATEditor.reinitV4))
        add(new RichMenuItem("FileOpen", x => try {
            OpenFileChooser.selectedFile = if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getParentFile() else jTrapKATEditor.currentFile
            OpenFileChooser.file match {
                case Some(file) => jTrapKATEditor.openFile(file)
                case None       => {}
            }
        }
        catch {
            case ex: IllegalArgumentException => {
                Dialog.showMessage(null, ex.getLocalizedMessage(), L.G("InvalidSysexFile"), Dialog.Message.Error, null)
            }
        }
        ))

        contents += new Separator()

        object SaveMenuItem {
            var currentItem: SaveMenuItem = null
        }
        class SaveMenuItem(_name: String) extends RichMenuItem(s"FileSave${_name}", x => jTrapKATEditor.saveFileAs(jTrapKATEditor.currentType, jTrapKATEditor.currentFile)) {
            protected def saveAction: Action = {
                if (SaveMenuItem.currentItem != null) {
                    SaveMenuItem.currentItem.action = Action.NoAction
                    SaveMenuItem.currentItem.text = L.G(s"mi${SaveMenuItem.currentItem._name}")
                }
                SaveMenuItem.currentItem = this
                new Action(L.G(s"miFileSave${_name}")) {
                    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(L.G("accFileSave").charAt(0)), InputEvent.CTRL_MASK))
                    override def apply = {}
                }
            }
        }
        object SaveAsMenuItem {
            protected def saveAs(dumpType: model.DumpType.DumpType): Unit = {
                SaveFileChooser.selectedFile = if (dumpType == jTrapKATEditor.currentType)
                    jTrapKATEditor.currentFile
                else
                    new java.io.File(
                        (if (dumpType != model.DumpType.Kit)
                            dumpType.toString
                        else if (jTrapKATEditor.currentKit != null)
                            jTrapKATEditor.currentKit.kitName.trim()
                        else "CurrentKit" // should never happen
                        ) + ".syx")
                SaveFileChooser.file(dumpType.toString) match {
                    case Some(file) => jTrapKATEditor.saveFileAs(dumpType, file)
                    case _          => {}
                }
            }
        }
        class SaveAsMenuItem(_name: String, dumpType: model.DumpType.DumpType) extends RichMenuItem(s"FileSave${_name}As", x => SaveAsMenuItem.saveAs(dumpType))

        add(new SaveMenuItem("AllMemory") {
            listenTo(jTrapKATEditor)
            listenTo(jTrapKATEditor.currentAllMemory)
            reactions += {
                case e: eventX.AllMemoryChanged if jTrapKATEditor.currentType == model.DumpType.AllMemory => {
                    action = saveAction
                }
                case e: eventX.DataItemChanged if e.dataItem == jTrapKATEditor.currentAllMemory && jTrapKATEditor.currentType == model.DumpType.AllMemory => {
                    enabled = jTrapKATEditor.currentFile.isFile() && jTrapKATEditor.currentAllMemory.changed
                }
            }
            action = saveAction
            enabled = false
        })
        add(new SaveAsMenuItem("AllMemory", model.DumpType.AllMemory))
        add(new SaveMenuItem("GlobalMemory") {
            listenTo(jTrapKATEditor)
            listenTo(jTrapKATEditor.currentAllMemory.global)
            reactions += {
                case e: eventX.GlobalChanged if jTrapKATEditor.currentType == model.DumpType.Global => {
                    action = saveAction
                }
                case e: eventX.DataItemChanged if e.dataItem == jTrapKATEditor.currentAllMemory.global && jTrapKATEditor.currentType == model.DumpType.Global => {
                    enabled = jTrapKATEditor.currentFile.isFile() && jTrapKATEditor.currentAllMemory.global.changed
                }
            }
            enabled = false
        })
        add(new SaveAsMenuItem("GlobalMemory", model.DumpType.Global))
        add(new SaveMenuItem("CurrentKit") {
            listenTo(jTrapKATEditor)
            reactions += {
                case kc: eventX.KitChanged if jTrapKATEditor.currentType == model.DumpType.Kit => {
                    action = saveAction
                }
                case e: eventX.DataItemChanged if e.dataItem == jTrapKATEditor.currentKit && jTrapKATEditor.currentType == model.DumpType.Kit => {
                    enabled = jTrapKATEditor.currentFile.isFile() && jTrapKATEditor.currentKit.changed
                }
            }
            enabled = false
        })
        add(new SaveAsMenuItem("CurrentKit", model.DumpType.Kit) {
            listenTo(jTrapKATEditor)
            listenTo(pnKitsPads)
            reactions += {
                case e: eventX.KitChanged => enabled = e.newKit != -1
            }
            enabled = false
        })

        contents += new Separator()

        add(new RichMenuItem("FileClose", x => jTrapKATEditor.exitClose))

        contents += new Separator()

        add(new RichMenuItem("FileExit", x => jTrapKATEditor.exitClose))

    }

    object mnEdit extends RichMenu("Edit") {

        add(new RichMenuItem("EditUndo", x => Console.println("Edit Undo")) { visible = false })
        add(new RichMenuItem("EditRedo", x => Console.println("Edit Redo")) { visible = false })

        contents += new Separator() { visible = false }

        add(new RichMenuItem("EditCopyKit", x => Console.println("Edit CopyKit")))
        add(new RichMenuItem("EditSwapKits", x => Console.println("Edit SwapKits")))

        contents += new Separator()

        add(new RichMenuItem("EditCopyPad", x => Console.println("Edit CopyPad")))
        add(new RichMenuItem("EditPastePad", x => Console.println("Edit PastePad")))
        add(new RichMenuItem("EditSwapPads", x => Console.println("Edit SwapPads")))

    }

    object mnTools extends RichMenu("Tools") {

        contents += new RichMenu("ToolsOptions") {

            contents += new RichMenu("ToolsOptionsDMN") {

                import PadSlot.DisplayMode._

                private[this] val bgTODMN = new ButtonGroup();

                class DisplayModeMenuItem(displayMode: DisplayMode, val nameSuffix: String)
                    extends RadioMenuItem(L.G("miToolsOptionsDMN" + nameSuffix)) {
                    name = "miToolsOptionsDMN" + nameSuffix
                    listenTo(PadSlot)
                    bgTODMN.buttons.add(this)
                    reactions += {
                        case e: eventX.DisplayNotesAs if e.newMode == displayMode => selected = true
                        case ButtonClicked(_)                                     => PadSlot.displayMode = displayMode
                    }
                }

                contents += new DisplayModeMenuItem(AsNumber, "AsNumbers")
                contents += new DisplayModeMenuItem(AsNamesC3, "AsNamesC3")
                contents += new DisplayModeMenuItem(AsNamesC4, "AsNamesC4")
            }
        }

        add(new RichMenuItem("ToolsConvert", x => {
            def f(from: String, to: String, converter: => Unit): Unit = Dialog.showConfirmation(null,
                L.G("ConvertVersions", L.G(from), L.G(to)), L.G("ConvertCaption"),
                Dialog.Options.YesNo, Dialog.Message.Question, null) match {
                    case Dialog.Result.Yes => converter
                    case _                 => {}
                }
            jTrapKATEditor.currentAllMemory match {
                case v3: model.AllMemoryV3 => f("V3", "V4", jTrapKATEditor.convertToV4)
                case _                     => f("V4", "V3", jTrapKATEditor.convertToV3)
            }
        }))

    }

    object mnHelp extends RichMenu("Help") {

        add(new RichMenuItem("HelpContents", x => util.browse("http://www.drealm.info/kat/TrapKATEditor/")) {
            action = new Action(L.G(name)) {
                accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
                override def apply = {}
            }
        })

        contents += new Separator()

        add(new RichMenuItem("HelpCheckForUpdate", x => updateTool.Checker.getUpdate()))

        contents += new CheckMenuItem(L.G("miHelpCheckAutomatically")) {
            name = "miHelpCheckAutomatically"
            listenTo(updateTool.Checker)
            reactions += {
                case e: updateTool.Checker.AutoUpdateModeChanged => selected = e.newMode == updateTool.Checker.AutoUpdateMode.Automatically
                case ButtonClicked(_)                            => prefs.updateAutomatically = if (selected) updateTool.Checker.AutoUpdateMode.Automatically else updateTool.Checker.AutoUpdateMode.Off
            }
        }

        contents += new Separator()

        add(new RichMenuItem("HelpAbout", x => Dialog.showMessage(null,
            L.G("helpAbout", updateTool.Checker.currentVersion, L.G("UT" + prefs.updateAutomatically.toString()), f"${prefs.lastUpdateTS}%TF"),
            L.G("helpAboutCaption"))
        ))
    }

    contents += mnFile
    contents += mnEdit
    contents += mnTools
    contents += mnHelp
}

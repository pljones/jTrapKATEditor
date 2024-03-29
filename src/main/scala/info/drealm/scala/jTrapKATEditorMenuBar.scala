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
import info.drealm.scala.{ Localization => L }
import info.drealm.scala.prefs.{ Preferences => P }

object jTrapKATEditorMenuBar extends MenuBar {

    class RichMenu(_name: String) extends Menu(L.G(s"mn${_name}")) {
        name = s"mn${_name}"
        L.G(s"mne${_name}") match {
            case x if x == s"<<mne${_name}>>" => {}
            case mne                          => mnemonic = scala.swing.event.Key(mne.charAt(0))
        }

        def add(mi: MenuItem) = {
            listenTo(mi)
            contents += mi
        }

        // Yet more scala.swing brokenness: these should just be there, really
        private[this] val menu = this
        peer.addMenuListener(new javax.swing.event.MenuListener {
            def menuCanceled(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuCanceled(menu)) // not my choice of spelling - to match PopupMenuCanceled
            def menuDeselected(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuWillBecomeInvisible(menu))
            def menuSelected(e: javax.swing.event.MenuEvent) = publish(new eventX.MenuWillBecomeVisible(menu))
        })
    }

    trait EnrichenMenuItem extends MenuItem {
        protected val _name: String
        protected val miClick: MenuItem => Unit = null
        name = s"mi${_name}"

        // Must define any accelerator before mnemonic...
        L.G(s"acc${_name}") match {
            case x if x == s"<<acc${_name}>>" => {}
            case acc => action = new Action(L.G(name)) {
                accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(acc.charAt(0)), InputEvent.CTRL_DOWN_MASK))
                override def apply = {}
            }
        }
        L.G(s"mne${_name}") match {
            case x if x == s"<<mne${_name}>>" => {}
            case mne                          => mnemonic = scala.swing.event.Key(mne.charAt(0))
        }

        reactions += {
            case e: ButtonClicked => if (miClick != null) miClick(e.source.asInstanceOf[MenuItem])
        }
    }
    class RichMenuItem(override protected val _name: String, override protected val miClick: MenuItem => Unit = null)
        extends MenuItem(L.G(s"mi${_name}")) with EnrichenMenuItem

    class RichCheckMenuItem(override protected val _name: String, override protected val miClick: MenuItem => Unit = null)
        extends CheckMenuItem(L.G(s"mi${_name}")) with EnrichenMenuItem

    object mnFile extends RichMenu("File") {
        add(new RichMenuItem("FileNewV3", x => jTrapKATEditor.reinitV3()))
        add(new RichMenuItem("FileNewV4", x => jTrapKATEditor.reinitV4()))
        add(new RichMenuItem("FileNewV5", x => jTrapKATEditor.reinitV5()))
        add(new RichMenuItem("FileOpen", x => try {
            OpenFileChooser.selectedFile = if (jTrapKATEditor.currentFile.isDirectory()) new java.io.File(s"${jTrapKATEditor.currentFile.getCanonicalPath()}/.") else jTrapKATEditor.currentFile
            OpenFileChooser.file match {
                case Some(file) => jTrapKATEditor.openFile(file)
                case None       => {}
            }
        } catch {
            case ex: IllegalArgumentException => {
                Dialog.showMessage(tpnMain, ex.getLocalizedMessage(), L.G("InvalidSysexFile"), Dialog.Message.Error, null)
            }
        }))

        contents += new Separator()

        private[this] object SaveMenuItem {
            var currentItem: SaveMenuItem = null
            def saveAction(mi: SaveMenuItem): Action = {
                if (currentItem != null) {
                    currentItem.action = Action.NoAction
                    currentItem.text = L.G(s"mi${currentItem._name}")
                }
                currentItem = mi
                new Action(L.G(s"mi${mi._name}")) {
                    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(L.G("accFileSave").charAt(0)), InputEvent.CTRL_DOWN_MASK))
                    override def apply = {}
                }
            }
        }
        class SaveMenuItem(_name: String, dumpType: model.DumpType.DumpType) extends RichMenuItem(s"FileSave${_name}", x => jTrapKATEditor.saveFileAs(jTrapKATEditor.currentType, jTrapKATEditor.currentFile))

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

        private[this] val SaveAllMemoryMenuItem = new SaveMenuItem("AllMemory", model.DumpType.AllMemory) { enabled = false }
        add(SaveAllMemoryMenuItem)
        add(new SaveAsMenuItem("AllMemory", model.DumpType.AllMemory))

        private[this] val SaveGlobalMemoryMenuItem = new SaveMenuItem("GlobalMemory", model.DumpType.Global) { enabled = false }
        add(SaveGlobalMemoryMenuItem)
        add(new SaveAsMenuItem("GlobalMemory", model.DumpType.Global))

        private[this] val SaveCurrentKitMenuItem = new SaveMenuItem("CurrentKit", model.DumpType.Kit) { enabled = false }
        add(SaveCurrentKitMenuItem)
        private[this] val SaveCurrentKitAsMenuItem = new SaveAsMenuItem("CurrentKit", model.DumpType.Kit) { enabled = false }
        add(SaveCurrentKitAsMenuItem)

        contents += new Separator()

        add(new RichMenuItem("FileClose", x => jTrapKATEditor.exitClose))

        contents += new Separator()

        add(new RichMenuItem("FileExit", x => jTrapKATEditor.exitClose))

        reactions += {
            case e: eventX.MenuWillBecomeVisible => {
                jTrapKATEditor.currentType match {
                    case model.DumpType.Global => SaveGlobalMemoryMenuItem.action = SaveMenuItem.saveAction(SaveGlobalMemoryMenuItem)
                    case model.DumpType.Kit    => SaveCurrentKitMenuItem.action = SaveMenuItem.saveAction(SaveCurrentKitMenuItem)
                    case _                     => SaveAllMemoryMenuItem.action = SaveMenuItem.saveAction(SaveAllMemoryMenuItem)
                }

                val isFile = jTrapKATEditor.currentFile.isFile()
                SaveAllMemoryMenuItem.enabled = isFile && jTrapKATEditor.currentType == model.DumpType.AllMemory && jTrapKATEditor.currentAllMemory.changed
                SaveGlobalMemoryMenuItem.enabled = isFile && jTrapKATEditor.currentType == model.DumpType.Global && jTrapKATEditor.currentAllMemory.global.changed
                SaveCurrentKitMenuItem.enabled = isFile && jTrapKATEditor.currentType == model.DumpType.Kit && jTrapKATEditor.currentKit.changed
                SaveCurrentKitAsMenuItem.enabled = jTrapKATEditor.currentKit != null
            }
        }
    }

    object mnEdit extends RichMenu("Edit") {

        val miEditUndo = new RichMenuItem("EditUndo", x => EditHistory.undoAction())
        add(miEditUndo)
        val miEditRedo = new RichMenuItem("EditRedo", x => EditHistory.redoAction())
        add(miEditRedo)

        contents += new Separator()

        val miEditCopyPad = new RichMenuItem("EditCopyPad", x => Clipboard.copyPad(this))
        add(miEditCopyPad)

        val miEditPastePad = new RichMenuItem("EditPastePad", x => Clipboard.pastePad(this))
        add(miEditPastePad)

        val miEditSwapPads = new RichCheckMenuItem("EditSwapPads", x => Clipboard.swapPads(this))
        add(miEditSwapPads)

        contents += new Separator()

        val miEditCopyKit = new RichMenuItem("EditCopyKit", x => Clipboard.copyKit(this))
        add(miEditCopyKit)

        val miEditPasteKit = new RichMenuItem("EditPasteKit", x => Clipboard.pasteKit(this))
        add(miEditPasteKit)

        val miEditSwapKits = new RichCheckMenuItem("EditSwapKits", x => Clipboard.swapKits(this))
        add(miEditSwapKits)

        reactions += {
            case e: eventX.MenuWillBecomeVisible => {
                miEditUndo.enabled = EditHistory.canUndo
                miEditUndo.text = EditHistory.undoActionName match { case Some(actionName) => L.G("miEditUndoAction", L.G(actionName)); case _ => L.G("miEditUndo") }
                miEditRedo.enabled = EditHistory.canRedo
                miEditRedo.text = EditHistory.redoActionName match { case Some(actionName) => L.G("miEditRedoAction", L.G(actionName)); case _ => L.G("miEditRedo") }
                val clipboardType = Clipboard.clipboardType
                miEditPastePad.enabled = clipboardType == Clipboard.ClipboardType.Pad
                miEditSwapPads.selected = clipboardType == Clipboard.ClipboardType.PadSwap
                miEditPasteKit.enabled = clipboardType == Clipboard.ClipboardType.Kit
                miEditSwapKits.selected = clipboardType == Clipboard.ClipboardType.KitSwap
            }
        }
    }

    object mnTools extends RichMenu("Tools") {

        contents += new RichMenu("ToolsOptions") {

            contents += new RichMenu("ToolsOptionsDMN") {

                import DisplayMode._

                private[this] val bgTODMN = new ButtonGroup();

                class DisplayModeMenuItem(displayMode: DisplayMode, val nameSuffix: String)
                    extends RadioMenuItem(L.G("miToolsOptionsDMN" + nameSuffix)) {
                    name = "miToolsOptionsDMN" + nameSuffix
                    bgTODMN.buttons.add(this)
                    listenTo(prefs.Preferences)
                    reactions += {
                        case e: P.NotesAsPreferencChanged if P.notesAs == displayMode => selected = true
                        case ButtonClicked(_)                                         => P.notesAs = displayMode
                    }
                }

                contents += new DisplayModeMenuItem(AsNumber, "AsNumbers") { selected = P.notesAs == AsNumber }
                contents += new DisplayModeMenuItem(AsNamesC3, "AsNamesC3") { selected = P.notesAs == AsNamesC3 }
                contents += new DisplayModeMenuItem(AsNamesC4, "AsNamesC4") { selected = P.notesAs == AsNamesC4 }
            }
        }

        contents += new RichMenu("ToolsConvert") with AllMemorySelectionReactor {
            protected def _isUIChange = true
            protected def _uiReaction = {
                toV3.enabled = jTrapKATEditor.doV3V4V5(false, true, true)
                toV4.enabled = jTrapKATEditor.doV3V4V5(true, false, true)
                toV5.enabled = jTrapKATEditor.doV3V4V5(true, true, false)
            }

            def f(from: String, to: String, converter: => Unit): Unit = Dialog.showConfirmation(
                tpnMain,
                L.G("ConvertVersions", L.G(from), L.G(to)), L.G("ConvertCaption"),
                Dialog.Options.YesNo, Dialog.Message.Question, null) match {
                    case Dialog.Result.Yes => converter
                    case _                 => {}
                }

            val toV3 = new RichMenuItem("ToolsConvertToV3", x => jTrapKATEditor.doV3V4V5({}, f("V4", "V3", jTrapKATEditor.convertToV3()), f("V5", "V3", jTrapKATEditor.convertToV3())))
            val toV4 = new RichMenuItem("ToolsConvertToV4", x => jTrapKATEditor.doV3V4V5(f("V3", "V4", jTrapKATEditor.convertToV4()), {}, f("V5", "V4", jTrapKATEditor.convertToV4())))
            val toV5 = new RichMenuItem("ToolsConvertToV5", x => jTrapKATEditor.doV3V4V5(f("V3", "V5", jTrapKATEditor.convertToV5()), f("V4", "V5", jTrapKATEditor.convertToV5()), {}))

            contents += toV3
            contents += toV4
            contents += toV5

            setDisplay()
        }

    }

    object mnHelp extends RichMenu("Help") {

        add(new RichMenuItem("HelpContents", x => util.browse("http://pljones.github.io/jTrapKATEditor/help.html")) {
            action = new Action(L.G(name)) {
                accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
                override def apply = {}
            }
        })

        contents += new Separator()

        add(new RichMenuItem("HelpCheckForUpdate", x => if (!updateTool.Checker.getUpdate().getOrElse(true)) // true as error already displayed
            Dialog.showMessage(tpnMain, L.G("UHNoUpdate"), L.G("UCAvailableCaption", L.G("ApplicationProductName")), Dialog.Message.Info)))

        contents += new CheckMenuItem(L.G("miHelpCheckAutomatically")) {
            import updateTool.Checker._
            name = "miHelpCheckAutomatically"
            selected = autoUpdateMode == AutoUpdateMode.Automatically

            listenTo(updateTool.Checker)
            reactions += {
                case e: eventX.AutoUpdateModeChanged => {
                    selected = e.newMode == AutoUpdateMode.Automatically
                    dailyCheck()
                }
                case ButtonClicked(_) if (selected) => autoUpdateMode = AutoUpdateMode.Automatically
                case ButtonClicked(_)               => autoUpdateMode = AutoUpdateMode.Off
            }
        }

        contents += new Separator()

        add(new RichMenuItem("HelpAbout", x => Dialog.showMessage(
            tpnMain,
            L.G("helpAbout", updateTool.Checker.currentVersion, L.G("UT" + P.updateAutomatically.toString()), f"${P.lastUpdateTS}%TF"),
            L.G("helpAboutCaption"))))

        contents += new Separator()

        add(new RichMenuItem("HelpWarranty", x => Dialog.showMessage(
            tpnMain,
            L.G("helpWarranty"),
            L.G("helpWarrantyCaption"))))

        add(new RichMenuItem("HelpLicence", x => Dialog.showConfirmation(
            tpnMain,
            L.G("helpLicence"), L.G("helpLicenceCaption")) match {
                case Dialog.Result.Yes => util.browse("http://www.gnu.org/licenses/gpl.html")
                case Dialog.Result.No  => {}
            }))
    }

    contents += mnFile
    contents += mnEdit
    contents += mnTools
    contents += mnHelp
}

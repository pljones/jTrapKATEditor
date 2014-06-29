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

object jTrapKATEditorMenuBar extends MenuBar {
    def menuEventCallback(source: Menu, action: eventX.MenuEvent.Action) = {
        deafTo(this)
        publish(new eventX.MenuEvent(source, action))
        listenTo(this)
    }
    class RichMenu(_name: String) extends Menu(L.G(s"mn${_name}")) {
        name = s"mn${_name}"
        L.G(s"mne${_name}") match {
            case x if x == s"<<mne${_name}>>" => {}
            case mne                          => mnemonic = scala.swing.event.Key(mne.charAt(0))
        }

        reactions += {
            case e: eventX.MenuCanceled   => menuEventCallback(e.source, eventX.MenuEvent.Canceled)
            case e: eventX.MenuDeselected => menuEventCallback(e.source, eventX.MenuEvent.Deselected)
            case e: eventX.MenuSelected   => menuEventCallback(e.source, eventX.MenuEvent.Selected)
        }

        private[this] final def MenuListener(f1: javax.swing.event.MenuEvent => Unit, f2: javax.swing.event.MenuEvent => Unit, f3: javax.swing.event.MenuEvent => Unit) = new javax.swing.event.MenuListener {
            def menuCanceled(e: javax.swing.event.MenuEvent) { f1(e) }
            def menuDeselected(e: javax.swing.event.MenuEvent) { f2(e) }
            def menuSelected(e: javax.swing.event.MenuEvent) { f3(e) }
        }
        peer.addMenuListener(MenuListener(
            { e => publish(new eventX.MenuCanceled(this)) },
            { e => publish(new eventX.MenuDeselected(this)) },
            { e => publish(new eventX.MenuSelected(this)) }
        ))
    }

    def menuItemEventCallback(source: MenuItem) = {
        deafTo(this)
        listenTo(this)
        source.name match {
            case miName if miName.startsWith("miFile")  => publish(new eventX.FileMenuEvent(source))
            case miName if miName.startsWith("miEdit")  => publish(new eventX.EditMenuEvent(source))
            case miName if miName.startsWith("miTools") => publish(new eventX.ToolsMenuEvent(source))
            case miName if miName.startsWith("miHelp")  => publish(new eventX.HelpMenuEvent(source))
            case otherwise                              => {}
        }
    }
    class RichMenuItem(_name: String) extends MenuItem(L.G(s"mi${_name}")) {
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

        reactions += { case e: ButtonClicked => menuItemEventCallback(e.source.asInstanceOf[MenuItem]) }

    }

    object mnFile extends RichMenu("File") {

        contents += new RichMenuItem("FileNewV3")
        contents += new RichMenuItem("FileNewV4")
        contents += new RichMenuItem("FileOpen")

        contents += new Separator()

        contents += new RichMenuItem("FileSaveAllMemory")
        contents += new RichMenuItem("FileSaveAllMemoryAs")
        contents += new RichMenuItem("FileSaveGlobalMemory")
        contents += new RichMenuItem("FileSaveGlobalMemoryAs")
        contents += new RichMenuItem("FileSaveCurrentKit")
        contents += new RichMenuItem("FileSaveCurrentKitAs")

        contents += new Separator()

        contents += new RichMenuItem("FileClose")

        contents += new Separator()

        contents += new RichMenuItem("FileExit")

    }

    object mnEdit extends RichMenu("Edit") {

        contents += new RichMenuItem("EditUndo") { visible = false }
        contents += new RichMenuItem("EditRedo") { visible = false }

        contents += new Separator() { visible = false }

        contents += new RichMenuItem("EditCopyKit")
        contents += new RichMenuItem("EditSwapKits")

        contents += new Separator()

        contents += new RichMenuItem("EditCopyPad")
        contents += new RichMenuItem("EditPastePad")
        contents += new RichMenuItem("EditSwapPads")

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
                        case ButtonClicked(_)                                     => menuItemEventCallback(this)
                    }
                }

                contents += new DisplayModeMenuItem(AsNumber, "AsNumbers")
                contents += new DisplayModeMenuItem(AsNamesC3, "AsNamesC3")
                contents += new DisplayModeMenuItem(AsNamesC4, "AsNamesC4")
            }
        }

        contents += new RichMenuItem("ToolsConvert")

    }

    object mnHelp extends RichMenu("Help") {

        contents += new RichMenuItem("HelpContents") {
            action = new Action(L.G(name)) {
                accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))
                override def apply = {}
            }
        }

        contents += new Separator()

        contents += new RichMenuItem("HelpCheckForUpdate")

        contents += new CheckMenuItem(L.G("miHelpCheckAutomatically")) {
            name = "miHelpCheckAutomatically"
            listenTo(updateTool.Checker)
            reactions += {
                case e: updateTool.Checker.AutoUpdateModeChanged => selected = e.newMode == updateTool.Checker.AutoUpdateMode.Automatically
                case ButtonClicked(_)                            => menuItemEventCallback(this)
            }
        }

        contents += new Separator()

        contents += new RichMenuItem("HelpAbout")

    }

    contents += mnFile
    contents += mnEdit
    contents += mnTools
    contents += mnHelp
}

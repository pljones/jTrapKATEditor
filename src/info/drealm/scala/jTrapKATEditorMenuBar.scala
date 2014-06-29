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
    class RichMenu(override val name: String) extends Menu(L.G(name)) {

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
    class RichMenuItem(override val name: String,
                       private val _accelerator: Option[KeyStroke],
                       private val _mnemonic: Option[event.Key.Value])
        extends MenuItem(L.G(name)) {

        def this(name: String) = this(name, None, None)
        def this(name: String, mnemonic: event.Key.Value) = this(name, None, Some(mnemonic))
        def this(name: String, accelerator: KeyStroke) = this(name, Some(accelerator), None)
        def this(name: String, mnemonic: event.Key.Value, accelerator: KeyStroke) = this(name, Some(accelerator), Some(mnemonic))
        def this(name: String, accelerator: KeyStroke, mnemonic: event.Key.Value) = this(name, Some(accelerator), Some(mnemonic))

        reactions += { case e: ButtonClicked => menuItemEventCallback(e.source.asInstanceOf[MenuItem]) }

        _accelerator match {
            case Some(a) => action = new Action(L.G(name)) {
                accelerator = Some(a)
                override def apply = {}
            }
            case None => {}
        }

        _mnemonic match {
            case Some(m) => mnemonic = m
            case None    => {}
        }
    }

    object mnFile extends RichMenu("mnFile") {
        mnemonic = Key.F

        contents += new RichMenuItem("miFileNewV3", Key.Key3, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("miFileNewV4", Key.Key4, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("miFileOpen", Key.O, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK))

        contents += new Separator()

        contents += new RichMenuItem("miFileSaveAllMemory", Key.S, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("miFileSaveAllMemoryAs")
        contents += new RichMenuItem("miFileSaveGlobalMemory")
        contents += new RichMenuItem("miFileSaveGlobalMemoryAs")
        contents += new RichMenuItem("miFileSaveCurrentKit")
        contents += new RichMenuItem("miFileSaveCurrentKitAs")

        contents += new Separator()

        contents += new RichMenuItem("miFileClose", Key.C, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK))

        contents += new Separator()

        contents += new RichMenuItem("miFileExit", Key.X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK))
    }

    object mnEdit extends RichMenu("mnEdit") {
        mnemonic = Key.E

        contents += new RichMenuItem("miEditUndo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)) { visible = false }
        contents += new RichMenuItem("miEditRedo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)) { visible = false }

        contents += new Separator() { visible = false }

        contents += new RichMenuItem("miEditCopyKit")
        contents += new RichMenuItem("miEditSwapKits")

        contents += new Separator()

        contents += new RichMenuItem("miEditCopyPad", Key.C, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("miEditPastePad", Key.P, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("miEditSwapPads", KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK))

    }

    object mnTools extends RichMenu("mnTools") {
        mnemonic = Key.T

        contents += new RichMenu("mnToolsOptions") {
            mnemonic = Key.O

            contents += new RichMenu("mnToolsOptionsDMN") {

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

        contents += new RichMenuItem("miToolsConvert", Key.C)
    }

    object mnHelp extends RichMenu("mnHelp") {
        mnemonic = Key.H

        contents += new RichMenuItem("miHelpContents", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))

        contents += new Separator()

        contents += new RichMenuItem("miHelpCheckForUpdate", Key.C)

        contents += new CheckMenuItem(L.G("miHelpCheckAutomatically")) {
            name = "miHelpCheckAutomatically"
            listenTo(updateTool.Checker)
            reactions += {
                case e: updateTool.Checker.AutoUpdateModeChanged => selected = e.newMode == updateTool.Checker.AutoUpdateMode.Automatically
                case ButtonClicked(_)                            => menuItemEventCallback(this)
            }
        }

        contents += new Separator()

        contents += new RichMenuItem("miHelpAbout", Key.A)
    }

    contents += mnFile
    contents += mnEdit
    contents += mnTools
    contents += mnHelp
}
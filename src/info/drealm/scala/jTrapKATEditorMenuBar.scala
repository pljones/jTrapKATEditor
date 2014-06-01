/****************************************************************************
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
 ****************************************************************************/

package info.drealm.scala

import java.awt.event.{ InputEvent, KeyEvent }
import javax.swing.KeyStroke

import swing._
import swing.event._

object jTrapKATEditorMenuBar extends MenuBar {
    def menuEventCallback(source: Menu, action: eventX.MenuEvent.Action) = {
        deafTo(this)
        publish(new eventX.MenuEvent(source, action))
        listenTo(this)
    }
    class RichMenu(title: String) extends Menu(title) {

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
    class RichMenuItem(private val _title: String,
                       private val _name: String,
                       private val _accelerator: Option[KeyStroke],
                       private val _mnemonic: Option[event.Key.Value])
        extends MenuItem(_title) {

        def this(title: String, name: String) = this(title, name, None, None)
        def this(title: String, name: String, mnemonic: event.Key.Value) = this(title, name, None, Some(mnemonic))
        def this(title: String, name: String, accelerator: KeyStroke) = this(title, name, Some(accelerator), None)
        def this(title: String, name: String, mnemonic: event.Key.Value, accelerator: KeyStroke) = this(title, name, Some(accelerator), Some(mnemonic))
        def this(title: String, name: String, accelerator: KeyStroke, mnemonic: event.Key.Value) = this(title, name, Some(accelerator), Some(mnemonic))

        reactions += { case e: ButtonClicked => menuItemEventCallback(e.source.asInstanceOf[MenuItem]) }

        name = _name

        _accelerator match {
            case Some(a) => action = new Action(_title) {
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

    object mnFile extends RichMenu("File") {
        name = "mnFile"
        mnemonic = Key.F

        contents += new RichMenuItem("New (V3)", "miFileNewV3", Key.Key3, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("New (V4)", "miFileNewV4", Key.Key4, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("Open...", "miFileOpen", Key.O, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK))

        contents += new Separator()

        contents += new RichMenuItem("Save All Memory", "miFileSaveAllMemory", Key.S, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("Save All Memory As...", "miFileSaveAllMemoryAs")
        contents += new RichMenuItem("Save Global Memory", "miFileSaveGlobalMemory")
        contents += new RichMenuItem("Save Global Memory As...", "miFileSaveGlobalMemoryAs")
        contents += new RichMenuItem("Save Current Kit", "miFileSaveCurrentKit")
        contents += new RichMenuItem("Save Current Kit As...", "miFileSaveCurrentKitAs")

        contents += new Separator()

        contents += new RichMenuItem("Close", "miFileClose", Key.C, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK))

        contents += new Separator()

        contents += new RichMenuItem("Exit", "miFileExit", Key.X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK))
    }

    object mnEdit extends RichMenu("Edit") {
        name = "mnEdit"
        mnemonic = Key.E

        contents += new RichMenuItem("Undo", "miEditUndo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK)) { visible = false }
        contents += new RichMenuItem("Redo", "miEditRedo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK)) { visible = false }

        contents += new Separator() { visible = false }

        contents += new RichMenuItem("Copy Kit...", "miEditCopyKit")
        contents += new RichMenuItem("Swap Kits...", "miEditSwapKits")

        contents += new Separator()

        contents += new RichMenuItem("Copy Pad", "miEditCopyPad", Key.C, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("Paste Pad", "miEditPastePad", Key.P, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK))
        contents += new RichMenuItem("Swap Pads", "miEditSwapPads", KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK))

    }

    object mnTools extends RichMenu("Tools") {
        name = "mnTools"
        mnemonic = Key.T

        contents += new RichMenu("Options") {
            name = "mnToolsOptions"
            mnemonic = Key.O

            contents += new RichMenu("Display MIDI Notes") {
                name = "mnToolsOptionsDMN"

                private[this] val bgTODMN = new ButtonGroup();
                
                def dmnAs = if (bgTODMN.selected.isDefined) bgTODMN.selected.get.name.stripPrefix("miToolsOptionsDMNAs") else "" 

                contents += new RadioMenuItem("As Numbers") {
                    name = "miToolsOptionsDMNAsNumbers"
                    selected = true
                    bgTODMN.buttons.add(this)
                    reactions += { case ButtonClicked(_) => menuItemEventCallback(this) }
                }
                contents += new RadioMenuItem("As Names (60=C3)") {
                    name = "miToolsOptionsDMNAsNamesC3"
                    bgTODMN.buttons.add(this)
                    reactions += { case ButtonClicked(_) => menuItemEventCallback(this) }
                }
                contents += new RadioMenuItem("As Names (60=C4)") {
                    name = "miToolsOptionsDMNAsNamesC4"
                    bgTODMN.buttons.add(this)
                    reactions += { case ButtonClicked(_) => menuItemEventCallback(this) }
                }
            }
        }

        contents += new RichMenuItem("Convert...", "miToolsConvert", Key.C)
    }

    object mnHelp extends RichMenu("Help") {

        name = "mnHelp"
        mnemonic = Key.H

        contents += new RichMenuItem("Contents...", "miHelpContents", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0))

        contents += new Separator()

        contents += new RichMenuItem("Check For Update...", "miHelpCheckForUpdate", Key.C)

        contents += new CheckMenuItem("Check Automatically") {
            name = "miHelpCheckAutomatically"
            reactions += { case ButtonClicked(_) => menuItemEventCallback(this) }
        }

        contents += new Separator()

        contents += new RichMenuItem("About...", "miHelpAbout", Key.A)
    }

    contents += mnFile
    contents += mnEdit
    contents += mnTools
    contents += mnHelp
}
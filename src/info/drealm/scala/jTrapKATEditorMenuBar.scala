package info.drealm.scala

import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.KeyStroke

import swing._
import swing.event._

object jTrapKATEditorMenuBar extends MenuBar {

    class MenuItemEvent(val menuItem: MenuItem) extends Event
    def menuItemEventCallback(mi: MenuItem) = publish(new MenuItemEvent(mi))

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

        reactions += { case ButtonClicked(_) => menuItemEventCallback(this) }

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

    object mnFile extends Menu("File") {

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

    object mnEdit extends Menu("Edit") {

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

    object mnTools extends Menu("Tools") {

        name = "mnTools"
        mnemonic = Key.T

        contents += new Menu("Options") {
            mnemonic = Key.O
            name = "miToolsOptions"

            contents += new Menu("Display MIDI Notes") {
                name = "miToolsOptionsDMN"

                val bgTODMN = new ButtonGroup();

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

    object mnHelp extends Menu("Help") {

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
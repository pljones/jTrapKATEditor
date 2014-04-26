package info.drealm.scala

import swing._
import swing.event._
import info.drealm.scala.migPanel._

object frmTrapkatSysexEditor extends MainFrame {
    class FileMenuEvent(val menuItem: MenuItem) extends Event
    class EditMenuEvent(val menuItem: MenuItem) extends Event
    class ToolsMenuEvent(val menuItem: MenuItem) extends Event
    class HelpMenuEvent(val menuItem: MenuItem) extends Event

    iconImage = toolkit.getImage("resources/tk_wild2-sq.png")
    resizable = false
    title = "TrapKAT SysEx Editor"
    bounds = new Rectangle(100, 100, 880, 516)

    menuBar = jTrapKATEditorMenuBar

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {

        contents += (new TabbedPane() {
            name = "tpnMain"
            pages += pnKitsPads
            pages += new TabbedPane.Page("Global", new MigPanel("insets 5", "[]", "[]") {
                name = "pnGlobal"
                contents += (new Label("Being reworked"), "cell 0 0")
            })
        }, "cell 0 0,grow")

        contents += (new Label("MIDI OX SysEx Transmit: 512 bytes, 8 buffers, 160ms between buffers, 320ms after F7"), "cell 0 1,alignx center")

    }

    listenTo(menuBar)
    reactions += {
        case mie: jTrapKATEditorMenuBar.MenuItemEvent => {
            mie.menuItem.name match {
                case miName if miName.startsWith("miFile")  => publish(new FileMenuEvent(mie.menuItem))
                case miName if miName.startsWith("miEdit")  => publish(new EditMenuEvent(mie.menuItem))
                case miName if miName.startsWith("miTools") => publish(new ToolsMenuEvent(mie.menuItem))
                case miName if miName.startsWith("miHelp")  => publish(new HelpMenuEvent(mie.menuItem))
                case otherwise                              => {}
            }
        }
    }

    centerOnScreen
}
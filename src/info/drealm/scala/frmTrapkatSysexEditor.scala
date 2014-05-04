package info.drealm.scala

import swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._

object frmTrapkatSysexEditor extends MainFrame {

    iconImage = toolkit.getImage("resources/tk_wild2-sq.png")
    resizable = false
    title = "TrapKAT SysEx Editor"
    bounds = new Rectangle(100, 100, 880, 516)

    menuBar = jTrapKATEditorMenuBar

    private[this] object tpnMain extends TabbedPane {
        listenTo(selection)
        reactions += { case tpnE: SelectionChanged => publish(new TabChangeEvent(selection.page)) }

        name = "tpnMain"
        pages += pnKitsPads
        pages += new TabbedPane.Page("Global", new MigPanel("insets 5", "[]", "[]") {
            name = "pnGlobal"
            contents += (new Label("Being reworked"), "cell 0 0")
        })
    }

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {

        contents += (tpnMain, "cell 0 0,grow")

        contents += (new Label("MIDI OX SysEx Transmit: 512 bytes, 8 buffers, 160ms between buffers, 320ms after F7"), "cell 0 1,alignx center")

    }

    listenTo(menuBar)
    listenTo(tpnMain)
    listenTo(pnKitsPads.content)
    reactions += {
        case miE: MenuItemEvent => {
            deafTo(this)
            publish(miE)
            listenTo(this)
        }
        case mnE: MenuEvent => {
            deafTo(this)
            publish(mnE)
            listenTo(this)
        }
        case tpnE: TabChangeEvent => {
            deafTo(this)
            publish(tpnE)
            listenTo(this)
        }
    }

    centerOnScreen
}

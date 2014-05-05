package info.drealm.scala

import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._

object PadSlot {
    val padFunction = Seq("Off", "Seq Start", "Seq Stop", "Seq Cont", "Alt Reset", "Next Kit", "Prev Kit")
}

class Pad(pad: String) extends MigPanel("insets 2", "[grow,right][fill,left]", "[]") {

    name = "pnPad" + pad
    private[this] val lblPadSlot = new Label("" + pad)
    private[this] val cbxPadSlot = new RichComboBox(PadSlot.padFunction, "cbxPad" + pad, lblPadSlot) {
        makeEditable()
        selection.index = -1
        selection.item = ""
    }
    contents += (lblPadSlot, "cell 0 0,alignx trailing,aligny baseline")
    contents += (cbxPadSlot, "cell 1 0,grow")

    listenTo(cbxPadSlot.selection)
    reactions += {
        case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }
}

class Slot(private[this] val slot: Integer) extends Tuple3(slot, new Label("" + slot), new RichComboBox(PadSlot.padFunction, "cbxSlot" + slot) {
    makeEditable()
    selection.index = -1
    selection.item = ""
}) {
    _2.peer.setDisplayedMnemonic(("" + slot).last)
    // Uhhhhh, right...
    _2.peer.setLabelFor(_3.peer.asInstanceOf[java.awt.Component])
}
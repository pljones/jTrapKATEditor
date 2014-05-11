package info.drealm.scala

import scala.swing._
import swing.event._
import info.drealm.scala.migPanel._

object PadSlot {
    val padFunction = Seq("Off", "Seq Start", "Seq Stop", "Seq Cont", "Alt Reset", "Next Kit", "Prev Kit")
}

class Pad(pad: String) extends MigPanel("insets 4 2 4 2", "[grow,right][fill,left]", "[]") {

    name = "pnPad" + pad
    private[this] val lblPad = new Label("" + pad)
    private[this] val cbxPad = new RichComboBox(PadSlot.padFunction, "cbxPad" + pad, lblPad) {
        makeEditable()
        peer.getEditor().getEditorComponent().asInstanceOf[javax.swing.JTextField].setColumns(4)
        selection.index = -1
        selection.item = ""
    }
    contents += (lblPad, "cell 0 0,alignx trailing,aligny baseline")
    contents += (cbxPad, "cell 1 0,grow")

    listenTo(cbxPad.selection)
    listenTo(cbxPad)

    reactions += {
        case e: SelectionChanged if (e.source.isInstanceOf[ComboBox[_]]) => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
        case e: eventX.CbxEditorFocused => {
            deafTo(this)
            publish(e)
            listenTo(this)
        }
    }
}

class Slot(private[this] val slot: Integer) extends Tuple3(slot, new Label("" + slot), new RichComboBox(PadSlot.padFunction, "cbxSlot" + slot) {
    makeEditable()
    peer.getEditor().getEditorComponent().asInstanceOf[javax.swing.JTextField].setColumns(4)
    selection.index = 0
}) {
    _2.peer.setDisplayedMnemonic(("" + slot).last)
    // Uhhhhh, right...
    _2.peer.setLabelFor(_3.peer.asInstanceOf[java.awt.Component])
}
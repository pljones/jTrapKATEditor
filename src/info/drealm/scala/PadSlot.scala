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
        editorPeer.setColumns(4)
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
    editorPeer.setColumns(4)
    selection.index = 0
}) {
    _2.peer.setDisplayedMnemonic(("" + slot).last)
    // Uhhhhh, right...
    _2.peer.setLabelFor(_3.peer.asInstanceOf[java.awt.Component])
}

// For note names, we need to convert to 0..127 based on note and octave
// number.
// "octave" should be set to indicate the octave for middle C (note 60),
// e.g. 3 for C3=60 or 4 for C4=60.
trait NoteNameToNumber {
    val notes = "CDEFGAB"
    val octave: Int
    /**
     * Used to convert note name to note number
     *
     * @param noteName String representation of a note (such as "C#2")
     * @param octave   Number that follows "C" for middle C (note 60)
     * @return         The integer note number
     */
    def toNumber(noteName: String): Int = {
        try {
            val note = notes.indexOf(noteName.head.toUpper)
            val sharp = noteName.indexOf('#') == 1
            val flat = noteName.indexOf('b') == 1
            val octaveNum = noteName.drop(1 + (if (sharp || flat) 1 else 0)).toInt
            if (note < 0) -1
            else note +
                (if (sharp) 1 else 0) +
                (if (flat) -1 else 0) +
                (octaveNum - octave + 5) * 12
        }
        catch {
            // At the moment, allow empty string
            // TODO: just return -1
            case _: Throwable => if (noteName == "") 0 else -1
        }
    }
}

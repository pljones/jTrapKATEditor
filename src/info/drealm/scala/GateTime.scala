package info.drealm.scala

object GateTime {
    val gateSelection = Seq("Latch mode", "Infinite", "Roll mode")

    /*
     * Minor rant time.
     * If I want a signed byte value, I will say I want a signed value.
     * Byte should be 0 to 255.
     * No one ever uses "signed bytes" in the real world.  Ever...
     * Forcing use of larger data types by not supporting unsigned values at all
     * is simply ridiculous.
     * To avoid some of that, magic (byteValue & intMask) is used to up-cast
     * without sign extension, to keep the logic readable.
     */

    def toGateTime(value: String): Byte = gateSelection.indexOf(value.trim().toLowerCase().capitalize) match {
        case -1 => value.toDouble match {
            case tooSmall if tooSmall < 0.005 => throw new IllegalArgumentException("Numeric value must be 0.005 or over and under 6.4")
            case tooBig if tooBig >= 6.4      => throw new IllegalArgumentException("Numeric value must be 0.005 or over and under 6.4")
            case tiny if tiny < 0.4           => ((tiny - 0.005) / 0.005).toByte
            case medium if medium < 4.2       => (79 + ((medium - 0.4) / 0.025)).toByte
            case large                        => (231 + ((large - 4.2) / 0.1)).toByte
        }
        case x => (253 + x).toByte
    }
    def toString(v: Byte): String = (0x000000ff & v) match {
        case tiny if tiny <= 79      => f"${0.005 + (tiny * 0.005)}%.3f"
        case medium if medium <= 231 => f"${0.4 + ((medium - 79) * 0.025)}%.3f"
        case large if large < 253    => f"${4.2 + ((large - 231) * 0.1)}%.3f"
        case index                   => gateSelection(index - 253)
    }

    val verifier = new javax.swing.InputVerifier {

        import swing._
        import javax.swing.{ JComponent, JComboBox, JTextField }

        // Use pattern matching to neatly get the ComboBox
        // If super.shouldYieldFocus is true and this is not a dropdown value
        // round it to nearest known value
        override def shouldYieldFocus(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] if super.shouldYieldFocus(c) => {
                        cb.setSelectedItem(e.getText())
                        if (cb.getSelectedIndex() == -1) {
                            e.setText(GateTime.toString(toGateTime(e.getText())))
                            cb.setSelectedItem(e.getText())
                        }
                        true
                    }
                    case cb: JComboBox[_] => {
                        e.setText(cb.getSelectedItem().asInstanceOf[String])
                        e.selectAll()
                        true
                    }
                    case _ => false
                }
                case _ => false
            }
        }

        // Use pattern matching to neatly get the ComboBox
        // If not a dropdown item, check the value in the editor
        // otherwise it's a good 'un
        def verify(c: JComponent): Boolean = {
            c match {
                case e: JTextField => e.getParent() match {
                    case cb: JComboBox[_] => gateTimeOK(e.getText())
                    case _                => false
                }
                case _ => false
            }
        }

        def gateTimeOK(gateTime: String): Boolean = {
            try {
                toGateTime(gateTime)
                true
            }
            catch {
                case _: Throwable => false
            }
        }

    }
}

class GateTimeComboBox(name: String, label: swing.Label) extends RichComboBox[String](GateTime.gateSelection, name, label) {
    makeEditable()
    editorPeer.setInputVerifier(GateTime.verifier)
    selection.index = -1
    selection.item = "0.115"
}

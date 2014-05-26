package info.drealm.scala

object GateTime {
    val verifier = new GateTimeVerifier
    val gateSelection = Seq("Latch mode", "Infinite", "Roll mode")

    def roundGateTime(value: String): String = toString(fromString(value))

    /*
     * Minor rant time.
     * If I want a signed byte value, I will say I want a signed value.
     * Byte should be 0 to 255.
     * No one ever uses "signed bytes" in the real world.  Ever...
     * Forcing use of larger data types by not supporting unsigned values at all
     * is simply ridiculous.
     */

    def toByte(value: Double): Int = value match {
        case negative if value < 0     => throw new IllegalArgumentException("Can only convert positive doubles to Byte")
        case tooSmall if value < 0.005 => throw new IllegalArgumentException("Can only convert doubles from 0.005 to under 6.4 to Byte")
        case tooBig if value >= 6.4    => throw new IllegalArgumentException("Can only convert doubles from 0.005 to under 6.4 to Byte")
        case tiny if value < 0.4       => ((value - 0.005) / 0.005).toInt
        case medium if value < 4.2     => (79 + ((value - 0.4) / 0.025)).toInt
        case large                     => (231 + ((value - 4.2) / 0.1)).toInt
    }
    def fromByte(value: Int): Double = value match {
        case tiny if value <= 79    => 0.005 + (value * 0.005)
        case medium if value <= 231 => 0.4 + ((value - 79) * 0.025)
        case large if value < 253   => 4.2 + ((value - 231) * 0.1)
        case _                      => throw new IllegalArgumentException("Can only convert bytes 0 to 252 to Double; use toString for higher values")
    }
    def fromString(value: String): Int = gateSelection.indexOf(value.trim().toLowerCase().capitalize) match {
        case -1 => toByte(value.toDouble)
        case x  => (253 + x).toInt
    }
    def toString(value: Int): String = value match {
        case x if x < 253 => f"${fromByte(value)}%.3f"
        case otherwise    => gateSelection(value - 253)
    }
}

class GateTimeVerifier extends javax.swing.InputVerifier {

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
                        e.setText(GateTime.roundGateTime(e.getText()))
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
            GateTime.fromString(gateTime)
            true
        }
        catch {
            case _: Throwable => false
        }
    }

}
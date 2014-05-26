package info.drealm.scala.verifier

import swing._
import javax.swing.{ JComponent, JComboBox, JTextField, InputVerifier }
import info.drealm.scala.{ PadSlot, NoteNameToNumber }

object NoteVerifier {
    val numbers = new NoteVerifier
    val asNamesC3 = new NoteVerifierAsNames { val octave = 3 }
    val asNamesC4 = new NoteVerifierAsNames { val octave = 4 }

    // Set the input verifier on a collection of pad and slot editors
    // These RichComboBoxes *must* be editable (else null for you)
    def set(padsSlots: Seq[info.drealm.scala.RichComboBox[String]], v: NoteVerifier): Unit = padsSlots foreach (p => p.editorPeer.setInputVerifier(v))
}

// Verify that a pad or slot value is either one of the
// drop down entries or a valid note (number 0 to 127)
class NoteVerifier extends InputVerifier {

    // Use pattern matching to neatly get the ComboBox
    // If the verifier is happy, tidy the value up and commit it
    // Either way, set the editor to the ComboBox value
    override def shouldYieldFocus(c: JComponent): Boolean = {
        c match {
            case e: JTextField => e.getParent() match {
                case cb: JComboBox[_] if super.shouldYieldFocus(c) => {
                    PadSlot.padFunction.indexOf(e.getText().trim().toLowerCase().capitalize) match {
                        case x if x >= 0 => e.setText(PadSlot.padFunction(x))
                        case _           => e.setText("" + e.getText().toInt)
                    }
                    cb.setSelectedItem(e.getText())
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
    // Check the padFunctions for the value
    // Otherwise check for a value 0 to 127
    def verify(c: JComponent): Boolean = {
        c match {
            case e: JTextField => e.getParent() match {
                case cb: JComboBox[_] => {
                    val value = e.getText().trim().toLowerCase().capitalize
                    PadSlot.padFunction.contains(value) || padSlotOK(value)
                }
                case _ => false
            }
            case _ => false
        }
    }

    // String must be an int 0 to 127
    def padSlotOK(value: String): Boolean = try {
        val note = value.toInt
        note >= 0 && note <= 127
    }
    catch {
        case _: Throwable => false
    }

}

// For NoteVerifier, we need to turn a string into an int between 0 and 127.
// We use NoteNameToNumber but let concrete instances decide the octave.
abstract class NoteVerifierAsNames extends NoteVerifier with NoteNameToNumber {
    override def padSlotOK(value: String): Boolean = {
        val note = toNumber(value)
        note >= 0 && note <= 127
    }
}

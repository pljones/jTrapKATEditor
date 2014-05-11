package info.drealm.scala

import scala.swing.ComboBox
import scala.swing.event._

class RichComboBox[A](items: Seq[A], _name: String, _label: scala.swing.Label) extends ComboBox[A](items) {
    def this(items: Seq[A], _name: String) = this(items, _name, null)
    def this(items: Seq[A]) = this(items, "", null)

    name = _name
    if (_label != null) {
        // Uhhhhh, right...
        _label.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
    }

    override def makeEditable()(implicit editor: ComboBox[A] => ComboBox.Editor[A]) {
        super.makeEditable()
        listenTo(editor(this).component)
        reactions += { case e: FocusGained => publish(new eventX.CbxEditorFocused(this)) }
    }
}
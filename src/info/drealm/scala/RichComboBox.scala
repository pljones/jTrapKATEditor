package info.drealm.scala

import scala.swing.ComboBox

class RichComboBox[A](items: Seq[A], private[this] val _name: String, private[this] val _label: scala.swing.Label) extends ComboBox[A](items) {
    def this(items: Seq[A], _name: String) = this(items, _name, null)
    def this(items: Seq[A]) = this(items, "", null)

    name = _name
    if (_label != null) {
        // Uhhhhh, right...
        _label.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
    }
}
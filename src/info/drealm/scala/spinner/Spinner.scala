package info.drealm.scala.spinner

import swing._
import javax.swing.{ JSpinner, SpinnerModel }

class Spinner(private[this] val _model: SpinnerModel, private[this] val _name: String, private[this] val _label: scala.swing.Label) extends Component with Publisher {
    def this(_model: SpinnerModel, _name: String) = this(_model, _name, null)
    def this(_model: SpinnerModel) = this(_model, "", null)

    override lazy val peer = new javax.swing.JSpinner(_model) with SuperMixin

    name = _name
    if (_label != null) {
        // Uhhhhh, right...
        _label.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
    }

    def value: Any = peer.getValue
    def value_=(o: Any) { peer.setValue(o) }

    def model: SpinnerModel = peer.getModel()
    def model_=(m: SpinnerModel) { peer.setModel(m) }

    peer.addChangeListener(Swing.ChangeListener { e =>
        publish(new event.ValueChanged(this))
    })
}
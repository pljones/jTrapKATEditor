package info.drealm.scala.spinner

import swing._
import javax.swing.{ JSpinner, SpinnerModel }

class Spinner(private[this] val _model: SpinnerModel) extends Component with Publisher {
    override lazy val peer = new javax.swing.JSpinner(_model) with SuperMixin

    def value: Any = peer.getValue
    def value_=(o: Any) { peer.setValue(o) }

    def model: SpinnerModel = peer.getModel()
    def model_=(m: SpinnerModel) { peer.setModel(m) }

    peer.addChangeListener(Swing.ChangeListener { e =>
        publish(new event.ValueChanged(this))
    })
}
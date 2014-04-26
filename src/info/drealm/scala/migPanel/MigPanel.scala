package info.drealm.scala.migPanel

/*
 * https://code.google.com/p/scala-samples/source/browse/trunk/simplecalculator/src/main/scala/MigPanel.scala
 */

import swing._
import net.miginfocom.swing._

class MigPanel(layoutConstraints: String, colConstraints: String, rowConstraints: String)
    extends Panel with LayoutContainer {

    def this() = this("", "", "")
    def this(layoutConstraints: String) = this(layoutConstraints, "", "")

    type Constraints = String
    protected class MigContent extends Content {
        def +=(c: Component, l: Constraints) = add(c, l)
    }

    def layoutManager = peer.getLayout.asInstanceOf[net.miginfocom.swing.MigLayout]

    protected def constraintsFor(comp: Component) =
        layoutManager.getConstraintMap.get(comp.peer).toString
    protected def areValid(c: Constraints): (Boolean, String) = (true, "")
    protected def add(c: Component, l: Constraints) = peer.add(c.peer, l)

    override lazy val peer = new javax.swing.JPanel(new MigLayout(layoutConstraints, colConstraints, rowConstraints)) with SuperMixin
    override def contents: MigContent = new MigContent
}

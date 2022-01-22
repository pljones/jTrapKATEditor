/****************************************************************************
 *                                                                          *
 *   (C) Copyright 2011 by ram.6383@gmail.com                               *
 *   https://code.google.com/u/101571804349167871099/                       *
 *                                                                          *
 *   This file originated from                                              *
 *   https://code.google.com/p/scala-samples/source/browse/trunk/simplecalculator/src/main/scala/MigPanel.scala
 *   and is free software; you can redistribute it and/or modify            *
 *   it under the terms of the GNU General Public License as published by   *
 *   the Free Software Foundation; either version 3 of the License, or      *
 *   (at your option) any later version.                                    *
 *                                                                          *
 *   This file is distributed in the hope that it will be useful,           *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *   GNU General Public License for more details.                           *
 *                                                                          *
 *   You should have received a copy of the GNU General Public License      *
 *   along with jTrapKATEditor.  If not, see http://www.gnu.org/licenses/   *
 *                                                                          *
 ****************************************************************************/

package info.drealm.scala.migPanel

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

/****************************************************************************
 *                                                                          *
 *   (C) Copyright 2014 by Peter L Jones                                    *
 *   pljones@users.sf.net                                                   *
 *                                                                          *
 *   This file is part of jTrapKATEditor.                                   *
 *                                                                          *
 *   jTrapKATEditor is free software; you can redistribute it and/or modify *
 *   it under the terms of the GNU General Public License as published by   *
 *   the Free Software Foundation; either version 3 of the License, or      *
 *   (at your option) any later version.                                    *
 *                                                                          *
 *   jTrapKATEditor is distributed in the hope that it will be useful,      *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *   GNU General Public License for more details.                           *
 *                                                                          *
 *   You should have received a copy of the GNU General Public License      *
 *   along with jTrapKATEditor.  If not, see http://www.gnu.org/licenses/   *
 *                                                                          *
 * ************************************************************************ *
 * **                                                                    ** *
 * ** Additionally, this file is also released under the same terms as   ** *
 * ** the Scala Swing library and it may bw included in that software    ** *
 * ** rather than jTrapKATEditor.                                        ** *
 * **                                                                    ** *
 * ************************************************************************ *
 *                                                                          *
 ****************************************************************************/

package info.drealm.scala.spinner

import swing._
import javax.swing.{ JSpinner, SpinnerModel }

class Spinner(_model: SpinnerModel, _name: String = "", _tip: String = null, _label: scala.swing.Label = null) extends Component with Publisher {
    def this(_model: SpinnerModel, _name: String, label: scala.swing.Label) = this(_model, _name, _label = label)

    override lazy val peer = new javax.swing.JSpinner(_model) with SuperMixin

    name = _name
    if (_tip != null) tooltip = _tip
    
    if (_label != null) {
        // Uhhhhh, right...
        _label.peer.setLabelFor(peer.asInstanceOf[java.awt.Component])
        if (_label.tooltip == null) _label.tooltip = tooltip
    }

    def value: Any = peer.getValue
    def value_=(o: Any) { peer.setValue(o) }

    def model: SpinnerModel = peer.getModel()
    def model_=(m: SpinnerModel) { peer.setModel(m) }

    peer.addChangeListener(Swing.ChangeListener { e =>
        publish(new event.ValueChanged(this))
    })
}
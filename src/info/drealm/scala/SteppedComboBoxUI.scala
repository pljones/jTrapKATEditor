/**
 * **************************************************************************
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
 * **************************************************************************
 */
package info.drealm.scala

import java.awt._
import javax.swing._
import javax.swing.plaf.basic._
import javax.swing.plaf.metal._
import com.sun.java.swing.plaf.windows._
//import javax.swing.plaf.nimbus._

object SteppedComboBoxUI {
    def getSteppedComboBoxUI(peer: JComboBox[_]): SteppedComboBoxUI = {
        UIManager.getLookAndFeel().getClass().getName() match {
            case "javax.swing.plaf.metal.MetalLookAndFeel" => new MetalSteppedComboBoxUI(peer)
            case "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" => new WindowsSteppedComboBoxUI(peer)
            case _ => new BasicSteppedComboBoxUI(peer)
        }
    }
}
trait SteppedComboBoxUI extends BasicComboBoxUI {
    val peer: JComboBox[_]
    def getArrowButton: JButton
    override def createPopup: ComboPopup = new BasicComboPopup(peer) {
        lazy val myWidth: Int = {
            val metrics: FontMetrics = peer.getFontMetrics(peer.getFont());
            ((0 to (peer.getItemCount() - 1)) map (i => peer.getItemAt(i))).foldLeft(peer.getBounds().width)((maxWidth, item) => {
                val width = metrics.stringWidth(item.toString())
                if (width > maxWidth) width else maxWidth
            }) + getArrowButton.getBounds().width + 2
        }
        lazy val popupBounds = computePopupBounds(0, peer.getBounds().height, myWidth, getPopupHeightForRowCount(peer.getMaximumRowCount()))
        override def show: Unit = {
            scroller.setMaximumSize(popupBounds.getSize());
            scroller.setPreferredSize(popupBounds.getSize());
            scroller.setMinimumSize(popupBounds.getSize());
            list.invalidate()
            peer.getSelectedIndex() match {
                case -1 => list.clearSelection()
                case i  => list.setSelectedIndex(i)
            }
            list.ensureIndexIsVisible(list.getSelectedIndex())
            setLightWeightPopupEnabled(peer.isLightWeightPopupEnabled())
            this.show(peer, popupBounds.x, popupBounds.y)
        }
        getAccessibleContext().setAccessibleParent(peer)
    }
}

class BasicSteppedComboBoxUI(val peer: JComboBox[_]) extends SteppedComboBoxUI {
    def getArrowButton = arrowButton
}

class MetalSteppedComboBoxUI(val peer: JComboBox[_]) extends MetalComboBoxUI with SteppedComboBoxUI {
    def getArrowButton = arrowButton
}

class WindowsSteppedComboBoxUI(val peer: JComboBox[_]) extends WindowsComboBoxUI with SteppedComboBoxUI {
    def getArrowButton = arrowButton
}

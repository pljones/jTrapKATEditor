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

// Because this extends BasicComboBoxUI, we forfeit platform look and feel
class SteppedComboBoxUI(peer: JComboBox[_]) extends BasicComboBoxUI {
    override def createPopup: ComboPopup = new BasicComboPopup(peer) {
        lazy val myWidth: Int = {
            val metrics: FontMetrics = peer.getFontMetrics(peer.getFont());
            ((0 to (peer.getItemCount() - 1)) map (i => peer.getItemAt(i))).foldLeft(peer.getBounds().width)((maxWidth, item) => {
                val width = metrics.stringWidth(item.toString())
                if (width > maxWidth) width else maxWidth
            }) + arrowButton.getBounds().width + 2
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

// However, for some reason, I cannot get scala.swing.ComboBox to take this as a peer
// Rolling my own JDK7-compatible ComboBox scala class will help...
class SteppedJComboBox[A](model: ComboBoxModel[A]) extends JComboBox[A](model) {
    private[this] var layingOut = false
    private[this] lazy val width = {
        val metrics: java.awt.FontMetrics = getFontMetrics(getFont());
        ((0 to (getItemCount() - 1)) map (i => getItemAt(i))).foldLeft(getBounds().width)((maxWidth, item) => {
            val width = metrics.stringWidth(item.toString())
            if (width > maxWidth) width else maxWidth
        }) + 3
    }
    override def doLayout = {
        layingOut = true
        try { super.doLayout() } finally { layingOut = false }
    }

    override def getSize: java.awt.Dimension = new java.awt.Dimension(
        if (!layingOut) width else super.getWidth(), super.getHeight())
}

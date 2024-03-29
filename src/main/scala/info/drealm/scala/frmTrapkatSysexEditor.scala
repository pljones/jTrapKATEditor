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

import info.drealm.scala.{Localization => L, Resource => R}
import info.drealm.scala.eventX._
import info.drealm.scala.migPanel._
import info.drealm.scala.prefs.{Preferences => P}
import info.drealm.scala.updateTool._

import scala.swing._
import scala.swing.event._

object frmTrapkatSysexEditor extends Frame with AllMemorySelectionReactor with AnyValueReactor {
    protected def _isUIChange = true
    protected def _uiReaction = title = getTitle()

    peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
    override def closeOperation = {
        // Saving the location is currently pointless as it cannot be used on start up reliably
        P.windowLocation = Tuple2(this.location.x, this.location.y)
        P.currentWorkingDirectory = (
            if (jTrapKATEditor.currentFile.isDirectory()) jTrapKATEditor.currentFile
            else if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getParentFile()
            else util.getHome).getCanonicalFile()
        jTrapKATEditor.exitClose
    }

    private val jTrapKATEditorIconString = "info/drealm/scala/tk_wild2-sq.png"
    if (jTrapKATEditorIconString != null) {
        val jTrapKATEditorIconResourceURL = R.U(jTrapKATEditorIconString)
        if (jTrapKATEditorIconResourceURL != null) {
            val jTrapKATEditorIconImageIcon = new javax.swing.ImageIcon(jTrapKATEditorIconResourceURL)
            if (jTrapKATEditorIconImageIcon != null) {
                iconImage = jTrapKATEditorIconImageIcon.getImage
            }
        }
    }
    title = getTitle()
    resizable = false

    menuBar = jTrapKATEditorMenuBar

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {
        contents += (tpnMain, "cell 0 0,grow")
        contents += (new Label(L.G("lbMIDIOX")), "cell 0 1,alignx center")
    }

    layout.Focus.set(pnKitsPads, "pnPad1")

    // Doing this breaks for any non-default location
    // this.location = new Point(P.windowLocation._1, P.windowLocation._2)
    // and without this, it doesn't display the window manager frame...
    centerOnScreen

    listenTo(jTrapKATEditor)

    setDisplay()

    private[this] def getTitle() = L.G(
        "MainProgramTitle",
        L.G("ApplicationProductName"),
        if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getName() else L.G("MainProgramTitleNewFile"),
        jTrapKATEditor.doV3V4V5(L.G("V3"), L.G("V4"), L.G("V5")),
        if (jTrapKATEditor.currentAllMemory.changed) "[*]" else "")

    def okayToSplat(dataItem: model.DataItem, to: String): Boolean = !dataItem.changed || (Dialog.showConfirmation(
        tpnMain,
        L.G("OKToSplat", to),
        L.G("ApplicationProductName"),
        Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok)

    def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(
        tpnMain,
        L.G("ImportThing", to, from, thing),
        L.G("ImportThingCaption", thing),
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes

    def okayToRenumber(into: Int, intoName: String, from: Int, fromName: String): Boolean = Dialog.showConfirmation(
        tpnMain,
        L.G("RenumberKit", s"${into}", intoName, s"${from}", fromName),
        L.G("RenumberKitCaption"),
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes
}

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

import swing._
import swing.event._
import info.drealm.scala.migPanel._
import info.drealm.scala.eventX._
import info.drealm.scala.{ jTrapKATEditorPreferences => prefs, Localization => L, Resource => R }
import info.drealm.scala.updateTool._

object frmTrapkatSysexEditor extends Frame {

    peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
    override def closeOperation = {
        prefs.currentWorkingDirectory = (
            if (jTrapKATEditor.currentFile.isDirectory()) jTrapKATEditor.currentFile
            else if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getParentFile()
            else util.getHome).getCanonicalFile()
        jTrapKATEditor.exitClose
    }

    iconImage = (new javax.swing.ImageIcon(R.U("info/drealm/scala/tk_wild2-sq.png"))).getImage
    title = getTitle()
    resizable = false

    menuBar = jTrapKATEditorMenuBar

    contents = new MigPanel("insets 3", "[grow]", "[grow, fill][bottom]") {
        contents += (tpnMain, "cell 0 0,grow")
        contents += (new Label(L.G("lbMIDIOX")), "cell 0 1,alignx center")
    }

    layout.Focus.set(pnKitsPads, "pnPad1")

    centerOnScreen

    listenTo(jTrapKATEditor)

    private[this] def getTitle() = L.G("MainProgramTitle",
        L.G("ApplicationProductName"),
        if (jTrapKATEditor.currentFile.isFile()) jTrapKATEditor.currentFile.getName() else L.G("MainProgramTitleNewFile"),
        jTrapKATEditor.doV3V4(L.G("V3"), L.G("V4")),
        if (jTrapKATEditor.currentAllMemory.changed) "[*]" else ""
    )

    reactions += {
        case wo: WindowOpened      => Checker.autoUpdateMode = prefs.updateAutomatically
        case amc: SomethingChanged => title = getTitle()
    }

    def okayToSplat(dataItem: model.DataItem, to: String): Boolean = {
        Console.println(s"okayToSplat: ${to} - changed? ${dataItem.changed}")
        !dataItem.changed || (Dialog.showConfirmation(null,
            L.G("OKToSplat", to),
            L.G("ApplicationProductName"),
            Dialog.Options.OkCancel, Dialog.Message.Warning, null) == Dialog.Result.Ok)
    }

    def okayToConvert(thing: String, from: String, to: String): Boolean = Dialog.showConfirmation(null,
        L.G("ImportThing", to, from, thing),
        L.G("ImportThingCaption", thing),
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes

    def okayToRenumber(into: Int, intoName: String, from: Int, fromName: String): Boolean = Dialog.showConfirmation(null,
        L.G("RenumberKit", s"${into}", intoName, s"${from}", fromName),
        L.G("RenumberKitCaption"),
        Dialog.Options.YesNo, Dialog.Message.Question, null) == Dialog.Result.Yes
}

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

trait HistoryAction { val actionName: String; def undoAction(): Unit; def redoAction(): Unit }
object EditHistory {
    private[this] var _editHistory = List[HistoryAction]()
    private[this] var _depth = 0

    def canUndo: Boolean = _depth < _editHistory.length
    def undoAction(): Unit = if (canUndo) _editHistory.drop(_depth).headOption map (x => { _depth = _depth + 1; x.undoAction() })
    def undoActionName: Option[String] = if (canUndo) _editHistory.drop(_depth).headOption.map(x => x.actionName) else None

    def canRedo: Boolean = _depth > 0
    def redoAction(): Unit = if (canRedo) _editHistory.drop(_depth - 1).headOption map (x => { _depth = _depth - 1; x.redoAction() })
    def redoActionName: Option[String] = if (canRedo) _editHistory.drop(_depth - 1).headOption.map(x => x.actionName) else None

    def add(action: HistoryAction): Unit = {
        _editHistory = action :: _editHistory.drop(_depth);
        _depth = 0
    }

    def clear(): Unit = {
        _editHistory = List()
        _depth = 0
    }
}

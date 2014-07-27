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

package info.drealm.scala.eventX

import scala.swing._
import scala.swing.event._

case class MenuCancelled(override val source: Menu) extends ComponentEvent
case class MenuDeselected(override val source: Menu) extends ComponentEvent
case class MenuSelected(override val source: Menu) extends ComponentEvent

class TabChangeEvent(val source: TabbedPane.Page) extends Event

trait ItemEvent extends ComponentEvent {
    val item: Any
}
class ItemDeselected(override val source: info.drealm.scala.RichComboBox[_], override val item: Any) extends ItemEvent
class ItemSelected(override val source: info.drealm.scala.RichComboBox[_], override val item: Any) extends ItemEvent
class CbxEditorFocused(override val source: info.drealm.scala.RichComboBox[_]) extends ComponentEvent

class V3V4SelectionChanged(val source: info.drealm.scala.V3V4ComboBox[_, _, _, _]) extends Event

class KitSelectionChanged(override val source: Component, val oldKit: Int, val newKit: Int) extends SelectionChanged(source)
class PadSelectionChanged(override val source: Component, val oldPad: Int, val newPad: Int) extends SelectionChanged(source)

class CurrentAllMemoryChanged(val source: AnyRef) extends Event
class GlobalChanged(val source: AnyRef) extends Event
class CurrentKitChanged(val source: AnyRef) extends Event
class CurrentPadChanged(val source: AnyRef) extends Event
class DataItemChanged(val dataItem: info.drealm.scala.model.DataItem) extends Event

class AutoUpdateModeChanged(val oldMode: info.drealm.scala.updateTool.Checker.AutoUpdateMode.AutoUpdateMode, val newMode: info.drealm.scala.updateTool.Checker.AutoUpdateMode.AutoUpdateMode) extends Event

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
 ****************************************************************************/

package info.drealm.scala.eventX

import scala.swing._
import scala.swing.event._

class MenuItemEvent(override val source: MenuItem) extends ComponentEvent
class FileMenuEvent(source: MenuItem) extends MenuItemEvent(source)
class EditMenuEvent(source: MenuItem) extends MenuItemEvent(source)
class ToolsMenuEvent(source: MenuItem) extends MenuItemEvent(source)
class HelpMenuEvent(source: MenuItem) extends MenuItemEvent(source)

object MenuEvent extends Enumeration {
    type Action = Value
    val Canceled, Deselected, Selected = Value
}
class MenuEvent(val source: Menu, val action: MenuEvent.Action) extends Event
case class MenuCanceled(override val source: Menu) extends ComponentEvent
case class MenuDeselected(override val source: Menu) extends ComponentEvent
case class MenuSelected(override val source: Menu) extends ComponentEvent

class TabChangeEvent(val source: TabbedPane.Page) extends Event

trait ItemEvent extends ComponentEvent {
    val item: Any
}
class ItemDeselected(override val source: info.drealm.scala.RichComboBox[_], override val item: Any) extends ItemEvent
class ItemSelected(override val source: info.drealm.scala.RichComboBox[_], override val item: Any) extends ItemEvent
class CbxEditorFocused(override val source: info.drealm.scala.RichComboBox[_]) extends ComponentEvent

class AllMemoryChanged extends Event
class GlobalChanged extends Event
class KitChanged(val oldKit: Int, val newKit: Int) extends Event
class PadChanged(val oldPad: Int, val newPad: Int) extends Event
class DisplayNotesAs(val oldMode: info.drealm.scala.PadSlot.DisplayMode.DisplayMode, val newMode: info.drealm.scala.PadSlot.DisplayMode.DisplayMode) extends Event

class DataItemChanged(val dataItem: info.drealm.scala.model.DataItem) extends Event

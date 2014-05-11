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

class CbxEditorFocused (override val source: info.drealm.scala.RichComboBox[_]) extends ComponentEvent

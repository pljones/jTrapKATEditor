package info.drealm.scala

import javax.swing.UIManager
import swing.SimpleSwingApplication

object jTrapKATEditor extends SimpleSwingApplication {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    //override def shutdown() = {}

    def top = frmTrapkatSysexEditor

    def loseUnsavedChanges = {
        true
    }

    listenTo(frmTrapkatSysexEditor)
    reactions += {
        case me: frmTrapkatSysexEditor.FileMenuEvent => {
            me.menuItem.name.stripPrefix("miFile") match {
                case "NewV3" if (loseUnsavedChanges) => Console.println("File NewV3")
                case "NewV4" if (loseUnsavedChanges) => Console.println("File NewV3")
                case "Open" if (loseUnsavedChanges)  => Console.println("File Open")
                case save if save.startsWith("Save") => {
                    save.stripPrefix("Save") match {
                        case saveAs if saveAs.endsWith("As") => {
                            saveAs.stripSuffix("As") match {
                                case "AllMemory"    => Console.println("FileSave AllMemory As")
                                case "GlobalMemory" => Console.println("FileSave GlobalMemory As")
                                case "CurrentKit"   => Console.println("FileSave CurrentKit As")
                                case otherwise      => {}
                            }
                        }
                        case otherwise => {
                            otherwise match {
                                case "AllMemory"    => Console.println("FileSave AllMemory")
                                case "GlobalMemory" => Console.println("FileSave GlobalMemory")
                                case "CurrentKit"   => Console.println("FileSave CurrentKit")
                                case otherwise      => {}
                            }
                        }
                    }
                }
                case "Close" if (loseUnsavedChanges) => Console.println("File Close")
                case "Exit" if (loseUnsavedChanges)  => quit
                case otherwise                       => {}
            }
        }
        case me: frmTrapkatSysexEditor.EditMenuEvent => {
            me.menuItem.name.stripPrefix("miEdit") match {
                case "Undo"     => {}
                case "Redo"     => {}
                case "CopyKit"  => {}
                case "SwapKits" => {}
                case "CopyPad"  => {}
                case "PastePad" => {}
                case "SwapPads" => {}
                case otherwise  => {}
            }
        }
        case me: frmTrapkatSysexEditor.ToolsMenuEvent => {
            me.menuItem.name.stripPrefix("miTools") match {
                case otherwise => {}
            }
        }
        case me: frmTrapkatSysexEditor.HelpMenuEvent => {
            me.menuItem.name.stripPrefix("miHelp") match {
                case otherwise => {}
            }
        }
    }
}
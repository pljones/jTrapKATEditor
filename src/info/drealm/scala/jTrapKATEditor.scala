package info.drealm.scala

import javax.swing.UIManager
import swing._
import swing.event._
import info.drealm.scala.eventX._

object jTrapKATEditor extends SimpleSwingApplication {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    UIManager.put("swing.boldMetal", false);

    //override def shutdown() = {}

    def top = frmTrapkatSysexEditor

    //TODO: This should say what is unsaved and ask for confirmation to lose it.
    def loseUnsavedChanges = {
        true
    }

    listenTo(frmTrapkatSysexEditor)
    reactions += {
        case mie: FileMenuEvent => {
            mie.source.name.stripPrefix("miFile") match {
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
                case otherwise => {
                    Console.println("File event " + mie.source.name)
                }
            }
        }
        case mie: EditMenuEvent => {
            mie.source.name.stripPrefix("miEdit") match {
                case "Undo"     => Console.println("Edit Undo")
                case "Redo"     => Console.println("Edit Redo")
                case "CopyKit"  => Console.println("Edit CopyKit")
                case "SwapKits" => Console.println("Edit SwapKits")
                case "CopyPad"  => Console.println("Edit CopyPad")
                case "PastePad" => Console.println("Edit PastePad")
                case "SwapPads" => Console.println("Edit SwapPads")
                case otherwise => {
                    Console.println("Edit event " + mie.source.name)
                }
            }
        }
        case mie: ToolsMenuEvent => {
            mie.source.name.stripPrefix("miTools") match {
                case "OptionsDMNAsNumbers" => Console.println("Tools Options DMN AsNumbers")
                case "OptionsDMNAsNamesC3" => Console.println("Tools Options DMN AsNamesC3")
                case "OptionsDMNAsNamesC4" => Console.println("Tools Options DMN AsNamesC4")
                case "Convert"             => Console.println("Tools Convert")
                case otherwise => {
                    Console.println("Tools event " + mie.source.name)
                }
            }
        }
        case mie: HelpMenuEvent => {
            mie.source.name.stripPrefix("miHelp") match {
                case "Contents"           => Console.println("Help Contents")
                case "CheckForUpdate"     => Console.println("Help CheckForUpdate")
                case "CheckAutomatically" => Console.println("Help CheckAutomatically")
                case "About"              => Console.println("Help About")
                case otherwise => {
                    Console.println("Help event " + mie.source.name)
                }
            }
        }
        case mne: MenuEvent => {
            mne.source.name.stripPrefix("mn") match {
                case "File"            => Console.println("File menu " + mne.action)
                case "Edit"            => Console.println("Edit menu " + mne.action)
                case "Tools"           => Console.println("Tools menu " + mne.action)
                case "ToolsOptions"    => Console.println("Tools Options menu " + mne.action)
                case "ToolsOptionsDMN" => Console.println("Tools Options DMN menu " + mne.action)
                case "Help"            => Console.println("Help menu " + mne.action)
                case otherwise => {
                    Console.println("MenuEvent" + mne.source.name + " action " + mne.action)
                }
            }
        }
        case tpe: TabChangeEvent => {
            tpe.source.content.name.stripPrefix("pn") match {
                case "KitsPads"   => Console.println("Main KitsPads")
                case "Global"     => Console.println("Main Global")
                case "PadDetails" => Console.println("KitPadsDetails PadDetails")
                case "MoreSlots"  => Console.println("KitPadsDetails MoreSlots")
                case "KitDetails" => Console.println("KitPadsDetails KitDetails")
                case otherwise => {
                    Console.println("TabChangeEvent " + otherwise)
                }
            }
        }
        case cbxE: SelectionChanged if (cbxE.source.isInstanceOf[ComboBox[_]]) => {
            cbxE.source.name.stripPrefix("cbx") match {
                case otherwise => {
                    Console.println("ComboBox SelectionChanged " + otherwise)
                }
            }
        }
        case spnE: ValueChanged if (spnE.source.isInstanceOf[info.drealm.scala.spinner.Spinner]) => {
            spnE.source.name.stripPrefix("spn") match {
                case otherwise => {
                    Console.println("Spinner ValueChanged " + otherwise)
                }
            }
        }
        case ckbE: ButtonClicked if (ckbE.source.isInstanceOf[CheckBox]) => {
            ckbE.source.name.stripPrefix("ckb") match {
                case otherwise => {
                    Console.println("CheckBox 'ButtonClicked' " + otherwise)
                }
            }
        }
        case txtE: EditDone if (txtE.source.isInstanceOf[TextField]) => {
            txtE.source.name.stripPrefix("txt") match {
                case otherwise => {
                    Console.println("TextField EditDone " + otherwise)
                }
            }
        }
    }
}

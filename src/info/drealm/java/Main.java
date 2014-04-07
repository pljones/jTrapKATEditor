package info.drealm.java;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.SpringLayout;

import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

import java.awt.Component;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class Main {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 887, 545);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[]", "[]"));

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);

		JMenuItem mntmFileNewV3 = new JMenuItem("New (V3)");
		mntmFileNewV3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmFileNewV3);

		JMenuItem mntmFileNewV4 = new JMenuItem("New (V4)");
		mntmFileNewV4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmFileNewV4);

		JMenuItem mntmFileOpen = new JMenuItem("Open...");
		mntmFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmFileOpen);

		JSeparator mntmFileSep1 = new JSeparator();
		mnFile.add(mntmFileSep1);

		JMenuItem mntmFileSaveAll = new JMenuItem("Save All Memory");
		mntmFileSaveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmFileSaveAll);

		JMenuItem mntmFileSaveAllAs = new JMenuItem("Save All Memory As...");
		mnFile.add(mntmFileSaveAllAs);

		JMenuItem mntmFileSaveGlobal = new JMenuItem("Save Global Memory");
		mnFile.add(mntmFileSaveGlobal);

		JMenuItem mntmFileSaveGlobalAs = new JMenuItem(
				"Save Global Memory As...");
		mnFile.add(mntmFileSaveGlobalAs);

		JMenuItem mntmFileSaveCurrentKit = new JMenuItem("Save Current Kit");
		mnFile.add(mntmFileSaveCurrentKit);

		JMenuItem mntmFileSaveCurrentKitAs = new JMenuItem(
				"Save Current Kit As...");
		mnFile.add(mntmFileSaveCurrentKitAs);

		JSeparator mntmFileSep2 = new JSeparator();
		mnFile.add(mntmFileSep2);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmClose);

		JSeparator mntmFileSep3 = new JSeparator();
		mnFile.add(mntmFileSep3);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				InputEvent.CTRL_MASK));
		mnFile.add(mntmExit);

		JMenu mnEdit = new JMenu("Edit");
		mnEdit.setMnemonic('E');
		menuBar.add(mnEdit);

		JMenuItem mntmEditUndo = new JMenuItem("Undo");
		mntmEditUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK));
		mnEdit.add(mntmEditUndo);

		JMenuItem mntmEditRedo = new JMenuItem("Redo");
		mntmEditRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.CTRL_MASK));
		mnEdit.add(mntmEditRedo);

		JSeparator mntmEditSep1 = new JSeparator();
		mnEdit.add(mntmEditSep1);

		JMenuItem mntmEditCopyKit = new JMenuItem("Copy Kit...");
		mnEdit.add(mntmEditCopyKit);

		JMenuItem mntmEditSwapKits = new JMenuItem("Swap Kits...");
		mnEdit.add(mntmEditSwapKits);

		JSeparator mntmEditSep2 = new JSeparator();
		mnEdit.add(mntmEditSep2);

		JMenuItem mntmCopyPad = new JMenuItem("Copy Pad");
		mnEdit.add(mntmCopyPad);

		JMenuItem mntmPastePad = new JMenuItem("Paste Pad");
		mnEdit.add(mntmPastePad);

		JMenuItem mntmSwapPads = new JMenuItem("Swap Pads");
		mnEdit.add(mntmSwapPads);

		JMenu mnTools = new JMenu("Tools");
		mnTools.setMnemonic('T');
		menuBar.add(mnTools);

		JMenu mnToolsOptions = new JMenu("Options");
		mnTools.add(mnToolsOptions);

		JMenu mnTODisplayMidiNotes = new JMenu("Display MIDI Notes");
		mnToolsOptions.add(mnTODisplayMidiNotes);

		ButtonGroup bgTODMN = new ButtonGroup();

		JRadioButtonMenuItem rdbtnmntmTODMNAsNumbers = new JRadioButtonMenuItem(
				"As Numbers");
		rdbtnmntmTODMNAsNumbers.setSelected(true);
		mnTODisplayMidiNotes.add(rdbtnmntmTODMNAsNumbers);
		bgTODMN.add(rdbtnmntmTODMNAsNumbers);

		JRadioButtonMenuItem rdbtnmntmTODMNAsNamesC3 = new JRadioButtonMenuItem(
				"As Names (60=C3)");
		mnTODisplayMidiNotes.add(rdbtnmntmTODMNAsNamesC3);
		bgTODMN.add(rdbtnmntmTODMNAsNamesC3);

		JRadioButtonMenuItem rdbtnmntmTODMNAsNamesC4 = new JRadioButtonMenuItem(
				"As Names (60=C4)");
		mnTODisplayMidiNotes.add(rdbtnmntmTODMNAsNamesC4);
		bgTODMN.add(rdbtnmntmTODMNAsNamesC4);

		bgTODMN.add(rdbtnmntmTODMNAsNumbers);

		JMenuItem mntmToolsConvert = new JMenuItem("Convert...");
		mnTools.add(mntmToolsConvert);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		JMenuItem mntmHelpContents = new JMenuItem("Contents...");
		mntmHelpContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
				0));
		mnHelp.add(mntmHelpContents);

		JSeparator mntmHelpSep1 = new JSeparator();
		mnHelp.add(mntmHelpSep1);

		JMenuItem mntmCheckForUpdate = new JMenuItem("Check for update...");
		mnHelp.add(mntmCheckForUpdate);

		JCheckBoxMenuItem chckbxmntmCheckAutomatically = new JCheckBoxMenuItem(
				"Check automatically");
		mnHelp.add(chckbxmntmCheckAutomatically);

		JSeparator mntmHelpSep2 = new JSeparator();
		mnHelp.add(mntmHelpSep2);
		
		JMenuItem mntmAbout = new JMenuItem("About...");
		mnHelp.add(mntmAbout);
	}

}

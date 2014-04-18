package info.drealm.java;

import java.awt.EventQueue;

import javax.swing.JFrame;

import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;

import java.awt.Dimension;
import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.JCheckBox;

import java.awt.Insets;

public class Main {

	private String[] padCbxItems = new String[] { "", "Off", "Seq Start",
			"Seq Stop", "Seq Cont", "Alt Reset", "Next Kit", "Prev Kit" };
	private String[] pads1_24 = new String[] { "Off", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24" };
	private String[] padCurves = new String[] { "Curve 1", "Curve 2",
			"Curve 3", "Curve 4", "Curve 5", "Curve 6", "Curve 7", "Curve 8",
			"2nd Note @ Hardest", "2nd Note @ Hard", "2nd Note @ Medium",
			"2nd Note @ Soft", "2 Note Layer", "Xfade @ Middle",
			"Xswitch @ Middle", "1@Medium;3@Hardest", "2@Medium;3@Hard",
			"2Double 1;3Medium", "3 Note Layer", "4 Note VelShift",
			"4 Note Layer", "Alternating", "Control + 3 Notes" };
	private String[] fcCurves = new String[] { "Curve 1", "Curve 2", "Curve 3",
			"Curve 4", "Curve 5", "Curve 6", "Curve 7" };
	private String[] padGates = new String[] { "", "Latch Mode", "Infinite",
			"Roll Mode" };
	private String[] fcFunctions = new String[] { "Off", "CC#01 (Mod Wheel)",
			"CC#04 (F/C 0..64)", "CC#04 (F/C 0..127)", "Hat Note" };
	private JFrame frmTrapkatSysexEditor;
	private JTextField txtKitName;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					Main window = new Main();
					window.frmTrapkatSysexEditor.pack();
					window.frmTrapkatSysexEditor.setVisible(true);
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
		frmTrapkatSysexEditor = new JFrame();
		frmTrapkatSysexEditor.setResizable(false);
		frmTrapkatSysexEditor.setTitle("TrapKAT SysEx Editor");
		frmTrapkatSysexEditor.setBounds(100, 100, 880, 516);
		frmTrapkatSysexEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmTrapkatSysexEditor.setJMenuBar(menuBar);

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

		// --------------------------------

		frmTrapkatSysexEditor.getContentPane().setLayout(
				new MigLayout("insets 3", "[grow]", "[grow,fill][bottom]"));

		JTabbedPane tpnMain = new JTabbedPane(JTabbedPane.TOP);
		frmTrapkatSysexEditor.getContentPane().add(tpnMain, "cell 0 0,grow");

		JPanel pnKitsPads = new JPanel();
		tpnMain.addTab("Kits & Pads", null, pnKitsPads, null);
		pnKitsPads.setLayout(new MigLayout("insets 3", "[grow]", "[][grow]"));

		JPanel pnKitsPadsTop = new JPanel();
		pnKitsPadsTop.setBorder(null);
		pnKitsPads.add(pnKitsPadsTop, "cell 0 0,grow");
		pnKitsPadsTop.setLayout(new MigLayout("insets 0", "[grow]",
				"[][][grow]"));

		JPanel pnSelector = new JPanel();
		pnSelector.setBorder(null);
		pnKitsPadsTop.add(pnSelector, "cell 0 0,growx,aligny baseline");
		pnSelector.setLayout(new MigLayout("insets 0",
				"[][][][grow,fill][][][grow,fill][][][]", "[]"));

		JLabel lblSelectKit = new JLabel("Select Kit:");
		lblSelectKit.setDisplayedMnemonic('K');
		pnSelector.add(lblSelectKit, "cell 0 0,alignx right");

		JComboBox<String> cbxSelectedKit = new JComboBox<String>();
		lblSelectKit.setLabelFor(cbxSelectedKit);
		cbxSelectedKit.setMaximumRowCount(24);
		cbxSelectedKit.setModel(new DefaultComboBoxModel<String>(
				new String[] { "WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW",
						"WWWWWWWWWWWW", "WWWWWWWWWWWW", "WWWWWWWWWWWW" }));
		pnSelector.add(cbxSelectedKit, "cell 1 0");

		JLabel lblKitEdited = new JLabel("Edited");
		pnSelector.add(lblKitEdited, "cell 2 0,alignx left");

		JLabel lblName = new JLabel("Name:");
		pnSelector.add(lblName, "cell 4 0,alignx right");

		txtKitName = new JTextField();
		lblName.setLabelFor(txtKitName);
		pnSelector.add(txtKitName, "cell 5 0");
		txtKitName.setText("WWWWWWWWWWWW");
		txtKitName.setColumns(16);

		JLabel lblSelectPad = new JLabel("Select Pad:");
		lblSelectPad.setDisplayedMnemonic('P');
		pnSelector.add(lblSelectPad, "cell 7 0,alignx right");

		JComboBox<String> cbxSelectedPad = new JComboBox<String>();
		lblSelectPad.setLabelFor(cbxSelectedPad);
		cbxSelectedPad.setMaximumRowCount(28);
		cbxSelectedPad.setModel(new DefaultComboBoxModel<String>(new String[] {
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
				"13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
				"23", "24", "Chick", "Splash", "B/C", "Bass" }));
		pnSelector.add(cbxSelectedPad, "cell 8 0");

		JLabel lblPadEdited = new JLabel("Edited");
		pnSelector.add(lblPadEdited, "cell 9 0,alignx left");

		JPanel pnPads = new JPanel();
		pnPads.setBorder(null);
		pnKitsPadsTop.add(pnPads, "cell 0 1,grow");
		pnPads.setLayout(new MigLayout("insets 0, gap 0",
				"[grow][grow][grow][grow][grow][grow][grow][grow]", "[][][][]"));

		JPanel pnPad1 = new JPanel();
		pnPad1.setBorder(null);
		pnPad1.setBackground(new Color(224, 255, 255));
		pnPad1.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad1 = new JLabel("1");

		JComboBox<String> cbxPad1 = new JComboBox<String>();
		lblPad1.setLabelFor(cbxPad1);
		cbxPad1.setMaximumSize(new Dimension(80, 32767));
		cbxPad1.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad1.setEditable(true);

		pnPad1.add(lblPad1, "cell 0 0,alignx trailing,aligny baseline");
		pnPad1.add(cbxPad1, "cell 1 0,grow");
		pnPads.add(pnPad1, "cell 2 2,grow");

		JPanel pnPad2 = new JPanel();
		pnPad2.setBorder(null);
		pnPad2.setBackground(new Color(224, 255, 255));
		pnPad2.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad2 = new JLabel("2");

		JComboBox<String> cbxPad2 = new JComboBox<String>();
		lblPad2.setLabelFor(cbxPad2);
		cbxPad2.setMaximumSize(new Dimension(80, 32767));
		cbxPad2.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad2.setEditable(true);

		pnPad2.add(lblPad2, "cell 0 0,alignx trailing,aligny baseline");
		pnPad2.add(cbxPad2, "cell 1 0,grow");
		pnPads.add(pnPad2, "cell 3 2,grow");

		JPanel pnPad3 = new JPanel();
		pnPad3.setBorder(null);
		pnPad3.setBackground(new Color(224, 255, 255));
		pnPad3.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad3 = new JLabel("3");

		JComboBox<String> cbxPad3 = new JComboBox<String>();
		lblPad3.setLabelFor(cbxPad3);
		cbxPad3.setMaximumSize(new Dimension(80, 32767));
		cbxPad3.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad3.setEditable(true);

		pnPad3.add(lblPad3, "cell 0 0,alignx trailing,aligny baseline");
		pnPad3.add(cbxPad3, "cell 1 0,grow");
		pnPads.add(pnPad3, "cell 4 2,grow");

		JPanel pnPad4 = new JPanel();
		pnPad4.setBorder(null);
		pnPad4.setBackground(new Color(224, 255, 255));
		pnPad4.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad4 = new JLabel("4");

		JComboBox<String> cbxPad4 = new JComboBox<String>();
		lblPad4.setLabelFor(cbxPad4);
		cbxPad4.setMaximumSize(new Dimension(80, 32767));
		cbxPad4.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad4.setEditable(true);

		pnPad4.add(lblPad4, "cell 0 0,alignx trailing,aligny baseline");
		pnPad4.add(cbxPad4, "cell 1 0,grow");
		pnPads.add(pnPad4, "cell 5 2,grow");

		JPanel pnPad5 = new JPanel();
		pnPad5.setBorder(null);
		pnPad5.setBackground(new Color(224, 255, 255));
		pnPad5.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad5 = new JLabel("5");

		JComboBox<String> cbxPad5 = new JComboBox<String>();
		lblPad5.setLabelFor(cbxPad5);
		cbxPad5.setMaximumSize(new Dimension(80, 32767));
		cbxPad5.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad5.setEditable(true);

		pnPad5.add(lblPad5, "cell 0 0,alignx trailing,aligny baseline");
		pnPad5.add(cbxPad5, "cell 1 0,grow");
		pnPads.add(pnPad5, "cell 1 2,grow");

		JPanel pnPad6 = new JPanel();
		pnPad6.setBorder(null);
		pnPad6.setBackground(new Color(224, 255, 255));
		pnPad6.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad6 = new JLabel("6");

		JComboBox<String> cbxPad6 = new JComboBox<String>();
		lblPad6.setLabelFor(cbxPad6);
		cbxPad6.setMaximumSize(new Dimension(80, 32767));
		cbxPad6.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad6.setEditable(true);

		pnPad6.add(lblPad6, "cell 0 0,alignx trailing,aligny baseline");
		pnPad6.add(cbxPad6, "cell 1 0,grow");
		pnPads.add(pnPad6, "cell 2 1,grow");

		JPanel pnPad7 = new JPanel();
		pnPad7.setBorder(null);
		pnPad7.setBackground(new Color(224, 255, 255));
		pnPad7.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad7 = new JLabel("7");

		JComboBox<String> cbxPad7 = new JComboBox<String>();
		lblPad7.setLabelFor(cbxPad7);
		cbxPad7.setMaximumSize(new Dimension(80, 32767));
		cbxPad7.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad7.setEditable(true);

		pnPad7.add(lblPad7, "cell 0 0,alignx trailing,aligny baseline");
		pnPad7.add(cbxPad7, "cell 1 0,grow");
		pnPads.add(pnPad7, "cell 3 1,grow");

		JPanel pnPad8 = new JPanel();
		pnPad8.setBorder(null);
		pnPad8.setBackground(new Color(224, 255, 255));
		pnPad8.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad8 = new JLabel("8");

		JComboBox<String> cbxPad8 = new JComboBox<String>();
		lblPad8.setLabelFor(cbxPad8);
		cbxPad8.setMaximumSize(new Dimension(80, 32767));
		cbxPad8.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad8.setEditable(true);

		pnPad8.add(lblPad8, "cell 0 0,alignx trailing,aligny baseline");
		pnPad8.add(cbxPad8, "cell 1 0,grow");
		pnPads.add(pnPad8, "cell 4 1,grow");

		JPanel pnPad9 = new JPanel();
		pnPad9.setBorder(null);
		pnPad9.setBackground(new Color(224, 255, 255));
		pnPad9.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad9 = new JLabel("9");

		JComboBox<String> cbxPad9 = new JComboBox<String>();
		lblPad9.setLabelFor(cbxPad9);
		cbxPad9.setMaximumSize(new Dimension(80, 32767));
		cbxPad9.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad9.setEditable(true);

		pnPad9.add(lblPad9, "cell 0 0,alignx trailing,aligny baseline");
		pnPad9.add(cbxPad9, "cell 1 0,grow");
		pnPads.add(pnPad9, "cell 5 1,grow");

		JPanel pnPad10 = new JPanel();
		pnPad10.setBorder(null);
		pnPad10.setBackground(new Color(224, 255, 255));
		pnPad10.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad10 = new JLabel("10");

		JComboBox<String> cbxPad10 = new JComboBox<String>();
		lblPad10.setLabelFor(cbxPad10);
		cbxPad10.setMaximumSize(new Dimension(80, 32767));
		cbxPad10.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad10.setEditable(true);

		pnPad10.add(lblPad10, "cell 0 0,alignx trailing,aligny baseline");
		pnPad10.add(cbxPad10, "cell 1 0,grow");
		pnPads.add(pnPad10, "cell 6 2,grow");

		JPanel pnPad11 = new JPanel();
		pnPad11.setBorder(null);
		pnPad11.setBackground(new Color(230, 230, 250));
		pnPad11.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad11 = new JLabel("11");

		JComboBox<String> cbxPad11 = new JComboBox<String>();
		lblPad11.setLabelFor(cbxPad11);
		cbxPad11.setMaximumSize(new Dimension(80, 32767));
		cbxPad11.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad11.setEditable(true);

		pnPad11.add(lblPad11, "cell 0 0,alignx trailing,aligny baseline");
		pnPad11.add(cbxPad11, "cell 1 0,grow");
		pnPads.add(pnPad11, "cell 2 3,grow");

		JPanel pnPad12 = new JPanel();
		pnPad12.setBorder(null);
		pnPad12.setBackground(new Color(230, 230, 250));
		pnPad12.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad12 = new JLabel("12");

		JComboBox<String> cbxPad12 = new JComboBox<String>();
		lblPad12.setLabelFor(cbxPad12);
		cbxPad12.setMaximumSize(new Dimension(80, 32767));
		cbxPad12.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad12.setEditable(true);

		pnPad12.add(lblPad12, "cell 0 0,alignx trailing,aligny baseline");
		pnPad12.add(cbxPad12, "cell 1 0,grow");
		pnPads.add(pnPad12, "cell 3 3,grow");

		JPanel pnPad13 = new JPanel();
		pnPad13.setBorder(null);
		pnPad13.setBackground(new Color(230, 230, 250));
		pnPad13.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad13 = new JLabel("13");

		JComboBox<String> cbxPad13 = new JComboBox<String>();
		lblPad13.setLabelFor(cbxPad13);
		cbxPad13.setMaximumSize(new Dimension(80, 32767));
		cbxPad13.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad13.setEditable(true);

		pnPad13.add(lblPad13, "cell 0 0,alignx trailing,aligny baseline");
		pnPad13.add(cbxPad13, "cell 1 0,grow");
		pnPads.add(pnPad13, "cell 4 3,grow");

		JPanel pnPad14 = new JPanel();
		pnPad14.setBorder(null);
		pnPad14.setBackground(new Color(230, 230, 250));
		pnPad14.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad14 = new JLabel("14");

		JComboBox<String> cbxPad14 = new JComboBox<String>();
		lblPad14.setLabelFor(cbxPad14);
		cbxPad14.setMaximumSize(new Dimension(80, 32767));
		cbxPad14.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad14.setEditable(true);

		pnPad14.add(lblPad14, "cell 0 0,alignx trailing,aligny baseline");
		pnPad14.add(cbxPad14, "cell 1 0,grow");
		pnPads.add(pnPad14, "cell 5 3,grow");

		JPanel pnPad15 = new JPanel();
		pnPad15.setBorder(null);
		pnPad15.setBackground(new Color(230, 230, 250));
		pnPad15.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad15 = new JLabel("15");

		JComboBox<String> cbxPad15 = new JComboBox<String>();
		lblPad15.setLabelFor(cbxPad15);
		cbxPad15.setMaximumSize(new Dimension(80, 32767));
		cbxPad15.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad15.setEditable(true);

		pnPad15.add(lblPad15, "cell 0 0,alignx trailing,aligny baseline");
		pnPad15.add(cbxPad15, "cell 1 0");
		pnPads.add(pnPad15, "cell 0 3,grow");

		JPanel pnPad16 = new JPanel();
		pnPad16.setBorder(null);
		pnPad16.setBackground(new Color(230, 230, 250));
		pnPad16.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad16 = new JLabel("16");

		JComboBox<String> cbxPad16 = new JComboBox<String>();
		lblPad16.setLabelFor(cbxPad16);
		cbxPad16.setMaximumSize(new Dimension(80, 32767));
		cbxPad16.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad16.setEditable(true);

		pnPad16.add(lblPad16, "cell 0 0,alignx trailing,aligny baseline");
		pnPad16.add(cbxPad16, "cell 1 0,grow");
		pnPads.add(pnPad16, "cell 0 2,grow");

		JPanel pnPad17 = new JPanel();
		pnPad17.setBorder(null);
		pnPad17.setBackground(new Color(230, 230, 250));
		pnPad17.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad17 = new JLabel("17");

		JComboBox<String> cbxPad17 = new JComboBox<String>();
		lblPad17.setLabelFor(cbxPad17);
		cbxPad17.setMaximumSize(new Dimension(80, 32767));
		cbxPad17.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad17.setEditable(true);

		pnPad17.add(lblPad17, "cell 0 0,alignx trailing,aligny baseline");
		pnPad17.add(cbxPad17, "cell 1 0,grow");
		pnPads.add(pnPad17, "cell 1 1,grow");

		JPanel pnPad18 = new JPanel();
		pnPad18.setBorder(null);
		pnPad18.setBackground(new Color(230, 230, 250));
		pnPad18.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad18 = new JLabel("18");

		JComboBox<String> cbxPad18 = new JComboBox<String>();
		lblPad18.setLabelFor(cbxPad18);
		cbxPad18.setMaximumSize(new Dimension(80, 32767));
		cbxPad18.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad18.setEditable(true);

		pnPad18.add(lblPad18, "cell 0 0,alignx trailing,aligny baseline");
		pnPad18.add(cbxPad18, "cell 1 0");
		pnPads.add(pnPad18, "cell 2 0,grow");

		JPanel pnPad19 = new JPanel();
		pnPad19.setBorder(null);
		pnPad19.setBackground(new Color(230, 230, 250));
		pnPad19.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad19 = new JLabel("19");

		JComboBox<String> cbxPad19 = new JComboBox<String>();
		lblPad19.setLabelFor(cbxPad19);
		cbxPad19.setMaximumSize(new Dimension(80, 32767));
		cbxPad19.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad19.setEditable(true);

		pnPad19.add(lblPad19, "cell 0 0,alignx trailing,aligny baseline");
		pnPad19.add(cbxPad19, "cell 1 0,grow");
		pnPads.add(pnPad19, "cell 3 0,grow");

		JPanel pnPad20 = new JPanel();
		pnPad20.setBorder(null);
		pnPad20.setBackground(new Color(230, 230, 250));
		pnPad20.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad20 = new JLabel("20");

		JComboBox<String> cbxPad20 = new JComboBox<String>();
		lblPad20.setLabelFor(cbxPad20);
		cbxPad20.setMaximumSize(new Dimension(80, 32767));
		cbxPad20.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad20.setEditable(true);

		pnPad20.add(lblPad20, "cell 0 0,alignx trailing,aligny baseline");
		pnPad20.add(cbxPad20, "cell 1 0,grow");
		pnPads.add(pnPad20, "cell 4 0,grow");

		JPanel pnPad21 = new JPanel();
		pnPad21.setBorder(null);
		pnPad21.setBackground(new Color(230, 230, 250));
		pnPad21.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad21 = new JLabel("21");

		JComboBox<String> cbxPad21 = new JComboBox<String>();
		lblPad21.setLabelFor(cbxPad21);
		cbxPad21.setMaximumSize(new Dimension(80, 32767));
		cbxPad21.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad21.setEditable(true);

		pnPad21.add(lblPad21, "cell 0 0,alignx trailing,aligny baseline");
		pnPad21.add(cbxPad21, "cell 1 0,grow");
		pnPads.add(pnPad21, "cell 5 0,grow");

		JPanel pnPad22 = new JPanel();
		pnPad22.setBorder(null);
		pnPad22.setBackground(new Color(230, 230, 250));
		pnPad22.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad22 = new JLabel("22");

		JComboBox<String> cbxPad22 = new JComboBox<String>();
		lblPad22.setLabelFor(cbxPad22);
		cbxPad22.setMaximumSize(new Dimension(80, 32767));
		cbxPad22.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad22.setEditable(true);

		pnPad22.add(lblPad22, "cell 0 0,alignx trailing,aligny baseline");
		pnPad22.add(cbxPad22, "cell 1 0,grow");
		pnPads.add(pnPad22, "cell 6 1,grow");

		JPanel pnPad23 = new JPanel();
		pnPad23.setBorder(null);
		pnPad23.setBackground(new Color(230, 230, 250));
		pnPad23.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad23 = new JLabel("23");

		JComboBox<String> cbxPad23 = new JComboBox<String>();
		lblPad23.setLabelFor(cbxPad23);
		cbxPad23.setMaximumSize(new Dimension(80, 32767));
		cbxPad23.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad23.setEditable(true);

		pnPad23.add(lblPad23, "cell 0 0,alignx trailing,aligny baseline");
		pnPad23.add(cbxPad23, "cell 1 0,grow");
		pnPads.add(pnPad23, "cell 7 2,grow");

		JPanel pnPad24 = new JPanel();
		pnPad24.setBorder(null);
		pnPad24.setBackground(new Color(230, 230, 250));
		pnPad24.setLayout(new MigLayout("insets 2", "[grow,right][fill,left]",
				"[]"));
		JLabel lblPad24 = new JLabel("24");

		JComboBox<String> cbxPad24 = new JComboBox<String>();
		lblPad24.setLabelFor(cbxPad24);
		cbxPad24.setMaximumSize(new Dimension(80, 32767));
		cbxPad24.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPad24.setEditable(true);

		pnPad24.add(lblPad24, "cell 0 0,alignx trailing,aligny baseline");
		pnPad24.add(cbxPad24, "cell 1 0,grow");
		pnPads.add(pnPad24, "cell 7 3,grow");

		JPanel pnPedals = new JPanel();
		pnKitsPadsTop.add(pnPedals, "cell 0 2,grow");
		pnPedals.setLayout(new MigLayout(
				"insets 0",
				"[grow,leading][][][grow,fill][][grow,fill][][][grow,trailing]",
				"[]"));

		JPanel pnPadChick = new JPanel();
		pnPadChick.setBorder(null);
		pnPadChick.setLayout(new MigLayout("insets 2",
				"[grow,right][left,fill]", "[]"));
		JLabel lblPadChick = new JLabel("Chick");

		JComboBox<String> cbxPadChick = new JComboBox<String>();
		lblPadChick.setLabelFor(cbxPadChick);
		cbxPadChick.setMaximumSize(new Dimension(80, 32767));
		cbxPadChick.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPadChick.setEditable(true);

		pnPadChick.add(lblPadChick, "cell 0 0,alignx trailing,aligny baseline");
		pnPadChick.add(cbxPadChick, "cell 1 0,grow");
		pnPedals.add(pnPadChick, "cell 1 0");

		JPanel pnPadSplash = new JPanel();
		pnPadSplash.setBorder(null);
		pnPadSplash.setLayout(new MigLayout("insets 2",
				"[grow,right][left,fill]", "[]"));
		JLabel lblPadSplash = new JLabel("Splash");

		JComboBox<String> cbxPadSplash = new JComboBox<String>();
		lblPadSplash.setLabelFor(cbxPadSplash);
		cbxPadSplash.setMaximumSize(new Dimension(80, 32767));
		cbxPadSplash.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPadSplash.setEditable(true);

		pnPadSplash.add(lblPadSplash,
				"cell 0 0,alignx trailing,aligny baseline");
		pnPadSplash.add(cbxPadSplash, "cell 1 0,grow");
		pnPedals.add(pnPadSplash, "cell 1 0");

		JPanel pnHH = new JPanel();
		pnHH.setBorder(null);
		pnHH.setLayout(new MigLayout("insets 2", "[grow,right][left,fill]",
				"[]"));
		JLabel lblHH = new JLabel("Hihat Pads");
		lblHH.setDisplayedMnemonic('d');

		JComboBox<String> cbxHH1 = new JComboBox<String>();
		lblHH.setLabelFor(cbxHH1);
		cbxHH1.setMaximumRowCount(25);
		cbxHH1.setMaximumSize(new Dimension(48, 32767));
		cbxHH1.setModel(new DefaultComboBoxModel<String>(pads1_24));

		JComboBox<String> cbxHH2 = new JComboBox<String>();
		cbxHH2.setMaximumRowCount(25);
		cbxHH2.setMaximumSize(new Dimension(48, 32767));
		cbxHH2.setModel(new DefaultComboBoxModel<String>(pads1_24));

		JComboBox<String> cbxHH3 = new JComboBox<String>();
		cbxHH3.setMaximumRowCount(25);
		cbxHH3.setMaximumSize(new Dimension(48, 32767));
		cbxHH3.setModel(new DefaultComboBoxModel<String>(pads1_24));

		JComboBox<String> cbxHH4 = new JComboBox<String>();
		cbxHH4.setMaximumRowCount(25);
		cbxHH4.setMaximumSize(new Dimension(48, 32767));
		cbxHH4.setModel(new DefaultComboBoxModel<String>(pads1_24));

		pnHH.add(lblHH, "cell 0 0,alignx trailing,aligny baseline");
		pnHH.add(cbxHH1, "cell 1 0,grow");
		pnHH.add(cbxHH2, "cell 2 0,grow");
		pnHH.add(cbxHH3, "cell 3 0,grow");
		pnHH.add(cbxHH4, "cell 4 0,grow");
		pnPedals.add(pnHH, "cell 3 0");

		JPanel pnPadBC = new JPanel();
		pnPadBC.setBorder(null);
		pnPadBC.setLayout(new MigLayout("insets 2", "[grow,right][left,fill]",
				"[]"));
		JLabel lblPadBC = new JLabel("B/C");

		JComboBox<String> cbxPadBC = new JComboBox<String>();
		lblPadBC.setLabelFor(cbxPadBC);
		cbxPadBC.setMaximumSize(new Dimension(80, 32767));
		cbxPadBC.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPadBC.setEditable(true);

		pnPadBC.add(lblPadBC, "cell 0 0,alignx trailing,aligny baseline");
		pnPadBC.add(cbxPadBC, "cell 1 0,grow");
		pnPedals.add(pnPadBC, "cell 5 0");

		JPanel pnPadBass = new JPanel();
		pnPadBass.setBorder(null);
		pnPadBass.setLayout(new MigLayout("insets 2",
				"[grow,right][left,fill]", "[]"));
		JLabel lblPadBass = new JLabel("Bass");

		JComboBox<String> cbxPadBass = new JComboBox<String>();
		lblPadBass.setLabelFor(cbxPadBass);
		cbxPadBass.setMaximumSize(new Dimension(80, 32767));
		cbxPadBass.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxPadBass.setEditable(true);

		pnPadBass.add(lblPadBass, "cell 0 0,alignx trailing,aligny baseline");
		pnPadBass.add(cbxPadBass, "cell 1 0,grow");
		pnPedals.add(pnPadBass, "cell 6 0");

		JTabbedPane tpnKitPadsDetails = new JTabbedPane(JTabbedPane.TOP);
		pnKitsPads.add(tpnKitPadsDetails, "cell 0 1,grow");

		JPanel pnPadDetails = new JPanel();
		tpnKitPadsDetails.addTab("Pad Details", null, pnPadDetails, null);
		pnPadDetails.setLayout(new MigLayout("insets 5, gapx 2, gapy 0",
				"[][16px:n,right][][16px:n][][][16px:n][][16px:n][]",
				"[][][][][][][]"));

		JLabel lblSlots = new JLabel("Slots:");
		pnPadDetails.add(lblSlots, "cell 0 0");
		JLabel lblSlot2 = new JLabel("2");
		pnPadDetails.add(lblSlot2, "cell 1 0");
		lblSlot2.setDisplayedMnemonic('2');

		JComboBox<String> cbxSlot2 = new JComboBox<String>();
		lblSlot2.setLabelFor(cbxSlot2);
		pnPadDetails.add(cbxSlot2, "cell 2 0");
		cbxSlot2.setMaximumSize(new Dimension(80, 32767));
		cbxSlot2.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot2.setEditable(true);

		JLabel lblSlot3 = new JLabel("3");
		pnPadDetails.add(lblSlot3, "cell 1 1");
		lblSlot3.setDisplayedMnemonic('3');

		JComboBox<String> cbxSlot3 = new JComboBox<String>();
		lblSlot3.setLabelFor(cbxSlot3);
		pnPadDetails.add(cbxSlot3, "cell 2 1");
		cbxSlot3.setMaximumSize(new Dimension(80, 32767));
		cbxSlot3.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot3.setEditable(true);

		JLabel lblSlot4 = new JLabel("4");
		pnPadDetails.add(lblSlot4, "cell 1 2");
		lblSlot4.setDisplayedMnemonic('4');

		JComboBox<String> cbxSlot4 = new JComboBox<String>();
		lblSlot4.setLabelFor(cbxSlot4);
		pnPadDetails.add(cbxSlot4, "cell 2 2");
		cbxSlot4.setMaximumSize(new Dimension(80, 32767));
		cbxSlot4.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot4.setEditable(true);

		JLabel lblSlot5 = new JLabel("5");
		pnPadDetails.add(lblSlot5, "cell 1 3");
		lblSlot5.setDisplayedMnemonic('5');

		JComboBox<String> cbxSlot5 = new JComboBox<String>();
		lblSlot5.setLabelFor(cbxSlot5);
		pnPadDetails.add(cbxSlot5, "cell 2 3");
		cbxSlot5.setMaximumSize(new Dimension(80, 32767));
		cbxSlot5.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot5.setEditable(true);

		JLabel lblSlot6 = new JLabel("6");
		pnPadDetails.add(lblSlot6, "cell 1 4");
		lblSlot6.setDisplayedMnemonic('6');

		JComboBox<String> cbxSlot6 = new JComboBox<String>();
		lblSlot6.setLabelFor(cbxSlot6);
		pnPadDetails.add(cbxSlot6, "cell 2 4");
		cbxSlot6.setMaximumSize(new Dimension(80, 32767));
		cbxSlot6.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot6.setEditable(true);

		JLabel lblPadCurve = new JLabel("Curve:");
		lblPadCurve.setDisplayedMnemonic('C');
		pnPadDetails.add(lblPadCurve, "cell 4 0,alignx right");

		JComboBox<String> cbxPadCurve = new JComboBox<String>();
		lblPadCurve.setLabelFor(cbxPadCurve);
		cbxPadCurve.setModel(new DefaultComboBoxModel<String>(padCurves));
		pnPadDetails.add(cbxPadCurve, "cell 5 0");

		JLabel lblPadGate = new JLabel("Gate:");
		lblPadGate.setDisplayedMnemonic('G');
		pnPadDetails.add(lblPadGate, "cell 4 1,alignx right");

		JComboBox<String> cbxPadGate = new JComboBox<String>();
		lblPadGate.setLabelFor(cbxPadGate);
		cbxPadGate.setEditable(true);
		cbxPadGate.setModel(new DefaultComboBoxModel<String>(padGates));
		pnPadDetails.add(cbxPadGate, "cell 5 1");

		JLabel lblPadChannel = new JLabel("Channel:");
		lblPadChannel.setDisplayedMnemonic('h');
		pnPadDetails.add(lblPadChannel, "cell 4 2,alignx right");

		JSpinner spnPadChannel = new JSpinner();
		lblPadChannel.setLabelFor(spnPadChannel);
		spnPadChannel.setModel(new SpinnerNumberModel(1, 1, 16, 1));
		pnPadDetails.add(spnPadChannel, "cell 5 2");

		JPanel pnPadVelocity = new JPanel();
		pnPadDetails.add(pnPadVelocity, "cell 7 0 1 3,growx");
		pnPadVelocity.setLayout(new MigLayout("insets 0, gap 0", "[][]",
				"[][][]"));

		JLabel lblPadVelocity = new JLabel("Velocity");
		pnPadVelocity.add(lblPadVelocity, "cell 0 0 2 1,alignx center");

		JLabel lblPadVelMin = new JLabel("Min");
		pnPadVelocity.add(lblPadVelMin, "cell 0 1,alignx center");

		JSpinner spnPadVelMin = new JSpinner();
		lblPadVelMin.setLabelFor(spnPadVelMin);
		pnPadVelocity.add(spnPadVelMin, "cell 0 2");
		spnPadVelMin.setModel(new SpinnerNumberModel(127, null, 127, 1));

		JLabel lblPadVelMax = new JLabel("Max");
		pnPadVelocity.add(lblPadVelMax, "cell 1 1,alignx center");

		JSpinner spnPadVelMax = new JSpinner();
		lblPadVelMax.setLabelFor(spnPadVelMax);
		pnPadVelocity.add(spnPadVelMax, "cell 1 2");
		spnPadVelMax.setModel(new SpinnerNumberModel(127, null, 127, 1));

		JPanel pnFlags = new JPanel();
		pnPadDetails.add(pnFlags, "cell 9 0 1 3,growx");
		pnFlags.setLayout(new MigLayout("insets 0, gap 0", "[][][][][][][][]",
				"[][][]"));

		JLabel lblFlags = new JLabel("Flags");
		pnFlags.add(lblFlags, "cell 0 0 9 1,alignx center");

		JLabel lblFlag7 = new JLabel("7");
		pnFlags.add(lblFlag7, "cell 0 1,alignx center");

		JCheckBox cbxFlag7 = new JCheckBox("");
		cbxFlag7.setMargin(new Insets(0, 0, 0, 0));
		lblFlag7.setLabelFor(cbxFlag7);
		pnFlags.add(cbxFlag7, "cell 0 2");

		JLabel lblFlag6 = new JLabel("6");
		pnFlags.add(lblFlag6, "cell 1 1,alignx center");

		JCheckBox cbxFlag6 = new JCheckBox("");
		cbxFlag6.setMargin(new Insets(0, 0, 0, 0));
		lblFlag6.setLabelFor(cbxFlag6);
		pnFlags.add(cbxFlag6, "cell 1 2");

		JLabel lblFlag5 = new JLabel("5");
		pnFlags.add(lblFlag5, "cell 2 1,alignx center");

		JCheckBox cbxFlag5 = new JCheckBox("");
		cbxFlag5.setMargin(new Insets(0, 0, 0, 0));
		lblFlag5.setLabelFor(cbxFlag5);
		pnFlags.add(cbxFlag5, "cell 2 2");

		JLabel lblFlag4 = new JLabel("4");
		pnFlags.add(lblFlag4, "cell 3 1,alignx center");

		JCheckBox cbxFlag4 = new JCheckBox("");
		cbxFlag4.setMargin(new Insets(0, 0, 0, 0));
		lblFlag4.setLabelFor(cbxFlag4);
		pnFlags.add(cbxFlag4, "cell 3 2");

		JLabel lblFlag3 = new JLabel("3");
		pnFlags.add(lblFlag3, "cell 4 1,alignx center");

		JCheckBox cbxFlag3 = new JCheckBox("");
		cbxFlag3.setMargin(new Insets(0, 0, 0, 0));
		lblFlag3.setLabelFor(cbxFlag3);
		pnFlags.add(cbxFlag3, "cell 4 2");

		JLabel lblFlag2 = new JLabel("2");
		pnFlags.add(lblFlag2, "cell 5 1,alignx center");

		JCheckBox cbxFlag2 = new JCheckBox("");
		cbxFlag2.setMargin(new Insets(0, 0, 0, 0));
		lblFlag2.setLabelFor(cbxFlag2);
		pnFlags.add(cbxFlag2, "cell 5 2");

		JLabel lblFlag1 = new JLabel("1");
		pnFlags.add(lblFlag1, "cell 6 1,alignx center");

		JCheckBox cbxFlag1 = new JCheckBox("");
		cbxFlag1.setMargin(new Insets(0, 0, 0, 0));
		lblFlag1.setLabelFor(cbxFlag1);
		pnFlags.add(cbxFlag1, "cell 6 2");

		JLabel lblFlag0 = new JLabel("0");
		pnFlags.add(lblFlag0, "cell 7 1,alignx center");

		JCheckBox cbxFlag0 = new JCheckBox("");
		cbxFlag0.setMargin(new Insets(0, 0, 0, 0));
		lblFlag0.setLabelFor(cbxFlag0);
		pnFlags.add(cbxFlag0, "cell 7 2");

		JPanel pnGlobalPadDynamics = new JPanel();
		pnGlobalPadDynamics.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
				null, null));
		pnPadDetails.add(pnGlobalPadDynamics, "cell 4 3 6 4,aligny center");
		pnGlobalPadDynamics.setLayout(new MigLayout("insets 0,gapx 2, gapy 0",
				"[4px:n][][][][][][][][4px:n]", "[][][][4px:n]"));

		JLabel lblGlobalPadDynamics = new JLabel("Global Pad Dynamics");
		lblGlobalPadDynamics.setDisplayedMnemonic('b');
		pnGlobalPadDynamics.add(lblGlobalPadDynamics, "cell 1 0 7 1");

		JLabel lblLowlevel = new JLabel("lowLevel");
		pnGlobalPadDynamics.add(lblLowlevel, "cell 2 1,alignx right");

		JSpinner spnLowLevel = new JSpinner();
		lblLowlevel.setLabelFor(spnLowLevel);
		lblGlobalPadDynamics.setLabelFor(spnLowLevel);
		spnLowLevel.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnLowLevel, "cell 3 1");

		JLabel lblHighlevel = new JLabel("highLevel");
		pnGlobalPadDynamics.add(lblHighlevel, "cell 2 2,alignx right");

		JSpinner spnHighLevel = new JSpinner();
		lblHighlevel.setLabelFor(spnHighLevel);
		spnHighLevel.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnHighLevel, "cell 3 2");

		JLabel lblThresholdManual = new JLabel("thresholdManual");
		pnGlobalPadDynamics.add(lblThresholdManual, "cell 4 1,alignx right");

		JSpinner spnThresholdManual = new JSpinner();
		lblThresholdManual.setLabelFor(spnThresholdManual);
		spnThresholdManual.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnThresholdManual, "cell 5 1");

		JLabel lblThresholdActual = new JLabel("thresholdActual");
		pnGlobalPadDynamics.add(lblThresholdActual, "cell 4 2,alignx right");

		JSpinner spnThresholdActual = new JSpinner();
		lblThresholdActual.setLabelFor(spnThresholdActual);
		spnThresholdActual.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnThresholdActual, "cell 5 2");

		JLabel lblUserMargin = new JLabel("userMargin");
		pnGlobalPadDynamics.add(lblUserMargin, "cell 6 1,alignx right");

		JSpinner spnUserMargin = new JSpinner();
		lblUserMargin.setLabelFor(spnUserMargin);
		spnUserMargin.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnUserMargin, "cell 7 1");

		JLabel lblInternalMargin = new JLabel("internalMargin");
		pnGlobalPadDynamics.add(lblInternalMargin, "cell 6 2,alignx right");

		JSpinner spnInternalMargin = new JSpinner();
		lblInternalMargin.setLabelFor(spnInternalMargin);
		spnInternalMargin.setModel(new SpinnerNumberModel(199, null, 255, 1));
		pnGlobalPadDynamics.add(spnInternalMargin, "cell 7 2");

		JPanel pnLinkTo = new JPanel();
		pnLinkTo.setBorder(null);
		pnLinkTo.setLayout(new MigLayout("insets 5", "[grow,right][left,fill]",
				"[]"));

		JLabel lblLinkTo = new JLabel("Link To");
		lblLinkTo.setDisplayedMnemonic('L');
		pnLinkTo.add(lblLinkTo, "cell 0 0");

		JComboBox<String> cbxLinkTo = new JComboBox<String>();
		lblLinkTo.setLabelFor(cbxLinkTo);
		/* Dynamic per pad selected excluding "this pad": */
		cbxLinkTo.setModel(new DefaultComboBoxModel<String>(pads1_24));
		cbxLinkTo.setMaximumRowCount(25);
		cbxLinkTo.setMaximumSize(new Dimension(48, 32767));
		pnLinkTo.add(cbxLinkTo, "cell 1 0");

		pnPadDetails.add(pnLinkTo, "cell 1 5 2 1,alignx center,aligny center");

		JPanel pnMoreSlots = new JPanel();
		tpnKitPadsDetails.addTab("More Slots", null, pnMoreSlots, null);
		pnMoreSlots.setLayout(new MigLayout("insets 5, gapx 2, gapy 0",
				"[][16px:n,right][][16px:n][16px:n,right][100px:n][]",
				"[][][][][]"));

		JLabel lblMoreSlots = new JLabel("Slots:");
		pnMoreSlots.add(lblMoreSlots, "cell 0 0");

		JLabel lblSlot7 = new JLabel("7");
		lblSlot7.setDisplayedMnemonic('7');
		pnMoreSlots.add(lblSlot7, "cell 1 0");

		JComboBox<String> cbxSlot7 = new JComboBox<String>();
		lblSlot7.setLabelFor(cbxSlot7);
		cbxSlot7.setMaximumSize(new Dimension(80, 32767));
		cbxSlot7.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot7.setEditable(true);
		pnMoreSlots.add(cbxSlot7, "cell 2 0");

		JLabel lblSlot8 = new JLabel("8");
		pnMoreSlots.add(lblSlot8, "cell 1 1");
		lblSlot8.setDisplayedMnemonic('8');

		JComboBox<String> cbxSlot8 = new JComboBox<String>();
		lblSlot8.setLabelFor(cbxSlot8);
		cbxSlot8.setMaximumSize(new Dimension(80, 32767));
		cbxSlot8.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot8.setEditable(true);
		pnMoreSlots.add(cbxSlot8, "cell 2 1");

		JLabel lblSlot9 = new JLabel("9");
		pnMoreSlots.add(lblSlot9, "cell 1 2");
		lblSlot9.setDisplayedMnemonic('9');

		JComboBox<String> cbxSlot9 = new JComboBox<String>();
		lblSlot9.setLabelFor(cbxSlot9);
		pnMoreSlots.add(cbxSlot9, "cell 2 2");
		cbxSlot9.setMaximumSize(new Dimension(80, 32767));
		cbxSlot9.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot9.setEditable(true);

		JLabel lblSlot10 = new JLabel("10");
		lblSlot10.setDisplayedMnemonic('0');
		pnMoreSlots.add(lblSlot10, "cell 1 3");

		JComboBox<String> cbxSlot10 = new JComboBox<String>();
		lblSlot10.setLabelFor(cbxSlot10);
		cbxSlot10.setMaximumSize(new Dimension(80, 32767));
		cbxSlot10.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot10.setEditable(true);
		pnMoreSlots.add(cbxSlot10, "cell 2 3");

		JLabel lblSlot11 = new JLabel("11");
		lblSlot11.setDisplayedMnemonic('1');
		pnMoreSlots.add(lblSlot11, "cell 1 4");

		JComboBox<String> cbxSlot11 = new JComboBox<String>();
		lblSlot11.setLabelFor(cbxSlot11);
		cbxSlot11.setMaximumSize(new Dimension(80, 32767));
		cbxSlot11.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot11.setEditable(true);
		pnMoreSlots.add(cbxSlot11, "cell 2 4");

		JLabel lblSlot12 = new JLabel("12");
		lblSlot12.setDisplayedMnemonic('2');
		pnMoreSlots.add(lblSlot12, "cell 4 0");

		JComboBox<String> cbxSlot12 = new JComboBox<String>();
		lblSlot12.setLabelFor(cbxSlot12);
		cbxSlot12.setMaximumSize(new Dimension(80, 32767));
		cbxSlot12.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot12.setEditable(true);
		pnMoreSlots.add(cbxSlot12, "cell 5 0");

		JLabel lblSlot13 = new JLabel("13");
		lblSlot13.setDisplayedMnemonic('3');
		pnMoreSlots.add(lblSlot13, "cell 4 1");

		JComboBox<String> cbxSlot13 = new JComboBox<String>();
		lblSlot13.setLabelFor(cbxSlot13);
		pnMoreSlots.add(cbxSlot13, "cell 5 1");
		cbxSlot13.setMaximumSize(new Dimension(80, 32767));
		cbxSlot13.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot13.setEditable(true);

		JLabel lblSlot14 = new JLabel("14");
		lblSlot14.setDisplayedMnemonic('4');
		pnMoreSlots.add(lblSlot14, "cell 4 2");

		JComboBox<String> cbxSlot14 = new JComboBox<String>();
		lblSlot14.setLabelFor(cbxSlot14);
		cbxSlot14.setMaximumSize(new Dimension(80, 32767));
		cbxSlot14.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot14.setEditable(true);
		pnMoreSlots.add(cbxSlot14, "cell 5 2");

		JLabel lblSlot15 = new JLabel("15");
		lblSlot15.setDisplayedMnemonic('5');
		pnMoreSlots.add(lblSlot15, "cell 4 3");

		JComboBox<String> cbxSlot15 = new JComboBox<String>();
		lblSlot15.setLabelFor(cbxSlot15);
		cbxSlot15.setMaximumSize(new Dimension(80, 32767));
		cbxSlot15.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot15.setEditable(true);
		pnMoreSlots.add(cbxSlot15, "cell 5 3");

		JLabel lblSlot16 = new JLabel("16");
		lblSlot16.setDisplayedMnemonic('6');
		pnMoreSlots.add(lblSlot16, "cell 4 4");

		JComboBox<String> cbxSlot16 = new JComboBox<String>();
		lblSlot16.setLabelFor(cbxSlot16);
		cbxSlot16.setMaximumSize(new Dimension(80, 32767));
		cbxSlot16.setModel(new DefaultComboBoxModel<String>(padCbxItems));
		cbxSlot16.setEditable(true);
		pnMoreSlots.add(cbxSlot16, "cell 5 4");

		JPanel pnKitDetails = new JPanel();
		tpnKitPadsDetails.addTab("Kit Details", null, pnKitDetails, null);
		pnKitDetails.setLayout(new MigLayout("insets 5, gapx 2, gapy 0", "[][][][16px:n][][16px:n][][][][4px:n][][][]", "[][][][24px:n][][][]"));

		JLabel lblKitCurve = new JLabel("Curve:");
		lblKitCurve.setDisplayedMnemonic('C');
		pnKitDetails.add(lblKitCurve, "cell 0 0,alignx right");

		JPanel pnKitCurve = new JPanel();
		pnKitCurve.setLayout(new MigLayout("insets 0,gap 0", "[][]", "[]"));
		pnKitDetails.add(pnKitCurve, "cell 1 0 2 1,grow");

		JComboBox<String> cbxKitCurve = new JComboBox<String>();
		pnKitCurve.add(cbxKitCurve, "cell 0 0");
		lblKitCurve.setLabelFor(cbxKitCurve);
		cbxKitCurve.setModel(new DefaultComboBoxModel<String>(padCurves));

		JCheckBox ckbVarCurve = new JCheckBox("Various");
		pnKitCurve.add(ckbVarCurve, "cell 1 0");

		JLabel lblKitGate = new JLabel("Gate:");
		lblKitGate.setDisplayedMnemonic('G');
		pnKitDetails.add(lblKitGate, "cell 0 1,alignx right");

		JPanel pnKitGate = new JPanel();
		pnKitGate.setLayout(new MigLayout("insets 0,gap 0", "[][]", "[]"));
		pnKitDetails.add(pnKitGate, "cell 1 1 2 1,grow");

		JComboBox<String> cbxKitGate = new JComboBox<String>();
		lblKitGate.setLabelFor(cbxKitGate);
		cbxKitGate.setEditable(true);
		cbxKitGate.setModel(new DefaultComboBoxModel<String>(padGates));
		pnKitGate.add(cbxKitGate, "cell 0 0");

		JCheckBox ckbVarGate = new JCheckBox("Various");
		pnKitGate.add(ckbVarGate, "cell 1 0");

		JLabel lblKitChannel = new JLabel("Channel:");
		lblKitChannel.setDisplayedMnemonic('h');
		pnKitDetails.add(lblKitChannel, "cell 0 2");

		JPanel pnKitChannel = new JPanel();
		pnKitChannel.setLayout(new MigLayout("insets 0,gap 0", "[][]", "[]"));
		pnKitDetails.add(pnKitChannel, "cell 1 2 2 1,grow");

		JSpinner spnKitChannel = new JSpinner();
		lblKitChannel.setLabelFor(spnKitChannel);
		spnKitChannel.setModel(new SpinnerNumberModel(1, 1, 16, 1));
		pnKitChannel.add(spnKitChannel, "cell 0 0");

		JCheckBox ckbVarChannel = new JCheckBox("Various");
		pnKitChannel.add(ckbVarChannel, "cell 1 0");

		JLabel lblFootController = new JLabel("Foot Controller");
		pnKitDetails.add(lblFootController,
				"cell 0 3 2 1,alignx center,aligny bottom");

		JLabel lblFCFunction = new JLabel("Function:");
		lblFCFunction.setDisplayedMnemonic('i');
		pnKitDetails.add(lblFCFunction, "cell 0 4,alignx right");

		JComboBox<String> cbxFCFunction = new JComboBox<String>();
		lblFCFunction.setLabelFor(cbxFCFunction);
		cbxFCFunction.setModel(new DefaultComboBoxModel<String>(fcFunctions));
		pnKitDetails.add(cbxFCFunction, "cell 1 4");

		JLabel lblFCChannel = new JLabel("Channel:");
		lblFCChannel.setDisplayedMnemonic('n');
		pnKitDetails.add(lblFCChannel, "cell 0 5,alignx right");

		JPanel pnFCChannel = new JPanel();
		pnFCChannel.setLayout(new MigLayout("insets 0,gap 0", "[][]", "[]"));
		pnKitDetails.add(pnFCChannel, "cell 1 5,grow");

		JSpinner spnFCChannel = new JSpinner();
		lblFCChannel.setLabelFor(spnFCChannel);
		spnFCChannel.setModel(new SpinnerNumberModel(1, 1, 16, 1));
		pnFCChannel.add(spnFCChannel, "cell 0 0");

		JCheckBox ckbFCChannel = new JCheckBox("Same as Chick");
		pnFCChannel.add(ckbFCChannel, "cell 1 0");

		JLabel lblFCCurve = new JLabel("Curve:");
		lblFCCurve.setDisplayedMnemonic('v');
		pnKitDetails.add(lblFCCurve, "cell 0 6,alignx right");

		JComboBox<String> cbxFCCurve = new JComboBox<String>();
		lblFCCurve.setLabelFor(cbxFCCurve);
		cbxFCCurve.setModel(new DefaultComboBoxModel<String>(fcCurves));
		pnKitDetails.add(cbxFCCurve, "cell 1 6");

		JPanel pnKitVelocity = new JPanel();
		pnKitDetails.add(pnKitVelocity, "cell 4 0 1 3,growx");
		pnKitVelocity.setLayout(new MigLayout("insets 0, gap 0", "[][]", "[][][][]"));

		JLabel lblKitVelocity = new JLabel("Velocity");
		pnKitVelocity.add(lblKitVelocity, "cell 0 0 2 1,alignx center");

		JLabel lblKitVelMin = new JLabel("Min");
		pnKitVelocity.add(lblKitVelMin, "cell 0 1,alignx center");

		JSpinner spnKitVelMin = new JSpinner();
		lblKitVelMin.setLabelFor(spnKitVelMin);
		pnKitVelocity.add(spnKitVelMin, "cell 0 2");
		spnKitVelMin.setModel(new SpinnerNumberModel(127, null, 127, 1));

		JCheckBox ckbVarVelMin =  new JCheckBox("Var.");
		pnKitVelocity.add(ckbVarVelMin, "cell 0 3");

		JLabel lblKitVelMax = new JLabel("Max");
		pnKitVelocity.add(lblKitVelMax, "cell 1 1,alignx center");

		JSpinner spnKitVelMax = new JSpinner();
		lblKitVelMax.setLabelFor(spnKitVelMax);
		pnKitVelocity.add(spnKitVelMax, "cell 1 2");
		spnKitVelMax.setModel(new SpinnerNumberModel(127, null, 127, 1));

		JCheckBox ckbVarVelMax =  new JCheckBox("Var.");
		pnKitVelocity.add(ckbVarVelMax, "cell 1 3");
		
		JPanel pnSoundControl = new JPanel();
		pnKitDetails.add(pnSoundControl, "cell 6 0 7 1,alignx center,growy");
		pnSoundControl.setLayout(new MigLayout("insets 0,gapx 2, gapy 0", "[][grow]", "[]"));
		
		JLabel lblSoundControl = new JLabel("Sound Control:");
		pnSoundControl.add(lblSoundControl, "cell 0 0,alignx trailing");
		
		JComboBox<String> cbxSoundControl = new JComboBox<String>();
		lblSoundControl.setLabelFor(cbxSoundControl);
		cbxSoundControl.setMinimumSize(new Dimension(32, 20));
		cbxSoundControl.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4"}));
		pnSoundControl.add(cbxSoundControl, "cell 1 0");
		
		JLabel lblVolume = new JLabel("Volume:");
		pnKitDetails.add(lblVolume, "cell 6 1,alignx right");
		
		JSpinner spnVolume = new JSpinner();
		lblVolume.setLabelFor(spnVolume);
		spnVolume.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnVolume, "cell 7 1");
		
		JCheckBox ckbNoVolume = new JCheckBox("Off");
		ckbNoVolume.setMargin(new Insets(0, 0, 0, 0));
		pnKitDetails.add(ckbNoVolume, "cell 8 1");
		
		JLabel lblPrgChg = new JLabel("PrgChg:");
		pnKitDetails.add(lblPrgChg, "cell 6 2,alignx right");
		
		JSpinner spnPrgChg = new JSpinner();
		lblPrgChg.setLabelFor(spnPrgChg);
		spnPrgChg.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnPrgChg, "cell 7 2");
		
		JCheckBox ckbNoPrgChg = new JCheckBox("Off");
		ckbNoPrgChg.setMargin(new Insets(0, 0, 0, 0));
		pnKitDetails.add(ckbNoPrgChg, "cell 8 2");
		
		JLabel lblPrgChgTxmChn = new JLabel("TxmChn:");
		pnKitDetails.add(lblPrgChgTxmChn, "cell 6 3,alignx right");
		
		JSpinner spnPrgChgTxmChn = new JSpinner();
		lblPrgChgTxmChn.setLabelFor(spnPrgChgTxmChn);
		spnPrgChgTxmChn.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnPrgChgTxmChn, "cell 7 3");
		
		JLabel lblBankMSB = new JLabel("MSB:");
		pnKitDetails.add(lblBankMSB, "cell 10 1,alignx right");
		
		JSpinner spnBankMSB = new JSpinner();
		lblBankMSB.setLabelFor(spnBankMSB);
		spnBankMSB.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnBankMSB, "cell 11 1");
		
		JCheckBox ckbNoBankMSB = new JCheckBox("Off");
		ckbNoBankMSB.setMargin(new Insets(0, 0, 0, 0));
		pnKitDetails.add(ckbNoBankMSB, "cell 12 1");
		
		JLabel lblBankLSB = new JLabel("LSB:");
		pnKitDetails.add(lblBankLSB, "cell 10 2,alignx right");
		
		JSpinner spnBankLSB = new JSpinner();
		lblBankLSB.setLabelFor(spnBankLSB);
		spnBankLSB.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnBankLSB, "cell 11 2");
		
		JCheckBox ckbNoBankLSB = new JCheckBox("Off");
		ckbNoBankLSB.setMargin(new Insets(0, 0, 0, 0));
		pnKitDetails.add(ckbNoBankLSB, "cell 12 2");
		
		JLabel lblBank = new JLabel("Bank:");
		pnKitDetails.add(lblBank, "cell 10 3,alignx right");

		JSpinner spnBank = new JSpinner();
		lblBank.setLabelFor(spnBank);
		spnBank.setModel(new SpinnerNumberModel(127, null, 127, 1));
		pnKitDetails.add(spnBank, "cell 11 3");
		
		JCheckBox ckbNoBank = new JCheckBox("Off");
		ckbNoBank.setMargin(new Insets(0, 0, 0, 0));
		pnKitDetails.add(ckbNoBank, "cell 12 3");

		JPanel pnGlobal = new JPanel();
		tpnMain.addTab("Global", null, pnGlobal, null);
		pnGlobal.setLayout(new MigLayout("insets 5", "[]", "[]"));
		
		JLabel lblBeingReworked = new JLabel("Being reworked");
		pnGlobal.add(lblBeingReworked, "cell 0 0");

		JLabel lblabel1 = new JLabel(
				"MIDI OX SysEx Transmit: 512 bytes, 8 buffers, 160ms between buffers, 320ms after F7");
		frmTrapkatSysexEditor.getContentPane().add(lblabel1,
				"cell 0 1,alignx center");
	}

}

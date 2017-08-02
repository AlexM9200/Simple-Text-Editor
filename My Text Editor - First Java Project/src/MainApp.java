import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;

public class MainApp extends JFrame implements Runnable {

	private final String[] fileMenuItemNames = { "New", "Open", "Save", "Save As", "Close" };

	private final String[] iconStrings = { "newIcon.png", "openIcon.gif", "iconSave.gif", "undoBtn.png", "redoBtn.png",
			"zoomInIcon.png", "zoomOutIcon.png", "closeIcon.png" };

	private final String[] editMenuItemNames = { "Undo", "Redo", "Cut", "Copy", "Paste", "Delete", "Select All",
			"Set Font", "Print Date and Time" };

	private final int[] editMenuShortcuts = { KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_DELETE, KeyEvent.VK_C,
			KeyEvent.VK_V, KeyEvent.VK_D, KeyEvent.VK_A, KeyEvent.VK_F9, KeyEvent.VK_1 };

	private final int NO_OF_TOOLBAR_BUTTONS = 8;
	private final int NO_OF_FILE_MENUITEMS = 5;
	private final int NO_OF_EDIT_MENUITEMS = 9;
	
	private static final long serialVersionUID = 1L;
	private JFileChooser fileChooser;
	private UndoManager manager;
	private PrintStream printStream;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JPanel statusBar;
	private JLabel rowAndColumnDisplay;
	private JFontChooser fontChooser;
	private JToolBar tools;
	private JButton[] toolbarBtnsArray;
	private JMenu fileMenu;
	private JMenuItem[] fileMenuItems;
	private JMenuItem[] editMenuItems;
	private JMenu editMenu;

	public MainApp() {
		super("Alex's Text Editor");
		fileChooser = new JFileChooser();
		manager = new UndoManager();
		textArea = new JTextArea();
		scrollPane = new JScrollPane(textArea);
		menuBar = new JMenuBar();
		fontChooser = new JFontChooser();
	}

	public void run() {
		makeGUI();
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			showSomethingWentWrongWarning();
		}
	}

	public void makeGUI() {
		frameSetup();
		makeStatusBar();
		makeToolbar();
		makeAndAddFileMenu();
		makeAndAddEditMenu();
		makeAndAddViewMenu();
		makeAndAddHelpMenu();
	}

	private void frameSetup() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1000, 500);
		this.setLocationRelativeTo(null);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage("myAppIcon.jpg"));
		this.add(scrollPane);
		this.setVisible(true);
	}

	public void makeStatusBar() {
		statusBar = new JPanel(new BorderLayout());
		statusBar.setForeground(Color.BLUE);
		statusBar.setPreferredSize(new Dimension(this.getWidth(), 20));
		statusBar.setVisible(false);
		
		rowAndColumnDisplay = new JLabel();
		rowAndColumnDisplay.setText("Row: " + 0 + "  Column: " + 0);
		textArea.addCaretListener(new MyCaretListener());
		statusBar.add(rowAndColumnDisplay);
		this.add(statusBar, BorderLayout.SOUTH);

	}

	public void makeToolbar() {
		tools = new JToolBar();
		addButtonsToToolbar();
		setToolbarBtnActions();
		this.add(tools, BorderLayout.PAGE_START);
	}

	private void addButtonsToToolbar() {
		toolbarBtnsArray = new JButton[NO_OF_TOOLBAR_BUTTONS];
		for (int i = 0; i < NO_OF_TOOLBAR_BUTTONS; i++) {
			toolbarBtnsArray[i] = makeImageButton(iconStrings[i]);
			tools.add(toolbarBtnsArray[i]);
			if (i == 2 || i == 4 || i == 6) {
				tools.addSeparator();
			}
		}
	}

	private static JButton makeImageButton(String imageName) {
		JButton button = new JButton();
		ImageIcon icon = new ImageIcon(imageName);
		button.setIcon(icon);
		return button;
	}

	private void setToolbarBtnActions() {
		ToolbarBtnListener toolbarBtnListener = new ToolbarBtnListener();
		for (int i = 0; i < NO_OF_TOOLBAR_BUTTONS; i++) {
			toolbarBtnsArray[i].addActionListener(toolbarBtnListener);
		}
	}

	public void makeAndAddFileMenu() {
		fileMenu = new JMenu("File");
		addFileMenuItems();
		setFileMenuItemActions();
		menuBar.add(fileMenu);
	}

	private void addFileMenuItems() {
		fileMenuItems = new JMenuItem[NO_OF_FILE_MENUITEMS];
		for (int i = 0; i < NO_OF_FILE_MENUITEMS; i++) {
			fileMenuItems[i] = new JMenuItem(fileMenuItemNames[i]);
			fileMenuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i, ActionEvent.ALT_MASK));
			fileMenu.add(fileMenuItems[i]);
		}
	}

	private void setFileMenuItemActions() {
		FileMenuItemListener fileMenuItemListener = new FileMenuItemListener();
		for (int i = 0; i < NO_OF_FILE_MENUITEMS; i++) {
			fileMenuItems[i].addActionListener(fileMenuItemListener);
		}
	}

	public void makeAndAddEditMenu() {
		editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		addEditMenuItems();
		setEditMenuItemActions();
		textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {

			public void undoableEditHappened(UndoableEditEvent e) {
				manager.addEdit(e.getEdit());
			}
		}); 
	}

	private void addEditMenuItems() {
		editMenuItems = new JMenuItem[NO_OF_EDIT_MENUITEMS];
		for (int i = 0; i < NO_OF_EDIT_MENUITEMS; i++) {
			editMenuItems[i] = new JMenuItem(editMenuItemNames[i]);
			editMenuItems[i].setAccelerator(KeyStroke.getKeyStroke(editMenuShortcuts[i], ActionEvent.ALT_MASK));
			editMenu.add(editMenuItems[i]);
		}
	}

	private void setEditMenuItemActions() {
		EditMenuItemListener editMenuItemListener = new EditMenuItemListener();
		for (int i = 0; i < NO_OF_EDIT_MENUITEMS; i++) {
			editMenuItems[i].addActionListener(editMenuItemListener);
		}
	}

	public void makeAndAddHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		JMenuItem menuItem = new JMenuItem("Get Help");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI("http://www.google.com"));
					} catch (IOException | URISyntaxException e) {
						showSomethingWentWrongWarning();
					}
				}
			}
		});
		helpMenu.add(menuItem);
		menuBar.add(helpMenu);
	}

	public void makeAndAddViewMenu() {
		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);
		JToggleButton statusBarToggle = new JToggleButton("Status Bar");
		statusBarToggle.setSelected(false);
		statusBarToggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (statusBarToggle.isSelected() == true) {
					statusBar.setVisible(true);
				} else {
					statusBar.setVisible(false);
				}
			}
		});
		viewMenu.add(statusBarToggle);
		setJMenuBar(menuBar);
	}

	public void showFileNotFoundWarning() {
		JOptionPane.showMessageDialog(this, "File not found!", "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void showSomethingWentWrongWarning() {
		JOptionPane.showMessageDialog(this, "Something went wrong!", "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void close() {
		Object[] options = { "Yes", "Cancel" };
		int choice = JOptionPane.showOptionDialog(this, "Are you sure you want to close Alex's Text Editor?",
				"Confirm Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
				options[1]);
		if (choice == 0) 
			dispose();
	}

	private class ToolbarBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == toolbarBtnsArray[0]) {
				newAction();
			} else if (e.getSource() == toolbarBtnsArray[1]) {
				open();
			} else if (e.getSource() == toolbarBtnsArray[2]) {
				save();
			} else if (e.getSource() == toolbarBtnsArray[3]) {
				undo();
			} else if (e.getSource() == toolbarBtnsArray[4]) {
				redo();
			} else if (e.getSource() == toolbarBtnsArray[5]) {
				fontChooser.setSelectedFontSize(fontChooser.getSelectedFontSize() + 2);
				textArea.setFont(fontChooser.getSelectedFont());
			} else if (e.getSource() == toolbarBtnsArray[6]) {
				fontChooser.setSelectedFontSize(fontChooser.getSelectedFontSize() - 2);
				textArea.setFont(fontChooser.getSelectedFont());
			} else if (e.getSource() == toolbarBtnsArray[7]) {
				close();
			}
		}
	}

	private class FileMenuItemListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == fileMenuItems[0]) {
				newAction();
			} else if (e.getSource() == fileMenuItems[1]) {
				open();
			} else if (e.getSource() == fileMenuItems[2]) {
				save();
			} else if (e.getSource() == fileMenuItems[3]) {
				saveAs();
			} else if (e.getSource() == fileMenuItems[4]) {
				close();
			}
		}
	}
	
	public void newAction() {
		Object[] options = { "Yes", "No", "Cancel" };
		int choice = JOptionPane.showOptionDialog(this, "Do you want to save the current file?", "Save file?",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
		if (choice == 0) {
			saveAs();
		}
		textArea.setText(null);
	}
	
	public void open() {
		try {
			tryToOpen();
		} catch (FileNotFoundException e) {
			showFileNotFoundWarning();
		} catch (IOException e) {
			showSomethingWentWrongWarning();
		}
	}

	public void tryToOpen() throws FileNotFoundException, IOException {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			textArea.read(br, null);
			br.close();
		}
	}
	
	private void save() {
		try {
			tryToSave();
		} catch (FileNotFoundException e) {
			showFileNotFoundWarning();
		} catch (IOException e) {
			showSomethingWentWrongWarning();
		}
	}

	public void tryToSave() throws FileNotFoundException, IOException {
		if (fileChooser.getSelectedFile() == null) {
			saveAs();
		} else {
			printStream = new PrintStream(new FileOutputStream(fileChooser.getSelectedFile()));
			printStream.write(textArea.getText().getBytes());
			printStream.close();
		}
	}
	
	public void saveAs() {
		try {
			tryToSaveAs();
		} catch (FileNotFoundException e) {
			showFileNotFoundWarning();
		} catch (IOException e) {
			showSomethingWentWrongWarning();
		}

	}

	public void tryToSaveAs() throws FileNotFoundException, IOException {
		int returnVal = fileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getCurrentDirectory().toString() + "/" + fileChooser.getSelectedFile().getName());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			printStream = new PrintStream(new FileOutputStream(file));
			printStream.write(textArea.getText().getBytes());
			printStream.close();
		}
	}

	private class EditMenuItemListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == editMenuItems[0]) {
				undo();
			} else if (e.getSource() == editMenuItems[1]) {
				redo();
			} else if (e.getSource() == editMenuItems[2]) {
				textArea.cut();
			} else if (e.getSource() == editMenuItems[3]) {
				textArea.copy();
			} else if (e.getSource() == editMenuItems[4]) {
				textArea.paste();
			} else if (e.getSource() == editMenuItems[5]) {
				textArea.setText(textArea.getText().replace(textArea.getSelectedText(), ""));
			} else if (e.getSource() == editMenuItems[6]) {
				textArea.select(0, textArea.getText().length());
			} else if (e.getSource() == editMenuItems[7]) {
				setFont();
			} else if (e.getSource() == editMenuItems[8]) {
				formatAndPrintDate();

			}
		}
	}
	
	public void undo() {
		if (manager.canUndo()) 
			manager.undo();
	}

	public void redo() {
		if (manager.canRedo()) 
			manager.redo();
	}
	
	public void setFont() {
		int result = fontChooser.showDialog(this);
		if (result == JFontChooser.OK_OPTION) {
			Font font = fontChooser.getSelectedFont();
			textArea.setFont(font);
			System.out.println("Selected Font : " + font);
		}
	}
	
	private void formatAndPrintDate() {
		DateTimeFormatter formatter2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);
		String formattedDateAndTime = LocalDateTime.now().format(formatter2);
		textArea.append(formattedDateAndTime);
	}
	
	private class MyCaretListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			int currentRow = getRow(e.getDot(), (JTextComponent) e.getSource());
			int currentCol = getColumn(e.getDot(), (JTextComponent) e.getSource());
			rowAndColumnDisplay.setText("Row: " + currentRow + "  Column: " + currentCol);
		}
	}

	private int getRow(int position, JTextComponent editor) {
		int rowNumber = (position == 0) ? 1 : 0;
		try {
			int offset = position;
			while (offset > 0) {
				offset = Utilities.getRowStart(editor, offset) - 1;
				rowNumber++;
			}
		} catch (BadLocationException e) {
			showSomethingWentWrongWarning();
		}
		return rowNumber;
	}

	private int getColumn(int position, JTextComponent editor) {
		try {
			return position - Utilities.getRowStart(editor, position) + 1;
		} catch (BadLocationException e) {
			showSomethingWentWrongWarning();
		}
		return -1;
	}
	
	public static void main(String[] args) {
		MainApp app = new MainApp();
		javax.swing.SwingUtilities.invokeLater(app);
	}

}

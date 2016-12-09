package com.sfauto.device.builder;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sfauto.device.Device;
import com.sfauto.device.DeviceFactory;

public class DevicePanel extends JPanel implements ActionListener {
	public static Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();

	public static ReferenceEditor refEditor = null;
	public static ReferenceRenderer refRender = null;

	public JTable table;
	private JButton btnSave;

	private JScrollPane scrollPane;
	public DeviceModel model;

	private RowHeader rowHeader;
	private HeaderRenderer mlh;
	private RowBackgroundRenderer rowRenderer;

	private Color colorLightYellow = new Color(218, 255, 218);
	private Color colorGridLine = new Color(120, 186, 186);	
	
	JFrame frameWindow = null;
	Device device = null;
	DeviceBuilder builder;

	public DevicePanel(JFrame frame, DeviceBuilder builder,
			PointSelector pointSelector, Device device) {
		this.frameWindow = frame;
		this.builder = builder;

		if (refEditor == null) {
			refEditor = new ReferenceEditor(frame, pointSelector);
		}

		if (refRender == null) {
			refRender = new ReferenceRenderer(pointSelector, colorLightYellow,
					Color.BLACK, true);
		}

		this.device = device;

		build();
	}

	public void build() {
		table = new JTable();
		table.setFont(new Font("新宋体", 0, 15));
		table.setRowHeight(24);
		table.setBackground(Color.WHITE);
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(table.AUTO_RESIZE_OFF);

		btnSave = new JButton("保存");		
		btnSave.addActionListener(this);
		
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		table.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);

	    KeyStroke delete = KeyStroke.getKeyStroke((char) KeyEvent.VK_DELETE);;
		table.registerKeyboardAction(this, "Delete", delete, JComponent.WHEN_FOCUSED);
		
		rowRenderer = new RowBackgroundRenderer(colorLightYellow, Color.BLACK,
				true);
		rowRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.setGridColor(colorGridLine);

		rowHeader = new RowHeader(table);

		mlh = new HeaderRenderer();

		JTableHeader header = table.getTableHeader();
		header.addMouseListener(new ColumnHeaderListener());

		table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(java.awt.event.MouseEvent e) {
				rowHeader.clearSelection();
			}

			public void mousePressed(java.awt.event.MouseEvent e) {
			}
		});

		setLayout(new BorderLayout());
		scrollPane = new JScrollPane(table);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setCorner(scrollPane.UPPER_LEFT_CORNER, btnSave);

		add(scrollPane, BorderLayout.CENTER);

		buildInternal();
	}

	public void notifySave(boolean isDirty) {
		if(isDirty){
			btnSave.setForeground(Color.red);
			btnSave.setToolTipText("点击保存配置信息");
			builder.deviceTree.revalidate();
			builder.deviceTree.repaint();
		}else{
			btnSave.setForeground(Color.black);
			btnSave.setToolTipText("已保存");
		}
	}

	public void buildInternal() {
		rowHeader.clearSelection();

		model = new DeviceModel(this, device);
		table.setModel(model);

		TableColumnModel tcm = table.getColumnModel();
		java.util.Enumeration en = tcm.getColumns();
		int i = 0;
		while (en.hasMoreElements()) {
			TableColumn tc = (TableColumn) en.nextElement();
			if (i == 2) {
				tc.setCellRenderer(refRender);
				tc.setCellEditor(refEditor);
			} else {
				tc.setCellRenderer(rowRenderer);
			}
			tc.setHeaderRenderer(mlh);
			packColumn(table, i);
			i++;
		}

		table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
		scrollPane.setRowHeaderView(rowHeader);

		revalidate();
	}

	public static void packColumn(JTable ptable, int vColIndex) {
		DefaultTableColumnModel colModel = (DefaultTableColumnModel) ptable
				.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);

		int width = 0;
		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null) {
			renderer = ptable.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(ptable,
				col.getHeaderValue(), false, false, 0, 0);
		width = comp.getPreferredSize().width;

		// Get maximum width of column data
		for (int r = 0; r < ptable.getRowCount(); r++) {
			renderer = ptable.getCellRenderer(r, vColIndex);
			comp = renderer
					.getTableCellRendererComponent(ptable,
							ptable.getValueAt(r, vColIndex), false, false, r,
							vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}
		col.setMinWidth(4);
		// col.setMaxWidth(width * 3);
		col.setPreferredWidth(width + 10);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Object source = e.getSource();
		if (source == btnSave) {
			if (model.isDirty) {
				DeviceFactory.getInstance().saveModel(device);
				model.isDirty = false;
				this.notifySave(false);
			}
		}
		if (command != null) {
			if (command.equals("Paste")) {
				pasteData();
			}else if(command.equals("Delete")){
				deleteConfig();
			}
		}
	}

	void deleteConfig(){
		int[] rows = table.getSelectedRows();
		int[] cols = table.getSelectedColumns();
		
		if(rows == null || cols == null){
			return;
		}
		
		boolean isColumnConfigSelected = false;
		for(int col:cols){
			if(col == 2){
				isColumnConfigSelected = true;
				break;
			}
		}
		
		if(isColumnConfigSelected){
			for(int row:rows){
				table.setValueAt((Integer)0, row, 2);
			}
			table.revalidate();
			table.repaint();
		}
	}
	
	private void pasteData() {
		int startRow = (table.getSelectedRows())[0];
		int startCol = (table.getSelectedColumns())[0];
		Object objValue = null;
		
		String trstring = null,rowstring,value;
		try {
			trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
		} catch (Exception ex2) {
		}
		if (trstring != null) {
			String[] strs = com.sfauto.toolkits.utils.Utils.seperateStringNoTrim(trstring, "\n");
			trstring = "";
			for (int i = 0; i < strs.length; i++) {
				if (strs[i].length() > 0) {
					trstring += strs[i];
				} else {
					trstring += "null";
				}
				if (i < strs.length - 1) {
					trstring += "\n";
				}
			}
			StringTokenizer st1 = new StringTokenizer(trstring, "\n");
			for (int i = 0; st1.hasMoreTokens(); i++) { // 行
				rowstring = st1.nextToken();
				if (rowstring.equals("null")) {
					continue;
				}
				strs = com.sfauto.toolkits.utils.Utils.seperateStringNoTrim(rowstring, "\t");
				rowstring = "";
				for (int m = 0; m < strs.length; m++) {
					if (strs[m].length() > 0) {
						rowstring += strs[m];
					} else {
						rowstring += "null";
					}
					if (m < strs.length - 1) {
						rowstring += "\t";
					}
				}
				StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
				int column_count = st2.countTokens();
				boolean bSeted = false;
				int k = startCol;
				if (startRow + i < 0 || startRow + i >= model.getRowCount()) {
					break;
				}
				for (int j = 0; st2.hasMoreTokens(); j++) { 
					value = (String) st2.nextToken();					
					if (value.equals("null")) {
						k++;
						continue;
					}
					if (k == 2) {
						int key = Integer.parseInt(value);
						model.setValueAt(key, startRow + i, k);
					}
					k++;
				}
			}
			table.revalidate();
			table.repaint();
		}
	}
	
	private class ColumnHeaderListener extends MouseAdapter {
		public void mouseClicked(MouseEvent evt) {
			int vColIndex = table.columnAtPoint(new Point(evt.getX(), evt
					.getY()));
			if (evt.getModifiers() == evt.BUTTON1_MASK) {
				table.clearSelection();
				if (vColIndex == -1) {
					table.selectAll();
					return;
				}
				table.setColumnSelectionInterval(vColIndex, vColIndex);
				table.setRowSelectionInterval(0, table.getRowCount() - 1);
				rowHeader.clearSelection();
			}
		}
	}
}

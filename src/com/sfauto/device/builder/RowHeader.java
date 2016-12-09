package com.sfauto.device.builder;

import javax.swing.*;

import java.awt.*;

import javax.swing.table.*;

public class RowHeader extends JTable {
	private JTable mainTable;

	public RowHeader(JTable table) {
		this.mainTable = table;
		setModel(new RowHeader_TableModel());
		setRowHeight(mainTable.getRowHeight());
		setRowSelectionAllowed(true);
		// setColumnSelectionAllowed(true);
		//setDefaultRenderer(Integer.class, new ButtonRenderer());

        JTableHeader header = table.getTableHeader();
        setBackground(header.getBackground());

		this.setFont(mainTable.getFont());
		LookAndFeel.installBorder(this, "TableHeader.cellBorder");
		setPreferredScrollableViewportSize(new Dimension(60, 50));
	}

	private class RowHeader_TableModel extends
			javax.swing.table.AbstractTableModel {

		public RowHeader_TableModel() {

		}

		public int getColumnCount() {
			return 1;
		}

		public String getColumnName(int col) {
			return "ÐòºÅ";
		}

		public int getRowCount() {
			return mainTable.getRowCount();
		}

		public Object getValueAt(int row, int col) {
			Integer integer = new Integer(row + 1);
			return integer;
		}

		public Class getColumnClass(int col) {
			Integer integer = new Integer(0);
			return integer.getClass();
		}

		public boolean isCellEditable(int row, int col) {
			boolean flag = false;
			return flag;
		}
	}
}

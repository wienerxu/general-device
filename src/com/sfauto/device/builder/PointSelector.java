package com.sfauto.device.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sfauto.device.DVariable;
import com.sfauto.device.DeviceProperty;
import com.sfauto.realdb.DBTable;
import com.sfauto.realdb.IDUtils;
import com.sfauto.realdb.JRDBSet;
import com.sfauto.realdb.ScadaDefine;
import com.sfauto.realdb.record.RealDBDefine;
import com.sfauto.realdb.record.Record_SCADA_DPU;
import com.sfauto.realdb.record.Record_SCADA_IO;
import com.sfauto.realdb.record.TableRecord;
import com.sfauto.toolkits.utils.NamedValue;
import com.sfauto.toolkits.utils.RM;

public class PointSelector extends SelectablePanel implements ActionListener{
	JSplitPane splitPane = null;
	JButton buttonOk,buttonCancel;

	JTree objTree = null;
	JTable dataTable = new JTable();
	DataModel dataModel = null;

	// tree
	DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(RM.gs("全部单元"));
	DefaultTreeModel treeModel = null;
	ImageIcon iconDPU = null;
	ImageIcon iconPoint = null;

	JTextField searchInput = null;
	JLabel labelInfo = new JLabel();
	
	boolean isSearch = false;
	
	DBTable tableDPU = JRDBSet.getInstance().getTable(RealDBDefine.dbID_SCADA,
			RealDBDefine.tableID_SCADA_DPU);

	DBTable tableAnalog = JRDBSet.getInstance().getTable(
			RealDBDefine.dbID_SCADA, RealDBDefine.tableID_SCADA_Analog);

	DBTable tableDigit = JRDBSet.getInstance().getTable(
			RealDBDefine.dbID_SCADA, RealDBDefine.tableID_SCADA_Digit);

	DBTable tablePulse = JRDBSet.getInstance().getTable(
			RealDBDefine.dbID_SCADA, RealDBDefine.tableID_SCADA_Pulse);

	int sample_type;
	int point_key = 0;
	int select_key = 0;
	
	public PointSelector() {
		iconDPU = createImageIcon("rename.gif");
		iconPoint = createImageIcon("edit.gif");
		
		buildContent();
	}

	private ImageIcon createImageIcon(String filename) {
		String path = "images/" + filename;
		return new ImageIcon(getClass().getResource(path));
	}

	public void buildContent() {
		initTree();

		dataTable.setFont(new Font(RM.getFontType("宋体"), 0, 13));
		dataTable.setRowHeight(20);
		dataTable.setBackground(Color.WHITE);
		dataTable.getTableHeader().setReorderingAllowed(false);
		dataTable.setRowSelectionAllowed(true);
		dataTable.setColumnSelectionAllowed(true);
		dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataTable.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getModifiers() == e.BUTTON1_MASK || e.getModifiers() == e.BUTTON3_MASK ) {
					int row = dataTable.getSelectedRow();
					dataModel.selectRow(row);
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getModifiers() == e.BUTTON3_MASK && e.getClickCount() >= 2) {
					fireSelectionEvent();
				}
			}
		});
		
		dataModel = new DataModel();
		dataTable.setModel(dataModel);

		packColumn(dataTable, 0, false);
		
		Insets insets = new Insets(1, 2, 1, 2);
		setLayout(new BorderLayout());

		JScrollPane scroll = new JScrollPane(objTree);
		scroll.setPreferredSize(new Dimension(200, 280));

		JPanel right = new JPanel(new BorderLayout());
		right.setBorder(BorderFactory.createEtchedBorder());
				
		labelInfo.setForeground(Color.blue);
		labelInfo.setHorizontalAlignment(JLabel.LEADING);
		if(point_key == 0){
			labelInfo.setText(RM.gs("未选择采集点"));
		}
		
		JPanel searchPanel = new JPanel();
        searchInput = new JTextField();
        searchInput.setPreferredSize(new Dimension(150, 25));
        searchInput.addKeyListener(new KeyAdapter(){
        	public void keyPressed(KeyEvent e) {
        		// TODO Auto-generated method stub
        		if(e.getSource() == searchInput){
        			if(e.getKeyCode() == KeyEvent.VK_ENTER){
        				searchPoints();
        			}
        		}
        	}
        });
        
        JButton btnSearch = new JButton(RM.gs("查找"));
        btnSearch.setActionCommand("SearchPoints");
        btnSearch.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {		
        		String source = e.getActionCommand();
        		if(source.equals("SearchPoints")){
        			searchPoints();
        		}
        	}
        });
        
        btnSearch.setMargin(insets);
        
        searchPanel.add(searchInput);
        searchPanel.add(btnSearch);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
		top.add(searchPanel);
		top.add(labelInfo);
		
        right.add(top,BorderLayout.NORTH);
		right.add(new JScrollPane(dataTable), BorderLayout.CENTER);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scroll,right);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);
		splitPane.setContinuousLayout(false);

		splitPane.setPreferredSize(new Dimension(600, 500));
		
		add(splitPane, BorderLayout.CENTER);
		
        JPanel buttons = new JPanel();
        buttonOk = new JButton("确定");
        buttonOk.addActionListener(this);
        buttons.add(buttonOk);
        buttonCancel = new JButton("取消");
        buttonCancel.addActionListener(this);
        buttons.add(buttonCancel);
        add(buttons,BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	}
	
    public void packColumn(JTable ptable, int vColIndex, boolean hide) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) ptable.
                                           getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);

        int width = 0;
        if (hide) {
            col.setMinWidth(0);
            col.setMaxWidth(0);
            col.setPreferredWidth(0);
            return;
        }
        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = ptable.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
                ptable, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int r = 0; r < ptable.getRowCount(); r++) {
            renderer = ptable.getCellRenderer(r, vColIndex);
            if(renderer != null){
                comp = renderer.getTableCellRendererComponent(
                        ptable, ptable.getValueAt(r, vColIndex), false, false, r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }
        }
        col.setMinWidth(4);
        //col.setMaxWidth(width * 3);
        col.setPreferredWidth(width + 10);
    }
    
	void selectPoint(){
		int current_dpu_id = IDUtils.getDPU(point_key);
		
		changeModel(current_dpu_id);		
		dataModel.select(point_key);		
		setLabelInfo(point_key);
		
		for (int i = 0; i < treeRoot.getChildCount(); i++) {
			DefaultMutableTreeNode s1Node = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
			DPU_INFO dpu = (DPU_INFO) s1Node.getUserObject();
			if(dpu.dpu_id == current_dpu_id){
				objTree.expandPath(new TreePath(s1Node.getPath()));
				objTree.setSelectionPath(new TreePath(s1Node.getPath()));
				break;
			}
		}
	}
	
	void clearPoint(){
		for (int i = 0; i < treeRoot.getChildCount(); i++) {
			DefaultMutableTreeNode s1Node = (DefaultMutableTreeNode) treeRoot.getChildAt(i);
			DPU_INFO dpu = (DPU_INFO) s1Node.getUserObject();			
			objTree.collapsePath(new TreePath(s1Node.getPath()));
		}		
		dataModel.points.clear();
		dataTable.revalidate();
		dataTable.repaint();
	}
	
	private void initTree() {		
		treeModel = new DefaultTreeModel(treeRoot);
		DefaultMutableTreeNode dpuNode, ioNode;
		
		DPU_INFO dpu_info = new DPU_INFO(0, "全部");
		dpuNode = new DefaultMutableTreeNode(dpu_info);
		treeModel.insertNodeInto(dpuNode, treeRoot,treeRoot.getChildCount());

		TableRecord[] recordsDPU = tableDPU.getRecord();
		for (TableRecord rDPU : recordsDPU) {
			
			Record_SCADA_DPU dpu = (Record_SCADA_DPU) rDPU;
			if (dpu.get_dpuType() == ScadaDefine.DPU_dpuType_Trans ||
				dpu.get_dpuType() == ScadaDefine.DPU_dpuType_MESMODEM) {
				continue;
			}

			int key = dpu.key();
			dpu_info = new DPU_INFO(key, dpu.get_dpuName());
			dpuNode = new DefaultMutableTreeNode(dpu_info);
			treeModel.insertNodeInto(dpuNode, treeRoot,treeRoot.getChildCount());
		}
		
		objTree = new JTree(treeModel);
		objTree.setFont(new Font(RM.getFontType("宋体"), 0, 15));
		objTree.setRowHeight(20);
		objTree.setRootVisible(true);
		objTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		objTree.addMouseListener(new ObjTreeMouseListener());
		objTree.setCellRenderer(new ObjTreeRender());
		objTree.setShowsRootHandles(true);
		objTree.expandRow(0);
		objTree.revalidate();
	}
	
	int[] getValue(){
		return dataModel.getSelectedKeys();
	}
	
	public void setValue(int sample_type,int point_key){
		this.sample_type = sample_type;
		this.point_key = point_key;
		select_key = point_key;
		if(point_key != 0){
			selectPoint();
		}else{
			clearPoint();
		}
	}
	
	void setLabelInfo(int key){
		int current_dpu_id = IDUtils.getDPU(key);
		Record_SCADA_DPU dpu = (Record_SCADA_DPU)tableDPU.findByKey(current_dpu_id);
		
		int nameField = -1,idField = -1;
		DBTable table_buf = null;
		TableRecord record = null;
		if (sample_type == DeviceProperty.SAMPLE_ANALOG) {
			table_buf = tableAnalog;
	        nameField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRID;
		} else if (sample_type == DeviceProperty.SAMPLE_DIGIT) {
			table_buf = tableDigit;
	        nameField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRID;
		} else if (sample_type == DeviceProperty.SAMPLE_PULSE) {
			table_buf = tablePulse;
	        nameField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRID;
		}
		record = table_buf.findByKey(key);
		if(dpu != null && record != null){
			if(record.getValue(nameField).toString().isEmpty()){
				labelInfo.setText("[" +dpu.get_dpuName() + "] " + record.getValue(idField));
			}else{
				labelInfo.setText("[" +dpu.get_dpuName() + "] " + record.getValue(nameField));
			}			
		}else{
			labelInfo.setText("未配置");
		}
	}
	
	public String getRender(int sType,int key){
		if(key == 0){
			return "未配置";
		}
		int current_dpu_id = IDUtils.getDPU(key);
		Record_SCADA_DPU dpu = (Record_SCADA_DPU)tableDPU.findByKey(current_dpu_id);
		
		int nameField = -1,idField = -1;
		DBTable table_buf = null;
		TableRecord record = null;
		if (sType == DeviceProperty.SAMPLE_ANALOG) {
			table_buf = tableAnalog;
	        nameField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRID;
		} else if (sType == DeviceProperty.SAMPLE_DIGIT) {
			table_buf = tableDigit;
	        nameField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRID;
		} else if (sType == DeviceProperty.SAMPLE_PULSE) {
			table_buf = tablePulse;
	        nameField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRNAME;
	        idField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRID;
		}
		record = table_buf.findByKey(key);
		if(dpu != null && record != null){
			if(record.getValue(nameField).toString().isEmpty()){
				return "[" + dpu.get_dpuName() + "] " + record.getValue(idField);
			}else{
				return "[" + dpu.get_dpuName() + "] " + record.getValue(nameField);
			}
		}else{
			return "配置错误";
		}
	}
	
	class DPU_INFO {
		int dpu_id;
		String dpu_name;

		public DPU_INFO(int dpu_id, String dpu_name) {
			this.dpu_id = dpu_id;
			this.dpu_name = dpu_name;
		}

		public String toString() {
			return dpu_name;
		}
	}

	void changeModel(int dpu_id) {
		isSearch = false;
		dataModel.build(dpu_id);	

		packColumn(dataTable, 0, false);

		dataTable.revalidate();
		dataTable.repaint();
	}
	
	class ObjTreeMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				JTree tree = (JTree) e.getSource();
				DefaultMutableTreeNode nodeSelected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				Object obj = nodeSelected.getUserObject();
				if (obj instanceof DPU_INFO) {
					DPU_INFO dpu = (DPU_INFO) obj;
					changeModel(dpu.dpu_id);
				}
			}
		}
	}

	public ImageIcon getImageIcon(DefaultMutableTreeNode node) {
		if (node == null) {
			return null;
		}
		Object obj = node.getUserObject();
		if (obj instanceof DPU_INFO) {
			return iconDPU;
		} 
		return null;
	}

	public class ObjTreeRender extends DefaultTreeCellRenderer {
		public ObjTreeRender() {
			super();
		}
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			String stringValue = tree.convertValueToText(value, sel, expanded,
					leaf, row, hasFocus);
			this.hasFocus = hasFocus;
			setText(stringValue);
			if (sel) {
				setForeground(getTextSelectionColor());
			} else {
				setForeground(getTextNonSelectionColor());
			}
			// There needs to be a way to specify disabled icons.
			if (!tree.isEnabled()) {
				setEnabled(false);
			} else {
				setEnabled(true);
			}
			Object v = ((DefaultMutableTreeNode) value).getUserObject();

			// do some special split...
			// 根据管理器类型获取不同的ImageIcon
			ImageIcon icon = getImageIcon((DefaultMutableTreeNode) value);
			setIcon(icon);
			setLeafIcon(icon);
			setClosedIcon(icon);
			setOpenIcon(icon);
			selected = sel;
			return this;
		}
	}

	private void searchPoints(){
		String strKeyWords = searchInput.getText();
		if(strKeyWords == null || strKeyWords.length() <= 0){
			strKeyWords = "";
		}
		
		dataModel.search(strKeyWords);
		
		dataTable.revalidate();
		dataTable.repaint();
	}
	
	class DataModel extends AbstractTableModel {
		ArrayList<NamedValue> points = new ArrayList<NamedValue>();
		ArrayList<NamedValue> search_points = new ArrayList<NamedValue>();
		
		public DataModel() {
		}

		int getKey(int row){
			if(isSearch){
				return search_points.get(row).getValue();
			}else{
				return points.get(row).getValue();
			}
		}
		
		public int[] getSelectedKeys(){
			int[] rows = dataTable.getSelectedRows();
			if(rows != null && rows.length > 0){
				int[] keys = new int[rows.length];
				for(int i=0;i<keys.length;i++){
					if(isSearch){
						keys[i] = search_points.get(rows[i]).getValue();
					}else{
						keys[i] = points.get(rows[i]).getValue();
					}
				}
				return keys;
			}else{
				return null;
			}
		}
		
		void selectRow(int row){
			select_key = getKey(row);
			setLabelInfo(select_key);
		}
		
		void select(int key){
			boolean isFound = false;
			int row = -1;
			for(int i=0;i<points.size();i++){
				NamedValue nv = points.get(i);
				if(nv.getValue() == key){
					isFound = true;
					row = i;
					break;
				}
			}
			if(isFound){
				dataTable.setRowSelectionInterval(row, row);
				dataTable.setColumnSelectionInterval(0, dataTable.getColumnCount() - 1);
				dataTable.scrollRectToVisible(dataTable.getCellRect(row, 0, true));
			}
		}
		
		public void build(int dpu_id) {			
			int dpuIDField = -1;
			int nameField = -1;
			int idField = -1;
			DBTable table_buf = null;
			if (sample_type == DeviceProperty.SAMPLE_ANALOG) {
				table_buf = tableAnalog;
				dpuIDField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_LDPUID;
		        nameField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRNAME;
		        idField = RealDBDefine.SCADA.SCADAAnalog.FIELD_INDEX_STRID;
			} else if (sample_type == DeviceProperty.SAMPLE_DIGIT) {
				table_buf = tableDigit;
				dpuIDField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_LDPUID;
		        nameField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRNAME;
		        idField = RealDBDefine.SCADA.SCADADigit.FIELD_INDEX_STRID;
			} else if (sample_type == DeviceProperty.SAMPLE_PULSE) {
				table_buf = tablePulse;
				dpuIDField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_LDPUID;
		        nameField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRNAME;
		        idField = RealDBDefine.SCADA.SCADAPulse.FIELD_INDEX_STRID;
			}

			points.clear();
			
			boolean isFind = false;
			String name;
			TableRecord[] records = table_buf.getRecord();
			
			if(dpu_id == 0){
				for (int i = 0; i < records.length; i++) {
					name = (String)records[i].getValue(idField) + " [ " + (String)records[i].getValue(nameField) + " ]";
					points.add(new NamedValue(records[i].key(),name));
				}	
			}else{
				for (int i = 0; i < records.length; i++) {
					if ((Integer) records[i].getValue(dpuIDField) == dpu_id) {					
						isFind = true;
						name = (String)records[i].getValue(idField) + " [ " + (String)records[i].getValue(nameField) + " ]";
						points.add(new NamedValue(records[i].key(),name));
					} else {
						if (isFind) {
							break;
						}
					}
				}					
			}
		}

		public void search(String name){
			if(name.isEmpty()){
				isSearch = false;
			}else{
				isSearch = true;
				search_points.clear();
				
				String[] items = name.split(" ");
				boolean isOK = false;
				
				for(NamedValue nv:points){
					isOK = true;
					for(String item:items){
						if(!nv.getName().contains(item)){
							isOK = false;
							break;
						}
					}
					if(isOK){
						search_points.add(nv);
					}
				}
			}
		}
		
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return RM.gs("采集点描述");
			}			
			return "";
		}

		public Object getValueAt(int row, int col) {
			ArrayList<NamedValue> items = isSearch?search_points:points;
			if(col == 0){
				return items.get(row).getName();
			}else{
				return null;
			}
		}

		public Class getColumnClass(int col) {			
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			return;   
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			ArrayList<NamedValue> items = isSearch?search_points:points;
			return items.size();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == buttonOk){
            fireSelectionEvent();
        } else if (source == buttonCancel) {
            fireCancelEvent();
        } 		
	}
}

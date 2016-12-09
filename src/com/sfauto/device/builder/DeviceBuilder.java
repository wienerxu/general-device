package com.sfauto.device.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sfauto.device.CommonTreeNode;
import com.sfauto.device.Device;
import com.sfauto.device.DeviceFactory;
import com.sfauto.device.DeviceModels;
import com.sfauto.device.DeviceTemplate;
import com.sfauto.toolkits.utils.SplashWindow;

public class DeviceBuilder extends JFrame implements ActionListener {
	DefaultMutableTreeNode nodeSelected = null,nodeCopyed = null;
	ImageIcon iconAdd,iconDelete,iconHome,iconDevice,iconRename,iconDeviceNotice;
	ImageIcon iconFolder,iconFolderNotice;
	PointSelector pointSelector = null;
	
	JTree deviceTree = null;
	JTabbedPaneEx tabedPane;

	DefaultTreeModel treeModel = null;
	JFrame frameWindow = null;
	DeviceModels deviceModels = null;
	
	JButton btnCheck;

	public DeviceBuilder() {
		super("通用设备建模工具(v1.0)");
		iconAdd = createImageIcon("insert.gif");
		iconDelete = createImageIcon("gc.gif");
		iconHome = createImageIcon("station.png");
		iconDevice = createImageIcon("device.png");
		iconDeviceNotice = createImageIcon("device-n.png");
		iconFolder = createImageIcon("folder1.png");
		iconFolderNotice = createImageIcon("folder1-n.png");
		
		iconRename = createImageIcon("rename.gif");

		this.setIconImage(iconDevice.getImage());

		frameWindow = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initialize();
	}

    protected void processWindowEvent(WindowEvent e) {
        int id = e.getID();
        if (id == WindowEvent.WINDOW_CLOSING) {
        	exit();
        }
    }
      
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		if (command.equals("ScanError")) {
			scanError();
		}else if(command.startsWith("add:")){
			String template = command.substring(4);
			addDevice(template);
		}else if(command.startsWith("rm:")){
			removeDevice();
		}else if(command.startsWith("rename:")){
			renameDevice();
		}else if(command.startsWith("copy-device:")){
			nodeCopyed = nodeSelected; 
		}else if(command.startsWith("paste-device:")){
			CommonTreeNode node = (CommonTreeNode)nodeCopyed.getUserObject();
			Device device = (Device)node.getData();
			
			if(device != null){
		        int retValue = JOptionPane.showConfirmDialog(frameWindow,"确认要复制设备 [ " + device.getName() + " ] ?", "提示",
		                JOptionPane.YES_NO_OPTION);
		        
		        if (retValue == JOptionPane.OK_OPTION) {			
					SplashWindow waitDialog = new SplashWindow(frameWindow, null, "正在复制设备...", 0,Color.white);
					WorkThread wt = new WorkThread(waitDialog,2);
					wt.start();
		        }
			}
		}
	}

	void exit(){
		SplashWindow waitDialog = new SplashWindow(frameWindow, null, "正在保存数据...", 0,Color.white);
		WorkThread wt = new WorkThread(waitDialog,3);
		wt.start();
	}
	
	void onSave(){
		DevicePanel tp;
		for (int i = 0; i < tabedPane.getTabCount(); i++) {
			tp = (DevicePanel)tabedPane.getComponentAt(i);
			if(tp.model.isDirty){
				DeviceFactory.getInstance().saveModel(tp.device);
				tp.model.isDirty = false;
			}
		}
	}
	
	void scanError(){		
	}
	
	void addDevice(String template){
		CommonTreeNode node = (CommonTreeNode)nodeSelected.getUserObject();
		DeviceTemplate t = deviceModels.getTemplate(template);
		if(t != null){
			String name = getName(t.description);
			if(name != null){
				Device device = new Device(-1,name,template);
				device.initialize();
				
				Device parendDevice = (Device)node.getData();
				if(DeviceFactory.getInstance().saveModel(device,parendDevice.getID())){
					CommonTreeNode n = node.addChild(device);
					DefaultMutableTreeNode tn = add2tree(nodeSelected,n);
					deviceTree.setSelectionPath(new TreePath(tn.getPath()));
				}								
			}			
		}
	}
	
    public void copyDevice(Device sourceDevice,DefaultMutableTreeNode node){
    	CommonTreeNode target_device_node = (CommonTreeNode)node.getUserObject();
    	Device parentDevice = (Device)target_device_node.getData();
    	CommonTreeNode sourceTreeNode = deviceModels.deviceTree.find(sourceDevice);
    	
    	Device newDevice = DeviceFactory.getInstance().copyDevice(sourceDevice, parentDevice.getID());

    	if(newDevice != null){
    		CommonTreeNode n = target_device_node.addChild(newDevice);
			DefaultMutableTreeNode tn = add2tree(node,n);
			
        	List<CommonTreeNode> childs = sourceTreeNode.getChildren();
        	if(childs != null){
	        	for(CommonTreeNode c:childs){
	        		Device d = (Device)c.getData();
	        		copyDevice(d,tn);
	        	}
        	}
    	}
    }
    
	void removeDevice(){	
		CommonTreeNode node = (CommonTreeNode)nodeSelected.getUserObject();
		Device device = (Device)node.getData();
		
        int retValue = JOptionPane.showConfirmDialog(frameWindow,"确认删除 [ " + device.getName() + " ] ?", "提示",
                JOptionPane.YES_NO_OPTION);
        
        if (retValue == JOptionPane.OK_OPTION) {			
			SplashWindow waitDialog = new SplashWindow(frameWindow, null, "正在删除设备...", 0,Color.white);
			WorkThread wt = new WorkThread(waitDialog,1);
			wt.start();
        }
	}
	
	void removeDeviceInternal(){
		CommonTreeNode node = (CommonTreeNode)nodeSelected.getUserObject();
		Device device = (Device)node.getData();
    	if(DeviceFactory.getInstance().removeDevice(device)){
        	//树操作
    		treeModel.removeNodeFromParent(nodeSelected);
    		deviceTree.revalidate();
    		deviceTree.repaint(); 
    		nodeSelected = null;
    	}
	}
	
	void renameDevice(){
		CommonTreeNode node = (CommonTreeNode)nodeSelected.getUserObject();
		Device device = (Device)node.getData();

		String oldname = device.getName();
		String name = JOptionPane.showInputDialog(frameWindow,"设备名称：", oldname);
		if (name == null || name.length() == 0 || name.equals(oldname)) {
			return ;
		}
		
    	if(DeviceFactory.getInstance().renameDevice(device,name)){
        	//树操作
    		treeModel.nodeChanged(nodeSelected);
    		deviceTree.revalidate();
    		deviceTree.repaint(); 
    	}
	}
	
	String getName(String tip){
		String name = JOptionPane.showInputDialog(frameWindow,"创建 [" + tip+"]：", "新建设备", JOptionPane.QUESTION_MESSAGE);
		if (name != null && name.length() > 0) {
			return name;
		}else{
			return null;
		}
	}
	
	
	public void initialize() {
		boolean isOK = DeviceFactory.getInstance().initialize(false);
		if(!isOK){
			JOptionPane.showMessageDialog(null,"工程目录下缺少device文件夹","OPPS！",JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
		
		deviceModels = DeviceFactory.getInstance().getModels();
		
		buildDeviceTree();

		pointSelector = new PointSelector();
		
		deviceTree = new JTree(treeModel);
		deviceTree.setRootVisible(true);
		deviceTree.setFont(new Font("宋体", 0, 15));
		deviceTree.setRowHeight(20);		
		deviceTree.setCellRenderer(new DeviceTreeRender());
		deviceTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		deviceTree.addMouseListener(new TreeMouseListener());
		deviceTree.setShowsRootHandles(true);

		tabedPane = new JTabbedPaneEx();
		tabedPane.addCloseListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(tabedPane.ON_TAB_CLOSE)) {
					int index = tabedPane.getSelectedIndex();
					
					DevicePanel panel = (DevicePanel)tabedPane.getSelectedComponent();
					if(panel.model.isDirty){
						DeviceFactory.getInstance().saveModel(panel.device);
					}
					
					tabedPane.removeTabAt(index);
				}
			}
		});

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(deviceTree), tabedPane);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(300);
		splitPane.setContinuousLayout(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

		JMenuBar bar = new JMenuBar();
		
		btnCheck = new JButton("配置检查");
		btnCheck.setActionCommand("ScanError");
		btnCheck.addActionListener(this);
		bar.add(btnCheck);
		
		JLabel dummy = new JLabel();
		dummy.setPreferredSize(new Dimension(2000, 20));
		bar.add(dummy);
		//this.getContentPane().add(bar, BorderLayout.NORTH);
		
		pack();
		Dimension frmSize = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		setBounds(0, 0, frmSize.width, frmSize.height);
		setVisible(true);
	}

	void buildDeviceTree(){
		DefaultMutableTreeNode root = null;

		CommonTreeNode dv_root = deviceModels.deviceTree.getRoot();
		root = new DefaultMutableTreeNode(dv_root);
		treeModel = new DefaultTreeModel(root);
				
		List<CommonTreeNode> childs = dv_root.getChildren();  
		if(childs != null){
			for(CommonTreeNode n:childs){
				add2tree(root,n);
			}
		}
	}

	DefaultMutableTreeNode add2tree(DefaultMutableTreeNode treeNode,CommonTreeNode dvNode){
		DefaultMutableTreeNode sNode = new DefaultMutableTreeNode(dvNode);
		treeModel.insertNodeInto(sNode, treeNode, treeNode.getChildCount());
		
		List<CommonTreeNode> childs = dvNode.getChildren(); 
		if(childs != null){
			for(CommonTreeNode n:childs){
				add2tree(sNode,n);
			}
		}
		return sNode;
	}
	
	private ImageIcon createImageIcon(String filename) {
		String path = "images/" + filename;
		return new ImageIcon(getClass().getResource(path));
	}


	class TreeMouseListener extends MouseAdapter {
		private Object nodeObject = null;

		public void mousePressed(MouseEvent e) {
			JTree tree = (JTree) e.getSource();
			
			Device device = null;
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			if (selRow != -1) {
				tree.setSelectionRow(selRow);
				nodeSelected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (nodeSelected != null) {
					nodeObject = nodeSelected.getUserObject();
					if (nodeObject instanceof CommonTreeNode) {
						CommonTreeNode t = (CommonTreeNode) nodeObject;
						device = (Device)t.getData();
					}
				}
			}
			
			if(device == null){
				return;
			}
			
			if (e.getModifiers() == e.BUTTON3_MASK) {
				JPopupMenu popup = getPopupMenu(device);
				popup.show(tree, e.getX(), e.getY());
			} else if (e.getClickCount() >= 2
					&& e.getModifiers() == e.BUTTON1_MASK) {
				openDevice(device);
			}
		}
	}

	JPopupMenu getPopupMenu(Device device){
		JPopupMenu menu = new JPopupMenu();
		CommonTreeNode rootNode = deviceModels.models.getRoot();
		if(device.getID() == 0){  //root
			List<CommonTreeNode> childs = rootNode.getChildren();
			if(childs != null){
				for(CommonTreeNode model:childs){
					DeviceTemplate template = (DeviceTemplate)model.getData();
					JMenuItem mi = new JMenuItem("创建：" +template.description);
					mi.setIcon(iconAdd);
					mi.setActionCommand("add:"+template.name);
					mi.addActionListener(this);
					menu.add(mi);
				}
			}

			if(nodeCopyed != null){
				Device parentDevice = (Device)((CommonTreeNode)((DefaultMutableTreeNode)nodeCopyed.getParent()).getUserObject()).getData();
				if(parentDevice.getID()==0){
					JMenuItem mi = new JMenuItem("粘贴");
					mi.setIcon(iconRename);
					mi.setActionCommand("paste-device:");
					mi.addActionListener(this);
					menu.add(mi);
				}
			}			
		}else{
			Object nodeObject = nodeSelected.getUserObject();
			CommonTreeNode pNode = (CommonTreeNode) nodeObject;					
			Device pDevice = (Device)pNode.getParent().getData();
			
			boolean hasChild = false;
			CommonTreeNode node = deviceModels.findModel(rootNode, pDevice.getType(),device.getType());
			if(node != null){
				List<CommonTreeNode> childs = node.getChildren();
				if(childs != null && childs.size()>0){
					hasChild = true;
					for(CommonTreeNode model:childs){
						DeviceTemplate template = (DeviceTemplate)model.getData();
						JMenuItem mi = new JMenuItem("创建：" +template.description);
						mi.setIcon(iconAdd);
						mi.setActionCommand("add:"+template.name);
						mi.addActionListener(this);
						menu.add(mi);
					}
				}
			}
			
			if(hasChild){
				menu.addSeparator();
			}
			JMenuItem mi = new JMenuItem("重命名...");
			mi.setIcon(iconRename);
			mi.setActionCommand("rename:");
			mi.addActionListener(this);
			menu.add(mi);
			
			mi = new JMenuItem("删除设备（包括子设备）");
			mi.setIcon(iconDelete);
			mi.setActionCommand("rm:");
			mi.addActionListener(this);
			menu.add(mi);
			
			mi = new JMenuItem("复制");
			mi.setIcon(iconRename);
			mi.setActionCommand("copy-device:");
			mi.addActionListener(this);
			menu.add(mi);
			
			if(nodeCopyed != null){
				Device parentDevice = (Device)((CommonTreeNode)((DefaultMutableTreeNode)nodeCopyed.getParent()).getUserObject()).getData();
				DeviceTemplate template = deviceModels.getTemplate(parentDevice.getType());
				
				if(template != null && template.name.equals(device.getType())){
					mi = new JMenuItem("粘贴");
					mi.setIcon(iconRename);
					mi.setActionCommand("paste-device:");
					mi.addActionListener(this);
					menu.add(mi);
				}
			}
		}
		return menu;
	}
	
	void openDevice(Device device){
		//nodeSelected
		if(!deviceTree.isExpanded(new TreePath(nodeSelected.getPath()))){
			String title = device.getName();
			
			DevicePanel tp;
			boolean viewExist = false;
			for (int i = 0; i < tabedPane.getTabCount(); i++) {
				tp = (DevicePanel)tabedPane.getComponentAt(i);
				if(tp.device == device){
					viewExist = true;
					tabedPane.setSelectedIndex(i);
					break;
				}
			}
			if (!viewExist) {
				tp = new DevicePanel(frameWindow, this,pointSelector,device);
				int index = tabedPane.getTabCount();
				tabedPane.add(tp, title);
				tabedPane.setSelectedIndex(index);
			}
			deviceTree.expandPath(new TreePath(nodeSelected.getPath()));
		}
	}
	
	
    public boolean isAllSet(DefaultMutableTreeNode node) {
    	CommonTreeNode t = (CommonTreeNode)node.getUserObject();
    	Device device = (Device)t.getData();

    	if(node.isLeaf()){
        	return device.isAllSet();
        }else{
        	boolean state = device.isAllSet();
        	if(!state){
        		return false;
        	}
        	int count = node.getChildCount();
        	for(int i=0;i<count;i++){
        		DefaultMutableTreeNode tt = (DefaultMutableTreeNode)node.getChildAt(i);
        		state = isAllSet(tt);
        		if(!state){
        			return false;
        		}
        	}
        }
    	return true;
    }  
    
    public class DeviceTreeRender extends DefaultTreeCellRenderer {
        public DeviceTreeRender() {
            super();
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel,
                boolean expanded,
                boolean leaf, int row,
                boolean hasFocus) {
        	String stringValue;
            this.hasFocus = hasFocus;
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            CommonTreeNode n = (CommonTreeNode)node.getUserObject();
            Device dev = (Device)n.getData();
            if(dev.getID() == 0){
            	stringValue = dev.toString();
            	setIcon(iconHome);
            }else{
            	stringValue = dev.toString()+ " ["+ dev.getType() + "]";
            	
            	if(node.getChildCount()>0){
            		if(isAllSet(node)){
            			setIcon(iconFolder);
            		}else{
            			setIcon(iconFolderNotice);
            		}
            	}else{
            		if(dev.isAllSet()){
            			setIcon(iconDevice);
    				}else{
    					setIcon(iconDeviceNotice);
    				}
            	}          	
            }
            setText(stringValue);
            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            selected = sel;
            return this;
        }
    }

	class WorkThread extends Thread {
		SplashWindow splash = null;
		int type;

		public WorkThread(SplashWindow splash,int type) {
			this.splash = splash;
			this.type = type;
			splash.showSplash();
		}
		
		public void run() {
			if(type == 1){
				try {
					removeDeviceInternal();
				} catch (Exception ex) {
				} finally {
					splash.close();
					splash = null;
				}
			}else if(type == 2){
				try {
					Device device = (Device)((CommonTreeNode)nodeCopyed.getUserObject()).getData();
					copyDevice(device, nodeSelected);
				} catch (Exception ex) {
				} finally {
					splash.close();
					splash = null;
				}
			}else if(type == 3){
				try {
			    	onSave();
			    	DeviceFactory.getInstance().destroy();			    	
				} catch (Exception ex) {
				} finally {
					splash.close();
					splash = null;
					System.exit(0);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.exit(0);
		}
		new DeviceBuilder();
	}
	
}

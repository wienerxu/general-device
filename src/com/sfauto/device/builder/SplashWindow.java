package com.sfauto.device.builder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class SplashWindow extends JDialog implements Runnable {
    private JLabel splashLabel = new JLabel();
    private JLabel iconLabel = null;
    private JLabel infoLabel = new JLabel();
    private JProgressBar progress = null;

    private JFrame parent = null;
    private final static Cursor defaultCursor = Cursor.getPredefinedCursor(
            Cursor.DEFAULT_CURSOR);
    private final static Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.
            WAIT_CURSOR);
    private int width, height;

    Thread theThread = null;
    private boolean isRunning = false;

    public void showSplash() {  	
    }

    public void run() {
        pack();
        if (parent == null) {
            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            Dimension frmSize = tk.getScreenSize();
            setLocation((frmSize.width - width) / 2,
                        (frmSize.height - height) / 2);
            setBounds((frmSize.width - width) / 2,
                      (frmSize.height - height) / 2, width, height);
        } else {
            Rectangle rect = parent.getBounds();
            int x = (rect.width - width) / 2 + rect.x;
            int y = (rect.height - height) / 2 + rect.y;
            setLocation(x, y);
            setBounds(x, y, width, height);
            parent.setCursor(waitCursor);
        }
        this.repaint();
        this.setCursor(waitCursor);
        isRunning = true;
        setVisible(true);
    }

    private void build(JFrame parent, ImageIcon icon, String info, int maxValue,
                       Color color) {
        this.parent = parent;
        this.setUndecorated(true);
        //this.setAlwaysOnTop(true);
        JPanel p = new JPanel();
        p.setLayout(null);

        int infoheight = 200;
        int size = 15;
        if (icon == null) {
            icon = createIcon("splash.gif");
        }

        if (maxValue != 0) {
            progress = new JProgressBar(0, maxValue);
        } else {
            progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.setString("������,�Ժ�...");
        }
        progress.setStringPainted(true);
        splashLabel.setIcon(icon);
        splashLabel.setBounds(new Rectangle(0, 0, icon.getIconWidth(),
                                            icon.getIconHeight()));
        infoheight = icon.getIconHeight() - 30;
        infoLabel.setBounds(new Rectangle(0, infoheight, icon.getIconWidth(),
                                          icon.getIconHeight() - infoheight));       
        infoLabel.setFont(new Font("����",Font.PLAIN,size));
        infoLabel.setForeground(color);
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setText(info);
        p.add(infoLabel);
        progress.setBounds(new Rectangle(10, icon.getIconHeight() - 50,
                                         icon.getIconWidth() - 20, 17));
        if (maxValue != 0) {
            p.add(progress);
        }
        p.add(splashLabel);
        width = icon.getIconWidth();
        height = icon.getIconHeight();
        p.setPreferredSize(new Dimension(icon.getIconWidth(),
                                         icon.getIconHeight()));
        getContentPane().add(p);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        theThread = new Thread(this);
        theThread.start();
    	while(!isRunning){
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
    	}           
        //SwingUtilities.invokeLater(this);       
    }

    private void build(JFrame parent, ImageIcon icon, ImageIcon icon2,
                       String info, int maxValue, Color color) {
        this.parent = parent;
        this.setUndecorated(true);
        //this.setAlwaysOnTop(true);
        JPanel p = new JPanel();
        p.setLayout(null);

        int infoheight = 200;
        int size = 15;
        if (icon == null) {
            icon = createIcon("splash.gif");
        }

        if (maxValue != 0) {
            progress = new JProgressBar(0, maxValue);
        } else {
            progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.setString("������,�Ժ�...");
        }
        progress.setStringPainted(true);
        splashLabel.setIcon(icon);
        splashLabel.setBounds(new Rectangle(0, 0, icon.getIconWidth(),
                                            icon.getIconHeight()));
        if (icon2 != null) {
            iconLabel = new JLabel(icon2);
            iconLabel.setBounds((icon.getIconWidth() - icon2.getIconWidth()) /
                                2,
                                (icon.getIconHeight() - icon2.getIconHeight()) / 2,
                                icon2.getIconWidth(), icon2.getIconHeight());
        }
        infoheight = icon.getIconHeight() - 30;
        infoLabel.setBounds(new Rectangle(0, infoheight, icon.getIconWidth(),
                                          icon.getIconHeight() - infoheight));       
        infoLabel.setFont(new Font("����",Font.PLAIN,size));
        infoLabel.setForeground(color);
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setText(info);
        p.add(infoLabel);
        progress.setBounds(new Rectangle(10, icon.getIconHeight() - 50,
                                         icon.getIconWidth() - 20, 17));
        if (maxValue != 0) {
            p.add(progress);
        }
        if (iconLabel != null) {
            p.add(iconLabel);
        }
        p.add(splashLabel);
        width = icon.getIconWidth();
        height = icon.getIconHeight();
        p.setPreferredSize(new Dimension(icon.getIconWidth(),
                                         icon.getIconHeight()));
        getContentPane().add(p);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        theThread = new Thread(this);
        theThread.start();
    	while(!isRunning){
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
    	}           
        //SwingUtilities.invokeLater(this);    
    }

    /**
     * ���캯��
     * @param parent�������ڶ���
     * @param icon��ͼƬ���󣬽�������������
     * @param info���ȴ�������ʾ����ʾ��Ϣ
     * @param maxValue�������������ֵ
     * @param color����ʾ���ֵ���ɫ
     */
    public SplashWindow(JFrame parent, ImageIcon icon, String info,
                        int maxValue, Color color) {
        super(parent, "", true);
        build(parent, icon, info, maxValue, color);
    }

    /**
     * ���캯��
     * @param parent��������
     * @param icon��ͼƬ���󣬸�����������
     * @param icon2��ͼƬ���󣬳������м�λ�ã������Ƕ���ͼƬ��
     * @param info���ȴ�������ʾ����ʾ��Ϣ
     * @param maxValue�������������ֵ
     * @param color����ʾ���ֵ���ɫ
     */
    public SplashWindow(JFrame parent, ImageIcon icon, ImageIcon icon2,
                        String info,
                        int maxValue, Color color) {
        super(parent, "", true);
        build(parent, icon, icon2, info, maxValue, color);
    }

    /**
     * ���캯��
     * @param parent��������
     * @param filename��ͼƬ�ļ�ȫ·��
     * @param info���ȴ�������ʾ����ʾ��Ϣ
     * @param maxValue�������������ֵ
     */
    public SplashWindow(JFrame parent, String filename, String info,
                        int maxValue) {
        super(parent, "", true);
        ImageIcon icon = null;
        if (filename != null) {
            File file = new File(filename);
            if (file.exists()) {
                icon = new ImageIcon(filename);
                if (icon.getImage() == null) {
                    icon = createIcon("splash.gif");
                }
            } else {
                icon = createIcon("splash.gif");
            }
        } else {
            icon = createIcon("splash.gif");
        }
        build(parent, icon, info, maxValue, Color.WHITE);
    }

    private ImageIcon createIcon(String name) {
        return new ImageIcon(getClass().getResource("images/" + name));
    }

       
    class InfoRunnable implements Runnable {
    	String info;
    	SplashWindow sw;
        public InfoRunnable(SplashWindow sw,String info) {
            this.sw = sw;
            this.info = info;
        }

        public void run() {
            infoLabel.setText(info);
            sw.repaint();
        }
    }
    
    /**
     * ������ʾ����
     * @param info������ʾ�������ַ���
     */
    public void setInfo(String info) {
    	SwingUtilities.invokeLater(new InfoRunnable(this,info));
    }

    /**
     * ���ý����������ֵ
     * @param max��������ֵ
     */
    public void setMaxValue(int max) {
        progress.setIndeterminate(false);
        progress.setString(null);
        progress.setMaximum(max);
    }

    class ProgressRunnable implements Runnable {
    	int value;
    	SplashWindow sw;
        public ProgressRunnable(SplashWindow sw,int value) {
            this.sw = sw;
            this.value = value;
        }

        public void run() {
            if (progress != null) {
                if (value <= progress.getMaximum() &&
                    value >= progress.getMinimum()) {
                    progress.setValue(value);
                    //paint(getGraphics());
                    sw.repaint();
                }
            }
        }
    }
    
    /**
     * ���ý������ĵ�ǰֵ
     * @param value�����ȵ�ǰֵ
     */
    public void setProgressValue(int value) {
    	SwingUtilities.invokeLater(new ProgressRunnable(this,value));
    }

    /**
     * �رջ�ӭ����
     */
    public void close() {
    	//this.setAlwaysOnTop(false);
        this.setVisible(false);
        this.dispose();

        if (parent != null) {
            parent.setCursor(defaultCursor);
        }
/*
        while (theThread.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
 */
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("asdf");
        //frame.setPreferredSize(new Dimension(800, 600));
        //frame.setSize(new Dimension(800, 600));
        frame.getContentPane().setLayout(new BorderLayout());
        JButton btn = new JButton("ddd");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SplashWindow sw = new SplashWindow(null, "", "��������...", 0);
                /*
                    for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ex) {
                    }
                    sw.setProgressValue(i + 1);
                                 }
                 */
                sw.setInfo("��������������������������������");
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                }
                sw.close();
            }
        });
        frame.getContentPane().add(btn, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}

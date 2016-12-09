package com.sfauto.device.builder;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class JTabbedPaneEx extends JTabbedPane {
    public static final String ON_TAB_CLOSE = "ON_TAB_CLOSE";
    public static final String ON_TAB_DOUBLECLICK = "ON_TAB_DOUBLECLICK";
    public int selected_index = -1;
    public JTabbedPaneEx() {
        super();
        init();
    }

    public JTabbedPaneEx(int tabPlacement) {
        super(tabPlacement);
        init();
    }

    public JTabbedPaneEx(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        init();
    }

    public void setFontEx(Font font) {
        setFont(font);
    }

    protected void init() {
        addMouseListener(new DefaultMouseAdapter());
    }

    public void setIconDrawCenter(int index, boolean drawCenter) {
        ((CloseIcon) getIconAt(index)).setDrawCenter(drawCenter);
        repaint();
    }

    protected EventListenerList closeListenerList = new EventListenerList();
    public void addCloseListener(ActionListener l) {
        closeListenerList.add(ActionListener.class, l);
    }

    public void removeCloseListener(ActionListener l) {
        closeListenerList.remove(ActionListener.class, l);
    }

    protected void fireClosed(ActionEvent e) {
        Object[] listeners = closeListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    class DefaultMouseAdapter extends MouseAdapter {
        CloseIcon icon;
        public void mousePressed(MouseEvent e) {
            int index = indexAtLocation(e.getX(), e.getY());
            if (index != -1) {
                icon = (CloseIcon) getIconAt(index);
                if (icon.getBounds().contains(e.getPoint())) {
                    icon.setPressed(true);
                    repaint();
                } 
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (icon != null) {
                if (icon.getBounds().contains(e.getPoint())) {
                    fireClosed(new ActionEvent(
                            e.getComponent(),
                            ActionEvent.ACTION_PERFORMED,
                            ON_TAB_CLOSE));
                } else {
                    icon.setPressed(false);
                    repaint();
                }
                icon = null;
            }
        }
    }


    public Icon getIconAt(int index) {
        Icon icon = super.getIconAt(index);
        if (icon == null || !(icon instanceof CloseIcon)) {
            super.setIconAt(index, new CloseIcon());
        }
        return super.getIconAt(index);
    }

    class CloseIcon implements Icon {
        Rectangle rec = new Rectangle(0, 0, 15, 16);
        private boolean pressed = false;
        private boolean drawCenter = true;
        public synchronized void paintIcon(
                Component c, Graphics g, int x1, int y1) {
            int x = x1, y = y1;
            if (pressed) {
                x++;
                y++;
            }
            rec.x = x;
            rec.y = y;
            Color oldColor = g.getColor();
            g.setColor(UIManager.getColor("TabbedPane.highlight"));
            g.drawLine(x, y, x, y + rec.height);
            g.drawLine(x, y, x + rec.width, y);
            g.setColor(UIManager.getColor("TabbedPane.shadow"));
            g.drawLine(x, y + rec.height, x + rec.width, y + rec.height);
            g.drawLine(x + rec.width, y, x + rec.width, y + rec.height);
            //g.setColor(UIManager.getColor("TabbedPane.foreground"));
            g.setColor(Color.black);
            //draw X
            //left top
            g.drawRect(x + 4, y + 4, 1, 1);
            g.drawRect(x + 5, y + 5, 1, 1);
            g.drawRect(x + 5, y + 9, 1, 1);
            g.drawRect(x + 4, y + 10, 1, 1);
            //center
            if (drawCenter) {
                g.drawRect(x + 6, y + 6, 1, 1);
                g.drawRect(x + 8, y + 6, 1, 1);
                g.drawRect(x + 6, y + 8, 1, 1);
                g.drawRect(x + 8, y + 8, 1, 1);
            }
            //right top
            g.drawRect(x + 10, y + 4, 1, 1);
            g.drawRect(x + 9, y + 5, 1, 1);
            //right bottom
            g.drawRect(x + 9, y + 9, 1, 1);
            g.drawRect(x + 10, y + 10, 1, 1);
            g.setColor(oldColor);
        }

        private void drawRec(Graphics g, int x, int y) {
            g.drawRect(x, y, 1, 1);
        }

        public Rectangle getBounds() {
            return rec;
        }

        public void setBounds(Rectangle rec) {
            this.rec = rec;
        }

        public int getIconWidth() {
            return rec.width;
        }

        public int getIconHeight() {
            return rec.height;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        public void setDrawCenter(boolean drawCenter) {
            this.drawCenter = drawCenter;
        }

        public boolean isPressed() {
            return pressed;
        }

        public boolean isDrawCenter() {
            return drawCenter;
        }
    }
}

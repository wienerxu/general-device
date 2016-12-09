package com.sfauto.device.builder;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class HeaderRenderer
    extends JPanel
    implements TableCellRenderer {
  private Color color = new Color(255, 255, 255);
  private Color filtercolor = Color.BLUE;
  private Color sortcolor = Color.RED;

  public int filter_column = -1;
  public int sort_column = -1;

  public HeaderRenderer() {
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    removeAll();
    StringTokenizer strtok = new StringTokenizer( (String) value, "\r\n");
    setLayout(new GridLayout(strtok.countTokens(), 1));
    while (strtok.hasMoreElements()) {
      JLabel label = new JLabel( (String) strtok.nextElement(), JLabel.CENTER);
      if(filter_column != -1 && column == filter_column)
        label.setForeground(filtercolor);
      if(sort_column != -1 && column == sort_column)
        label.setForeground(sortcolor);
      add(label);
    }

    LookAndFeel.installBorder(this, "TableHeader.cellBorder");
    return this;
  }

  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    Point pt = this.getParent().getLocation();
    Dimension dm = this.getSize();
    Rectangle rc = new java.awt.Rectangle(pt, dm);
    Paint gp = new GradientPaint(0, - (int) rc.getHeight() / 2, color.darker(),
                                 0, (int) rc.getHeight() / 2, color, true);
    g2.setPaint(gp);
    g2.fill(rc);
  }

}

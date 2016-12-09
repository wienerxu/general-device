package com.sfauto.device.builder;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class RowBackgroundRenderer
    extends DefaultTableCellRenderer {
  private Color bkcolor;
  private Color fgcolor;
  private boolean isCrossRender;

  public RowBackgroundRenderer(Color bkcolor,Color fgcolor,boolean isCrossRender) {
    super();
    this.bkcolor = bkcolor;
    this.fgcolor = fgcolor;
    this.isCrossRender = isCrossRender;
  }
  public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus, int row,
      int column) {
      if(isCrossRender){
          if (row % 2 == 0)
              setBackground(bkcolor);
          else
              setBackground(Color.WHITE);
      }else{
          setBackground(bkcolor);
      }
      setForeground(fgcolor);
    return super.getTableCellRendererComponent(table,
                                               value, isSelected, hasFocus,
                                               row,
                                               column);
  }
}

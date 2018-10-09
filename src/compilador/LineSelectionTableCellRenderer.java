package compilador;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class LineSelectionTableCellRenderer
        extends DefaultTableCellRenderer {
            
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        
        
        if (table.getValueAt(row,1).equals("Caractere inválido")) {
            result.setFont(new Font("Ubuntu", Font.PLAIN, 12));
            result.setForeground(Color.black);
            result.setBackground(Color.red);
        } else {
            result.setFont(new Font("Ubuntu", Font.PLAIN, 12));
            result.setForeground(Color.black);
            if(row % 2 == 0){
                result.setBackground(Color.white);
            }else{
                result.setBackground(Color.lightGray);
            }
        }
        
        return result;
    }
}

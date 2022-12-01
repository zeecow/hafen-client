package haven;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ZeeJTable extends JFrame {

    private static ZeeJTable instanceFood, instanceSwill;
    String filename;

    static ZeeJTable getTableFood(){
        if(instanceFood == null) {
            Path path = Paths.get(System.getProperty("user.home"),ZeeResearch.FILE_NAME_FOOD);
            if (!path.toFile().exists()){
                JOptionPane.showMessageDialog(null,"food file not found");
                return null;
            }
            instanceFood = new ZeeJTable(ZeeResearch.FILE_NAME_FOOD);
        }
        return instanceFood;
    }

    static ZeeJTable getTableSwill(){
        if(instanceSwill == null) {
            Path path = Paths.get(System.getProperty("user.home"),ZeeResearch.FILE_NAME_HERBALSWILL);
            if (!path.toFile().exists()){
                JOptionPane.showMessageDialog(null,"swill file not found");
                return null;
            }
            instanceSwill = new ZeeJTable(ZeeResearch.FILE_NAME_HERBALSWILL);
        }
        return instanceSwill;
    }

    private ZeeJTable(String filename) {
        super.setTitle(filename);
        this.filename = filename;
        setLocationRelativeTo(null);//center window
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800,400);
        setContents();
        //pack();
        setVisible(true);
    }

    private void setContents() {
        if (this.filename.contentEquals(ZeeResearch.FILE_NAME_FOOD))
            fillTableFood();
        else if (this.filename.contentEquals(ZeeResearch.FILE_NAME_HERBALSWILL))
            fillTableSwill();
    }


    // file line format "name;ql;[ingreds;][events;]"
    // ingreds format: "igr,[name],[perc];"
    private void fillTableFood() {

        //table headers
        DefaultTableModel tableModel = new DefaultTableModel();
        String[] cols = {"name","ql","ingr","evts"};
        tableModel.setColumnIdentifiers(cols);


        //table lines
        List<String> lines = ZeeResearch.readAllLinesFromFile(ZeeResearch.FILE_NAME_FOOD);
        if (lines==null) {
            println("fillTableFood > couldnt read food file");
            return;
        }
        String line;
        String[] arrLine, arrIgr, arrEvt;
        String lineRow;
        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            if (line.isBlank())
                continue;
            arrLine = line.split(";");

            //name
            lineRow = arrLine[0] + ";";

            //ql
            lineRow += arrLine[1] + ";";

            //ingreds
            for (int j = 0; j <arrLine.length; j++) {
                if (arrLine[j].startsWith("igr,")){
                    arrIgr = arrLine[j].split(",");
                    lineRow += arrIgr[1] + " " + (((int)Double.parseDouble(arrIgr[2]))*100) + "% , ";
                }
            }
            if (lineRow.endsWith(", ")) { //trim ingreds
                lineRow = lineRow.replaceAll(", $", ";");
            }
            else{ // no ingreds, empty col
                lineRow += ";";
            }


            //events
            for (int j = 0; j <arrLine.length; j++) {
                if (arrLine[j].startsWith("evt,")){
                    arrEvt = arrLine[j].split(",");
                    lineRow += arrEvt[1] + " " + arrEvt[2] + ", ";
                }
            }
            if (lineRow.endsWith(", ")) { //trim events
                lineRow = lineRow.replaceAll(", $", ";");
            }
            else{ // no events?, empty col
                lineRow += ";";
            }


            //add formatted line to jtable
            //println("addRow = "+lineRow);
            tableModel.addRow(lineRow.split(";"));
        }

        //build table
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Serif", Font.PLAIN, 11));
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tc = tcm.getColumn(0);
        tc.setMaxWidth(200);
        tc = tcm.getColumn(1);
        tc.setMaxWidth(50);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane);
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void fillTableSwill() {
    }

    @Override
    public void dispose() {
        if (this.filename.contentEquals(ZeeResearch.FILE_NAME_FOOD))
            instanceFood = null;
        else if (this.filename.contentEquals(ZeeResearch.FILE_NAME_HERBALSWILL))
            instanceSwill = null;
        super.dispose();
    }
}

package haven;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

public class ZeeJTable extends JFrame {

    private static ZeeJTable instanceFood, instanceSwill;
    String filename;

    TableRowSorter<DefaultTableModel> tableRowSorter;
    JTextField tfFilter;

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
        if (this.filename.contentEquals(ZeeResearch.FILE_NAME_FOOD)) {
            buildTableFood();
        }else if (this.filename.contentEquals(ZeeResearch.FILE_NAME_HERBALSWILL)) {
            buildTableSwill();
        }
    }


    // file line format "name;ql;[ingreds;][events;]"
    // ingreds format: "igr,[name],[perc];"
    private void buildTableFood() {

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


            /*
                ingreds
            */
            for (int j = 0; j <arrLine.length; j++) {
                if (arrLine[j].startsWith("igr,")){
                    arrIgr = arrLine[j].split(",");
                    lineRow += arrIgr[1] + " " + ((int)(Double.parseDouble(arrIgr[2])*100)) + "% , ";
                }
            }
            if (lineRow.endsWith(", ")) { //trim ingreds
                lineRow = lineRow.replaceAll(", $", ";");
            }
            else{ // no ingreds, empty col
                lineRow += ";";
            }


            /*
                food events (FEPs)
            */
            for (int j = 0; j <arrLine.length; j++) {
                if (arrLine[j].startsWith("evt,")){
                    arrEvt = arrLine[j].split(",");
                    lineRow += arrEvt[1] + " " + arrEvt[2] + ",";
                }
            }
            //trim events
            if (lineRow.endsWith(", ")) {
                lineRow = lineRow.replaceAll(", $", ";");
            }
            // no events?, empty col
            else{
                lineRow += ";";
            }
            //sort FEPs by highest number
            String[] arrLineRow = lineRow.split(";");
            String[] arrFeps = arrLineRow[arrLineRow.length-1].split(",");
            String fepJ, fepK, temp;
            //println("before = "+ Arrays.toString(arrFeps));
            for (int j = 0; j < arrFeps.length-1; j++) {
                for (int k = j+1; k < arrFeps.length; k++) {
                    // extract "9.9" from "INT+2 9.9"
                    fepJ = arrFeps[j].replaceAll("[\\^\\S]+\\s", "");
                    fepK = arrFeps[k].replaceAll("[\\^\\S]+\\s", "");
                    // switch highest fep to front
                    if (Double.parseDouble(fepK) > Double.parseDouble(fepJ)) {
                        //println("    "+arrFeps[j]+" > "+arrFeps[k]);
                        temp = arrFeps[j];
                        arrFeps[j] = arrFeps[k].trim();
                        arrFeps[k] = temp.trim();
                    }
                }
            }
            //println("after = "+ Arrays.toString(arrFeps));
            arrLineRow[arrLineRow.length-1] = String.join(",",arrFeps).trim().replaceAll(",",", ");
            lineRow = String.join(";",arrLineRow);


            //add formatted line to jtable
            //println("addRow = "+lineRow);
            tableModel.addRow(lineRow.split(";"));
        }

        // build table
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Serif", Font.PLAIN, 11));
        // table filter
        tableRowSorter = new TableRowSorter<DefaultTableModel>(tableModel);
        table.setRowSorter(tableRowSorter);
        tfFilter = new JTextField();
        this.add(tfFilter, BorderLayout.NORTH);
        tfFilter.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String txt = tfFilter.getText();
                if (txt.length()==0 || txt.isBlank() || evt.getKeyCode()==KeyEvent.VK_ESCAPE){
                    tfFilter.setText("");
                    tableRowSorter.setRowFilter(null);
                    return;
                }
                try{
                    String treatedText = txt.strip();
                    treatedText = Pattern.quote(treatedText); // normal text search
                    treatedText = "(?i)" + treatedText; // case insensitive
                    RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter(treatedText);
                    tableRowSorter.setRowFilter(rowFilter);
                } catch (java.util.regex.PatternSyntaxException exc) {
                    println(exc.getMessage());
                }
            }
        });
        // table columns
        TableColumnModel tcm = table.getColumnModel();
        TableColumn tc = tcm.getColumn(0);
        tc.setMaxWidth(200);
        tc = tcm.getColumn(1);
        tc.setMaxWidth(50);
        //add table
        JScrollPane scrollPane = new JScrollPane(table);
        this.add(scrollPane,BorderLayout.CENTER);
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void buildTableSwill() {
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

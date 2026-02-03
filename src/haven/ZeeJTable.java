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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class ZeeJTable extends JFrame {

    private static ZeeJTable instanceFood, instanceSwill;
    String filename;

    TableRowSorter<DefaultTableModel> tableRowSorter;
    JTextField tfFilter;

    static ZeeJTable getTableFood(){
        if(instanceFood == null) {
            Path path = Paths.get(System.getProperty("user.home"),ZeeResearch.fileNameFood);
            if (!path.toFile().exists()){
                JOptionPane.showMessageDialog(null,"food file not found");
                return null;
            }
            instanceFood = new ZeeJTable(ZeeResearch.fileNameFood);
        }
        return instanceFood;
    }

    static ZeeJTable getTableSwill(){
        if(instanceSwill == null) {
            Path path = Paths.get(System.getProperty("user.home"),ZeeResearch.fileNameHerbalswill);
            if (!path.toFile().exists()){
                JOptionPane.showMessageDialog(null,"swill file not found");
                return null;
            }
            instanceSwill = new ZeeJTable(ZeeResearch.fileNameHerbalswill);
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
        if (this.filename.contentEquals(ZeeResearch.fileNameFood)) {
            buildTableFood();
        }else if (this.filename.contentEquals(ZeeResearch.fileNameHerbalswill)) {
            buildTableSwill();
        }
    }


    // file line format "name;ql;totfep;[ingreds;][events;]"
    // ingreds format: "igr,[name],[perc];"
    private void buildTableFood() {

        //table headers
        DefaultTableModel tableModel = new DefaultTableModel();
        String[] cols = {"name","ql","ingrs","FEP","FEP details"};
        tableModel.setColumnIdentifiers(cols);


        //table lines
        List<String> lines = ZeeResearch.readAllLinesFromFile(ZeeResearch.fileNameFood);
        if (lines==null) {
            println("fillTableFood > couldnt read food file");
            return;
        }
        String line;
        String[] arrLine, arrIgr, arrEvt;
        String lineRow;
        for (int i = 0; i < lines.size(); i++) {

            line = lines.get(i);

            if (line.isBlank()) {
                println("blank line");
                continue;
            }

            arrLine = line.split(";");

            if (!line.contains("evt,")){
                println("no evts for "+arrLine[0]);
                continue;
            }

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


            //totfep
            lineRow += arrLine[2] + ";";


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
            lineRow = lineRow.trim();
            if (lineRow.endsWith(",")) {
                lineRow = lineRow.replaceAll(",$", ";");
            }
            // no idea
            else{
                lineRow += ";";
                println(arrLine[0]+" >?> "+lineRow);
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
            // round feps, format
            int totalFep = 0;
            for (int j = 0; j < arrFeps.length; j++) {
                // extract "9.9" from "INT+2 9.9"
                fepJ = arrFeps[j].replaceAll("[\\^\\S]+\\s", "");
                int round = (int) Math.rint(Double.parseDouble(fepJ));
                totalFep += round;
                // "INT+2 9.9" to "INT2 = 10"
                arrFeps[j] = arrFeps[j]
                    .replaceAll("[\\^\\S]+$","" + (round == 0 ? fepJ : round))
                    .replaceAll("\\s"," = ")
                ;
            }
            //add each fep percentage
            for (int j = 0; j < arrFeps.length; j++) {
                // extract "10" from "INT2 = 10"
                fepJ = arrFeps[j].replaceAll("[\\^\\S]+\\s", "");
                // from "INT2 = 10" to "INT2 = 10 (5%)"
                double perc = (Double.parseDouble(fepJ) / totalFep) * 100;
                //println( perc+" = "+ Math.rint(Double.parseDouble(fepJ)) +" / "+totalFep +"  * 100");
                if (perc < 100)
                    arrFeps[j] += " ("+String.format("%.0f", perc)+"%)";
            }
            //println("after = "+ Arrays.toString(arrFeps));
            arrLineRow[arrLineRow.length-1] = String.join(",",arrFeps).trim().replaceAll(",",", ");
            lineRow = String.join(";",arrLineRow);


            //add formatted line to jtable
            //println("addRow = "+lineRow);
            tableModel.addRow(lineRow.split(";"));
        }


        // build jtable
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Serif", Font.PLAIN, 11));

        // add jtable filter
        tableRowSorter = new TableRowSorter<DefaultTableModel>(tableModel);
        tableRowSorter.setComparator(1, Comparator.comparingInt(o -> Integer.parseInt(o.toString())));//ql
        tableRowSorter.setComparator(3, Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));//totfep
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
        TableColumn tc = tcm.getColumn(0);//col name
        tc.setMaxWidth(200);
        tc = tcm.getColumn(1);//col ql
        tc.setMaxWidth(50);
        tc = tcm.getColumn(3);//col total feps
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
        if (this.filename.contentEquals(ZeeResearch.fileNameFood))
            instanceFood = null;
        else if (this.filename.contentEquals(ZeeResearch.fileNameHerbalswill))
            instanceSwill = null;
        super.dispose();
    }
}

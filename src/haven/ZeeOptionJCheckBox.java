package haven;

import javax.swing.*;

public class ZeeOptionJCheckBox extends JCheckBox {
    private final String optName;
    final String label;

    public ZeeOptionJCheckBox(String label, String optName) {
        super(label);
        this.optName = optName;
        this.label = label;

        try {
            this.setSelected(this.getZeeConfigBoolean());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        this.addActionListener(actionEvent -> {
            boolean val = this.isSelected();
            try {
                this.setZeeConfigBoolean(val);
                ZeeQuickOptionsWindow.updateJCheckBoxWidget(optName,label);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    static void setZeeConfigBoolean(boolean val, String optName) {

        // save config
        try {
            ZeeConfig.class.getDeclaredField(optName).setBoolean(ZeeConfig.class,val);
            Utils.setprefb(optName, val);
            //ZeeConfig.println("setZeeConfigBoolean() > " + optName + " > " + getZeeConfigBoolean(optName));
        } catch (Exception e){
            e.printStackTrace();
        }

        // check for runnable
        try {
            Runnable r = (Runnable) ZeeConfig.class.getDeclaredField(optName+"Runnable").get(ZeeConfig.class);
            r.run();
        } catch (NoSuchFieldException e){
            // no runnable
        } catch (NullPointerException e){
            // no runnable
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    void setZeeConfigBoolean(boolean val) throws Exception {
        setZeeConfigBoolean(val,optName);
    }

    static  boolean getZeeConfigBoolean(String optName) throws Exception {
        return ZeeConfig.class.getDeclaredField(optName).getBoolean(ZeeConfig.class);
    }

    boolean getZeeConfigBoolean() throws Exception {
        return getZeeConfigBoolean(optName);
    }
}

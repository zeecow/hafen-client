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
                ZeeQuickOptionsWindow.updateCheckBoxWidget(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    void setZeeConfigBoolean(boolean val) throws Exception {
        ZeeConfig.class.getDeclaredField(optName).setBoolean(ZeeConfig.class,val);
        Utils.setprefb(optName, val);
        ZeeConfig.println("setZeeConfigBoolean() > "+optName+" > "+getZeeConfigBoolean());
    }

    boolean getZeeConfigBoolean() throws Exception {
        return ZeeConfig.class.getDeclaredField(optName).getBoolean(ZeeConfig.class);
    }
}

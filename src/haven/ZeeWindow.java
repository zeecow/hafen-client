package haven;

public class ZeeWindow extends Window {

    public ZeeWindow(Coord coord, String title) {
        super(coord, title);
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        ZeeConfig.println(this.getClass().getSimpleName()+" > "+msg);
        if(msg.equals("close")){
            close();
        }
    }

    private void close() {
        hide();
    }

    static class ZeeButton extends Button{
        String title;
        public ZeeButton(int width, String title) {
            super(width,title);
            this.title = title;
        }

        @Override
        public void wdgmsg(String msg, Object... args) {
            ZeeConfig.println(this.getClass().getSimpleName()+" > "+msg);
            if(msg.equals("activate")){
                if(title.equals("test")) {
                    try {
                        int tiles = Integer.parseInt(ZeeFarmingManager.windowTxtentryTilesBarrel.text().strip());
                        new ZeeFarmingManager(tiles).start();
                    }catch (NumberFormatException e){
                        ZeeConfig.msg("numbers only");
                    }
                }else if (title.equals("start")){
                    ZeeConfig.println("start farman");
                }
            }
        }
    }
}

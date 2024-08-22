package nl.nlcode.m.cli;

import nl.nlcode.m.ui.FxApp;

/**
 *
 * @author jq59bu
 */
public class GuiCommand /*extends Token*/ {

//    public GuiCommand(Token parent) {
//        super("gui", parent);
//        setType(Type.COMMAND);
//        setMustHaveMatchedChild();
//
//        new Token.Builder(this)
//                .command("start")
//                .execute(() -> {
//                    if (FxApp.getInstance() == null) {
//                        FxApp.start();
//                    } else {
//                        getControlCli().stdout().println("already running");
//                    }
//                })
//                .create();
//
//
//        new Token.Builder(this)
//                .command("stop")
//                .execute(() -> {
//                    FxApp instance = FxApp.getInstance();
//                    if (instance != null) {
//                        instance.stop();
//                    } else {
//                        getControlCli().stdout().println("not running");
//                    }
//                })
//                .create();
//    }


}

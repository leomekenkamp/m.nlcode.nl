//package nl.nlcode.m.ui;
//
//import java.nio.file.Path;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
//import nl.nlcode.m.engine.Control;
//import nl.nlcode.m.engine.Lookup;
//import nl.nlcode.m.engine.MidiInOut;
//import nl.nlcode.m.engine.Project;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.testfx.api.FxAssert;
//import org.testfx.api.FxRobot;
//import org.testfx.framework.junit5.ApplicationExtension;
//import org.testfx.framework.junit5.Start;
//import org.testfx.matcher.control.LabeledMatchers;
///**
// *
// * @author leo
// */
//@ExtendWith(ApplicationExtension.class)
//public class ProjectViewControllerFactoryTest {
//    
//    private Button button;
//    private Stage stage;
//    
//    public ProjectViewControllerFactoryTest() {
//    }
//
//    @Start
//    private void start(Stage stage) {
//        button = new Button("click me!");
//        button.setId("myButton");
//        button.setOnAction(actionEvent -> button.setText("clicked!"));
//        button.setOnMouseEntered(actionEvent -> button.setText("mouseEntered"));
//        stage.setScene(new Scene(new StackPane(button), 100, 100));
//        stage.show();
//    }
//    
//    @BeforeAll
//    public static void setUpClass() {
//    }
//    
//    @AfterAll
//    public static void tearDownClass() {
//    }
//    
//    @BeforeEach
//    public void setUp() {
//    }
//    
//    @AfterEach
//    public void tearDown() {
//    }
//
//    /**
//     * 
//     * Test of call method, of class ProjectViewControllerFactory.
//     */
//    @Test
//    public void testCall() {
//        Lookup lookup = Lookup.create();
//        MidiInOut midiInOut = new MidiInOut(lookup) {};
//        ProjectUi projectUi = new ProjectUi(new ControlUi(Control.getInstance()),
//                Project.create(Control.getInstance(), Path.of("shouldDefinitivelyNotExist")));
//        
//        assertThat(projectUi.getPath().toString(), equalTo("shouldDefinitivelyNotExist"));
//    }
//    
//    @Test
//    void when_button_is_clicked_text_changes(FxRobot robot) throws InterruptedException {
//        // when:
//        Thread.sleep(5000);
//
//        robot.clickOn(button);
//        robot.clickOn("#myButton");
//Thread.sleep(5000);
//        // then:
//        FxAssert.verifyThat(button, LabeledMatchers.hasText("clicked!"));
//        // or (lookup by css id):
//        FxAssert.verifyThat("#myButton", LabeledMatchers.hasText("clicked!"));
//        // or (lookup by css class):
//        FxAssert.verifyThat(".button", LabeledMatchers.hasText("clicked!"));
//    }
//    
//}

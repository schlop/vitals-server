package screencapture;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Paul on 30/10/2017.
 */
public class StartGUI{
    private MainController mainController;

    private JButton startScreencaptureServerButton;
    private JButton startScreencaptureDebuggingButton;
    private JButton stopButton;
    private JTextPane textPane1;
    public JPanel mainPannel;


    public StartGUI(final MainController mainController){
        this.mainController = mainController;

        startScreencaptureServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainController.start();
                System.out.println("Pressed Start");
            }
        });

        startScreencaptureDebuggingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainController.startWithoutServer();
                System.out.println("Pressed Start + Debugging");
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainController.stop();
            }
        });
    }
}

package com.systemincloud.ext.vip.examples.webcamclient.java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;
import com.systemincloud.sdk.java.SicClient;
import com.systemincloud.sdk.java.SicClientFactory;
import com.systemincloud.sdk.java.SicListener;
import com.systemincloud.sdk.java.msg.MachineInfo;

import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;

import java.awt.Component;

public class WebcamClient implements ActionListener, SicListener {

    private Webcam webcam = Webcam.getDefault();
    
    private JFrame window = new JFrame("System in Cloud - Webcam Client");
    
    private final JPanel mainPanel = new JPanel();
    
    private final JPanel videoPanels  = new JPanel();
    
    private JPanel videoIn;
    private JPanel videoOut;
    
    private final JPanel     controlPanel        = new JPanel();
   
    private final JPanel     credentials         = new JPanel();
    private final Box        credentialsBox      = Box.createVerticalBox();
    
    private final Box        accountNumberBox    = Box.createHorizontalBox();
    private final JLabel     lblAccountNumber    = new JLabel("Account Number: ");
    private final JTextField textAccountNumber   = new JTextField();
    
    private final Box        systemNameBox       = Box.createHorizontalBox();
    private final JLabel     lblSystemName       = new JLabel("System Name: ");
    private final JTextField textSystemName      = new JTextField();
    
    private final Box        systemKeyBox        = Box.createHorizontalBox();
    private final JLabel     lblSystemKey        = new JLabel("System Key: ");
    private final JTextField textSystemKey       = new JTextField();
    
    private final Box        reconnectionBox     = Box.createHorizontalBox();
    private final JButton    btnReconnect        = new JButton("Reconnect");
    private final JLabel     lblStatus           = new JLabel("  --");
    
    private final JLabel     lblMachines         = new JLabel("Machines:");
    private final JPanel     machines            = new JPanel();
    private final Box        machinesBox         = Box.createVerticalBox();
    private final JList<?>   machinesList        = new JList<>();
    private final Box        machinesBtnsBox     = Box.createHorizontalBox();
    private final JButton    btnRefreshMachines  = new JButton("Refresh");
    private final JButton    btnNewMachine       = new JButton("New");
    private final JButton    btnDeleteMachine    = new JButton("Delete");
    
    private final JLabel     lblInstances        = new JLabel("Instances:");
    private final JPanel     instances           = new JPanel();
    private final Box        instancesBox        = Box.createVerticalBox();
    private final JList<?>   instancesList       = new JList<>();
    private final Box        instancesBtnsBox    = Box.createHorizontalBox();
    private final JButton    btnRefreshInstances = new JButton("Refresh");
    private final JButton    btnNewInstance      = new JButton("New");
    private final JButton    btnDeleteInstance   = new JButton("Delete");
    
    private JToggleButton onOffButton = new JToggleButton("On/Off");
    
    private class NewMachineFrame extends JFrame { private static final long serialVersionUID = 1L;
        
    }
    
    private class DeleteMachineFrame extends JFrame { private static final long serialVersionUID = 1L;
        
    }
    
    private class NewInstanceFrame extends JFrame { private static final long serialVersionUID = 1L;
        
    }
    
    private class DeleteInstanceFrame extends JFrame { private static final long serialVersionUID = 1L;
        
    }
    
    private Timer timer = new Timer(100, this);
    
    private BufferedImage in = null;
    private BufferedImage out = null;
    
    private SicClient sicClient;
    
    public void startUI() {
        initPanels();
        initComponents();
        initActions();
        initLayout();
        addStyles();
        
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocation(100, 100);
        window.pack();
        window.setVisible(true);
    }
    
    private void initPanels() {
        videoIn = new JPanel() { private static final long serialVersionUID = 1L;
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(in != null) {
                    Image scaledImage = in.getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
                    g.drawImage(scaledImage, 0, 0, null);
                }
            }    
        };
        videoOut = new JPanel() { private static final long serialVersionUID = 1L;
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(out != null) {
                    Image scaledImage = out.getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
                    g.drawImage(scaledImage, 0, 0, null);
                }
            }    
        };
    }

    private void initComponents() {
        textAccountNumber  .setColumns(15);
        textSystemName     .setColumns(15);
        textSystemKey      .setColumns(15);
        machinesList       .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        btnRefreshMachines .setEnabled(false);
        btnNewMachine      .setEnabled(false);
        btnDeleteMachine   .setEnabled(false);
        btnRefreshInstances.setEnabled(false);
        btnNewInstance     .setEnabled(false);
        btnDeleteInstance  .setEnabled(false);
        onOffButton        .setEnabled(false);
        instancesList      .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        Properties config = new Properties();
		try { config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.properties"));
		} catch (Exception e) { } 
        if(config.getProperty("AccountNumber") != null) textAccountNumber.setText(config.getProperty("AccountNumber"));
        if(config.getProperty("SystemName")    != null) textSystemName   .setText(config.getProperty("SystemName"));
        if(config.getProperty("SystemKey")     != null) textSystemKey    .setText(config.getProperty("SystemKey"));
    }
    
    private void initActions() {
        btnReconnect       .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { initSystemInCloud(); } });
        btnRefreshMachines .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { refreshMachines(); } });
        btnNewMachine      .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { new NewMachineFrame()    .setVisible(true); } });
        btnDeleteMachine   .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { new DeleteMachineFrame() .setVisible(true); } });
        btnRefreshInstances.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { refreshInstances(); } });
        btnNewInstance     .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { new NewInstanceFrame()   .setVisible(true); } });
        btnDeleteInstance  .addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { new DeleteInstanceFrame().setVisible(true); } });
        onOffButton        .addItemListener  (new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    webcam.open();
                    timer.start();
                } else {
                    webcam.close();
                    timer.stop();
                }
            }
        });
    }
    
    private void initLayout() {
        videoIn.setPreferredSize(new Dimension(320, 240));
        
        videoOut.setPreferredSize(new Dimension(320, 240));
        
        videoPanels.setLayout(new BoxLayout(videoPanels, BoxLayout.X_AXIS));
        videoPanels.add(videoIn);
        videoPanels.add(videoOut);

        accountNumberBox.add(lblAccountNumber);
        accountNumberBox.add(textAccountNumber);
        systemNameBox   .add(lblSystemName);
        systemNameBox   .add(textSystemName);
        systemKeyBox    .add(lblSystemKey);
        systemKeyBox    .add(textSystemKey);
        
        credentialsBox.add(accountNumberBox);
        credentialsBox.add(systemNameBox);
        credentialsBox.add(systemKeyBox);
        
        reconnectionBox.add(btnReconnect);
        reconnectionBox.add(lblStatus);
        
        credentialsBox.add(reconnectionBox);
        
        credentials.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        credentials.add(credentialsBox);
        
        lblMachines.setAlignmentX(Component.RIGHT_ALIGNMENT);
        machinesBox.add(lblMachines);
        machinesBox.add(machinesList);
        
        machinesBtnsBox.add(btnRefreshMachines);
        machinesBtnsBox.add(btnNewMachine);
        machinesBtnsBox.add(btnDeleteMachine);
        
        machinesBox.add(machinesBtnsBox);
        machines   .add(machinesBox);
        
        lblInstances.setAlignmentX(Component.RIGHT_ALIGNMENT);
        instancesBox.add(lblInstances);
        instancesBox.add(instancesList);
        
        instancesBtnsBox.add(btnRefreshInstances);
        instancesBtnsBox.add(btnNewInstance);
        instancesBtnsBox.add(btnDeleteInstance);
        
        instancesBox.add(instancesBtnsBox);
        instances.add(instancesBox);
        
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.add(credentials);
        controlPanel.add(machines);
        controlPanel.add(instances);
        controlPanel.add(onOffButton);
        
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(videoPanels, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        window.getContentPane().add(mainPanel);
    }
    
    private void addStyles() {
        videoIn .setBackground(Color.LIGHT_GRAY);
        videoIn .setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        videoOut.setBackground(Color.LIGHT_GRAY);
        videoOut.setBorder(BorderFactory.createLineBorder(Color.black, 2));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage bi = webcam.getImage();
            ImageIO.write(bi, "JPEG", baos);
            byte[] bytesOut = baos.toByteArray();
            //Send Data to specified system instance and port
            List<Integer> dims = new ArrayList<>();
            dims.add(1);
//            sicConnection.send(instanceID, "Img In", new Data(DatatypeVip.JPEG, dims, bytesOut));
            this.in = bi;
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoIn.repaint();
    }
    
    private void initSystemInCloud() {
        this.sicClient = SicClientFactory.createClient(textAccountNumber.getText(),
                                                       textSystemName.getText(),
                                                       textSystemKey.getText());
        if(sicClient.isTestPassed()) {
            lblStatus.setText("  OK");
            lblStatus.setForeground(Color.GREEN);
            refreshMachines();
            refreshInstances();
            btnRefreshMachines .setEnabled(true);
            btnNewMachine      .setEnabled(true);
            btnRefreshInstances.setEnabled(true);
        } else {
            lblStatus.setText("  KO");
            lblStatus.setForeground(Color.RED);
            btnRefreshMachines .setEnabled(false);
            btnNewMachine      .setEnabled(false);
            btnDeleteMachine   .setEnabled(false);
            btnRefreshInstances.setEnabled(false);
            btnNewInstance     .setEnabled(false);
            btnDeleteInstance  .setEnabled(false);
            onOffButton        .setEnabled(false);
        }
    }
    
    private void refreshMachines() {
    	for(MachineInfo mi : this.sicClient.getMachines()) {
    		
    	}
    }

    private void refreshInstances() {

    }
    
    // Data received from the system instance
//    @Override
    public void receivedData(String instanceID, String portName, byte[] data) {
        
    }
    
    public static void main(String[] args) {
        WebcamClient wc = new WebcamClient();
        wc.startUI();
    }
}

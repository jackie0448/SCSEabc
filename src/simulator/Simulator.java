package simulator;

import algorithms.ExplorationAlgo;
import cjfastestpath.CCjFastestPath;
import cjfastestpath.Cj2FastestPath;
import ycfastestpath.YcFastestPath;

import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static utils.MapDescriptor.generateMapDescriptor;
import static utils.MapDescriptor.loadMapFromDisk;

/**
 * Simulator for robot navigation in virtual arena.
 *
 * @author Suyash Lakhotia
 */

public class Simulator {
    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    private static BoxLayout boxlayout = null;
    private static Robot bot;

    private static Map realMap = null;              // real map
    private static Map exploredMap = null;          // exploration map
    
    private static ExplorationAlgo exploration= null;
    private static YcFastestPath fastestPath = null;

    private static int timeLimit = 3600;            // time limit in seconds
    private static int coverageLimit = 300;         // coverage limit

    private static final CommMgr comm = CommMgr.getCommMgr();
    private static final boolean realRun = true;

    private static boolean testConnection = true;
    // save waypoint x y
    public static int wpx = 2;
    public static int wpy = 18;
    
    private static String mapName=""; //set currentmap to reset
    private static JButton btn_Exploration;
    private static JButton btn_Reset;
    private static JButton btn_FastestPath;
    
    
    
    private static Container contentPane; //added to load map 
    private static BoxLayout boxlayout2 = null;
    private static JPanel _displayPanel = null;    
    private static JPanel loadMap;
    private static JPanel timeExplo;
    private static JPanel coverageExplo;
    /**
     * Initialises the different maps and displays the application.
     */
    public static void main(String[] args) {
        if (realRun) comm.openConnection();

        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);

        if (!realRun) {
            realMap = new Map(bot);
            realMap.setAllUnexplored();
        }

        exploredMap = new Map(bot);
        exploredMap.setAllUnexplored();
        
        exploration = new ExplorationAlgo(exploredMap, realMap, 
        		bot, coverageLimit, timeLimit);
        
        fastestPath= new YcFastestPath(exploredMap,bot);

        displayEverything();

        Thread testConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (testConnection) {
                  //  comm.sendMsg(null, CommMgr.TEST_CONNECTION); -commented to remove message
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Test Connection Thread Error :: " + e.toString());
                    }
                }
            }
        });
        //testConnectionThread.start(); -commented to remove message
//	    if (realRun) {
//	    	class realFastestPath extends SwingWorker<Integer, String> {
//		    	protected Integer doInBackground() throws Exception {
//			    	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
//			        exploredMap.repaint();
//			
//		            while (true) {
//		                System.out.println("Waiting for FP_START...");
//		                String msg = comm.recvMsg();
//		                if (msg.contains("f")) {
//		                    try {
//		                        String wpCoord = msg.substring(8);
//		                        System.out.println("Waypoint: " + wpCoord);
//		                        String[] wpXy = wpCoord.split(",");
//		                        Simulator.wpx = Integer.parseInt(wpXy[0]);
//		                        Simulator.wpy = Integer.parseInt(wpXy[1]);
//		                    } catch (Exception e) {
//		                    }
//		                    break;
//		                }
//			        }
//			
//			        /*
//			        bot.setBotToFake();
//			        FastestPathAlgo fastestPath = new FastestPathAlgo(exploredMap, bot);
//			        fastestPath.runFastestPath(Simulator.wpy, Simulator.wpx + 5);
//			        exploredMap.setAllExplored();
//			        */
//			
//			        YcFastestPath fastestPath;
//			        fastestPath= new YcFastestPath(exploredMap,bot);
//			        
//			        int waypointRow = RobotConstants.GOAL_ROW;
//			        int waypointCol = RobotConstants.GOAL_COL;
//			        
//			        fastestPath.runfastestPath(RobotConstants.START_ROW, RobotConstants.START_COL,waypointRow,waypointCol ,RobotConstants.GOAL_ROW,RobotConstants.GOAL_COL);
//			
//			        return 111;
//			    }
//		    }
//	    	
//	    	class realExploration extends SwingWorker<Integer, String> {
//		    	protected Integer doInBackground() throws Exception {
//			    	int row, col;
//			
//			        row = RobotConstants.START_ROW;
//			        col = RobotConstants.START_COL;
//			
//			        bot.setRobotPos(row, col);
//			        
//			        exploredMap.setStart(true); //added to paint free cell
//			        exploredMap.repaint();
//			
//			
//			
//			        
//			         //may need to change because no need coveragelimit and timelimit
//			       
//			
//			        testConnection = false;
//			
//			        exploration.runExploration();
//			        generateMapDescriptor(exploredMap);
//			
//			        //comm.sendMsg(null, CommMgr.EX_DONE);
//			
//			        //comm.sendMsg("AAS", CommMgr.INSTRUCTIONS);
//			
//			        new realFastestPath().execute();
//			
//			        return 222;
//			    }
//		    }
//        	while (true) {
//	            System.out.println("Waiting1 for EX_START...");
//	            String msg = comm.recvMsg();
//	            System.out.println(msg);
//	            
//	            if (msg.equals("x")) break;
//	        }
//        	try {
//        		System.out.println("before explo");
//        		new realExploration().execute();
//        	} catch(Exception e) {
//        		System.out.println(" RealExploration Exception");
//        	}
//	    }
//    
	    
    }

    /**
     * Initialises the different parts of the application.
     */
    private static void displayEverything() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("Group 11 MDP Simulator");
        
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();;
        _appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    //   _appFrame.setUndecorated(true);
        _appFrame.setSize(r.width, r.height);
        _appFrame.setVisible(true);
      // _appFrame.setSize(new Dimension(850, 700));
/*        _appFrame.setResizable(false);
        _appFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        _appFrame.setUndecorated(true);
        _appFrame.setVisible(true);
        */
        
        // Center the main frame in the middle of the screen
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //_appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());
        _mapCards.setSize(new Dimension(850, 700));

        // Create the JPanel for the buttons
        _buttons = new JPanel();

        //Create the BoxLayout for button
        BoxLayout boxlayout = new BoxLayout(_buttons, BoxLayout.Y_AXIS);

        _buttons.setLayout(boxlayout);
        _buttons.setBorder(new EmptyBorder(40, 0, 0, 0));
        
        //create a panel to hold different panels
        _displayPanel = new JPanel();
        
        BoxLayout boxlayout2 = new BoxLayout(_displayPanel, BoxLayout.Y_AXIS);

        _buttons.setLayout(boxlayout2);
     //  _displayPanel.setLayout(new GridLayout(0, 1));
       _displayPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 150),BorderFactory.createTitledBorder("Settings")));
       _displayPanel.setPreferredSize(new Dimension(650, 500));

        

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        //contentPane.add(_buttons, BorderLayout.LINE_START);
        contentPane.add(_displayPanel, BorderLayout.LINE_END);
       

        // Initialize the main map view
        initMainLayout();

        // Initialize the buttons
        initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Initialises the main map view by adding the different maps as cards in the CardLayout. Displays realMap
     * by default.
     */
    private static void initMainLayout() {
        if (!realRun) {
            _mapCards.add(realMap, "REAL_MAP");
        }
        _mapCards.add(exploredMap, "EXPLORATION");

        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        if (!realRun) {
            cl.show(_mapCards, "REAL_MAP");
        } else {
            cl.show(_mapCards, "EXPLORATION");
        }
    }
    /**
     * Initialises the JPanel for the buttons.
     */
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout(0, 1));
        addButtons();
    }

    /**
     * Helper method to set particular properties for all the JButtons.
     */
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("sansserif", Font.BOLD, 15));
        btn.setSize(new Dimension(120,100));
        btn.setFocusPainted(false);
    }

    /**
     * Initialises and adds the five main buttons. Also creates the relevant classes (for multithreading) and JDialogs
     * (for user input) for the different functions of the buttons.
     */
    private static void addButtons() {
        if (!realRun) {
        	//add select map panel
   
        	
        	loadMap = new JPanel();
        	//loadMap.setBorder(BorderFactory.createEmptyBorder(80,50,50,200)); //top,left,bottom,right
        	loadMap.setBorder(new CompoundBorder(new EmptyBorder(20, 0, 0, 0),BorderFactory.createTitledBorder("Map")));
        	loadMap.setPreferredSize(new Dimension(450, 100));


            final JTextField loadTF = new JTextField(15);
            JButton loadMapButton = new JButton("Load");
            loadMapButton.setFont(new Font("sansserif", Font.BOLD, 13));
            loadMapButton.setBackground(Color.DARK_GRAY);
            loadMapButton.setForeground(Color.WHITE);
            loadMapButton.setPreferredSize(new Dimension(70, 20));

            loadMapButton.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                	//changed
                  // loadMap.setVisible(false);
                	
                	//remove it 
                   contentPane = _appFrame.getContentPane();
                   contentPane.remove(loadMap);
                   _appFrame.invalidate();
                   _appFrame.validate();
                   
                   
                    if(loadTF.getText()!=null && !loadTF.getText().equalsIgnoreCase("")) {
                    mapName =loadTF.getText();
                    loadMapFromDisk(realMap,mapName); 
                    loadMapFromDisk(exploredMap,mapName); //so reset will work
                    exploredMap.setAllUnexplored(); //need to reset the exploredMap everytime reload a new map
                    btn_Exploration.setEnabled(true); // to allow retry
                    CardLayout cl = ((CardLayout) _mapCards.getLayout());
                    cl.show(_mapCards, "REAL_MAP");
                    realMap.repaint();
                    _appFrame.repaint(); //added
                    
                    }
                    else {
                    	 JOptionPane.showMessageDialog(_appFrame, "Please enter a map name", "Map name not entered",
                    		        JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            JLabel fileName = new JLabel("Map Name: ");
            fileName.setFont(new Font("sansserif", Font.PLAIN, 15));  
            fileName.setPreferredSize(new Dimension(160, 50));
            

            loadMap.add(fileName,BorderLayout.WEST);
            loadMap.add(loadTF);
            loadMap.add(loadMapButton);
            loadMap.setVisible(true);
            loadMap.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            _displayPanel.add(loadMap);
            _appFrame.invalidate();
            _appFrame.validate();
        	
        	
        	

            
            

            
        }

        // FastestPath Class for Multithreading
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                if (realRun) {
                    while (true) {
                        System.out.println("Waiting for FP_START...");
                        String msg = comm.recvMsg();
                        if (msg.contains(CommMgr.FP_START)) {
                            try {
                                String wpCoord = msg.substring(8);
                                System.out.println("Waypoint: " + wpCoord);
                                String[] wpXy = wpCoord.split(",");
                                Simulator.wpx = Integer.parseInt(wpXy[0]);
                                Simulator.wpy = Integer.parseInt(wpXy[1]);
                            } catch (Exception e) {
                            }
                            break;
                        }
                    }
                }

                /*
                bot.setBotToFake();
                FastestPathAlgo fastestPath = new FastestPathAlgo(exploredMap, bot);
                fastestPath.runFastestPath(Simulator.wpy, Simulator.wpx + 5);
                exploredMap.setAllExplored();
                */
  
                
                
                int waypointRow = RobotConstants.GOAL_ROW;
                int waypointCol = RobotConstants.GOAL_COL;
                
                fastestPath.runfastestPath(RobotConstants.START_ROW, RobotConstants.START_COL,waypointRow,waypointCol ,RobotConstants.GOAL_ROW,RobotConstants.GOAL_COL);

                return 222;
            }
        }

        // Exploration Class for Multithreading
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                int row, col;

                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                bot.setRobotPos(row, col);
                
                exploredMap.setStart(true); //added to paint free cell
                exploredMap.repaint();
        

        
//                ExplorationAlgo exploration;
//                exploration = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit); //may need to change because no need coveragelimit and timelimit
               

                testConnection = false;

                exploration.runExploration();
                generateMapDescriptor(exploredMap);

                //comm.sendMsg(null, CommMgr.EX_DONE);

                //comm.sendMsg("AAS", CommMgr.INSTRUCTIONS);

                if (realRun) {
                    new FastestPath().execute();
                
                }

                return 111;
            }
        }
        
     	



        // Exploration Button
        btn_Exploration = new JButton("Exploration"); //changed
        btn_FastestPath = new JButton("Fastest Path"); //changed

        btn_Exploration.setBackground(Color.DARK_GRAY); //changed
        btn_Exploration.setForeground(Color.WHITE);
        btn_Exploration.setOpaque(true);
        btn_Exploration.setBorderPainted(false);
        btn_Exploration.setPreferredSize(new Dimension(450, 50));
        formatButton(btn_Exploration);
        btn_Exploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (!btn_Exploration.isEnabled()) return;
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                btn_Exploration.setEnabled(false); 
                new Exploration().execute();
               
            }
        });
        _buttons.add(btn_Exploration);
        _buttons.add(Box.createRigidArea(new Dimension(0, 10)));

        // Fastest Path Button
        btn_FastestPath.setBackground(Color.DARK_GRAY);
        btn_FastestPath.setForeground(Color.WHITE);
        btn_FastestPath.setOpaque(true);
        btn_FastestPath.setBorderPainted(false);
        btn_FastestPath.setPreferredSize(new Dimension(450, 50));
        formatButton(btn_FastestPath);
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new FastestPath().execute();
            }
        });
        _buttons.add(btn_FastestPath);
        _buttons.add(Box.createRigidArea(new Dimension(0, 10)));

        
        
        //added to reset the exploration to try again
        // Reset Button
        btn_Reset = new JButton("Reset"); //changed


        btn_Reset.setBackground(Color.DARK_GRAY); //changed
        btn_Reset.setForeground(Color.WHITE);
        btn_Reset.setOpaque(true);
        btn_Reset.setBorderPainted(false);
        btn_Reset.setPreferredSize(new Dimension(450, 50));
        formatButton(btn_Reset);
        btn_Reset.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                loadMapFromDisk(realMap,mapName);
                loadMapFromDisk(exploredMap,mapName);
                exploredMap.setAllUnexplored();
                btn_Exploration.setEnabled(true); // to allow retry
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
                exploredMap.repaint();
                realMap.repaint();
                _appFrame.repaint(); //added
               
            }
        });
        _buttons.add(btn_Reset);
        _buttons.add(Box.createRigidArea(new Dimension(0, 10)));

//        May not need it since not mentioned in the lecture

        // TimeExploration Class for Multithreading
        class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                
                exploredMap.setStart(true); //added to paint free cell
                exploredMap.repaint();

                ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);
                timeExplo.runExploration();

                generateMapDescriptor(exploredMap);

                return 333;
            }
        }

        
        // Time-limited Exploration Panel
    	timeExplo = new JPanel();
    	timeExplo.setBorder(new CompoundBorder(new EmptyBorder(40, 0, 0, 0),BorderFactory.createTitledBorder("Time Limited")));
        final JTextField timeTF = new JTextField(15);
        JButton timeSaveButton = new JButton("Run");
        timeSaveButton.setFont(new Font("sansserif", Font.BOLD, 13));
        timeSaveButton.setBackground(Color.DARK_GRAY);// changed
        timeSaveButton.setForeground(Color.WHITE);
        timeSaveButton.setPreferredSize(new Dimension(70, 20));
        
        timeExplo.setPreferredSize(new Dimension(450, 120));


        timeSaveButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
              //  timeExploDialog.setVisible(false);
            	
               	
            	//remove it 
               contentPane = _appFrame.getContentPane();
               contentPane.remove(timeExplo);
               _appFrame.invalidate();
               _appFrame.validate();
               
                String time = timeTF.getText();
                String[] timeArr = time.split(":");
                timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new TimeExploration().execute();


            }
        });
/*
        timeExploDialog.add(new JLabel("Time Limit (in MM:SS): "));
        timeExploDialog.add(timeTF);
        timeExploDialog.add(timeSaveButton);
        timeExploDialog.setVisible(true);
        */
        
        JLabel timeLimitLabel = new JLabel("Time Limit (in MM:SS): ");
        timeLimitLabel.setFont(new Font("sansserif", Font.PLAIN, 15));
        timeLimitLabel.setAlignmentX(JLabel.LEFT);
        timeLimitLabel.setPreferredSize(new Dimension(160, 50));
        
        timeExplo.add(timeLimitLabel);
        timeExplo.add(timeTF);
        timeExplo.add(timeSaveButton);
        timeExplo.setVisible(true);
        _displayPanel.add(timeExplo);
        _appFrame.invalidate();
        _appFrame.validate();

        
        
        
      

        // CoverageExploration Class for Multithreading
        class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                
                
                exploredMap.setStart(true); //added to paint free cell
                exploredMap.repaint();

                ExplorationAlgo coverageExplo = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);
                coverageExplo.runExploration();

                generateMapDescriptor(exploredMap);

                return 444;
            }
        }

        // Coverage-limited Exploration Panel 
        coverageExplo = new JPanel();
        coverageExplo.setBorder(new CompoundBorder(new EmptyBorder(40, 0, 0, 0),BorderFactory.createTitledBorder("Coverage Limited")));
        coverageExplo.setPreferredSize(new Dimension(450, 120));

        
        

      //	coverageExplo.setBorder(BorderFactory.createEmptyBorder(80,50,50,150)); //top,left,bottom,right

    	
        final JTextField coverageTF = new JTextField(15);
        JButton coverageSaveButton = new JButton("Run");
        coverageSaveButton.setFont(new Font("sansserif", Font.BOLD, 13));
        coverageSaveButton.setBackground(Color.DARK_GRAY);
        coverageSaveButton.setForeground(Color.WHITE);
        coverageSaveButton.setPreferredSize(new Dimension(70, 20));

        coverageSaveButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
             //   coverageExploDialog.setVisible(false);
            	
              	//remove it 
                contentPane = _appFrame.getContentPane();
                contentPane.remove(coverageExplo);
                _appFrame.invalidate();
                _appFrame.validate();
                
                coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * MapConstants.MAP_SIZE / 100.0);
                new CoverageExploration().execute();
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
            }
        });

        /*
        coverageExploDialog.add(new JLabel("Coverage Limit (% of maze): "));
        coverageExploDialog.add(coverageTF);
        coverageExploDialog.add(coverageSaveButton);
        coverageExploDialog.setVisible(true);
        */
        
        JLabel coverageLimitLabel = new JLabel("Coverage Limit (%):  ");
        coverageLimitLabel.setFont(new Font("sansserif", Font.PLAIN, 15));
        coverageLimitLabel.setAlignmentX(JLabel.LEFT);
        coverageLimitLabel.setPreferredSize(new Dimension(160, 50));
        coverageExplo.add(coverageLimitLabel);
        coverageExplo.add(coverageTF);
        coverageExplo.add(coverageSaveButton);
        coverageExplo.setVisible(true);
        
      
        _displayPanel.add(coverageExplo);
        _displayPanel.add(_buttons);
        _appFrame.invalidate();
        _appFrame.validate();
        
       
        
    }
   
}

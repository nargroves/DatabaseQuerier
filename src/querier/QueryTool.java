package querier;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class QueryTool {

	HashMap<JCheckBox,ArrayList<JCheckBox>>map = new HashMap<JCheckBox,ArrayList<JCheckBox>>();
	JFrame outline = new JFrame();
	JPanel entirePanel = new JPanel(new GridBagLayout());
	Connection activeConnection = null;
	JLabel lUsername = new JLabel("Username: ");
	JLabel lPassword = new JLabel("Password: ");
	JLabel lAddress = new JLabel("IP Address: ");
	JLabel lPort = new JLabel("Port Number: ");
	JLabel lDBName = new JLabel("Database Name: ");
	JTextField tfUsername = new JTextField(10);
	JPasswordField pfPassword = new JPasswordField(10);
	JTextField tfAddress = new JTextField(10);
	JTextField tfPort = new JTextField(10);
	JTextField tfDBName = new JTextField(10);
	JButton bConnect = new JButton("Test");
	JButton bSave = new JButton("Save");
	JButton bExecute = new JButton("Execute");
    JButton bAddRelationship = new JButton("Add Relationship");
	JPanel pWhatTables = new JPanel();
	JPanel pWhatColumns = new JPanel();
	JPanel pWhatRelationships = new JPanel();
	final JTextArea textArea = new JTextArea(5,50);
	JScrollPane spTables, spColumns, spRelationships, spQuery;
	
	String databaseName = "";

	
	public void setUpTool() {
		buildInternalGUI();
		
		//Sets up the JFrame, adds the appropriate panel to it, sets the sizes, and makes it visible
		outline.setTitle("Database Tool");
		outline.add(entirePanel);
		outline.pack();
		outline.setVisible(true);
		outline.setSize(new Dimension(1600,900));
		outline.setMinimumSize(new Dimension(1100,500));
		outline.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void buildInternalGUI() {
		clearGUI();
		addFileMenu();
		loadDatabaseDetails();
		activeConnection = testConnection();
		if(activeConnection == null) {
			createBlankUI();
		}
		else {
			buildUI();
		}
	}
	
	public void clearGUI() {
		entirePanel = new JPanel(new GridBagLayout());
	}
	
	public void createBlankUI() {
		JLabel lNoConnection = new JLabel("No Connection!");
		entirePanel.add(lNoConnection);
	}
	
	public void buildUI() {
		bExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testQuery();
			}
		});
		
		bAddRelationship.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pWhatRelationships = createNewRow(pWhatRelationships);
			}
		});
		
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		pWhatTables.setLayout(new BoxLayout(pWhatTables, BoxLayout.Y_AXIS));
		pWhatColumns.setLayout(new BoxLayout(pWhatColumns, BoxLayout.Y_AXIS));


		JLabel lWhatTables = new JLabel("What Tables?");
		JLabel lWhatColumns = new JLabel("What Columns?");
		JLabel lWhatRelationships = new JLabel("What Relationships?");
		getTablesFromDatabase();
		addTableCheckBoxes();

		spTables = new JScrollPane(pWhatTables,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spColumns = new JScrollPane(pWhatColumns,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		pWhatRelationships.setPreferredSize(new Dimension(200,200));
		spRelationships = new JScrollPane(pWhatRelationships,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		spQuery = new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		spTables.setPreferredSize(new Dimension(120,200));
		spColumns.setPreferredSize(new Dimension(120,200));
		spRelationships.setPreferredSize(new Dimension(200,200));
		spTables.setViewportView(pWhatTables);
		spColumns.setViewportView(pWhatColumns);

	    GridBagConstraints c = new GridBagConstraints();
	    c.gridx = 0;
	    c.gridy = 0;
		entirePanel.add(lWhatTables,c);
		c.gridx = 50;
	    c.gridy = 0;
		entirePanel.add(lWhatColumns,c);
		c.gridx = 75;
	    c.gridy = 0;
		entirePanel.add(lWhatRelationships,c);
		c.gridx = 75;
	    c.gridy = 10;
		entirePanel.add(bAddRelationship,c);
		c.gridx = 0;
	    c.gridy = 20;
		entirePanel.add(spTables,c);
		c.gridx = 50;
	    c.gridy = 20;
		entirePanel.add(spColumns,c);
		c.gridx = 75;
	    c.gridy = 20;
		entirePanel.add(spRelationships,c);
		
		c.gridx = 0;
	    c.gridy = 40;
	    c.gridwidth = 100;
		entirePanel.add(spQuery,c);
		c.gridx = 0;
	    c.gridy = 60;
	    c.gridwidth = 1;
		entirePanel.add(bExecute,c);
	}
	
	public JPanel createNewRow(JPanel panel) {
		String [] operations = {" = ", " != "};
		JComboBox firstOption = new JComboBox();
		JComboBox operation = new JComboBox(operations);
		JComboBox secondOption = new JComboBox();
		panel.add(firstOption);
		panel.add(operation);
		panel.add(secondOption);
		return panel;
	}
	
	public void testQuery() {
		try {
			Statement st = activeConnection.createStatement();
	        final ResultSet rs = st.executeQuery(textArea.getText());
	        
	        JTable table = new JTable(buildTableModel(rs));
	        table.setEnabled(false);
	        JOptionPane.showMessageDialog(null, new JScrollPane(table));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {

	    ResultSetMetaData metaData = rs.getMetaData();

	    // names of columns
	    Vector<String> columnNames = new Vector<String>();
	    int columnCount = metaData.getColumnCount();
	    for (int column = 1; column <= columnCount; column++) {
	        columnNames.add(metaData.getColumnName(column));
	    }

	    // data of the table
	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	    while (rs.next()) {
	        Vector<Object> vector = new Vector<Object>();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            vector.add(rs.getObject(columnIndex));
	        }
	        data.add(vector);
	    }

	    return new DefaultTableModel(data, columnNames);

	}
	
	public void addTableCheckBoxes() {
		for (Entry<JCheckBox, ArrayList<JCheckBox>> entry : map.entrySet()) { 
			pWhatTables.add(entry.getKey());
		}
	}
	
	private void getTablesFromDatabase() {
		try {
			Statement st = activeConnection.createStatement();
			Statement st2 = activeConnection.createStatement();
	        final ResultSet rs = st.executeQuery("SELECT DISTINCT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='"+ databaseName +"';");
			while(rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				final JCheckBox chk = new JCheckBox(tableName);
				chk.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							if(chk.isSelected()) {
								addColumnsToPanel(chk);
							}
							else {
								removeColumnsFromPanel(chk);
							}
							updateQuery();
						}
						catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				ArrayList<JCheckBox>columns = new ArrayList<JCheckBox>();
		        final ResultSet rs2 = st2.executeQuery("SELECT * FROM " + tableName + " LIMIT 1;");
		        ResultSetMetaData rsmd = rs2.getMetaData();
		        int columnCount = rsmd.getColumnCount();
				for(int i = 1; i < columnCount + 1; i++) {
					final JCheckBox chk2 = new JCheckBox(tableName+"."+rsmd.getColumnName(i));
					chk2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								/*if(chk.isSelected()) {
									addColumnsToPanel(chk.getText());
								}
								else {
									removeColumnsFromPanel(chk.getText());
								}*/
								updateQuery();
							}
							catch(Exception ex) {
								ex.printStackTrace();
							}
						}
					});
					columns.add(chk2);
				}
				map.put(chk,columns);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateQuery() {
		textArea.setText("");
		Component [] columnComponents = pWhatColumns.getComponents();
		Component [] tableComponents = pWhatTables.getComponents();
		String columnString = "  ", tableString = "  ";
		boolean selectAll = true;
		for(int i = 0; i < columnComponents.length; i++) {
			if(((JCheckBox)columnComponents[i]).isSelected()) {
				columnString += ((JCheckBox)columnComponents[i]).getText() + ", ";
				selectAll = false;
			}
		}
		for(int i = 0; i < tableComponents.length; i++) {
			if(((JCheckBox)tableComponents[i]).isSelected()) {
				tableString += ((JCheckBox)tableComponents[i]).getText() + ", ";
			}
		}
		columnString = columnString.substring(0,columnString.length()-2);
		tableString = tableString.substring(0,tableString.length()-2);
		if(selectAll == true) {
			textArea.setText("SELECT * FROM " + tableString);
		}
		else {
			textArea.setText("SELECT " + columnString + " FROM " + tableString);
		}
	}
	
	public void addColumnsToPanel(JCheckBox chk) {
		addCheckboxes(chk);
	}
	
	public void removeColumnsFromPanel(JCheckBox chk) {
		removeCheckboxes(chk);
	}
	
	public void addCheckboxes(JCheckBox chk) {
		pWhatColumns.setSize(new Dimension(150,200));

		ArrayList<JCheckBox>columnsToAdd = map.get(chk);
		for(JCheckBox each : columnsToAdd) {
			pWhatColumns.add(each);
		}	
	}
	
	public void removeCheckboxes(JCheckBox chk) {
		pWhatColumns.setSize(new Dimension(150,200));

		ArrayList<JCheckBox>columnsToRemove = map.get(chk);
		for(JCheckBox each : columnsToRemove) {
			pWhatColumns.remove(each);
		}
	}
	
	public void addFileMenu() {
		JMenu file = new JMenu("File");
		file.setMnemonic('F');
		JMenuItem dbConnection = new JMenuItem("Database Connection");
		dbConnection.setMnemonic('D');
		file.add(dbConnection);
		
		//adding action listener to menu items
		dbConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDatabaseConfigPanel();
				loadDatabaseDetails();
			}
		});
		JMenuBar bar = new JMenuBar();
		bar.add(file);
		outline.setJMenuBar(bar);
	}
	
	public void openDatabaseConfigPanel() {
		JFrame configPanelOutline = new JFrame();
		JPanel configPanel = new JPanel(new GridBagLayout());
	
		bConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activeConnection = testConnection();
				if(activeConnection == null) {
		            JOptionPane.showConfirmDialog(null, "Connection Failed! \n Please make changes and retry!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else {	            
					JOptionPane.showConfirmDialog(null, "Connection Succeeded!", "Success", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		bSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDatabaseDetails();
			}
		});

	    GridBagConstraints c = new GridBagConstraints();
	    c.gridx = 0;
	    c.gridy = 0;
		configPanel.add(lUsername,c);
		c.gridx = 10;
	    c.gridy = 0;
		configPanel.add(tfUsername,c);
		c.gridx = 0;
	    c.gridy = 10;
		configPanel.add(lPassword,c);
		c.gridx = 10;
	    c.gridy = 10;
		configPanel.add(pfPassword,c);
		c.gridx = 0;
	    c.gridy = 20;
		configPanel.add(lAddress,c);
		c.gridx = 10;
	    c.gridy = 20;
		configPanel.add(tfAddress,c);
		c.gridx = 0;
	    c.gridy = 30;
		configPanel.add(lPort,c);
		c.gridx = 10;
	    c.gridy = 30;
		configPanel.add(tfPort,c);
		c.gridx = 0;
	    c.gridy = 40;
		configPanel.add(lDBName,c);
		c.gridx = 10;
	    c.gridy = 40;
		configPanel.add(tfDBName,c);
		c.gridx = 0;
	    c.gridy = 50;
		configPanel.add(bConnect,c);
		c.gridx = 10;
	    c.gridy = 50;
		configPanel.add(bSave,c);
		configPanelOutline.add(configPanel);
		configPanelOutline.pack();
		configPanelOutline.setVisible(true);
		configPanelOutline.setSize(new Dimension(250,250));
		configPanelOutline.setResizable(false);
	}
	
	public Connection testConnection() {
		try {
            //get the password from the field and loop through to decode the password
            String driver = "", passwordDecoded = "", url = "", ipaddress = tfAddress.getText(), portnumber = tfPort.getText(), username = tfUsername.getText(), databasename = tfDBName.getText();
            databaseName = databasename;
            char[] password = pfPassword.getPassword();
            for (int i = 0; i < password.length; i++) {
                passwordDecoded += password[i];
            }//END for

            /*//sets up global variables for url and driver
            driver = "org.postgresql.Driver";
            url = "jdbc:postgresql://" + ipaddress + ":" + portnumber + "/" + databasename + "?user=" + username + "&password=" + passwordDecoded;
			*/
            
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + ipaddress + ":" + portnumber + "/" + databasename;
			
            //creates connection to database
            Connection con = null;
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(url, username, passwordDecoded);
            Statement st = con.createStatement();
            @SuppressWarnings("unused")
			ResultSet rs = null;
            rs = st.executeQuery("SELECT DISTINCT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='"+ databasename +"';");
            //rs = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_name not like 'pg_%' AND table_name not like 'sql_%' ORDER BY table_name;");
            return con;
        }//END try
        catch (Exception e) {
        	e.printStackTrace();
        	return null;
        }//END catch
	}
	
	public void saveDatabaseDetails() {
        String ipaddress = tfAddress.getText(), portnumber = tfPort.getText(), username = tfUsername.getText(), databasename = tfDBName.getText();
        char[] password = pfPassword.getPassword();

        //loop through to decode the password
        String actualPassword = "";
        for (int i = 0; i < password.length; i++) {
            actualPassword += password[i];
        }

        //actually physically writes the information to the file
        String pipeDelim = "||";
        try {
            File file = new File("default.txt");
            FileOutputStream fos = new FileOutputStream(file);
            if (file.exists()) {
                fos.write(username.getBytes());
                fos.write(pipeDelim.getBytes());
                fos.write(actualPassword.getBytes());
                fos.write(pipeDelim.getBytes());
                fos.write(portnumber.getBytes());
                fos.write(pipeDelim.getBytes());
                fos.write(ipaddress.getBytes());
                fos.write(pipeDelim.getBytes());
                fos.write(databasename.getBytes());
                fos.write(pipeDelim.getBytes());
                fos.flush();
                fos.close();
                JOptionPane.showConfirmDialog(null, "Saved Default Profile!", "Success", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception npe) {
            JOptionPane.showConfirmDialog(null, "File not saved correctly! \n Please try again!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
		buildInternalGUI();
	}
	
	public void loadDatabaseDetails() {
        try {
            File file = new File("default.txt");
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            String parseThis = fileData.toString();
            String[] parsed = parseThis.split("\\|\\|");
            tfUsername.setText(parsed[0]);
            pfPassword.setText(parsed[1]);
            tfPort.setText(parsed[2]);
            tfAddress.setText(parsed[3]);
            tfDBName.setText(parsed[4]);
        }
        catch (Exception e) {
            //JOptionPane.showConfirmDialog(null, "Couldn't load file! Please try again later!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
	}
}

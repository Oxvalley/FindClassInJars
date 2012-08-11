package oxvalley;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class FindClass implements KeyListener
{
   private static String version = "2.1";

   JFrame mainFrame;

   JTextField textJarDir;
   JTextField textClassName;
   JTextArea textResult;

   public static final String APP_NAME = "Find Jar v " + version;
   protected static final String lcOSName = System.getProperty("os.name")
         .toLowerCase();
   protected static final boolean IS_MAC = lcOSName.startsWith("mac os x");
   private final static int MIN_WINDOWS_WIDTH = 800;
   private final static int MIN_WINDOWS_HEIGHT = 600;

   /**
    * @param args
    * @throws Exception
    */
   public static void main(final String[] args) throws Exception
   {
      if (IS_MAC)
      {
         // take the menu bar off the jframe
         System.setProperty("apple.laf.useScreenMenuBar", "true");

         // set the name of the application menu item
         System.setProperty("com.apple.mrj.application.apple.menu.about.name",
               APP_NAME);
      }

      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      new FindClass().open();
   }

   private boolean processingFlag = false;

   private void saveProperties()
   {
      // Write properties file.
      try
      {
         properties.setProperty("jarDir", textJarDir.getText());
         properties.setProperty("SearchFor", textClassName.getText());
         properties.store(new FileOutputStream(propFileName), null);
      }
      catch (IOException e2)
      {
         System.out.println("Could not store property file " + propFileName);
      }
   }

   private static String propFileName = "prefs.properties";
   Properties properties = new Properties();

   private JButton btnSearch;

   private JButton btnCancel;

   private void open()
   {
      mainFrame = new JFrame(APP_NAME);

         mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/findjar.gif"));

      WindowListener wndCloser = new WindowAdapter()
      {
         @Override
         public void windowClosing(final WindowEvent e)
         {
            if (!processingFlag)
            {
               saveProperties();

               System.exit(0);
            }
         }

      };
      mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mainFrame.addWindowListener(wndCloser);
      ComponentListener compListener = new ComponentAdapter()
      {
         @Override
         public void componentResized(final ComponentEvent e)
         {
            Component c = (Component) e.getSource();
            if (c instanceof JFrame)
            {
               Dimension newSize = c.getSize();
               int newWidth = (int) newSize.getWidth();
               int newHeight = (int) newSize.getHeight();
               if (newWidth < MIN_WINDOWS_WIDTH)
               {
                  newWidth = MIN_WINDOWS_WIDTH;
               }
               if (newHeight < MIN_WINDOWS_HEIGHT)
               {
                  newHeight = MIN_WINDOWS_HEIGHT;
               }
               c.setSize(newWidth, newHeight);
            }
         }
      };
      mainFrame.addComponentListener(compListener);

      // Read properties file.
      try
      {
         properties.load(new FileInputStream(propFileName));
      }
      catch (IOException e)
      {
         System.out.println("Could not load property file " + propFileName);
      }

      JPanel mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBorder(BorderFactory.createTitledBorder(""));

      GridBagConstraints c = new GridBagConstraints();
      // c.insets = new Insets(5, 5, 5, 5);
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0;
      c.weighty = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.WEST;

      textJarDir = new JTextField(properties.getProperty("jarDir"));
      mainPanel.add(textJarDir, c);

      c.gridx = 1;
      c.gridy = 0;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      JButton btnSelectdir = new JButton("Select Folder");
      mainPanel.add(btnSelectdir, c);
      btnSelectdir.addActionListener(new ActionListener()
      {
         public void actionPerformed(final ActionEvent arg0)
         {
            if (IS_MAC)
            {
               selectDistFileAWT();
            }
            else
            {
               selectDistFileSwing();
            }
         }
      });

      c.gridx = 0;
      c.gridy = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1;
      textClassName = new JTextField(properties.getProperty("SearchFor"));
      textClassName.addKeyListener(this);
      mainPanel.add(textClassName, c);

      c.gridx = 1;
      c.gridy = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      btnSearch = new JButton("Search");
      mainPanel.add(btnSearch, c);
      btnSearch.addActionListener(new ActionListener()
      {
         public void actionPerformed(final ActionEvent arg0)
         {
            searchActions();
         }

      });

      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 1;
      c.weighty = 0;
      c.gridwidth = 1;
      c.fill = GridBagConstraints.NONE;
      mainPanel.add(new JLabel("Search Result"), c);
      
      c.gridx = 1;
      c.gridy = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      btnCancel = new JButton("Cancel");
      mainPanel.add(btnCancel, c);
      btnSearch.addActionListener(new ActionListener()
      {
         public void actionPerformed(final ActionEvent arg0)
         {
            cancelActions();
         }
      });

      textResult = new JTextArea();
      textResult.setEnabled(true);
      c.gridx = 0;
      c.gridy = 3;
      c.weightx = 1;
      c.weighty = 1;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.BOTH;
      mainPanel.add(new JScrollPane(textResult), c);

      mainFrame.getContentPane().add(mainPanel);

      mainFrame.setSize(MIN_WINDOWS_WIDTH, MIN_WINDOWS_HEIGHT);
      mainFrame.setVisible(true);
   }

   FileFilter fileFilter = new FileFilter()
   {
      public boolean accept(final File f)
      {
         if (f.isDirectory())
         {
            return true;
         }
         String name = f.getName().toLowerCase();
         if (name.endsWith(".jar") || name.endsWith(".zip"))
         {
            return true;
         }
         return false;
      }
   };

   private void searchJarDir(final File jarDir, final String className,
         final int level)
   {
      File[] fileArray = jarDir.listFiles(fileFilter);
      if (fileArray != null && fileArray.length > 0)
      {
         for (File sub : fileArray)
         {
            if (sub.isDirectory())
            {
               searchJarDir(sub, className, level + 1);
            }
            else
            {
               searchJarFile(sub, className);
            }
         }
      }
      if (level == 0)
      {
         textResult.setText(textResult.getText() + "Search completed" + "\n");
      }
      return;
   }

   private void searchActions()
   {
      if (textJarDir.getText().length() == 0)
      {
         JOptionPane.showMessageDialog(null,
               "Please enter a jar folder to find!", "Error",
               JOptionPane.ERROR_MESSAGE);
      }
      if (textClassName.getText().length() == 0)
      {
         JOptionPane.showMessageDialog(null,
               "Please enter class name to find!", "Error",
               JOptionPane.ERROR_MESSAGE);
      }
      final File jarDir = new File(textJarDir.getText());
      if (!jarDir.exists() || !jarDir.isDirectory())
      {
         JOptionPane.showMessageDialog(null, textJarDir.getText()
               + " is not exist or not a folder!", "Error",
               JOptionPane.ERROR_MESSAGE);
      }

      textResult.setText(null);

      new Thread()
      {
         @Override
         public void run()
         {
            mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            btnSearch.setEnabled(false);

            searchJarDir(jarDir, textClassName.getText(), 0);
            mainFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            btnSearch.setEnabled(true);
         }
      }.start();
   }

   private void cancelActions()
   {
      System.out.println("Cancel");
      
   }
   
   private void searchJarFile(final File jarFile, final String className2)
   {

      String className = className2.trim();
      String nameForClass = className + ".class";
      String nameForSource = className + ".java";
      try
      {
         ZipFile zipFile = new ZipFile(jarFile);
         Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
         while (zipEntries.hasMoreElements())
         {
            ZipEntry entry = zipEntries.nextElement();
            String name = entry.getName();
            // Process the name, here we just print it out
            System.out.println(name);
            if (name.endsWith(nameForClass))
            {
               addSearchResult("file", name, jarFile);
            }
            else if (name.endsWith(nameForSource))
            {
               addSearchResult("source file", name, jarFile);
            }
            else if (name.contains(className))
            {
               addSearchResult("part of file name", name, jarFile);
            }
            else if (name.toLowerCase().contains(className.toLowerCase()))
            {
               addSearchResult("case insensitive part of file name", name,
                     jarFile);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         addError("problem with jar file: " + jarFile.getName(), e);
      }
   }

   private void addSearchResult(String heading, String name, final File jarFile)
   {
      textResult.append("Found " + heading + " " + name + " in: "
            + jarFile.getAbsolutePath() + "\n");
   }

   private void addError(String message, final Exception e)
   {
      textResult.append(String.format("%s - exception message: %s\n", message,
            e.getMessage()));
   }

   protected void selectDistFileAWT()
   {
      FileDialog fd = new FileDialog(mainFrame, "Select Jar Folder",
            FileDialog.LOAD);
      fd.setAlwaysOnTop(true);
      System.setProperty("apple.awt.fileDialogForDirectories", "true");
      fd.setVisible(true);
      System.setProperty("apple.awt.fileDialogForDirectories", "false");
      String fs = fd.getFile();

      String fileName = (fs != null) ? fd.getDirectory() + fs : "";
      textJarDir.setText(fileName);
   }

   private void selectDistFileSwing()
   {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);
      int option = chooser.showSaveDialog(mainFrame);
      if (option == JFileChooser.APPROVE_OPTION)
      {
         String fileName = (chooser.getSelectedFile() != null) ? chooser
               .getSelectedFile().getPath() : "";
         textJarDir.setText(fileName);
      }
   }

   public void keyTyped(KeyEvent e)
   {
      // TODO Auto-generated method stub

   }

   public void keyPressed(KeyEvent e)
   {
      int key = e.getKeyCode();
      if (key == KeyEvent.VK_ENTER)
      {
         searchActions();
      }
   }

   public void keyReleased(KeyEvent e)
   {
      // TODO Auto-generated method stub

   }

}
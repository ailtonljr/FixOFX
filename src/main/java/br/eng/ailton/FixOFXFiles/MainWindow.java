package br.eng.ailton.FixOFXFiles;

import java.awt.EventQueue;
import java.io.File;import java.io.FilenameFilter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainWindow {

	private JFrame frmOfxFix;
	private JTextField textFile;
	private JTextField textLimite;

	private JFileChooser ofxFileChooser = new JFileChooser();
	
	private JTextField textDir;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmOfxFix.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmOfxFix = new JFrame();
		frmOfxFix.setTitle("OFX Fix");
		frmOfxFix.setBounds(100, 100, 602, 275);
		frmOfxFix.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmOfxFix.getContentPane().setLayout(null);
		
		JLabel lblFilename = new JLabel("Arquivo");
		lblFilename.setBounds(10, 38, 56, 16);
		frmOfxFix.getContentPane().add(lblFilename);
		
		textFile = new JTextField();
		textFile.setBounds(78, 33, 445, 26);
		frmOfxFix.getContentPane().add(textFile);
		textFile.setColumns(10);
		
		JButton btnAbrirArquivo = new JButton("...");
		btnAbrirArquivo.setBounds(521, 33, 45, 29);
		
		btnAbrirArquivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				FileFilter filter = new FileNameExtensionFilter("Arquivo OFX","ofx");
			    
				ofxFileChooser.setFileFilter(filter);
				
				int result = ofxFileChooser.showOpenDialog(frmOfxFix);
				
				if(result == JFileChooser.APPROVE_OPTION)
				{
					textFile.setText(ofxFileChooser.getSelectedFile().getAbsolutePath());
					ofxFileChooser.setCurrentDirectory(ofxFileChooser.getSelectedFile().getParentFile());
				}
				
			    
				
			}
		});
		
		frmOfxFix.getContentPane().add(btnAbrirArquivo);
		
		JButton btnCorrigirCartoDe = new JButton("Corrigir Cartão de Crédito");
		btnCorrigirCartoDe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String ofxFile = textFile.getText();
		        
		        OFXEditor editor = new OFXEditor();
		        
		        editor.setOfxFile(new File(ofxFile));
		        
		        editor.FixVisa();
		        JOptionPane.showMessageDialog(frmOfxFix, "Arquivo corrigido.");
				
			}
		});
		btnCorrigirCartoDe.setBounds(4, 66, 206, 29);
		frmOfxFix.getContentPane().add(btnCorrigirCartoDe);
		
		JLabel lblLimite = new JLabel("Limite");
		lblLimite.setBounds(209, 71, 39, 16);
		frmOfxFix.getContentPane().add(lblLimite);
		
		textLimite = new JTextField();
		textLimite.setText("5000");
		textLimite.setBounds(260, 66, 88, 26);
		frmOfxFix.getContentPane().add(textLimite);
		textLimite.setColumns(10);
		
		JButton btnCorrigirSaldoDa = new JButton("Corrigir saldo da Caixa");
		btnCorrigirSaldoDa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String ofxFile = textFile.getText();
				double limite = Double.parseDouble( textLimite.getText());
		        
				
		        OFXEditor editor = new OFXEditor();
		        
		        editor.setOfxFile(new File(ofxFile));
		        
		        editor.FixBalanceOnCaixa(limite);
		        
		        JOptionPane.showMessageDialog(frmOfxFix, "Arquivo corrigido.");
			}
		});
		btnCorrigirSaldoDa.setBounds(360, 66, 206, 29);
		frmOfxFix.getContentPane().add(btnCorrigirSaldoDa);
		
		JLabel lblDiretrio = new JLabel("Diretório");
		lblDiretrio.setBounds(14, 114, 61, 16);
		frmOfxFix.getContentPane().add(lblDiretrio);
		
		textDir = new JTextField();
		textDir.setBounds(78, 109, 445, 26);
		frmOfxFix.getContentPane().add(textDir);
		textDir.setColumns(10);
		
		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileFilter filter = new FileNameExtensionFilter("Arquivo OFX","ofx");
			    
				ofxFileChooser.setFileFilter(filter);
				
				int result = ofxFileChooser.showOpenDialog(frmOfxFix);
				
				if(result == JFileChooser.APPROVE_OPTION)
				{
					textDir.setText(ofxFileChooser.getSelectedFile().getParent());
					ofxFileChooser.setCurrentDirectory(ofxFileChooser.getSelectedFile().getParentFile());
				}
			}
		});
		button.setBounds(521, 107, 45, 29);
		frmOfxFix.getContentPane().add(button);
		
		JButton btnCorrigirCartoDe_1 = new JButton("Corrigir Cartão de Crédito");
		btnCorrigirCartoDe_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				File ofxFilesDir = new File(textDir.getText());
				FilenameFilter filter = new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".ofx") && !name.startsWith("Fixed");
				    }
				};
				
				File [] ofxFilesList = ofxFilesDir.listFiles(filter);
				
				for(File ofxFile : ofxFilesList)
				{
					
					OFXEditor editor = new OFXEditor();
			        
			        editor.setOfxFile(ofxFile);
			        
			        editor.FixVisa();
					
				}
				
				JOptionPane.showMessageDialog(frmOfxFix, "Arquivos corrigidos.");
				
				
			}
		});
		btnCorrigirCartoDe_1.setBounds(6, 142, 204, 29);
		frmOfxFix.getContentPane().add(btnCorrigirCartoDe_1);
		
		JButton btnNewButton = new JButton("Corrigir saldo da Caixa");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				File ofxFilesDir = new File(textDir.getText());
				FilenameFilter filter = new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".ofx") && !name.startsWith("Fixed");
				    }
				};
				
				File [] ofxFilesList = ofxFilesDir.listFiles(filter);
				
				for(File ofxFile : ofxFilesList)
				{
					
					OFXEditor editor = new OFXEditor();
			        
			        editor.setOfxFile(ofxFile);
			        
			        double limite = Double.parseDouble(textLimite.getText());
			        
			        
			        editor.FixBalanceOnCaixa(limite);
					
				}
				
				JOptionPane.showMessageDialog(frmOfxFix, "Arquivos corrigidos.");
				
			}
		});
		btnNewButton.setBounds(360, 142, 200, 29);
		frmOfxFix.getContentPane().add(btnNewButton);
	}
}

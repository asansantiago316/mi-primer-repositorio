package escaner_de_red;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.*;
import java.util.*;
import java.io.*;


public class pantalla_principal extends JFrame implements ActionListener{
	JButton btnescaner, btnlimpiar;
	JTextField txtIP;
	
	public pantalla_principal() {
		
		JFrame frame = new JFrame("Escaner de red");
	     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     frame.setSize(400, 200);
	     frame.setLocationRelativeTo(null);
	  
		
		JPanel panel1 = new JPanel(new GridLayout (3, 4, 10, 10));
		panel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		panel1.add(new JLabel("IP"));
		txtIP = new JTextField();
		panel1.add(txtIP);
		
		btnescaner = new JButton("Ejecutar Escaneo");
		panel1.add(btnescaner);
		btnlimpiar = new JButton("Limpiar");
		panel1.add(btnlimpiar);
		
		btnescaner.addActionListener(this);
		btnlimpiar.addActionListener(this);
		
		frame.setContentPane(panel1);
	    frame.setVisible(true);
	    
	  
	    
	}
	
	  public void actionPerformed(ActionEvent e) {
		  
			if (e.getSource() == btnescaner) {
				String obtener_ip = txtIP.getText();
				Scanner sc = new Scanner (obtener_ip);
				int ip_a_int = Integer.parseInt(obtener_ip); 
				try {
					Process buscarIP = Runtime.getRuntime().exec("cmd /c ping " + ip_a_int); 
					BufferedReader lector = new BufferedReader(new InputStreamReader(buscarIP.getInputStream()));
				}
				catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "no existe la ip");
				}
				JOptionPane.showInputDialog(null, "elje el rango de numeros a escanear(max 255)");
			}
	  }
	  
	  	

	public static void main(String[] args) {
		pantalla_principal gui = new pantalla_principal();
	
	}


}


//tengo que hacer un codigo con el cual yo ingreso una ip y a esta le asigno un rengo es decir si tengo la ip 1.00.1.1 tengo que poner un rango que por ejemplo sea hasta 99 entonces va a ir viendo la ip hasta 1.00.1.99

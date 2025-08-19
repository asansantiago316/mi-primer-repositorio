package escaner_de_red;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Vista extends JFrame {
    private JTextField txtIP;
    private JTextField txtIP2;
    private JButton btnEscanear;
    private JButton btnClearInputs;
    private JButton btnClearTable;
    private JButton btnExport;
    private JProgressBar progressBar;
    private JTable table;
    private DefaultTableModel tableModel;

    public Vista() {
        super("Escáner de red");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Panel de inputs
        JPanel panelInputs = new JPanel(new GridLayout(3, 2, 10, 10));
        panelInputs.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        panelInputs.add(new JLabel("IP inicial:"));
        txtIP = new JTextField();
        panelInputs.add(txtIP);
        panelInputs.add(new JLabel("IP final:"));
        txtIP2 = new JTextField();
        panelInputs.add(txtIP2);

        btnEscanear    = new JButton("Ejecutar Escaneo");
        btnClearInputs = new JButton("Limpiar Inputs");
        panelInputs.add(btnEscanear);
        panelInputs.add(btnClearInputs);

        // Tabla de resultados
        tableModel = new DefaultTableModel(new Object[]{"IP", "Ping", "NSLookup"}, 0);
        table      = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new TitledBorder("Resultados"));

        // Botones de tabla
        JPanel panelTableBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnClearTable = new JButton("Limpiar Tabla");
        btnExport     = new JButton("Exportar a texto");
        panelTableBtns.add(btnClearTable);
        panelTableBtns.add(btnExport);

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        // Layout principal
        setLayout(new BorderLayout(10, 10));
        add(panelInputs, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(panelTableBtns, BorderLayout.NORTH);
        south.add(progressBar, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    // Getters para que el controlador acceda a los componentes
    public JTextField getTxtIP()      { return txtIP;      }
    public JTextField getTxtIP2()     { return txtIP2;     }
    public JButton    getBtnEscanear(){ return btnEscanear;}
    public JButton    getBtnClearInputs(){ return btnClearInputs;}
    public JButton    getBtnClearTable(){ return btnClearTable;}
    public JButton    getBtnExport()  { return btnExport;  }
    public JProgressBar getProgressBar(){ return progressBar;}
    public DefaultTableModel getTableModel(){ return tableModel;}
}
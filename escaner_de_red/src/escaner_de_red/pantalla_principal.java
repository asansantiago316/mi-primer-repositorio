package escaner_de_red;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class pantalla_principal implements ActionListener {
    private final Vista view; //la clase de la interfaz grafica

    public pantalla_principal(Vista view) {
        this.view = view;

        // Validación en tiempo real de IPs
        view.getTxtIP().getDocument().addDocumentListener(new IPFieldValidator(view.getTxtIP()));
        view.getTxtIP2().getDocument().addDocumentListener(new IPFieldValidator(view.getTxtIP2()));
        view.getBtnEscanear().setEnabled(false); //en caso de estar mal la ip se deshabilita

        // Registramos listeners de botones
        view.getBtnEscanear().addActionListener(this);
        view.getBtnClearInputs().addActionListener(this);
        view.getBtnClearTable().addActionListener(this);
        view.getBtnExport().addActionListener(this);

        view.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource(); //obtiene el componente el cual hizo que se llame la funcion

        if (src == view.getBtnClearInputs()) {
            view.getTxtIP().setText("");
            view.getTxtIP2().setText("");
            view.getTxtIP().setBorder(null);
            view.getTxtIP2().setBorder(null);
            return; // si src = a borrar inputs los borra
        }

        if (src == view.getBtnClearTable()) {
            view.getTableModel().setRowCount(0);
            view.getProgressBar().setValue(0);
            return; //borra la tabla
        }

        if (src == view.getBtnExport()) {
            exportTableToText(); //lama la funcion exportar
            return;
        }

        if (src == view.getBtnEscanear()) {
            doScan(); //lama el escaneo
        }
    }
    
    

    // Lanza el SwingWorker que hace ping + nslookup
    private void doScan() {
        String startIp = view.getTxtIP().getText().trim(); //consigue el texto sin espacios de la ip inicio
        String endIp   = view.getTxtIP2().getText().trim();//consigue el texto sin espacios de ip final

        if (!isValidIP(startIp) || !isValidIP(endIp)) {
            JOptionPane.showMessageDialog(view,
                "Introduce direcciones IP válidas.",
                "Error de validación",
                JOptionPane.ERROR_MESSAGE
            ); //tira error si no cumple con la validacion de ip
            return;
        }

        long start = ipToLong(startIp);
        long end   = ipToLong(endIp);
        if (start > end) {
            JOptionPane.showMessageDialog(view,"La IP inicial debe ser menor o igual que la IP final","Error de rango",JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Preparamos UI
        view.getBtnEscanear().setEnabled(false);
        view.getTxtIP().setEnabled(false);
        view.getTxtIP2().setEnabled(false);
        view.getTableModel().setRowCount(0); //dejata dodo en false para inhabilitarlo durante la ejecucion

        int total = (int)(end - start + 1); //le suma 1 a la barra de progreso
        JProgressBar bar = view.getProgressBar();
        bar.setMinimum(0);
        bar.setMaximum(total);
        bar.setValue(0);

        new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws Exception {
                int count = 0;
                for (long ipNum = start; ipNum <= end; ipNum++) {
                    String ipStr = longToIp(ipNum); //mientras el numero de la ip sea menor a la final sigue convirtiendo los numeros en ip

                    // 🔍 Validación extra de IP
                    if (!isValidIP(ipStr)) {
                        continue;  // salta IPs inválidas como 192.168.1.300
                    }

                    String pingRes;
                    try {
                        Process p = Runtime.getRuntime().exec("cmd /c ping -n 1 -w 1000 " + ipStr);
                        pingRes = (p.waitFor() == 0) ? "Activo" : "No responde";
                    } catch (Exception ex) {
                        pingRes = "Error ping";
                    } //ejecuta ping

                    StringBuilder nsb = new StringBuilder();
                    try {
                        Process n = Runtime.getRuntime().exec("cmd /c nslookup " + ipStr);
                        BufferedReader r = new BufferedReader(new InputStreamReader(n.getInputStream()));
                        String line;
                        while ((line = r.readLine()) != null) {
                            nsb.append(line).append(" | "); //ejecuta nslookup usa buffered readers para luego colocarlo en el texto
                        }
                    } catch (IOException ex) {
                        nsb.append("Error nslookup");
                    }

                    publish(new Object[]{ ipStr, pingRes, nsb.toString() });
                    count++;
                    bar.setValue(count);  // Actualizo la barra directamente
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                chunks.forEach(row -> view.getTableModel().addRow(row));
                // No hace falta actualizar la barra acá porque ya la actualizamos arriba
            }

            @Override
            protected void done() {
                view.getBtnEscanear().setEnabled(true);
                view.getTxtIP().setEnabled(true);
                view.getTxtIP2().setEnabled(true); //vuelve a poner todo en true porque termino el escaneo
                bar.setValue(0); //pone la barra de vuelta en 0%
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(view,
                        "Error en el escaneo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    // Exporta la tabla a un archivo .txt
    private void exportTableToText() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar resultados como texto");
        if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                // Escribe encabezados
                for (int c = 0; c < view.getTableModel().getColumnCount(); c++) {
                    fw.write(view.getTableModel().getColumnName(c));
                    fw.write(c < view.getTableModel().getColumnCount() - 1 ? "\t" : "\n");
                }
                // Escribe filas
                for (int r = 0; r < view.getTableModel().getRowCount(); r++) {
                    for (int c = 0; c < view.getTableModel().getColumnCount(); c++) {
                        Object val = view.getTableModel().getValueAt(r, c);
                        fw.write(val != null ? val.toString() : "");
                        fw.write(c < view.getTableModel().getColumnCount() - 1 ? "\t" : "\n");
                    }
                }
                JOptionPane.showMessageDialog(view,
                    "Exportación completada: " + file.getAbsolutePath(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view,
                    "Error al exportar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // ---------------- Utilidades ----------------

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String pattern =
            "^(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\." + //cubre valores de IPV4
            "(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\." +
            "(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\." +
            "(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)$"; //son 4 porque la ip esta dividida en cuatro digitos distintos
        return ip.matches(pattern);
    }

    private long ipToLong(String ip) {
        String[] octs = ip.split("\\."); //long canbia el num a 32bits y los separa en 8 cada uno
        long val = 0;
        for (String o : octs) {
            val = (val << 8) | Integer.parseInt(o); //los convierte en int
        }
        return val;
    }

    private String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF, //los enmascara como 255 maximo cada uno y va dezplazando
            (ip >> 16) & 0xFF,// hasta conseguir toda la ip de vuelta
            (ip >>  8) & 0xFF,
             ip        & 0xFF
        );
    }

    // DocumentListener interno para validar IP
    private class IPFieldValidator implements DocumentListener {
        private final JTextField field;
        private final Color validColor   = Color.GREEN.darker();
        private final Color invalidColor = Color.RED.darker();

        IPFieldValidator(JTextField field) {
            this.field = field;
        }

        @Override
        public void insertUpdate(DocumentEvent e) { validate(); }
        @Override
        public void removeUpdate(DocumentEvent e) { validate(); }
        @Override
        public void changedUpdate(DocumentEvent e) { validate(); }

        private void validate() {
            String text = field.getText().trim();
            boolean ok   = isValidIP(text);

            if (text.isEmpty()) {
                field.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
            } else {
                field.setBorder(BorderFactory.createLineBorder(ok ? validColor : invalidColor));
            }

            // Habilita botón solo si las dos IPs son válidas
            boolean bothValid = isValidIP(view.getTxtIP().getText().trim())
                             && isValidIP(view.getTxtIP2().getText().trim());
            view.getBtnEscanear().setEnabled(bothValid);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new pantalla_principal(new Vista())
        );
    }
}
//tengo que hacer un codigo con el cual yo ingreso una ip y a esta le asigno un rengo es decir si tengo la ip 1.00.1.1 tengo que poner un rango que por ejemplo sea hasta 99 entonces va a ir viendo la ip hasta 1.00.1.99

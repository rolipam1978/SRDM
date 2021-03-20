package srdm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Ramiro Oliveira Pamponet - rolipam Informática
 */
public class SRDM extends javax.swing.JFrame {
    
    // Conexão com o banco de dados
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private String url = "jdbc:derby:SRDM"; 
    //private String url = "jdbc:derby://localhost:1527/SRDM";
    private String usr = "srdm";
    private String pwd = "srdm";
    
    Statement stm;
    ResultSet rs;
    private Connection con;
    PreparedStatement pstm;
    
    private String enfermaria;
    private String novaEnfermaria;
    private Short motivo;
    private Short novoMotivo;
    
    private HashMap parametros; // Para passar o parâmetro para o relatório
    JasperPrint jp;
    JasperViewer jrv;
    
    Bean bean = new Bean();
    
    MaskFormatter mesAno, doisDigitos;
    private String tipoRegistro = "";
    
    String consulta;
    
    List<Bean> solicitacoes;
    
    DefaultTableModel tmSRDM = new DefaultTableModel(null, new String[]{"Mês / Ano", "Enfermaria", "Motivo"}) {

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    ListSelectionModel lsmSRDM;    
    
    /**
     * Creates new form SRDM
     */
    public SRDM() {
        initComponents();
        consultarDados(); // Faz a Consulta e Monta o List a partir dos Beans
        mostrarSolicitacoes(solicitacoes); // Monta a Tabela a partir do List
        mostrarQtdeTotalRegistros();
    }
    
    // Consulta o Banco de Dados e Monta o List<Bean> a partir dos beans
    private List<Bean> consultarDados(){
        String sql = "SELECT * FROM SRDM ORDER BY ID DESC";
        solicitacoes = new ArrayList<>();
        try {
            conecta();
            executeSQL(sql);
            //pstm = conecta().prepareStatement(sql);
            //rs = pstm.executeQuery(); 
            while(rs.next()){
                bean = new Bean();
                bean.setId(rs.getInt("id"));
                bean.setData(rs.getString("data"));
                bean.setEnfermaria(rs.getString("enfermaria"));
                bean.setMotivo(rs.getShort("motivo"));   
                solicitacoes.add(bean);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Não Existe Registro!");
            ex.printStackTrace();
        }finally{
            desconecta();
        }
        return solicitacoes;
    }
    
    private void mostrarQtdeTotalRegistros(){
        String query1 = "SELECT COUNT(*) as QTDE FROM SRDM";
        try{
            conecta();
            executeSQL(query1);
            //pstm = conecta().prepareStatement(query1);
            //rs = pstm.executeQuery();
            if(rs.first()){
                tfRegitros.setText(""+rs.getInt("QTDE"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            desconecta();
        }
    }
    
    public Connection conecta()
       {
            try
            {
                Class.forName(driver);
                con = DriverManager.getConnection(url, usr, pwd);
                //JOptionPane.showMessageDialog(null, "Conectado ao Banco de Dados!");

            }
            catch (ClassNotFoundException Driver)
            {
                JOptionPane.showMessageDialog(null, "Falha na Conexão com o Banco de Dados!");
                Driver.printStackTrace();
                            }
            catch (SQLException Fonte)
            {
                JOptionPane.showMessageDialog(null, "Erro de Conexão!");
                Fonte.printStackTrace();
                            }
            return con;
       }
       public void desconecta()
       {
            //boolean result = true;

            try
            {
                con.close();
                //JOptionPane.showMessageDialog(null, "Conexão Fechada!");
            }
            catch (SQLException e)
            {
                    JOptionPane.showMessageDialog(null, "Não Foi Possíve Fechar o Banco de Dados!");
                    e.printStackTrace();
                    //result = false;
             }
            
       }
       public void executeSQL (String sql){
           try{
               stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
               rs = stm.executeQuery(sql);

           }catch (Exception ex){
               JOptionPane.showMessageDialog(null, "Erro ao Executar o Comando!");
               JOptionPane.showMessageDialog(null, ex);
               ex.printStackTrace();
           }
       }
         
    private void geraIDNovo(){
        bean = new Bean();
        try {
            conecta();
            String sql = "select max(id) as id from SRDM";
            executeSQL(sql);
            if (rs.first()) {
                bean.setId(rs.getInt("id"));

            } else {
                bean.setId(0);

            }
            tfId.setText("" + (bean.getId() + 1));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao Gerar ID!");
        }finally{
            desconecta();
        }      
    }

    private void limparCampos(){
        
        tbDados.getSelectionModel().clearSelection();
        tfId.setText("");
        tfData.setText("");
        cbEnfermaria.setSelectedItem("Selecione ...");
        cbMotivo.setSelectedItem("Selecione ...");
    }
    
    private void validarCamposVazios(){
        if(tfId.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Esse campo deve conter algum valor. Crie um novo registro ou selecione um na Tabela.");
        }
        if(tfData.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Informe uma DATA no formato mês/ano (mmm/aa) ou selecione um registro na Tabela.");
        }
        if(cbEnfermaria.getSelectedItem().equals("Selecione ...")){
            JOptionPane.showMessageDialog(null, "Selecione uma enfermaria no ComboBox ou selecione um registro na Tabela.");
        }
        if(cbMotivo.getSelectedItem().equals("Selecione ..."))
            JOptionPane.showMessageDialog(null, "Selecione um Motivo no ComboBox ou selecione um registro na Tabela.");
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rbMesAno = new javax.swing.JRadioButton();
        rbAno = new javax.swing.JRadioButton();
        rbEnfermaria = new javax.swing.JRadioButton();
        try{
            mesAno = new MaskFormatter("##/##");
        }catch(Exception e){
            e.printStackTrace();
        }
        tfMesAno = new JFormattedTextField(mesAno);
        jLabel2 = new javax.swing.JLabel();
        try{
            doisDigitos = new MaskFormatter("##");
        }catch(Exception e){
            e.printStackTrace();
        }
        tfAno = new JFormattedTextField(doisDigitos);
        jLabel3 = new javax.swing.JLabel();
        try{
            doisDigitos = new MaskFormatter("**");
        }catch(Exception e){
            e.printStackTrace();
        }
        tfEnfermaria = new JFormattedTextField(doisDigitos);
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbDados = new javax.swing.JTable();
        btFiltrar = new javax.swing.JButton();
        btClear = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        tfRegitros = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        try{       mesAno = new MaskFormatter("##/##");   }catch(Exception e){       e.printStackTrace();   }
        tfData = new JFormattedTextField(mesAno);
        cbEnfermaria = new javax.swing.JComboBox<>();
        cbMotivo = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        tfId = new javax.swing.JTextField();
        btNovo = new javax.swing.JButton();
        btSalvar = new javax.swing.JButton();
        btExcluir = new javax.swing.JButton();
        btLimpar = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        rbCiMes = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        rbCiEnfermariaMes = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        rbCiMotivoMes = new javax.swing.JRadioButton();
        rbCiEnfermariaAno = new javax.swing.JRadioButton();
        rbCiMotivoAno = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        rbMotivoEnfermariaMes = new javax.swing.JRadioButton();
        rbMotivoEnfermariaAno = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        btGerar = new javax.swing.JButton();
        tfParametro = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SRDM - CI's HUPES by rolipam Informática");
        setLocation(new java.awt.Point(350, 100));
        setMinimumSize(new java.awt.Dimension(800, 600));
        setPreferredSize(new java.awt.Dimension(850, 710));
        setResizable(false);

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Filtrar na Tabela por:");

        buttonGroup1.add(rbMesAno);
        rbMesAno.setText("Mês / Ano");
        rbMesAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbMesAnoActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbAno);
        rbAno.setText("Ano");
        rbAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbAnoActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbEnfermaria);
        rbEnfermaria.setText("Enfermaria");
        rbEnfermaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbEnfermariaActionPerformed(evt);
            }
        });

        tfMesAno.setEditable(false);
        tfMesAno.setFont(new java.awt.Font("Courier New", 1, 11)); // NOI18N
        tfMesAno.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel2.setText("Ex.: 10/21");

        tfAno.setEditable(false);
        tfAno.setFont(new java.awt.Font("Courier New", 1, 11)); // NOI18N
        tfAno.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel3.setText("Ex.: 21 (2021)");

        tfEnfermaria.setEditable(false);
        tfEnfermaria.setFont(new java.awt.Font("Courier New", 1, 11)); // NOI18N
        tfEnfermaria.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfEnfermaria.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfEnfermariaFocusLost(evt);
            }
        });

        jLabel4.setText("Ex.: 1A");

        tbDados.setModel(tmSRDM);
        tbDados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  
        lsmSRDM = tbDados.getSelectionModel();  
        lsmSRDM.addListSelectionListener(new ListSelectionListener() {          
            public void valueChanged(ListSelectionEvent e){          
                if(!e.getValueIsAdjusting()){          
                    tbDadosLinhaSelecionada(tbDados);  
                }  
            }  
        });
        jScrollPane1.setViewportView(tbDados);

        btFiltrar.setFont(new java.awt.Font("Tahoma", 3, 11)); // NOI18N
        btFiltrar.setText("Filtrar");
        btFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFiltrarActionPerformed(evt);
            }
        });

        btClear.setFont(new java.awt.Font("Tahoma", 3, 11)); // NOI18N
        btClear.setText("Limpar");
        btClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btClearActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Total de Registros:");

        tfRegitros.setEditable(false);
        tfRegitros.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        tfRegitros.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfRegitros.setEnabled(false);
        tfRegitros.setFocusable(false);
        tfRegitros.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btFiltrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btClear))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(rbMesAno)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfMesAno, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(rbAno)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfAno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(rbEnfermaria)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfEnfermaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfRegitros)))
                .addContainerGap())
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {rbAno, rbEnfermaria, rbMesAno});

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tfAno, tfEnfermaria, tfMesAno});

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btClear, btFiltrar});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbMesAno)
                    .addComponent(tfMesAno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbAno)
                    .addComponent(tfAno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbEnfermaria)
                    .addComponent(tfEnfermaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btFiltrar)
                    .addComponent(btClear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(tfRegitros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cadastro", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tempus Sans ITC", 1, 12)); // NOI18N
        jLabel5.setText("Data:");

        jLabel6.setFont(new java.awt.Font("Tempus Sans ITC", 1, 12)); // NOI18N
        jLabel6.setText("Enfermaria:");

        jLabel7.setFont(new java.awt.Font("Tempus Sans ITC", 1, 12)); // NOI18N
        jLabel7.setText("Motivo:");

        tfData.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        tfData.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        cbEnfermaria.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        cbEnfermaria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione ...", "TA - UDAP", "TB - UTI.2", "1A - ONCOHEMATO", "1B - ONCOHEMATO / TMO", "1C - UPL", "1CS - UTI.PED", "1D - CARDIO", "2A - CLINICA MÉDICA", "2B - INFECTO", "2C - OFTALMO", "2D - CLINICA MÉDICA", "3B - PSIQUIATRIA", "3C - NEURO", "3D - VAZIA", "4A - CLI. CIR. M", "4B - GASTRO-HEPATO", "4C - UTI.1", "4D - CLI. CIR. F", "UM - METABÓLICA", "XY - OUTRAS" }));
        cbEnfermaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEnfermariaActionPerformed(evt);
            }
        });

        cbMotivo.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        cbMotivo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione ...", "1 - Ampola quebrou durante preparo", "2 - Caiu no chão", "3 - Desprezado", "4 - Erro no preparo (Reconstituição / Diluição)", "5 - Pcte rejeitou, cuspiu, vomitou", "6 - Não encontrado no box", "7 - Sem justificativa", "8 - Outros" }));
        cbMotivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMotivoActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tempus Sans ITC", 1, 12)); // NOI18N
        jLabel8.setText("ID:");

        tfId.setEditable(false);
        tfId.setBackground(new java.awt.Color(255, 255, 255));
        tfId.setFont(new java.awt.Font("Courier New", 1, 12)); // NOI18N
        tfId.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfId.setEnabled(false);
        tfId.setFocusable(false);
        tfId.setRequestFocusEnabled(false);

        btNovo.setFont(new java.awt.Font("Harrington", 1, 12)); // NOI18N
        btNovo.setText("Novo");
        btNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNovoActionPerformed(evt);
            }
        });

        btSalvar.setFont(new java.awt.Font("Harrington", 1, 12)); // NOI18N
        btSalvar.setText("Salvar");
        btSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSalvarActionPerformed(evt);
            }
        });

        btExcluir.setFont(new java.awt.Font("Harrington", 1, 12)); // NOI18N
        btExcluir.setText("Excluir");
        btExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExcluirActionPerformed(evt);
            }
        });

        btLimpar.setFont(new java.awt.Font("Harrington", 1, 12)); // NOI18N
        btLimpar.setText("Limpar");
        btLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLimparActionPerformed(evt);
            }
        });

        jLabel15.setText("Mês / Ano: mm/aa");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btNovo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSalvar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btExcluir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btLimpar))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(tfData, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel15))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cbEnfermaria, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(cbMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel5, jLabel6, jLabel7, jLabel8});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tfData, tfId});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btExcluir, btLimpar, btNovo, btSalvar});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(tfId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tfData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbEnfermaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbMotivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btNovo)
                    .addComponent(btSalvar)
                    .addComponent(btExcluir)
                    .addComponent(btLimpar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Opções de Relatórios (Gráficos)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        buttonGroup2.add(rbCiMes);
        rbCiMes.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbCiMes.setText("Total de CI's por mês (Período: 1 ano)");
        rbCiMes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCiMesActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel9.setText("Gráfico em Coluna com os meses do ano no eixo X");

        buttonGroup2.add(rbCiEnfermariaMes);
        rbCiEnfermariaMes.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbCiEnfermariaMes.setText("Total de CI's por enfermaria (Mensal)");
        rbCiEnfermariaMes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCiEnfermariaMesActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel10.setText("Gráfico em Colunas com as enfermarias no eixo X");

        buttonGroup2.add(rbCiMotivoMes);
        rbCiMotivoMes.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbCiMotivoMes.setText("Total de CI's por motivo (Mensal)");
        rbCiMotivoMes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCiMotivoMesActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbCiEnfermariaAno);
        rbCiEnfermariaAno.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbCiEnfermariaAno.setText("Total de CI's por enfermaria (Anual)");
        rbCiEnfermariaAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCiEnfermariaAnoActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbCiMotivoAno);
        rbCiMotivoAno.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbCiMotivoAno.setText("Total de CI's por motivo (Anual)");
        rbCiMotivoAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCiMotivoAnoActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel11.setText("Gráfico em Pizza 100%");

        buttonGroup2.add(rbMotivoEnfermariaMes);
        rbMotivoEnfermariaMes.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbMotivoEnfermariaMes.setText("Motivos por enfermaria (Mensal)");
        rbMotivoEnfermariaMes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbMotivoEnfermariaMesActionPerformed(evt);
            }
        });

        buttonGroup2.add(rbMotivoEnfermariaAno);
        rbMotivoEnfermariaAno.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        rbMotivoEnfermariaAno.setText("Motivos por enfermaria (Anual)");
        rbMotivoEnfermariaAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbMotivoEnfermariaAnoActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel12.setText("Gráfico em Columas 100% empilhadas");

        btGerar.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        btGerar.setText("Gerar Relatório em PDF");
        btGerar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGerarActionPerformed(evt);
            }
        });

        tfParametro.setFont(new java.awt.Font("Courier New", 1, 11)); // NOI18N
        tfParametro.setToolTipText("Informe o ano com dois digitos ou mês/ano");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(rbCiMes)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbMotivoEnfermariaMes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rbCiMotivoMes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rbCiEnfermariaMes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbCiEnfermariaAno, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbCiMotivoAno, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rbMotivoEnfermariaAno, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(108, 108, 108))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(97, 97, 97))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(97, 97, 97))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(tfParametro)
                        .addGap(18, 18, 18)
                        .addComponent(btGerar)
                        .addGap(109, 109, 109))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rbCiMes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCiEnfermariaMes)
                    .addComponent(rbCiEnfermariaAno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCiMotivoMes)
                    .addComponent(rbCiMotivoAno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbMotivoEnfermariaMes)
                    .addComponent(rbMotivoEnfermariaAno))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btGerar)
                    .addComponent(tfParametro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sobre", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel13.setFont(new java.awt.Font("Lucida Calligraphy", 0, 11)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Desenvolvido por Ramiro Oliveira Pamponet");

        jLabel14.setFont(new java.awt.Font("Lucida Calligraphy", 0, 11)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Todos os Direitos Reservados - rolipam Informática - Copyright 2021");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("Arquivo");

        jMenuItem1.setText("Sair");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbEnfermariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEnfermariaActionPerformed
        
        if(cbEnfermaria.getSelectedItem().equals("TA - UDAP")){
            bean.setEnfermaria("TA");
            enfermaria = "TA";
            novaEnfermaria = "TA";
        }else if(cbEnfermaria.getSelectedItem().equals("TB - UTI.2")){
            bean.setEnfermaria("TB");
            enfermaria = "TB";
            novaEnfermaria = "TB";
        }else if(cbEnfermaria.getSelectedItem().equals("1A - ONCOHEMATO")){
            bean.setEnfermaria("1A");
            enfermaria = "1A";
            novaEnfermaria = "1A";
        }else if(cbEnfermaria.getSelectedItem().equals("1B - ONCOHEMATO / TMO")){
            bean.setEnfermaria("1B");
            enfermaria = "1B";
            novaEnfermaria = "1B";
        }else if(cbEnfermaria.getSelectedItem().equals("1C - UPL")){
            bean.setEnfermaria("1C");
            enfermaria = "1C";
            novaEnfermaria = "1C";
        }else if(cbEnfermaria.getSelectedItem().equals("1CS - UTI.PED")){
            bean.setEnfermaria("CS");
            enfermaria = "CS";
            novaEnfermaria = "CS";
        }else if(cbEnfermaria.getSelectedItem().equals("1D - CARDIO")){
            bean.setEnfermaria("1D");
            enfermaria = "1D";
            novaEnfermaria = "1D";
        }else if(cbEnfermaria.getSelectedItem().equals("2A - CLINICA MÉDICA")){
            bean.setEnfermaria("2A");
            enfermaria = "2A";
            novaEnfermaria = "2A";
        }else if(cbEnfermaria.getSelectedItem().equals("2B - INFECTO")){
            bean.setEnfermaria("2B");
            enfermaria = "2B";
            novaEnfermaria = "2B";
        }else if(cbEnfermaria.getSelectedItem().equals("2C - OFTALMO")){
            bean.setEnfermaria("2C");
            enfermaria = "2C";
            novaEnfermaria = "2C";
        }else if(cbEnfermaria.getSelectedItem().equals("2D - CLINICA MÉDICA")){
            bean.setEnfermaria("2D");
            enfermaria = "2D";
            novaEnfermaria = "2D";
        }else if(cbEnfermaria.getSelectedItem().equals("3B - PSIQUIATRIA")){
            bean.setEnfermaria("3B");
            enfermaria = "3B";
            novaEnfermaria = "3B";
        }else if(cbEnfermaria.getSelectedItem().equals("3C - NEURO")){
            bean.setEnfermaria("3C");
            enfermaria = "3C";
            novaEnfermaria = "3C";
        }else if(cbEnfermaria.getSelectedItem().equals("3D - VAZIA")){
            bean.setEnfermaria("3D");
            enfermaria = "3D";
            novaEnfermaria = "3D";
        }else if(cbEnfermaria.getSelectedItem().equals("4A - CLI. CIR. M")){
            bean.setEnfermaria("4A");
            enfermaria = "4A";
            novaEnfermaria = "4A";
        }else if(cbEnfermaria.getSelectedItem().equals("4B - GASTRO-HEPATO")){
            bean.setEnfermaria("4B");
            enfermaria = "4B";
            novaEnfermaria = "4B";
        }else if(cbEnfermaria.getSelectedItem().equals("4C - UTI.1")){
            bean.setEnfermaria("4C");
            enfermaria = "4C";
            novaEnfermaria = "4C";
        }else if(cbEnfermaria.getSelectedItem().equals("4D - CLI. CIR. F")){
            bean.setEnfermaria("4D");
            enfermaria = "4D";
            novaEnfermaria = "4D";
        }else if(cbEnfermaria.getSelectedItem().equals("UM - METABÓLICA")){
            bean.setEnfermaria("UM");
            enfermaria = "UM";
            novaEnfermaria = "UM";
        }else if(cbEnfermaria.getSelectedItem().equals("XY - OUTRAS")){
            bean.setEnfermaria("XY");
            enfermaria = "XY";
            novaEnfermaria = "XY";
        }else{
            bean.setEnfermaria(""); // cbEnfermaria.getSelectedItem().equals("Selecione ...");
            enfermaria = "";
            novaEnfermaria = "";
        }
    }//GEN-LAST:event_cbEnfermariaActionPerformed

    private void cbMotivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMotivoActionPerformed
        
        if(cbMotivo.getSelectedItem().equals("1 - Ampola quebrou durante preparo")){
            bean.setMotivo(Short.valueOf("1"));
            motivo = Short.valueOf("1");
            novoMotivo = Short.valueOf("1");
        }else if(cbMotivo.getSelectedItem().equals("2 - Caiu no chão")){
            bean.setMotivo(Short.valueOf("2"));
            motivo = Short.valueOf("2");
            novoMotivo = Short.valueOf("2");
        }else if(cbMotivo.getSelectedItem().equals("3 - Desprezado")){
            bean.setMotivo(Short.valueOf("3"));
            motivo = Short.valueOf("3");
            novoMotivo = Short.valueOf("3");
        }else if(cbMotivo.getSelectedItem().equals("4 - Erro no preparo (Reconstituição / Diluição)")){
            bean.setMotivo(Short.valueOf("4"));
            motivo = Short.valueOf("4");
            novoMotivo = Short.valueOf("4");
        }else if(cbMotivo.getSelectedItem().equals("5 - Pcte rejeitou, cuspiu, vomitou")){
            bean.setMotivo(Short.valueOf("5"));
            motivo = Short.valueOf("5");
            novoMotivo = Short.valueOf("5");
        }else if(cbMotivo.getSelectedItem().equals("6 - Não encontrado no box")){
            bean.setMotivo(Short.valueOf("6"));
            motivo = Short.valueOf("6");
            novoMotivo = Short.valueOf("6");
        }else if(cbMotivo.getSelectedItem().equals("7 - Sem justificativa")){
            bean.setMotivo(Short.valueOf("7"));
            motivo = Short.valueOf("7");
            novoMotivo = Short.valueOf("7");
        }else if(cbMotivo.getSelectedItem().equals("8 - Outros")){
            bean.setMotivo(Short.valueOf("8"));
            motivo = Short.valueOf("8");
            novoMotivo = Short.valueOf("8");
        }else{
            bean.setMotivo(Short.valueOf("0")); // cbMotivo.getSelectedItem().equals("Selecione ...")
            motivo = Short.valueOf("0");
            novoMotivo = Short.valueOf("0");
        }
    }//GEN-LAST:event_cbMotivoActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void rbMesAnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbMesAnoActionPerformed
        tfMesAno.setEditable(true);
        tfMesAno.requestFocus();
        tfMesAno.setText("");
        consulta = "SELECT * FROM SRDM WHERE data LIKE ? ORDER BY id DESC ";
        tfAno.setText("");
        tfAno.setEditable(false);
        tfEnfermaria.setText("");
        tfEnfermaria.setEditable(false);
        
    }//GEN-LAST:event_rbMesAnoActionPerformed

    private void rbAnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAnoActionPerformed
        tfAno.setEditable(true);
        tfAno.requestFocus();
        tfAno.setText("");
        consulta = "SELECT * FROM SRDM WHERE data LIKE ? ORDER BY id DESC ";
        tfMesAno.setText("");
        tfMesAno.setEditable(false);
        tfEnfermaria.setText("");
        tfEnfermaria.setEditable(false);
        
    }//GEN-LAST:event_rbAnoActionPerformed

    private void rbEnfermariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbEnfermariaActionPerformed
        tfEnfermaria.setEditable(true);
        tfEnfermaria.requestFocus();
        tfEnfermaria.setText("");
        consulta = "SELECT * FROM SRDM WHERE enfermaria LIKE ? ORDER BY id DESC ";
        tfMesAno.setText("");
        tfMesAno.setEditable(false);
        tfAno.setText("");
        tfAno.setEditable(false);
        
    }//GEN-LAST:event_rbEnfermariaActionPerformed
      
    private void btFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFiltrarActionPerformed
        while (tmSRDM.getRowCount() > 0) {
            tmSRDM.removeRow(0);
        }
        if(rbMesAno.isSelected()){
            listarSolicitacoes(tfMesAno.getText());
            mostrarQdeRegitrosFiltrada(tfMesAno.getText());
        } else if(rbAno.isSelected()){
            listarSolicitacoes("%"+tfAno.getText()+"%");
            mostrarQdeRegitrosFiltrada("%"+tfAno.getText()+"%");
        }else if(rbEnfermaria.isSelected()){
            listarSolicitacoes(tfEnfermaria.getText());
            mostrarQdeRegitrosFiltrada(tfEnfermaria.getText());
        }
        mostrarSolicitacoes(solicitacoes);
        
    }//GEN-LAST:event_btFiltrarActionPerformed

    //Consulta por mês/ano ou ano ou enfermaria
    private List<Bean> listarSolicitacoes (String nome){
        solicitacoes = new ArrayList<>();
        try {
            pstm = conecta().prepareStatement(consulta,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            pstm.setString(1, nome);
            rs = pstm.executeQuery(); 
            while(rs.next()){
                bean = new Bean();
                bean.setId(rs.getInt("id"));
                bean.setData(rs.getString("data"));
                bean.setEnfermaria(rs.getString("enfermaria"));
                bean.setMotivo(rs.getShort("motivo"));   
                solicitacoes.add(bean);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Não Existe Registro!");
            ex.printStackTrace();
        }finally{
            desconecta();
        }
        return solicitacoes;
    }
    
    private void mostrarQdeRegitrosFiltrada(String nome){
        String query = "";
        
        if(rbAno.isSelected()){
            query = "SELECT COUNT(*) as QTDE FROM SRDM WHERE data LIKE ?";
            
        }else if(rbMesAno.isSelected()){
            query = "SELECT COUNT(*) as QTDE FROM SRDM WHERE data LIKE ?";
            
        }else if(rbEnfermaria.isSelected()){
            query = "SELECT COUNT(*) as QTDE FROM SRDM WHERE enfermaria LIKE ?";
            
        }
        try {
            pstm = conecta().prepareStatement(query);
            pstm.setString(1, nome);
            rs = pstm.executeQuery(); 
            if(rs.next()){
                tfRegitros.setText(""+rs.getInt("QTDE"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Não Existe Registro!");
            ex.printStackTrace();
        }finally{
            desconecta();
        }
    }    
    
    private void mostrarSolicitacoes(List<Bean> solicitacoes) {
        tbDados.getColumnModel().getColumn(0).setPreferredWidth(5); // mês/ano
        tbDados.getColumnModel().getColumn(1).setPreferredWidth(5); // enfermaria
        tbDados.getColumnModel().getColumn(2).setPreferredWidth(5); // motivo
        
        while (tmSRDM.getRowCount() > 0) {
            tmSRDM.removeRow(0);
        }

        for (int i = 0; i < solicitacoes.size(); i++) {
            String[] campos = new String[]{null, null, null};
            tmSRDM.addRow(campos);
            tmSRDM.setValueAt(solicitacoes.get(i).getData(), i, 0);
            tmSRDM.setValueAt(solicitacoes.get(i).getEnfermaria(), i, 1);
            tmSRDM.setValueAt(solicitacoes.get(i).getMotivo(), i, 2);
        }
    }    
    
       private void tbDadosLinhaSelecionada(JTable tb){

            if (tb.getSelectedRow() != -1) {
                
                tfId.setText("" + solicitacoes.get(tb.getSelectedRow()).getId());
                tfData.setText(solicitacoes.get(tb.getSelectedRow()).getData());
                
                //atualizaComboBoxEnfermaria();
                if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("TA")){
                   cbEnfermaria.setSelectedItem("TA - UDAP");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("TB")){
                   cbEnfermaria.setSelectedItem("TB - UTI.2");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("1A")){
                   cbEnfermaria.setSelectedItem("1A - ONCOHEMATO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("1B")){
                   cbEnfermaria.setSelectedItem("1B - ONCOHEMATO / TMO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("1C")){
                   cbEnfermaria.setSelectedItem("1C - UPL");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("CS")){
                   cbEnfermaria.setSelectedItem("1CS - UTI.PED");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("1D")){
                   cbEnfermaria.setSelectedItem("1D - CARDIO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("2A")){
                   cbEnfermaria.setSelectedItem("2A - CLINICA MÉDICA");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("2B")){
                   cbEnfermaria.setSelectedItem("2B - INFECTO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("2C")){
                   cbEnfermaria.setSelectedItem("2C - OFTALMO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("2D")){
                   cbEnfermaria.setSelectedItem("2D - CLINICA MÉDICA");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("3B")){
                   cbEnfermaria.setSelectedItem("3B - PSIQUIATRIA");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("3C")){
                   cbEnfermaria.setSelectedItem("3B - NEURO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("3D")){
                   cbEnfermaria.setSelectedItem("3B - VAZIA");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("4A")){
                   cbEnfermaria.setSelectedItem("4A - CLI. CIR. M");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("4B")){
                   cbEnfermaria.setSelectedItem("4B - GASTRO-HEPATO");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("4C")){
                   cbEnfermaria.setSelectedItem("4C - UTI.1");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("4D")){
                   cbEnfermaria.setSelectedItem("4D - CLI. CIR. F");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("UM")){
                   cbEnfermaria.setSelectedItem("UM - METABÓLLICA");
                }else if(solicitacoes.get(tb.getSelectedRow()).getEnfermaria().equals("XY")){
                   cbEnfermaria.setSelectedItem("XY - OUTRAS");
                }else{
                   cbEnfermaria.setSelectedItem("Selecione ...");
                }
                
               //atualizaComboBoxMotivo();
                if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 1){
                    cbMotivo.setSelectedItem("1 - Ampola quebrou durante preparo");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 2){
                    cbMotivo.setSelectedItem("2 - Caiu no chão");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 3){
                    cbMotivo.setSelectedItem("3 - Desprezado");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 4){
                    cbMotivo.setSelectedItem("4 - Erro no preparo (Reconstituição / Diluição)");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 5){
                    cbMotivo.setSelectedItem("5 - Pcte rejeitou, cuspiu, vomitou");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 6){
                    cbMotivo.setSelectedItem("6 - Não encontrado no box");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 7){
                    cbMotivo.setSelectedItem("7 - Sem justificativa");
                }else if(solicitacoes.get(tb.getSelectedRow()).getMotivo() == 8){
                    cbMotivo.setSelectedItem("8 - Outros");
                }else{
                    cbMotivo.setSelectedItem("Selecione ...");
                }
            }       
        }    
    
    private void btNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNovoActionPerformed
        tipoRegistro = "novo";
        limparCampos();
        geraIDNovo();
        tfData.requestFocus();
    }//GEN-LAST:event_btNovoActionPerformed

    private void btSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSalvarActionPerformed
        String grava = "INSERT INTO SRDM (id, data, enfermaria, motivo) values (?, ?, ?, ?)";
        String altera = "UPDATE SRDM set data = ?, enfermaria = ?, motivo = ? WHERE id = ?";
        
        validarCamposVazios();
        if(tipoRegistro.equals("novo")){
            try {
                // Gravar
                pstm = conecta().prepareStatement(grava,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                pstm.setInt(1, Integer.parseInt(tfId.getText()));
                pstm.setString(2, tfData.getText());
                pstm.setString(3, bean.getEnfermaria());
                pstm.setShort(4, bean.getMotivo());
                pstm.executeUpdate();
                JOptionPane.showMessageDialog(null, "Registro Gravado com Sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Não Foi Possível Gravar o Registro!");
                ex.printStackTrace();
            }finally{
                desconecta();
            }
        }else{
            try {
                // Alterar
                pstm = conecta().prepareStatement(altera,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                pstm.setString(1, tfData.getText());
                pstm.setString(2, bean.getEnfermaria());
                pstm.setShort(3, bean.getMotivo());
                pstm.setInt(4, Integer.parseInt(tfId.getText()));
                pstm.executeUpdate();
                JOptionPane.showMessageDialog(null, "Registro Alterado com Sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Não Foi Possível Alterar o Registro!");
                ex.printStackTrace();
            }finally{
                desconecta();
            }
        }
        consultarDados();
        mostrarSolicitacoes(solicitacoes);
        mostrarQtdeTotalRegistros();
        limparCampos();
        
    }//GEN-LAST:event_btSalvarActionPerformed

    private void btExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExcluirActionPerformed
        String excluir = "DELETE FROM SRDM WHERE id = ?";
        int opcao = JOptionPane.showConfirmDialog(null, "Deseja Deletar o resgistro selecionado", "Deletar Registro", JOptionPane.YES_NO_OPTION);
        // Excluir um registro selecionado na Tabela
        if(tfId.getText().equals("")){
            JOptionPane.showMessageDialog(null, "Selecione um regsitro na Tabela para excluir.");
        }
        try{
            pstm = conecta().prepareStatement(excluir,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            pstm.setInt(1, Integer.parseInt(tfId.getText()));
            if (opcao == JOptionPane.YES_OPTION) {
                //pstm.executeUpdate();
                int excluiu = pstm.executeUpdate();
                if (excluiu == 1) {
                    JOptionPane.showMessageDialog(null, "Registro Excluído com Sucesso!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Registro Não Foi Excluído!");
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não Foi Possível Excluir o Registro!");
        }finally{
            desconecta();
        }        
        consultarDados();
        mostrarSolicitacoes(solicitacoes);
        mostrarQtdeTotalRegistros();
        limparCampos();
        
    }//GEN-LAST:event_btExcluirActionPerformed

    private void btLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLimparActionPerformed
        limparCampos();
        consultarDados();
        mostrarSolicitacoes(solicitacoes);
        mostrarQtdeTotalRegistros();
    }//GEN-LAST:event_btLimparActionPerformed

    private void btGerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGerarActionPerformed
        if(rbCiMes.isSelected()){
            imprimirCiMes();
        }else if(rbCiEnfermariaMes.isSelected()){
            imprimirCiEnfermariaMes();
        }else if(rbCiEnfermariaAno.isSelected()){
            imprimirCiEnfermariaAno();
        }else if(rbCiMotivoMes.isSelected()){
            imprimirCiMotivoMes();
        }else if(rbCiMotivoAno.isSelected()){
            imprimirCiMotivoAno();
        }else if(rbMotivoEnfermariaMes.isSelected()){
            imprimirMotivoEnfermariaMes();
        }else if(rbMotivoEnfermariaAno.isSelected()){
            imprimirMotivoEnfermariaAno();
        }
    }//GEN-LAST:event_btGerarActionPerformed

    private void imprimirCiMes(){
        try {
            
            parametros = new HashMap();
            parametros.put("ano", "%"+tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/TotalCIsMes.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        } 
    }
    
    private void imprimirCiEnfermariaMes(){
        
        try {
            
            parametros = new HashMap();
            parametros.put("mesAno", tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/TotalCIsEnfermariaMes.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }        
    }
    
    private void imprimirCiEnfermariaAno(){
        
        try {
            
            parametros = new HashMap();
            parametros.put("ano", "%"+tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/TotalCIsEnfermariaAno.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }        
    }
    
    private void imprimirCiMotivoMes(){
        
        try {
            
            parametros = new HashMap();
            parametros.put("mesAno", tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/CIsPorMotivoMes.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }        
    }
    
    private void imprimirCiMotivoAno(){

        try {
            
            parametros = new HashMap();
            parametros.put("ano", "%"+tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/CIsPorMotivoAno.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }        
    }
    
    private void imprimirMotivoEnfermariaMes(){

        try {
            
            parametros = new HashMap();
            parametros.put("mesAno", tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/MotivoEnfermariaMes.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }         
    }
    
    private void imprimirMotivoEnfermariaAno(){

        try {
            
            parametros = new HashMap();
            parametros.put("ano", "%"+tfParametro.getText());
            jp = JasperFillManager.fillReport("./Relatorios/MotivoEnfermariaAno.jasper", parametros, conecta());
            jrv = new JasperViewer(jp, false);
            jrv.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jrv.setVisible(true);
            jrv.setExtendedState(MAXIMIZED_BOTH);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao Tentar Imprimir Relatório.");
            e.printStackTrace();
        }         
    }
    
    private void btClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btClearActionPerformed
        tfMesAno.setText("");
        tfAno.setText("");
        tfEnfermaria.setText("");
        consultarDados();
        mostrarSolicitacoes(solicitacoes);
        mostrarQtdeTotalRegistros();
        limparCampos();
    }//GEN-LAST:event_btClearActionPerformed

    private void tfEnfermariaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfEnfermariaFocusLost
        String codEnfermaria = tfEnfermaria.getText();
        tfEnfermaria.setText(codEnfermaria.toUpperCase());
        if(tfEnfermaria.getText().equals("1CS")){
            tfEnfermaria.setText("CS");
        }
    }//GEN-LAST:event_tfEnfermariaFocusLost

    private void rbCiMesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCiMesActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbCiMesActionPerformed

    private void rbCiEnfermariaMesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCiEnfermariaMesActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbCiEnfermariaMesActionPerformed

    private void rbCiEnfermariaAnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCiEnfermariaAnoActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbCiEnfermariaAnoActionPerformed

    private void rbCiMotivoMesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCiMotivoMesActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbCiMotivoMesActionPerformed

    private void rbCiMotivoAnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCiMotivoAnoActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbCiMotivoAnoActionPerformed

    private void rbMotivoEnfermariaMesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbMotivoEnfermariaMesActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbMotivoEnfermariaMesActionPerformed

    private void rbMotivoEnfermariaAnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbMotivoEnfermariaAnoActionPerformed
        tfParametro.setText("");
        tfParametro.requestFocus();
    }//GEN-LAST:event_rbMotivoEnfermariaAnoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SRDM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SRDM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SRDM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SRDM.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SRDM().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btClear;
    private javax.swing.JButton btExcluir;
    private javax.swing.JButton btFiltrar;
    private javax.swing.JButton btGerar;
    private javax.swing.JButton btLimpar;
    private javax.swing.JButton btNovo;
    private javax.swing.JButton btSalvar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JComboBox<String> cbEnfermaria;
    private javax.swing.JComboBox<String> cbMotivo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton rbAno;
    private javax.swing.JRadioButton rbCiEnfermariaAno;
    private javax.swing.JRadioButton rbCiEnfermariaMes;
    private javax.swing.JRadioButton rbCiMes;
    private javax.swing.JRadioButton rbCiMotivoAno;
    private javax.swing.JRadioButton rbCiMotivoMes;
    private javax.swing.JRadioButton rbEnfermaria;
    private javax.swing.JRadioButton rbMesAno;
    private javax.swing.JRadioButton rbMotivoEnfermariaAno;
    private javax.swing.JRadioButton rbMotivoEnfermariaMes;
    private javax.swing.JTable tbDados;
    private javax.swing.JTextField tfAno;
    private javax.swing.JTextField tfData;
    private javax.swing.JTextField tfEnfermaria;
    private javax.swing.JTextField tfId;
    private javax.swing.JTextField tfMesAno;
    private javax.swing.JTextField tfParametro;
    private javax.swing.JTextField tfRegitros;
    // End of variables declaration//GEN-END:variables
}

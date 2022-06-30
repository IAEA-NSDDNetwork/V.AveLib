/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visualaveraginglibrary;

import averagingAlgorithms.averagingMethods;
import averagingAlgorithms.outlierMethods;
import ensdf_datapoint.dataPt;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

public class outliersWindow extends javax.swing.JDialog {

    private JTextField textField1;

    /**
     * Creates new form outliersWindow
     */
    public outliersWindow(java.awt.Frame parent, boolean modal, dataPt[] data,
            String txt, VAveLib_GUI mainWin) {
        super(parent, modal);
        initComponents();
        
        dataset = data;
        mainWindow = mainWin;
        origText = txt;
        
        methodDescriptions = new String[3];
        
        methodDescriptions[0] = "<html> Calculates the probability of observing a deviation from the mean greater than that obtained by each data point assuming a normal distribution ";
        methodDescriptions[0] += "with mean given by the unweighted average of the data set and variance determined by the unbiased sample variance estimator. ";
        methodDescriptions[0] += "A data point is rejected if this probability is less than 1/2n, where n is the number of data points. This procedure is iterative, ";
        methodDescriptions[0] += "meaning each time a point is rejected the mean and variance are re-calculated and the data set is checked again. ";
        methodDescriptions[0] += "The uncertainties on the data are not taken into account. </html>";
        
        methodDescriptions[1] = "<html>Identifies all outliers at once by Peirce's principle: a set of m points should be rejected if ";
        methodDescriptions[1] += "likelihood(Complete Data Set) &lt; likelihood(Reduced Data Set) X Probability(m outliers exist) ";
        methodDescriptions[1] += "This procedure is not iterative. The uncertainties are not considered in the likelihood functions. </html>";
        
        methodDescriptions[2] = "<html>A data point is considered an outlier if the difference between the measurement and a given mean is ";
        methodDescriptions[2] += "inconsistent with zero at a certain confidence level. This procedure is not iterative. The uncertainties are considered in both the ";
        methodDescriptions[2] += "measurement and the given mean. This method can be reversed to give an estimate on the mean for the ";
        methodDescriptions[2] += "data by finding the value with minimum uncertainty for which none of the data may be considered outliers.</html>";
        
        methodDescriptionLabel.setText(methodDescriptions[0]);
        
        jPanel1.setVisible(false);
        jTextField1.setEnabled(false);
        jTextField1.setText(averagingMethods.weightedAverage(dataset).toString(false));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        methodComboBox = new javax.swing.JComboBox();
        methodDescriptionLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jComboBox1 = new javax.swing.JComboBox();
        jTextField1 = new javax.swing.JTextField();
        ConsistentMinimumVarianceButton = new javax.swing.JButton();
        checkOutliersButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Method:");

        methodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Chauvent's Criterion", "Peirce's Criterion", "Birch's Criterion" }));
        methodComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                methodComboBoxActionPerformed(evt);
            }
        });

        methodDescriptionLabel.setText("jLabel2");

        jLabel2.setText("Birch's Criterion Options");

        jLabel3.setText("Confidence Level: ");

        jLabel4.setText("Given Mean: ");

        jSpinner1.setModel(new SpinnerNumberModel(99d, 85d, 100d, 0.5d));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Unweighted Average", "Weighted Average", "EVM", "Mandel-Paule", "Custom" }));
        jComboBox1.setSelectedIndex(1);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jTextField1.setText("jTextField1");

        ConsistentMinimumVarianceButton.setText("Consistent Minimum Variance Mean");
        ConsistentMinimumVarianceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConsistentMinimumVarianceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSpinner1)
                                .addGap(97, 97, 97))
                            .addComponent(jTextField1))))
                .addGap(141, 141, 141))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(ConsistentMinimumVarianceButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ConsistentMinimumVarianceButton))
        );

        checkOutliersButton.setText("Check Outliers");
        checkOutliersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkOutliersButtonActionPerformed(evt);
            }
        });

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(methodDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(methodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(checkOutliersButton)
                        .addGap(89, 89, 89)
                        .addComponent(jButton1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(methodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(methodDescriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkOutliersButton)
                    .addComponent(jButton1))
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void methodComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_methodComboBoxActionPerformed
        methodDescriptionLabel.setText(methodDescriptions[methodComboBox.getSelectedIndex()]);
        
        if(methodComboBox.getSelectedIndex() == 2){
            jPanel1.setVisible(true);
        }else{
            jPanel1.setVisible(false);
        }
    }//GEN-LAST:event_methodComboBoxActionPerformed

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        if((double) jSpinner1.getValue() > 99.99d){
            jSpinner1.setValue(99.99d);
        }
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if(jComboBox1.getSelectedIndex() == 4){
            jTextField1.setEnabled(true);
        }else{
            jTextField1.setEnabled(false);
            if(jComboBox1.getSelectedIndex() == 0){ //unweighted average
                jTextField1.setText(averagingMethods.unweightedAverage(dataset).toString(false));
            }else if(jComboBox1.getSelectedIndex() == 1){ //weighted average
                jTextField1.setText(averagingMethods.weightedAverage(dataset).toString(false));
            }
            else if(jComboBox1.getSelectedIndex() == 2){ //EVM
                jTextField1.setText(averagingMethods.evm(dataset).toString(false));
            }else if(jComboBox1.getSelectedIndex() == 3){ //Mandel-Paule
                jTextField1.setText(averagingMethods.mp(dataset).toString(false));
            }
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void checkOutliersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkOutliersButtonActionPerformed
        dataPt[] outliers ;
        List<dataPt> outliersList;
        String[] origTextLines;
        int i,n;
        final String[] methods = {"Chauvenet", "Peirce", "Birch"};
        dataPt[] givenMean;
        String newDataset;

        n = dataset.length;
        if(methodComboBox.getSelectedIndex() == 0){
            outliers = outlierMethods.ChauvenetCriterion(dataset);
        }else if(methodComboBox.getSelectedIndex() == 1){
            outliers = outlierMethods.PeirceCriterion(dataset);
        }else{
            givenMean = VAveLib_GUI_methods.createDataset(jTextField1.getText());
            if(givenMean == null){
                return;
            }
            outliers = outlierMethods.BirchCriterion(dataset, 
                    givenMean[0],
                    (double)jSpinner1.getValue() / 100d);
        }
        
        outliersList = new ArrayList<>();
        if(outliers.length > 0){
            for(i=0; i<outliers.length; i++){
                if(averagingMethods.askRemove(methods[methodComboBox.getSelectedIndex()], outliers[i])){
                    outliersList.add(outliers[i]);
                }
            }
        }
        
        if(outliersList.isEmpty()){
            JOptionPane.showMessageDialog(null, "No outliers identified.");
        }else{
            newDataset = "";
            origTextLines = origText.split("\n");
            i=0;
            for(String line : origTextLines){
                if(dataPt.isParsable(line)){
                    if(outliersList.contains(dataset[i])){
                        newDataset += "## " + line + "  #outlier";
                    }else{
                        newDataset += line;
                    }
                    i += 1;
                }else{
                    newDataset += line;
                }
                newDataset += "\n";
            }
            mainWindow.inputBoxes.get(mainWindow.jTabbedPane1.getSelectedIndex()).setText(newDataset);
            this.dispose();
        }
    }//GEN-LAST:event_checkOutliersButtonActionPerformed

    private void ConsistentMinimumVarianceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConsistentMinimumVarianceButtonActionPerformed
        dataPt result = averagingMethods.consistanMinimumVarianceMethod(dataset, (double)jSpinner1.getValue());
        JOptionPane.showMessageDialog(null, "Consistent Minimum Variance Result: " + result.toString(false));
    }//GEN-LAST:event_ConsistentMinimumVarianceButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ConsistentMinimumVarianceButton;
    private javax.swing.JButton checkOutliersButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox methodComboBox;
    private javax.swing.JLabel methodDescriptionLabel;
    // End of variables declaration//GEN-END:variables
    private String[] methodDescriptions;
    private dataPt[] dataset;
    private String origText;
    private VAveLib_GUI mainWindow;
}

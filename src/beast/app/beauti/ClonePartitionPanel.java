package beast.app.beauti;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ClonePartitionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    final BeautiPanel beautiPanel;
    final JComboBox cloneFromComboBox;
    final JButton okButton = new JButton("OK");

	public ClonePartitionPanel(BeautiPanel beautiPanel) {
        this.beautiPanel = beautiPanel;

        DefaultListModel listModel = beautiPanel.listModel;
        Object[] models = new Object[listModel.getSize()];
        for(int i=0; i < listModel.getSize(); i++){
            models[i] = listModel.getElementAt(i);
        }

        cloneFromComboBox = new JComboBox(models);

        init();
    }


    public void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel jPanel = new JPanel(new FlowLayout());

        JLabel label = new JLabel("Clone from");
        jPanel.add(label);

        cloneFromComboBox.setMaximumRowCount(10);
        jPanel.add(cloneFromComboBox);

        add(Box.createRigidArea(new Dimension(0, 10)));
        add(jPanel);
        add(Box.createVerticalGlue());
        add(Box.createVerticalStrut(5));

        okButton.setName("ok");
        okButton.setToolTipText("Click to clone configuration from the above selected partition " +
                "into all selected partitions on the left.");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clonePartitions();
            }
        });
        add(okButton);

    } // init

    protected void clonePartitions() {
        String sourceId = cloneFromComboBox.getSelectedItem().toString();

        for (Object targetId : beautiPanel.listOfPartitions.getSelectedValues()) {
             beautiPanel.cloneFrom(sourceId, targetId.toString());
        }
    }
}

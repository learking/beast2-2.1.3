package beast.app.draw;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import beast.app.beauti.BeautiDoc;
import beast.app.beauti.BeautiPanel;
import beast.app.beauti.PartitionContext;
import beast.core.Distribution;
import beast.core.Input;
import beast.core.Operator;
import beast.core.BEASTObject;
import beast.core.parameter.RealParameter;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.math.distributions.ParametricDistribution;





public class ParameterInputEditor extends BEASTObjectInputEditor {
	boolean isParametricDistributionParameter = false;
	
    //public ParameterInputEditor() {}
    public ParameterInputEditor(BeautiDoc doc) {
		super(doc);
	}

	private static final long serialVersionUID = 1L;
    JCheckBox m_isEstimatedBox;

    @Override
    public Class<?> type() {
        return RealParameter.class;
    }
    
    
    @Override
    public void init(Input<?> input, BEASTObject plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
    	super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
    	m_plugin = plugin;
    }

    @Override
    protected void initEntry() {
        if (m_input.get() != null) {
        	if (itemNr < 0) {
        		RealParameter parameter = (RealParameter) m_input.get();
        		String s = "";
        		for (Double d : parameter.valuesInput.get()) {
        			s += d + " ";
        		}
        		m_entry.setText(s);
        	} else {
        		RealParameter parameter = (RealParameter) ((List)m_input.get()).get(itemNr);
        		String s = "";
        		for (Double d : parameter.valuesInput.get()) {
        			s += d + " ";
        		}
        		m_entry.setText(s);
        	}
        }
    }

    @Override
    protected void processEntry() {
        try {
            String sValue = m_entry.getText();
            RealParameter parameter = (RealParameter) m_input.get();
        	String oldValue = "";
    		for (Double d : parameter.valuesInput.get()) {
    			oldValue += d + " ";
    		}
            int oldDim = parameter.getDimension();
            parameter.valuesInput.setValue(sValue, parameter);
            parameter.initAndValidate();
            int newDim = parameter.getDimension();
            if (oldDim != newDim) {
            	parameter.setDimension(oldDim);
                parameter.valuesInput.setValue(oldValue, parameter);
                parameter.initAndValidate();
                throw new Exception("Entry caused change in dimension");
            }
            validateInput();
        } catch (Exception ex) {
            m_validateLabel.setVisible(true);
            m_validateLabel.setToolTipText("<html><p>Parsing error: " + ex.getMessage() + ". Value was left at " + m_input.get() + ".</p></html>");
            m_validateLabel.m_circleColor = Color.orange;
            repaint();
        }
    }


    @Override
    protected void addComboBox(JComponent box, Input<?> input, BEASTObject plugin) {
        Box paramBox = Box.createHorizontalBox();
        RealParameter parameter = null;
        if (itemNr >= 0) {
        	parameter = (RealParameter) ((List) input.get()).get(itemNr);
        } else {
        	parameter = (RealParameter) input.get();
        }

        if (parameter == null) {
            super.addComboBox(box, input, plugin);
        } else {
            setUpEntry();
            paramBox.add(m_entry);
            if (doc.bAllowLinking) {
	            boolean isLinked = doc.isLinked(m_input);
				if (isLinked || doc.suggestedLinks((BEASTObject) m_input.get()).size() > 0) {
		            JButton linkbutton = new JButton(BeautiPanel.getIcon(BeautiPanel.ICONPATH + 
		            		(isLinked ? "link.png" : "unlink.png")));
		            linkbutton.setBorder(BorderFactory.createEmptyBorder());
		            linkbutton.setToolTipText("link/unlink this parameter with another compatible parameter");
		            linkbutton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (doc.isLinked(m_input)) {
								// unlink
								try {
									BEASTObject candidate = doc.getUnlinkCandidate(m_input, m_plugin);
									m_input.setValue(candidate, m_plugin);
									doc.deLink(m_input);
								} catch (Exception e2) {
									e2.printStackTrace();
								}
								
							} else {
								// create a link
								List<BEASTObject> candidates = doc.suggestedLinks((BEASTObject) m_input.get());
								JComboBox jcb = new JComboBox(candidates.toArray());
								JOptionPane.showMessageDialog( null, jcb, "select parameter to link with", JOptionPane.QUESTION_MESSAGE);
								BEASTObject candidate = (BEASTObject) jcb.getSelectedItem();
								if (candidate != null) {
									try {
										m_input.setValue(candidate, m_plugin);
										doc.addLink(m_input);
									} catch (Exception e2) {
										e2.printStackTrace();
									}
								}
							}
							refreshPanel();
						}
					});
		            paramBox.add(linkbutton);
				}
            }            
            
            paramBox.add(Box.createHorizontalGlue());

            m_isEstimatedBox = new JCheckBox(doc.beautiConfig.getInputLabel(parameter, parameter.isEstimatedInput.getName()));
            m_isEstimatedBox.setName(input.getName() + ".isEstimated");
            if (input.get() != null) {
                m_isEstimatedBox.setSelected(parameter.isEstimatedInput.get());
            }
            m_isEstimatedBox.setToolTipText(parameter.isEstimatedInput.getHTMLTipText());

            boolean bIsClockRate = false;
            for (BEASTObject output : parameter.outputs) {
                if (output instanceof BranchRateModel.Base) {
                    bIsClockRate |= ((BranchRateModel.Base) output).meanRateInput.get() == parameter;
                }
            }
            m_isEstimatedBox.setEnabled(!bIsClockRate || !getDoc().bAutoSetClockRate);


            m_isEstimatedBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        RealParameter parameter = (RealParameter) m_input.get();
                        parameter.isEstimatedInput.setValue(m_isEstimatedBox.isSelected(), parameter);
                        if (isParametricDistributionParameter) {
                        	String sID = parameter.getID();
                        	

                        	if (sID.startsWith("RealParameter")) {
                            	ParametricDistribution parent = null; 
                	            for (BEASTObject plugin2 : parameter.outputs) {
                	                if (plugin2 instanceof ParametricDistribution) {
                                		parent = (ParametricDistribution) plugin2; 
                	                    break;
                	                }
                	            }
                	            Distribution grandparent = null; 
                	            for (BEASTObject plugin2 : parent.outputs) {
                	                if (plugin2 instanceof Distribution) {
                                		grandparent = (Distribution) plugin2; 
                	                    break;
                	                }
                	            }
                        		sID = "parameter.hyper" + parent.getClass().getSimpleName() + "-" + 
                        				m_input.getName() + "-" + grandparent.getID();
                        		doc.pluginmap.remove(parameter.getID());
                        		parameter.setID(sID);
                        		doc.addPlugin(parameter);
                        	}
                        	
                        	
                        	PartitionContext context = new PartitionContext(sID.substring("parameter.".length()));
                        	System.err.println(context + " " + sID);
                        	doc.beautiConfig.hyperPriorTemplate.createSubNet(context, true);
                        }
                        refreshPanel();
                    } catch (Exception ex) {
                        System.err.println("ParameterInputEditor " + ex.getMessage());
                    }
                }
            });
            paramBox.add(m_isEstimatedBox);

            // only show the estimate flag if there is an operator that works on this parameter
            m_isEstimatedBox.setVisible(doc.isExpertMode());
            m_isEstimatedBox.setToolTipText("Estimate value of this parameter in the MCMC chain");
            //m_editPluginButton.setVisible(false);
            //m_bAddButtons = false;
            if (itemNr < 0) {
	            for (BEASTObject plugin2 : ((BEASTObject) m_input.get()).outputs) {
	                if (plugin2 instanceof ParametricDistribution) {
	                    m_isEstimatedBox.setVisible(true);
	                	isParametricDistributionParameter = true;
	                    break;
	                }
	            }
	            for (BEASTObject plugin2 : ((BEASTObject) m_input.get()).outputs) {
	                if (plugin2 instanceof Operator) {
	                    m_isEstimatedBox.setVisible(true);
	                    //m_editPluginButton.setVisible(true);
	                    break;
	                }
	            }
            } else {
	            for (BEASTObject plugin2 : ((BEASTObject) ((List)m_input.get()).get(itemNr)).outputs) {
	                if (plugin2 instanceof Operator) {
	                    m_isEstimatedBox.setVisible(true);
	                    //m_editPluginButton.setVisible(true);
	                    break;
	                }
	            }
            }

            box.add(paramBox);
        }
    }

    @Override
    protected void addValidationLabel() {
        super.addValidationLabel();
        // make edit button invisible (if it exists) when this parameter is not estimateable
        if (m_editPluginButton != null)
            m_editPluginButton.setVisible(m_isEstimatedBox.isVisible());
    }

    @Override
    void refresh() {
        RealParameter parameter = (RealParameter) m_input.get();
		String s = "";
		for (Double d : parameter.valuesInput.get()) {
			s += d + " ";
		}
		m_entry.setText(s);
        m_isEstimatedBox.setSelected(parameter.isEstimatedInput.get());
        repaint();
    }

}

/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.alexfalappa.nbspringboot.projects.initializr;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIDefaults;

import org.apache.commons.lang.WordUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.springframework.web.util.UriTemplate;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Custom widget to display a dependency selection checkbox on the left a description label with a smaller font below the checkbox
 * and optionally up to two link buttons on the right.
 * <p>
 * The buttons open in an external browser the first "reference" and "guide" urls found in the initializr service metadata.
 *
 * @author Alessandro Falappa
 */
public class DependencyToggleBox extends javax.swing.JPanel {

    private static final String PROP_VERSION_RANGE = "versionRange";
    private static final String PROP_DESCRIPTION = "boot.description";
    private static final String PROP_REFERENCE_TEMPLATE_URL = "urltemplate.reference";
    private static final String PROP_GUIDE_TEMPLATE_URL = "urltemplate.guide";
    private static final int TOOLTIP_WIDTH = 80;
    private static final ImageIcon ICO_QST_LGHT = new ImageIcon(BootDependenciesPanel.class.getResource("question_light.png"));
    private static final ImageIcon ICO_QST_MDM = new ImageIcon(BootDependenciesPanel.class.getResource("question_medium.png"));
    private static final ImageIcon ICO_QST_DRK = new ImageIcon(BootDependenciesPanel.class.getResource("question_dark.png"));
    private static final ImageIcon ICO_BOK_LGHT = new ImageIcon(BootDependenciesPanel.class.getResource("book_light.png"));
    private static final ImageIcon ICO_BOK_MDM = new ImageIcon(BootDependenciesPanel.class.getResource("book_medium.png"));
    private static final ImageIcon ICO_BOK_DRK = new ImageIcon(BootDependenciesPanel.class.getResource("book_dark.png"));
    private static final Insets INSETS_SMALLBUTTON = new Insets(1, 1, 1, 1);
    private static final ActionListener refActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent c = (JComponent) e.getSource();
            final Object urlTemplate = c.getClientProperty(PROP_REFERENCE_TEMPLATE_URL);
            if (urlTemplate != null && currentBootVersion != null) {
                try {
                    UriTemplate template = new UriTemplate(urlTemplate.toString());
                    final URI uri = template.expand(currentBootVersion);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    };
    private static final ActionListener guideActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent c = (JComponent) e.getSource();
            final Object urlTemplate = c.getClientProperty(PROP_GUIDE_TEMPLATE_URL);
            if (urlTemplate != null && currentBootVersion != null) {
                try {
                    UriTemplate template = new UriTemplate(urlTemplate.toString());
                    final URI uri = template.expand(currentBootVersion);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    };
    private static final UIDefaults uiDef = new UIDefaults();
    private static String currentBootVersion = null;
    private JButton bReference;
    private JButton bGuide;

    static {
        uiDef.put("Button.contentMargins", INSETS_SMALLBUTTON);
    }

    public DependencyToggleBox() {
        initComponents();
    }

    public void initFromMetadata(JsonNode dn) {
        final String name = dn.path("name").asText();
        final String id = dn.path("id").asText();
        final String description = dn.path("description").asText();
        final String versRange = dn.path("versionRange").asText();
        lDepName.setText(name);
        this.setName(id);
        this.putClientProperty(PROP_VERSION_RANGE, versRange);
        this.putClientProperty(PROP_DESCRIPTION, description);
        if (dn.has("_links")) {
            final JsonNode links = dn.path("_links");
            if (links.has("reference")) {
                JsonNode ref = links.path("reference");
                if (ref.isArray()) {
                    ref = ref.get(0);
                }
                setLinkReference(ref.path("href").asText(), ref.path("title").asText());
            }
            if (links.has("guide")) {
                JsonNode ref = links.path("guide");
                if (ref.isArray()) {
                    ref = ref.get(0);
                }
                setLinkGuide(ref.path("href").asText(), ref.path("title").asText());
            }
        }
    }

    public void setLinkReference(String url, String title) {
        Objects.requireNonNull(url);
        if (bReference == null) {
            bReference = new JButton();
            bReference.setIcon(ICO_QST_LGHT);
            bReference.setRolloverIcon(ICO_QST_MDM);
            bReference.setPressedIcon(ICO_QST_DRK);
            bReference.setMargin(INSETS_SMALLBUTTON);
            bReference.setOpaque(false);
            bReference.setContentAreaFilled(false);
            bReference.setBorderPainted(false);
            bReference.setFocusable(false);
            bReference.putClientProperty("Nimbus.Overrides", uiDef);
            bReference.addActionListener(refActionListener);
            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 3;
            gbc.gridy = 0;
            this.add(bReference, gbc);
        }
        bReference.setToolTipText(title != null && !title.isEmpty() ? String.format("Reference: %s", title) : "Reference");
        bReference.putClientProperty(PROP_REFERENCE_TEMPLATE_URL, url);
    }

    public void setLinkGuide(String url, String title) {
        Objects.requireNonNull(url);
        if (bGuide == null) {
            bGuide = new JButton();
            bGuide.setIcon(ICO_BOK_LGHT);
            bGuide.setRolloverIcon(ICO_BOK_MDM);
            bGuide.setPressedIcon(ICO_BOK_DRK);
            bGuide.setMargin(INSETS_SMALLBUTTON);
            bGuide.setOpaque(false);
            bGuide.setContentAreaFilled(false);
            bGuide.setBorderPainted(false);
            bGuide.setFocusable(false);
            bGuide.putClientProperty("Nimbus.Overrides", uiDef);
            bGuide.addActionListener(guideActionListener);
            GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 0;
            this.add(bGuide, gbc);
        }
        bGuide.setToolTipText(title != null && !title.isEmpty() ? String.format("Guide: %s", title) : "Guide");
        bGuide.putClientProperty(PROP_GUIDE_TEMPLATE_URL, url);
    }

    public void adaptToBootVersion(String bootVersion) {
        currentBootVersion = bootVersion;
        String verRange = (String) this.getClientProperty(PROP_VERSION_RANGE);
        String description = (String) this.getClientProperty(PROP_DESCRIPTION);
        final boolean allowable = allowable(verRange, bootVersion);
        cbDep.setEnabled(allowable);
        lDepName.setEnabled(allowable);
        lDesc.setText(prepDescription(description, allowable, verRange));
    }

    public boolean isSelected() {
        return cbDep.isSelected();
    }

    public void setSelected(boolean flag) {
        cbDep.setSelected(flag);
    }

    public String getText() {
        return lDepName.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbDep = new javax.swing.JCheckBox();
        lDepName = new javax.swing.JLabel();
        lDesc = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());
        add(cbDep, new java.awt.GridBagConstraints());

        org.openide.awt.Mnemonics.setLocalizedText(lDepName, org.openide.util.NbBundle.getMessage(DependencyToggleBox.class, "DependencyToggleBox.lDepName.text")); // NOI18N
        lDepName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lDepNameMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(lDepName, gridBagConstraints);

        lDesc.setFont(lDesc.getFont().deriveFont(lDesc.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(lDesc, org.openide.util.NbBundle.getMessage(DependencyToggleBox.class, "DependencyToggleBox.lDesc.text")); // NOI18N
        lDesc.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(lDesc, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void lDepNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lDepNameMouseClicked
        cbDep.doClick();
    }//GEN-LAST:event_lDepNameMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbDep;
    private javax.swing.JLabel lDepName;
    private javax.swing.JLabel lDesc;
    // End of variables declaration//GEN-END:variables

    private boolean allowable(String verRange, String bootVersion) {
        boolean ret = true;
        if (verRange != null && !verRange.isEmpty()) {
            if (verRange.indexOf('[') >= 0 || verRange.indexOf('(') >= 0
                    || verRange.indexOf(']') >= 0 || verRange.indexOf(')') >= 0) {
                // bounded range
                String[] bounds = verRange.substring(1, verRange.length() - 1).split(",");
                // check there are two bounds
                if (bounds.length != 2) {
                    return false;
                }
                // test various cases
                if (bootVersion.compareTo(bounds[0]) > 0 && bootVersion.compareTo(bounds[1]) < 0) {
                    return true;
                } else if (bootVersion.compareTo(bounds[0]) == 0 && verRange.startsWith("[")) {
                    return true;
                } else if (bootVersion.compareTo(bounds[0]) == 0 && verRange.startsWith("(")) {
                    return false;
                } else if (bootVersion.compareTo(bounds[1]) == 0 && verRange.endsWith("]")) {
                    return true;
                } else if (bootVersion.compareTo(bounds[1]) == 0 && verRange.endsWith(")")) {
                    return false;
                } else {
                    return false;
                }
            } else {
                // unbounded range
                return bootVersion.compareTo(verRange) >= 0;
            }
        }
        return ret;
    }

    private String prepDescription(String description, boolean allowable, String versRange) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(WordUtils.wrap(description, TOOLTIP_WIDTH, "<br/>", false));
        if (!allowable) {
            sb.append("<br/><i>").append(decodeVersRange(versRange)).append("</i>");
        }
        return sb.toString();
    }

    private String decodeVersRange(String verRange) {
        StringBuilder sb = new StringBuilder();
        if (verRange != null && !verRange.isEmpty()) {
            if (verRange.indexOf('[') >= 0 || verRange.indexOf('(') >= 0 || verRange.indexOf(']') >= 0 || verRange.indexOf(')') >= 0) {
                // bounded range
                String[] bounds = verRange.substring(1, verRange.length() - 1).split(",");
                // check there are two bounds
                if (bounds.length == 2) {
                    sb.append(bounds[0]);
                    if (verRange.startsWith("[")) {
                        sb.append(" &lt;= ");
                    } else if (verRange.startsWith("(")) {
                        sb.append(" &lt; ");
                    }
                    sb.append("Spring Boot version");
                    if (verRange.endsWith("]")) {
                        sb.append(" &lt;= ");
                    } else if (verRange.endsWith(")")) {
                        sb.append(" &lt; ");
                    }
                    sb.append(bounds[1]);
                }
            } else {
                // unbounded range
                sb.append("Spring Boot version &gt;= ").append(verRange);
            }
        }
        return sb.toString();
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client.GUI;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Kenneth
 */
public class FileChooser extends JFileChooser {
private final TextFilter textFilter = new TextFilter();

    @Override
    public void setFileFilter(FileFilter textFilter) {
        super.setFileFilter(this.textFilter);
    }
@Override
public void approveSelection(){
    File f = getSelectedFile();
    if(f.exists() && getDialogType() == SAVE_DIALOG){
        int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
        switch(result){
            case JOptionPane.YES_OPTION:
                super.approveSelection();
                return;
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CANCEL_OPTION:
                cancelSelection();
                return;
        }
    }
    super.approveSelection();
}

}

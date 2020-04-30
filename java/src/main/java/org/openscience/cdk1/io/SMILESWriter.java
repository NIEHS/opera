/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1997-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk1.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.openscience.cdk1.annotations.TestClass;
import org.openscience.cdk1.annotations.TestMethod;
import org.openscience.cdk1.exception.CDKException;
import org.openscience.cdk1.interfaces.IChemFile;
import org.openscience.cdk1.interfaces.IChemObject;
import org.openscience.cdk1.interfaces.IMolecule;
import org.openscience.cdk1.interfaces.IMoleculeSet;
import org.openscience.cdk1.io.formats.IResourceFormat;
import org.openscience.cdk1.io.formats.SMILESFormat;
import org.openscience.cdk1.io.setting.BooleanIOSetting;
import org.openscience.cdk1.io.setting.IOSetting;
import org.openscience.cdk1.smiles.SmilesGenerator;
import org.openscience.cdk1.tools.ILoggingTool;
import org.openscience.cdk1.tools.LoggingToolFactory;

/**
 * Writes the SMILES strings to a plain text file.
 *
 * @cdk.module  smiles
 * @cdk.githash
 * @cdk.iooptions
 *
 * @cdk.keyword file format, SMILES
 */
@TestClass("org.openscience.cdk1.io.SMILESWriterTest")
public class SMILESWriter extends DefaultChemObjectWriter {

    private static ILoggingTool logger =
        LoggingToolFactory.createLoggingTool(SMILESWriter.class);
    private BufferedWriter writer;

    private BooleanIOSetting useAromaticityFlag;

    /**
     * Constructs a new SMILESWriter that can write a list of SMILES to a Writer
     *
     * @param   out  The Writer to write to
     */
    public SMILESWriter(Writer out) {
    	try {
    		if (out instanceof BufferedWriter) {
                writer = (BufferedWriter)out;
            } else {
                writer = new BufferedWriter(out);
            }
        } catch (Exception exc) {
        }
        initIOSettings();
    }

    public SMILESWriter(OutputStream output) {
        this(new OutputStreamWriter(output));
    }

    public SMILESWriter() {
        this(new StringWriter());
    }

    @TestMethod("testGetFormat")
    public IResourceFormat getFormat() {
        return SMILESFormat.getInstance();
    }

    public void setWriter(Writer out) throws CDKException {
    	if (out instanceof BufferedWriter) {
            writer = (BufferedWriter)out;
        } else {
            writer = new BufferedWriter(out);
        }
    }

    public void setWriter(OutputStream output) throws CDKException {
    	setWriter(new OutputStreamWriter(output));
    }

    /**
     * Constructs a new SMILESWriter that can write an list of SMILES to a given OutputStream
     *
     * @param   out  The OutputStream to write to
     */
    public SMILESWriter(FileOutputStream out) {
        this(new OutputStreamWriter(out));
    }

    /**
     * Flushes the output and closes this object
     */
    @TestMethod("testClose")
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

	@TestMethod("testAccepts")
    public boolean accepts(Class classObject) {
        if (IMolecule.class.equals(classObject)) return true;
        if (IMoleculeSet.class.equals(classObject)) return true;
		Class[] interfaces = classObject.getInterfaces();
		for (int i=0; i<interfaces.length; i++) {
			if (IMoleculeSet.class.equals(interfaces[i])) return true;
			if (IMolecule.class.equals(interfaces[i])) return true;
		}
        Class superClass = classObject.getSuperclass();
        if (superClass != null) return this.accepts(superClass);
		return false;
	}

    /**
     * Writes the content from object to output.
     *
     * @param   object  IChemObject of which the data is outputted.
     */
	public void write(IChemObject object) throws CDKException {
		if (object instanceof IMoleculeSet) {
		    writeMoleculeSet((IMoleculeSet)object);
		} else if (object instanceof IMolecule) {
		    writeMolecule((IMolecule)object);
		} else {
		    throw new CDKException("Only supported is writing of ChemFile and Molecule objects.");
		}
	}

	/**
	 * Writes a list of molecules to an OutputStream
	 *
	 * @param   som  MoleculeSet that is written to an OutputStream
	 */
	public void  writeMoleculeSet(IMoleculeSet som)
	{
		writeMolecule(som.getMolecule(0));
		for (int i = 1; i <= som.getMoleculeCount() - 1; i++) {
			try {
				writeMolecule(som.getMolecule(i));
			} catch (Exception exc) {
			}
		}
	}

    /**
     * Writes the content from molecule to output.
     *
     * @param   molecule  Molecule of which the data is outputted.
     */
    public void writeMolecule(IMolecule molecule) {
        SmilesGenerator sg = new SmilesGenerator();
        sg.setUseAromaticityFlag(useAromaticityFlag.isSet());
        String smiles = "";
        try {
            smiles = sg.createSMILES(molecule);
            logger.debug("Generated SMILES: " + smiles);
            writer.write(smiles);
            writer.newLine();
            writer.flush();
            logger.debug("file flushed...");
        } catch(Exception exc) {
            logger.error("Error while writing Molecule: ", exc.getMessage());
            logger.debug(exc);
        }
    }

    private void initIOSettings() {
        useAromaticityFlag = new BooleanIOSetting(
            "UseAromaticity",
            IOSetting.LOW,
            "Should aromaticity information be stored in the SMILES?",
            "false"
        );
    }

    public void customizeJob() {
        fireIOSettingQuestion(useAromaticityFlag);
    }

    public IOSetting[] getIOSettings() {
        IOSetting[] settings = new IOSetting[1];
        settings[0] = useAromaticityFlag;
        return settings;
    }
}

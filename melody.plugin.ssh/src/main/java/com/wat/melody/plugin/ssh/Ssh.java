package com.wat.melody.plugin.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.Property;
import com.wat.melody.plugin.ssh.common.AbstractSshConnectionManagedOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.types.Exec;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Ssh extends AbstractSshConnectionManagedOperation {

	private static Log log = LogFactory.getLog(Ssh.class);

	/**
	 * The 'ssh' XML element used in the Sequence Descriptor
	 */
	public static final String SSH = "ssh";

	/**
	 * The 'declare' XML Nested Element
	 */
	public static final String DECLARE_NE = "declare";

	/**
	 * The 'export' XML Nested Element
	 */
	public static final String EXPORT_NE = "export";

	/**
	 * The 'description' XML attribute
	 */
	public static final String DESCRIPTION_ATTR = "description";

	/**
	 * The 'requiretty' XML attribute
	 */
	public static final String REQUIRETTY_ATTR = "requiretty";

	private String msCommandToExecute;
	private String msDescription;
	private boolean mbRequiretty;

	public Ssh() {
		super();
		msCommandToExecute = "";
		setDescription("[exec ssh]");
		setRequiretty(false);
	}

	@Override
	public void validate() throws SshException {
		super.validate();
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		int exitStatus = execSshCommand(getCommandToExecute(), getRequiretty(),
				getDescription());
		String recapMsg = getDescription() + " " + "[STATUS] ";
		switch (exitStatus) {
		case 0:
			log.info(recapMsg + "--->    OK    <---");
			break;
		case 200:
			log.warn(recapMsg + "--->   WARN   <---");
			break;
		case 201:
			log.warn(recapMsg + "--->   KILL   <---");
			break;
		case 202:
			log.error(recapMsg + "--->   FAIL   <---");
			throw new SshException(recapMsg + "--->   FAIL   <---");
		default:
			log.error(recapMsg + "---> CRITICAL <---  [" + exitStatus + "]");
			throw new SshException(recapMsg + "---> CRITICAL <---  ["
					+ exitStatus + "]" + Util.NEW_LINE
					+ "Here is the complete script which generates the error :"
					+ Util.NEW_LINE + "-----" + Util.NEW_LINE
					+ getCommandToExecute().trim() + Util.NEW_LINE + "-----"
					+ Util.NEW_LINE);
		}
	}

	public String getCommandToExecute() {
		return msCommandToExecute;
	}

	public String getDescription() {
		return msDescription;
	}

	@Attribute(name = DESCRIPTION_ATTR)
	public String setDescription(String d) {
		String previous = getDescription();
		msDescription = d;
		return previous;
	}

	public boolean getRequiretty() {
		return mbRequiretty;
	}

	@Attribute(name = REQUIRETTY_ATTR)
	public boolean setRequiretty(boolean requiretty) {
		boolean previous = getRequiretty();
		mbRequiretty = requiretty;
		return previous;
	}

	@NestedElement(name = Exec.EXEC, type = Type.ADD)
	public void addInsludeScript(Exec is) throws SshException {
		if (is == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid IncludeScript.");
		}
		if (is.getCommand() != null && is.getFile() != null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_BOTH_COMMAND_OR_SCRIPT_ATTR,
					Exec.COMMAND_ATTR, Exec.FILE_ATTR));
		} else if (is.getCommand() != null) {
			msCommandToExecute += is.getCommand() + "\n";
		} else if (is.getFile() != null) {
			try {
				String fileContent = null;
				if (is.getTemplate()) {
					try {
						fileContent = getContext().expand(
								Paths.get(is.getFile().toString()));
					} catch (IllegalFileException Ex) {
						throw new RuntimeException("Unexpected error while "
								+ "templating the file " + is.getFile() + "."
								+ "Source code has certainly been modified "
								+ "and a bug have been introduced.", Ex);
					} catch (ExpressionSyntaxException Ex) {
						throw new SshException(Ex);
					}
				} else {
					fileContent = new String(Files.readAllBytes(Paths.get(is
							.getFile().toString())));
				}
				msCommandToExecute += fileContent + "\n";
			} catch (IOException Ex) {
				throw new SshException(Messages.bind(
						Messages.SshEx_READ_IO_ERROR, is.getFile()), Ex);
			}
		} else {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_COMMAND_OR_SCRIPT_ATTR,
					Exec.COMMAND_ATTR, Exec.FILE_ATTR));
		}
	}

	@NestedElement(name = DECLARE_NE, type = Type.ADD)
	public void addDeclareVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid DeclareVariable.");
		}
		msCommandToExecute += "declare " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

	@NestedElement(name = EXPORT_NE, type = Type.ADD)
	public void addExportVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid DeclareVariable.");
		}
		msCommandToExecute += "export " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

}

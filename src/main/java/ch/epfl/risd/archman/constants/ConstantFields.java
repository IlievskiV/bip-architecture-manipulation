package ch.epfl.risd.archman.constants;

import java.util.LinkedList;
import java.util.List;

/**
 * This class contains constant fields used in this project
 */
public final class ConstantFields {

	/* The name of the PATH parameter in the configuration file */
	public static final String PATH_PARAM = "path";

	/* The name of the COORDINATORS parameter in the configuration file */
	public static final String COORDINATORS_PARAM = "coordinators";

	/* The name of the OPERANDS parameter in the configuration file */
	public static final String OPERANDS_PARAM = "operands";

	/* The name of the PORTS parameter in the configuration file */
	public static final String PORTS_PARAM = "ports";

	/* The name of the CONNECTORS parameter in the configuration file */
	public static final String CONNECTORS_PARAM = "connectors";

	/* The name of the INTERACTIONS parameter in the configuration file */
	public static final String INTERACTIONS_PARAM = "interactions";

	/* The name of the OPERANDS MAPPING parameter in the configuration files */
	public static final String OPERANDS_MAPPING_PARAM = "operands_mapping";

	/* The name of the PORTS MAPPING parameter in the configuration file */
	public static final String PORTS_MAPPING_PARAM = "ports_mapping";

	/*
	 * The name of the ARCH_STYLE_CONF_FILE_PATH parameter in the test
	 * configuration file
	 */
	public static final String ARCH_STYLE_CONF_FILE_PATH_PARAM = "architecture_style_conf_file_path";

	/*
	 * The name of the ARCH_OP_CONF_FILE_PATH parameter in the test
	 * configuration file
	 */
	public static final String ARCH_OP_CONF_FILE_PATH_PARAM = "architecture_operands_conf_file_path";

	/*
	 * The name of the OUTPUT_FOLDER_PATH parameter in the test configuration
	 * file
	 */
	public static final String OUTPUT_FOLDER_PATH_PARAM = "ouput_folder_path";

	/*
	 * List of required parameters for the architecture style configuration file
	 */
	public static final List<String> architectureStyleRequiredParams = new LinkedList<String>() {
		{
			add(PATH_PARAM);
			add(COORDINATORS_PARAM);
			add(OPERANDS_PARAM);
			add(PORTS_PARAM);
			add(CONNECTORS_PARAM);
		}
	};

	/*
	 * List of required parameters for the architecture operands configuration
	 * file
	 */
	public static final List<String> architectureOperandsRequiredParams = new LinkedList<String>() {
		{
			add(PATH_PARAM);
			add(OPERANDS_MAPPING_PARAM);
			add(PORTS_MAPPING_PARAM);
		}
	};

	/*
	 * List of required parameters for the architecture instance configuration
	 * file
	 */
	public static final List<String> architectureInstanceRequiredParams = new LinkedList<String>() {
		{
			add(PATH_PARAM);
			add(COORDINATORS_PARAM);
			add(OPERANDS_PARAM);
			add(PORTS_PARAM);
			add(INTERACTIONS_PARAM);
		}
	};

	/*
	 * List of required parameters for the architecture instantiation
	 * configuration file
	 */
	public static final List<String> architectureInstantiationTestRequiredParams = new LinkedList<String>() {
		{
			add(ARCH_STYLE_CONF_FILE_PATH_PARAM);
			add(ARCH_OP_CONF_FILE_PATH_PARAM);
			add(OUTPUT_FOLDER_PATH_PARAM);
		}
	};

	/* String used in the process of generating connector type names */
	public static final String CONNECTOR_TYPE = "connectorType";
	
	/* String used in the process of generating connector type instance names */
	public static final String INSTANCE = "Instance";

}
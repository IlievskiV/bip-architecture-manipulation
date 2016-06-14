package ch.epfl.risd.archman.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import BIPTransformation.TransformationFunction;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.IllegalTransitionPortException;
import ch.epfl.risd.archman.exceptions.InvalidAtomTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidComponentNameException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidVariableNameException;
import ch.epfl.risd.archman.extractor.ArchitectureOperandsExtractor;
import ch.epfl.risd.archman.extractor.ArchitectureStyleExtractor;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ch.epfl.risd.archman.model.PortTuple;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.PetriNet;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.InnerPortReference;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PartElementReference;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.PortParameterReference;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.ACFusion;

public class ArchitectureInstantiator {

	/****************************************************************************/
	/* VARIABLES */
	/***************************************************************************/

	/* The Architecture InExtractorstance for building */
	private ArchitectureInstance architectureInstance;

	/* The Architecture Style or Template of the instance */
	private ArchitectureStyle architectureStyle;

	/* The Architecture Operands for the instance */
	private ArchitectureOperands architectureOperands;

	/* The builder for the Architecture Instance */
	private ArchitectureInstanceBuilder architectureInstanceBuilder;

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method generates the BIP code for the existing Architecture Instance
	 * and writes it to the external file
	 * 
	 * @param pathToBIPFile
	 *            - the path to the file where the BIP code will be written
	 * @param pathToConfFile
	 *            - the path to the configuration file
	 */
	public void instantiateArchitecture(String pathToBIPFile, String pathToConfFile) throws IOException {
		/* Write the generated code in the file */
		TransformationFunction.CreateBIPFile(pathToBIPFile, this.architectureInstance.getBipFileModel().getSystem());
		/* Set the path to the BIP file containing the code */
		this.architectureInstance.getParameters().put(ConstantFields.PATH_PARAM, pathToBIPFile);

		/* Create the File object */
		File confFile = new File(pathToConfFile);

		/* If the configuration file does not exist */
		if (!confFile.exists()) {
			/* Create the file */
			confFile.createNewFile();
			/* Write the content */
			this.generateConfigurationFile(confFile);
		} else {
			/* Erase the previous text and write new content */
			this.generateConfigurationFile(confFile);
		}
	}

	/**
	 * This method generates the configuration file based on the Architecture
	 * Instance we have.
	 * 
	 * @throws IOException
	 */
	private void generateConfigurationFile(File confFile) throws IOException {
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(confFile);

			/* Print the path to the BIP file */
			printer.println(ConstantFields.PATH_PARAM + ":"
					+ (this.architectureInstance.getParameters().get(ConstantFields.PATH_PARAM) == null ? ""
							: this.architectureInstance.getParameters().get(ConstantFields.PATH_PARAM)));

			/* Print the names of the coordinators for the architecture */
			printer.println(ConstantFields.COORDINATORS_PARAM + ":"
					+ (this.architectureInstance.getParameters().get(ConstantFields.COORDINATORS_PARAM) == null ? ""
							: this.architectureInstance.getParameters().get(ConstantFields.COORDINATORS_PARAM)));
		} finally {
			if (printer != null) {
				printer.flush();
				printer.close();
			}
		}
	}

	/**
	 * This method calculates the parameters in every connector tuple, i.e. it
	 * calculates the multiplicity and the degree
	 * 
	 * @throws ArchitectureBuilderException
	 */
	private void calculateParameters() throws ArchitectureBuilderException {

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = this.architectureStyle.getConnectors();

		/* Iterate the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {
			/* Get the port tuples */
			List<PortTuple> portTuples = connectorTuple.getPortTuples();
			/* Iterate the port tuples */
			for (PortTuple portTuple : portTuples) {

				/* If the port tuple is coordinator tuple */
				if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
					/* If the multiplicity is not calculated */
					if (!portTuple.isMultiplicityCalculated()) {
						/* Calculate the multiplicity */
						calculateMultiplicityCoordinator(connectorTuple, portTuple);
					}
					/* If the degree is not calculated */
					if (!portTuple.isDegreeCalculated()) {
						/* Calculate the degree */
						calculateDegreeCoordinator(connectorTuple, portTuple);
					}
				}

				/* If the port tuple is operand tuple */
				if (portTuple.getType() == PortTupleType.OPERAND_TUPLE) {
					/* If the multiplicity is not calculated */
					if (!portTuple.isMultiplicityCalculated()) {
						/* Calculate the multiplicity */
						calculateMultiplicityOperand(portTuple);
					}
					/* If the degree is not calculated */
					if (!portTuple.isDegreeCalculated()) {
						/* Calculate the degree */
						calculateDegreeOperand(connectorTuple, portTuple);
					}
				}

			}
		}
	}

	/**
	 * This method calculates the multiplicity for the coordinator end of the
	 * connector (not implemented)
	 * 
	 * @param connectorTuple
	 *            - The connector tuple for which the coordinator end
	 *            multiplicity should be calculated
	 * @param portTuple
	 *            - The port tuple which is coordinator end in the connector
	 *            tuple
	 * @throws ArchitectureBuilderException
	 */
	private void calculateMultiplicityCoordinator(ConnectorTuple connectorTuple, PortTuple portTuple)
			throws ArchitectureBuilderException {
		throw new ArchitectureBuilderException(
				"Not implemented feature: calculation of the multiplicity of the coordinator end");
	}

	/**
	 * This method calculates the degree for the coordinator end of the
	 * connector
	 * 
	 * @param connectorTuple
	 *            - The connector tuple for which the coordinator end degree
	 *            should be calculated
	 * @param portTuple
	 *            - The port tuple which is coordinator end in the connector
	 *            tuple
	 */
	private void calculateDegreeCoordinator(ConnectorTuple connectorTuple, PortTuple portTuple) {
		/* The resultant degree */
		int degree = 1;
		/* Get all operand port tuples */
		List<PortTuple> operandPortTuples = connectorTuple.getOperandPortTuples();
		/* Iterate all operand port tuples */
		for (PortTuple pt : operandPortTuples) {
			/*
			 * Get the name of the operand instance name where the port belongs
			 */
			String operandInstanceName = (pt.getPortInstanceName().split("\\."))[0];
			/*
			 * Get the number of operands to which this parameter operand is
			 * mapped
			 */
			int numMappingOperands = this.architectureOperands.getOperandsMapping().get(operandInstanceName).size();

			degree *= numMappingOperands;
		}

		/* Set the calculated degree */
		portTuple.setCalculatedDegree(degree);
		portTuple.setDegreeCalculated(true);
	}

	/**
	 * This method calculates the multiplicity for the operand end of the
	 * connector tuple
	 * 
	 * @param portTuple
	 *            - The port tuple which is the operand end of the connector
	 *            tuple
	 */
	private void calculateMultiplicityOperand(PortTuple portTuple) {
		/* Get the name of the port instance */
		String portInstanceName = portTuple.getPortInstanceName();
		/* Find to how many ports it is mapping */
		int multiplicity = this.architectureOperands.getPortsMapping().get(portInstanceName).size();
		/* Set the multiplicity */
		portTuple.setCalculatedMultiplicity(multiplicity);
		portTuple.setMultiplicityCalculated(true);
	}

	/**
	 * This method calculates the degree for the operand end of the connector
	 * tuple (not implemented)
	 * 
	 * @param connectorTuple
	 *            - The connector tuple for which the operator end degree should
	 *            be calculated
	 * @param portTuple
	 *            - The port tuple which is operator end in the connector tuple
	 * @throws ArchitectureBuilderException
	 */
	private void calculateDegreeOperand(ConnectorTuple connectorTuple, PortTuple portTuple)
			throws ArchitectureBuilderException {
		throw new ArchitectureBuilderException("Not implemented feature: calculation of the degree of the operandend");
	}

	private void createInstance() throws IOException, ArchitectureExtractorException, InvalidComponentNameException,
			InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException, InvalidPortTypeNameException, InvalidAtomTypeNameException,
			InvalidVariableNameException, InvalidPortNameException, IllegalTransitionPortException {

		/* Instantiate architecture operand extractor */
		ArchitectureOperandsExtractor architectureOperandsExtractor = new ArchitectureOperandsExtractor(
				architectureOperands);

		/* Instantiate architecture style extractor */
		ArchitectureStyleExtractor architectureStyleExtractor = new ArchitectureStyleExtractor(architectureStyle);

		/* Extractor for the architecture instance */
		BIPExtractor extractor = new BIPExtractor(this.architectureInstance.getBipFileModel());

		/* 1. Operands */

		/* Get the list of all operands */
		List<Component> operands = architectureOperandsExtractor.getArchitectureOperands();

		/* Insert the operands */
		this.architectureInstanceBuilder.insertComponents(operands, false);

		/* 2. Get the list of all coordinators */
		List<Component> coordinators = architectureStyleExtractor.getArchitectureStyleCoordinators();

		/* Insert the coordinators */
		this.architectureInstanceBuilder.insertComponents(coordinators, true);

		/* For the 1:1 -- 1:1 case */
		boolean coordinatorCleared = false;

		/* 3. Get the list of calculated Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectors();

		/* Iterate connector tuples */
		for (ConnectorTuple ct : connectorTuples) {

			/* Get the name of the connector type */
			String connecotrTypeName = architectureStyleExtractor.getConnectorByName(ct.getConnectorInstanceName())
					.getType().getName();

			/*
			 * Here we will consider only the special case, zero or one
			 * coordinator port tuples, zero or one operand port tuples
			 */

			/* Declare the coordinator port tuple */
			PortTuple coordinatorPortTuple;
			/* Declare the operand port tuple */
			PortTuple operandPortTuple;

			/* One coordinator tuple and one operand tuple */
			if (ct.getCoordinatorPortTuples().size() != 0 && ct.getOperandPortTuples().size() != 0) {

				/* Here, we have three cases */

				/* Get the coordinator port tuple */
				coordinatorPortTuple = ct.getCoordinatorPortTuples().get(0);
				/* Get the operand port tuple */
				operandPortTuple = ct.getOperandPortTuples().get(0);

				/* First case 1:n -- 1:1 */
				if (coordinatorPortTuple.getCalculatedMultiplicity() == 1
						&& coordinatorPortTuple.getCalculatedDegree() > 1
						&& operandPortTuple.getCalculatedMultiplicity() == 1
						&& operandPortTuple.getCalculatedDegree() == 1) {

					/* Get the coordinator port name and type */
					String portName1 = (coordinatorPortTuple.getPortInstanceName().split("\\."))[1];
					/* Declare the port type */
					PortType portType1;

					/* If the coordinator port type does not exist */
					if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName1).getType())) {
						portType1 = this.architectureInstanceBuilder
								.copyPortType(architectureStyleExtractor.getPortByName(portName1).getType());
					} else {
						portType1 = extractor.getPortTypeByName(
								architectureStyleExtractor.getPortByName(portName1).getType().getName());
					}

					/* Get operand port name and type */
					String portName2 = (operandPortTuple.getPortInstanceName().split("\\."))[1];

					PortType portType2;

					/* If the operand port type does not exist */
					if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName2).getType())) {
						portType2 = this.architectureInstanceBuilder
								.copyPortType(architectureStyleExtractor.getPortByName(portName2).getType());
					} else {
						portType2 = extractor.getPortTypeByName(
								architectureStyleExtractor.getPortByName(portName2).getType().getName());
					}

					/* Create Port Parameters */
					PortParameter portParameter1 = this.architectureInstanceBuilder.createPortParameter(portType1,
							portName1);
					PortParameter portParameter2 = this.architectureInstanceBuilder.createPortParameter(portType2,
							portName2);

					/* Create the list of port parameters */
					List<PortParameter> portParameters = new LinkedList<PortParameter>();
					portParameters.add(portParameter1);
					portParameters.add(portParameter2);

					/* Create the port parameter references */
					PortParameterReference portParameterReference1 = this.architectureInstanceBuilder
							.createPortParameterReference(portParameter1);
					PortParameterReference portParameterReference2 = this.architectureInstanceBuilder
							.createPortParameterReference(portParameter2);

					/* Create list of port parameter references */
					List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
					portParameterReferences.add(portParameterReference1);
					portParameterReferences.add(portParameterReference2);

					/* Create a list of AC Expressions */
					List<ACExpression> expressions = new LinkedList<ACExpression>();
					expressions.addAll(portParameterReferences);

					/* Create the ACFusion */
					ACFusion acFusion = this.architectureInstanceBuilder.createACFusion(expressions);

					/* Create an empty list of interactions */
					List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

					/* Create the connector type */
					ConnectorType connectorType = this.architectureInstanceBuilder.createConnectorType(
							connecotrTypeName, portParameters, acFusion, interactionSpecifications);

					/*
					 * Retrieve the set of ports to which the operand port is
					 * mapped, in order to get the number of instances of the
					 * corresponding connector type
					 */
					Set<String> portsMapping = architectureOperands.getPortsMapping()
							.get(operandPortTuple.getPortInstanceName());

					/* counter for creating connector instance names */
					int i = 0;

					/* Iterate mapping ports */
					for (String portMapping : portsMapping) {

						/* Instantiate empty list of port parameters */
						List<ActualPortParameter> acp = new LinkedList<ActualPortParameter>();

						/* The first input port is the coordinator port */
						PartElementReference partElementReference1 = this.architectureInstanceBuilder
								.createPartElementReference(
										architectureStyleExtractor.getArchitectureStyleCoordinators().get(0));
						InnerPortReference innerPortReference1 = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference1,
										architectureStyleExtractor.getPortByName(portName1));

						/* The second input port is from the mapped ports */
						PartElementReference partElementReference2 = this.architectureInstanceBuilder
								.createPartElementReference(architectureOperandsExtractor
										.getComponentByName((portMapping.split("\\."))[0]));
						InnerPortReference innerPortReference2 = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference2,
										architectureOperandsExtractor.getPortByName((portMapping.split("\\."))[1]));

						/* Add the input ports in the list */
						acp.add(innerPortReference1);
						acp.add(innerPortReference2);

						/* Create one instance of the connector type */
						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName() + "_" + (i + 1),
								connectorType, this.architectureInstance.getBipFileModel().getRootType(), acp);
						i++;
					}

				}
				/* Case 1:1 - n:1 */
				else if (coordinatorPortTuple.getCalculatedMultiplicity() == 1
						&& coordinatorPortTuple.getCalculatedDegree() == 1
						&& operandPortTuple.getCalculatedMultiplicity() > 1
						&& operandPortTuple.getCalculatedDegree() == 1) {

					/* List of port parameters, for the connector type */
					List<PortParameter> portParameters = new LinkedList<PortParameter>();

					/*
					 * List of Actual Port Parameters, for the connector
					 * instance
					 */
					List<ActualPortParameter> acp = new LinkedList<ActualPortParameter>();

					/* Get the coordinator port name and type */
					String portName1 = (coordinatorPortTuple.getPortInstanceName().split("\\."))[1];

					PortType portType1;

					/* If the port type does not exist */
					if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName1).getType())) {
						portType1 = this.architectureInstanceBuilder
								.copyPortType(architectureStyleExtractor.getPortByName(portName1).getType());
					} else {
						portType1 = extractor.getPortTypeByName(
								architectureStyleExtractor.getPortByName(portName1).getType().getName());
					}

					/* Create port parameter */
					PortParameter portParameter1 = this.architectureInstanceBuilder.createPortParameter(portType1,
							portName1);
					/* Add the coordinator port parameter */
					portParameters.add(portParameter1);

					/* Get the name of the coordinator component */
					String componentName1 = (coordinatorPortTuple.getPortInstanceName().split("\\."))[0];
					/* Create coordinator part element reference */
					PartElementReference partElementReference1 = this.architectureInstanceBuilder
							.createPartElementReference(architectureStyleExtractor.getComponentByName(componentName1));
					/* Create coordinator inner port reference */
					InnerPortReference innerPortReference1 = this.architectureInstanceBuilder.createInnerPortReference(
							partElementReference1, architectureStyleExtractor.getPortByName(portName1));
					acp.add(innerPortReference1);

					/* Get operand port type and name */
					String fullPortName2 = operandPortTuple.getPortInstanceName();
					/* Get the mapping set */
					Set<String> mappingPorts = architectureOperands.getPortsMapping().get(fullPortName2);

					/* Iterate ports */
					for (String mappingPort : mappingPorts) {
						/* Get the port name */
						String portName = (mappingPort.split("\\."))[1];

						PortType portType;

						/* If the port type does not exist */
						if (!this.inspector
								.portTypeExists(architectureOperandsExtractor.getPortByName(portName).getType())) {
							portType = this.architectureInstanceBuilder
									.copyPortType(architectureOperandsExtractor.getPortByName(portName).getType());
						} else {
							portType = extractor.getPortTypeByName(
									architectureOperandsExtractor.getPortByName(portName).getType().getName());
						}

						/* Create Port Parameter */
						PortParameter portParameter = this.architectureInstanceBuilder.createPortParameter(portType,
								portName);
						/* Add to the list */
						portParameters.add(portParameter);

						/* Get the component name */
						String componentName = (mappingPort.split("\\."))[0];
						/* Create part element reference */
						PartElementReference partElementReference = this.architectureInstanceBuilder
								.createPartElementReference(
										architectureOperandsExtractor.getComponentByName(componentName));
						/* Create the inner port reference */
						InnerPortReference innerPortReference = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference,
										architectureOperandsExtractor.getPortByName(portName));
						acp.add(innerPortReference);
					}

					/* Create an empty list of port parameter reference */
					List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();

					/* Iterate port parameters */
					for (PortParameter pp : portParameters) {
						/*
						 * Create port parameter reference from the current port
						 * parameter
						 */
						PortParameterReference portParameterReference = this.architectureInstanceBuilder
								.createPortParameterReference(pp);

						/* Add to the list */
						portParameterReferences.add(portParameterReference);
					}

					/* Create a list of AC Expressions */
					List<ACExpression> expressions = new LinkedList<ACExpression>();
					expressions.addAll(portParameterReferences);

					/* Create the ACFusion */
					ACFusion acFusion = this.architectureInstanceBuilder.createACFusion(expressions);

					/* Create an empty list of interactions */
					List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

					ConnectorType connectorType;

					if (!this.inspector.connectorTypeExists(connecotrTypeName)) {
						connectorType = this.architectureInstanceBuilder.createConnectorType(connecotrTypeName,
								portParameters, acFusion, interactionSpecifications);
						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
								this.architectureInstance.getBipFileModel().getRootType(), acp);
					} else {
						connectorType = extractor.getConnectorTypeByName(connecotrTypeName);
						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
								this.architectureInstance.getBipFileModel().getRootType(), acp);
					}

				}
				/* Case 1:1 -- 1:1 */
				else if (coordinatorPortTuple.getCalculatedMultiplicity() == 1
						&& coordinatorPortTuple.getCalculatedDegree() == 1
						&& operandPortTuple.getCalculatedMultiplicity() == 1
						&& operandPortTuple.getCalculatedDegree() == 1) {

					if (!coordinatorCleared) {
						coordinatorCleared = true;

						/* Get the reference to the coordinator */
						Component coordinator = architectureStyleExtractor.getArchitectureStyleCoordinators().get(0);

						/* Get the type of the coordinator */
						AtomType coordinatorType = ((AtomType) coordinator.getType());

						/* Get the old behavior of the coordinator */
						PetriNet oldBehavior = ((PetriNet) coordinatorType.getBehavior());

						System.out.println(oldBehavior);

						/* Clear the transitions */
						// oldBehavior.getTransition().clear();

						/* Clear the ports of the coordinator type */
						coordinatorType.getPort().clear();
					}

					/* Get the coordinator port name and type */
					String portName1 = (coordinatorPortTuple.getPortInstanceName().split("\\."))[1];
					/* This port type exist in the style and in the instance */
					PortType portType1;

					/* If the coordinator port type does not exist */
					if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName1).getType())) {
						portType1 = architectureStyleExtractor.getPortByName(portName1).getType();
					}
					/* If the coordinator port type exist */
					else {
						portType1 = extractor.getPortTypeByName(
								architectureStyleExtractor.getPortByName(portName1).getType().getName());
					}

					/* Get operand port type and name */
					String portName2 = (operandPortTuple.getPortInstanceName().split("\\."))[1];
					PortType portType2;

					/* If the operand port type does not exist */
					if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName2).getType())) {
						portType2 = architectureStyleExtractor.getPortByName(portName2).getType();
					}
					/* If the operand port type exist */
					else {
						portType2 = extractor.getPortTypeByName(
								architectureStyleExtractor.getPortByName(portName2).getType().getName());
					}

					/* Create Port Parameters */
					PortParameter portParameter1 = this.architectureInstanceBuilder.createPortParameter(portType1,
							portName1);
					PortParameter portParameter2 = this.architectureInstanceBuilder.createPortParameter(portType2,
							portName2);

					/* Create the list of port parameters */
					List<PortParameter> portParameters = new LinkedList<PortParameter>();
					portParameters.add(portParameter1);
					portParameters.add(portParameter2);

					/* Create the port parameter references */
					PortParameterReference portParameterReference1 = this.architectureInstanceBuilder
							.createPortParameterReference(portParameter1);
					PortParameterReference portParameterReference2 = this.architectureInstanceBuilder
							.createPortParameterReference(portParameter2);

					/* Create list of port parameter references */
					List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
					portParameterReferences.add(portParameterReference1);
					portParameterReferences.add(portParameterReference2);

					/* Create a list of AC Expressions */
					List<ACExpression> expressions = new LinkedList<ACExpression>();
					expressions.addAll(portParameterReferences);

					/* Create the ACFusion */
					ACFusion acFusion = this.architectureInstanceBuilder.createACFusion(expressions);

					List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

					ConnectorType connectorType = this.architectureInstanceBuilder.createConnectorType(
							connecotrTypeName, portParameters, acFusion, interactionSpecifications);

					Set<String> portsMapping = architectureOperands.getPortsMapping()
							.get(operandPortTuple.getPortInstanceName());

					int i = 0;

					for (String portMapping : portsMapping) {
						/* Instantiate empty list of port parameters */
						List<ActualPortParameter> acp = new LinkedList<ActualPortParameter>();

						PartElementReference partElementReference1 = this.architectureInstanceBuilder
								.createPartElementReference(
										architectureStyleExtractor.getArchitectureStyleCoordinators().get(0));
						InnerPortReference innerPortReference1 = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference1,
										architectureStyleExtractor.getPortByName(portName1));

						PartElementReference partElementReference2 = this.architectureInstanceBuilder
								.createPartElementReference(architectureOperandsExtractor
										.getComponentByName((portMapping.split("\\."))[0]));

						InnerPortReference innerPortReference2 = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference2,
										architectureOperandsExtractor.getPortByName((portMapping.split("\\."))[1]));

						acp.add(innerPortReference1);
						acp.add(innerPortReference2);

						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName() + "_" + (i + 1),
								connectorType, this.architectureInstance.getBipFileModel().getRootType(), acp);
						i++;
					}

				}

			}
			/* Singleton operand */
			else if (ct.getCoordinatorPortTuples().size() == 0 && ct.getOperandPortTuples().size() != 0) {
				/* Get the operand port tuple */
				operandPortTuple = ct.getOperandPortTuples().get(0);

				if (operandPortTuple.getCalculatedMultiplicity() > 1 && operandPortTuple.getCalculatedDegree() == 1) {
					/* List of port parameters, for the connector type */
					List<PortParameter> portParameters = new LinkedList<PortParameter>();

					/*
					 * List of Actual Port Parameters, for the connector
					 * instance
					 */
					List<ActualPortParameter> acp = new LinkedList<ActualPortParameter>();

					/* Get operand port type and name */
					String fullPortName2 = operandPortTuple.getPortInstanceName();
					/* Get the mapping set */
					Set<String> mappingPorts = architectureOperands.getPortsMapping().get(fullPortName2);

					/* Iterate ports */
					for (String mappingPort : mappingPorts) {
						/* Get the port name */
						String portName = (mappingPort.split("\\."))[1];

						PortType portType;

						/* If the port type does not exist */
						if (!this.inspector
								.portTypeExists(architectureOperandsExtractor.getPortByName(portName).getType())) {
							portType = this.architectureInstanceBuilder
									.copyPortType(architectureOperandsExtractor.getPortByName(portName).getType());
						} else {
							portType = extractor.getPortTypeByName(
									architectureOperandsExtractor.getPortByName(portName).getType().getName());
						}

						/* Create Port Parameter */
						PortParameter portParameter = this.architectureInstanceBuilder.createPortParameter(portType,
								portName);
						/* Add to the list */
						portParameters.add(portParameter);

						/* Get the component name */
						String componentName = (mappingPort.split("\\."))[0];
						/* Create part element reference */
						PartElementReference partElementReference = this.architectureInstanceBuilder
								.createPartElementReference(
										architectureOperandsExtractor.getComponentByName(componentName));
						/* Create the inner port reference */
						InnerPortReference innerPortReference = this.architectureInstanceBuilder
								.createInnerPortReference(partElementReference,
										architectureOperandsExtractor.getPortByName(portName));
						acp.add(innerPortReference);
					}

					/* Create an empty list of port parameter reference */
					List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();

					/* Iterate port parameters */
					for (PortParameter pp : portParameters) {
						/*
						 * Create port parameter reference from the current port
						 * parameter
						 */
						PortParameterReference portParameterReference = this.architectureInstanceBuilder
								.createPortParameterReference(pp);

						/* Add to the list */
						portParameterReferences.add(portParameterReference);
					}

					/* Create a list of AC Expressions */
					List<ACExpression> expressions = new LinkedList<ACExpression>();
					expressions.addAll(portParameterReferences);

					/* Create the ACFusion */
					ACFusion acFusion = this.architectureInstanceBuilder.createACFusion(expressions);

					List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

					ConnectorType connectorType;

					if (!this.inspector.connectorTypeExists(connecotrTypeName)) {
						connectorType = this.architectureInstanceBuilder.createConnectorType(connecotrTypeName,
								portParameters, acFusion, interactionSpecifications);
						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
								this.architectureInstance.getBipFileModel().getRootType(), acp);
					} else {
						connectorType = extractor.getConnectorTypeByName(connecotrTypeName);
						this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
								this.architectureInstance.getBipFileModel().getRootType(), acp);
					}
				}

			}
			/* Singleton coordinator */
			else if (ct.getCoordinatorPortTuples().size() != 0 && ct.getOperandPortTuples().size() == 0) {
				/* Get the coordinator port tuple */
				coordinatorPortTuple = ct.getCoordinatorPortTuples().get(0);

				/* List of port parameters, for the connector type */
				List<PortParameter> portParameters = new LinkedList<PortParameter>();

				/*
				 * List of Actual Port Parameters, for the connector instance
				 */
				List<ActualPortParameter> acp = new LinkedList<ActualPortParameter>();

				/* Port parameter reference list */
				List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();

				/* Get the name of the coordinator port */
				String portName = (coordinatorPortTuple.getPortInstanceName().split("\\."))[1];

				PortType portType;

				/* If the port type does not exist */
				if (!this.inspector.portTypeExists(architectureStyleExtractor.getPortByName(portName).getType())) {
					portType = this.architectureInstanceBuilder
							.copyPortType(architectureStyleExtractor.getPortByName(portName).getType());
				} else {
					portType = extractor
							.getPortTypeByName(architectureStyleExtractor.getPortByName(portName).getType().getName());
				}

				/* Create Port Parameters */
				PortParameter portParameter1 = this.architectureInstanceBuilder.createPortParameter(portType, portName);
				portParameters.add(portParameter1);

				/* Create Port Parameter reference */
				PortParameterReference portParameterReference = this.architectureInstanceBuilder
						.createPortParameterReference(portParameter1);
				portParameterReferences.add(portParameterReference);

				/* Create a list of AC Expressions */
				List<ACExpression> expressions = new LinkedList<ACExpression>();
				expressions.addAll(portParameterReferences);
				/* Create the ACFusion */
				ACFusion acFusion = this.architectureInstanceBuilder.createACFusion(expressions);

				List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

				/* Get the component name */
				String componentName = (coordinatorPortTuple.getPortInstanceName().split("\\."))[0];
				/* Create part element reference */
				PartElementReference partElementReference = this.architectureInstanceBuilder
						.createPartElementReference(architectureStyleExtractor.getComponentByName(componentName));
				/* Create the inner port reference */
				InnerPortReference innerPortReference = this.architectureInstanceBuilder.createInnerPortReference(
						partElementReference, architectureStyleExtractor.getPortByName(portName));
				acp.add(innerPortReference);

				ConnectorType connectorType;

				if (!this.inspector.connectorTypeExists(connecotrTypeName)) {
					connectorType = this.architectureInstanceBuilder.createConnectorType(connecotrTypeName,
							portParameters, acFusion, interactionSpecifications);
					this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
							this.architectureInstance.getBipFileModel().getRootType(), acp);
				} else {
					connectorType = extractor.getConnectorTypeByName(connecotrTypeName);
					this.architectureInstanceBuilder.createConnector(ct.getConnectorInstanceName(), connectorType,
							this.architectureInstance.getBipFileModel().getRootType(), acp);
				}

			}
		}

		this.instantiateArchitecture("/home/vladimir/example.bip", "/home/vladimir/exampleConf.txt");
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * Constructor for this class
	 * 
	 * @param architectureInstance
	 *            - the empty Architecture Instance
	 * @param architectureStyle
	 *            - the Architecture Style for the instance
	 * @param architectureOperands
	 *            - the operands for the instance
	 * @throws ArchitectureBuilderException
	 * @throws IOException
	 * @throws ArchitectureExtractorException
	 */
	public ArchitectureInstantiator(ArchitectureInstance architectureInstance, ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands)
			throws ArchitectureBuilderException, IOException, ArchitectureExtractorException {

		/* Assign the architecture instance, style and operands */
		this.architectureInstance = architectureInstance;
		this.architectureStyle = architectureStyle;
		this.architectureOperands = architectureOperands;

		/* Instantiate architecture instance builder */
		this.architectureInstanceBuilder = new ArchitectureInstanceBuilder(this.architectureInstance);

		/* Instantiate the inspector of the instance */
		this.inspector = new BIPExtractor(this.architectureInstance.getBipFileModel());

		/* Instantiate the extractor of the instance */
		this.extractor = new BIPExtractor(this.architectureInstance.getBipFileModel());

		/* 1. Calculate the Parameters */
		this.calculateParameters();

		/* 2. Create the Architecture Instance */
		this.createInstance();
	}

	public ArchitectureInstance getArchitectureInstance() {
		return architectureInstance;
	}

	public ArchitectureStyle getArchitectureStyle() {
		return architectureStyle;
	}

	public ArchitectureOperands getArchitectureOperands() {
		return architectureOperands;
	}

	/* Testing the methods */
	public static void main(String[] args) {

		try {

			/* passed for this instance */
			ArchitectureInstance architectureInstance1 = new ArchitectureInstance("MutualExclusion", "Mutex", "mutex");
			ArchitectureInstance architectureInstance2 = new ArchitectureInstance("ModeControl2", "ModeControl",
					"modeControl2");
			ArchitectureInstance architectureInstance3 = new ArchitectureInstance("ActionSequence12", "ActionSequence",
					"actionSequence");

			String archStylePath1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
			String archStylePath2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf.txt";
			String archStylePath3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf.txt";

			ArchitectureStyle architectureStyle1 = new ArchitectureStyle(archStylePath1);
			ArchitectureStyle architectureStyle2 = new ArchitectureStyle(archStylePath2);
			ArchitectureStyle architectureStyle3 = new ArchitectureStyle(archStylePath3);

			String archOperandsPath1 = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
			String archOperandsPath2 = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf-instance2.txt";
			String archOperandsPath3 = "/home/vladimir/Architecture_examples/Archive/ActionSequence/AEConf-instance12.txt";

			ArchitectureOperands architectureOperands1 = new ArchitectureOperands(archOperandsPath1);
			ArchitectureOperands architectureOperands2 = new ArchitectureOperands(archOperandsPath2);
			ArchitectureOperands architectureOperands3 = new ArchitectureOperands(archOperandsPath3);

			ArchitectureInstantiator builder = new ArchitectureInstantiator(architectureInstance1, architectureStyle1,
					architectureOperands1);

			ArchitectureStyle resultArchitectureStyle = builder.getArchitectureStyle();

			List<ConnectorTuple> connectorTuples = resultArchitectureStyle.getConnectors();

			for (ConnectorTuple connectorTuple : connectorTuples) {
				List<PortTuple> portTuples = connectorTuple.getPortTuples();

				for (PortTuple portTuple : portTuples) {
					if (portTuple.isMultiplicityCalculated()) {
						System.out.println(
								"The multiplicity end for the port instance name " + portTuple.getPortInstanceName()
										+ " is calculated, and the value is " + portTuple.getCalculatedMultiplicity());
					} else {
						System.out.println("The multiplicity end for the port instance name "
								+ portTuple.getPortInstanceName() + " is not calculated");
					}

					if (portTuple.isDegreeCalculated()) {
						System.out
								.println("The degree end for the port instance name " + portTuple.getPortInstanceName()
										+ " is calculated, and the value is " + portTuple.getCalculatedDegree());
					} else {
						System.out.println("The degree end for the port instance name "
								+ portTuple.getPortInstanceName() + " is not calculated");
					}
				}
			}

		} catch (FileNotFoundException | ConfigurationFileException | ArchitectureExtractorException e) {
			e.printStackTrace();
		} catch (ArchitectureBuilderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

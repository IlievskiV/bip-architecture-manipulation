package ch.epfl.risd.archman.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.constants.ConstantFields;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
import ch.epfl.risd.archman.exceptions.PortNotFoundException;
import ch.epfl.risd.archman.exceptions.TestConfigurationFileException;
import ch.epfl.risd.archman.extractor.ArchitectureOperandsExtractor;
import ch.epfl.risd.archman.extractor.ArchitectureStyleExtractor;
import ch.epfl.risd.archman.extractor.BIPExtractor;
import ch.epfl.risd.archman.model.ArchitectureInstance;
import ch.epfl.risd.archman.model.ArchitectureOperands;
import ch.epfl.risd.archman.model.ArchitectureStyle;
import ch.epfl.risd.archman.model.BIPFileModel;
import ch.epfl.risd.archman.model.ConnectorTuple;
import ch.epfl.risd.archman.model.PortTuple;
import ch.epfl.risd.archman.model.PortTuple.PortTupleType;
import ujf.verimag.bip.Core.Behaviors.AtomType;
import ujf.verimag.bip.Core.Behaviors.PetriNet;
import ujf.verimag.bip.Core.Behaviors.Port;
import ujf.verimag.bip.Core.Behaviors.PortDefinitionReference;
import ujf.verimag.bip.Core.Behaviors.PortType;
import ujf.verimag.bip.Core.Behaviors.Transition;
import ujf.verimag.bip.Core.Interactions.ActualPortParameter;
import ujf.verimag.bip.Core.Interactions.Component;
import ujf.verimag.bip.Core.Interactions.CompoundType;
import ujf.verimag.bip.Core.Interactions.ConnectorType;
import ujf.verimag.bip.Core.Interactions.InteractionSpecification;
import ujf.verimag.bip.Core.Interactions.PortParameter;
import ujf.verimag.bip.Core.Interactions.PortParameterReference;
import ujf.verimag.bip.Core.PortExpressions.ACExpression;
import ujf.verimag.bip.Core.PortExpressions.ACFusion;

public class ArchitectureInstantiator {

	/****************************************************************************/
	/* PRIVATE(UTILITY) METHODS */
	/****************************************************************************/

	/**
	 * This method calculates the parameters in every connector tuple, i.e. it
	 * calculates the multiplicity and the degree
	 * 
	 * @throws ArchitectureBuilderException
	 */
	private static void calculateParameters(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands) throws ArchitectureBuilderException {

		/* Get the list of all Connector Tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();

		/* Iterate the connector tuples */
		for (ConnectorTuple connectorTuple : connectorTuples) {
			/* Get the port tuples */
			List<PortTuple> portTuples = connectorTuple.getPortTuples();
			/* Iterate the port tuples */
			for (PortTuple portTuple : portTuples) {

				/* If the port tuple is coordinator tuple */
				if (portTuple.getType() == PortTupleType.COORDINATOR_TUPLE) {
					/* If the degree is not calculated */
					if (!portTuple.isDegreeCalculated()) {
						/* Calculate the degree */
						calculateDegreeCoordinator(architectureOperands, connectorTuple, portTuple);
					}
				}

				/* If the port tuple is operand tuple */
				if (portTuple.getType() == PortTupleType.OPERAND_TUPLE) {
					/* If the multiplicity is not calculated */
					if (!portTuple.isMultiplicityCalculated()) {
						/* Calculate the multiplicity */
						calculateMultiplicityOperand(architectureOperands, portTuple);
					}
				}
			}
		}
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
	protected static void calculateDegreeCoordinator(ArchitectureOperands architectureOperands,
			ConnectorTuple connectorTuple, PortTuple portTuple) {
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
			int numMappingOperands = architectureOperands.getOperandsMapping().get(operandInstanceName).size();

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
	protected static void calculateMultiplicityOperand(ArchitectureOperands architectureOperands, PortTuple portTuple) {
		/* Get the name of the port instance */
		String portInstanceName = portTuple.getPortInstanceName();
		/* Find to how many ports it is mapping */
		int multiplicity = architectureOperands.getPortsMapping().get(portInstanceName).size();
		/* Set the multiplicity */
		portTuple.setCalculatedMultiplicity(multiplicity);
		portTuple.setMultiplicityCalculated(true);
	}

	/**
	 * Helper method to create one connector type with multiple input port
	 * parameters, but only one instance
	 * 
	 * @param connectorTuple
	 * @param architectureInstance
	 * @throws ArchitectureExtractorException
	 * @throws IllegalPortParameterReferenceException
	 * @throws InvalidPortParameterNameException
	 * @throws InvalidConnectorTypeNameException
	 */
	protected static void degCoord1MultOpN(ConnectorTuple connectorTuple, ArchitectureInstance architectureInstance,
			ArchitectureOperands architectureOperands, ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {

		/* 1.Get coordinator port tuple */
		PortTuple coordinatorPortTuple = connectorTuple.getCoordinatorPortTuples().get(0);
		/* 2.Get operand port tuples */
		PortTuple operandPortTuple = connectorTuple.getOperandPortTuples().get(0);

		/* 3.Create an empty list of actual port parameters */
		List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

		/* 4. Create empty list of port parameters */
		List<PortParameter> portParameters = new LinkedList<PortParameter>();

		/* 4.1. Create coordinator port parameter */
		String coordinatorPortInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[1];
		String coordinatorPortTypeName = BIPExtractor
				.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName).getType().getName();

		portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
				BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), coordinatorPortTypeName),
				coordinatorPortInstanceName));

		/* 4.2. Create an actual port reference */
		String coordinatorInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[0];
		actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
				ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
						.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName)),
				BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName)));

		/* 5. Get the set of mapping ports */
		Set<String> operandMappingPorts = architectureOperands.getPortsMapping()
				.get((String) operandPortTuple.getPortInstanceName());

		/*
		 * 6. For every mapping port create port parameter and actual port
		 * parameter
		 */
		for (String operandPort : operandMappingPorts) {
			/* 6.1. Create operand port parameter */
			String operandPortInstanceName = operandPort.split("\\.")[1];
			String operandPortTypeName = BIPExtractor
					.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName).getType().getName();

			portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
					BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), operandPortTypeName),
					operandPortInstanceName));

			/* 6.2. Create operand actual port parameter */
			String operandInstanceName = operandPort.split("\\.")[0];
			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), operandInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName)));
		}

		/* 7.Create a list of port parameter references */
		List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
		for (PortParameter pp : portParameters) {

			PortParameterReference portParameterReference = ArchitectureInstanceBuilder
					.createPortParameterReference(pp);
			portParameterReferences.add(portParameterReference);
		}

		/* 8. Create a list of AC Expressions */
		List<ACExpression> expressions = new LinkedList<ACExpression>();
		expressions.addAll(portParameterReferences);

		/* 9.Create the ACFusion */
		ACFusion acFusion = ArchitectureInstanceBuilder.createACFusion(expressions);

		/* 10. Create an empty list of interactions */
		List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

		/* 11. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();

		ConnectorType connectorType;

		/*
		 * 11.1 Create the connector type if not exists and one instance of it
		 */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.createConnectorType(architectureInstance, connectorTypeName,
					portParameters, acFusion, interactionSpecifications);

			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName(), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
		}
		/* 11.2 If connector type exists create only one instance */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName(), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
		}
	}

	protected static void degCoordNMultOp1(ConnectorTuple connectorTuple, ArchitectureInstance architectureInstance,
			ArchitectureOperands architectureOperands, ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {
		/* 1.Get coordinator port tuple */
		PortTuple coordinatorPortTuple = connectorTuple.getCoordinatorPortTuples().get(0);
		/* 2.Get operand port tuple */
		PortTuple operandPortTuple = connectorTuple.getOperandPortTuples().get(0);

		/* 3. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();
		/* 4. Get the connector type */
		ConnectorType connectorType;

		/* 4.1. Create if not exists */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.copyConnectorType(architectureInstance, BIPExtractor
					.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
					.getType());
		}
		/* 4.2. Extract it, if it exists */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
		}

		/* 5. Get the mapped ports */
		Set<String> operandMappingPorts = architectureOperands.getPortsMapping()
				.get((String) operandPortTuple.getPortInstanceName());
		int i = 0;

		/* 5.1. Iterate ever the mapped ports */
		for (String operandPort : operandMappingPorts) {
			/* 5.1.1.Create an empty list of actual port parameters */
			List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

			/* 5.1.2. Actual port parameter for the coordinator side */
			String coordinatorInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[0];
			String coordinatorPortInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[1];

			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName)));

			/* 5.1.3. Actual port parameter for the operand side */
			String operandInstanceName = operandPort.split("\\.")[0];
			String operandPortInstanceName = operandPort.split("\\.")[1];
			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), operandInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName)));

			/* 5.1.4. Create connector instance */
			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName() + "_" + (i + 1), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
			i++;
		}

	}

	protected static void degCoord1MultOp1(ConnectorTuple connectorTuple, ArchitectureInstance architectureInstance,
			ArchitectureOperands architectureOperands, ArchitectureStyle architectureStyle)
			throws PortNotFoundException, ArchitectureExtractorException, InvalidConnectorTypeNameException,
			InvalidPortParameterNameException, IllegalPortParameterReferenceException {

		/* 1.Get coordinator port tuple */
		PortTuple coordinatorPortTuple = connectorTuple.getCoordinatorPortTuples().get(0);
		/* 2.Get operand port tuple */
		PortTuple operandPortTuple = connectorTuple.getOperandPortTuples().get(0);

		/* 3. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();

		/* 4. Get the connector type */
		ConnectorType connectorType;

		/* 4.1. Create if not exists */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.copyConnectorType(architectureInstance, BIPExtractor
					.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
					.getType());
		}
		/* 4.2. Extract it, if it exists */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
		}

		/* 5.Delete the coordinator port instance */
		String coordinatorInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[0];

		String coordinatorPortInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[1];
		Port deletedPort = ArchitectureInstanceBuilder.deletePortInstance(BIPExtractor
				.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName).getType(),
				coordinatorPortInstanceName);
		/* Remove the port from the configuration file */
		architectureInstance.removePort(coordinatorInstanceName + "." + deletedPort.getName());

		/* 6. Delete transitions labeled by the deleted port */
		List<Transition> deletedTransitions = ArchitectureInstanceBuilder.deleteTransitions(
				(AtomType) BIPExtractor
						.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName).getType(),
				deletedPort);

		/* 7. Get the mapped ports */
		Set<String> operandMappingPorts = architectureOperands.getPortsMapping()
				.get((String) operandPortTuple.getPortInstanceName());
		int i = 0;

		/* 12.1. Iterate ever the mapped ports */
		for (String operandPort : operandMappingPorts) {
			/* 12.1.1.Create an empty list of actual port parameters */
			List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

			/* 12.1.2. Create port instance in the coordinator */
			Port newPort = ArchitectureInstanceBuilder.createPortInstance(operandPort.split("\\.")[1],
					operandPort.split("\\.")[1], deletedPort.getType());

			/* Get the coordinator type */
			AtomType coordinatorType = (AtomType) BIPExtractor
					.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName).getType();
			/* 12.1.3. Add the new port in the coordinator */
			coordinatorType.getPort().add(newPort);
			/* Add the new port in the configuration file too */
			architectureInstance.addPort(coordinatorInstanceName + "." + newPort.getName());

			/* Recreate transitions */
			List<Transition> newTranstitions = new LinkedList<Transition>();

			for (Transition t : deletedTransitions) {
				PortDefinitionReference portDefinitionReference = ArchitectureInstanceBuilder
						.createPortDefinitionReference(
								ArchitectureInstanceBuilder.createPortDefinition(newPort.getName(), newPort.getType()));
				newTranstitions.add(ArchitectureInstanceBuilder.createTransition(portDefinitionReference,
						t.getOrigin().get(0), t.getDestination().get(0), t.getGuard(), t.getAction()));
			}

			/* Get the coordinator Petri Net and change it */
			PetriNet petriNet = (PetriNet) coordinatorType.getBehavior();
			petriNet.getTransition().addAll(newTranstitions);
			coordinatorType.setBehavior(petriNet);

			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName)),
					newPort));

			/* 10.1.3. Actual port parameter for the operand side */
			String operandInstanceName = operandPort.split("\\.")[0];
			String operandPortInstanceName = operandPort.split("\\.")[1];
			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), operandInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName)));

			/* 10.1.4. Create connector instance */
			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName() + "_" + (i + 1), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
			i++;
		}
	}

	protected static void singletonOpMultNDeg1(ConnectorTuple connectorTuple, ArchitectureInstance architectureInstance,
			ArchitectureOperands architectureOperands, ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {

		/* 1.Get operand port tuple */
		PortTuple operandPortTuple = connectorTuple.getOperandPortTuples().get(0);
		/* 2.Create an empty list of actual port parameters */
		List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();
		/* 3. Create an empty list of port parameters */
		List<PortParameter> portParameters = new LinkedList<PortParameter>();

		/* 4. Get the set of mapping ports */
		Set<String> operandMappingPorts = architectureOperands.getPortsMapping()
				.get((String) operandPortTuple.getPortInstanceName());

		/* 5. Iterate ever the mapped ports */
		for (String operandPort : operandMappingPorts) {
			/* 5.1. Create operand port parameter */
			String operandPortInstanceName = operandPort.split("\\.")[1];
			String operandPortTypeName = BIPExtractor
					.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName).getType().getName();

			portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
					BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), operandPortTypeName),
					operandPortInstanceName));

			/* 5.2. Create operand actual port parameter */
			String operandInstanceName = operandPort.split("\\.")[0];
			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), operandInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName)));
		}

		/* 6.Create a list of port parameter references */
		List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
		for (PortParameter pp : portParameters) {

			PortParameterReference portParameterReference = ArchitectureInstanceBuilder
					.createPortParameterReference(pp);
			portParameterReferences.add(portParameterReference);
		}

		/* 7. Create a list of AC Expressions */
		List<ACExpression> expressions = new LinkedList<ACExpression>();
		expressions.addAll(portParameterReferences);

		/* 8.Create the ACFusion */
		ACFusion acFusion = ArchitectureInstanceBuilder.createACFusion(expressions);

		/* 9. Create an empty list of interactions */
		List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

		/* 10. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();

		ConnectorType connectorType;

		/*
		 * 10.1 Create the connector type if not exists and one instance of it
		 */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.createConnectorType(architectureInstance, connectorTypeName,
					portParameters, acFusion, interactionSpecifications);

			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName(), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
		}
		/* 10.2 If connector type exists create only one instance */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
			ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
					connectorTuple.getConnectorInstanceName(), connectorType,
					architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
		}
	}

	protected static void singletonCoordMult1Deg1(ConnectorTuple connectorTuple,
			ArchitectureInstance architectureInstance, ArchitectureOperands architectureOperands,
			ArchitectureStyle architectureStyle)
			throws ArchitectureExtractorException, InvalidConnectorTypeNameException, InvalidPortParameterNameException,
			IllegalPortParameterReferenceException {
		/* 1.Get coordinator port tuple */
		PortTuple coordinatorPortTuple = connectorTuple.getCoordinatorPortTuples().get(0);

		/* 8. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();

		ConnectorType connectorType;

		/*
		 * 8.1 Create the connector type if not exists and one instance of it
		 */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.copyConnectorType(architectureInstance, BIPExtractor
					.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
					.getType());
		}
		/* 8.2 If connector type exists create only one instance */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
		}

		/* 2.Create an empty list of actual port parameters */
		List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

		/* 3.2. Actual port parameter for the coordinator side */
		String coordinatorInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[0];
		String coordinatorPortInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[1];
		actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
				ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
						.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName)),
				BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName)));

		/* Create instance */
		ArchitectureInstanceBuilder.createConnectorInstance(architectureInstance,
				connectorTuple.getConnectorInstanceName(), connectorType,
				architectureInstance.getBipFileModel().getRootType(), actualPortParameters);
	}

	/****************************************************************************/
	/* PUBLIC METHODS */
	/***************************************************************************/

	/**
	 * 
	 * @param architectureStyle
	 * @param architectureOperands
	 * @param architectureInstanceBIPFile
	 * @param pathToSaveBIPFile
	 * @param pathToSaveConfFile
	 * @return
	 * @throws ArchitectureBuilderException
	 * @throws ArchitectureExtractorException
	 * @throws IOException
	 */
	public static ArchitectureInstance createArchitectureInstance(ArchitectureStyle architectureStyle,
			ArchitectureOperands architectureOperands, BIPFileModel architectureInstanceBIPFile,
			String pathToSaveBIPFile, String pathToSaveConfFile)
			throws ArchitectureBuilderException, ArchitectureExtractorException, IOException {

		/* 0.Create an empty architecture instance */
		ArchitectureInstance instance = new ArchitectureInstance(architectureInstanceBIPFile);

		/* 1.Calculate the degree of the coordinator side */
		/* 2.Calculate the multiplicity of the operand side */
		ArchitectureInstantiator.calculateParameters(architectureStyle, architectureOperands);

		/* 3.Take all Port Types and plug them */
		List<PortType> allPortTypes = new LinkedList<PortType>();
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureStyle.getBipFileModel()));
		allPortTypes.addAll(BIPExtractor.getAllPortTypes(architectureOperands.getBipFileModel()));
		ArchitectureInstanceBuilder.copyAllPortTypes(instance, allPortTypes);

		/* 4.Take all coordinators and plug them */
		List<Component> coordinators = ArchitectureStyleExtractor.getArchitectureStyleCoordinators(architectureStyle);
		for (Component c : coordinators) {
			if (c.getType() instanceof AtomType) {
				AtomType atomType = ArchitectureInstanceBuilder.copyAtomicType(instance, (AtomType) c.getType());
				/* 4.1. Make an atomic type instance of the coordinator */
				ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), atomType,
						instance.getBipFileModel().getRootType(), true);
			} else {
				CompoundType compoundType = ArchitectureInstanceBuilder.copyCompoundType(instance,
						(CompoundType) c.getType());
				/* 4.1. Make a compound type instance of the coordinator */
				ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), compoundType,
						instance.getBipFileModel().getRootType(), true);
			}
		}

		/* 5.Take all operands and plug them */
		List<Component> operands = ArchitectureOperandsExtractor.getArchitectureOperands(architectureOperands);
		for (Component c : operands) {
			if (c.getType() instanceof AtomType) {
				AtomType atomType = ArchitectureInstanceBuilder.copyAtomicType(instance, (AtomType) c.getType());
				/* 5.1. Make an atomic type instance of the operand */
				ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), atomType,
						instance.getBipFileModel().getRootType(), false);
			} else {
				/* 5.1. Make a compound type instance of the operand */
				CompoundType compoundType = ArchitectureInstanceBuilder.copyCompoundType(instance,
						(CompoundType) c.getType());
				/* 4.1. Make a compound type instance of the coordinator */
				ArchitectureInstanceBuilder.addComponentInstance(instance, c.getName(), compoundType,
						instance.getBipFileModel().getRootType(), false);
			}
		}

		/* 7.Iterate over the connector tuples */
		List<ConnectorTuple> connectorTuples = architectureStyle.getConnectorsTuples();
		for (ConnectorTuple connectorTuple : connectorTuples) {
			/* 7.1.If not singleton */
			if (connectorTuple.getPortTuples().size() > 1) {
				/*
				 * 7.1.1.Case: degree coordinator = 1, multiplicity operand = N
				 */
				if (connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree() == 1
						&& connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity() > 1) {
					ArchitectureInstantiator.degCoord1MultOpN(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
				/*
				 * 7.1.2.Case: degree coordinator = N, multiplicity operand = 1
				 */
				else if (connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree() > 1
						&& connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity() == 1) {
					ArchitectureInstantiator.degCoordNMultOp1(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
				/*
				 * 7.1.3.Case: degree coordinator = 1, multiplicity operand = 1
				 */
				else if (connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree() == 1
						&& connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity() == 1) {
					ArchitectureInstantiator.degCoord1MultOp1(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
			}
			/* 7.2.If singleton */
			else {
				/* 7.3. If singleton operand end */
				if (connectorTuple.getCoordinatorPortTuples().size() == 0
						&& connectorTuple.getOperandPortTuples().size() > 0) {
					ArchitectureInstantiator.singletonOpMultNDeg1(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
				/* 7.4. If singleton coordinator end */
				else {
					ArchitectureInstantiator.singletonCoordMult1Deg1(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
			}
		}

		/* End. Generate BIP file */
		instance.generateBipFile(pathToSaveBIPFile);
		/* End. Generate configuration file */
		instance.generateConfigurationFile(pathToSaveConfFile);

		return instance;
	}

	/**
	 * Function for instantiation of architecture, when the test configuration
	 * file is given. Should change the name of the function probably
	 * 
	 * @param pathToTestConfFile
	 *            - absolute path to the configuration file
	 * @throws TestConfigurationFileException
	 * @throws ArchitectureExtractorException
	 * @throws ConfigurationFileException
	 * @throws IOException
	 * @throws ArchitectureBuilderException
	 */
	public static ArchitectureInstance instantiateArchitecture(String pathToTestConfFile)
			throws TestConfigurationFileException, ConfigurationFileException, ArchitectureExtractorException,
			ArchitectureBuilderException, IOException {

		/*
		 * Set to null the three required entities to instantiate architecture
		 */
		ArchitectureStyle architectureStyle = null;
		ArchitectureOperands architectureOperands = null;
		String outputFolderPath = null;

		/* Existence of the ARCH_STYLE_CONF_FILE_PATH parameter */
		boolean hasArchStyleConfFilePath = false;

		/* Existence of the ARCH_OP_CONF_FILE_PATH parameter */
		boolean hasArchOpConfFilePath = false;

		/* Existence of the OUTPUT_FOLDER_PATH parameter */
		boolean hasOutputFolderPath = false;

		/* Set scanner to null */
		Scanner scanner = null;

		try {

			/* Initialize the scanner */
			scanner = new Scanner(new File(pathToTestConfFile));

			/* Reading and parsing the configuration file */
			while (scanner.hasNext()) {
				/* Take the current line and split it where the semicolon is */
				String[] tokens = scanner.nextLine().split(":");

				/* No more than one colon in a line exception */
				if (tokens.length > 2) {
					throw new TestConfigurationFileException("More than one colon (:) in the line");
				}

				/* Check for ARCH_STYLE_CONF_FILE_PATH parameter */
				if (tokens[0].equals(ConstantFields.ARCH_STYLE_CONF_FILE_PATH_PARAM)) {
					hasArchStyleConfFilePath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the ARCH_STYLE_CONF_FILE_PATH parameter is missing");
					} else {
						/* Instantiate the architecture style */
						architectureStyle = new ArchitectureStyle(new File(tokens[1]).getAbsolutePath());
					}
				}

				/* Check for ARCH_OP_CONF_FILE_PATH_PARAM parameter */
				if (tokens[0].equals(ConstantFields.ARCH_OP_CONF_FILE_PATH_PARAM)) {
					hasArchOpConfFilePath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the ARCH_OP_CONF_FILE_PATH parameter is missing");
					} else {
						/* Instantiate the architecture operands */
						architectureOperands = new ArchitectureOperands(new File(tokens[1]).getAbsolutePath());
					}
				}

				/* Check for OUTPUT_FOLDER_PATH_PARAM parameter */
				if (tokens[0].equals(ConstantFields.OUTPUT_FOLDER_PATH_PARAM)) {
					hasOutputFolderPath = true;

					/* Check if value is missing */
					if (tokens[1].trim().equals("")) {
						throw new TestConfigurationFileException(
								"The value of the OUTPUT_FOLDER_PATH parameter is missing");
					} else {
						/* Instantiate the output folder path */
						outputFolderPath = new File(tokens[1]).getAbsolutePath();
					}
				}
			}

		} finally {
			/* Close the scanner */
			if (scanner != null)
				scanner.close();
		}

		/* If there is not some of the mandatory parameters */
		if (!hasArchStyleConfFilePath) {
			throw new TestConfigurationFileException("ARCH_STYLE_CONF_FILE_PATH parameter is missing");
		}

		if (!hasArchOpConfFilePath) {
			throw new TestConfigurationFileException("ARCH_OP_CONF_FILE_PATH parameter is missing");
		}

		if (!hasOutputFolderPath) {
			throw new TestConfigurationFileException("OUTPUT_FOLDER_PATH is missing");
		}

		/* The name of the module */
		String systemName = architectureStyle.getBipFileModel().getSystem().getName();
		/* The name of the root type in the module */
		String rootTypeName = architectureStyle.getBipFileModel().getRootType().getName();
		/* The name of the root type instance in the module */
		String rootInstanceName = architectureStyle.getBipFileModel().getRoot().getName();

		/* Create the BIP File Model for the instance */
		BIPFileModel bipFileModel = new BIPFileModel(systemName, rootTypeName, rootInstanceName);

		/* Create the output folder if not exists */
		File outputFolder = new File(outputFolderPath);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		/* Create the path to the resulting BIP file */
		String pathToSaveBIPFile = outputFolderPath + "/" + systemName + ".bip";
		/* Create the path to the resulting configuration file */
		String pathToSaveConfFile = outputFolderPath + "/" + systemName + "Conf.txt";

		/* Create the instance */
		return ArchitectureInstantiator.createArchitectureInstance(architectureStyle, architectureOperands,
				bipFileModel, pathToSaveBIPFile, pathToSaveConfFile);
	}

	public static void main(String[] args) throws ConfigurationFileException, ArchitectureExtractorException,
			IOException, ArchitectureBuilderException {
		try {

		} catch (Exception e) {

		}

		String archStylePath = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf.txt";
		ArchitectureStyle architectureStyle = new ArchitectureStyle(archStylePath);

		String archOperandsPath = "/home/vladimir/Architecture_examples/Archive/Modes2/AEConf-instance2.txt";
		ArchitectureOperands architectureOperands = new ArchitectureOperands(archOperandsPath);

		BIPFileModel bipFileModel = new BIPFileModel("Mutex", "Mutex", "mutex");

		String pathToSaveBIPFile = "/home/vladimir/Desktop/example.bip";

		String pathToSaveConfFile = "/home/vladimir/Desktop/exampleconf.txt";

		ArchitectureInstance instance = ArchitectureInstantiator.createArchitectureInstance(architectureStyle,
				architectureOperands, bipFileModel, pathToSaveBIPFile, pathToSaveConfFile);

		// ArchitectureInstance architectureInstance1 = new
		// ArchitectureInstance("MutualExclusion", "Mutex", "mutex");
		//
		// String archStylePath1 =
		// "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
		// ArchitectureStyle architectureStyle1 = new
		// ArchitectureStyle(archStylePath1);
		//
		// String archOperandsPath1 =
		// "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
		// ArchitectureOperands architectureOperands1 = new
		// ArchitectureOperands(archOperandsPath1);
		//
		// List<Component> coordinators =
		// ArchitectureStyleExtractor.getArchitectureStyleCoordinators(architectureStyle1);
		// List<Component> operands =
		// ArchitectureOperandsExtractor.getArchitectureOperands(architectureOperands1);
		//
		// List<Port> ports =
		// ArchitectureStyleExtractor.getArchitectureStylePorts(architectureStyle1);
		//
		// List<Connector> connectors =
		// ArchitectureStyleExtractor.getArchitectureStyleConnectors(architectureStyle1);
		//
		// ConnectorType c =
		// ArchitectureInstanceBuilder.copyConnectorType(architectureInstance1,
		// connectors.get(0).getType());
		//
		// AtomType a =
		// ArchitectureInstanceBuilder.copyAtomicType(architectureInstance1,
		// (AtomType) coordinators.get(0).getType());
		//
		// PortType p =
		// ArchitectureInstanceBuilder.copyPortType(architectureInstance1,
		// ports.get(0).getType());
		//
		// architectureInstance1.generateBipFile("/home/vladimir/Desktop/example.bip");
		// architectureInstance1.generateConfigurationFile("/home/vladimir/Desktop/exampleconf.txt");

	}

}

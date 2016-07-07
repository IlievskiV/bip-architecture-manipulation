package ch.epfl.risd.archman.builder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.epfl.risd.archman.checker.BIPChecker;
import ch.epfl.risd.archman.exceptions.ArchitectureBuilderException;
import ch.epfl.risd.archman.exceptions.ArchitectureExtractorException;
import ch.epfl.risd.archman.exceptions.ConfigurationFileException;
import ch.epfl.risd.archman.exceptions.IllegalPortParameterReferenceException;
import ch.epfl.risd.archman.exceptions.InvalidConnectorTypeNameException;
import ch.epfl.risd.archman.exceptions.InvalidPortParameterNameException;
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
import ujf.verimag.bip.Core.Behaviors.PortType;
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
		/* 2.Get operand port tuples */
		PortTuple operandPortTuple = connectorTuple.getOperandPortTuples().get(0);
		/* 3. Create an empty list of port parameters */
		List<PortParameter> portParameters = new LinkedList<PortParameter>();

		/* 3.1. Create coordinator port parameter */
		String coordinatorPortInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[1];
		String coordinatorPortTypeName = BIPExtractor
				.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName).getType().getName();

		portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
				BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), coordinatorPortTypeName),
				coordinatorPortInstanceName));

		/* 3.2 Create operand port parameter */
		String operandPortInstanceName = operandPortTuple.getPortInstanceName().split("\\.")[1];
		String operandPortTypeName = BIPExtractor
				.getPortByName(architectureInstance.getBipFileModel(), operandPortInstanceName).getType().getName();

		portParameters.add(ArchitectureInstanceBuilder.createPortParameter(
				BIPExtractor.getPortTypeByName(architectureInstance.getBipFileModel(), operandPortTypeName),
				operandPortInstanceName));

		/* 4.Create a list of port parameter references */
		List<PortParameterReference> portParameterReferences = new LinkedList<PortParameterReference>();
		for (PortParameter pp : portParameters) {

			PortParameterReference portParameterReference = ArchitectureInstanceBuilder
					.createPortParameterReference(pp);
			portParameterReferences.add(portParameterReference);
		}

		/* 5. Create a list of AC Expressions */
		List<ACExpression> expressions = new LinkedList<ACExpression>();
		expressions.addAll(portParameterReferences);

		/* 6.Create the ACFusion */
		ACFusion acFusion = ArchitectureInstanceBuilder.createACFusion(expressions);

		/* 7. Create an empty list of interactions */
		List<InteractionSpecification> interactionSpecifications = new LinkedList<InteractionSpecification>();

		/* 8. Get the name of the connector type */
		String connectorTypeName = BIPExtractor
				.getConnectorByName(architectureStyle.getBipFileModel(), connectorTuple.getConnectorInstanceName())
				.getType().getName();
		/* 9. Get the connector type */
		ConnectorType connectorType;

		/* 9.1. Create if not exists */
		if (!BIPChecker.connectorTypeExists(architectureInstance.getBipFileModel(), connectorTypeName)) {
			connectorType = ArchitectureInstanceBuilder.createConnectorType(architectureInstance, connectorTypeName,
					portParameters, acFusion, interactionSpecifications);
		}
		/* 9.2. Extract it, if it exists */
		else {
			connectorType = BIPExtractor.getConnectorTypeByName(architectureInstance.getBipFileModel(),
					connectorTypeName);
		}

		/* 10. Get the mapped ports */
		Set<String> operandMappingPorts = architectureOperands.getPortsMapping()
				.get((String) operandPortTuple.getPortInstanceName());
		int i = 0;

		/* 10.1. Iterate ever the mapped ports */
		for (String operandPort : operandMappingPorts) {
			/* 10.1.1.Create an empty list of actual port parameters */
			List<ActualPortParameter> actualPortParameters = new LinkedList<ActualPortParameter>();

			/* 10.1.2. Actual port parameter for the coordinator side */
			String coordinatorInstanceName = coordinatorPortTuple.getPortInstanceName().split("\\.")[0];
			actualPortParameters.add(ArchitectureInstanceBuilder.createInnerPortReference(
					ArchitectureInstanceBuilder.createPartElementReference(BIPExtractor
							.getComponentByName(architectureInstance.getBipFileModel(), coordinatorInstanceName)),
					BIPExtractor.getPortByName(architectureInstance.getBipFileModel(), coordinatorPortInstanceName)));

			/* 10.1.3. Actual port parameter for the operand side */
			String operandInstanceName = operandPort.split("\\.")[0];
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

	protected static void degCoord1MultOp1(ArchitectureStyle architectureStyle, ConnectorTuple connectorTuple) {

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

					System.out.println(connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedMultiplicity());
					System.out.println(connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree());

					System.out.println(connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity());
					System.out.println(connectorTuple.getOperandPortTuples().get(0).getCalculatedDegree());

					ArchitectureInstantiator.degCoordNMultOp1(connectorTuple, instance, architectureOperands,
							architectureStyle);
				}
				/*
				 * 7.1.3.Case: degree coordinator = 1, multiplicity operand = 1
				 */
				else if (connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree() == 1
						&& connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity() == 1) {

					System.out.println(connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedMultiplicity());
					System.out.println(connectorTuple.getCoordinatorPortTuples().get(0).getCalculatedDegree());

					System.out.println(connectorTuple.getOperandPortTuples().get(0).getCalculatedMultiplicity());
					System.out.println(connectorTuple.getOperandPortTuples().get(0).getCalculatedDegree());

					ArchitectureInstantiator.degCoord1MultOp1(architectureStyle, connectorTuple);
				}
			}
			/* 7.2.If singleton */
			else {

			}
		}

		/* End. Generate BIP file */
		instance.generateBipFile(pathToSaveBIPFile);
		/* End. Generate configuration file */
		instance.generateConfigurationFile(pathToSaveConfFile);

		return instance;
	}

	public static void main(String[] args) throws ConfigurationFileException, ArchitectureExtractorException,
			IOException, ArchitectureBuilderException {
		try {

		} catch (Exception e) {

		}

		String archStylePath = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf.txt";
		ArchitectureStyle architectureStyle = new ArchitectureStyle(archStylePath);

		String archOperandsPath = "/home/vladimir/Architecture_examples/Archive/Mutex/AEConf-instance2.txt";
		ArchitectureOperands architectureOperands = new ArchitectureOperands(archOperandsPath);

		BIPFileModel bipFileModel = new BIPFileModel("MutualExclusion", "Mutex", "mutex");

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

package de.b2tla.analysis.typerestriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import de.b2tla.analysis.MachineContext;
import de.b2tla.analysis.Typechecker;
import de.b2tla.analysis.nodes.ElementOfNode;
import de.b2tla.analysis.nodes.EqualsNode;
import de.b2tla.analysis.nodes.NodeType;
import de.b2tla.analysis.nodes.SubsetNode;
import de.b2tla.ltl.LTLFormulaVisitor;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AAnySubstitution;
import de.be4.classicalb.core.parser.node.AComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.AConstraintsMachineClause;
import de.be4.classicalb.core.parser.node.ADisjunctPredicate;
import de.be4.classicalb.core.parser.node.AEqualPredicate;
import de.be4.classicalb.core.parser.node.AExistsPredicate;
import de.be4.classicalb.core.parser.node.AForallPredicate;
import de.be4.classicalb.core.parser.node.AGeneralProductExpression;
import de.be4.classicalb.core.parser.node.AGeneralSumExpression;
import de.be4.classicalb.core.parser.node.AImplicationPredicate;
import de.be4.classicalb.core.parser.node.AInitialisationMachineClause;
import de.be4.classicalb.core.parser.node.ALambdaExpression;
import de.be4.classicalb.core.parser.node.ALetSubstitution;
import de.be4.classicalb.core.parser.node.AMemberPredicate;
import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.APreconditionSubstitution;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.AQuantifiedIntersectionExpression;
import de.be4.classicalb.core.parser.node.AQuantifiedUnionExpression;
import de.be4.classicalb.core.parser.node.ASelectSubstitution;
import de.be4.classicalb.core.parser.node.ASubsetPredicate;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.ltl.core.parser.node.AExistsLtl;
import de.be4.ltl.core.parser.node.AForallLtl;

public class TypeRestrictor extends DepthFirstAdapter {

	private MachineContext machineContext;
	private final IdentifierDependencies identifierDependencies;

	private Hashtable<Node, ArrayList<NodeType>> restrictedTypesSet;
	private HashSet<Node> removedNodes;

	public void addRemoveNode(Node node){
		this.removedNodes.add(node);
	}
	
	public TypeRestrictor(Start start, MachineContext machineContext,
			Typechecker typechecker) {
		this.machineContext = machineContext;
		this.restrictedTypesSet = new Hashtable<Node, ArrayList<NodeType>>();
		this.removedNodes = new HashSet<Node>();

		this.identifierDependencies = new IdentifierDependencies(machineContext);

		
		start.apply(this);

		checkLTLFormulas();
	}

	private void checkLTLFormulas() {
		for (LTLFormulaVisitor visitor : machineContext.getLTLFormulas()) {

			for (de.be4.ltl.core.parser.node.Node ltlNode : visitor
					.getUnparsedHashTable().keySet()) {
				Node bNode = visitor.getBAst(ltlNode);

				if (ltlNode instanceof AExistsLtl) {
					Node id = visitor.getLTLIdentifier(((AExistsLtl) ltlNode)
							.getExistsIdentifier().getText());
					HashSet<Node> list = new HashSet<Node>();
					list.add(id);
					analysePredicate(bNode, list, new HashSet<Node>());
				} else if (ltlNode instanceof AForallLtl) {
					Node id = visitor.getLTLIdentifier(((AForallLtl) ltlNode)
							.getForallIdentifier().getText());
					HashSet<Node> list = new HashSet<Node>();
					list.add(id);
					analysePredicate(bNode, list, new HashSet<Node>());
				}
				bNode.apply(this);
			}

		}
	}

	public ArrayList<NodeType> getRestrictedTypesSet(Node node) {
		return restrictedTypesSet.get(node);
	}

	public boolean hasARestrictedType(Node node) {
		return restrictedTypesSet.containsKey(node);
	}

	public boolean removeNode(Node node) {
		return this.removedNodes.contains(node);
	}

	private void putRestrictedType(Node identifier, NodeType expression) {
		ArrayList<NodeType> list = restrictedTypesSet.get(identifier);

		if (list == null) {
			list = new ArrayList<NodeType>();
			list.add(expression);
			restrictedTypesSet.put(identifier, list);
		} else {
			list.add(expression);
		}
	}

	@Override
	public void inAConstraintsMachineClause(AConstraintsMachineClause node) {
		HashSet<Node> list = new HashSet<Node>();
		list.addAll(machineContext.getSetParamter().values());
		list.addAll(machineContext.getScalarParameter().values());
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	@Override
	public void inAPropertiesMachineClause(APropertiesMachineClause node) {
		HashSet<Node> list = new HashSet<Node>();
		list.addAll(machineContext.getConstants().values());
		
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	
	public void analyseDisjunktionPredicate(PPredicate node, HashSet<Node> list) {
		if (node instanceof ADisjunctPredicate) {
			ADisjunctPredicate dis = (ADisjunctPredicate) node;
			analyseDisjunktionPredicate(dis.getLeft(), list);
			analyseDisjunktionPredicate(dis.getRight(), list);
		}else{
			analysePredicate(node, list, new HashSet<Node>());
		}
	}
	
	private void analysePredicate(Node n, HashSet<Node> list, HashSet<Node> ignoreList) {
		if (n instanceof AEqualPredicate) {
			PExpression left = ((AEqualPredicate) n).getLeft();
			Node r_left = machineContext.getReferences().get(left);
			PExpression right = ((AEqualPredicate) n).getRight();
			Node r_right = machineContext.getReferences().get(right);

			if (list.contains(r_left)
					&& isAConstantExpression(right, list, ignoreList)) {
				EqualsNode setNode = new EqualsNode(right);
				putRestrictedType(r_left, setNode);
				if (!machineContext.getConstants().containsValue(r_left)) {
					removedNodes.add(n);
				}

			}
			if (list.contains(r_right)
					&& isAConstantExpression(right, list, ignoreList)) {
				EqualsNode setNode = new EqualsNode(left);
				putRestrictedType(r_right, setNode);
				if (!machineContext.getConstants().containsValue(r_left)) {
					removedNodes.add(n);
				}
			}
			return;
		}

		if (n instanceof AMemberPredicate) {
			PExpression left = ((AMemberPredicate) n).getLeft();
			Node r_left = machineContext.getReferences().get(left);
			PExpression right = ((AMemberPredicate) n).getRight();
			if (list.contains(r_left)
					&& isAConstantExpression(right, list, ignoreList)) {
				putRestrictedType(r_left, new ElementOfNode(right));
				if (!machineContext.getConstants().containsValue(r_left)) {
					removedNodes.add(n);
				}
			}
			return;
		}

		if (n instanceof ASubsetPredicate) {
			PExpression left = ((ASubsetPredicate) n).getLeft();
			Node r_left = machineContext.getReferences().get(left);
			PExpression right = ((ASubsetPredicate) n).getRight();

			if (list.contains(r_left)
					&& isAConstantExpression(right, list, ignoreList)) {
				putRestrictedType(r_left, new SubsetNode(right));
				if (!machineContext.getConstants().containsValue(r_left)) {
					removedNodes.add(n);
				}
			}
			return;
		}

		if (n instanceof AConjunctPredicate) {
			analysePredicate(((AConjunctPredicate) n).getLeft(), list, ignoreList);
			analysePredicate(((AConjunctPredicate) n).getRight(), list, ignoreList);
			return;
		}

		if (n instanceof AExistsPredicate) {
			HashSet<Node> set = new HashSet<Node>();
			for (PExpression e : ((AExistsPredicate) n).getIdentifiers()) {
				set.add(e);
			}
			set.addAll(ignoreList);
			analysePredicate(((AExistsPredicate) n).getPredicate(), list, set);
		}

		if (n instanceof Start) {
			analysePredicate(((Start) n).getPParseUnit(), list, ignoreList);
		}

		if (n instanceof APredicateParseUnit) {
			analysePredicate(((APredicateParseUnit) n).getPredicate(), list, ignoreList);
			return;
		}
	}
	
	public boolean isAConstantExpression(Node node, HashSet<Node> list, HashSet<Node> ignoreList){
		HashSet<Node> newList = new HashSet<Node>();
		newList.addAll(list);
		newList.addAll(ignoreList);
		if(identifierDependencies.containsIdentifier(node, newList)){
			return false;
		}
		return true;
	}

	@Override
	public void inAForallPredicate(AForallPredicate node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		AImplicationPredicate implication = (AImplicationPredicate) node
				.getImplication();
		analysePredicate(implication.getLeft(), list, new HashSet<Node>());
	}

	@Override
	public void inAExistsPredicate(AExistsPredicate node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicate(), list, new HashSet<Node>());
	}

	@Override
	public void inAQuantifiedUnionExpression(AQuantifiedUnionExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	@Override
	public void inAQuantifiedIntersectionExpression(
			AQuantifiedIntersectionExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	@Override
	public void inAComprehensionSetExpression(AComprehensionSetExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
			// e.apply(this);
		}
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	@Override
	public void inALambdaExpression(ALambdaExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicate(), list, new HashSet<Node>());
	}

	public void inAGeneralSumExpression(AGeneralSumExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	public void inAGeneralProductExpression(AGeneralProductExpression node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		analysePredicate(node.getPredicates(), list, new HashSet<Node>());
	}

	private Hashtable<Node, HashSet<Node>> expectedIdentifieListTable = new Hashtable<Node, HashSet<Node>>();

	@Override
	public void caseAInitialisationMachineClause(
			AInitialisationMachineClause node) {
		expectedIdentifieListTable.put(node.getSubstitutions(),
				new HashSet<Node>());
		node.getSubstitutions().apply(this);
	}

	@Override
	public void caseAOperation(AOperation node) {
		HashSet<Node> list = new HashSet<Node>();
		{
			List<PExpression> copy = new ArrayList<PExpression>(
					node.getReturnValues());
			for (PExpression e : copy) {
				list.add(e);
			}
		}
		{
			List<PExpression> copy = new ArrayList<PExpression>(
					node.getParameters());
			for (PExpression e : copy) {
				list.add(e);
			}
		}
		expectedIdentifieListTable.put(node.getOperationBody(), list);
		if (node.getOperationBody() != null) {
			node.getOperationBody().apply(this);
		}
	}

	@Override
	public void inAPreconditionSubstitution(APreconditionSubstitution node) {
		HashSet<Node> list = getExpectedIdentifier(node);
		analysePredicate(node.getPredicate(), list, new HashSet<Node>());
	}

	private HashSet<Node> getExpectedIdentifier(Node node) {
		HashSet<Node> list = expectedIdentifieListTable.get(node);
		if (list == null)
			list = new HashSet<Node>();
		return list;
	}

	@Override
	public void inASelectSubstitution(ASelectSubstitution node) {
		HashSet<Node> list = getExpectedIdentifier(node);
		analysePredicate(node.getCondition(), list, new HashSet<Node>());
	}

	@Override
	public void inAAnySubstitution(AAnySubstitution node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		list.addAll(getExpectedIdentifier(node));
		analysePredicate(node.getWhere(), list, new HashSet<Node>());
	}

	@Override
	public void inALetSubstitution(ALetSubstitution node) {
		HashSet<Node> list = new HashSet<Node>();
		List<PExpression> copy = new ArrayList<PExpression>(
				node.getIdentifiers());
		for (PExpression e : copy) {
			list.add(e);
		}
		list.addAll(getExpectedIdentifier(node));
		analysePredicate(node.getPredicate(), list, new HashSet<Node>());
	}

}
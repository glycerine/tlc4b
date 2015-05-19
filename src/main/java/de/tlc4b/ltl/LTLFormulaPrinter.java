package de.tlc4b.ltl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.POperation;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.ltl.core.parser.analysis.DepthFirstAdapter;
import de.be4.ltl.core.parser.node.AAndFair1Ltl;
import de.be4.ltl.core.parser.node.AAndFair2Ltl;
import de.be4.ltl.core.parser.node.AAndLtl;
import de.be4.ltl.core.parser.node.ADeadlockLtl;
import de.be4.ltl.core.parser.node.ADetLtl;
import de.be4.ltl.core.parser.node.AEnabledLtl;
import de.be4.ltl.core.parser.node.AExistsLtl;
import de.be4.ltl.core.parser.node.AFairnessImplicationLtl;
import de.be4.ltl.core.parser.node.AFalseLtl;
import de.be4.ltl.core.parser.node.AFinallyLtl;
import de.be4.ltl.core.parser.node.AForallLtl;
import de.be4.ltl.core.parser.node.AGloballyLtl;
import de.be4.ltl.core.parser.node.AImpliesLtl;
import de.be4.ltl.core.parser.node.ANotLtl;
import de.be4.ltl.core.parser.node.AOpActions;
import de.be4.ltl.core.parser.node.AOrLtl;
import de.be4.ltl.core.parser.node.AStrongFairLtl;
import de.be4.ltl.core.parser.node.ATrueLtl;
import de.be4.ltl.core.parser.node.AUnparsedLtl;
import de.be4.ltl.core.parser.node.AWeakFairLtl;
import de.be4.ltl.core.parser.node.PActions;
import de.tlc4b.analysis.typerestriction.TypeRestrictor;
import de.tlc4b.prettyprint.TLAPrinter;

public class LTLFormulaPrinter extends DepthFirstAdapter {

	private final LTLFormulaVisitor ltlFormulaVisitor;
	private final TLAPrinter tlaPrinter;
	private final TypeRestrictor typeRestrictor;

	public LTLFormulaPrinter(TLAPrinter tlaPrinter,
			LTLFormulaVisitor ltlFormulaVisitor, TypeRestrictor typeRestrictor) {
		this.ltlFormulaVisitor = ltlFormulaVisitor;
		this.tlaPrinter = tlaPrinter;
		this.typeRestrictor = typeRestrictor;

		ltlFormulaVisitor.getLTLFormulaStart().apply(this);
	}

	@Override
	public void caseAGloballyLtl(AGloballyLtl node) {
		tlaPrinter.moduleStringAppend("[](");
		node.getLtl().apply(this);
		tlaPrinter.moduleStringAppend(")");
	}

	@Override
	public void caseAFinallyLtl(AFinallyLtl node) {
		tlaPrinter.moduleStringAppend("<>(");
		node.getLtl().apply(this);
		tlaPrinter.moduleStringAppend(")");
	}

	@Override
	public void caseATrueLtl(ATrueLtl node) {
		tlaPrinter.moduleStringAppend("TRUE");
	}

	@Override
	public void caseAFalseLtl(AFalseLtl node) {
		tlaPrinter.moduleStringAppend("FALSE");
	}

	@Override
	public void caseAUnparsedLtl(AUnparsedLtl node) {
		ltlFormulaVisitor.getBAst(node).apply(tlaPrinter);
	}

	@Override
	public void caseAAndLtl(AAndLtl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" /\\ ");
		node.getRight().apply(this);
	}

	@Override
	public void caseAAndFair1Ltl(AAndFair1Ltl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" /\\ ");
		node.getRight().apply(this);
	}

	@Override
	public void caseAAndFair2Ltl(AAndFair2Ltl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" /\\ ");
		node.getRight().apply(this);
	}

	@Override
	public void caseAOrLtl(AOrLtl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" \\/ ");
		node.getRight().apply(this);
	}

	@Override
	public void caseANotLtl(ANotLtl node) {
		tlaPrinter.moduleStringAppend("\\neg(");
		node.getLtl().apply(this);
		tlaPrinter.moduleStringAppend(")");
	}

	@Override
	public void caseAImpliesLtl(AImpliesLtl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" => ");
		node.getRight().apply(this);
	}

	@Override
	public void caseAFairnessImplicationLtl(AFairnessImplicationLtl node) {
		node.getLeft().apply(this);
		tlaPrinter.moduleStringAppend(" => ");
		node.getRight().apply(this);
	}

	@Override
	public void caseAEnabledLtl(AEnabledLtl node) {
		tlaPrinter.moduleStringAppend("ENABLED(");
		tlaPrinter.moduleStringAppend(node.getOperation().getText());
		tlaPrinter.moduleStringAppend(")");
	}

	@Override
	public void caseAWeakFairLtl(AWeakFairLtl node) {
		tlaPrinter
				.printWeakFairnessWithParameter(node.getOperation().getText());
	}

	@Override
	public void caseAStrongFairLtl(AStrongFairLtl node) {
		tlaPrinter.printStrongFairness(node.getOperation().getText());
	}

	@Override
	public void caseAExistsLtl(AExistsLtl node) {
		tlaPrinter.moduleStringAppend("\\E ");
		tlaPrinter.moduleStringAppend(node.getExistsIdentifier().getText());
		tlaPrinter.moduleStringAppend(" \\in ");
		Node id = this.ltlFormulaVisitor.getLTLIdentifier(node
				.getExistsIdentifier().getText());
		typeRestrictor.getRestrictedNode(id).apply(tlaPrinter);
		tlaPrinter.moduleStringAppend(": ");
		Start start = (Start) ltlFormulaVisitor.getBAst(node);
		APredicateParseUnit p = (APredicateParseUnit) start.getPParseUnit();
		if (!typeRestrictor.isARemovedNode(p.getPredicate())) {
			ltlFormulaVisitor.getBAst(node).apply(tlaPrinter);
			tlaPrinter.moduleStringAppend(" /\\ ");
		}
		node.getLtl().apply(this);
	}

	@Override
	public void caseAForallLtl(AForallLtl node) {
		tlaPrinter.moduleStringAppend("\\A ");
		tlaPrinter.moduleStringAppend(node.getForallIdentifier().getText());
		tlaPrinter.moduleStringAppend(" \\in ");
		Node id = this.ltlFormulaVisitor.getLTLIdentifier(node
				.getForallIdentifier().getText());
		typeRestrictor.getRestrictedNode(id).apply(tlaPrinter);
		tlaPrinter.moduleStringAppend(": ");
		Start start = (Start) ltlFormulaVisitor.getBAst(node);
		APredicateParseUnit p = (APredicateParseUnit) start.getPParseUnit();
		if (!typeRestrictor.isARemovedNode(p.getPredicate())) {
			ltlFormulaVisitor.getBAst(node).apply(tlaPrinter);
			tlaPrinter.moduleStringAppend(" => ");
		}
		node.getLtl().apply(this);
	}

	@Override
	public void caseADetLtl(ADetLtl node) {
		List<PActions> copy = new ArrayList<PActions>(node.getArgs());
		for (int i = 0; i < copy.size(); i++) {
			AOpActions action1 = (AOpActions) copy.get(i);
			for (int j = i+1; j < copy.size(); j++) {
				if(! (i == 0 && j == 1)){
					tlaPrinter.moduleStringAppend(" /\\ ");
				}
				tlaPrinter.moduleStringAppend("\\neg(ENABLED(");
				tlaPrinter.moduleStringAppend(action1.getOperation().getText());
				tlaPrinter.moduleStringAppend(") /\\ ENABLED(");
				AOpActions action2 = (AOpActions) copy.get(j);
				tlaPrinter.moduleStringAppend(action2.getOperation().getText());
				tlaPrinter.moduleStringAppend("))");
			}
		}
	}

	
    @Override
    public void caseADeadlockLtl(ADeadlockLtl node)
    {
    	tlaPrinter.moduleStringAppend("\\neg(");
    	
    	
    Iterator<POperation> itr = this.tlaPrinter.getTLAModule().getOperations().iterator();
		while (itr.hasNext()) {
			Node operation = itr.next();
			tlaPrinter.moduleStringAppend("ENABLED(");
			tlaPrinter.printOperationCall(operation);
			tlaPrinter.moduleStringAppend(")");
			if (itr.hasNext()) {
				tlaPrinter.moduleStringAppend(" \\/ ");
			}
		}
    	
        tlaPrinter.moduleStringAppend(")");
    }
	
}

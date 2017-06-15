package tregression.views;

import java.io.File;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import microbat.model.BreakPoint;
import microbat.model.trace.TraceNode;
import microbat.views.DebugFeedbackView;
import microbat.views.MicroBatViews;
import microbat.views.TraceView;
import tregression.editors.CompareEditor;
import tregression.editors.CompareTextEditorInput;
import tregression.model.PairList;
import tregression.model.TraceNodePair;
import tregression.separatesnapshots.DiffMatcher;
import tregression.separatesnapshots.diff.FileDiff;

public class BuggyTraceView extends TraceView {

	private PairList pairList;
	private DiffMatcher diffMatcher;

	public BuggyTraceView() {
	}

	private void openInCompare(CompareTextEditorInput input) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage workBenchPage = win.getActivePage();

		try {
			workBenchPage.openEditor(input, CompareEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	class CompareFileName {
		String buggyFileName;
		String fixFileName;

		public CompareFileName(String buggyFileName, String fixFileName) {
			super();
			this.buggyFileName = buggyFileName;
			this.fixFileName = fixFileName;
		}

	}

	private CompareFileName generateCompareFile(BreakPoint breakPoint, DiffMatcher matcher) {
		String buggyFileName;
		String fixFileName;
		
		String buggyPath = diffMatcher.getBuggyPath() + File.separator + matcher.getSourceFolderName() + File.separator;
		String fixPath = diffMatcher.getFixPath() + File.separator + matcher.getSourceFolderName() + File.separator;

		FileDiff fileDiff = diffMatcher.findSourceFileDiff(breakPoint);
		if (diffMatcher == null || fileDiff == null) {
			String baseFileName = breakPoint.getDeclaringCompilationUnitName();
			baseFileName = baseFileName.replace(".", File.separator) + ".java";

			buggyFileName = buggyPath + baseFileName;
			fixFileName = fixPath + baseFileName;
		} else {
			String sourceBase = fileDiff.getSourceDeclaringCompilationUnit();
			sourceBase = sourceBase.replace(".", File.separator) + ".java";
			buggyFileName = buggyPath + sourceBase;

			String targetBase = fileDiff.getTargetDeclaringCompilationUnit();
			targetBase = targetBase.replace(".", File.separator) + ".java";
			fixFileName = fixPath + targetBase;
		}
		
		CompareFileName cfn = new CompareFileName(buggyFileName, fixFileName);
		return cfn;
	}

	@Override
	protected void markJavaEditor(TraceNode node) {
		BreakPoint breakPoint = node.getBreakPoint();
		
		CompareFileName cfn = generateCompareFile(breakPoint, diffMatcher);

		CompareTextEditorInput input = new CompareTextEditorInput(cfn.buggyFileName, cfn.fixFileName, diffMatcher);

		openInCompare(input);

	}

	@Override
	protected void otherViewsBehavior(TraceNode node) {
		if (this.refreshProgramState) {
			DebugFeedbackView feedbackView = MicroBatViews.getDebugFeedbackView();
			feedbackView.setTraceView(BuggyTraceView.this);
			feedbackView.refresh(node);
		}

		TraceNodePair pair = pairList.findByMutatedNode(node);

		if (pair != null) {
			TraceNode originalNode = pair.getOriginalNode();

			if (originalNode != null) {
				CorrectTraceView view = EvaluationViews.getBeforeTraceView();
				view.jumpToNode(view.getTrace(), originalNode.getOrder(), false);
			}
		}

		markJavaEditor(node);
	}

	public PairList getPairList() {
		return pairList;
	}

	public void setPairList(PairList pairList) {
		this.pairList = pairList;
	}

	public DiffMatcher getDiffMatcher() {
		return diffMatcher;
	}

	public void setDiffMatcher(DiffMatcher diffMatcher) {
		this.diffMatcher = diffMatcher;
	}
}
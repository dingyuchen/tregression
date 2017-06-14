package tregression.separatesnapshots.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DiffParser {
	public List<FileDiff> parseDiff(List<String> diffContent, String sourceFolderName){
		List<FileDiff> fileDiffList = new ArrayList<>();
		FileDiff fileDiff = null;
		
		for(String line: diffContent){
			if(line.startsWith("diff")){
				if(fileDiff != null){
					fileDiffList.add(fileDiff);
				}
				fileDiff = new FileDiff();
				fileDiff.setSourceFolderName(sourceFolderName);
			}
			else if(line.startsWith("---")){
				String sourceFile = line.substring(line.indexOf("a"+File.separator)+1, line.length());
				fileDiff.setSourceFile(sourceFile);
			}
			else if(line.startsWith("+++")){
				String targetFile = line.substring(line.indexOf("b"+File.separator)+1, line.length());
				fileDiff.setTargetFile(targetFile);
			}
			else if(line.startsWith("@@")){
				String chunkInfo = line.substring(line.indexOf("@@")+3, line.lastIndexOf("@@")-1);
				
				String startLineInSource = chunkInfo.substring(chunkInfo.indexOf("-")+1, chunkInfo.indexOf(","));
				int startLineInS = Integer.valueOf(startLineInSource);
				
				String lengthInSource = chunkInfo.substring(chunkInfo.indexOf(",")+1, chunkInfo.indexOf(" "));
				int lengthInS = Integer.valueOf(lengthInSource);
				
				String startLineInTarget = chunkInfo.substring(chunkInfo.indexOf(" ")+2, chunkInfo.lastIndexOf(","));
				int startLinInT = Integer.valueOf(startLineInTarget);
				
				String lengthInTarget = chunkInfo.substring(chunkInfo.lastIndexOf(",")+1, chunkInfo.length());
				int lengthInT = Integer.valueOf(lengthInTarget);
				
				DiffChunk chunk = new DiffChunk(startLineInS, lengthInS, startLinInT, lengthInT);
				fileDiff.getChunks().add(chunk);
			}
			else if(line.startsWith(" ") || line.startsWith("+") || line.startsWith("-")){
				int size = fileDiff.getChunks().size();
				DiffChunk chunk = fileDiff.getChunks().get(size-1);
				
				int index = chunk.getChangeList().size();
				int type = changeType(line.toCharArray()[0]);
				
				LineChange change = new LineChange(index, type, line);
				chunk.getChangeList().add(change);
			}
		}
		
		return fileDiffList;
	}
	
	private int changeType(char s){
		if(s==' '){
			return LineChange.UNCHANGE;
		}
		else if(s=='+'){
			return LineChange.ADD;
		}
		else if(s=='-'){
			return LineChange.REMOVE;
		}
		
		return -1;
	}
}
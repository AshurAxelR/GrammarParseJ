package com.xrbpowered.parser.token;

import java.util.ArrayList;
import java.util.List;

public class LineColProvider {

	public record LineColPos(int line, int col, int pos) {
		@Override
		public String toString() {
			return String.format("(%d:%d)", line+1, col+1);
		}
	}
	
	public final String input;
	public final int tabSize;
	
	private List<Integer> lines = null;
	private int lastPos;
	private int length;
	private LineColPos endlc = null;
	
	public LineColProvider(String input, int tabSize) {
		this.input = input;
		this.tabSize = tabSize;
		length = input.isEmpty() ? 0 : -1; // -1 for not reached
	}

	public LineColProvider(String input) {
		this(input, 1);
	}

	public boolean isComplete() {
		return length >= 0;
	}
	
	public boolean isEmpty() {
		return length==0;
	}
	
	private int lineIndex(int pos) {
		int line = 0;
		if(lines!=null) {
			// TODO use binary search?
			for(int linePos : lines) {
				if(linePos > pos)
					return line-1;
				line++;
			}
			line--;
		}
		else if(!isComplete()) {
			lines = new ArrayList<>();
			lines.add(0);
			lastPos = 0;
			line = 0;
		}
		
		if(isComplete())
			return line-1;
		else
			return advance(line, pos);
	}
	
	private int advance(int line, int pos) {
		try {
			while(lastPos<=pos) {
				boolean cr = false;
				for(;;) {
					char ch = input.charAt(lastPos++);
					if(cr && ch!='\n') {
						lastPos--;
						break;
					}
					else if(ch=='\n')
						break;
					else if(ch=='\r')
						cr = false;
				}
				lines.add(lastPos);
				line++;
			}
			return line-1;
		}
		catch (IndexOutOfBoundsException e) {
			lines.add(lastPos);
			length = lastPos;
			endlc = new LineColPos(line, findCol(line, lastPos-1), lastPos);
			return line;
		}
	}
	
	private int findCol(int line, int pos) {
		int p = lines.get(line);
		if(tabSize<=1)
			return pos-p;
		
		int col = 0;
		while(p<pos) {
			char ch = input.charAt(p++);
			if(ch=='\t')
				col = (col/tabSize+1)*tabSize;
			else
				col++;
		}
		return col;
	}
	
	public LineColPos find(int pos, boolean clamp) {
		if(pos<0 || isEmpty())
			return (clamp || pos==0) ? new LineColPos(0, 0, 0) : null;
		if(isComplete() && pos>=length)
			return (clamp || pos==length) ? endlc : null;
		
		int line = lineIndex(pos);
		
		if(isComplete() && pos>=length)
			return (clamp || pos==length) ? endlc : null;
		return new LineColPos(line, findCol(line, pos), pos);
	}

	public LineColPos find(int pos) {
		return find(pos, true);
	}

}

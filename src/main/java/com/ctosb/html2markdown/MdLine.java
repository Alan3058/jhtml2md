
package com.ctosb.html2markdown;

public class MdLine {

	private int level;
	private MDLineType type;
	private StringBuilder content;

	public MdLine(MDLineType type, int level, String content) {
		this.type = type;
		this.level = level;
		this.content = new StringBuilder(content);
	}

	public String getContent() {
		return content.toString();
	}

	public MdLine append(String appendContent) {
		content.append(appendContent);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof MdLine && ((MdLine) o).type.equals(this.type);
	}

	@Override
	public String toString() {
		StringBuilder newLine = new StringBuilder();
		for (int j = 0; j < level; j++) {
			newLine.append("    ");
		}
		if (type.equals(MDLineType.Ordered)) {
			newLine.append(String.valueOf(1)).append(". ");
		} else if (type.equals(MDLineType.Unordered)) {
			newLine.append("* ");
		} else if (type.equals(MDLineType.Head1)) {
			newLine.append("# ");
		} else if (type.equals(MDLineType.Head2)) {
			newLine.append("## ");
		} else if (type.equals(MDLineType.Head3)) {
			newLine.append("### ");
		} else if (type.equals(MDLineType.HR)) {
			newLine.append("----");
		}
		String contentStr = content.toString();
		if (type.equals(MDLineType.Unordered)) {
			contentStr = contentStr.replaceAll("^\n", "");
		}
		newLine.append(contentStr);
		return newLine.toString();
	}

	public enum MDLineType {
		Ordered, Unordered, None, Head1, Head2, Head3, HR
	}
}

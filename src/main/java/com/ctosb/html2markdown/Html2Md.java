
package com.ctosb.html2markdown;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

/**
 * Convert Html to MarkDown
 * @date 2018/5/2 19:39
 * @author alan
 * @since 1.0.0
 */
public class Html2Md {

	/**
	 * li ol 缩排层级
	 */
	private static int indentation = -1;
	private static boolean orderedList = false;
	private static String[] languages = "1c,abnf,accesslog,actionscript,ada,apache,applescript,arduino,armasm,asciidoc,aspectj,autohotkey,autoit,avrasm,awk,axapta,bash,basic,bnf,brainfuck,cal,capnproto,ceylon,clean,clojure,clojure-repl,cmake,coffeescript,coq,cos,cpp,crmsh,crystal,cs,csp,css,d,dart,delphi,diff,django,dns,dockerfile,dos,dsconfig,dts,dust,ebnf,elixir,elm,erb,erlang,erlang-repl,excel,fix,flix,fortran,fsharp,gams,gauss,gcode,gherkin,glsl,go,golo,gradle,groovy,haml,handlebars,haskell,haxe,hsp,htmlbars,http,hy,inform7,ini,irpf90,java,javascript,json,julia,kotlin,lasso,ldif,leaf,less,lisp,livecodeserver,livescript,llvm,lsl,lua,makefile,markdown,mathematica,matlab,maxima,mel,mercury,mipsasm,mizar,mojolicious,monkey,moonscript,n1ql,nginx,nimrod,nix,nsis,objectivec,ocaml,openscad,oxygene,parser3,perl,pf,php,pony,powershell,processing,profile,prolog,protobuf,puppet,purebasic,python,q,qml,r,rib,roboconf,rsl,ruby,ruleslanguage,rust,scala,scheme,scilab,scss,smali,smalltalk,sml,sqf,sql,stan,stata,step21,stylus,subunit,swift,taggerscript,tap,tcl,tex,thrift,tp,twig,typescript,vala,vbnet,vbscript,vbscript-html,verilog,vhdl,vim,x86asm,xl,xml,xquery,yaml,zephir"
			.split(",");

	public static String convert(String theHTML, String baseURL) {
		Document doc = Jsoup.parse(theHTML, baseURL);
		return parseDocument(doc);
	}

	public static String convert(URL url, int timeoutMillis) throws IOException {
		Document doc = Jsoup.parse(url, timeoutMillis);
		return parseDocument(doc);
	}

	public static String convertHtml(String html, String charset) {
		Document doc = Jsoup.parse(html, charset);
		return parseDocument(doc);
	}

	public static String convertFile(File file, String charset) throws IOException {
		Document doc = Jsoup.parse(file, charset);
		return parseDocument(doc);
	}

	private static String parseDocument(Document doc) {
		indentation = -1;
		String title = doc.title();
		if (!title.trim().equals("")) {
			return "# " + title + "\n\n" + getTextContent(doc);
		} else {
			return getTextContent(doc);
		}
	}

	/**
	 * 获取元素文本内容
	 * @date 2018/5/2 19:04
	 * @author alan
	 * @since 1.0.0
	 * @param element
	 * @return
	 */
	private static String getTextContent(Element element) {
		ArrayList<MdLine> lines = new ArrayList<MdLine>();
		List<Node> children = element.childNodes();
		for (Node child : children) {
			if (child instanceof TextNode) {
				// 文本
				TextNode textNode = (TextNode) child;
				MdLine line = getLastLine(lines);
				if (element.tagName().equals("pre") || element.tagName().equals("code")) {
					// 处理代码块
					line.append(textNode.getWholeText());
				} else {
					// 非代码块
					line.append(textNode.text().replaceAll("#", "\\\\#").replaceAll("\\*", "\\\\*"));
				}
			} else if (child instanceof Element) {
				// 非静态文本
				Element childElement = (Element) child;
				processElement(childElement, lines);
			}
		}
		int blankLines = 0;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).toString();
			if ("".equals(line.trim())) {
				blankLines++;
			} else {
				blankLines = 0;
			}
			if (blankLines < 2) {
				// 添加内容
				result.append(line);
				// 处理空行
				if (i < lines.size() - 1) {
					result.append("\n");
				}
			}
		}
		return result.toString();
	}

	/**
	 * 处理元素
	 * @date 2018/5/2 19:02
	 * @author alan
	 * @since 1.0.0
	 * @param element
	 * @param lines
	 */
	private static void processElement(Element element, ArrayList<MdLine> lines) {
		Tag tag = element.tag();
		String tagName = tag.getName();
		if (tagName.equals("div")) {
			div(element, lines);
		} else if (tagName.equals("p")) {
			p(element, lines);
		} else if (tagName.equals("br")) {
			br(lines);
		} else if (tagName.matches("^h[0-9]+$")) {
			h(element, lines);
		} else if (tagName.equals("strong") || tagName.equals("b")) {
			strong(element, lines);
		} else if (tagName.equals("em")) {
			em(element, lines);
		} else if (tagName.equals("hr")) {
			hr(lines);
		} else if (tagName.equals("a")) {
			a(element, lines);
		} else if (tagName.equals("img")) {
			img(element, lines);
		} else if (tagName.equals("code")) {
			code(element, lines);
		} else if (tagName.equals("pre")) {
			pre(element, lines);
		} else if (tagName.equals("ul")) {
			ul(element, lines);
		} else if (tagName.equals("ol")) {
			ol(element, lines);
		} else if (tagName.equals("li")) {
			li(element, lines);
		} else {
			MdLine line = getLastLine(lines);
			line.append(getTextContent(element));
		}
	}

	/**
	 * 获取集合最后一个元素，没有则创建
	 * @date 2018/5/2 19:03
	 * @author alan
	 * @since 1.0.0
	 * @param lines
	 * @return
	 */
	private static MdLine getLastLine(ArrayList<MdLine> lines) {
		MdLine line;
		if (lines.size() > 0) {
			line = lines.get(lines.size() - 1);
		} else {
			line = new MdLine(MdLine.MDLineType.None, 0, "");
			lines.add(line);
		}
		return line;
	}

	private static void div(Element element, ArrayList<MdLine> lines) {
		MdLine line = getLastLine(lines);
		String content = getTextContent(element);
		if (!content.equals("")) {
			if (!line.getContent().trim().equals("")) {
				lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
				lines.add(new MdLine(MdLine.MDLineType.None, 0, content));
				lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
			} else {
				if (!content.trim().equals(""))
					line.append(content);
			}
		}
	}

	private static void p(Element element, ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
		lines.add(new MdLine(MdLine.MDLineType.None, 0, getTextContent(element)));
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void br(ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void h(Element element, ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
		int level = Integer.valueOf(element.tagName().substring(1));
		switch (level) {
			case 1:
				lines.add(new MdLine(MdLine.MDLineType.Head1, 0, getTextContent(element)));
				break;
			case 2:
				lines.add(new MdLine(MdLine.MDLineType.Head2, 0, getTextContent(element)));
				break;
			default:
				lines.add(new MdLine(MdLine.MDLineType.Head3, 0, getTextContent(element)));
				break;
		}
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void strong(Element element, ArrayList<MdLine> lines) {
		MdLine line = getLastLine(lines);
		line.append("**").append(getTextContent(element)).append("**");
	}

	private static void em(Element element, ArrayList<MdLine> lines) {
		MdLine line = getLastLine(lines);
		line.append("*").append(getTextContent(element)).append("*");
	}

	private static void hr(ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
		lines.add(new MdLine(MdLine.MDLineType.HR, 0, ""));
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void a(Element element, ArrayList<MdLine> lines) {
		MdLine line = getLastLine(lines);
		line.append("[").append(getTextContent(element)).append("]").append("(");
		String url = element.attr("href");
		line.append(url);
		String title = element.attr("title");
		if (!title.equals("")) {
			line.append(" \"").append(title).append("\"");
		}
		line.append(")");
	}

	private static void img(Element element, ArrayList<MdLine> lines) {
		MdLine line = getLastLine(lines);
		line.append("![");
		String alt = element.attr("alt");
		line.append(alt).append("]").append("(");
		String url = element.attr("src");
		line.append(url);
		String title = element.attr("title");
		if (!title.equals("")) {
			line.append(" \"").append(title).append("\"");
		}
		line.append(")");
	}

	private static void code(Element element, ArrayList<MdLine> lines) {
		pre(element, lines);
	}

	private static void pre(Element element, ArrayList<MdLine> lines) {
		// 查找支持的语言
		String language = Arrays.stream(languages)
				.filter(lang -> element.attributes().html()
						.matches("(.*[^0-9a-zA-Z]{1}){0,1}" + lang + "([^0-9a-zA-Z]{1}.*){0,1}"))
				.findFirst().orElse("java");
		// 添加语言
		lines.add(new MdLine(MdLine.MDLineType.None, 0, "```" + language));
		lines.add(new MdLine(MdLine.MDLineType.None, 0, getTextContent(element)));
		lines.add(new MdLine(MdLine.MDLineType.None, 0, "```"));
	}

	private static void ul(Element element, ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
		indentation++;
		orderedList = false;
		lines.add(new MdLine(MdLine.MDLineType.None, 0, getTextContent(element)));
		indentation--;
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void ol(Element element, ArrayList<MdLine> lines) {
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
		indentation++;
		orderedList = true;
		lines.add(new MdLine(MdLine.MDLineType.None, 0, getTextContent(element)));
		indentation--;
		lines.add(new MdLine(MdLine.MDLineType.None, 0, ""));
	}

	private static void li(Element element, ArrayList<MdLine> lines) {
		if (orderedList) {
			lines.add(new MdLine(MdLine.MDLineType.Ordered, indentation, getTextContent(element)));
		} else {
			lines.add(new MdLine(MdLine.MDLineType.Unordered, indentation, getTextContent(element)));
		}
	}
}

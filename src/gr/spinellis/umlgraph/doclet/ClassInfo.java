/*
 * Create a graphviz graph based on the classes in the specified java
 * source files.
 *
 * (C) Copyright 2002 Diomidis Spinellis
 * 
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * $Id$
 *
 */

import com.sun.javadoc.*;
import java.io.*;
import java.lang.*;
import java.util.*;

class ClassGraph {
	private ClassDoc c;
	private static HashMap classnames = new HashMap();
	private static int classnum;
	private PrintWriter w;
	private boolean stripPath;

	ClassGraph(PrintWriter iw, boolean isp, ClassDoc ic) { 
		c = ic;
		w = iw;
		stripPath = isp;
	}

	private String name(ClassDoc c) {
		String name;

		if ((name = (String)classnames.get(c)) == null) {
			// Associate classnames alias
			name = "c" + (new Integer(classnum)).toString();
			classnames.put(c, name);
			classnum++;
			String r = c.toString();
			if (stripPath) {
				// Create readable string by stripping leading path
				int dotpos = r.lastIndexOf('.');
				if (dotpos != -1)
					r = r.substring(dotpos + 1, r.length());
			}
			// Create label
			w.print("\t" + name + " [");
			w.print("label=\"" + r + "\"");
			if (c.isAbstract())
				w.print(", fontname=\"Helvetica-Oblique\"");
			w.println("];");
		}
		return name;
	}

	public void print() {
		String cs = name(c);
		// Print the derivation path
		ClassDoc s = c.superclass();
		if (s != null && !s.toString().equals("java.lang.Object")) {
			w.print("\t" + name(s) + " -> " + cs + " [dir=back,arrowtail=empty];");
			w.println("\t//" + c + " extends " + s);
		}
	}
}

public class UmlGraph {
	private static PrintWriter w;
	private static boolean stripPath = true;

	public static boolean start(RootDoc root)
                            throws IOException, UnsupportedEncodingException {
		FileOutputStream fos = new FileOutputStream("graph.dot");
		w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
		prologue();
		ClassDoc[] classes = root.classes();
		for (int i = 0; i < classes.length; i++) {
			ClassGraph c = new ClassGraph(w, stripPath, classes[i]);
			c.print();
		}
		epilogue();
		return true;
	}

	public static int optionLength(String option) {
		if(option.equals("-wholename")) {
			stripPath = false;
			return 1;
		}
			return 0;
	}

	private static void prologue() {
		w.println(
			"#!/usr/local/bin/dot\n" +
			"#\n" +
			"# Class hirerarchy\n" +
			"#\n\n" +
			"digraph G {\n" +
			"\tnode [fontname=\"Helvetica\",fontsize=8,shape=record];"
		);
	}

	private static void epilogue() {
		w.println("}\n");
		w.flush();
	}
}

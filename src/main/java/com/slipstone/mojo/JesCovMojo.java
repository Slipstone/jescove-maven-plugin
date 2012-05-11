package com.slipstone.mojo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import com.olabini.jescov.Configuration;
import com.olabini.jescov.CoverageData;
import com.olabini.jescov.generators.Generator;
import com.olabini.jescov.generators.HtmlGenerator;
import com.olabini.jescov.generators.JsonIngester;
import com.olabini.jescov.generators.XmlGenerator;

/**
 * Generate JesCov reports
 * 
 * @author Keith & Aaron
 * @author Based on ant plugin by olabini
 * @goal report
 */
public class JesCovMojo extends AbstractMojo {
	// plexus injected fields first
	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The format of the output
	 * 
	 * @parameter default-value="html"
	 * @required
	 */
	private String format;

	/**
	 * Specifies the source of the coverage data
	 * 
	 * @parameter default-value="${basedir}/jescov.json.ser"
	 * @required
	 */
	private File jsonOutputFile;

	/**
	 * Specifies the destination of the html report
	 * 
	 * @parameter default-value="${basedir}/target/jescov"
	 * @required
	 */
	private File htmlOutputDir;

	/**
	 * Specifies the destination of the xml report
	 * 
	 * @parameter default-value="${basedir}/target/jescov.xml"
	 * @required
	 */
	private File xmlOutputFile;

	/**
	 * The location of the javascript source code being tested
	 * 
	 * @parameter default-value="${basedir}"
	 * @required
	 */
	private File javascriptSourceDir;

	// Local fields below this point
	final Configuration c = new Configuration();

	/** @see org.apache.maven.plugin.Mojo#execute() */
	public void execute() throws MojoExecutionException {
		try {
			doIt();
		} catch (final Exception e) {
			throw new MojoExecutionException("", e);
		}
	}

	private void doIt() throws IOException {
		c.setJsonOutputFile(jsonOutputFile.getPath());
		c.setXmlOutputFile(xmlOutputFile.getPath());
		c.setHtmlOutputDir(htmlOutputDir.getPath());
		c.setSourceDirectory(javascriptSourceDir.getPath());

		if ("html".equalsIgnoreCase(format)) {
			executeHtml();
		} else if ("xml".equalsIgnoreCase(format)) {
			executeXml();
		} else {
			throw new RuntimeException("unknown JesCov format: " + format);
		}
	}

	private CoverageData read() throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(jsonOutputFile);
			return new JsonIngester().ingest(fr);
		} finally {
			if (fr != null) {
				fr.close();
			}
		}
	}

	private void executeHtml() throws IOException {
		final CoverageData data = read();
		new HtmlGenerator(c).generate(data);
	}

	private void executeXml() throws IOException {
		final CoverageData data = read();
		FileWriter fw = null;
		try {
			fw = new FileWriter(c.getXmlOutputFile());
			final Generator g = new XmlGenerator(fw);
			g.generate(data);
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
}

package org.msh.pharmadex.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELException;
import javax.faces.context.FacesContext;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.msh.pharmadex.dao.iface.FileTemplateDAO;
import org.msh.pharmadex.domain.FileTemplate;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to process review details template in docx format
 * @author Alex Kurasoff
 *
 */
@Service
public class TemplService {
	private static Pattern pat = Pattern.compile("\\#\\{.+?\\}");
	
	@Autowired
	FileTemplateDAO fileTemplateDAO;
	
	/**
	 * save FileTemplate Entity
	 * @param fileTemplate 
	 */
	public FileTemplate save(FileTemplate fileTemplate){
		return fileTemplateDAO.saveAndFlush(fileTemplate);
	}
	
	public FileTemplate findByTemplateType(TemplateType templateType){
		List<FileTemplate> list = getFileTemplateDAO().findByTemplateType(templateType);
		if(list!=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}
	 
	/**
	 * Simply test it
	 * @param inputDocument
	 * @return quantity of ELs found
	 * @throws IOException
	 * @throws ELException
	 */
	public int testIt(InputStream inputDocument) throws IOException, ELException {
		XWPFDocument doc = new XWPFDocument(inputDocument);
		return resolveDocument(doc);
	}
	
	/**
	 * Generate document and create input stream from the result
	 * @param inputDocument 
	 * @return input stream for future processing
	 */
	public InputStream generateReport(InputStream inputDocument) throws IOException, ELException {
		XWPFDocument doc = new XWPFDocument(inputDocument);
		resolveDocument(doc);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.write(out);
		doc.close();
		return new ByteArrayInputStream(out.toByteArray());
	}
	
	/**
	 * Resolve EL expressions in the doc
	 * @param doc
	 * @return quantity of ELs
	 */
	private int resolveDocument(XWPFDocument doc) throws ELException{
		int ret = 0;
		for (XWPFParagraph par :doc.getParagraphs()){
			ret = ret+resolveParagraph(par);
		}
		for (XWPFTable tbl : doc.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph par : cell.getParagraphs()) {
						ret = ret+resolveParagraph(par);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Resolve paragraph data
	 * @param par paragraph
	 * @return quantity of EL
	 */
	private int resolveParagraph(XWPFParagraph par) throws ELException {
		int ret = 0;
		XWPFRun prevRun=null;
		// normalize runs
		for(XWPFRun run : par.getRuns()){
			if(run!=null){
				if(prevRun==null){
					prevRun=run;
				}else{
					if(compareRuns(prevRun,run)){
						prevRun.setText(prevRun.getText(0) + run.getText(0),0);
						run.setText("",0);
					}else{
						prevRun = run;
					}
				}
			}
		}
		// resolve
		for(XWPFRun run : par.getRuns()){
			if(run.getText(0)!= null){
				//System.out.println(run.getText(0));
				Matcher match = pat.matcher(run.getText(0));
				StringBuffer sb = new StringBuffer();
				while(match.find()){
					String toEval=match.group();
					//match.appendReplacement(sb, toEval.toUpperCase()); //simulate evaluation
					//String repl = (String) Faces.evaluateExpressionGet(toEval);

					String repl = evaluateEL(toEval);
					if(repl != null){
						match.appendReplacement(sb, repl); //real evaluation
					}else{
						throw new ELException("Impossibe to evaluate " + toEval);
					}
					ret++;
				}
				match.appendTail(sb);
				run.setText(sb.toString(),0); //test resolve
			}
		}
		return ret;
	}
	public String evaluateEL(String el){
		//get current EL context
		javax.el.ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		 
		//get the expression factory (for JSF or ADF).
		javax.el.ExpressionFactory expressionFactory = 
		FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
		 
		//Create value expression as the EL I'm evaluating is a value e.g. #{bean.property}. 
		//Create MethodExpression if the EL is a method e.g. #{bean.method()}
		javax.el.ValueExpression valueExpression = 
		expressionFactory.createValueExpression(elContext, el, 
		String.class);
		return (String) valueExpression.getValue(elContext);
	}

	public boolean compareRuns(XWPFRun prev, XWPFRun actual){
		return	prev.getCharacterSpacing() == actual.getCharacterSpacing()
				&& prev.getKerning() == actual.getKerning()
				&& prev.getSubscript().equals(actual.getSubscript())
				&& prev.getUnderline().equals(actual.getUnderline())
				&& prev.isBold() == actual.isBold()
				&& prev.isCapitalized() == actual.isCapitalized()
				&& prev.isDoubleStrikeThrough() == actual.isDoubleStrikeThrough()
				&& prev.isEmbossed() == actual.isEmbossed()
				&& prev.isHighlighted() == actual.isHighlighted()
				&& prev.isImprinted() == actual.isImprinted()
				&& prev.isItalic() == actual.isItalic()
				&& prev.isShadowed() == actual.isShadowed()
				&& prev.isSmallCaps() == actual.isSmallCaps()
				&& prev.isStrikeThrough() == actual.isStrikeThrough()
				&& prev.getEmbeddedPictures().size()==0;
	}
	
	/**
	 * load all FileTemplate
	 * @return 
	 */
	public List<FileTemplate> findAll() {
		List<FileTemplate> ret = getFileTemplateDAO().findAll();
		Collections.sort(ret, new Comparator<FileTemplate>() {
			@Override
			public int compare(FileTemplate o1, FileTemplate o2) {
				return o1.getTemplateType().compareTo(o2.getTemplateType());
			}
		});
		return ret; 
	}
	
	public FileTemplateDAO getFileTemplateDAO() {
		return fileTemplateDAO;
	}
	public void setFileTemplateDAO(FileTemplateDAO fileTemplateDAO) {
		this.fileTemplateDAO = fileTemplateDAO;
	}

	

}

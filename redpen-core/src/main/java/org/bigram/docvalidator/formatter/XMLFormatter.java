/**
 * redpen: a text inspection tool
 * Copyright (C) 2014 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigram.docvalidator.formatter;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bigram.docvalidator.DocumentValidatorException;
import org.bigram.docvalidator.ValidationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * XML Output formatter.
 */
public class XMLFormatter implements Formatter {

  /**
   * Constructor.
   *
   * @throws org.bigram.docvalidator.DocumentValidatorException when failed to create Formatter
   */
  public XMLFormatter() throws DocumentValidatorException {
    super();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      this.db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new DocumentValidatorException(e.getMessage());
    }
  }

  @Override
  public String convertError(ValidationError error) {
    // create dom
    Document doc = db.newDocument();
    Element errorElement = doc.createElement("error");
    doc.appendChild(errorElement);


    Element validatorElement = doc.createElement("validator");
    errorElement.appendChild(validatorElement);
    Text validator = doc.createTextNode(error.getValidatorName());
    validatorElement.appendChild(validator);

    if (error.getMessage() != null && !error.getMessage().equals("")) {
      Element contentElement = doc.createElement("message");
      errorElement.appendChild(contentElement);
      Text content = doc.createTextNode(error.getMessage());
      contentElement.appendChild(content);
    }

    if (error.getFileName() != null && !error.getFileName().equals("")) {
      Element fileNameElement = doc.createElement("file");
      errorElement.appendChild(fileNameElement);
      Text fileName = doc.createTextNode(error.getFileName());
      fileNameElement.appendChild(fileName);
    }

    Element lineNumberElement = doc.createElement("lineNum");
    errorElement.appendChild(lineNumberElement);
    Text lineNum = doc.createTextNode(Integer.toString(error.getLineNumber()));
    lineNumberElement.appendChild(lineNum);

    if (error.getSentence() != null
        && !error.getSentence().content.equals("")) {
      Element sentencElement = doc.createElement("sentence");
      errorElement.appendChild(sentencElement);
      Text content = doc.createTextNode(error.getSentence().content);
      sentencElement.appendChild(content);
    }

    // create a transformer
    Transformer transformer = createTransformer();
    if (transformer == null) {
      throw new IllegalStateException("Failed to create XML Transformer");
    }

    // convert the result dom into a string
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    DOMSource source = new DOMSource(doc);
    try {
      transformer.transform(source, result);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    return writer.toString();
  }

  private Transformer createTransformer() {
    TransformerFactory tf;
    try {
      tf = TransformerFactory.newInstance();
    } catch (Throwable e) {
      LOG.error(e.getMessage());
      return null;
    }

    Transformer transformer;
    try {
      transformer = tf.newTransformer();
    } catch (TransformerConfigurationException e) {
      LOG.error(e.getMessage());
      return null;
    }
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    return transformer;
  }

  @Override
  public String header() {
    return "<validation-result>";
  }

  @Override
  public String footer() {
    return "</validation-result>";
  }

  private DocumentBuilder db;

  private static final Logger LOG =
      LoggerFactory.getLogger(XMLFormatter.class);
}

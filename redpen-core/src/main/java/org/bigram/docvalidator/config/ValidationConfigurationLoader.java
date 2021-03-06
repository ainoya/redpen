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
package org.bigram.docvalidator.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bigram.docvalidator.util.SAXErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Load configuration file of Validators.
 */
public final class ValidationConfigurationLoader {

  /**
   * Constructor.
   *
   * @param stream input configuration settings
   * @return Configuration loaded from input stream
   * NOTE: return null when failed to create Configuration object
   */
  public static ValidatorConfiguration loadConfiguration(InputStream stream) {
    Document doc = parseConfigurationString(stream);
    if (doc == null) {
      LOG.error("Failed to parse configuration string");
      return null;
    }

    doc.getDocumentElement().normalize();
    NodeList rootComponentElementList = doc.getElementsByTagName("component");
    doc.getElementsByTagName("configuration");
    if (rootComponentElementList.getLength() == 0) {
      LOG.error("No \"configuration\" block found in the configuration");
      return null;
    } else if (rootComponentElementList.getLength() > 1) {
      LOG.warn("Found more than one root \"component\""
          + " blocks in the configuration");
      LOG.warn("Use the first configuration block ...");
    }

    Element rootElement = (Element) rootComponentElementList.item(0);
    ValidatorConfiguration rootConfiguration =
        new ValidatorConfiguration(rootElement.getAttribute("name"));

    NodeList nodeList = rootElement.getChildNodes();
    if (nodeList.getLength() == 0) {
      LOG.warn("No validator is registered...");
    }

    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node nNode = nodeList.item(temp);
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) nNode;
        if (element.getNodeName().equals("component")) {
          rootConfiguration.addChild(
              createConfiguration(element, rootConfiguration));
        } else if (element.getNodeName().equals("property")) {
          rootConfiguration.addAttribute(element.getAttribute("name"),
              element.getAttribute("value"));
        } else {
          LOG.warn("Invalid block: \"" + element.getNodeName() + "\"");
          LOG.warn("Skip this block ...");
        }
      }
    }
    return rootConfiguration;
  }

  /**
   * Load Configuration settings from the specified file.
   *
   * @param xmlFile configuration file (xml format)
   * @return Configuration object containing the settings written in input file
   */
  public static ValidatorConfiguration loadConfiguration(String xmlFile) {
    InputStream fis = null;
    try {
      fis = new FileInputStream(xmlFile);
    } catch (FileNotFoundException e) {
      LOG.error(e.getMessage());
    }
    return loadConfiguration(fis);
  }

  private static Document parseConfigurationString(InputStream input) {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    Document doc = null;
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      dBuilder.setErrorHandler(new SAXErrorHandler());
      doc = dBuilder.parse(input);
    } catch (SAXException e) {
      LOG.error(e.getMessage());
    } catch (IOException e) {
      LOG.error(e.getMessage());
    } catch (ParserConfigurationException e) {
      LOG.error(e.getMessage());
    } catch (Throwable e) {
      LOG.error(e.getMessage());
    }
    return doc;
  }

  private static ValidatorConfiguration createConfiguration(
      Element element, ValidatorConfiguration parent) {
    ValidatorConfiguration currentConfiguration =
        new ValidatorConfiguration(element.getAttribute("name"), parent);
    NodeList nodeList = element.getChildNodes();
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node childNode = nodeList.item(temp);
      String nodeName = childNode.getNodeName();
      if (nodeName.equals("component")) {
        currentConfiguration.addChild(createConfiguration(
            (Element) childNode, currentConfiguration));
      } else if (nodeName.equals("property")) {
        Element currentElement = (Element) childNode;
        currentConfiguration.addAttribute(currentElement.getAttribute("name"),
            currentElement.getAttribute("value"));
      }
    }
    return currentConfiguration;
  }

  /**
   * Default Constructor.
   */
  private ValidationConfigurationLoader() {
  }

  private static final Logger LOG =
      LoggerFactory.getLogger(ValidationConfigurationLoader.class);
}

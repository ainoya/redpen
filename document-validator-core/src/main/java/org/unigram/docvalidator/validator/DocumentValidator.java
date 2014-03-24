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
package org.unigram.docvalidator.validator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unigram.docvalidator.model.DocumentCollection;
import org.unigram.docvalidator.model.Document;
import org.unigram.docvalidator.util.*;

/**
 * Validate all input files using appended Validators.
 */
public class DocumentValidator {

  private DocumentValidator(Builder builder) throws DocumentValidatorException {
    DVResource resource = builder.resource;
    this.distributor = builder.distributor;
    this.conf = resource.getConfiguration();
    this.charTable = resource.getCharacterTable();
    this.validators = loadValidators(this.conf, this.charTable);
  }

  /**
   * Load validators written in the configuration file.
   *
   * @return true when succeeded to load all the validators, false otherwise
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public List<Validator> loadValidators(ValidatorConfiguration rootConfig,
                                        CharacterTable charTable)
      throws DocumentValidatorException {
    List<Validator> validators = new ArrayList<Validator>();

    for (ValidatorConfiguration config : rootConfig.getChildren()) {
      String confName = config.getConfigurationName();
      Validator validator =
          ValidatorFactory.createValidator(confName, config, charTable);
      validators.add(validator);
    }

    return validators;
  }

  /**
   * Validate the input document collection.
   *
   * @param documentCollection input document collection generated by Parser
   * @return list of validation errors
   */
  public List<ValidationError> check(DocumentCollection documentCollection) {
    distributor.flushHeader();
    List<ValidationError> errors = new ArrayList<ValidationError>();
    for (Validator validator : this.validators) {
      Iterator<Document> fileIterator = documentCollection.getDocuments();
      while (fileIterator.hasNext()) {
        try {
          List<ValidationError> currentErrors =
              validator.check(fileIterator.next(), distributor);
          errors.addAll(currentErrors);
        } catch (Throwable e) {
          LOG.error("Error occurs in validation: " + e.getMessage());
          LOG.error("Validator class: " + validator.getClass());
        }
      }
    }
    distributor.flushFooter();
    return errors;
  }

  /**
   * Constructor only for testing.
   */
  protected DocumentValidator() {
    this.distributor = ResultDistributorFactory.createDistributor("plain",
        System.out);
    this.validators = new ArrayList<Validator>();
    this.conf = null;
    this.charTable = null;
  }

  /**
   * Append a specified validator.
   *
   * @param validator Validator used in testing
   */
  protected void appendValidator(Validator validator) {
    this.validators.add(validator);
  }

  public static class Builder {

    private DVResource resource;

    private ResultDistributor distributor = new DefaultResultDistributor(
        new PrintStream(System.out)
    );

    public Builder setResource(DVResource resource) {
      this.resource = resource;
      return this;
    }

    public Builder setResultDistributor(ResultDistributor distributor) {
      this.distributor = distributor;
      return this;
    }

    public DocumentValidator build() throws DocumentValidatorException {
      return new DocumentValidator(this);
    }
  }

  private final List<Validator> validators;

  private final ValidatorConfiguration conf;

  private final CharacterTable charTable;

  private ResultDistributor distributor;

  private static final Logger LOG =
      LoggerFactory.getLogger(DocumentValidator.class);
}

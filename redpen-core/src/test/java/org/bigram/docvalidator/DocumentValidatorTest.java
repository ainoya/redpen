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
package org.bigram.docvalidator;

import org.apache.commons.io.input.ReaderInputStream;
import org.bigram.docvalidator.distributor.FakeResultDistributor;
import org.junit.Test;
import org.bigram.docvalidator.config.Configuration;
import org.bigram.docvalidator.config.ValidationConfigurationLoader;
import org.bigram.docvalidator.config.ValidatorConfiguration;
import org.bigram.docvalidator.model.DocumentCollection;
import org.bigram.docvalidator.model.Sentence;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class DocumentValidatorTest {

  @Test
  public void testEmptyValidator() throws DocumentValidatorException {

    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("")
        .addSection(1, new ArrayList<Sentence>())
        .addParagraph()
        .addSentence(
            "In a land far away, there once was as a hungry programmer.",
            1)
        .addSentence(
            "He was hungry for programming and programmed all day - "
            + " - in Java, Python, C++, etc.", 2)
        .addSentence(
            "Whe he wasn't programming, he was eating noodles.",
            3)
        .addParagraph()
        .addSentence(
            "One day while programming, he got a new idea.", 4)
        .build();

   ValidatorConfiguration validatorConfig = new ValidatorConfiguration(
        "<?xml version=\"1.0\"?>\n" +
            "<character-table></character-table>"
    );
    Configuration configuration = new Configuration(
        validatorConfig); // = ValidatorConfiguration + CharacterTable

    DocumentValidator validator = new DocumentValidator.Builder()
        .setConfiguration(configuration)
        .setResultDistributor(new FakeResultDistributor())
        .build();

    List<ValidationError> errors = validator.check(documents);

    assertEquals(0, errors.size());
  }


  @Test
  public void testSentenceValidatorWithSimpleDocument()
      throws DocumentValidatorException {
    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("tested file")
        .addSection(0, new ArrayList<Sentence>())
        .addParagraph()
        .addSentence("it is a piece of a cake.", 0)
        .addSentence("that is also a piece of a cake.", 1)
        .build();

    DocumentValidator validator = getValidaorWithSentenceValidator();

    List<ValidationError> errors = validator.check(documents);

    // validate the errors
    assertEquals(2, errors.size());
    for (ValidationError error : errors) {
      assertThat(error.getValidatorName(), is("SentenceLength"));
      assertThat(error.getMessage(),
          containsString("The length of the line exceeds the maximum "));
    }
  }

  @Test
  public void testSectionValidatorWithSimpleDocument()
      throws DocumentValidatorException {
    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("tested file")
        .addSection(0, new ArrayList<Sentence>())
        .addSectionHeader("foobar")
        .addParagraph()
        .addSentence("it is a piece of a cake.", 0)
        .addSentence("that is also a piece of a cake.", 1)
        .build();

    DocumentValidator validator = getValidaorWithSectionValidator();
    List<ValidationError> errors = validator.check(documents);

    // validate the errors
    assertEquals(1, errors.size());
    for (ValidationError error : errors) {
      assertThat(error.getValidatorName(), is("SectionLength"));
      assertThat(error.getMessage(),
          containsString("The number of the character exceeds the maximum"));
    }
  }

  @Test
  public void testDocumentWithHeader() throws DocumentValidatorException {
    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("tested file")
        .addSection(0)
        .addSectionHeader("this is it.")
        .addParagraph()
        .addSentence("it is a piece of a cake.", 0)
        .addSentence("that is also a piece of a cake.", 1)
        .build();

    DocumentValidator validator = getValidaorWithSentenceValidator();
    List<ValidationError> errors = validator.check(documents);

    // validate the errors
    assertEquals(3, errors.size());
    for (ValidationError error : errors) {
      assertThat(error.getValidatorName(), is("SentenceLength"));
      assertThat(error.getMessage(),
          containsString("The length of the line exceeds the maximum "));
    }
  }

  @Test
  public void testDocumentWithList() throws DocumentValidatorException {
    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("tested file")
        .addSection(0, new ArrayList<Sentence>())
        .addSectionHeader("this is it")
        .addParagraph()
        .addSentence("it is a piece of a cake.", 0)
        .addSentence("that is also a piece of a cake.", 1)
        .addListBlock()
        .addListElement(0, "this is a list.")
        .build();

    DocumentValidator validator = getValidaorWithSentenceValidator();
    List<ValidationError> errors = validator.check(documents);

    // validate the errors
    assertEquals(4, errors.size());
    for (ValidationError error : errors) {
      assertThat(error.getValidatorName(), is("SentenceLength"));
      assertThat(error.getMessage(),
          containsString("The length of the line exceeds the maximum "));
    }
  }

  @Test
  public void testDocumentWithoutContent() throws DocumentValidatorException {
    DocumentCollection documents = new DocumentCollection.Builder()
        .addDocument("tested file")
        .build();

    DocumentValidator validator = getValidaorWithSentenceValidator();
    List<ValidationError> errors = validator.check(documents);

    // validate the errors
    assertEquals(0, errors.size());

  }

  private DocumentValidator getValidaorWithSentenceValidator() throws
      DocumentValidatorException {
    ValidatorConfiguration validatorConfig =
        ValidationConfigurationLoader.loadConfiguration(
            new ReaderInputStream(new StringReader(
                "<?xml version=\"1.0\"?>\n" +
                    "<component name=\"Validator\">" +
                    "  <component name=\"SentenceLength\">\n" +
                    "    <property name=\"max_length\" value=\"5\"/>\n" +
                    "  </component>" +
                    "</component>"
            ))
        );
    Configuration configuration = new Configuration(validatorConfig);
    return new DocumentValidator.Builder()
        .setConfiguration(configuration)
        .setResultDistributor(new FakeResultDistributor())
        .build();
  }

  private DocumentValidator getValidaorWithSectionValidator() throws
      DocumentValidatorException {
    ValidatorConfiguration validatorConfig =
        ValidationConfigurationLoader.loadConfiguration(
            new ReaderInputStream(new StringReader(
                "<?xml version=\"1.0\"?>\n" +
                    "<component name=\"Validator\">" +
                    "  <component name=\"SectionLength\">\n" +
                    "    <property name=\"max_char_num\" value=\"5\"/>\n" +
                    "  </component>" +
                    "</component>"
            ))
        );
    Configuration configuration = new Configuration(validatorConfig);
    return new DocumentValidator.Builder()
        .setConfiguration(configuration)
        .setResultDistributor(new FakeResultDistributor())
        .build();
  }
}

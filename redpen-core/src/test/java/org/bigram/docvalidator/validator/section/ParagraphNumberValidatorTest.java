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
package org.bigram.docvalidator.validator.section;

import org.bigram.docvalidator.validator.section.ParagraphNumberValidator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.bigram.docvalidator.model.Document;
import org.bigram.docvalidator.model.Paragraph;
import org.bigram.docvalidator.model.Section;
import org.bigram.docvalidator.ValidationError;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParagraphNumberValidatorTest {
  
  private static ParagraphNumberValidator validator;
  
  @BeforeClass
  public static void setUp() {
    validator = new ParagraphNumberValidator();
    validator.setMaxParagraphNumber(3);
  }
  
  @Test
  public void testSectionWithManySection() {
    Section section = new Section(0, "header");

    section.appendParagraph(new Paragraph());
    section.appendParagraph(new Paragraph());
    section.appendParagraph(new Paragraph());
    section.appendParagraph(new Paragraph());

    List<ValidationError> errors = validator.validate(section);
    assertEquals(1, errors.size());
  }

  @Test
  public void testSectionWithOnlyOneSection() {

    Section section = new Section(0);
    section.appendParagraph(new Paragraph());

    Document document = new Document();
    document.appendSection(section);

    List<ValidationError> errors = validator.validate(section);
    assertEquals(0, errors.size());
  }

}

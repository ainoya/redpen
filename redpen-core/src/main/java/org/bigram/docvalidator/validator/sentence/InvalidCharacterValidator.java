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
package org.bigram.docvalidator.validator.sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bigram.docvalidator.DocumentValidatorException;
import org.bigram.docvalidator.model.Sentence;
import org.bigram.docvalidator.ValidationError;
import org.bigram.docvalidator.config.CharacterTable;
import org.bigram.docvalidator.config.ValidatorConfiguration;

/**
 * Validate if there is invalid characters in sentences.
 */
public class InvalidCharacterValidator implements SentenceValidator {
  public InvalidCharacterValidator(ValidatorConfiguration config,
                                   CharacterTable characterTable)
      throws DocumentValidatorException {
    initialize(characterTable);
  }

  public InvalidCharacterValidator() {
  }

  public List<ValidationError> validate(Sentence sentence) {
    List<ValidationError> errors = new ArrayList<ValidationError>();
    Set<String> names = characterTable.getNames();
    for (String name : names) {
      ValidationError error = validateCharacter(sentence, name);
      if (error != null) {
        errors.add(error);
      }
    }
    return errors;
  }

  private boolean initialize(CharacterTable characters)
      throws DocumentValidatorException {
    this.characterTable = characters;
    return true;
  }

  protected void setCharacterTable(CharacterTable characters) {
    this.characterTable = characters;
  }

  private ValidationError validateCharacter(Sentence sentence, String name) {
    String sentenceStr = sentence.content;
    org.bigram.docvalidator.config.Character character = characterTable.getCharacter(name);
    List<String> invalidCharsList = character.getInvalidChars();
    for (String invalidChar : invalidCharsList) {
      if (sentenceStr.contains(invalidChar)) {
        return new ValidationError(
            this.getClass(),
            "Invalid symbol found: \"" + invalidChar + "\"",
            sentence);
      }
    }
    return null;
  }

  private CharacterTable characterTable;
}

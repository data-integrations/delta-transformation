/*
 * Copyright © 2021 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.delta.transformation;

import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.transformation.api.MutableRowSchema;
import io.cdap.transformation.api.MutableRowValue;
import io.cdap.transformation.api.Transformation;
import io.cdap.transformation.api.TransformationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mask Transformation
 *
 * Mask by character substitution
 * <p>
 *  Substitution masking is generally used for masking credit card or SSN numbers.
 *  This type of masking is fixed masking, where the pattern is applied on the
 *  fixed length string.
 *  “directive” : “mask col_name direction masking_character n”
 *  Masks everything by substituting masking character except
 *  first n characters in the given direction of the given column.
 *  Masking directions can be right or left.
 * </p>
 */
@Plugin(type = Transformation.PLUGIN_TYPE)
@Name(MaskTransformation.NAME)
public class MaskTransformation implements Transformation {

  public static final String NAME = "mask";
  public static final String RIGHT_DIRECTION = "right";
  public static final String LEFT_DIRECTION = "left";
  private String srcColumn;
  private String maskCharacter;
  private int countN;
  private String direction;

  @Override
  public void initialize(TransformationContext context) throws Exception {
    parseDirective(context);
  }

  private void parseDirective(TransformationContext context) {
    String commandLine = context.getDirective().getWholeCommandLine();
    if (commandLine == null) {
      throw new IllegalArgumentException("Directive command line is null.");
    }
    String[] splits = commandLine.split(" ");
    if (splits.length != 5) {
      throw new IllegalArgumentException("Directive is missing some parts or containing more parts," +
                                           "Expected: mask column_name direction mask_char n, " +
                                           "given directive: " + commandLine);
    }
    if (!NAME.equals(splits[0])) {
      throw new IllegalArgumentException("Directive is not a mask transformation. Usage: mask column_name " +
                                           "from_position to_position.");
    }

    srcColumn = splits[1];
    direction = splits[2];
    if (!(direction.equalsIgnoreCase(RIGHT_DIRECTION) || direction.equalsIgnoreCase(LEFT_DIRECTION))) {
      throw new IllegalArgumentException(String.format("masking direction should be %s or %s",
                                                       RIGHT_DIRECTION, LEFT_DIRECTION));
    }
    
    maskCharacter = splits[3];
    if (maskCharacter.length() != 1) {
      throw new IllegalArgumentException(String.format("masking_character: %s is not a character", maskCharacter));
    }

    try {
      countN = Integer.parseInt(splits[4]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("n is not an integer.", e);
    }
  }

  @Override
  public void transformValue(MutableRowValue rowValue) throws Exception {
    String value;
    try {
      value = (String) rowValue.getColumnValue(srcColumn);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(String.format("Column %s is supposed to have string value.", srcColumn), e);
    }
    if (value == null) {
      return;
    }

    if (value.length() <= countN) {
      return;
    }
    StringBuilder maskedValue = new StringBuilder(value);
    if (direction.equalsIgnoreCase(RIGHT_DIRECTION)) {
      maskedValue.replace(0, value.length() - countN, Strings.repeat(maskCharacter, value.length() - countN));
    } else {
      maskedValue.replace(countN, value.length(), Strings.repeat(maskCharacter, value.length() - countN));
    }

    rowValue.setColumnValue(srcColumn, maskedValue.toString());
  }

  @Override
  public void transformSchema(MutableRowSchema rowSchema) throws Exception {
    //verify whether the filed is string
    Schema.Field field = rowSchema.getField(srcColumn);
    Schema requiredSchema = Schema.of(Schema.Type.STRING);
    if (requiredSchema.equals(field.getSchema()) ||
      (field.getSchema().isNullable() && (requiredSchema.equals(field.getSchema().getNonNullable())))) {
      return;
      //no schema changes
    }
    throw new IllegalArgumentException(String.format("Field %s is supposed to be string.", srcColumn));
  }
}

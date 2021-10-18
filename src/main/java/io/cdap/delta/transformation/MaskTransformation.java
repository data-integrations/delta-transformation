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

/**
 * A Transformation that mask certain positions of the value
 */
@Plugin(type = Transformation.PLUGIN_TYPE)
@Name(MaskTransformation.NAME)
public class MaskTransformation implements Transformation {

  public static final String NAME = "mask";
  public static final String MASK_CHARACTER = "*";
  private String srcColumn;
  private int fromPos;
  private int toPos;

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
    if (splits.length != 4) {
      throw new IllegalArgumentException("Directive should have 4 arguments. Usage: mask column_name from_position " +
                                           "to_position.");
    }
    if (!NAME.equals(splits[0])) {
      throw new IllegalArgumentException("Directive is not a mask transformation. Usage: mask column_name " +
                                           "from_position to_position.");
    }

    srcColumn = splits[1];
    fromPos = Integer.parseInt(splits[2]);
    toPos = Integer.parseInt(splits[3]);
    if (fromPos > toPos) {
      throw new IllegalArgumentException("to_position is greater than from_position.");
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
    if (value.length() <= fromPos) {
      return;
    }
    StringBuilder maskedValue = new StringBuilder(value);
    if (value.length() < toPos) {
      maskedValue.replace(fromPos, value.length(), Strings.repeat(MASK_CHARACTER, value.length() - fromPos));
      rowValue.setColumnValue(srcColumn, maskedValue.toString());
      return;
    }
    maskedValue.replace(fromPos, toPos, Strings.repeat(MASK_CHARACTER, toPos - fromPos));
    rowValue.setColumnValue(srcColumn, maskedValue.toString());
    return;
  }

  @Override
  public void transformSchema(MutableRowSchema rowSchema) throws Exception {
    //verify whether the filed is string
    Schema.Field field = rowSchema.getField(srcColumn);
    if (!(Schema.of(Schema.Type.STRING).equals(field.getSchema()))) {
      throw new IllegalArgumentException(String.format("Field %s is supposed to be string.", srcColumn));
    }
    //no schema changes
    return;
  }
}

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

import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.transformation.api.MutableRowSchema;
import io.cdap.transformation.api.MutableRowValue;
import io.cdap.transformation.api.Transformation;
import io.cdap.transformation.api.TransformationContext;

/**
 * Transformation that convert the value to stirng.
 */
@Plugin(type = Transformation.PLUGIN_TYPE)
@Name(ToStringTransformation.NAME)
public class ToStringTransformation implements Transformation {

  public static final String NAME = "to-string";
  private String srcColumn;

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
    if (splits.length != 2) {
      throw new IllegalArgumentException("Directive should have 2 arguments. Usage: to-string column_name.");
    }
    if (!NAME.equals(splits[0])) {
      throw new IllegalArgumentException("Directive is not a to string transformation. Usage: to-string column_name.");
    }

    srcColumn = splits[1];
  }

  @Override
  public void transformValue(MutableRowValue rowValue) throws Exception {
    Object value = rowValue.getColumnValue(srcColumn);
    if (value == null) {
      return;
    }
    rowValue.setColumnValue(srcColumn, value.toString());
    return;
  }

  @Override
  public void transformSchema(MutableRowSchema rowSchema) throws Exception {
    rowSchema.setField(Schema.Field.of(srcColumn, Schema.of(Schema.Type.STRING)));
    return;
  }
}

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

package io.cdap.delta.transformation.plugins;

import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.transformation.api.MutableRowSchema;
import io.cdap.transformation.api.MutableRowValue;
import io.cdap.transformation.api.Transformation;
import io.cdap.transformation.api.TransformationContext;

/**
 * Rename transformation
 */
@Plugin(type = Transformation.PLUGIN_TYPE)
@Name(RenameTransformation.NAME)
public class RenameTransformation implements Transformation {
  public static final String NAME = "rename";
  private String fromColumn;
  private String toColumn;

  @Override
  public void initialize(TransformationContext context) throws Exception {
    parseDirective(context);
  }

  private void parseDirective(TransformationContext context) {
    String commandLine = context.getDirective().getWholeCommandLine();
    if (commandLine == null) {
      throw new IllegalArgumentException("Directive command line is null.");
    }
    String[] splits = commandLine.split("\\s+");
    if (splits.length != 3) {
      throw new IllegalArgumentException("Directive should have two arguments. Usage: rename old_column_name " +
                                            "new_column_name");
    }
    if (!NAME.equals(splits[0])) {
      throw new IllegalArgumentException("Directive is not a rename transformation. Usage: rename old_column_name " +
                                           "new_column_name");
    }

    fromColumn = splits[1];
    toColumn = splits[2];
  }

  @Override
  public void transformValue(MutableRowValue rowValue) throws Exception {
    rowValue.renameColumn(fromColumn, toColumn);
  }

  @Override
  public void transformSchema(MutableRowSchema rowSchema) throws Exception {
    rowSchema.renameField(fromColumn, toColumn);
  }
}

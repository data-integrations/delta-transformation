/*
 * Copyright © 2022 Cask Data, Inc.
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
import io.cdap.transformation.api.MutableRowSchema;
import io.cdap.transformation.api.MutableRowValue;
import io.cdap.transformation.api.Transformation;
import io.cdap.transformation.api.TransformationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tink transformation
 */
@Plugin(type = Transformation.PLUGIN_TYPE)
@Name(TinkTransformation.NAME)
public class TinkTransformation implements Transformation {
  private static final Logger LOG = LoggerFactory.getLogger(TinkTransformation.class);
  public static final String NAME = "tink";

  @Override
  public void initialize(TransformationContext context) throws Exception {
    parseDirective(context);
  }

  private void parseDirective(TransformationContext context) {
    LOG.info("FAKE TINK: initialize with CMD: " + context.getDirective().getWholeCommandLine());
  }

  @Override
  public void transformSchema(MutableRowSchema mutableRowSchema) throws Exception {

  }

  @Override
  public void transformValue(MutableRowValue mutableRowValue) throws Exception {

  }
}

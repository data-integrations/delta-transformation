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

import io.cdap.transformation.api.Directive;
import io.cdap.transformation.api.MutableRowSchema;
import io.cdap.transformation.api.MutableRowValue;
import io.cdap.transformation.api.TransformationContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RenameTransformationTest {

  private RenameTransformation rename = new RenameTransformation();

  @Test
  void testInitializeWithNullContext() throws Exception {
    assertThrows(NullPointerException.class, () -> rename.initialize(null));
  }

  @Test
  void testInitializeWithNullDirective() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    when(context.getDirective()).thenReturn(null);

    assertThrows(NullPointerException.class, () -> rename.initialize(context));
  }

  @Test
  void testInitializeWithNullCommandLine() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn(null);
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> rename.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectArgumentLength() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("rename column");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> rename.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectDirectiveName() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("renames column 0 5");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> rename.initialize(context));
  }

  @Test
  void testTransformSchema() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("rename from to");
    when(context.getDirective()).thenReturn(directive);
    rename.initialize(context);
    MutableRowSchema schema = mock(MutableRowSchema.class);
    rename.transformSchema(schema);
    verify(schema, never()).setField(any());
    verify(schema, never()).getField(any());
    ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
    verify(schema, times(1)).renameField(from.capture(), to.capture());
    assertEquals("from", from.getValue());
    assertEquals("to", to.getValue());
  }

  @Test
  void testTransformValue() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("rename from to");
    when(context.getDirective()).thenReturn(directive);
    rename.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    rename.transformValue(value);
    ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
    verify(value, times(1)).renameColumn(from.capture(), to.capture());
    verify(value, never()).getColumnValue(matches("column"));
    verify(value, never()).setColumnValue(any(), any());
    assertEquals("from", from.getValue());
    assertEquals("to", to.getValue());
  }
}

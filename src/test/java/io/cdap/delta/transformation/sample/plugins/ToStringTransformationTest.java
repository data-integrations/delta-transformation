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

package io.cdap.delta.transformation.sample.plugins;

import io.cdap.cdap.api.data.schema.Schema;
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

class ToStringTransformationTest {
  private ToStringTransformation toString = new ToStringTransformation();

  @Test
  void testInitializeWithNullContext() throws Exception {
    assertThrows(NullPointerException.class, () -> toString.initialize(null));
  }

  @Test
  void testInitializeWithNullDirective() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    when(context.getDirective()).thenReturn(null);

    assertThrows(NullPointerException.class, () -> toString.initialize(context));
  }

  @Test
  void testInitializeWithNullCommandLine() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn(null);
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> toString.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectArgumentLength() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("to-String");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> toString.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectDirectiveName() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("to-strings column");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> toString.initialize(context));
  }

  @Test
  void testTransformSchema() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("to-string column");
    when(context.getDirective()).thenReturn(directive);
    toString.initialize(context);
    MutableRowSchema schema = mock(MutableRowSchema.class);
    toString.transformSchema(schema);
    verify(schema, never()).renameField(any(), any());
    verify(schema, never()).getField(any());
    ArgumentCaptor<Schema.Field> columnField = ArgumentCaptor.forClass(Schema.Field.class);
    verify(schema, times(1)).setField(columnField.capture());
    Schema.Field field = columnField.getValue();
    assertEquals("column", field.getName());
    assertEquals(Schema.of(Schema.Type.STRING), field.getSchema());
  }

  @Test
  void testTransformValue() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("to-string column");
    when(context.getDirective()).thenReturn(directive);
    toString.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    when(value.getColumnValue(matches("column"))).thenReturn(true);
    toString.transformValue(value);
    ArgumentCaptor<String> columnName = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object> columnValue = ArgumentCaptor.forClass(Object.class);
    verify(value, times(1)).setColumnValue(columnName.capture(), columnValue.capture());
    verify(value, times(1)).getColumnValue(matches("column"));
    verify(value, never()).renameColumn(any(), any());
    assertEquals("column", columnName.getValue());
    assertEquals("true", columnValue.getValue());
  }
}

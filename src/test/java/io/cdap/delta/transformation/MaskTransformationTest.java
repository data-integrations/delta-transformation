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

public class MaskTransformationTest {

  private MaskTransformation mask = new MaskTransformation();

  @Test
  void testInitializeWithNullContext() throws Exception {
    assertThrows(NullPointerException.class, () -> mask.initialize(null));
  }

  @Test
  void testInitializeWithNullDirective() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    when(context.getDirective()).thenReturn(null);

    assertThrows(NullPointerException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithNullCommandLine() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn(null);
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectArgumentLength() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectDirectiveName() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("masks column 0 5");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectFirstArgumentType() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column a 5");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithIncorrectSecondArgumentType() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 0 a");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testInitializeWithFirstArgumentGreaterThanSecondArgument() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 5 0");
    when(context.getDirective()).thenReturn(directive);
    assertThrows(IllegalArgumentException.class, () -> mask.initialize(context));
  }

  @Test
  void testTransformSchema() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 0 5");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowSchema schema = mock(MutableRowSchema.class);
    when(schema.getField(matches("column"))).thenReturn(Schema.Field.of("column",
                                                                        Schema.of(Schema.Type.STRING)));
    mask.transformSchema(schema);
    verify(schema, never()).setField(any());
    verify(schema, times(1)).getField(matches("column"));
    verify(schema, never()).renameField(any(), any());
  }

  @Test
  void testTransformValue() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 0 5");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    when(value.getColumnValue(matches("column"))).thenReturn("abcdefghijk");
    ArgumentCaptor<String> columnName = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object> columnValue = ArgumentCaptor.forClass(Object.class);
    mask.transformValue(value);
    verify(value, times(1)).setColumnValue(columnName.capture(), columnValue.capture());
    verify(value, times(1)).getColumnValue(matches("column"));
    verify(value, never()).renameColumn(any(), any());
    assertEquals("column", columnName.getValue());
    assertEquals("*****fghijk", columnValue.getValue());
  }

  @Test
  void testTransformValueFromGreaterThanLength() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 4 5");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    when(value.getColumnValue(matches("column"))).thenReturn("abcd");
    mask.transformValue(value);
    verify(value, never()).setColumnValue(any(), any());
    verify(value, times(1)).getColumnValue(matches("column"));
    verify(value, never()).renameColumn(any(), any());
  }

  @Test
  void testTransformValueToGreaterThanLength() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 4 8");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    when(value.getColumnValue(matches("column"))).thenReturn("abcdefg");
    ArgumentCaptor<String> columnName = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Object> columnValue = ArgumentCaptor.forClass(Object.class);
    mask.transformValue(value);
    verify(value, times(1)).setColumnValue(columnName.capture(), columnValue.capture());
    verify(value, times(1)).getColumnValue(matches("column"));
    verify(value, never()).renameColumn(any(), any());

    assertEquals("column", columnName.getValue());
    assertEquals("abcd***", columnValue.getValue());
  }

  @Test
  void testTransformNonStringField() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 0 5");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowSchema schema = mock(MutableRowSchema.class);
    when(schema.getField(matches("column"))).thenReturn(Schema.Field.of("column",
                                                                        Schema.of(Schema.Type.INT)));

    assertThrows(IllegalArgumentException.class, () -> mask.transformSchema(schema));
  }

  @Test
  void testTransformNonStringValue() throws Exception {
    TransformationContext context = mock(TransformationContext.class);
    Directive directive = mock(Directive.class);
    when(directive.getWholeCommandLine()).thenReturn("mask column 4 5");
    when(context.getDirective()).thenReturn(directive);
    mask.initialize(context);
    MutableRowValue value = mock(MutableRowValue.class);
    when(value.getColumnValue(matches("column"))).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> mask.transformValue(value));
  }
}

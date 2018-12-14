/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.text;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spongepowered.api.text.action.TextActions.insertText;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.text.impl.TextImpl;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

@RunWith(LaunchWrapperTestRunner.class)
public class TextTest {

    @Before
    public void initialize() throws Exception {
    }

    @Test
    public void testTextOf() {
        Text text = Text.of(TextColors.RED, "Red");
        assertThat(text.toPlain(), is("Red"));

        text = findText(text, "Red");
        assertThat(text.getColor(), is(TextColors.RED));
        assertTrue(text.getStyle().isEmpty());
        assertThat(text.getChildren(), empty());
    }

    @Test
    public void testNestedTextOf() {
        Text text = Text.of(TextColors.RED, "Red", TextColors.YELLOW, "Yellow");
        assertThat(text.toPlain(), is("RedYellow"));

        Text red = findText(text, "Red");
        assertThat(red.toPlain(), is("Red"));
        assertThat(red.getColor(), is(TextColors.RED));

        Text yellow = findText(text, "Yellow");
        assertThat(yellow.toPlain(), is("Yellow"));
        assertThat(yellow.getColor(), is(TextColors.YELLOW));
    }

    @Test
    public void testSimpleCompositeText() {
        Text text = Text.of(TextColors.YELLOW, Text.of("White"));
        assertThat(text.toPlain(), is("White"));

        text = findText(text, "White");
        assertThat(text.getColor(), is(TextColors.YELLOW));
    }

    @Test
    public void testCompositeText() {
        Text text = Text.of(TextColors.GREEN, insertText("Welcome Spongie!"), "Welcome ", Text.of(TextColors.YELLOW, "Spongie"), " to the server!");
        assertThat(text.toPlain(), is("Welcome Spongie to the server!"));

        Text welcome = findText(text, "Welcome");
        assertThat(welcome.getColor(), is(TextColors.GREEN));
        assertThat(welcome.getShiftClickAction().get(), is(insertText("Welcome Spongie!")));

        Text spongie = findText(text, "Spongie");
        assertThat(spongie.getColor(), is(TextColors.YELLOW));
        assertThat(spongie.getShiftClickAction().get(), is(insertText("Welcome Spongie!")));

        Text server = findText(text, "server");
        assertThat(server.getColor(), is(TextColors.GREEN));
        assertThat(server.getShiftClickAction().get(), is(insertText("Welcome Spongie!")));
    }

    private static Text findText(Text root, String text) {
        for (Text t : root.withChildren()) {
            if (t instanceof LiteralText && ((LiteralText) t).getContent().contains(text)) {
                return t;
            }
        }

        fail("No text with content '" + text + "' found");
        throw new AssertionError(); // Should never happen
    }

    @Test
    public void testCompactedTextOf() {
        Text text = Text.ofCompacted("Hello", " ", "there");
        assertThat(text.toPlain(), is("Hello there"));
    }

    @Test
    public void testCompactedComposite() {
        Text text = Text.ofCompacted(Text.of("Yay "), Text.of("for "), Text.of("compacted"));
        assertThat(text.toPlain(), is("Yay for compacted"));
    }

    @Test
    public void testAppendCompactedEmpty() {
        Text baseText = Text.of("This is some text");
        Text text = baseText.toBuilder().setCompact(true).append(Text.empty()).build();
        assertThat(text, is(baseText));
    }

    @Test
    public void noChildrenCompact() {
        Text text = Text.of(TextColors.RED, "Foo");
        assertThat(text.compact(), is(text));
    }

    @Test
    public void mergeableSameFormat() {
        Optional<TextImpl> merged = ((TextImpl)Text.of(TextColors.RED, "Foo")).merge(Text.of(TextColors.RED, "Bar"));
        assertTrue(merged.isPresent());
        assertThat(merged.get(), is(Text.of(TextColors.RED, "FooBar")));
    }

    @Test
    public void singleChildCompacted() {
        Text text = Text.of(TextColors.RED, "Foo", Text.of(TextColors.RED, "Bar"));
        assertThat(text.compact(), is(Text.of(TextColors.RED, "FooBar")));
    }

    @Test
    public void twoChildCompacted() {
        Text text = Text.of(TextColors.RED, Text.of("Foo"), Text.of("Bar")).compact();
        assertThat(text, is(Text.of(TextColors.RED, "FooBar")));
    }
}

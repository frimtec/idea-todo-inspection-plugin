package com.github.frimtec.idea.plugin.todoinspection;

import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OptionDialogHelper {
    private OptionDialogHelper() {
        throw new AssertionError("Not instantiable");
    }

    static JComponent createOptionsPanel(List<Option> options) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            constraints.gridx = 0;
            constraints.gridy = i;
            panel.add(new JLabel(option.label()), constraints);
            constraints.gridx = 1;
            constraints.gridy = i;
            panel.add(option.component(), constraints);
        }
        return panel;
    }

    interface Option {
        String label();
        JComponent component();
    }

    static Option textOption(String label, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
        return new StringOption(label, propertyAccessor, propertySetter, false);
    }

    static Option secretOption(String label, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
        return new StringOption(label, propertyAccessor, propertySetter, true);
    }

    static Option booleanOption(String label, Supplier<Boolean> propertyAccessor, Consumer<Boolean> propertySetter) {
        return new BooleanOption(label, propertyAccessor, propertySetter);
    }

    private record StringOption(String label, Supplier<String> propertyAccessor, Consumer<String> propertySetter, boolean secret) implements Option {

        @Override
        public JComponent component() {
            JTextComponent field = secret ? new JPasswordField(propertyAccessor.get()) : new JTextField(propertyAccessor.get());
            field.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                public void textChanged(@NotNull DocumentEvent event) {
                    propertySetter.accept(field.getText());
                }
            });
            return field;
        }
    }

    private record BooleanOption(String label, Supplier<Boolean> propertyAccessor, Consumer<Boolean> propertySetter) implements Option {

        @Override
        public JComponent component() {
            JCheckBox field = new JCheckBox("", propertyAccessor.get());
            field.addActionListener(e -> propertySetter.accept(field.isSelected()));
            return field;
        }
    }

}

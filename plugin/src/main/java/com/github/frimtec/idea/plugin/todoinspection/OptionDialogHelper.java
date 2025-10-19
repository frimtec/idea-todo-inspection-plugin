package com.github.frimtec.idea.plugin.todoinspection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;
import com.intellij.ui.DocumentAdapter;

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
            panel.add(new JLabel(option.label), constraints);
            constraints.gridx = 1;
            constraints.gridy = i;
            panel.add(createOptionField(option.propertyAccessor, option.propertySetter), constraints);
        }
        return panel;
    }

    private static JTextField createOptionField(Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
        JTextField field = new JTextField(propertyAccessor.get());
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            public void textChanged(@NotNull DocumentEvent event) {
                propertySetter.accept(field.getText());
            }
        });
        return field;
    }

    static final class Option {
        private final String label;
        private final Supplier<String> propertyAccessor;
        private final Consumer<String> propertySetter;

        private Option(String label, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
            this.label = label;
            this.propertyAccessor = propertyAccessor;
            this.propertySetter = propertySetter;
        }

        static Option create(String label, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
            return new Option(label, propertyAccessor, propertySetter);
        }
    }

}

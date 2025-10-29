package com.github.frimtec.idea.plugin.todoinspection;

import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
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
        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < options.size(); i++) {
            c.gridy = i;
            Option option = options.get(i);
            if (option instanceof Separator) {
                c.gridx = 0;
                c.gridwidth = 2;
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = JBUI.insets(4, 0);
                panel.add(option.component(), c);
            } else {
                c.gridwidth = 1;
                c.anchor = GridBagConstraints.LINE_START;
                c.insets = JBUI.insets(2, 4);

                c.gridx = 0;
                c.weightx = 0.0;
                c.fill = GridBagConstraints.NONE;
                panel.add(new JLabel(option.label()), c);

                c.gridx = 1;
                c.weightx = 1.0;
                c.fill = GridBagConstraints.HORIZONTAL;
                panel.add(option.component(), c);
            }
        }
        return panel;
    }
    interface Option {
        String label();

        JComponent component();
    }

    static Option textOption(String label, String description, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
        return new StringOption(label, description, propertyAccessor, propertySetter, false);
    }

    static Option secretOption(String label, String description, Supplier<String> propertyAccessor, Consumer<String> propertySetter) {
        return new StringOption(label, description, propertyAccessor, propertySetter, true);
    }

    static Option booleanOption(String label, String checkedText, String uncheckedText, Supplier<Boolean> propertyAccessor, Consumer<Boolean> propertySetter) {
        return new BooleanOption(label, checkedText, uncheckedText, propertyAccessor, propertySetter);
    }

    static Option separator() {
        return new Separator();
    }

    static Option action(String label, Consumer<JButton> actionListener) {
        return new Action(label, actionListener);
    }

    private record StringOption(String label, String description, Supplier<String> propertyAccessor,
                                Consumer<String> propertySetter,
                                boolean secret) implements Option {

        @Override
        public JComponent component() {
            JTextComponent field = secret ? new JPasswordField(propertyAccessor.get()) : new JTextField(propertyAccessor.get());
            field.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                public void textChanged(@NotNull DocumentEvent event) {
                    propertySetter.accept(field.getText());
                }
            });
            field.setToolTipText(this.description);
            return field;
        }
    }

    private record BooleanOption(String label, String checkedText, String uncheckedText,
                                 Supplier<Boolean> propertyAccessor,
                                 Consumer<Boolean> propertySetter) implements Option {

        @Override
        public JComponent component() {
            Boolean checked = propertyAccessor.get();
            JCheckBox field = new JCheckBox(text(checked), checked);
            field.addActionListener(e -> {
                propertySetter.accept(field.isSelected());
                field.setText(text(field.isSelected()));
            });
            return field;
        }

        private String text(boolean checked) {
            return checked ? checkedText : uncheckedText;
        }
    }

    private static class Separator implements Option {

        @Override
        public String label() {
            return "";
        }

        @Override
        public JComponent component() {
            return new JSeparator();
        }
    }

    private record Action(String label, Consumer<JButton> actionListener) implements Option {

        @Override
        public String label() {
            return "";
        }

        @Override
        public JComponent component() {
            JButton button = new JButton(this.label);
            button.addActionListener(e -> actionListener.accept(button));
            return button;
        }
    }

}

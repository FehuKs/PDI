package src.ui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Category navigation and its tool palette share the same Actions as the desktop menus. */
public final class ToolSidebar extends JPanel {
    public record Group(String name, LineIcon.Kind icon, List<Action> tools) {}

    private final List<Group> groups;
    private final List<AppButton> navigation = new ArrayList<>();
    private final JPanel palette = Theme.panel(new BorderLayout(0, Theme.MD), Theme.SIDEBAR);
    private final JLabel brand = Theme.label("PDI", Theme.SECTION, Theme.TEXT);
    private final JLabel caption = Theme.label("FERRAMENTAS", Theme.SMALL, Theme.MUTED);
    private final JLabel paletteTitle = Theme.label("", Theme.SECTION, Theme.TEXT);
    private final JButton collapse;
    private final JLabel footer = Theme.label("", Theme.SMALL, Theme.MUTED);
    private boolean compact;
    private int selected = 1;

    public ToolSidebar(List<Group> groups, Runnable onCollapse) {
        super(new BorderLayout(0, Theme.LG));
        this.groups = List.copyOf(groups);
        setName("sidebar");
        setBackground(Theme.SIDEBAR);
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                        new EmptyBorder(Theme.LG, Theme.MD, Theme.LG, Theme.MD)));
        JPanel heading = Theme.panel(new BorderLayout(), Theme.SIDEBAR);
        brand.setBorder(new EmptyBorder(0, Theme.MD, 0, 0));
        heading.add(brand, BorderLayout.CENTER);
        collapse = new AppButton("", LineIcon.Kind.SIDEBAR, AppButton.Style.GHOST);
        collapse.setName("sidebar.toggle");
        collapse.setToolTipText("Recolher ferramentas · " + Theme.shortcutHint("B"));
        collapse.getAccessibleContext().setAccessibleName("Recolher ferramentas");
        collapse.addActionListener(e -> onCollapse.run());
        heading.add(collapse, BorderLayout.EAST);
        JPanel top = Theme.panel(new BorderLayout(0, Theme.LG), Theme.SIDEBAR);
        top.add(heading, BorderLayout.NORTH);
        JPanel nav = Theme.panel(new GridLayout(0, 1, 0, Theme.XS), Theme.SIDEBAR);
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            int index = i;
            AppButton button =
                    new AppButton(group.name(), group.icon(), AppButton.Style.NAVIGATION);
            button.setName("nav." + i);
            button.setToolTipText(group.name());
            button.addActionListener(e -> select(index, button));
            navigation.add(button);
            nav.add(button);
        }
        JPanel categories = Theme.panel(new BorderLayout(0, Theme.SM), Theme.SIDEBAR);
        caption.setBorder(new EmptyBorder(0, Theme.MD, 0, 0));
        categories.add(caption, BorderLayout.NORTH);
        categories.add(nav, BorderLayout.CENTER);
        top.add(categories, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        palette.setBorder(new EmptyBorder(0, Theme.XS, 0, Theme.XS));
        add(palette, BorderLayout.CENTER);
        footer.setText(Theme.shortcutHint("O") + " para abrir uma imagem");
        footer.setBorder(new EmptyBorder(Theme.SM, Theme.MD, 0, 0));
        add(footer, BorderLayout.SOUTH);
        renderPalette();
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
        brand.setVisible(!compact);
        caption.setVisible(!compact);
        palette.setVisible(!compact);
        footer.setVisible(!compact);
        for (int i = 0; i < navigation.size(); i++)
            navigation.get(i).setText(compact ? "" : groups.get(i).name());
        collapse.setToolTipText(
                compact
                        ? "Expandir ferramentas · " + Theme.shortcutHint("B")
                        : "Recolher ferramentas · " + Theme.shortcutHint("B"));
        collapse.getAccessibleContext()
                .setAccessibleName(compact ? "Expandir ferramentas" : "Recolher ferramentas");
        setPreferredSize(new Dimension(compact ? Theme.RAIL_WIDTH : Theme.SIDEBAR_WIDTH, 0));
        revalidate();
        repaint();
    }

    public boolean isCompact() {
        return compact;
    }

    private void select(int index, Component anchor) {
        selected = index;
        renderPalette();
        if (compact) {
            JPopupMenu menu = new JPopupMenu();
            menu.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Theme.BORDER),
                            new EmptyBorder(Theme.SM, Theme.SM, Theme.SM, Theme.SM)));
            for (Action action : groups.get(index).tools()) {
                JMenuItem item = new JMenuItem(action);
                item.setBorder(new EmptyBorder(Theme.SM, Theme.MD, Theme.SM, Theme.MD));
                menu.add(item);
            }
            menu.show(anchor, anchor.getWidth() + Theme.SM, 0);
        }
    }

    private void renderPalette() {
        palette.removeAll();
        paletteTitle.setText(groups.get(selected).name());
        paletteTitle.setBorder(new EmptyBorder(Theme.MD, Theme.SM, 0, 0));
        palette.add(paletteTitle, BorderLayout.NORTH);
        JPanel list = Theme.panel(new GridBagLayout(), Theme.SIDEBAR);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, Theme.XS, 0);
        for (Action action : groups.get(selected).tools()) {
            AppButton tool =
                    new AppButton(
                            (String) action.getValue(Action.NAME), null, AppButton.Style.TOOL);
            tool.setAction(action);
            tool.setHorizontalAlignment(SwingConstants.LEFT);
            tool.setName("tool." + action.getValue(Action.ACTION_COMMAND_KEY));
            list.add(tool, c);
            c.gridy++;
        }
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        list.add(Box.createGlue(), c);
        JScrollPane scroll = Theme.scroll(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        palette.add(scroll, BorderLayout.CENTER);
        for (int i = 0; i < navigation.size(); i++) navigation.get(i).setSelected(i == selected);
        palette.revalidate();
        palette.repaint();
    }
}

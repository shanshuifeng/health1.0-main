package com.healthsys.ui.medical;

import com.healthsys.common.entity.CheckItemGroup;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.dao.CheckItemDAO;
import com.healthsys.dao.CheckItemGroupDAO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckGroupDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private CheckItemGroup checkItemGroup;
    private int option = CANCEL_OPTION;

    private static final Color PRIMARY   = new Color(70, 104, 197);
    private static final Color SUCCESS   = new Color(76, 175, 80);
    private static final Color DANGER    = new Color(229, 115, 115);
    private static final Color BG_CARD   = new Color(248, 249, 252);
    private static final Color BORDER    = new Color(210, 215, 225);
    private static final Font  LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font  FIELD_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    // ── 基本信息 ──
    private JTextField        idField;
    private JTextField        nameField;
    private JTextArea         descriptionArea;
    private JTextField        priceField;
    private JTextField        dailyLimitField;
    private JToggleButton     statusOnlineBtn;
    private JToggleButton     statusOfflineBtn;

    // ── 检查项列表 ──
    private JList<CheckItem>  itemList;
    private DefaultListModel<CheckItem> fullModel;       // 始终持有全部检查项
    private Set<Long>         preservedSelectionIds;     // 过滤时保留的选中 ID
    private JLabel            selectedCountLabel;
    private JTextField        itemFilterField;

    public CheckGroupDialog(CheckItemGroup checkItemGroup) {
        this.checkItemGroup = checkItemGroup != null ? checkItemGroup : new CheckItemGroup();
        this.preservedSelectionIds = new HashSet<>();
        initializeUI();
    }

    // ═══════════════════════════════════════════════════════════════
    //  UI 构建
    // ═══════════════════════════════════════════════════════════════

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(720, 640);
        setLocationRelativeTo(null);
        setModal(true);
        setTitle(checkItemGroup.getId() == null ? "新增检查组" : "编辑检查组 — " + checkItemGroup.getName());

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(Color.WHITE);

        root.add(buildInfoCard(), BorderLayout.NORTH);
        root.add(buildItemCard(),  BorderLayout.CENTER);
        root.add(buildButtons(),  BorderLayout.SOUTH);

        add(root);
    }

    // ── 基本信息卡片 ──
    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 235)),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        // ID（只读）
        label(card, g, 0, r, "检查组ID");
        idField = field(checkItemGroup.getId() != null ? String.valueOf(checkItemGroup.getId()) : "自动生成");
        idField.setEditable(false);
        idField.setBackground(Color.WHITE);
        idField.setForeground(new Color(160, 160, 160));
        field(card, g, 1, r, idField, 1.0);
        r++;

        // 名称
        label(card, g, 0, r, "检查组名称");
        nameField = field(checkItemGroup.getName());
        field(card, g, 1, r, nameField, 1.0);
        r++;

        // 价格 + 每日限额 并排
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(BG_CARD);
        GridBagConstraints rg = new GridBagConstraints();
        rg.insets = new Insets(0, 0, 0, 12);
        rg.fill = GridBagConstraints.HORIZONTAL;

        labelRaw(row, rg, 0, "价格 (¥)");
        priceField = field(checkItemGroup.getPrice() != null ? checkItemGroup.getPrice().toString() : "");
        fieldRaw(row, rg, 1, priceField, 1.0);

        rg.insets = new Insets(0, 24, 0, 0);
        labelRaw(row, rg, 2, "每日限额");
        dailyLimitField = field(checkItemGroup.getDailyLimit() != null ? checkItemGroup.getDailyLimit().toString() : "50");
        rg.insets = new Insets(0, 0, 0, 0);
        fieldRaw(row, rg, 3, dailyLimitField, 1.0);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.weightx = 1;
        card.add(row, g);
        g.gridwidth = 1;
        r++;

        // 状态
        label(card, g, 0, r, "状态");
        JPanel statusGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusGroup.setBackground(BG_CARD);
        ButtonGroup bg = new ButtonGroup();
        statusOnlineBtn  = new JToggleButton("● 上架");
        statusOfflineBtn = new JToggleButton("● 下架");
        styleToggle(statusOnlineBtn,  true);
        styleToggle(statusOfflineBtn, false);
        bg.add(statusOnlineBtn);
        bg.add(statusOfflineBtn);
        statusGroup.add(statusOnlineBtn);
        statusGroup.add(Box.createHorizontalStrut(8));
        statusGroup.add(statusOfflineBtn);
        boolean online = checkItemGroup.getStatus() == null || checkItemGroup.getStatus() == 1;
        statusOnlineBtn.setSelected(online);
        statusOfflineBtn.setSelected(!online);
        field(card, g, 1, r, statusGroup, 1.0);
        r++;

        // 描述
        g.anchor = GridBagConstraints.NORTH;
        label(card, g, 0, r, "描述");
        descriptionArea = new JTextArea(checkItemGroup.getDescription(), 3, 20);
        descriptionArea.setFont(FIELD_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(insetBorder());
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(0, 60));
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        g.anchor = GridBagConstraints.CENTER;
        field(card, g, 1, r, descScroll, 1.0);

        return card;
    }

    // ── 检查项卡片 ──
    private JPanel buildItemCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        // 工具栏
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(BG_CARD);
        JLabel title = new JLabel("关联检查项");
        title.setFont(new Font("微软雅黑", Font.BOLD, 14));
        toolbar.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setBackground(BG_CARD);

        itemFilterField = new JTextField(10);
        itemFilterField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        itemFilterField.setBorder(insetBorder());
        itemFilterField.putClientProperty("JTextField.placeholderText", "搜索检查项…");
        actions.add(new JLabel("🔍"));
        actions.add(itemFilterField);

        JButton allBtn = new JButton("全选");
        allBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        allBtn.setBackground(Color.WHITE);
        allBtn.setFocusPainted(false);
        allBtn.addActionListener(e -> {
            itemList.setSelectionInterval(0, itemList.getModel().getSize() - 1);
            syncPreservedFromSelection();
        });
        actions.add(allBtn);

        JButton noneBtn = new JButton("取消全选");
        noneBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        noneBtn.setBackground(Color.WHITE);
        noneBtn.setFocusPainted(false);
        noneBtn.addActionListener(e -> {
            itemList.clearSelection();
            preservedSelectionIds.clear();
            updateCount();
        });
        actions.add(noneBtn);

        toolbar.add(actions, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // 列表（全量数据）
        fullModel = new DefaultListModel<>();
        for (CheckItem it : new CheckItemDAO().getAll()) fullModel.addElement(it);
        itemList = new JList<>(fullModel);
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemList.setVisibleRowCount(10);
        itemList.setFixedCellHeight(32);
        itemList.setBackground(Color.WHITE);
        itemList.setCellRenderer(new CheckItemRenderer());

        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(0, 280));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(215, 220, 228)));
        card.add(listScroll, BorderLayout.CENTER);

        // 底部
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_CARD);
        selectedCountLabel = new JLabel("已选: 0 项");
        selectedCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        selectedCountLabel.setForeground(new Color(120, 120, 120));
        footer.add(selectedCountLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("Ctrl+点击多选  Shift+点击范围选择");
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hint.setForeground(new Color(170, 170, 170));
        footer.add(hint, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        // ── 搜索过滤（关键修复：保存→过滤→恢复选中） ──
        itemFilterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
        });

        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncPreservedFromSelection();
                updateCount();
            }
        });

        // ── 编辑模式预选 ──
        if (checkItemGroup.getId() != null) {
            try {
                List<CheckItem> existing = new CheckItemGroupDAO().getCheckItemsByGroup(checkItemGroup.getId());
                for (CheckItem ex : existing) preservedSelectionIds.add(ex.getItemId());
                applyPreservedToSelection();
            } catch (Exception ignored) {}
        }

        updateCount();
        return card;
    }

    // ── 按钮 ──
    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        panel.setBackground(new Color(245, 245, 245));

        JButton cancel = CrudPanel.createStyledButton("取消", new Color(200, 200, 200));
        cancel.addActionListener(e -> dispose());

        JButton ok = CrudPanel.createStyledButton("确定", new Color(102, 204, 153));
        ok.addActionListener(e -> {
            if (validateInput()) {
                option = OK_OPTION;
                dispose();
            }
        });

        panel.add(cancel);
        panel.add(ok);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    //  过滤逻辑 — 核心修复
    // ═══════════════════════════════════════════════════════════════

    /** 输入框内容变化时：保存当前选中 ID → 重建过滤模型 → 恢复选中 */
    private void applyFilter() {
        syncPreservedFromSelection();                       // 1. 记录当前选中
        String kw = itemFilterField.getText().trim().toLowerCase();

        DefaultListModel<CheckItem> filtered = new DefaultListModel<>();
        for (int i = 0; i < fullModel.size(); i++) {
            CheckItem it = fullModel.get(i);
            if (kw.isEmpty()
                    || it.getItemName().toLowerCase().contains(kw)
                    || it.getCode().toLowerCase().contains(kw)
                    || (it.getCategory() != null && it.getCategory().toLowerCase().contains(kw))) {
                filtered.addElement(it);
            }
        }
        itemList.setModel(filtered);                        // 2. 切换模型
        applyPreservedToSelection();                        // 3. 恢复选中
        updateCount();
    }

    /** 将当前列表选中项的 ID 同步到 preservedSelectionIds */
    private void syncPreservedFromSelection() {
        for (CheckItem it : itemList.getSelectedValuesList()) {
            preservedSelectionIds.add(it.getItemId());
        }
    }

    /** 根据 preservedSelectionIds 恢复列表选中 */
    private void applyPreservedToSelection() {
        ListModel<CheckItem> model = itemList.getModel();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            if (preservedSelectionIds.contains(model.getElementAt(i).getItemId())) {
                indices.add(i);
            }
        }
        if (!indices.isEmpty()) {
            itemList.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    private void updateCount() {
        selectedCountLabel.setText("已选: " + preservedSelectionIds.size() + " 项");
    }

    // ═══════════════════════════════════════════════════════════════
    //  渲染 & 样式
    // ═══════════════════════════════════════════════════════════════

    private void styleToggle(JToggleButton btn, boolean isOnline) {
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)));
        btn.addItemListener(e -> {
            if (btn.isSelected()) {
                if (isOnline) {
                    btn.setBackground(new Color(232, 245, 233));
                    btn.setForeground(new Color(46, 125, 50));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(SUCCESS),
                            BorderFactory.createEmptyBorder(5, 16, 5, 16)));
                } else {
                    btn.setBackground(new Color(255, 235, 238));
                    btn.setForeground(new Color(198, 40, 40));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(DANGER),
                            BorderFactory.createEmptyBorder(5, 16, 5, 16)));
                }
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(120, 120, 120));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(5, 16, 5, 16)));
            }
        });
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(120, 120, 120));
    }

    private static class CheckItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean selected, boolean focused) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 12));
            row.setBackground(selected ? PRIMARY : (index % 2 == 0 ? Color.WHITE : new Color(248, 249, 252)));

            CheckItem it = (CheckItem) value;

            JLabel nameLbl = new JLabel(it.getItemName());
            nameLbl.setFont(new Font("微软雅黑", Font.BOLD, 13));
            nameLbl.setForeground(selected ? Color.WHITE : new Color(50, 50, 50));

            JLabel codeLbl = new JLabel(it.getCode());
            codeLbl.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            codeLbl.setForeground(selected ? new Color(220, 230, 255) : new Color(150, 150, 150));

            JLabel catLbl = new JLabel(it.getCategory() != null ? it.getCategory() : "");
            catLbl.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            catLbl.setForeground(selected ? new Color(200, 210, 240) : new Color(170, 170, 170));

            JLabel priceLbl = new JLabel(String.format("¥%.2f", it.getPrice()));
            priceLbl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            priceLbl.setForeground(selected ? new Color(200, 255, 200) : SUCCESS);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            left.add(nameLbl);
            left.add(codeLbl);
            left.add(catLbl);

            // 垂直居中
            JPanel centerWrap = new JPanel(new GridBagLayout());
            centerWrap.setOpaque(false);
            centerWrap.add(left);

            row.add(centerWrap, BorderLayout.CENTER);
            row.add(priceLbl, BorderLayout.EAST);
            return row;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  工具方法
    // ═══════════════════════════════════════════════════════════════

    private void label(JPanel p, GridBagConstraints g, int x, int y, String text) {
        g.gridx = x; g.gridy = y; g.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(LABEL_FONT);
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lbl, g);
    }

    private void labelRaw(JPanel p, GridBagConstraints g, int x, String text) {
        g.gridx = x; g.weightx = 0;
        JLabel lbl = new JLabel(text);
        lbl.setFont(LABEL_FONT);
        p.add(lbl, g);
    }

    private void field(JPanel p, GridBagConstraints g, int x, int y, JComponent c, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx;
        p.add(c, g);
    }

    private void fieldRaw(JPanel p, GridBagConstraints g, int x, JComponent c, double wx) {
        g.gridx = x; g.weightx = wx;
        p.add(c, g);
    }

    private JTextField field(String text) {
        JTextField f = new JTextField(text != null ? text : "");
        f.setFont(FIELD_FONT);
        f.setBorder(insetBorder());
        return f;
    }

    private javax.swing.border.Border insetBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    // ═══════════════════════════════════════════════════════════════
    //  校验 & 取值
    // ═══════════════════════════════════════════════════════════════

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入检查组名称", "提示", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        try {
            if (Double.parseDouble(priceField.getText().trim()) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的价格（非负数字）", "错误", JOptionPane.ERROR_MESSAGE);
            priceField.requestFocus();
            return false;
        }
        try {
            if (Integer.parseInt(dailyLimitField.getText().trim()) < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的每日限额（正整数）", "错误", JOptionPane.ERROR_MESSAGE);
            dailyLimitField.requestFocus();
            return false;
        }
        return true;
    }

    public int showDialog() {
        setVisible(true);
        return option;
    }

    public CheckItemGroup getCheckItemGroup() {
        if (checkItemGroup.getId() != null) checkItemGroup.setId(Long.parseLong(idField.getText()));
        checkItemGroup.setName(nameField.getText().trim());
        checkItemGroup.setDescription(descriptionArea.getText().trim());
        checkItemGroup.setPrice(Double.parseDouble(priceField.getText().trim()));
        checkItemGroup.setDailyLimit(Integer.parseInt(dailyLimitField.getText().trim()));
        checkItemGroup.setStatus(statusOnlineBtn.isSelected() ? 1 : 0);
        return checkItemGroup;
    }

    public List<Long> getSelectedItemIds() {
        syncPreservedFromSelection();
        return new ArrayList<>(preservedSelectionIds);
    }
}

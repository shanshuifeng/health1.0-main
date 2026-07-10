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
import java.util.*;

public class CheckGroupDialog extends JDialog {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;

    private CheckItemGroup checkItemGroup;
    private int option = CANCEL_OPTION;

    private static final Color PRIMARY   = new Color(70, 104, 197);
    private static final Color SUCCESS   = new Color(76, 175, 80);
    private static final Color DANGER    = new Color(255, 152, 0);
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
    private boolean           updatingSelection;         // 程序化更新标志
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
        setSize(750, 660);
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
        g.insets = new Insets(6, 8, 6, 8);
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
        rg.insets = new Insets(0, 0, 0, 10);
        rg.fill = GridBagConstraints.HORIZONTAL;
        rg.weightx = 0.5;

        // 价格标签
        rg.gridx = 0;
        rg.weightx = 0;
        JLabel priceLabel = new JLabel("价格 (¥)");
        priceLabel.setFont(LABEL_FONT);
        row.add(priceLabel, rg);

        // 价格输入框 - 自动累加，只读
        rg.gridx = 1;
        rg.weightx = 0.4;
        priceField = field("0.00");
        priceField.setEditable(false);
        priceField.setBackground(new Color(240, 240, 240));
        priceField.setForeground(new Color(100, 100, 100));
        priceField.setPreferredSize(new Dimension(100, 32));
        row.add(priceField, rg);

        // 每日限额标签
        rg.gridx = 2;
        rg.weightx = 0;
        rg.insets = new Insets(0, 20, 0, 0);
        JLabel limitLabel = new JLabel("每日限额");
        limitLabel.setFont(LABEL_FONT);
        row.add(limitLabel, rg);

        // 每日限额输入框 - 固定宽度
        rg.gridx = 3;
        rg.weightx = 0.4;
        rg.insets = new Insets(0, 0, 0, 0);
        dailyLimitField = field(checkItemGroup.getDailyLimit() != null ? checkItemGroup.getDailyLimit().toString() : "50");
        dailyLimitField.setPreferredSize(new Dimension(100, 32));
        row.add(dailyLimitField, rg);

        // 右侧填充占位
        rg.gridx = 4;
        rg.weightx = 1.0;
        row.add(Box.createHorizontalGlue(), rg);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.weightx = 1.0;
        g.insets = new Insets(6, 8, 6, 8);
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

        itemFilterField = new JTextField(12);
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
            ListModel<CheckItem> model = itemList.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                preservedSelectionIds.add(model.getElementAt(i).getItemId());
            }
            applyPreservedToSelection();
            updatePriceFromSelection();
            updateCount();
        });
        actions.add(allBtn);

        JButton noneBtn = new JButton("取消全选");
        noneBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        noneBtn.setBackground(Color.WHITE);
        noneBtn.setFocusPainted(false);
        noneBtn.addActionListener(e -> {
            itemList.clearSelection();
            preservedSelectionIds.clear();
            updatePriceFromSelection();
            updateCount();
        });
        actions.add(noneBtn);

        toolbar.add(actions, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        // 列表（全量数据）
        fullModel = new DefaultListModel<>();
        for (CheckItem it : new CheckItemDAO().getAll()) fullModel.addElement(it);
        itemList = new JList<>(fullModel);
        itemList.setVisibleRowCount(10);
        itemList.setFixedCellHeight(34);
        itemList.setBackground(Color.WHITE);
        itemList.setCellRenderer(new CheckItemRenderer());

        // 屏蔽 JList 的默认鼠标选择行为，只允许 applyPreservedToSelection() 控制选中
        itemList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (updatingSelection) super.setSelectionInterval(index0, index1);
            }
            @Override
            public void addSelectionInterval(int index0, int index1) {
                if (updatingSelection) super.addSelectionInterval(index0, index1);
            }
            @Override
            public void removeSelectionInterval(int index0, int index1) {
                if (updatingSelection) super.removeSelectionInterval(index0, index1);
            }
        });

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
        JLabel hint = new JLabel("点击右侧「选择」按钮关联检查项  |  价格自动累加");
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hint.setForeground(new Color(170, 170, 170));
        footer.add(hint, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        // ── 点击按钮区域切换选择 ──
        itemList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int idx = itemList.locationToIndex(e.getPoint());
                if (idx < 0) return;
                // 只响应右侧按钮区域（JList 右边缘 80px 内）
                int btnZoneStart = itemList.getWidth() - 80;
                if (e.getX() < btnZoneStart) return;
                CheckItem clicked = itemList.getModel().getElementAt(idx);
                if (preservedSelectionIds.contains(clicked.getItemId())) {
                    preservedSelectionIds.remove(clicked.getItemId());
                } else {
                    preservedSelectionIds.add(clicked.getItemId());
                }
                applyPreservedToSelection();
                updatePriceFromSelection();
                updateCount();
            }
        });

        // ── 搜索过滤（关键修复：保存→过滤→恢复选中） ──
        itemFilterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
        });

        // ── 编辑模式预选 ──
        if (checkItemGroup.getId() != null) {
            try {
                List<CheckItem> existing = new CheckItemGroupDAO().getCheckItemsByGroup(checkItemGroup.getId());
                for (CheckItem ex : existing) preservedSelectionIds.add(ex.getItemId());
                applyPreservedToSelection();
                updatePriceFromSelection();
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

    /** 根据 preservedSelectionIds 刷新列表选中状态（仅此方法可改变 JList 选中） */
    private void applyPreservedToSelection() {
        ListModel<CheckItem> model = itemList.getModel();
        updatingSelection = true;
        try {
            itemList.clearSelection();
            for (int i = 0; i < model.getSize(); i++) {
                if (preservedSelectionIds.contains(model.getElementAt(i).getItemId())) {
                    itemList.addSelectionInterval(i, i);
                }
            }
        } finally {
            updatingSelection = false;
        }
        itemList.repaint();
    }

    private void updateCount() {
        selectedCountLabel.setText("已选: " + preservedSelectionIds.size() + " 项");
    }

    private void updatePriceFromSelection() {
        double total = 0.0;
        for (int i = 0; i < fullModel.size(); i++) {
            CheckItem ci = fullModel.get(i);
            if (preservedSelectionIds.contains(ci.getItemId())) {
                total += (ci.getPrice() != null ? ci.getPrice() : 0.0);
            }
        }
        priceField.setText(String.format("%.2f", total));
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
        // 按钮颜色
        private static final Color BTN_SELECT   = new Color(70, 104, 197);   // 蓝色-选择
        private static final Color BTN_CANCEL   = new Color(220, 80, 80);    // 红色-取消
        private static final Color BG_SELECTED  = new Color(60, 95, 190);    // 选中行背景

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean selected, boolean focused) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 6));

            CheckItem it = (CheckItem) value;

            // 选中：深蓝底白字；未选中：交替行色
            Color bg = selected ? BG_SELECTED : (index % 2 == 0 ? Color.WHITE : new Color(248, 249, 252));
            row.setBackground(bg);

            Color nameColor = selected ? Color.WHITE : new Color(50, 50, 50);
            Color codeColor = selected ? new Color(200, 220, 255) : new Color(150, 150, 150);
            Color catColor  = selected ? new Color(180, 200, 240) : new Color(170, 170, 170);

            JLabel nameLbl = new JLabel(it.getItemName());
            nameLbl.setFont(new Font("微软雅黑", Font.BOLD, 13));
            nameLbl.setForeground(nameColor);

            JLabel codeLbl = new JLabel(it.getCode());
            codeLbl.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            codeLbl.setForeground(codeColor);

            JLabel catLbl = new JLabel(it.getCategory() != null ? it.getCategory() : "");
            catLbl.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            catLbl.setForeground(catColor);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            left.add(nameLbl);
            left.add(codeLbl);
            left.add(catLbl);

            // 右侧：价格 + 选择/取消按钮
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);

            JLabel priceLbl = new JLabel(String.format("¥%.2f", it.getPrice()));
            priceLbl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            priceLbl.setForeground(selected ? new Color(180, 255, 180) : SUCCESS);
            right.add(priceLbl);

            // 按钮-label（模拟按钮外观）
            JLabel btnLbl = new JLabel(selected ? "取消" : "选择");
            btnLbl.setFont(new Font("微软雅黑", Font.BOLD, 11));
            btnLbl.setOpaque(true);
            btnLbl.setHorizontalAlignment(SwingConstants.CENTER);
            btnLbl.setPreferredSize(new java.awt.Dimension(60, 24));
            if (selected) {
                btnLbl.setBackground(BTN_CANCEL);
                btnLbl.setForeground(Color.WHITE);
            } else {
                btnLbl.setBackground(BTN_SELECT);
                btnLbl.setForeground(Color.WHITE);
            }
            btnLbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            right.add(btnLbl);

            JPanel centerWrap = new JPanel(new GridBagLayout());
            centerWrap.setOpaque(false);
            centerWrap.add(left);

            row.add(centerWrap, BorderLayout.CENTER);
            row.add(right, BorderLayout.EAST);
            return row;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  工具方法
    // ═══════════════════════════════════════════════════════════════

    private void label(JPanel p, GridBagConstraints g, int x, int y, String text) {
        g.gridx = x; g.gridy = y; g.weightx = 0;
        g.insets = new Insets(6, 8, 6, 8);
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
        g.insets = new Insets(6, 8, 6, 8);
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
        f.setPreferredSize(new Dimension(200, 34));
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
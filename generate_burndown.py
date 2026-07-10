import matplotlib.pyplot as plt
import subprocess
from collections import defaultdict

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei']  # 用于正常显示中文标签
plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

# 直接从git命令获取提交历史
result = subprocess.run(
    ['git', 'log', '--format=%ad', '--date=short'],
    capture_output=True,
    text=True
)

# 统计每日提交数
commits_by_date = defaultdict(int)
for line in result.stdout.strip().split('\n'):
    if line:
        commits_by_date[line] += 1

# 按日期排序
sorted_dates = sorted(commits_by_date.keys())
daily_commits = [commits_by_date[d] for d in sorted_dates]

# 计算累计提交数
cumulative_commits = []
total = 0
for c in daily_commits:
    total += c
    cumulative_commits.append(total)

# 创建图表
fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(12, 10))

# 子图1：每日提交数柱状图
ax1.bar(sorted_dates, daily_commits, color='skyblue')
ax1.set_title('每日Git提交数', fontsize=14)
ax1.set_xlabel('日期', fontsize=12)
ax1.set_ylabel('提交次数', fontsize=12)
ax1.tick_params(axis='x', rotation=45)
ax1.grid(axis='y', linestyle='--', alpha=0.7)

# 子图2：累计提交燃尽图
ax2.plot(sorted_dates, cumulative_commits, marker='o', linestyle='-', color='orange', linewidth=2)
ax2.set_title('累计提交燃尽图', fontsize=14)
ax2.set_xlabel('日期', fontsize=12)
ax2.set_ylabel('累计提交次数', fontsize=12)
ax2.tick_params(axis='x', rotation=45)
ax2.grid(axis='y', linestyle='--', alpha=0.7)

plt.tight_layout()
plt.savefig('burndown_chart.png', dpi=300, bbox_inches='tight')
print("燃尽图已生成: burndown_chart.png")

# 打印统计信息
print("\n=== 项目提交统计 ===")
print(f"总提交次数: {total}")
print(f"提交天数: {len(sorted_dates)}")
print(f"平均每日提交: {total/len(sorted_dates):.2f}")
print(f"最早提交日期: {sorted_dates[0]}")
print(f"最近提交日期: {sorted_dates[-1]}")
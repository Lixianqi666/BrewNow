# 沏刻茶叶电商平台 - 数据导出规范（全角色）

## 概述
系统支持三个用户角色的数据导出功能，每个角色导出的内容和权限不同。所有导出文件统一采用 `.xlsx` 格式，命名遵循中文规范。

---

## 一、消费者（普通用户）导出规范

### 1.1 导出入口
- **页面位置**：`/orders` (订单列表) 和 `/favorites` (收藏列表) 页面右上角
- **按钮文本**：导出数据(.xlsx)
- **权限**：仅消费者可访问

### 1.2 订单导出

#### 文件名格式
```
我的订单_2026-04-27.xlsx
```

#### 文件结构（2个 Sheet）

##### Sheet 1：订单汇总
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 订单总数 | total_orders | 消费者全部订单数 |
| 待支付订单数 | pending_payment | status = PENDING_PAYMENT 的订单数 |
| 已发货订单数 | shipped | status = SHIPPED 的订单数 |
| 已完成订单数 | completed | status = COMPLETED 的订单数 |
| 已取消订单数 | cancelled | status = CANCELLED 的订单数 |
| 总消费金额 | total_amount | 所有已完成订单的金额累计 |
| 总节省金额 | total_discount | 所有折扣累计（如有） |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：订单明细
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 订单号 | order_id | 15 | 唯一标识 |
| 订单日期 | order_date | 12 | YYYY-MM-DD HH:MM:SS |
| 商品名称 | product_name | 25 | 逗号分隔（多商品） |
| 商品数量 | product_quantity | 10 | 总数量 |
| 订单金额 | order_amount | 12 | 人民币，单位：元 |
| 优惠金额 | discount_amount | 12 | 折扣或优惠，单位：元 |
| 实际支付 | actual_amount | 12 | 最终支付金额，单位：元 |
| 订单状态 | order_status | 12 | PENDING_PAYMENT / SHIPPED / COMPLETED / CANCELLED |
| 收货地址 | shipping_address | 30 | 完整地址 |
| 备注 | remarks | 20 | 订单备注 |

#### API 接口
```
GET /api/order/export
响应头：Content-Disposition: attachment; filename=我的订单_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 1.3 收藏导出

#### 文件名格式
```
我的收藏_2026-04-27.xlsx
```

#### 文件结构（1个 Sheet）

##### Sheet 1：收藏商品
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商品ID | product_id | 10 | 唯一标识 |
| 商品名称 | product_name | 25 | 茶叶商品名 |
| 商家 | merchant_name | 20 | 所属商家 |
| 分类 | category | 15 | 商品分类 |
| 市场价 | market_price | 10 | 单位：元 |
| 现价 | current_price | 10 | 单位：元 |
| 库存 | stock | 8 | 件数 |
| 评分 | rating | 6 | 0-5 星 |
| 评论数 | review_count | 8 | 条 |
| 收藏时间 | favorited_date | 12 | YYYY-MM-DD HH:MM:SS |

#### API 接口
```
GET /api/favorite/export
响应头：Content-Disposition: attachment; filename=我的收藏_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

## 二、商家导出规范

### 2.1 导出入口
- **页面位置**：`/merchant` (商家后台总览) 和 `/merchant/products` (商品管理) 页面右上角
- **按钮文本**：导出数据(.xlsx)
- **权限**：仅该商家可导出自己的数据

### 2.2 商家总览导出

#### 文件名格式
```
商家总览_[商家ID]_2026-04-27.xlsx
```
示例：`商家总览_MERCHANT_001_2026-04-27.xlsx`

#### 文件结构（2个 Sheet）

##### Sheet 1：总览统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 商家ID | merchant_id | 商家唯一标识 |
| 公司名称 | company_name | 营业执照上的公司名 |
| 商家等级 | merchant_level | 普通 / 优质 / VIP（如有分级） |
| 商品总数 | total_products | 上架商品数（未删除） |
| 库存总值 | total_stock_value | 全部库存的价值评估（单位：元） |
| 低库存商品数 | low_stock_count | 库存 ≤ 预警值的商品数 |
| 订单总数 | total_orders | 历史订单数（包括已取消） |
| 进行中订单 | pending_orders | 待发货或未完成订单数 |
| 已完成订单 | completed_orders | 已完成订单数 |
| 总收入 | total_revenue | 所有已完成订单的收入（单位：元） |
| 本月收入 | monthly_revenue | 当月收入（单位：元） |
| 客户总数 | total_customers | 购买过的唯一客户数 |
| 新客户 | new_customers_this_month | 本月新客户数 |
| 平均评分 | average_rating | 商家整体评分（0-5星） |
| 好评率 | positive_rate | 好评订单占比（%） |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：低库存商品清单
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商品ID | product_id | 10 | 唯一标识 |
| 商品名称 | product_name | 25 | 茶叶商品名 |
| 分类 | category | 15 | 商品分类 |
| SKU | sku | 15 | 库存单位代码 |
| 现价 | current_price | 10 | 单位：元 |
| 当前库存 | current_stock | 10 | 件数 |
| 预警库存 | warning_stock | 10 | 库存警戒线 |
| 缺货状态 | stock_status | 10 | 充足 / 预警 / 缺货 |
| 上次补货 | last_restock_date | 12 | YYYY-MM-DD（或"未补货"） |
| 建议补货量 | suggested_restock | 10 | 件数 |

#### API 接口
```
GET /api/merchant/dashboard/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=商家总览_[商家ID]_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 2.3 商品导出

#### 文件名格式
```
我的商品_[商家ID]_2026-04-27.xlsx
```
示例：`我的商品_MERCHANT_001_2026-04-27.xlsx`

#### 文件结构（2个 Sheet）

##### Sheet 1：商品统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 商品总数 | total_products | 上架商品数 |
| 上架中 | online_count | status = ONLINE 的商品数 |
| 下架中 | offline_count | status = OFFLINE 的商品数 |
| 分类数 | category_count | 涉及多少个分类 |
| 平均库存 | average_stock | 库存平均值 |
| 最低库存商品 | min_stock_product | 库存最少的商品名 |
| 最高库存商品 | max_stock_product | 库存最多的商品名 |
| 平均价格 | average_price | 商品平均价格（单位：元） |
| 价格区间 | price_range | "¥X - ¥Y" |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：商品明细
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商品ID | product_id | 10 | 唯一标识 |
| 商品名称 | product_name | 25 | 茶叶商品名 |
| 分类 | category | 15 | 商品分类 |
| 规格 | specification | 15 | 容量或克数 |
| SKU | sku | 15 | 库存单位代码 |
| 市场价 | market_price | 10 | 单位：元 |
| 销售价 | sale_price | 10 | 单位：元 |
| 成本价 | cost_price | 10 | 单位：元（可隐藏） |
| 当前库存 | current_stock | 10 | 件数 |
| 销售量 | sales_count | 10 | 总销售件数 |
| 月销售量 | monthly_sales | 10 | 本月销售件数 |
| 平均评分 | average_rating | 8 | 0-5 星 |
| 评论数 | review_count | 8 | 条 |
| 状态 | status | 8 | ONLINE / OFFLINE |
| 上架日期 | online_date | 12 | YYYY-MM-DD |
| 更新日期 | updated_date | 12 | YYYY-MM-DD |

#### API 接口
```
GET /api/merchant/products/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=我的商品_[商家ID]_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

## 三、管理员导出规范

### 3.1 导出入口
- **页面位置**：
  - `/admin/recommendation` (推荐统计) 页面右上角
  - `/admin/users` (用户管理) 页面右上角
  - `/admin/merchants` (商家管理) 页面右上角
  - `/admin/products` (商品管理) 页面右上角
  - `/admin/orders` (订单管理) 页面右上角
- **按钮文本**：导出数据(.xlsx)
- **权限**：仅管理员（ADMIN / SUPER_ADMIN / OPERATOR 角色）可访问

### 3.2 推荐统计导出（已实现）

#### 文件名格式
```
推荐统计_2026-04-27.xlsx
```

#### 文件结构（4个 Sheet）

##### Sheet 1：概览
（详见原有实现，列举关键指标）

##### Sheet 2：行为分布
（详见原有实现）

##### Sheet 3：策略评估
（详见原有实现）

##### Sheet 4：近期行为样本
（详见原有实现）

#### API 接口
```
GET /api/recommend/stats/export?topK=10
响应头：Content-Disposition: attachment; filename=推荐统计_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 3.3 用户管理导出

#### 文件名格式
```
用户统计_2026-04-27.xlsx
```

#### 文件结构（3个 Sheet）

##### Sheet 1：用户统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 用户总数 | total_users | 全部用户数（未删除） |
| 消费者 | consumer_count | role = CONSUMER 的用户数 |
| 商家 | merchant_count | role = MERCHANT 的用户数 |
| 活跃用户 | active_users | 最近30天有登录的用户数 |
| 新注册用户 | new_users_this_month | 本月注册用户数 |
| 已禁用用户 | disabled_users | status = INACTIVE 的用户数 |
| 今日登录 | today_login_count | 本日登录用户数 |
| 平均留存 | average_retention | 保留率（%） |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：用户明细
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 用户ID | user_id | 10 | 唯一标识 |
| 账号 | account | 15 | 登录账号 |
| 用户名 | username | 15 | 昵称 |
| 角色 | role | 8 | CONSUMER / MERCHANT |
| 电话 | phone | 12 | 手机号 |
| 邮箱 | email | 20 | 电子邮件 |
| 注册时间 | register_time | 12 | YYYY-MM-DD HH:MM:SS |
| 最后登录 | last_login_time | 12 | YYYY-MM-DD HH:MM:SS |
| 登录次数 | login_count | 8 | 累计次数 |
| 状态 | status | 8 | ACTIVE / INACTIVE |
| 备注 | remarks | 20 | 管理员备注 |

##### Sheet 3：商家用户（仅消费者的角色为MERCHANT）
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商家ID | merchant_id | 15 | 唯一标识 |
| 关联账号 | user_account | 15 | 对应user表的account |
| 公司名称 | company_name | 25 | 企业名 |
| 营业执照 | business_license | 20 | 许可证号 |
| 联系人 | contact_person | 12 | 负责人名 |
| 联系电话 | contact_phone | 12 | 手机号 |
| 营业地址 | business_address | 30 | 完整地址 |
| 审核状态 | status | 10 | PENDING / APPROVED / REJECTED / SUSPENDED |
| 申请时间 | create_time | 12 | YYYY-MM-DD |
| 审批时间 | approve_time | 12 | YYYY-MM-DD（如已批准） |

#### API 接口
```
GET /api/admin/users/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=用户统计_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 3.4 商家管理导出

#### 文件名格式
```
商家统计_2026-04-27.xlsx
```

#### 文件结构（3个 Sheet）

##### Sheet 1：商家统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 商家总数 | total_merchants | 全部商家数（未删除） |
| 待审核 | pending_count | status = PENDING 的商家数 |
| 已批准 | approved_count | status = APPROVED 的商家数 |
| 已拒绝 | rejected_count | status = REJECTED 的商家数 |
| 已暂停 | suspended_count | status = SUSPENDED 的商家数 |
| 平均评分 | average_rating | 全部商家平均评分（0-5星） |
| 总商品数 | total_products | 所有商家的上架商品数 |
| 总订单数 | total_orders | 所有商家的订单数 |
| 总交易额 | total_revenue | 所有商家的交易总额（单位：元） |
| 本月新增商家 | new_merchants_this_month | 本月申请的商家数 |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：商家详情
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商家ID | merchant_id | 15 | 唯一标识 |
| 公司名称 | company_name | 25 | 企业名 |
| 联系人 | contact_person | 12 | 负责人名 |
| 联系电话 | contact_phone | 12 | 手机号 |
| 营业地址 | business_address | 30 | 完整地址 |
| 审核状态 | status | 10 | PENDING / APPROVED / REJECTED / SUSPENDED |
| 商品数 | product_count | 8 | 上架商品数 |
| 订单数 | order_count | 8 | 历史订单数 |
| 交易额 | revenue | 12 | 单位：元 |
| 平均评分 | average_rating | 8 | 0-5 星 |
| 好评率 | positive_rate | 8 | 百分比（%） |
| 申请时间 | create_time | 12 | YYYY-MM-DD |
| 审批时间 | approve_time | 12 | YYYY-MM-DD（如已批准） |
| 备注 | remarks | 20 | 审核意见或备注 |

##### Sheet 3：商家月度统计
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商家ID | merchant_id | 15 | 唯一标识 |
| 公司名称 | company_name | 25 | 企业名 |
| 年月 | year_month | 8 | YYYY-MM |
| 本月订单 | monthly_orders | 10 | 本月订单数 |
| 本月收入 | monthly_revenue | 12 | 单位：元 |
| 本月新客户 | new_customers | 10 | 新客户数 |
| 月度评分 | monthly_rating | 8 | 当月平均评分 |

#### API 接口
```
GET /api/admin/merchants/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=商家统计_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 3.5 商品管理导出

#### 文件名格式
```
商品统计_2026-04-27.xlsx
```

#### 文件结构（3个 Sheet）

##### Sheet 1：商品统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 商品总数 | total_products | 全部上架商品数 |
| 分类数 | category_count | 涉及多少个分类 |
| 平均库存 | average_stock | 库存平均值 |
| 低库存商品 | low_stock_count | 库存 ≤ 预警值的商品数 |
| 缺货商品 | out_of_stock | 库存 = 0 的商品数 |
| 平均评分 | average_rating | 全部商品平均评分（0-5星） |
| 销售总件数 | total_sales | 全部商品的销售件数 |
| 销售总额 | total_revenue | 全部商品的销售总额（单位：元） |
| 本月热销商品 | top_product_this_month | 本月销量最高的商品名 |
| 本月总销额 | monthly_revenue | 本月销售总额（单位：元） |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：商品详情
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 商品ID | product_id | 10 | 唯一标识 |
| 商品名称 | product_name | 25 | 茶叶商品名 |
| 商家 | merchant_name | 20 | 所属商家 |
| 分类 | category | 15 | 商品分类 |
| 规格 | specification | 15 | 容量或克数 |
| 市场价 | market_price | 10 | 单位：元 |
| 销售价 | sale_price | 10 | 单位：元 |
| 当前库存 | current_stock | 10 | 件数 |
| 销售量 | sales_count | 10 | 总销售件数 |
| 平均评分 | average_rating | 8 | 0-5 星 |
| 评论数 | review_count | 8 | 条 |
| 收藏数 | favorite_count | 8 | 条 |
| 状态 | status | 8 | ONLINE / OFFLINE |
| 上架日期 | online_date | 12 | YYYY-MM-DD |

##### Sheet 3：分类统计
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 分类ID | category_id | 10 | 唯一标识 |
| 分类名称 | category_name | 20 | 分类名 |
| 商品数 | product_count | 8 | 该分类下的商品数 |
| 总库存 | total_stock | 10 | 件数 |
| 销售量 | total_sales | 10 | 总销售件数 |
| 销售额 | total_revenue | 12 | 单位：元 |
| 平均价格 | average_price | 10 | 单位：元 |
| 平均评分 | average_rating | 8 | 0-5 星 |

#### API 接口
```
GET /api/admin/products/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=商品统计_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

### 3.6 订单管理导出

#### 文件名格式
```
订单统计_2026-04-27.xlsx
```

#### 文件结构（3个 Sheet）

##### Sheet 1：订单统计
| 中文字段名 | 英文字段 | 说明 |
|-----------|---------|------|
| 订单总数 | total_orders | 全部订单数 |
| 待支付 | pending_payment | status = PENDING_PAYMENT 的订单数 |
| 已发货 | shipped | status = SHIPPED 的订单数 |
| 已完成 | completed | status = COMPLETED 的订单数 |
| 已取消 | cancelled | status = CANCELLED 的订单数 |
| 总交易额 | total_amount | 全部订单的金额累计（单位：元） |
| 平均订单额 | average_order | 平均单笔订单金额（单位：元） |
| 今日订单 | today_orders | 今日新增订单数 |
| 本月订单 | monthly_orders | 本月新增订单数 |
| 本月销售额 | monthly_revenue | 本月销售总额（单位：元） |
| 导出时间 | export_time | 导出的时间戳 |

##### Sheet 2：订单明细
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 订单号 | order_id | 15 | 唯一标识 |
| 用户 | username | 15 | 购买者用户名 |
| 商家 | merchant_name | 20 | 卖方商家 |
| 订单日期 | order_date | 12 | YYYY-MM-DD HH:MM:SS |
| 商品名称 | product_name | 25 | 逗号分隔（多商品） |
| 商品数量 | product_quantity | 10 | 总数量 |
| 订单金额 | order_amount | 12 | 单位：元 |
| 优惠金额 | discount_amount | 12 | 单位：元 |
| 实际支付 | actual_amount | 12 | 单位：元 |
| 订单状态 | order_status | 12 | PENDING_PAYMENT / SHIPPED / COMPLETED / CANCELLED |
| 收货地址 | shipping_address | 30 | 完整地址 |
| 完成日期 | completed_date | 12 | YYYY-MM-DD HH:MM:SS（如已完成） |

##### Sheet 3：日期统计
| 中文字段名 | 英文字段 | 宽度 | 说明 |
|-----------|---------|-----|------|
| 日期 | date | 10 | YYYY-MM-DD |
| 订单数 | order_count | 8 | 该日期的订单数 |
| 销售额 | daily_revenue | 12 | 单位：元 |
| 平均客单价 | average_order | 12 | 单位：元 |
| 完成订单 | completed_count | 8 | 已完成的订单数 |

#### API 接口
```
GET /api/admin/orders/export
响应头：
  - Authorization: Bearer {token}
  - Content-Disposition: attachment; filename=订单统计_2026-04-27.xlsx
返回：XSSFWorkbook 字节流 (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
```

---

## 四、通用命名规范

### 4.1 文件名规范

#### 格式模板
```
[导出功能名称]_[角色标识]_[日期时间].xlsx
```

#### 角色标识（可选）
| 角色 | 标识 | 示例文件名 |
|------|------|----------|
| 消费者 | 无 | 我的订单_2026-04-27.xlsx |
| 商家 | 商家ID | 商家总览_MERCHANT_001_2026-04-27.xlsx |
| 管理员 | 无 | 推荐统计_2026-04-27.xlsx |

#### 日期格式
```
YYYY-MM-DD  (如：2026-04-27)
```

#### 中文命名清单
| 导出场景 | 文件名 | 角色 |
|--------|-------|------|
| 我的订单 | 我的订单_YYYY-MM-DD.xlsx | 消费者 |
| 我的收藏 | 我的收藏_YYYY-MM-DD.xlsx | 消费者 |
| 商家总览 | 商家总览_[商家ID]_YYYY-MM-DD.xlsx | 商家 |
| 我的商品 | 我的商品_[商家ID]_YYYY-MM-DD.xlsx | 商家 |
| 推荐统计 | 推荐统计_YYYY-MM-DD.xlsx | 管理员 |
| 用户统计 | 用户统计_YYYY-MM-DD.xlsx | 管理员 |
| 商家统计 | 商家统计_YYYY-MM-DD.xlsx | 管理员 |
| 商品统计 | 商品统计_YYYY-MM-DD.xlsx | 管理员 |
| 订单统计 | 订单统计_YYYY-MM-DD.xlsx | 管理员 |

---

### 4.2 Sheet 标签命名规范

#### 统一规则
- 中文命名，清晰简洁，不超过 20 个字符
- 第一个 Sheet 优先使用"汇总"或"统计"
- 详细数据 Sheet 使用"明细"或"详情"
- 样本或补充数据使用"样本"、"分布"或"拆分"

#### Sheet 标签清单

| 导出类型 | Sheet 1 | Sheet 2 | Sheet 3 | Sheet 4 |
|---------|--------|--------|--------|--------|
| 消费者订单 | 订单汇总 | 订单明细 | - | - |
| 消费者收藏 | 收藏商品 | - | - | - |
| 商家总览 | 总览统计 | 低库存商品清单 | - | - |
| 商家商品 | 商品统计 | 商品明细 | - | - |
| 推荐统计 | 概览 | 行为分布 | 策略评估 | 近期行为样本 |
| 管理员用户 | 用户统计 | 用户明细 | 商家用户 | - |
| 管理员商家 | 商家统计 | 商家详情 | 商家月度统计 | - |
| 管理员商品 | 商品统计 | 商品详情 | 分类统计 | - |
| 管理员订单 | 订单统计 | 订单明细 | 日期统计 | - |

---

### 4.3 字段命名规范

#### Excel 列头（中文）
- **必填字段**用 ✓ 标记
- **可选字段**用 ○ 标记
- **隐藏字段**（如成本价）用 ⊗ 标记

#### 格式示例
```
✓ 订单号        （必填，主键）
✓ 订单日期      （必填，YYYY-MM-DD HH:MM:SS）
✓ 订单状态      （必填，枚举值：中文 + 英文）
○ 备注          （可选，长文本）
⊗ 成本价        （隐藏，仅供内部导出）
```

---

### 4.4 数据格式规范

#### 日期时间
```
日期：YYYY-MM-DD
时间：HH:MM:SS
日期时间：YYYY-MM-DD HH:MM:SS
```

#### 金额（货币）
```
单位：元
格式：12.50 （保留2位小数）
前缀：无（¥ 仅用于展示，不写入单元格）
```

#### 百分比
```
格式：80.5% 或 0.805（根据需要）
范围：0 - 100
```

#### 星级评分
```
格式：4.5 星 或 4.5/5.0
范围：0 - 5
```

#### 枚举值（中文表示）
```
订单状态：
  - PENDING_PAYMENT → 待支付
  - SHIPPED → 已发货
  - COMPLETED → 已完成
  - CANCELLED → 已取消

商品状态：
  - ONLINE → 上架中
  - OFFLINE → 下架中

商家审核状态：
  - PENDING → 待审核
  - APPROVED → 已批准
  - REJECTED → 已拒绝
  - SUSPENDED → 已暂停

库存状态：
  - 充足
  - 预警
  - 缺货
```

---

### 4.5 Excel 样式规范

#### 通用样式
- **表头行**：灰色背景 (RGB: 217, 217, 217)，粗体，居中对齐
- **数据行**：白色背景，左对齐（金额/数字右对齐）
- **行高**：默认 18 px
- **列宽**：根据字段 "宽度" 参数自动调整，最小 8，最大 30

#### 单元格格式
| 字段类型 | 格式 | 示例 |
|---------|------|------|
| 日期 | YYYY-MM-DD | 2026-04-27 |
| 时间 | HH:MM:SS | 14:30:00 |
| 日期时间 | YYYY-MM-DD HH:MM:SS | 2026-04-27 14:30:00 |
| 金额 | ¥#,##0.00 | 99.50 |
| 百分比 | 0.00% | 80.50% |
| 数字 | 0 | 100 |
| 文本 | @ | 2026-04-27 14:30:00 |

#### 冻结窗格
- 冻结首行（表头）
- 视数据量决定是否冻结首列

---

## 五、API 响应标准

### 5.1 成功响应
```
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="[中文文件名].xlsx"
Content-Length: [字节数]

[XLSX 文件二进制流]
```

### 5.2 错误响应
```
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "code": 401,
  "message": "未登录或登录过期，请重新登录",
  "success": false,
  "timestamp": "2026-04-27T14:30:00Z"
}
```

### 5.3 权限拒绝
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "code": 403,
  "message": "无权限导出该数据，仅 [角色名] 可访问",
  "success": false,
  "timestamp": "2026-04-27T14:30:00Z"
}
```

---

## 六、实现清单（按优先级）

### 第一阶段（已完成）
- ✅ 推荐统计导出（管理员）
- ✅ 商家总览导出（商家）

### 第二阶段（待实现）
- ⏳ 消费者订单导出
- ⏳ 消费者收藏导出
- ⏳ 商家商品导出
- ⏳ 管理员用户导出
- ⏳ 管理员商家导出
- ⏳ 管理员商品导出
- ⏳ 管理员订单导出

---

## 七、测试用例

### 测试账号（来自 start.sh）
```
消费者账号：
  用户名：test_consumer
  密码：123456
  
商家账号：
  用户名：test_merchant
  密码：123456
  
管理员账号：
  用户名：管理员
  电话：18075950460
  密码：123456
```

### 测试流程
1. 登录对应角色的账号
2. 导航到对应的导出页面
3. 点击 "导出数据(.xlsx)" 按钮
4. 验证文件下载
5. 在 Excel 中打开，验证以下要素：
   - 文件名是否符合中文规范
   - Sheet 标签是否正确
   - 表头是否为中文
   - 数据是否完整
   - 样式是否符合规范
   - 日期、金额、百分比等格式是否正确

---

**文档版本**：v1.0  
**最后更新**：2026-04-27  
**维护人**：沏刻茶叶电商平台开发团队

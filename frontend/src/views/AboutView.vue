<template>
  <div class="about-page">
    <section class="hero">
      <div class="hero-content">
        <span class="hero-pill">BrewNow · 沏刻茶叶电商平台</span>
        <h1>现代茶叶电商与多角色协同管理系统</h1>
        <p>
          BrewNow 聚焦“原产地茶叶线上零售”场景，覆盖消费者选购下单、商家商品管理、管理员平台运营，
          支持对业务流、权限流与界面体验的完整展示。
        </p>
      </div>
    </section>

    <section class="module-section">
      <h2>功能模块</h2>
      <div class="module-grid">
        <el-card class="module-card" shadow="hover">
          <h3>商品浏览</h3>
          <p>支持按茶类筛选、搜索与详情查看，展示茶品规格、库存与价格信息。</p>
        </el-card>
        <el-card class="module-card" shadow="hover">
          <h3>下单流程</h3>
          <p>消费者可加入购物车、选择收货地址、提交订单并完成支付流程模拟。</p>
        </el-card>
        <el-card class="module-card" shadow="hover">
          <h3>推荐系统</h3>
          <p>接入热销推荐与个性化推荐能力，支持结合用户行为、商品特征与推荐理由进行展示。</p>
        </el-card>
      </div>
    </section>

    <section class="arch-section">
      <h2>技术架构</h2>
      <el-card class="arch-card" shadow="hover">
        <div class="arch-list">
          <div><strong>后端：</strong>Spring Boot + MyBatis</div>
          <div><strong>前端：</strong>Vue 3 + Element Plus + Pinia</div>
          <div><strong>数据库：</strong>MySQL</div>
          <div><strong>认证：</strong>JWT 令牌鉴权</div>
        </div>
      </el-card>
    </section>

  </div>
</template>

<style scoped>
.about-page {
  width: min(1200px, 92vw);
  margin: 20px auto 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero {
  border-radius: 26px;
  overflow: hidden;
  background:
    linear-gradient(120deg, rgba(17, 54, 41, 0.82), rgba(39, 88, 69, 0.78)),
    url('https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1600&q=80');
  background-size: cover;
  background-position: center;
  color: #fff;
}

.hero-content {
  padding: 36px 30px;
}

.hero-pill {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.hero h1 {
  margin: 12px 0 10px;
  font-size: clamp(28px, 4.8vw, 44px);
}

.hero p {
  margin: 0;
  max-width: 840px;
  line-height: 1.8;
  opacity: 0.94;
}

.module-section h2,
.arch-section h2,
.style-section h2 {
  margin: 0 0 10px;
  color: #173a2f;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.module-card,
.arch-card,
.style-card {
  border-radius: 16px;
  border: 1px solid rgba(200, 155, 82, 0.2);
  background: linear-gradient(180deg, #fffefb, #f8f1e5);
}

.module-card h3 {
  margin: 0 0 8px;
  color: #214a38;
}

.module-card p,
.style-card p {
  margin: 0;
  color: #6f6759;
  line-height: 1.75;
}

.arch-list {
  display: grid;
  gap: 10px;
  color: #4b463d;
}

@media (max-width: 900px) {
  .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
